package com.wondersgroup.tzscws1.service;

import com.wondersgroup.tzscws1.model.CodeInfo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
@Service
public interface CodeInfoService {
     List<CodeInfo> selectByCodeInfoId(BigDecimal codeInfo);
}
