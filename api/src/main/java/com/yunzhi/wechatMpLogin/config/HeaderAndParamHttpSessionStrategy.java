package com.yunzhi.wechatMpLogin.config;

import org.springframework.session.web.http.HeaderHttpSessionStrategy;

import javax.servlet.http.HttpServletRequest;

/**
 * Header或是请求参数中的带有token的认证策略
 * 如此可以使用请求http://xxxx/xxx?x-auth-token=12324231的方法来完成认证，解决大文件下载的问题
 */
public class HeaderAndParamHttpSessionStrategy extends HeaderHttpSessionStrategy {
  /**
   * header认证关键字名称
   */
  private String headerName = "x-auth-token";

  @Override
  public String getRequestedSessionId(HttpServletRequest request) {
    String token = request.getHeader(this.headerName);
    return (token != null && !token.isEmpty()) ? token : request.getParameter(this.headerName);
  }
}
