package com.yunzhi.wechatMpLogin.service;

import com.yunzhi.wechatMpLogin.entity.WeChatUser;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

/**
 * 微信公众号事件处理器
 */
public interface WeChatMpEventKeyHandler {
  /**
   * 获取是否过期
   * <p>
   *   扫描都会给个有效期，过了有效期就不在处理了
   * </p>
   */
  boolean getExpired();

  /**
   * 处理器，处理微信打码后的回调
   * @param wxMpXmlMessage 微信公众号的推送过来的消息
   * @param weChatUser 发送的公众号
   * @return 向微信用户发送消息
   */
  WxMpXmlOutMessage handle(WxMpXmlMessage wxMpXmlMessage, WeChatUser weChatUser);
}
