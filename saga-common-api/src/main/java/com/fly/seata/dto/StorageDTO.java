package com.fly.seata.dto;

import java.io.Serializable;

/**
 * @author: peijiepang
 * @date 2019-11-21
 * @Description:
 */
public class StorageDTO implements Serializable {

  /**
   * 订单号
   */
  private String orderNo;

  /**
   * 商品id
   */
  private Long productId;

  /**
   * 数量
   */
  private Integer count;

  public String getOrderNo() {
    return orderNo;
  }

  public void setOrderNo(String orderNo) {
    this.orderNo = orderNo;
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

  @Override
  public String toString() {
    return "StorageDTO{" +
        "orderNo='" + orderNo + '\'' +
        ", productId=" + productId +
        ", count=" + count +
        '}';
  }
}
