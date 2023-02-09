package com.yunzhi.wechatMpLogin.service;


import com.yunzhi.wechatMpLogin.entity.WeChatUser;

public interface WechatService {

  WeChatUser getOneByOpenidAndAppId(String openId, String toUser);

}
