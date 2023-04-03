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
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpSession;
import java.util.*;

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
   * 将loginUid与微信用户绑定在一起
   * 前端微信用户扫码成功后，将使用loginUid进行登录，而登录是否成功，登录是哪个用户，取决于当前方法loginUid与哪个微个用户绑定在一起了
   * @param loginUid loginUid
   * @param weChatUser 微信用户
   */
  void bindWsUuidToWeChatUser(String loginUid, WeChatUser weChatUser) {
    this.map.put(loginUid, weChatUser.getUsername());
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
        this.logger.info("1. 生成用于回调的sceneStr，请将推送给微信，微信当推送带有sceneStr的二维码，用户扫码后微信则会把带有sceneStr的信息回推过来");
      }

      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();

      final Map<String, String> variables = new HashMap<>();
      variables.put("username", userDetails.getUsername());
      variables.put("sessionId", sessionId);

      String requestUrl = UserServiceImpl.addParam("http:/localhost:8082/request/getQrCode", variables);

      System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");

      RestTemplate restTemplate = new RestTemplate();
      String bindQrCode = restTemplate.getForObject(requestUrl, String.class, variables);

      return bindQrCode;
    } catch (Exception e) {
      this.logger.error("获取临时公众号图片时发生错误：" + e.getMessage());
    }
    return "";
  }

  @Transactional
  @Override
  public void bindWeChatUserToUser(WeChatUser weChatUser, String username, String sessionId, String openId) {
    WeChatUser wechat = this.weChatUserRepository.findById(weChatUser.getId()).get();
    User user = this.userRepository.findByUsername(username).get();
    wechat.setUser(user);
    this.weChatUserRepository.save(wechat);

    String wsToken = webSocketService.getWsToken(sessionId);
    this.logger.info("wsToken:" + wsToken);
    simpMessagingTemplate.convertAndSendToUser(wsToken,
            "/stomp/scanBindUserQrCode",
            openId);
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
        this.logger.info("1. 生成用于回调的sceneStr，请将推送给微信，微信当推送带有sceneStr的二维码，用户扫码后微信则会把带有sceneStr的信息回推过来");
      }
      // 生成临时二维码场景值，之后微信回调信息会回发该值，根据此值调用handler
      // 例如ScanHandler的handleKey函数， 那里的wxMpXmlMessage.getEventKey()的值，就是该场景值
      String sceneStr = UUID.randomUUID().toString();
      WxMpQrCodeTicket wxMpQrCodeTicket = this.wxMpService.getQrcodeService().qrCodeCreateTmpTicket(sceneStr, 10 * 60);
      this.wxMpService.addHandler(sceneStr, new WeChatMpEventKeyHandler() {
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
            this.logger.info("2. 用户扫描后触发该方法, 发送扫码成功的同时，将loginUid与微信用户绑定在一起，后面使用loginUid登录");
          }
          // 微信用户的id
          String openid = wxMpXmlMessage.getFromUser();
          if (openid == null) {
            this.logger.error("openid is null");
          }

          if (weChatUser.getUser() != null) {
            // 登录凭证 前台凭该loginUid作为用户名和密码登录
            String loginUid = UUID.randomUUID().toString();
            bindWsUuidToWeChatUser(loginUid, weChatUser);
            simpMessagingTemplate.convertAndSendToUser(wsLoginToken,
                "/stomp/scanLoginQrCode",
                    loginUid);
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

  static public String addParam(String url , Map<String, String> variables) {
    String requestUrl = url;
    String symbol = "?";

    // 添加url参数格式
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      requestUrl = requestUrl + symbol + entry.getKey() + "={" + entry.getKey() + "}";
      symbol = "&";
    }

    return requestUrl;
  }
}
