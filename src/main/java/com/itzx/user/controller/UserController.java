package com.itzx.user.controller;

import com.itzx.until.Result;
import com.itzx.user.biz.UserBiz;
import com.itzx.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("user")
@CrossOrigin(origins = "http://localhost:8080",maxAge = 3600)
@Slf4j
public class UserController {
    @Autowired
    private UserBiz userBiz;
    private Result result;

    //登录
    @RequestMapping(value = "/login")
    @ResponseBody
    public Result login(String uname, String upwd){
        return userBiz.login(uname, upwd);

    }
   //注册（接收 JSON 请求体）
    @PostMapping(value = "/register")
    @ResponseBody
    public Result register(@RequestBody User user){
        Result register = userBiz.register(user);
        return register;
    }
    //重置密码
    @RequestMapping(value = "/updateUser")
    @ResponseBody
    public Result updateUser( String email, String uname,String upwd) {
        return userBiz.updateUser( email, uname, upwd);
    }

    //发送重置密码验证码
    @PostMapping(value = "/sendResetPwdCode")
    @ResponseBody
    public Result sendResetPwdCode(@RequestParam String email) {
        return userBiz.sendResetPwdCode(email);
    }

    //邮箱+验证码重置密码
    @PostMapping(value = "/resetPwdByEmailCode")
    @ResponseBody
    public Result resetPwdByEmailCode(@RequestParam String email,
                                      @RequestParam String code,
                                      @RequestParam String newPwd) {
        return userBiz.resetPwdByEmailCode(email, code, newPwd);
    }
    //查询所有用户
    @RequestMapping(value = "/findUser")
    @ResponseBody
    public List<User> findUser(Integer index){
        return userBiz.findUser(index,5);
    }
    //按用户名模糊查询用户列表
    @RequestMapping(value = "/findUserMo")
    @ResponseBody
    public List<User> findUserMo(Integer index,String uname){
        return userBiz.findUserMo(uname,index,5);
    }
    //删除用户
    @RequestMapping(value = "delUser")
    @ResponseBody
    public boolean delUser(int id){
        return userBiz.delUser(id);
    }

}
