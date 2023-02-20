package com.fly.seata.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单dto
 * @author: peijiepang
 * @date 2019-11-19
 * @Description:
 */
public class OrderDTO implements Serializable {

  /**
   * 订单号
   */
  private String orderNo;

  /**
   * 用户id
   */
  private Long userId;

  /**
   * 商品id
   */
  private Long productId;

  /**
   * 数量
   */
  private Integer count;

  /**
   * 金额
   */
  private BigDecimal money;

  public String getOrderNo() {
    return orderNo;
  }

  public void setOrderNo(String orderNo) {
    this.orderNo = orderNo;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }


  public BigDecimal getMoney() {
    return money;
  }

  public void setMoney(BigDecimal money) {
    this.money = money;
  }

  @Override
  public String toString() {
    return "OrderDTO{" +
        "userId=" + userId +
        ", productId=" + productId +
        ", count=" + count +
        ", money=" + money +
        '}';
  }
}
