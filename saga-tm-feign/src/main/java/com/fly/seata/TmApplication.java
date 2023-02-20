package com.fly.seata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: peijiepang
 * @date 2019-11-18
 * @Description:
 */
@EnableFeignClients
@EnableEurekaClient
@SpringBootApplication
public class TmApplication {

  public static void main(String[] args) {
    SpringApplication.run(TmApplication.class,args);
  }

}
