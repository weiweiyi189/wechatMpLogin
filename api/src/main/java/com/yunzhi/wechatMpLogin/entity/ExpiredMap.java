package com.yunzhi.wechatMpLogin.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在一定时间后不使用则过期的Map
 * @param <K> key
 * @param <V> value
 */
public class ExpiredMap<K,V> {
  private final Map<K,Expired<V>> map = new ConcurrentHashMap();

  public V get(Object key) {
    return this.map.get(key) == null ? null : this.map.get(key).getData();
  }

  public V put(K key, V value) {
    Expired<V> result = this.map.put(key, new Expired<>(value, 60 * 1000));
    return result == null ? null : result.getData();
  }

  public V put(K key, V value, long lifeTime) {
    return this.map.put(key, new Expired<>(value, lifeTime)).getData();
  }

  public V remove(Object key) {
    Expired<V> result = this.map.remove(key);
    return result == null ? null : result.getData();
  }

  public boolean containsKey(Object key) {
    return this.map.containsKey(key);
  }

  private class Expired<T> {
    T data;
    long lifeTime;
    long lastUpdateTime;

    public Expired(T data, long lifeTime) {
      this.data = data;
      this.lifeTime = lifeTime;
      this.lastUpdateTime = System.currentTimeMillis();
    }

    boolean isExpired() {
      return System.currentTimeMillis() - this.lastUpdateTime - this.lifeTime > 0;
    }

    void setData(T data) {
      this.data = data;
    }

    T getData() {
      return this.data;
    }
  }
}
