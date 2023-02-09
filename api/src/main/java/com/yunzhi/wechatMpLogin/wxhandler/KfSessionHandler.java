package com.yunzhi.wechatMpLogin.wxhandler;

import com.yunzhi.wechatMpLogin.service.WeChatMpService;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Binary Wang
 */
@Component
public class KfSessionHandler extends AbstractHandler {
  public KfSessionHandler(WeChatMpService weChatMpService) {
    super(weChatMpService);
  }

  @Override
  public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                  Map<String, Object> context, WxMpService wxMpService,
                                  WxSessionManager sessionManager) {
    //TODO 对会话做处理
    return null;
  }

}
