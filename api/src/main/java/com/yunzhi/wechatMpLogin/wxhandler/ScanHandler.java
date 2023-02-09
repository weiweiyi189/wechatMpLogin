package com.yunzhi.wechatMpLogin.wxhandler;

import com.yunzhi.wechatMpLogin.entity.WeChatUser;
import com.yunzhi.wechatMpLogin.repository.WeChatUserRepository;
import com.yunzhi.wechatMpLogin.service.WeChatMpService;
import com.yunzhi.wechatMpLogin.service.WechatService;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Binary Wang
 */
@Service
public class ScanHandler extends AbstractHandler {
  final WeChatUserRepository weChatUserRepository;

  final WechatService wechatService;

  public ScanHandler(WeChatUserRepository weChatUserRepository,
                     WeChatMpService weChatMpService,
                     @Lazy WechatService wechatService) {
    super(weChatMpService);
    this.weChatUserRepository = weChatUserRepository;
    this.wechatService = wechatService;
  }

  @Override
  public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager) throws WxErrorException {
    WeChatUser weChatUser = this.wechatService.getOneByOpenidAndAppId(wxMessage.getFromUser(), wxMessage.getToUser());
    return this.handleKey(weChatUser, wxMessage);
  }

  /**
   * 事理key事件
   */
  private WxMpXmlOutMessage handleKey(WeChatUser weChatUser, WxMpXmlMessage wxMpXmlMessage) {
    System.out.println("scan事件");
    return this.handleByEventKey(wxMpXmlMessage.getEventKey(), weChatUser, wxMpXmlMessage);
  }
}
