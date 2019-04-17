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


    /**
     * 根据id批量查找
     * @param idList id列表
     * @return
     */
    List<ZybYrdw> selectByIdList(List<String> idList);

    /**
     * 根据name批量查找
     * @param nameList name列表
     * @return
     */
    List<ZybYrdw> selectByNameList(List<String> nameList);

    /**
     * 根据creditCode查找
     */
    ZybYrdw selectByCreditCode(String creditCode);

    /**
     * 批量插入
     * @param zybYrdwList
     * @return
     */
    public boolean insertBatch(List<ZybYrdw> zybYrdwList);
}
