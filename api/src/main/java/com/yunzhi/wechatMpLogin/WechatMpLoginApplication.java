package com.yunzhi.wechatMpLogin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class WechatMpLoginApplication {

	public static void main(String[] args) {
		SpringApplication.run(WechatMpLoginApplication.class, args);
	}

}
