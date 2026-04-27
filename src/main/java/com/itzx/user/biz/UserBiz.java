package com.itzx.user.biz;

import com.itzx.until.Result;
import com.itzx.user.entity.User;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserBiz {
     Result login(String uname, String upwd);
     Result register(User user);
     Result updateUser(String email,String uname,String upwd);
     Result sendResetPwdCode(String email);
     Result resetPwdByEmailCode(String email, String code, String newPwd);
     List<User> findUser (int index, int size);
     List<User> findUserMo(String uname,int index,int size);
     boolean delUser(int id);

}
