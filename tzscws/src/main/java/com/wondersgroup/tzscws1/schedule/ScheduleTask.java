package com.wondersgroup.tzscws1.schedule;

import com.wondersgroup.tzscws1.constant.Constant;
import com.wondersgroup.tzscws1.model.ZybGak;
import com.wondersgroup.tzscws1.model.ZybYrdw;
import com.wondersgroup.tzscws1.service.ZybGakService;
import com.wondersgroup.tzscws1.service.ZybYrdwService;
import com.wondersgroup.tzscws1.util.SoapWebServiceClient;
import com.wondersgroup.tzscws1.verification.Verification;
import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定时任务
 */
@Component
public class ScheduleTask {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ZybGakService zybGakService;

    @Autowired
    private ZybYrdwService zybYrdwService;

    /**
     * 调用上传省平台接口-定时
     *
     * @throws Exception
     */
    @Scheduled(fixedDelayString = "${callProvincialPlatformTime}" )
    public void callProvincialPlatform() {
        List<ZybGak> list = zybGakService.selectForCallProvincial();    // 已审核，但未发送的数据
        if (CollectionUtils.isEmpty(list)) {
            logger.info("==========没有数据要上传============");
            return;
        }
        for (ZybGak p : list) {
            if (StringUtils.isEmpty(p.getHosId())) {
                p.setHosId("");
            }
        }

        // 按医院编号分组，一次只发送同一个医院的
//        Map<String, List<ZybGak>> listMap = list.stream().collect(Collectors.groupingBy(ZybGak::getHosId));
        Map<String, List<ZybGak>> listMap = new HashMap<>();
        String hosid;
        for (ZybGak p : list) {
            hosid = p.getHosId();
            if (listMap.containsKey(hosid)) {
                listMap.get(hosid).add(p);
            } else {
                List<ZybGak> item = new ArrayList<>();
                item.add(p);
                listMap.put(hosid, item);
            }
        }

        for (Map.Entry<String, List<ZybGak>> en : listMap.entrySet()) {
            callProvincialPlatformHos(en.getValue());
        }
//        listMap.forEach((k, v) -> callProvincialPlatformHos(v));
    }

    /**
     * 调用上传省平台接口-单个医院
     */
    private void callProvincialPlatformHos(List<ZybGak> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        // 分批次调用上传省平台接口
        int dealNum = 50;   // 单次发送的数据量
        int insertLength = list.size();
        int i = 0;
        while (insertLength > dealNum) {
            callProvincialPlatform(list.subList(i, i + dealNum));
            i = i + dealNum;
            insertLength = insertLength - dealNum;
        }
        if (insertLength > 0) {
            callProvincialPlatform(list.subList(i, i + insertLength));
        }
    }

    /**
     * 调用上传省平台接口
     */
    private void callProvincialPlatform(List<ZybGak> list) {
        try {
            if (CollectionUtils.isEmpty(list)) {
                return;
            }

            // 批量查找企业信息
            List<String> employerCodeList = new ArrayList<>();  // 所有的企业id
            for (ZybGak p : list) {
                if (!StringUtils.isEmpty(p.getOrgCode())) {
                    employerCodeList.add(p.getOrgCode());
                }
            }
            List<ZybYrdw> employerList = zybYrdwService.selectByIdList(employerCodeList);
//            Map<String, ZybYrdw> employerMap = employerList.stream().collect(Collectors.toMap(ZybYrdw :: getEmployerCode, p -> p, (k1, k2) -> k2));
            Map<String, ZybYrdw> employerMap = new HashMap<>();
            for (ZybYrdw p : employerList) {
                employerMap.put(p.getEmployerCode(), p);
            }

            StringBuilder strXml = new StringBuilder();
            StringBuilder bodyStr = new StringBuilder();    //body标签另外做一个字符串用于加签
            StringBuilder employingStr = new StringBuilder();  //企业基本信息
            String eventId = "test";//业务请求类型编码  test免验证用于测试
            String hosId = list.get(0).getHosId();//医院编码
            Date date = new Date();
            Verification verification = new Verification();
            String headSign = verification.MD5(eventId + date + hosId + Constant.PASSORD);//用户秘钥
            String bodySign = "";//数据签名
            strXml.append("<data><header><eventId>");
            strXml.append(eventId);
            strXml.append("</eventId><hosId>");
            strXml.append(hosId);
            strXml.append("</hosId><requestTime>");
            strXml.append(date);
            strXml.append("</requestTime><headSign>");
            strXml.append(headSign);
            strXml.append("</headSign><bodySign>");
            strXml.append(bodySign);
            strXml.append("</bodySign></header>");
            bodyStr.append("<body><reportCards>");

            ZybYrdw zybYrdw;    // 单个企业信息
            for (ZybGak gak : list) {
                bodyStr.append("<reportCard>")
                        .append("<code>").append(gak.getCode()).append("</code><hosId>")
                        .append(gak.getHosId()).append("</hosId><name>")
                        .append(gak.getName()).append("</name><idcard>")
                        .append(gak.getIdcard()).append("</idcard><bodyCheckType>")
                        .append(gak.getBodyCheckType()).append("</bodyCheckType><sexCode>")
                        .append(gak.getSexCode()).append("</sexCode><birthday>")
                        .append(gak.getBirthday()).append("</birthday><hazardCode>")
                        .append(gak.getHazardCode()).append("</hazardCode><hazardYear>")
                        .append(gak.getHazardYear()).append("</hazardYear><hazardMonth>")
                        .append(gak.getHazardMonth()).append("</hazardMonth><sysPressResult>")
                        .append(gak.getSysPressResult()).append("</sysPressResult><orgCode>")
                        .append(gak.getOrgCode()).append("</orgCode><employerName>")
                        .append(gak.getEmployerName()).append("</employerName><telPhone>")
                        .append(gak.getTelPhone()).append("</telPhone><seniorityYear>")
                        .append(gak.getSeniorityYear()).append("</seniorityYear><seniorityMonth>")
                        .append(gak.getSeniorityMonth()).append("</seniorityMonth><exposureYear>")
                        .append(gak.getExposureYear()).append("</exposureYear><exposureMonth>")
                        .append(gak.getExposureMonth()).append("</exposureMonth><workShop>")
                        .append(gak.getWorkShop()).append("</workShop><jobCode>")
                        .append(gak.getJobCode()).append("</jobCode><sysPressUnitName>")
                        .append(gak.getSysPressUnitName()).append("</sysPressUnitName><diasPressResult>")
                        .append(gak.getDiasPressResult()).append("</diasPressResult><diasPressUnitName>")
                        .append(gak.getDiasPressUnitName()).append("</diasPressUnitName><wbcResult>")
                        .append(gak.getWbcResult()).append("</wbcResult><wbcUnitName>")
                        .append(gak.getWbcUnitName()).append("</wbcUnitName><wbcMiniRange>")
                        .append(gak.getWbcMiniRange()).append("</wbcMiniRange><wbcMaxRange>")
                        .append(gak.getWbcMaxRange()).append("</wbcMaxRange><rbcResult>")
                        .append(gak.getRbcResult()).append("</rbcResult><rbcUnitName>")
                        .append(gak.getRbcUnitName()).append("</rbcUnitName><rbcMiniRange>")
                        .append(gak.getRbcMiniRange()).append("</rbcMiniRange><rbcMaxRange>")
                        .append(gak.getRbcMaxRange()).append("</rbcMaxRange><hbResult>")
                        .append(gak.getHbResult()).append("</hbResult><hbUnitName>")
                        .append(gak.getHbUnitName()).append("</hbUnitName><hbMiniRange>")
                        .append(gak.getHbMiniRange()).append("</hbMiniRange><hbMaxRange>")
                        .append(gak.getHbMaxRange()).append("</hbMaxRange><pltResult>")
                        .append(gak.getPltResult()).append("</pltResult><pltUnitName>")
                        .append(gak.getPltUnitName()).append("</pltUnitName><pltMiniRange>")
                        .append(gak.getPltMiniRange()).append("</pltMiniRange><pltMaxRange>")
                        .append(gak.getPltMaxRange()).append("</pltMaxRange><gluResult>")
                        .append(gak.getGluResult()).append("</gluResult><gluUnitName>")
                        .append(gak.getGluUnitName()).append("</gluUnitName><gluMiniRange>")
                        .append(gak.getGluMiniRange()).append("</gluMiniRange><gluMaxRange>")
                        .append(gak.getGluMaxRange()).append("</gluMaxRange><proResult>")
                        .append(gak.getProResult()).append("</proResult><proUnitName>")
                        .append(gak.getProUnitName()).append("</proUnitName><proMiniRange>")
                        .append(gak.getProMiniRange()).append("</proMiniRange><proMaxRange>")
                        .append(gak.getProMaxRange()).append("</proMaxRange><uwbcResult>")
                        .append(gak.getUwbcResult()).append("</uwbcResult><uwbcUnitName>")
                        .append(gak.getUwbcUnitName()).append("</uwbcUnitName><uwbcMiniRange>")
                        .append(gak.getUwbcMiniRange()).append("</uwbcMiniRange><uwbcMaxRange>")
                        .append(gak.getUwbcMaxRange()).append("</uwbcMaxRange><bldResult>")
                        .append(gak.getBldResult()).append("</bldResult><bldUnitName>")
                        .append(gak.getBldUnitName()).append("</bldUnitName><bldMiniRange>")
                        .append(gak.getBldMiniRange()).append("</bldMiniRange><bldMaxRange>")
                        .append(gak.getBldMaxRange()).append("</bldMaxRange><altResult>")
                        .append(gak.getAltResult()).append("</altResult><altUnitName>")
                        .append(gak.getAltUnitName()).append("</altUnitName><altMiniRange>")
                        .append(gak.getAltMiniRange()).append("</altMiniRange><altMaxRange>")
                        .append(gak.getAltMaxRange()).append("</altMaxRange><ecgCode>")
                        .append(gak.getEcgCode()).append("</ecgCode><chestCode>")
                        .append(gak.getChestCode()).append("</chestCode><fvcResult>")
                        .append(gak.getFvcResult()).append("</fvcResult><fvcUnitName>")
                        .append(gak.getFvcUnitName()).append("</fvcUnitName><fvcMiniRange>")
                        .append(gak.getFvcMiniRange()).append("</fvcMiniRange><fvcMaxRange>")
                        .append(gak.getFvcMaxRange()).append("</fvcMaxRange><fev1Result>")
                        .append(gak.getFev1Result()).append("</fev1Result><fev1UnitName>")
                        .append(gak.getFev1UnitName()).append("</fev1UnitName><fev1MiniRange>")
                        .append(gak.getFev1MiniRange()).append("</fev1MiniRange><fev1MaxRange>")
                        .append(gak.getFev1MaxRange()).append("</fev1MaxRange><fev1fvcResult>")
                        .append(gak.getFev1fvcResult()).append("</fev1fvcResult><fev1fvcUnitName>")
                        .append(gak.getFev1fvcUnitName()).append("</fev1fvcUnitName><fev1fvcMiniRange>")
                        .append(gak.getFev1fvcMiniRange()).append("</fev1fvcMiniRange><fev1fvcMaxRange>")
                        .append(gak.getFev1fvcMaxRange()).append("</fev1fvcMaxRange><bLeadResult>")
                        .append(gak.getbLeadResult()).append("</bLeadResult><bLeadUnitName>")
                        .append(gak.getbLeadUnitName()).append("</bLeadUnitName><bLeadMiniRange>")
                        .append(gak.getbLeadMiniRange()).append("</bLeadMiniRange><bLeadMaxRange>")
                        .append(gak.getbLeadMaxRange()).append("</bLeadMaxRange><uLeadResult>")
                        .append(gak.getuLeadResult()).append("</uLeadResult><uLeadUnitName>")
                        .append(gak.getuLeadUnitName()).append("</uLeadUnitName><uLeadMiniRange>")
                        .append(gak.getuLeadMiniRange()).append("</uLeadMiniRange><uLeadMaxRange>")
                        .append(gak.getuLeadMaxRange()).append("</uLeadMaxRange><zppResult>")
                        .append(gak.getZppResult()).append("</zppResult><zppUnitName>")
                        .append(gak.getZppUnitName()).append("</zppUnitName><zppMiniRange>")
                        .append(gak.getZppMiniRange()).append("</zppMiniRange><zppMaxRange>")
                        .append(gak.getZppMaxRange()).append("</zppMaxRange><neutResult>")
                        .append(gak.getNeutResult()).append("</neutResult><neutUnitName>")
                        .append(gak.getNeutUnitName()).append("</neutUnitName><neutMiniRange>")
                        .append(gak.getNeutMiniRange()).append("</neutMiniRange><neutMaxRange>")
                        .append(gak.getNeutMaxRange()).append("</neutMaxRange><hearingReuslt>")
                        .append(gak.getHearingReuslt()).append("</hearingReuslt><hearingUnitName>")
                        .append(gak.getHearingUnitName()).append("</hearingUnitName><hearingMiniRange>")
                        .append(gak.getHearingMiniRange()).append("</hearingMiniRange><hearingMaxRange>")
                        .append(gak.getHearingMaxRange()).append("</hearingMaxRange><rpbtCode>")
                        .append(gak.getRpbtCode()).append("</rpbtCode><wrightCode>")
                        .append(gak.getWrightCode()).append("</wrightCode><conclusionsCode>")
                        .append(gak.getConclusionsCode()).append("</conclusionsCode>")
                        .append("</reportCard>");

                //查询企业信息
                zybYrdw = (employerMap != null && employerMap.containsKey(gak.getOrgCode())) ? employerMap.get(gak.getOrgCode()) : null;
                if (zybYrdw != null) {
                    employingStr.append("<employingUnit><employerCode>")
                            .append(zybYrdw.getEmployerCode()).append("</employerCode><employerName>")
                            .append(zybYrdw.getEmployerName()).append("</employerName><employerDesc>")
                            .append(zybYrdw.getEmployerDesc()).append("</employerDesc><areaStandard>")
                            .append(zybYrdw.getAreaStandard()).append("</areaStandard><areaAddress>")
                            .append(zybYrdw.getAreaAddress()).append("</areaAddress><economicCode>")
                            .append(zybYrdw.getEconomicCode()).append("</economicCode><industryCateCode>")
                            .append(zybYrdw.getIndustryCateCode()).append("</industryCateCode><enterpriseCode>")
                            .append(zybYrdw.getEnterpriseCode()).append("</enterpriseCode><secondEmployerCode>")
                            .append(zybYrdw.getSecondEmployerCode()).append("</secondEmployerCode><secondEmployerName>")
                            .append(zybYrdw.getSecondEmployerName()).append("</secondEmployerName><postAddress>")
                            .append(zybYrdw.getPostAddress()).append("</postAddress><zipCode>")
                            .append(zybYrdw.getZipCode()).append("</zipCode><contactPerson>")
                            .append(zybYrdw.getContactPerson()).append("</contactPerson><contactPhone>")
                            .append(zybYrdw.getContactPhone()).append("</contactPhone><monitorOrgCode>")
                            .append(zybYrdw.getMonitorOrgCode()).append("</monitorOrgCode><monitorOrgName>")
                            .append(zybYrdw.getMonitorOrgName()).append("</monitorOrgName><remarks>")
                            .append(zybYrdw.getRemarks()).append("</remarks><sbbz>")
                            .append(zybYrdw.getSbbz()).append("</sbbz><sbyy>")
                            .append(zybYrdw.getSbyy()).append("</sbyy><sbsj>")
                            .append(zybYrdw.getSbsj()).append("</sbsj><logsj>")
                            .append(zybYrdw.getLogsj()).append("</logsj>").append("</employingUnit>");
                }
            }
            bodyStr.append("</reportCards><employingUnits>").append(employingStr).append("</employingUnits></body>");
            //bodySign  加签

            strXml.append(bodyStr).append("</data>");
            //调用省平台的接口
            SoapWebServiceClient soap = new SoapWebServiceClient();
            Object resultJson = soap.callWebService("https://www.xiaoyisheng.net.cn/ws_data/ws/TJ?wsdl", "transport",
                    strXml.toString());

            JsonConfig jsonConfig = new JsonConfig();
            jsonConfig.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);
            JSONArray jsonArray = JSONArray.fromObject(resultJson, jsonConfig);
            Object result = jsonArray.get(0);
            logger.info("begin=====调用上传省平台接口返回结果=====begin");
            logger.info(result != null ? result.toString() : "");
            logger.info("end=====调用上传省平台接口返回结果=====end");

            Map<String, Object> resultMap = parseXmlStr2Map(result != null ? result.toString() : "");   // 解析返回的结果
            updateData(list, resultMap);    // 根据省平台接口返回的结果，回写更新数据状态
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @description 将省平台接口返回的xml字符串解析换成map
     * @param xml
     * @return Map
     */
    private Map<String, Object> parseXmlStr2Map(String xml) {
        if (StringUtils.isEmpty(xml)) {
            return new HashMap<>();
        }
        Map<String, Object> map = new HashMap();
        Document doc;
        try {
            // 将字符串转为XML
            doc = DocumentHelper.parseText(xml);
            // 获取根节点
            Element rootElt = doc.getRootElement();

            // 解析resultCode
            Element returnCodeEl = rootElt.element("returnCode");
            map.put("returnCode", returnCodeEl.getStringValue());
            // 解析message，如果有的话
            Element messageEl = rootElt.element("message");
            if (messageEl != null) {
                map.put("message", messageEl.getStringValue());
            }

            // 解析errorDatas中的节点
            Element errorDatasEl = rootElt.element("errorDatas");
            if (errorDatasEl == null) {
                return map;
            }
            Element errorReportCardsEl = errorDatasEl.element("errorReportCards");
            if (errorReportCardsEl == null) {
                return map;
            }

            Map<String, String> errorDataMap = new HashMap<>(); // 每条个案卡数据的错误消息，如果有的话,code+hosId -> errorMessage
            Iterator errorReportCardsIterator = errorReportCardsEl.elementIterator();
            while (errorReportCardsIterator.hasNext()) {
                Element errorDataEl = (Element) errorReportCardsIterator.next();
                Element reportCardEl = errorDataEl.element("reportCard");
                errorDataMap.put(getElementStr(reportCardEl, "code") + getElementStr(reportCardEl, "hosId"), getElementStr(errorDataEl, "errorMessage"));
            }
            map.put("errorDataMap", errorDataMap);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return map;
    }

    /**
     * 获取节点的字符串值，空时返回""，避免null值
     * @param element
     * @param name
     * @return
     */
    private String getElementStr(Element element, String name) {
        if (element == null || StringUtils.isEmpty(name)) {
            return "";
        }
        String value = element.elementTextTrim(name);
        return value != null ? value : "";
    }

    /**
     * 根据省平台接口返回的结果，回写更新数据状态
     * @param list
     * @param resultMap
     */
    private void updateData(List<ZybGak> list, Map<String, Object> resultMap) {
        if (CollectionUtils.isEmpty(list) || CollectionUtils.isEmpty(resultMap)) {
            return;
        }
        String returnCode = resultMap.get("returnCode") != null ? resultMap.get("returnCode").toString() : "";  // 返回码
        ZybGak updateObj;
        Date updateDate = new Date();
        if ("0".equals(returnCode)) { // 上传成功
            logger.info("==============上传成功=========");
            for (ZybGak gak : list) {
                updateObj = new ZybGak();
                updateObj.setId(gak.getId());
                updateObj.setSbbz("1");
                updateObj.setSbsj(updateDate);
                zybGakService.updateByPrimaryKeySelective(updateObj);
            }
            return;
        }

        // 上传失败时，更新状态和原因，如果某个案卡有单独返回的错误原因，则更新成此原因，如果没有，则更新成总的message
        String message = resultMap.get("message") != null ? resultMap.get("message").toString() : "";   // 错误说明
        Map<String, String> errorDataMap = resultMap.containsKey("errorDataMap") ? (Map<String, String>) resultMap.get("errorDataMap") : null;  // 每条个案卡数据的错误消息
        logger.info("===========上传失败=========");
        for (ZybGak gak : list) {
            updateObj = new ZybGak();
            updateObj.setId(gak.getId());
            updateObj.setSbbz("2");
            updateObj.setSbsj(updateDate);
            updateObj.setSbyy((errorDataMap != null && errorDataMap.containsKey(gak.getCode() + gak.getHosId())) ? errorDataMap.get(gak.getCode() + gak.getHosId()) : message);
            zybGakService.updateByPrimaryKeySelective(updateObj);
        }
    }
}
