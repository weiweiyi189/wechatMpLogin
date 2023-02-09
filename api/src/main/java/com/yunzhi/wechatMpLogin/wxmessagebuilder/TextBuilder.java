package com.yunzhi.wechatMpLogin.wxmessagebuilder;

import com.yunzhi.wechatMpLogin.service.WeChatMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTextMessage;

/**
 * @author Binary Wang
 */
public class TextBuilder extends AbstractBuilder {

  @Override
  public WxMpXmlOutMessage build(String content, WxMpXmlMessage wxMessage,
                                 WeChatMpService service) {
    WxMpXmlOutTextMessage m = WxMpXmlOutMessage.TEXT().content(content)
      .fromUser(wxMessage.getToUser()).toUser(wxMessage.getFromUser())
      .build();
    return m;
  }

}
