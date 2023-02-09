package com.yunzhi.wechatMpLogin.entity;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 微信用户
 */
@Entity
public class WeChatUser implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 用于获取与微信后台交换当前用户加密信息的密钥，该密钥不定期过期，过期后应该重新使用code更新
   */
  @Transient
  @JsonView(SessionKeyJsonView.class)
  private String sessionKey;

  @Column(nullable = false)
  private String openid;

  @JsonView(UnionIdJsonView.class)
  private String unionId;

  @Column(nullable = false)
  private String appId;

  /**
   * 对应的系统用户
   */
  @OneToOne
  @JsonView(UserJsonView.class)
  private User user;

  /**
   * 是否注册用户
   */
  private boolean registered = false;

  public WeChatUser() {
  }

  public WeChatUser(String openid, String appId) {
    this.openid = openid;
    this.appId = appId;
  }

  public WeChatUser(User user, String openid, String appId) {
    this.setUser(user);
    this.openid = openid;
    this.appId = appId;
  }


  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return new ArrayList<>();
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.registered = user.getId() != null;
    this.user = user;
  }

  public void setOpenid(String openid) {
    this.openid = openid;
  }

  public String getOpenid() {
    return this.openid;
  }

  public String getSessionKey() {
    return sessionKey;
  }

  public void setSessionKey(String sessionKey) {
    this.sessionKey = sessionKey;
  }

  @Override
  public String getPassword() {
    return this.user.getPassword();
  }

  @Override
  public String getUsername() {
    return this.user.getUsername();
  }

  public boolean isRegistered() {
    return registered;
  }

  public void setRegistered(boolean registered) {
    this.registered = registered;
  }

  @Override
  public boolean isAccountNonExpired() {
    return this.user.getId() != null;
  }

  @Override
  public boolean isAccountNonLocked() {
    return this.user.getId() != null;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return this.user.getId() != null;
  }

  @Override
  public boolean isEnabled() {
    return this.user.getId() != null;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getUnionId() {
    return unionId;
  }

  public void setUnionId(String unionId) {
    this.unionId = unionId;
  }

  private class SessionKeyJsonView {
  }

  public interface UserJsonView {}

  private interface UnionIdJsonView {}
}
