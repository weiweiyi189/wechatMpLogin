package com.yunzhi.wechatMpLogin.service;

import com.yunzhi.wechatMpLogin.entity.User;

import javax.servlet.http.HttpSession;
import java.util.Optional;


public interface UserService {

  /**
   * 获取获取的二维码
   * @param wsLoginToken 用于登录的wsLoginToken
   * @param httpSession httpSession
   * @return 用于触发回调的uuid
   */
  String getLoginQrCode(String wsLoginToken, HttpSession httpSession);

  /**
   * 生成绑定当前用户的微信二维码
   *
   * @param sessionId sessionId
   * @return 返回图片URL地址
   */
  String generateBindQrCode(String sessionId);


  boolean checkWeChatLoginUuidIsValid(String uuid);

  User getByUsername(String name);

  /**
   * 获取登录用户
   *
   * @return 登录用户 | null
   */
  Optional<User> getCurrentLoginUser();

}
