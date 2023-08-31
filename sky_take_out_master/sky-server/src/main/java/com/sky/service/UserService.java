package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface UserService {

    /**
     * 用户微信登陆
     * @param userLoginDTO
     * @return
     */
    public User wechatLogin(UserLoginDTO userLoginDTO);
}
