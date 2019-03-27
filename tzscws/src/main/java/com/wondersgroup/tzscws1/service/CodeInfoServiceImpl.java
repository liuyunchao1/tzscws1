package com.wondersgroup.tzscws1.service;

import com.wondersgroup.tzscws1.dao.CodeInfoMapper;
import com.wondersgroup.tzscws1.model.CodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CodeInfoServiceImpl implements  CodeInfoService{
    @Autowired
    private CodeInfoMapper codeInfoMapper ;//这里会报错，但是并不会影响
    @Override
    public List<CodeInfo> selectByCodeInfoId(BigDecimal codeInfo) {

        return codeInfoMapper.selectByCodeInfoId(codeInfo);
    }
}
