package com.wondersgroup.tzscws1.service;

import com.wondersgroup.tzscws1.dao.ZybYrdwMapper;
import com.wondersgroup.tzscws1.model.ZybYrdw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZybYrdwServiceImpl implements  ZybYrdwService{
    @Autowired
    private ZybYrdwMapper zybYrdwMapper;
    @Override
    public List<ZybYrdw> selectByEmployerName(String employerName){
        return zybYrdwMapper.selectByEmployerName(employerName);
    }
    public ZybYrdw selectByPrimaryKey(String employerCode){
        return zybYrdwMapper.selectByPrimaryKey(employerCode);
    }
}
