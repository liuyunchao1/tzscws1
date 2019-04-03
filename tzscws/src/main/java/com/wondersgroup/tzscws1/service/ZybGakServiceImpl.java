package com.wondersgroup.tzscws1.service;

import com.wondersgroup.tzscws1.dao.ZybGakMapper;
import com.wondersgroup.tzscws1.model.ZybGak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;


@Service
public class ZybGakServiceImpl implements ZybGakService{
    @Autowired
    private  ZybGakMapper zybGakMapper;

    public  ZybGak selectByCodeAndHosId(HashMap<String,String> param){
        return zybGakMapper.selectByCodeAndHosId(param);
    }
    public int insert(ZybGak zybGak){
        return zybGakMapper.insert(zybGak);
    }
    public int updateByPrimaryKey(ZybGak zybGak){
        return zybGakMapper.updateByPrimaryKey(zybGak);
    }
    public  ZybGak selectByPrimaryKey(String param){
        return zybGakMapper.selectByPrimaryKey(param);
    }

    @Override
    public List<ZybGak> selectForCallProvincial() {
        return zybGakMapper.selectForCallProvincial();
    }

    @Override
    public int updateByPrimaryKeySelective(ZybGak record) {
        return zybGakMapper.updateByPrimaryKeySelective(record);
    }
}
