package com.wondersgroup.tzscws1.schedule;

import com.wondersgroup.tzscws1.constant.Constant;
import com.wondersgroup.tzscws1.model.ZybGak;
import com.wondersgroup.tzscws1.service.ProvincialPlatformService;
import com.wondersgroup.tzscws1.service.ZybGakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * 定时任务
 */
@Component
public class ScheduleTask {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ZybGakService zybGakService;

    @Autowired
    private ProvincialPlatformService provincialPlatformService;

    /**
     * 调用上传省平台接口-定时
     *
     * @throws Exception
     */
    @Scheduled(fixedDelayString = "${callProvincialPlatformTime}" )
    public void callProvincialPlatform() {
        logger.info("定时上传省平台接口开始：");
        long beginTime = System.currentTimeMillis();

        List<ZybGak> list = zybGakService.selectForCallProvincial(null);    // 已审核，但未发送的数据
        if (CollectionUtils.isEmpty(list)) {
            logger.info("==========没有数据要上传============");
            return;
        }

        Map<String, Integer> result = provincialPlatformService.batchCallProvincialPlatform(list);
        logger.info("成功数量: " + ((result != null && result.get(Constant.SUC_NUM) != null) ? result.get(Constant.SUC_NUM) : 0));
        logger.info("失败数量: " + ((result != null && result.get(Constant.FAI_NUM) != null) ? result.get(Constant.FAI_NUM) : 0));
        logger.info("定时上传省平台接口结束，耗时: " + (System.currentTimeMillis() - beginTime) + " ms");
    }


}
