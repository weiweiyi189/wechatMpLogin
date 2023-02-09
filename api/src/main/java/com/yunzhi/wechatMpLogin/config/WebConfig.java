package com.yunzhi.wechatMpLogin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * WEB配置
 */
@EnableWebMvc
@EnableAsync
@Configuration
public class WebConfig implements WebMvcConfigurer {

  public WebConfig() {
  }

  /**
   * 配置JsonView
   */
  @Override
  public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
    final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().defaultViewInclusion(true).build();
    converters.add(new MappingJackson2HttpMessageConverter(mapper));
  }

  /**
   * URL忽略大小写
   *
   * @param configurer 配置信息
   */
  @Override
  public void configurePathMatch(final PathMatchConfigurer configurer) {
    final AntPathMatcher pathMatcher = new AntPathMatcher();
    pathMatcher.setCaseSensitive(false);
    configurer.setPathMatcher(pathMatcher);
  }
}
