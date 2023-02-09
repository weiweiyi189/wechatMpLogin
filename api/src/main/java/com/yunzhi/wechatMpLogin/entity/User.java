package com.yunzhi.wechatMpLogin.entity;

import com.fasterxml.jackson.annotation.JsonView;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户
 */
@Entity
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected Long id;


  @Column(nullable = false, length = 200)
  @JsonView(UsernameJsonView.class)
  private String username;

  /**
   * 密码加密
   */
  private static PasswordEncoder passwordEncoder;

  @JsonView(PasswordJsonView.class)
  private String password;

  /**
   * 微信对应的openid
   */
  @JsonView(OpenidJsonView.class)
  private String openid;

  @Column(nullable = false)
  @JsonView(NameJsonView.class)
  private String name;

  @OneToOne(mappedBy = "user")
  @JsonView(WeChatUserJsonView.class)
  private WeChatUser weChatUser = null;



  public static void setPasswordEncoder(PasswordEncoder passwordEncoder) {
    User.passwordEncoder = passwordEncoder;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    if (User.passwordEncoder == null) {
      throw new RuntimeException("未设置User实体的passwordEncoder，请调用set方法设置");
    }
    this.password = User.passwordEncoder.encode(password);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public WeChatUser getWeChatUser() {
    return weChatUser;
  }

  public void setWeChatUser(WeChatUser weChatUser) {
    this.weChatUser = weChatUser;
  }

  public String getOpenid() {
    return openid;
  }

  public void setOpenid(String openid) {
    this.openid = openid;
  }


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }


  public interface UsernameJsonView {
  }

  public interface NameJsonView {
  }

  public interface OpenidJsonView {
  }

  private class PasswordJsonView {
  }


  public interface WeChatUserJsonView {}
}
