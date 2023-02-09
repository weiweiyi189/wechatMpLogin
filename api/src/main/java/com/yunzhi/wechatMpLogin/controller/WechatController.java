package com.yunzhi.wechatMpLogin.controller;


import com.yunzhi.wechatMpLogin.service.WeChatMpService;
import com.yunzhi.wechatMpLogin.service.WechatService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


/**
 * 微信后台服务
 * https://github.com/Wechat-Group/WxJava/tree/develop/spring-boot-starters/wx-java-miniapp-spring-boot-starter
 */
@RequestMapping("wechat")
@RestController
public class WechatController {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());


  @Autowired
  WeChatMpService weChatMpService;

  private final WechatService wechatService;

  public WechatController(WechatService wechatService) {
    this.wechatService = wechatService;
  }


  /**
   * 对接 API，注意返回类型为void，不能为String。原样返回的数据需要直接使用HttpServletResponse
   * 微信官方说明：https://developers.weixin.qq.com/doc/offiaccount/Basic_Information/Access_Overview.html
   *
   * @param signature 微信加密签名，signature结合了开发者填写的 token 参数和请求中的 timestamp 参数、nonce参数。
   * @param timestamp 时间戳
   * @param nonce     这是个随机数
   * @param echostr   随机字符串，验证成功后原样返回
   */
  @GetMapping
  public void get(@RequestParam(required = false) String signature,
                  @RequestParam(required = false) String timestamp,
                  @RequestParam(required = false) String nonce,
                  @RequestParam(required = false) String echostr,
                  HttpServletResponse response) throws IOException {
    if (!this.weChatMpService.checkSignature(timestamp, nonce, signature)) {
      this.logger.warn("接收到了未通过校验的微信消息，这可能是token配置错了，或是接收了非微信官方的请求");
      return;
    }
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(echostr);
    response.getWriter().flush();
    response.getWriter().close();
  }

  /**
   * 当设置完微信公众号的接口后，微信会把用户发送的消息，扫码事件等推送过来
   *
   * @param signature 微信加密签名，signature结合了开发者填写的 token 参数和请求中的 timestamp 参数、nonce参数。
   * @param encType 加密类型（暂未启用加密消息）
   * @param msgSignature 加密的消息
   * @param timestamp 时间戳
   * @param nonce 随机数
   * @throws IOException
   */
  @PostMapping(produces = "text/xml; charset=UTF-8")
  public void api(HttpServletRequest httpServletRequest,
                  HttpServletResponse httpServletResponse,
                  @RequestParam("signature") String signature,
                  @RequestParam(name = "encrypt_type", required = false) String encType,
                  @RequestParam(name = "msg_signature", required = false) String msgSignature,
                  @RequestParam("timestamp") String timestamp,
                  @RequestParam("nonce") String nonce) throws IOException {
    if (!this.weChatMpService.checkSignature(timestamp, nonce, signature)) {
      this.logger.warn("接收到了未通过校验的微信消息，这可能是token配置错了，或是接收了非微信官方的请求");
      return;
    }
    BufferedReader bufferedReader = httpServletRequest.getReader();
    String str;
    StringBuilder requestBodyBuilder = new StringBuilder();
    while ((str = bufferedReader.readLine()) != null) {
      requestBodyBuilder.append(str);
    }
    String requestBody = requestBodyBuilder.toString();

    this.logger.info("\n接收微信请求：[signature=[{}], encType=[{}], msgSignature=[{}],"
                    + " timestamp=[{}], nonce=[{}], requestBody=[\\n{}\\n]",
            signature, encType, msgSignature, timestamp, nonce, requestBody);

    if (this.logger.isDebugEnabled()) {
      this.logger.info("\n接收微信请求：[signature=[{}], encType=[{}], msgSignature=[{}],"
              + " timestamp=[{}], nonce=[{}], requestBody=[\\n{}\\n]",
          signature, encType, msgSignature, timestamp, nonce, requestBody);
      this.logger.info(httpServletRequest.getQueryString());
      this.logger.info(httpServletRequest.getContentType());
    }
    WxMpXmlMessage inMessage2 = WxMpXmlMessage.fromXml(requestBody);
    this.logger.info("事件为" + inMessage2.getEventKey());

    String out = null;
    if (encType == null) {
      // 明文传输的消息
      WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);
      WxMpXmlOutMessage outMessage = this.weChatMpService.route(inMessage);
      if (outMessage == null) {
        httpServletResponse.getOutputStream().write(new byte[0]);
        httpServletResponse.flushBuffer();
        httpServletResponse.getOutputStream().close();
        return;
      }

      out = outMessage.toXml();
    } else if ("aes".equals(encType)) {
      // aes加密的消息
      WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(requestBody,
          this.weChatMpService.getWxMpConfigStorage(), timestamp, nonce, msgSignature);
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("\n消息解密后内容为：\n{} ", inMessage.toString());
      }
      WxMpXmlOutMessage outMessage = this.weChatMpService.route(inMessage);
      if (outMessage == null) {
        httpServletResponse.getOutputStream().write(new byte[0]);
        httpServletResponse.flushBuffer();
        httpServletResponse.getOutputStream().close();
        return;
      }
      out = outMessage.toEncryptedXml(this.weChatMpService.getWxMpConfigStorage());
    }

    if (this.logger.isDebugEnabled()) {
      this.logger.info("\n组装回复信息：{}", out);
    }
    httpServletResponse.getOutputStream().write(out.getBytes(StandardCharsets.UTF_8));
    httpServletResponse.flushBuffer();
    httpServletResponse.getOutputStream().close();
  }


  private class LoginJsonView {
  }
}


