package com.yunzhi.wechatMpLogin.wxhandler;

import com.yunzhi.wechatMpLogin.service.WeChatMpService;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Binary Wang
 */
@Component
public class UnsubscribeHandler extends AbstractHandler {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  public UnsubscribeHandler(WeChatMpService weChatMpService) {
    super(weChatMpService);
  }

  @Override
  public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                  Map<String, Object> context, WxMpService wxMpService,
                                  WxSessionManager sessionManager) {
    String openId = wxMessage.getFromUser();
    this.logger.info("取消关注用户 OPENID: " + openId);
    // TODO 可以更新本地数据库为取消关注状态
    return null;
  }

}
