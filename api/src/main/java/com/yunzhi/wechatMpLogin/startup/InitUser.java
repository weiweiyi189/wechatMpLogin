package com.yunzhi.wechatMpLogin.startup;


import com.yunzhi.wechatMpLogin.entity.User;
import com.yunzhi.wechatMpLogin.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;


/**
 * 初始化测试账户
 * admin / admin
 */
@Component
public class InitUser implements ApplicationListener<ContextRefreshedEvent>, Ordered {
  public static int order = Integer.MIN_VALUE;
  private static final Logger logger = LoggerFactory.getLogger(InitUser.class);

  private final UserRepository userRepository;

  public InitUser(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

    logger.info("初始化用户");
    if (userRepository.count() == 0) {
      User user = new User();
      user.setName("admin");
      user.setUsername("admin");
      user.setPassword("admin");
      userRepository.save(user);
    }
  }

  @Override
  public int getOrder() {
    return order;
  }
}
