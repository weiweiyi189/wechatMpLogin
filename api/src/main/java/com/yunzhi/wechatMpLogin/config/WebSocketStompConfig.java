package com.yunzhi.wechatMpLogin.config;
import com.yunzhi.wechatMpLogin.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * WebSocket Stomp 配置
 * <ol>
 *   <li>当同时配置Stomp(EnableWebSocketMessageBroker)及原生的WEB Socket(@EnableWebSocket)时，Stomp将失效，也就是说两者不可以同时使用</li>
 *   <li>webscoket 配置：https://docs.spring.io/spring-framework/docs/5.3.9/reference/html/web.html#websocket</li>
 *   <li>stomp配置: https://docs.spring.io/spring-framework/docs/5.3.9/reference/html/web.html#websocket-stomp</li>
 * </ol>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketStompConfig implements WebSocketMessageBrokerConfigurer {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  /**
   * 此方法在于统一用websocket请求设置这个用户标识，用户标识可用于向该用户主动发送消息
   * <p>
   *  前台的stompjs在发起请求时，均会携带特定的header(我们自己规定的)做为用户标识信息
   *  此后：C层中凡是以@MessageMapping注解的方法便可以直接注入Principal
   *  而这个Principal正是我们此处给的 () -> wxAuthToken
   *  <br />
   *    参考：WebSocketController -> bindToXAuthToken
   *   <br />
   *    官方文档：https://docs.spring.io/spring-framework/docs/5.3.9/reference/html/web.html#websocket-stomp-authentication-token-based
   * </p>
   * <h4>我们在此并没有直接使用xAuthToken来在握手阶段完成websocket认证，这是由于以下原因造成的</h4>
   * <ul>
   *   <li>启用扫码登录的原因，需要在用户登录前就进行websocket连接，此时假设在握手阶段传入xAuthToken（123）</li>
   *   <li>登录成功后，Spring Security会发送一个新的xAuthToken(456)</li>
   *   <li>此时若想使用新的xAuthToken(456)，则需要重新握手</li>
   *   <li>重新握手成本不大，但重新握手后原websocket链接将失效，所以还需要进行重新注册</li>
   *   <li>而重新注册的成本就大了</li>
   *   <li>所以最后我们采用了在前台生成websocket uuid(wsToken)，然后在后台完成wsToken与xAuthToken来标识websocket与httpsession间的关系</li>
   *   <li>此时我们仅需要在登录用户（实际上应该是XAuthToken) 发生变化时，重新绑定wsToken与xAuthToken即可</li>
   * </ul>
   */
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {
      @Override
      public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
          String wxAuthToken = WebSocketService.getWxAuthToken(accessor.getMessageHeaders());
          accessor.setUser(() -> wxAuthToken);
        }
        return message;
      }
    });
  }

  /**
   * 配置消息经纪人
   * 配置一个入口前缀，一个出口前缀。
   * 注意：出口需要保留/user前缀，stomp主动向某个用户发送数据时，将以/user前缀前头（可配置）
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // 设置入口前缀，处理所有以app打头的请求
    config.setApplicationDestinationPrefixes("/app");
    // 设置出口前缀，处理所有以/stomp打头的出口数据
    config.enableSimpleBroker("/stomp");
  }

  /**
   * 定义一个连接点(处理第一次webSocket的握手)
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/websocket")
        .addInterceptors(new HttpSessionHandshakeInterceptor())
        .setAllowedOriginPatterns("*")
        .withSockJS();
  }
}
