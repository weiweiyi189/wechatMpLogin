package com.yunzhi.wechatMpLogin.config;

import com.yunzhi.wechatMpLogin.entity.User;
import com.yunzhi.wechatMpLogin.security.myBCryptPasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.web.http.HttpSessionStrategy;


@Configuration
@EnableWebSecurity
@EnableSpringHttpSession
public class MvcSecurityConfig {

    private final BCryptPasswordEncoder passwordEncoder;


    public MvcSecurityConfig() {
        this.passwordEncoder = new myBCryptPasswordEncoder();
        User.setPasswordEncoder(this.passwordEncoder);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                // 开放端口
                .antMatchers("/user/getLoginQrCode/**").permitAll()
                .antMatchers("/wechat/**").permitAll()
                .antMatchers("/websocket/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic()
                .and().cors()
                .and().csrf().disable();
        http.headers().frameOptions().disable();

        return http.build();
    }

    /**
     * 使用header认证来替换默认的cookie认证
     */
    @Bean
    public HttpSessionStrategy httpSessionStrategy() {
        return new HeaderAndParamHttpSessionStrategy();
    }


    /**
     * 由于我们启用了@EnableSpringHttpSession后，而非RedisHttpSession.
     * 所以应该为SessionRepository提供一个实现。
     * 而Spring中默认给了一个SessionRepository的实现MapSessionRepository.
     *
     * @return session策略
     */
    @Bean
    public SessionRepository sessionRepository() {
        return new MapSessionRepository();
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        return this.passwordEncoder;
    }
}
