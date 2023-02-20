package com.fly.seata.service;

import com.fly.seata.dao.StorageDao;
import com.fly.seata.domain.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: peijiepang
 * @date 2019-11-20
 * @Description:
 */
@Slf4j
@Service("storageService")
public class StorageService {

  @Autowired
  private StorageDao storageDao;

  /**
   * 扣减库存
   * @return
   */
  public String reduceStorage(Long productId,Integer count){
    log.info("reductStorage productId:{} count:{}",productId,count);
    storageDao.reduce(productId,count);
    return "ok";
  }

  /**
   * 回滚扣减库存
   * @return
   */
  public String rollbackReduceStorage(Long prodectId,Integer count){
    log.info("rollbackreducestorage productId:{} count:{}",prodectId,count);
    storageDao.rollback(prodectId,count);
    return "ok";
  }

  /**
   * 插入库存
   * @param productId
   * @return
   */
  public Long insertStorage(Long productId){
    Storage storage = new Storage();
    storage.setProductId(productId);
    storage.setUsed(0);
    storageDao.insert(storage);
    return storage.getId();
//    throw new RuntimeException("测试回滚");
  }

  /**
   * 删除库存
   * @param productId
   */
  public void deleteStorageById(Long productId){
    storageDao.delete(productId);
  }

}
