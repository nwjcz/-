package com.itzx.user.biz.imp;

import com.github.pagehelper.PageHelper;
import com.itzx.until.Result;
import com.itzx.user.biz.UserBiz;
import com.itzx.user.entity.User;
import com.itzx.user.mapper.UserMapper;
import com.itzx.until.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service("userBiz")
public class UserBizImpl implements UserBiz {
    //创建UserMapper的对象，调用UserMapper的接口方法4
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    // BCrypt密码编码器
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Duration RESET_CODE_TTL = Duration.ofMinutes(3);
    private static final int RESET_CODE_MAX_VERIFY_ATTEMPTS = 3;
    private static final int RESET_CODE_MAX_SEND_PER_DAY = 3;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /*
    * 登录*/
    @Override
    public Result login(String uname, String upwd) {
        // 1. 根据用户名查询用户
        User user = userMapper.login(uname);
        if (user == null) {
            return Result.error("用户名错误");
        }

        // 2. 校验密码（明文 vs 加密后的密码）
        if (!passwordEncoder.matches(upwd, user.getUpwd())) {
            return Result.error("用户名或密码错误");
        }

        // 3. 校验账号状态
        Integer status = user.getStatus();
        if (status != null && status != 0) {
            return Result.error("用户账号被冻结，请联系管理员");
        }

        // 4. 生成 JWT Token（根据用户类型设置角色，type=1为买家，type=2为管理员，type=3为商家，其它为普通用户）
        String role = "USER";
        int type = user.getType();
        if (type == 2) {
            role = "ADMIN";
        } else if (type == 3) {
            role = "MERCHANT";
        }
        String token = jwtUtils.generateToken(user.getUname(), role);

        // 5. 不把密码返回给前端
        user.setUpwd(null);

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("token", token);

        return Result.success(data);
    }
    //注册
    @Override
    public Result register(User user) {
        if (user == null || user.getUpwd() == null || user.getUname() == null) {
            return Result.error("用户名或密码不能为空");
        }
        // 1. 校验用户名是否已存在（复用selectByUname方法）
        User existUser = userMapper.selectByUname(user.getUname(),user.getEmail());
        if (existUser!= null) {
            return Result.error("用户名已存在");
        }
        // 2. 对密码进行加密存储
        user.setUpwd(passwordEncoder.encode(user.getUpwd()));

        // 3. 填充默认值（status=0，createTime=当前时间；如未指定type，则默认为1-买家）
        if (user.getType() == 0) {
            user.setType(1);
        }
        user.setStatus(0); // 状态默认为0（正常）
        user.setCreateTime(new Date(System.currentTimeMillis()));
        // 创建时间为当前时间

        // 4. 调用Mapper的注册插入方法（XML中已配置SQL）
        boolean isSuccess = userMapper.register(user);

        // 5. 根据插入结果返回Result
        if (isSuccess) {
            return Result.success("注册成功");
        } else {
            return Result.error("注册失败，请稍后重试");
        }
    }


    /*
    * 重置密码*/
    @Override
    public Result updateUser(String email, String uname, String upwd) {
        User emailUser = userMapper.selectByUnameEmail(uname, email);
        if (emailUser == null) {
            return Result.error("用户不存在,请重新输入");
        }
        // 对新密码进行加密后再更新
        String encodedPwd = passwordEncoder.encode(upwd);
        int rows = userMapper.updatePwd(email, uname, encodedPwd);
        if (rows <= 0) {
            return Result.error("重置密码失败");
        }
        return Result.success("重置密码成功");

    }

    @Override
    public Result sendResetPwdCode(String email) {
        if (email == null || email.isBlank()) {
            return Result.error("邮箱不能为空");
        }

        if (!isValidEmailFormat(email)) {
            return Result.error("邮箱格式不正确");
        }

        User user = userMapper.selectByEmail(email);
        if (user == null) {
            return Result.error("邮箱不存在");
        }

        String dailyKey = buildDailySendCountKey(email);
        Long current = stringRedisTemplate.opsForValue().increment(dailyKey);
        if (current == null) {
            return Result.error("发送失败，请稍后重试");
        }
        if (current == 1L) {
            long seconds = secondsUntilEndOfDay();
            if (seconds > 0) {
                stringRedisTemplate.expire(dailyKey, Duration.ofSeconds(seconds));
            }
        }
        if (current > RESET_CODE_MAX_SEND_PER_DAY) {
            return Result.error("该邮箱今日验证码发送次数已达上限");
        }

        String code = generate6DigitCode();
        String codeKey = buildResetCodeKey(email);
        String attemptsKey = buildResetAttemptsKey(email);

        stringRedisTemplate.opsForValue().set(codeKey, code, RESET_CODE_TTL);
        stringRedisTemplate.opsForValue().set(attemptsKey, "0", RESET_CODE_TTL);

        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                if (mailFrom != null && !mailFrom.isBlank()) {
                    message.setFrom(mailFrom);
                }
                message.setTo(email);
                message.setSubject("重置密码验证码");
                message.setText("你的重置密码验证码是：" + code + "，有效期3分钟。若非本人操作请忽略。");
                mailSender.send(message);
            }
        } catch (Exception ignored) {
        }

        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("code", code);
        data.put("expireSeconds", RESET_CODE_TTL.getSeconds());
        return Result.success(data);
    }

    @Override
    public Result resetPwdByEmailCode(String email, String code, String newPwd) {
        if (email == null || email.isBlank()) {
            return Result.error("邮箱不能为空");
        }

        if (!isValidEmailFormat(email)) {
            return Result.error("邮箱格式不正确");
        }
        if (code == null || code.isBlank()) {
            return Result.error("验证码不能为空");
        }
        if (newPwd == null || newPwd.isBlank()) {
            return Result.error("新密码不能为空");
        }

        User user = userMapper.selectByEmail(email);
        if (user == null) {
            return Result.error("邮箱不存在");
        }

        String codeKey = buildResetCodeKey(email);
        String attemptsKey = buildResetAttemptsKey(email);
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (storedCode == null || storedCode.isBlank()) {
            return Result.error("验证码已过期或不存在");
        }

        String attemptsStr = stringRedisTemplate.opsForValue().get(attemptsKey);
        int attempts = 0;
        if (attemptsStr != null && !attemptsStr.isBlank()) {
            try {
                attempts = Integer.parseInt(attemptsStr);
            } catch (NumberFormatException ignored) {
                attempts = 0;
            }
        }
        if (attempts >= RESET_CODE_MAX_VERIFY_ATTEMPTS) {
            stringRedisTemplate.delete(codeKey);
            stringRedisTemplate.delete(attemptsKey);
            return Result.error("验证码错误次数过多，请重新获取验证码");
        }

        if (!storedCode.equals(code)) {
            Long after = stringRedisTemplate.opsForValue().increment(attemptsKey);
            int left = RESET_CODE_MAX_VERIFY_ATTEMPTS - (after == null ? (attempts + 1) : after.intValue());
            if (left <= 0) {
                stringRedisTemplate.delete(codeKey);
                stringRedisTemplate.delete(attemptsKey);
                return Result.error("验证码错误次数过多，请重新获取验证码");
            }
            return Result.error("验证码错误，还可尝试" + left + "次");
        }

        String encodedPwd = passwordEncoder.encode(newPwd);
        int rows = userMapper.updatePwdByEmail(email, encodedPwd);
        if (rows <= 0) {
            return Result.error("重置密码失败");
        }

        stringRedisTemplate.delete(codeKey);
        stringRedisTemplate.delete(attemptsKey);
        return Result.success("重置密码成功");
    }

    private static String generate6DigitCode() {
        int n = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", n);
    }

    private static String buildDailySendCountKey(String email) {
        String date = LocalDate.now().toString();
        return "resetpwd:sendcnt:" + date + ":" + email;
    }

    private static String buildResetCodeKey(String email) {
        return "resetpwd:code:" + email;
    }

    private static String buildResetAttemptsKey(String email) {
        return "resetpwd:attempts:" + email;
    }

    private static long secondsUntilEndOfDay() {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDateTime endLocal = now.toLocalDate().plusDays(1).atStartOfDay();
        ZonedDateTime end = endLocal.atZone(zoneId);
        return Duration.between(now, end).getSeconds();
    }

    private static boolean isValidEmailFormat(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    @Override
    public List<User> findUser(int index, int size) {
        // 使用 PageHelper 进行分页，index 为当前页码，size 为每页条数
        PageHelper.startPage(index, size);
        return userMapper.findUser();
    }

    @Override
    public List<User> findUserMo(String uname, int index, int size) {
        // 使用 PageHelper 进行分页
        PageHelper.startPage(index, size);
        return userMapper.findUserMo(uname);
    }

    @Override
    public boolean delUser(int id) {
        return userMapper.delUser(id);
    }
}
