package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private static final String longinUrl="https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;
    /**
     * 用户微信登陆
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wechatLogin(UserLoginDTO userLoginDTO) {
        String openid = getOpenId(userLoginDTO.getCode());
        //判断OpenId是否为空，如果为空则登陆失败（即用户的合法信不是我们来判断的，而是微信官方来判断的）
        if (openid==null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        //对于外卖系统来说是否是新用户
        User user = userMapper.getByOpenId(openid);
        if (user==null) {
            user=User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        //返回用户对象
        return user;
    }

    /**
     * 获取OpenId的私有方法
     * @param code
     * @return
     */
    private String getOpenId(String code){
        //调用微信接口服务，获得微信用户的userId
        Map<String,String> param=new HashMap<>();
        param.put("appid",weChatProperties.getAppid());
        param.put("secret",weChatProperties.getSecret());
        param.put("js_code",code);
        param.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(longinUrl, param);
        //判断OpenId是否为空，如果为空则登陆失败（即用户的合法信不是我们来判断的，而是微信官方来判断的）
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
