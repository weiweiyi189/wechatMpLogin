package com.yunzhi.wechatMpLogin.repository;

import com.sun.istack.NotNull;
import com.yunzhi.wechatMpLogin.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.util.Assert;

import java.util.Optional;

public interface UserRepository extends PagingAndSortingRepository<User, Long>, JpaSpecificationExecutor {


  /**
   * 根据 openid 查询
   *
   * @param openid openid
   */
  Optional<User> findByOpenid(String openid);

  /**
   * 根据用户名查询用户
   */
  Optional<User> findByUsername(String username);
}
