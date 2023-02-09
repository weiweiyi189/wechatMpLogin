package com.yunzhi.wechatMpLogin.service;


import com.yunzhi.wechatMpLogin.entity.ExpiredMap;
import com.yunzhi.wechatMpLogin.entity.User;
import com.yunzhi.wechatMpLogin.entity.WeChatUser;
import com.yunzhi.wechatMpLogin.repository.UserRepository;
import com.yunzhi.wechatMpLogin.repository.WeChatUserRepository;
import com.yunzhi.wechatMpLogin.wxmessagebuilder.TextBuilder;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final WeChatMpService wxMpService;
  private final WeChatUserRepository weChatUserRepository;
  private final WebSocketService webSocketService;
  private final ExpiredMap<String, String> map = new ExpiredMap<>();


  /**
   * 实现：webSocket向特定的用户推送消息
   */
  @Autowired
  SimpMessagingTemplate simpMessagingTemplate;


  public UserServiceImpl(UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         WeChatMpService wxMpService, WeChatUserRepository weChatUserRepository, WebSocketService webSocketService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.wxMpService = wxMpService;
    this.weChatUserRepository = weChatUserRepository;
    this.webSocketService = webSocketService;
  }

  /**
   * 将webSocket的uuid与微信用户绑定在一起
   * 前端微信用户扫码成功后，将使用uuid进行登录，而登录是否成功，登录是哪个用户，取决于当前方法wsUuid与哪个微个用户绑定在一起了
   * @param wsUuid wsUuid
   * @param weChatUser 微信用户
   */
  void bindWsUuidToWeChatUser(String wsUuid, WeChatUser weChatUser) {
    this.map.put(wsUuid, weChatUser.getUsername());
  }


  /**
   * 生成与当前登录用户绑定的二维码
   * @param sessionId sessionId
   * @return 扫描后触发的回调关键字
   */
  @Override
  public String generateBindQrCode(String sessionId) {
    try {
      if (this.logger.isDebugEnabled()) {
        this.logger.info("1. 生成用于回调的uuid，请将推送给微信，微信当推送带有UUID的二维码，用户扫码后微信则会把带有uuid的信息回推过来");
      }
      String uuid = UUID.randomUUID().toString();
      WxMpQrCodeTicket wxMpQrCodeTicket = this.wxMpService.getQrcodeService().qrCodeCreateTmpTicket(uuid, 10 * 60);
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();

      this.wxMpService.addHandler(uuid, new WeChatMpEventKeyHandler() {
        long beginTime = System.currentTimeMillis();
        private Logger logger = LoggerFactory.getLogger(this.getClass());

        @Override
        public boolean getExpired() {
          return System.currentTimeMillis() - beginTime > 10 * 60 * 1000;
        }

        @Override
        public WxMpXmlOutMessage handle(WxMpXmlMessage wxMpXmlMessage, WeChatUser weChatUser) {
          if (this.logger.isDebugEnabled()) {
            this.logger.info("用户扫码后通过uuid触发该方法。1. 向前台发送已扫描成功。 2. 向微信发送绑定成功的信息");
          }
          String openid = wxMpXmlMessage.getFromUser();
          if (openid == null) {
            this.logger.error("openid is null");
          }

          bindWeChatUserToUser(weChatUser, userDetails);

          String wsToken = webSocketService.getWsToken(sessionId);
          this.logger.info("wsToken:" + wsToken);
          simpMessagingTemplate.convertAndSendToUser(wsToken,
              "/stomp/scanBindUserQrCode",
              openid);

          return new TextBuilder().build(String.format("您当前的微信号已与系统用户 %s 成功绑定。", userDetails.getUsername()),
              wxMpXmlMessage,
              null);
        }
      });
      return this.wxMpService.getQrcodeService().qrCodePictureUrl(wxMpQrCodeTicket.getTicket());
    } catch (Exception e) {
      this.logger.error("获取临时公众号图片时发生错误：" + e.getMessage());
    }
    return "";
  }

  @Transactional
  void bindWeChatUserToUser(WeChatUser weChatUser, UserDetails userDetails) {
    WeChatUser wechat = this.weChatUserRepository.findById(weChatUser.getId()).get();
    User user = this.userRepository.findByUsername(userDetails.getUsername()).get();
    wechat.setUser(user);
    this.weChatUserRepository.save(wechat);
  }


  /**
   * 校验微信扫码登录后的认证ID是否有效
   * @param wsAuthUuid websocket认证ID
   */
  @Override
  public boolean checkWeChatLoginUuidIsValid(String wsAuthUuid) {
    return this.map.containsKey(wsAuthUuid);
  }


  @Override
  public String getLoginQrCode(String wsLoginToken, HttpSession httpSession) {
    try {
      if (this.logger.isDebugEnabled()) {
        this.logger.info("1. 生成用于回调的uuid，请将推送给微信，微信当推送带有UUID的二维码，用户扫码后微信则会把带有uuid的信息回推过来");
      }
      String qrUuid = UUID.randomUUID().toString();
      WxMpQrCodeTicket wxMpQrCodeTicket = this.wxMpService.getQrcodeService().qrCodeCreateTmpTicket(qrUuid, 10 * 60);
      this.wxMpService.addHandler(qrUuid, new WeChatMpEventKeyHandler() {
        long beginTime = System.currentTimeMillis();
        private Logger logger = LoggerFactory.getLogger(this.getClass());

        @Override
        public boolean getExpired() {
          return System.currentTimeMillis() - beginTime > 10 * 60 * 1000;
        }

        /**
         * 扫码后调用该方法
         * @param wxMpXmlMessage 扫码消息
         * @param weChatUser 扫码用户
         * @return 输出消息
         */
        @Override
        public WxMpXmlOutMessage handle(WxMpXmlMessage wxMpXmlMessage, WeChatUser weChatUser) {
          if (this.logger.isDebugEnabled()) {
            this.logger.info("2. 用户扫描后触发该方法, 发送扫码成功的同时，将wsUuid与微信用户绑定在一起，用后面使用wsU");
          }
          String openid = wxMpXmlMessage.getFromUser();
          if (openid == null) {
            this.logger.error("openid is null");
          }

          if (weChatUser.getUser() != null) {
            String uuid = UUID.randomUUID().toString();
            bindWsUuidToWeChatUser(uuid, weChatUser);
            simpMessagingTemplate.convertAndSendToUser(wsLoginToken,
                "/stomp/scanLoginQrCode",
                uuid);
            return new TextBuilder().build(String.format("登录成功，登录的用户为： %s", weChatUser.getUser().getName()),
                wxMpXmlMessage,
                null);
          } else {
            simpMessagingTemplate.convertAndSendToUser(wsLoginToken,
                "/stomp/scanLoginQrCode",
                false);
            return new TextBuilder().build(String.format("登录原则，原因：您尚未绑定微信用户"),
                wxMpXmlMessage,
                null);
          }
        }
      });
      return this.wxMpService.getQrcodeService().qrCodePictureUrl(wxMpQrCodeTicket.getTicket());
    } catch (Exception e) {
      this.logger.error("获取临时公众号图片时发生错误：" + e.getMessage());
    }
    return "";
  }

  @Override
  public Optional<User> getCurrentLoginUser() {
    logger.debug("初始化用户");
    Optional<User> user = null;

    logger.debug("获取用户认证信息");
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    logger.debug("根据认证信息查询用户");
    if (authentication != null && authentication.isAuthenticated()) {
      user = userRepository.findByUsername(authentication.getName());
    }

    return user;
  }


  @Override
  public User getByUsername(String name) {
    return this.userRepository.findByUsername(name).orElseThrow(EntityNotFoundException::new);
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    if (this.map.get(username) != null) {
      username = this.map.get(username);
    }

    User user = this.userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

    // 设置用户角色
    List<SimpleGrantedAuthority> authorities = new ArrayList<>();

    return new org.springframework.security.core.userdetails.User(username, user.getPassword(), authorities);
  }
}
