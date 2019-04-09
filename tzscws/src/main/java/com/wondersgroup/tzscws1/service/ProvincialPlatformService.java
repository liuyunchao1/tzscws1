package com.wondersgroup.tzscws1.service;

import com.wondersgroup.tzscws1.model.ZybGak;

import java.util.List;
import java.util.Map;

/**
 * 调用省平台接口Service
 */
public interface ProvincialPlatformService {

    /**
     * 调用上传省平台接口,分批次上传
     * @return {success:成功数量,fail:失败数量}
     */
    Map<String, Integer> batchCallProvincialPlatform(List<ZybGak> list);
}
