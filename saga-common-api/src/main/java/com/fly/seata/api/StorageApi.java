package com.fly.seata.api;

import com.fly.seata.dto.StorageDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author: peijiepang
 * @date 2019-11-18
 * @Description:
 */
@FeignClient(name = "seata-sage-rm-two")
public interface StorageApi {

  @GetMapping("/rm2/test")
  String test();

  @PostMapping(value = "/storage/reduce",consumes = MediaType.APPLICATION_JSON_VALUE)
  void reduce(@RequestBody StorageDTO storageDTO);

  @PostMapping(value = "/storage/compensatereduce",consumes = MediaType.APPLICATION_JSON_VALUE)
  void compensateReduce(@RequestBody StorageDTO storageDTO);

  @RequestMapping(value = "/storage/insertstorage")
  Long insertStorage(@RequestParam Long productId);

  @RequestMapping(value = "/storage/compensateinsert")
  void compensateInsert(@RequestParam Long storageId);
}
