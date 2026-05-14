package com.itzx.signin.biz;

import com.itzx.until.Result;

public interface SignInBiz {
    Result signIn(Integer userId);

    Result getSignInStatus(Integer userId);
}
