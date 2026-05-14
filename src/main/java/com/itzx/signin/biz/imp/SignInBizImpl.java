package com.itzx.signin.biz.imp;

import com.itzx.coupon.entity.Coupon;
import com.itzx.coupon.entity.UserCoupon;
import com.itzx.coupon.mapper.CouponMapper;
import com.itzx.coupon.mapper.UserCouponMapper;
import com.itzx.signin.biz.SignInBiz;
import com.itzx.until.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("signInBiz")
public class SignInBizImpl implements SignInBiz {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    private static final int SIGN_IN_CYCLE_DAYS = 7;
    private static final int COUPON_EXPIRE_DAYS = 30;
    private static final Duration KEY_EXPIRE = Duration.ofDays(60);

    @Override
    @Transactional
    public Result signIn(Integer userId) {
        LocalDate today = LocalDate.now();

        if (isSignedIn(userId, today)) {
            int consecutiveDays = calcConsecutiveDays(userId, today);
            Map<String, Object> data = new HashMap<>();
            data.put("signed", true);
            data.put("consecutiveDays", consecutiveDays);
            return new Result(200, "今日已签到", data);
        }

        setSignedIn(userId, today);
        int consecutiveDays = calcConsecutiveDays(userId, today);

        Coupon awardedCoupon = null;
        if (consecutiveDays == 1 || consecutiveDays == 3 || consecutiveDays == 7) {
            List<Coupon> coupons = couponMapper.findApprovedBySignInDay(consecutiveDays);
            if (coupons != null && !coupons.isEmpty()) {
                awardedCoupon = coupons.get(0);
                UserCoupon userCoupon = new UserCoupon();
                userCoupon.setUserId(userId);
                userCoupon.setCouponId(awardedCoupon.getId());
                userCoupon.setStatus(0);
                userCoupon.setReceiveTime(LocalDateTime.now());
                userCoupon.setExpireTime(LocalDateTime.now().plusDays(COUPON_EXPIRE_DAYS));
                userCouponMapper.insert(userCoupon);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("consecutiveDays", consecutiveDays);
        data.put("awardedCoupon", awardedCoupon);
        return new Result(200, "签到成功", data);
    }

    @Override
    public Result getSignInStatus(Integer userId) {
        LocalDate today = LocalDate.now();
        boolean todaySigned = isSignedIn(userId, today);
        int consecutiveDays = todaySigned ? calcConsecutiveDays(userId, today) : calcConsecutiveDays(userId, today.minusDays(1));

        boolean broken = false;
        if (!todaySigned) {
            LocalDate yesterday = today.minusDays(1);
            boolean yesterdaySigned = isSignedIn(userId, yesterday);
            if (yesterdaySigned && consecutiveDays > 0 && consecutiveDays < SIGN_IN_CYCLE_DAYS) {
                broken = true;
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("todaySigned", todaySigned);
        data.put("consecutiveDays", todaySigned ? consecutiveDays : (broken ? consecutiveDays : 0));
        data.put("streakBroken", broken);
        return Result.success(data);
    }

    // ========== Redis Bitmap 操作 ==========

    private String signInKey(Integer userId, LocalDate date) {
        return "signin:uid:" + userId + ":" + date.format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    private boolean isSignedIn(Integer userId, LocalDate date) {
        String key = signInKey(userId, date);
        int offset = date.getDayOfMonth() - 1;
        Boolean bit = stringRedisTemplate.opsForValue().getBit(key, offset);
        return Boolean.TRUE.equals(bit);
    }

    private void setSignedIn(Integer userId, LocalDate date) {
        String key = signInKey(userId, date);
        int offset = date.getDayOfMonth() - 1;
        stringRedisTemplate.opsForValue().setBit(key, offset, true);
        stringRedisTemplate.expire(key, KEY_EXPIRE);
    }

    private int calcConsecutiveDays(Integer userId, LocalDate fromDate) {
        int count = 0;
        LocalDate cursor = fromDate;
        while (count < 31) {
            if (isSignedIn(userId, cursor)) {
                count++;
                cursor = cursor.minusDays(1);
            } else {
                break;
            }
        }
        return Math.min(count, SIGN_IN_CYCLE_DAYS);
    }
}
