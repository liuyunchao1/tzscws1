package com.wondersgroup.tzscws1.controller;

import com.wondersgroup.tzscws1.constant.Constant;
import com.wondersgroup.tzscws1.model.ZybGak;
import com.wondersgroup.tzscws1.service.ProvincialPlatformService;
import com.wondersgroup.tzscws1.service.ZybGakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用省平台接口Controller
 */
@RestController
@RequestMapping("/provincialPlatform")
public class ProvincialPlatformController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProvincialPlatformService provincialPlatformService;

    @Autowired
    private ZybGakService zybGakService;

    /**
     * 手动调用上传省平台接口
     * @return
     */
    @PostMapping("callProvincialPlatform")
    public String callProvincialPlatform(@RequestBody(required = false) Map<String, Object> reqParams) {
        try {
            if (CollectionUtils.isEmpty(reqParams)) {
                return "参数为空";
            }

            List<String> idList = (List<String>) reqParams.get("idList");
            if (CollectionUtils.isEmpty(idList)) {
                return "参数为空";
            }

            Map<String, Object> params = new HashMap<>();
            params.put("idList", idList);
            List<ZybGak> list = zybGakService.selectForCallProvincial(params);    // 已审核，但未发送的数据
            if (CollectionUtils.isEmpty(list)) {
                return "没有需要发送的数据";
            }

            Map<String, Integer> result = provincialPlatformService.batchCallProvincialPlatform(list);
            String str = "成功数量: " + ((result != null && result.get(Constant.SUC_NUM) != null) ? result.get(Constant.SUC_NUM) : 0)
                    + " ; " + "失败数量: " + ((result != null && result.get(Constant.FAI_NUM) != null) ? result.get(Constant.FAI_NUM) : 0);
            return str;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "操作失败";
        }
    }
}
