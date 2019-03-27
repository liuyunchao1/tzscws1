package com.wondersgroup.tzscws1.service;

import com.wondersgroup.tzscws1.model.ZybGak;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public interface ZybGakService {

    public ZybGak selectByCodeAndHosId(HashMap<String,String> param);
}
