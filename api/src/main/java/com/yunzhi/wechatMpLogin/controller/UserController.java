package com.yunzhi.wechatMpLogin.controller;


import com.fasterxml.jackson.annotation.JsonView;
import com.yunzhi.wechatMpLogin.entity.User;
import com.yunzhi.wechatMpLogin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("User")
public class UserController {
  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }



  /**
   * 生成绑定微信的二维码
   * @param httpSession session
   * @return 二维码对应的系统ID(用于触发扫码后的回调)
   */
  @GetMapping("generateBindQrCode")
  public String generateBindQrCode(HttpSession httpSession) {
    logger.info("httpSession.getId() :" + httpSession.getId());
    return this.userService.generateBindQrCode(httpSession.getId());
  }

  /**
   * 获取登录的二维码
   * @param wsAuthToken webSocket认证token
   * @param httpSession session
   * @return 二维码对应的系统ID(用于触发扫码后的回调)
   */
  @GetMapping("getLoginQrCode/{wsAuthToken}")
  public String getLoginQrCode(@PathVariable String wsAuthToken, HttpSession httpSession) {
    return this.userService.getLoginQrCode(wsAuthToken, httpSession);
  }

  @RequestMapping("login")
  @JsonView(LoginJsonView.class)
  public User login(Principal user) {
    return this.userService.getByUsername(user.getName());
  }

  @GetMapping("currentLoginUser")
  @JsonView(GetCurrentLoginUserJsonView.class)
  public User getCurrentLoginUser(HttpSession httpSession, Principal principal) {
    return principal == null ? null : this.userService.getCurrentLoginUser().get();
  }

  @GetMapping("logout")
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    logger.info("用户注销");
    // 获取用户认证信息
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // 存在认证信息，注销
    if (authentication != null) {
      new SecurityContextLogoutHandler().logout(request, response, authentication);
    }
  }

  public interface GetCurrentLoginUserJsonView extends User.NameJsonView,
          User.UsernameJsonView,
          User.WeChatUserJsonView {
  }

  public interface LoginJsonView extends User.UsernameJsonView, User.NameJsonView, User.WeChatUserJsonView {
  }

}
