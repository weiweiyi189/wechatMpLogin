package com.yunzhi.wechatMpLogin.security;

import com.yunzhi.wechatMpLogin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

/**
 * 注意：其不能够声明为@Component组件出现，否则将触发DaoAuthenticationProvider的构造函数
 * 从而直接注册DelegatingPasswordEncoder校验器
 */
public class myBCryptPasswordEncoder extends BCryptPasswordEncoder {

    @Autowired
    @Lazy
    private UserService userService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    public myBCryptPasswordEncoder() {
        super();
    }


    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }
        // 若不是微信用户 则进行默认的用户名和密码匹配
        if (this.myMatches(rawPassword, encodedPassword)) {
            return true;
        }
        return super.matches(rawPassword, encodedPassword);
    }

    /**
     * 微信用户验证
     */
    public boolean myMatches(CharSequence rawPassword, String encodedPassword) {
        // 增加微信扫码后使用webSocket uuid充当用户名与密码进行认证
        if (this.userService.checkWeChatLoginUuidIsValid(rawPassword.toString())) {
            if (this.logger.isDebugEnabled()) {
                this.logger.info("校验微信扫码登录成功");
            }
            return true;
        }
        return false;
    }
}
