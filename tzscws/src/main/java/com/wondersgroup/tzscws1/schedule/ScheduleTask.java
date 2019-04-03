package com.wondersgroup.tzscws1.schedule;

import com.wondersgroup.tzscws1.constant.Constant;
import com.wondersgroup.tzscws1.model.ZybGak;
import com.wondersgroup.tzscws1.model.ZybYrdw;
import com.wondersgroup.tzscws1.service.ZybGakService;
import com.wondersgroup.tzscws1.service.ZybYrdwService;
import com.wondersgroup.tzscws1.util.CommonUtils;
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

            // 批量获取企业信息
            Map<String, Map<String, ZybYrdw>> employerMap = batchGetEmployerMap(list);
            Map<String, ZybYrdw> idMap = employerMap.get("idMap");
            Map<String, ZybYrdw> nameMap = employerMap.get("nameMap");

            StringBuilder strXml = new StringBuilder();
            StringBuilder bodyStr = new StringBuilder();    //body标签另外做一个字符串用于加签
            StringBuilder employingStr = new StringBuilder();  //企业基本信息
            String eventId = "test";//业务请求类型编码  test免验证用于测试
            String hosId = list.get(0).getHosId();//医院编码
            String dateStr = CommonUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
            Verification verification = new Verification();
            String headSign = verification.MD5(eventId + dateStr + hosId + Constant.PASSORD);//用户秘钥
            String bodySign = "";//数据签名
            strXml.append("<data><header><eventId>");
            strXml.append(eventId);
            strXml.append("</eventId><hosId>");
            strXml.append(hosId);
            strXml.append("</hosId><requestTime>");
            strXml.append(dateStr);
            strXml.append("</requestTime><headSign>");
            strXml.append(headSign);
            strXml.append("</headSign><bodySign>");
            strXml.append(bodySign);
            strXml.append("</bodySign></header>");
            bodyStr.append("<body><reportCards>");

            ZybYrdw zybYrdw;    // 单个企业信息
            for (ZybGak gak : list) {
                bodyStr.append("<reportCard>")
                        .append("<code>").append(CommonUtils.getString(gak.getCode())).append("</code><hosId>")
                        .append(CommonUtils.getString(gak.getHosId())).append("</hosId><name>")
                        .append(CommonUtils.getString(gak.getName())).append("</name><idcard>")
                        .append(CommonUtils.getString(gak.getIdcard())).append("</idcard><bodyCheckType>")
                        .append(CommonUtils.getString(gak.getBodyCheckType())).append("</bodyCheckType><sexCode>")
                        .append(CommonUtils.getString(gak.getSexCode())).append("</sexCode><birthday>")
                        .append(CommonUtils.getString(CommonUtils.formatDate(gak.getBirthday(), "yyyy-MM-dd"))).append("</birthday><hazardCode>")
                        .append(CommonUtils.getString(gak.getHazardCode())).append("</hazardCode><hazardYear>")
                        .append(CommonUtils.getString(gak.getHazardYear())).append("</hazardYear><hazardMonth>")
                        .append(CommonUtils.getString(gak.getHazardMonth())).append("</hazardMonth><sysPressResult>")
                        .append(CommonUtils.getString(gak.getSysPressResult())).append("</sysPressResult><orgCode>")
                        .append(CommonUtils.getString(gak.getOrgCode())).append("</orgCode><employerName>")
                        .append(CommonUtils.getString(gak.getEmployerName())).append("</employerName><telPhone>")
                        .append(CommonUtils.getString(gak.getTelPhone())).append("</telPhone><seniorityYear>")
                        .append(CommonUtils.getString(gak.getSeniorityYear())).append("</seniorityYear><seniorityMonth>")
                        .append(CommonUtils.getString(gak.getSeniorityMonth())).append("</seniorityMonth><exposureYear>")
                        .append(CommonUtils.getString(gak.getExposureYear())).append("</exposureYear><exposureMonth>")
                        .append(CommonUtils.getString(gak.getExposureMonth())).append("</exposureMonth><workShop>")
                        .append(CommonUtils.getString(gak.getWorkShop())).append("</workShop><jobCode>")
                        .append(CommonUtils.getString(gak.getJobCode())).append("</jobCode><sysPressUnitName>")
                        .append(CommonUtils.getString(gak.getSysPressUnitName())).append("</sysPressUnitName><diasPressResult>")
                        .append(CommonUtils.getString(gak.getDiasPressResult())).append("</diasPressResult><diasPressUnitName>")
                        .append(CommonUtils.getString(gak.getDiasPressUnitName())).append("</diasPressUnitName><wbcResult>")
                        .append(CommonUtils.getString(gak.getWbcResult())).append("</wbcResult><wbcUnitName>")
                        .append(CommonUtils.getString(gak.getWbcUnitName())).append("</wbcUnitName><wbcMiniRange>")
                        .append(CommonUtils.getString(gak.getWbcMiniRange())).append("</wbcMiniRange><wbcMaxRange>")
                        .append(CommonUtils.getString(gak.getWbcMaxRange())).append("</wbcMaxRange><rbcResult>")
                        .append(CommonUtils.getString(gak.getRbcResult())).append("</rbcResult><rbcUnitName>")
                        .append(CommonUtils.getString(gak.getRbcUnitName())).append("</rbcUnitName><rbcMiniRange>")
                        .append(CommonUtils.getString(gak.getRbcMiniRange())).append("</rbcMiniRange><rbcMaxRange>")
                        .append(CommonUtils.getString(gak.getRbcMaxRange())).append("</rbcMaxRange><hbResult>")
                        .append(CommonUtils.getString(gak.getHbResult())).append("</hbResult><hbUnitName>")
                        .append(CommonUtils.getString(gak.getHbUnitName())).append("</hbUnitName><hbMiniRange>")
                        .append(CommonUtils.getString(gak.getHbMiniRange())).append("</hbMiniRange><hbMaxRange>")
                        .append(CommonUtils.getString(gak.getHbMaxRange())).append("</hbMaxRange><pltResult>")
                        .append(CommonUtils.getString(gak.getPltResult())).append("</pltResult><pltUnitName>")
                        .append(CommonUtils.getString(gak.getPltUnitName())).append("</pltUnitName><pltMiniRange>")
                        .append(CommonUtils.getString(gak.getPltMiniRange())).append("</pltMiniRange><pltMaxRange>")
                        .append(CommonUtils.getString(gak.getPltMaxRange())).append("</pltMaxRange><gluResult>")
                        .append(CommonUtils.getString(gak.getGluResult())).append("</gluResult><gluUnitName>")
                        .append(CommonUtils.getString(gak.getGluUnitName())).append("</gluUnitName><gluMiniRange>")
                        .append(CommonUtils.getString(gak.getGluMiniRange())).append("</gluMiniRange><gluMaxRange>")
                        .append(CommonUtils.getString(gak.getGluMaxRange())).append("</gluMaxRange><proResult>")
                        .append(CommonUtils.getString(gak.getProResult())).append("</proResult><proUnitName>")
                        .append(CommonUtils.getString(gak.getProUnitName())).append("</proUnitName><proMiniRange>")
                        .append(CommonUtils.getString(gak.getProMiniRange())).append("</proMiniRange><proMaxRange>")
                        .append(CommonUtils.getString(gak.getProMaxRange())).append("</proMaxRange><uwbcResult>")
                        .append(CommonUtils.getString(gak.getUwbcResult())).append("</uwbcResult><uwbcUnitName>")
                        .append(CommonUtils.getString(gak.getUwbcUnitName())).append("</uwbcUnitName><uwbcMiniRange>")
                        .append(CommonUtils.getString(gak.getUwbcMiniRange())).append("</uwbcMiniRange><uwbcMaxRange>")
                        .append(CommonUtils.getString(gak.getUwbcMaxRange())).append("</uwbcMaxRange><bldResult>")
                        .append(CommonUtils.getString(gak.getBldResult())).append("</bldResult><bldUnitName>")
                        .append(CommonUtils.getString(gak.getBldUnitName())).append("</bldUnitName><bldMiniRange>")
                        .append(CommonUtils.getString(gak.getBldMiniRange())).append("</bldMiniRange><bldMaxRange>")
                        .append(CommonUtils.getString(gak.getBldMaxRange())).append("</bldMaxRange><altResult>")
                        .append(CommonUtils.getString(gak.getAltResult())).append("</altResult><altUnitName>")
                        .append(CommonUtils.getString(gak.getAltUnitName())).append("</altUnitName><altMiniRange>")
                        .append(CommonUtils.getString(gak.getAltMiniRange())).append("</altMiniRange><altMaxRange>")
                        .append(CommonUtils.getString(gak.getAltMaxRange())).append("</altMaxRange><ecgCode>")
                        .append(CommonUtils.getString(gak.getEcgCode())).append("</ecgCode><chestCode>")
                        .append(CommonUtils.getString(gak.getChestCode())).append("</chestCode><fvcResult>")
                        .append(CommonUtils.getString(gak.getFvcResult())).append("</fvcResult><fvcUnitName>")
                        .append(CommonUtils.getString(gak.getFvcUnitName())).append("</fvcUnitName><fvcMiniRange>")
                        .append(CommonUtils.getString(gak.getFvcMiniRange())).append("</fvcMiniRange><fvcMaxRange>")
                        .append(CommonUtils.getString(gak.getFvcMaxRange())).append("</fvcMaxRange><fev1Result>")
                        .append(CommonUtils.getString(gak.getFev1Result())).append("</fev1Result><fev1UnitName>")
                        .append(CommonUtils.getString(gak.getFev1UnitName())).append("</fev1UnitName><fev1MiniRange>")
                        .append(CommonUtils.getString(gak.getFev1MiniRange())).append("</fev1MiniRange><fev1MaxRange>")
                        .append(CommonUtils.getString(gak.getFev1MaxRange())).append("</fev1MaxRange><fev1fvcResult>")
                        .append(CommonUtils.getString(gak.getFev1fvcResult())).append("</fev1fvcResult><fev1fvcUnitName>")
                        .append(CommonUtils.getString(gak.getFev1fvcUnitName())).append("</fev1fvcUnitName><fev1fvcMiniRange>")
                        .append(CommonUtils.getString(gak.getFev1fvcMiniRange())).append("</fev1fvcMiniRange><fev1fvcMaxRange>")
                        .append(CommonUtils.getString(gak.getFev1fvcMaxRange())).append("</fev1fvcMaxRange><bLeadResult>")
                        .append(CommonUtils.getString(gak.getbLeadResult())).append("</bLeadResult><bLeadUnitName>")
                        .append(CommonUtils.getString(gak.getbLeadUnitName())).append("</bLeadUnitName><bLeadMiniRange>")
                        .append(CommonUtils.getString(gak.getbLeadMiniRange())).append("</bLeadMiniRange><bLeadMaxRange>")
                        .append(CommonUtils.getString(gak.getbLeadMaxRange())).append("</bLeadMaxRange><uLeadResult>")
                        .append(CommonUtils.getString(gak.getuLeadResult())).append("</uLeadResult><uLeadUnitName>")
                        .append(CommonUtils.getString(gak.getuLeadUnitName())).append("</uLeadUnitName><uLeadMiniRange>")
                        .append(CommonUtils.getString(gak.getuLeadMiniRange())).append("</uLeadMiniRange><uLeadMaxRange>")
                        .append(CommonUtils.getString(gak.getuLeadMaxRange())).append("</uLeadMaxRange><zppResult>")
                        .append(CommonUtils.getString(gak.getZppResult())).append("</zppResult><zppUnitName>")
                        .append(CommonUtils.getString(gak.getZppUnitName())).append("</zppUnitName><zppMiniRange>")
                        .append(CommonUtils.getString(gak.getZppMiniRange())).append("</zppMiniRange><zppMaxRange>")
                        .append(CommonUtils.getString(gak.getZppMaxRange())).append("</zppMaxRange><neutResult>")
                        .append(CommonUtils.getString(gak.getNeutResult())).append("</neutResult><neutUnitName>")
                        .append(CommonUtils.getString(gak.getNeutUnitName())).append("</neutUnitName><neutMiniRange>")
                        .append(CommonUtils.getString(gak.getNeutMiniRange())).append("</neutMiniRange><neutMaxRange>")
                        .append(CommonUtils.getString(gak.getNeutMaxRange())).append("</neutMaxRange><hearingReuslt>")
                        .append(CommonUtils.getString(gak.getHearingReuslt())).append("</hearingReuslt><hearingUnitName>")
                        .append(CommonUtils.getString(gak.getHearingUnitName())).append("</hearingUnitName><hearingMiniRange>")
                        .append(CommonUtils.getString(gak.getHearingMiniRange())).append("</hearingMiniRange><hearingMaxRange>")
                        .append(CommonUtils.getString(gak.getHearingMaxRange())).append("</hearingMaxRange><rpbtCode>")
                        .append(CommonUtils.getString(gak.getRpbtCode())).append("</rpbtCode><wrightCode>")
                        .append(CommonUtils.getString(gak.getWrightCode())).append("</wrightCode><conclusionsCode>")
                        .append(CommonUtils.getString(gak.getConclusionsCode())).append("</conclusionsCode>")
                        .append("</reportCard>");

                //查询企业信息
                if (!StringUtils.isEmpty(CommonUtils.getString(gak.getOrgCode()))) {    // 有编号则按编号查，无编号再按名称查
                    zybYrdw = idMap.get(CommonUtils.getString(gak.getOrgCode()));
                } else if (!StringUtils.isEmpty(CommonUtils.getString(gak.getEmployerName()))) {
                    zybYrdw = nameMap.get(CommonUtils.getString(gak.getEmployerName()));
                } else {
                    zybYrdw = null;
                }

                if (zybYrdw != null) {
                    employingStr.append("<employingUnit><employerCode>")
                            .append(CommonUtils.getString(zybYrdw.getEmployerCode())).append("</employerCode><employerName>")
                            .append(CommonUtils.getString(zybYrdw.getEmployerName())).append("</employerName><employerDesc>")
                            .append(CommonUtils.getString(zybYrdw.getEmployerDesc())).append("</employerDesc><areaStandard>")
                            .append(CommonUtils.getString(zybYrdw.getAreaStandard())).append("</areaStandard><areaAddress>")
                            .append(CommonUtils.getString(zybYrdw.getAreaAddress())).append("</areaAddress><economicCode>")
                            .append(CommonUtils.getString(zybYrdw.getEconomicCode())).append("</economicCode><industryCateCode>")
                            .append(CommonUtils.getString(zybYrdw.getIndustryCateCode())).append("</industryCateCode><enterpriseCode>")
                            .append(CommonUtils.getString(zybYrdw.getEnterpriseCode())).append("</enterpriseCode><secondEmployerCode>")
                            .append(CommonUtils.getString(zybYrdw.getSecondEmployerCode())).append("</secondEmployerCode><secondEmployerName>")
                            .append(CommonUtils.getString(zybYrdw.getSecondEmployerName())).append("</secondEmployerName><postAddress>")
                            .append(CommonUtils.getString(zybYrdw.getPostAddress())).append("</postAddress><zipCode>")
                            .append(CommonUtils.getString(zybYrdw.getZipCode())).append("</zipCode><contactPerson>")
                            .append(CommonUtils.getString(zybYrdw.getContactPerson())).append("</contactPerson><contactPhone>")
                            .append(CommonUtils.getString(zybYrdw.getContactPhone())).append("</contactPhone><monitorOrgCode>")
                            .append(CommonUtils.getString(zybYrdw.getMonitorOrgCode())).append("</monitorOrgCode><monitorOrgName>")
                            .append(CommonUtils.getString(zybYrdw.getMonitorOrgName())).append("</monitorOrgName><remarks>")
                            .append(CommonUtils.getString(zybYrdw.getRemarks())).append("</remarks><sbbz>")
                            .append(CommonUtils.getString(zybYrdw.getSbbz())).append("</sbbz><sbyy>")
                            .append(CommonUtils.getString(zybYrdw.getSbyy())).append("</sbyy><sbsj>")
                            .append(CommonUtils.getString(zybYrdw.getSbsj())).append("</sbsj>").append("</employingUnit>");
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
     * 批量查找企业信息
     * @param list 个人案卡列表
     */
    private Map<String, Map<String, ZybYrdw>> batchGetEmployerMap(List<ZybGak> list) {
        Map<String, ZybYrdw> idMap = new HashMap<>();
        Map<String, ZybYrdw> nameMap = new HashMap<>();
        Map<String, Map<String, ZybYrdw>> resultMap = new HashMap<>();
        resultMap.put("idMap", idMap);
        resultMap.put("nameMap", nameMap);
        if (CollectionUtils.isEmpty(list)) {
            return resultMap;
        }

        // 批量查找企业信息
        List<String> employerCodeList = new ArrayList<>();  // 所有的企业id
        List<String> employerNameList = new ArrayList<>();  // 所有的企业名称
        for (ZybGak p : list) {
            if (!StringUtils.isEmpty(p.getOrgCode())) {
                employerCodeList.add(p.getOrgCode());
            } else if (!StringUtils.isEmpty(p.getEmployerName())) {
                employerNameList.add(p.getEmployerName());
            }
        }
        List<ZybYrdw> employerList1 = null;
        if (!CollectionUtils.isEmpty(employerCodeList)) {
            employerList1 = zybYrdwService.selectByIdList(employerCodeList);
        }
        List<ZybYrdw> employerList2 = null;
        if (!CollectionUtils.isEmpty(employerNameList)) {
            employerList2 = zybYrdwService.selectByNameList(employerNameList);
        }

//            Map<String, ZybYrdw> employerMap = employerList.stream().collect(Collectors.toMap(ZybYrdw :: getEmployerCode, p -> p, (k1, k2) -> k2));
        if (!CollectionUtils.isEmpty(employerList1)) {
            for (ZybYrdw p : employerList1) {
                idMap.put(p.getEmployerCode(), p);
            }
        }
        if (!CollectionUtils.isEmpty(employerList2)) {
            for (ZybYrdw p : employerList2) {
                nameMap.put(p.getEmployerName(), p);
            }
        }

        return resultMap;
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
