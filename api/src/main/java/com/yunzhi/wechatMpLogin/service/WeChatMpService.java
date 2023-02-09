package com.yunzhi.wechatMpLogin.service;

import com.yunzhi.wechatMpLogin.config.WxMpConfig;
import com.yunzhi.wechatMpLogin.wxhandler.*;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.kefu.result.WxMpKfOnlineList;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static me.chanjar.weixin.common.api.WxConsts.EventType;
import static me.chanjar.weixin.common.api.WxConsts.EventType.*;
import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType;
import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType.EVENT;
import static me.chanjar.weixin.mp.constant.WxMpEventConstants.CustomerService.*;
import static me.chanjar.weixin.mp.constant.WxMpEventConstants.POI_CHECK_NOTIFY;

/**
 * 这段作用是根据事件类型来选择处理器
 * @author Binary Wang
 */
@Service
public class WeChatMpService extends WxMpServiceImpl {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private ConcurrentHashMap<String, WeChatMpEventKeyHandler> hashMap = new ConcurrentHashMap<>();

  @Autowired @Lazy
  protected LogHandler logHandler;

  @Autowired @Lazy
  protected NullHandler nullHandler;

  @Autowired @Lazy
  protected ScanHandler scanHandler;

  @Autowired @Lazy
  protected KfSessionHandler kfSessionHandler;


  @Autowired @Lazy
  private LocationHandler locationHandler;

  @Autowired @Lazy
  private MenuHandler menuHandler;

  @Autowired @Lazy
  private MsgHandler msgHandler;

  @Autowired @Lazy
  private UnsubscribeHandler unsubscribeHandler;

  @Autowired @Lazy
  private SubscribeHandler subscribeHandler;

  private WxMpMessageRouter router;

  @Autowired
  private WxMpConfig wxMpConfig;

  @PostConstruct
  public void init() {
    final WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
    // 设置微信公众号的appid
    config.setAppId(this.wxMpConfig.getAppid());
    // 设置微信公众号的app corpSecret
    config.setSecret(this.wxMpConfig.getAppSecret());
    // 设置微信公众号的token
    config.setToken(this.wxMpConfig.getToken());
    // 设置消息加解密密钥
    config.setAesKey(this.wxMpConfig.getAesKey());
    super.setWxMpConfigStorage(config);
    this.refreshRouter();
  }

  public void addHandler(String key, WeChatMpEventKeyHandler weChatMpEventKeyHandler) {
    this.hashMap.put(key, weChatMpEventKeyHandler);
  }

  public void removeHandler(String key) {
    this.hashMap.remove(key);
  }

  /**
   * 通过uuid来获取对应的处理器
   * @param uuid 每个二维码都对应一个维一的uuid
   * @return 事件处理器
   */
  public WeChatMpEventKeyHandler getHandler(String uuid) {
    this.logger.info("key:" + uuid);
    this.randomRemoveExpiredHandler();
    WeChatMpEventKeyHandler weChatMpEventKeyHandler = this.hashMap.get(uuid);
    if (weChatMpEventKeyHandler != null && weChatMpEventKeyHandler.getExpired()) {
      this.hashMap.remove(uuid);
      weChatMpEventKeyHandler = null;
    }
    return weChatMpEventKeyHandler;
  }

  /**
   * 1/10的概率清空过期的Handler
   */
  private void randomRemoveExpiredHandler() {
    if (new Random().nextInt(10) % 10 == 1) {
      List<String> removeKes = new ArrayList<>();
      if (new Random().nextInt(10) % 10 == 1) {
        for (String k : this.hashMap.keySet()) {
          if (this.hashMap.get(k).getExpired()) {
            removeKes.add(k);
          }
        }
      }

      for(String removeKey : removeKes) {
        this.hashMap.remove(removeKey);
      }
    }
  }

  private void refreshRouter() {
    final WxMpMessageRouter newRouter = new WxMpMessageRouter(this);

    // 记录所有事件的日志
    newRouter.rule().handler(this.logHandler).next();

    // 接收客服会话管理事件
    newRouter.rule().async(false).msgType(EVENT).event(KF_CREATE_SESSION)
      .handler(this.kfSessionHandler).end();
    newRouter.rule().async(false).msgType(EVENT).event(KF_CLOSE_SESSION)
      .handler(this.kfSessionHandler).end();
    newRouter.rule().async(false).msgType(EVENT).event(KF_SWITCH_SESSION)
      .handler(this.kfSessionHandler).end();


    // 自定义菜单事件
    newRouter.rule().async(false).msgType(EVENT).event(EventType.CLICK).handler(this.menuHandler).end();

    // 点击菜单连接事件
    newRouter.rule().async(false).msgType(EVENT).event(EventType.VIEW).handler(this.nullHandler).end();

    // 关注事件
    newRouter.rule().async(false).msgType(EVENT).event(SUBSCRIBE).handler(this.subscribeHandler).end();

    // 取消关注事件
    newRouter.rule().async(false).msgType(EVENT).event(UNSUBSCRIBE).handler(this.unsubscribeHandler).end();

    // 上报地理位置事件
    newRouter.rule().async(false).msgType(EVENT).event(LOCATION).handler(this.locationHandler).end();

    // 接收地理位置消息
    newRouter.rule().async(false).msgType(XmlMsgType.LOCATION).handler(this.locationHandler).end();

    // 扫码事件
    newRouter.rule().async(false).msgType(EVENT).event(SCAN).handler(this.scanHandler).end();

    // 默认
    newRouter.rule().async(false).handler(this.msgHandler).end();

    this.router = newRouter;
  }

  /**
   * 微信事件通过这个入口进来
   */
  public WxMpXmlOutMessage route(WxMpXmlMessage message) {
    try {
      return this.router.route(message);
    } catch (Exception e) {
      this.logger.error(e.getMessage(), e);
    }

    return null;
  }

  public boolean hasKefuOnline() {
    try {
      WxMpKfOnlineList kfOnlineList = this.getKefuService().kfOnlineList();
      return kfOnlineList != null && kfOnlineList.getKfOnlineList().size() > 0;
    } catch (Exception e) {
      this.logger.error("获取客服在线状态异常: " + e.getMessage(), e);
    }

    return false;
  }
}
