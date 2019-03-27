package com.wondersgroup.tzscws1.service;

import com.wondersgroup.tzscws1.dao.ZybGakMapper;
import com.wondersgroup.tzscws1.model.ZybGak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;


@Service
public class ZybGakServiceImpl implements ZybGakService{
    @Autowired
    private  ZybGakMapper zybGakMapper;
    @Override
    public  ZybGak selectByCodeAndHosId(HashMap<String,String> param){
        System.out.println("code:" +param.get("code"));
        System.out.println("hosId:" +param.get("hosId"));
        return zybGakMapper.selectByCodeAndHosId(param);
    }
}
