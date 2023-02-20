package com.fly.seata.api;

import com.fly.seata.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author: peijiepang
 * @date 2019-11-18
 * @Description:
 */
@FeignClient(name = "seata-saga-rm-one")
public interface OrderApi {

   @GetMapping("/rm1/test")
   String test();

   @GetMapping("/order/createOrderNo")
   String createOrderNo();

   @PostMapping(value= "/order/createOrderNo",consumes = MediaType.APPLICATION_JSON_VALUE)
   String createOrder(@RequestBody OrderDTO orderDTO);

   @GetMapping("/order/canal/{orderNo}")
   void canalOrder(@PathVariable("orderNo") String orderNo);
}
