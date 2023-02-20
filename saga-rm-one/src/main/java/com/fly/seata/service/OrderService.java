package com.fly.seata.service;

import com.fly.seata.dao.OrderDao;
import com.fly.seata.dto.OrderDTO;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 下单服务
 * @author: peijiepang
 * @date 2019-11-19
 * @Description:
 */
@Slf4j
@Service("orderService")
public class OrderService {

  @Autowired
  private OrderDao orderDao;

  /**
   * 返回下单流程
   * @param order
   * @return
   */
  public String createOrder(OrderDTO order){
    String orderNo = UUID.randomUUID().toString();
    log.info("模拟下单成功流程,订单号：{} 订单信息:{}",orderNo,order.toString());
//    throw new RuntimeException("模拟下单失败！！！");
    order.setOrderNo(orderNo);
    orderDao.insert(order);
    return orderNo;
  }

  /**
   * 取消订单
   * @param orderNo
   */
  public void canalOrder(String orderNo){
    orderDao.delete(orderNo);
  }

}
