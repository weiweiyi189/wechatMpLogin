package com.yunzhi.wechatMpLogin.service;

import com.yunzhi.wechatMpLogin.entity.WeChatUser;
import com.yunzhi.wechatMpLogin.repository.WeChatUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class WechatServiceImpl implements WechatService {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final WeChatUserRepository weChatUserRepository;


  public WechatServiceImpl(WeChatUserRepository weChatUserRepository) {
    this.weChatUserRepository = weChatUserRepository;
  }

  @Override
  public WeChatUser getOneByOpenidAndAppId(String openId, String appId) {
    return this.weChatUserRepository.findByOpenidAndAppId(openId, appId)
        .orElseGet(() -> this.weChatUserRepository.save(new WeChatUser(openId, appId)));
  }

}
