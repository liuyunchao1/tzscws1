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
    @Override
    public ZybYrdw selectByPrimaryKey(String employerCode){
        return zybYrdwMapper.selectByPrimaryKey(employerCode);
    }
    @Override
    public int updateByPrimaryKey(ZybYrdw zybYrdw){
        return zybYrdwMapper.updateByPrimaryKey(zybYrdw);
    }
    @Override
    public int insert(ZybYrdw zybYrdw){
        return zybYrdwMapper.insert(zybYrdw);
    }

    @Override
    public List<ZybYrdw> selectByIdList(List<String> idList) {
        return zybYrdwMapper.selectByIdList(idList);
    }

    @Override
    public List<ZybYrdw> selectByNameList(List<String> nameList) {
        return zybYrdwMapper.selectByNameList(nameList);
    }

    /**
     * 根据creditCode查找
     */
   public ZybYrdw selectByCreditCode(String creditCode){
        return zybYrdwMapper.selectByCreditCode(creditCode);
    }
    public boolean insertBatch(List<ZybYrdw> zybYrdwList){
        return zybYrdwMapper.insertBatch(zybYrdwList);
    }
}
