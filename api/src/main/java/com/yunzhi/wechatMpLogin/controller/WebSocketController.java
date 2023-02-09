package com.yunzhi.wechatMpLogin.controller;

import com.yunzhi.wechatMpLogin.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket
 */
@Controller
public class WebSocketController {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final WebSocketService webSocketService;

  public WebSocketController(WebSocketService webSocketService) {
    this.webSocketService = webSocketService;
  }

  /**
   * 绑定wsToken(websocket认证）与 xAuthToken（httpSession认证)
   * 此后可以通过wsToken来获取xAuthToken，用于发起ws连接时获取当前的登录用户
   * 也可以通过xAuthToken来获取wsToken，用于后台主动向某个用户发送消息
   * @param xAuthToken xAuthToken
   * @param message wsToken
   */
  @MessageMapping("/ws/bind")
  public void bindToXAuthToken(@Payload String xAuthToken, Principal message) {
    String wsAuthToken = message.getName();
    this.webSocketService.bindToXAuthToken(wsAuthToken, xAuthToken);
  }
}
