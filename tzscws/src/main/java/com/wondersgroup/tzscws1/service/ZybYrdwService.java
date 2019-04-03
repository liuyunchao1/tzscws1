package com.wondersgroup.tzscws1.service;

import com.wondersgroup.tzscws1.model.ZybYrdw;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ZybYrdwService {
    public List<ZybYrdw> selectByEmployerName(String employerName);
    public ZybYrdw selectByPrimaryKey(String employerCode);
    public int updateByPrimaryKey(ZybYrdw zybYrdw);
    public int insert(ZybYrdw zybYrdw);

}
