package com.wondersgroup.tzscws1.service;

import com.wondersgroup.tzscws1.model.ZybGak;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public interface ZybGakService {

    public ZybGak selectByCodeAndHosId(HashMap<String,String> param);
    public int insert(ZybGak zybGak);
    public int updateByPrimaryKey(ZybGak zybGak);
    public  ZybGak selectByPrimaryKey(String param);

    /**
     * 调用上传省平台接口查找数据
     * @return
     */
    List<ZybGak> selectForCallProvincial(Map<String, Object> params);

    /**
     * 更新
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(ZybGak record);
}
