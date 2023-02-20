package com.fly.seata.controller;

import com.fly.seata.api.StorageApi;
import com.fly.seata.dto.OrderDTO;
import io.seata.saga.engine.StateMachineEngine;
import io.seata.saga.statelang.domain.StateMachineInstance;
import io.seata.spring.annotation.GlobalTransactional;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: peijiepang
 * @date 2019-11-18
 * @Description:
 */
@RequestMapping("/seata/tm")
@RestController
public class TmController {

  @Autowired
  private StateMachineEngine stateMachineEngine;

  @Autowired
  private StorageApi storageApi;

  /**
   * 模拟购买商品流程
   * @return
   */
  @GlobalTransactional
  @PostMapping("/purchase")
  public String purchase(HttpServletRequest request,@RequestBody OrderDTO orderDTO){
    Map<String, Object> startParams = new HashMap<>();
    startParams.put("order",orderDTO);
    StateMachineInstance stateMachineInstance = null;
    String type = request.getHeader("type");
    if(StringUtils.isNotEmpty(type) && type.equalsIgnoreCase("hot")){
        stateMachineInstance = stateMachineEngine.start("purchaseProcess2",null,startParams);
    }else {
        stateMachineInstance = stateMachineEngine.start("purchaseProcess3",null,startParams);
    }
    return "执行状态:"+stateMachineInstance.getStatus().getStatusString();
  }

  @GetMapping("/test")
  public String test(){
    return "ok";
  }

}
