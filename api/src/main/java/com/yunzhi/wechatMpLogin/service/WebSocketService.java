package com.yunzhi.wechatMpLogin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class WebSocketService {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final Map<String, TokenWithLastUsedTime> map = new ConcurrentHashMap<>();

  public static String getWxAuthToken(MessageHeaders headers) {
    Map<String, List<String>> nativeHeaders = (Map) headers.get("nativeHeaders");
    List<String> wsAuthTokens = nativeHeaders.get("ws-auth-token");
    String wsAuthToken = wsAuthTokens.get(0);
    return wsAuthToken;
  }

  /**
   * 将webSocket的token与http的认证token绑定在一起，这样便可以互相都找的到了
   * @param wsAuthToken webSocket token
   * @param xAuthToken http token
   */
  public void bindToXAuthToken(String wsAuthToken, String xAuthToken) {
    this.map.put(wsAuthToken, new TokenWithLastUsedTime(xAuthToken));
  }

  /**
   * 定期清除无效的xAuthToken
   */
  @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
  void clearInvalidToken() {
    Set<String> keys = new HashSet<>();
    for (Map.Entry<String, TokenWithLastUsedTime> entry : map.entrySet()) {
      TokenWithLastUsedTime value = entry.getValue();
      if (Calendar.getInstance().getTimeInMillis() - value.getLastUsedTime().getTimeInMillis() > 24 * 60 * 60 * 1000) {
        keys.add(entry.getKey());
      }
    }

    for (String key: keys) {
      this.map.remove(key);
    }
  }

  /**
   * 通过sessionId获取webSocket的认证id
   * @param sessionId sessionId
   */
  public String getWsToken(String sessionId) {
     List<String> tokens = this.map.entrySet()
        .stream()
        .filter(entry -> sessionId.equals(entry.getValue().getToken()))
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
     String wsToken = tokens.size() > 0 ? tokens.get(0) : null;
     if (wsToken != null) {
       this.map.get(wsToken).setLastUsedTime(Calendar.getInstance());
     } else {
       throw new RuntimeException(String.format("未获取到sessionId: %s 对应的webSocketSession", sessionId));
     }
     return wsToken;
  }

  /**
   * 携带有最后一次使用时间的token
   */
  private static class TokenWithLastUsedTime {
    /**
     * 上次使用时间
     */
    private Calendar lastUsedTime;

    private String token;

    public TokenWithLastUsedTime(String token) {
      this.token = token;
      this.lastUsedTime = Calendar.getInstance();
    }

    public Calendar getLastUsedTime() {
      return lastUsedTime;
    }

    public void setLastUsedTime(Calendar lastUsedTime) {
      this.lastUsedTime = lastUsedTime;
    }

    public String getToken() {
      return token;
    }

    public void setToken(String token) {
      this.token = token;
    }
  }
}
