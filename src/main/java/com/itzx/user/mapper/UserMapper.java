package com.itzx.user.mapper;

import com.itzx.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    //登录
    public User login(@Param("uname") String uname);
    //根据用户名和邮箱查询用户
    public User selectByUname(@Param("uname") String uname,@Param("email") String email);
    public User selectByUnameEmail(@Param("uname") String uname, @Param("email") String email);
    public User selectByEmail(@Param("email") String email);
    //注册
    public boolean register(User user);
    //重置密码
    public int updatePwd(@Param("email") String email, @Param("uname") String uname,@Param("upwd") String upwd);
    public int updatePwdByEmail(@Param("email") String email, @Param("upwd") String upwd);
    //查询用户列表（分页由 PageHelper 控制）
    public List<User> findUser();
    //按用户名模糊查询用户列表（分页由 PageHelper 控制）
    public List<User> findUserMo(@Param("uname") String uname);
    //删除用户
    public boolean delUser(int id);
}
