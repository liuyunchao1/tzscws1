package com.wondersgroup.tzscws1.controller;

import com.wondersgroup.tzscws1.entity.BodyDataEntity;
import com.wondersgroup.tzscws1.entity.HeaderDataEntty;
import com.wondersgroup.tzscws1.model.CodeInfo;
import com.wondersgroup.tzscws1.model.ZybGak;
import com.wondersgroup.tzscws1.model.ZybYrdw;
import com.wondersgroup.tzscws1.service.CodeInfoServiceImpl;
import com.wondersgroup.tzscws1.service.ZybGakServiceImpl;
import com.wondersgroup.tzscws1.service.ZybYrdwServiceImpl;
import com.wondersgroup.tzscws1.util.CommonUtils;
import com.wondersgroup.tzscws1.verification.Verification;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;


@RestController
@RequestMapping(value = "/tzscws")
public class ReadStringXmlController {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 测试解析xml字符串
     *
     * @param args
     */
    @Autowired
    private ZybGakServiceImpl zybGakServiceImpl;
    @Autowired
    private ZybYrdwServiceImpl zybYrdwServiceImpl;
    @Autowired
    private CodeInfoServiceImpl codeInfoServiceImpl;

    public static final String SUCCED = "suceed";
    boolean  hazardCodeInflag = false;

    /**
     * 解析header 头部返回HeaderDataEntty对像
     *
     * @param xml
     */
    private HeaderDataEntty readStringHeader(String xml) {

        HeaderDataEntty headerData = new HeaderDataEntty();
        Document doc = null;
        try {
            // 读取并解析XML文档
            // SAXReader就是一个管道，用一个流的方式，把xml文件读出来
            //
            // SAXReader reader = new SAXReader(); //User.hbm.xml表示你要解析的xml文档
            // Document document = reader.read(new File("User.hbm.xml"));
            // 下面的是通过解析xml字符串的new String(text.getBytes("gbk")，"utf-8");
            doc = DocumentHelper.parseText(xml); // 将字符串转为XML
            Element rootElt = doc.getRootElement(); // 获取根节点
            logger.info("根节点：" + rootElt.getName()); // 拿到根节点的名称
            Iterator iter = rootElt.elementIterator("header"); // 获取根节点下的子节点header

            // 遍历header节点
            while (iter.hasNext()) {
                Element recordEle = (Element) iter.next();
                String nodeName = recordEle.getName();
                logger.info(nodeName);
//               String title = recordEle.elementTextTrim("title"); // 拿到head节点下的子节点title值
                Iterator iters = recordEle.elementIterator();// 拿到head节点下的所有子节点值
                while (iters.hasNext()) {
                    Element itemEle = (Element) iters.next();
                    String nodeNames = itemEle.getName();
                    if (nodeNames.equals("eventId")) {
                        headerData.setEventId(itemEle.getStringValue());
                    }
                    if (nodeNames.equals("hosId")) {
                        headerData.setHosId(itemEle.getStringValue());
                    }
                    if (nodeNames.equals("requestTime")) {
                        headerData.setRequestTime(itemEle.getStringValue());
                    }
                    if (nodeNames.equals("headSign")) {
                        headerData.setHeadSign(itemEle.getStringValue());
                    }
                    if (nodeNames.equals("bodySign")) {
                        headerData.setBodySign(itemEle.getStringValue());
                    }

                }

            }
        } catch (DocumentException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();

        }
        return headerData;

    }
    @ResponseBody
    @RequestMapping(value="/dataExchange",method = RequestMethod.POST)
    public String reportZybData(@RequestParam("reqData") String xml) {
        //返回xml格式字符串
        logger.info("======请求入参======"+xml);
        Document retDoc = DocumentHelper.createDocument();
        retDoc.setXMLEncoding("UTF-8");
        Element dataInfo = retDoc.addElement("data");
        Element returnCode = dataInfo.addElement("returnCode");
        Element message = dataInfo.addElement("message");
        List<BodyDataEntity> dateStr = new ArrayList<BodyDataEntity>();
        List<BodyDataEntity> empDateStr = new ArrayList<BodyDataEntity>();
        List<BodyDataEntity> dateStrUpdate = new ArrayList<BodyDataEntity>();
        List<BodyDataEntity> empDateStrUpdate = new ArrayList<BodyDataEntity>();
        //用于后续判断数据是否全部成功入库
        List<Boolean>  empDataStrFlag = new ArrayList<Boolean>();
        List<Boolean>  dateStrFlag = new ArrayList<Boolean>();
        if(StringUtils.isEmpty(xml)){
            returnCode.setText("105");
            message.setText("请求参数为空!");
            logger.info("请求参数为空!"+retDoc.toString());
            return retDoc.asXML();
        }
        HeaderDataEntty headerDatas = readStringHeader(xml);
        boolean flag = false;
        if (headerDatas != null) {
            Verification verification = new Verification();
            flag = verification.verHeadSign(headerDatas);
            logger.info(flag + "");
        }
        if (flag) {
            returnCode.setText("201");
            message.setText("秘钥校验错误!");
            logger.info("秘钥校验错误!");
            return retDoc.asXML();
        }
        Document doc = null;
        ArrayList<ZybGak> zybGakList =  new ArrayList<ZybGak>();
        ArrayList<ZybYrdw> zybYrdwList =  new ArrayList<ZybYrdw>();

        try {
            // 读取并解析XML文档
            // 下面的是通过解析xml字符串的
            doc = DocumentHelper.parseText(xml); // 将字符串转为XML
            Element rootElt = doc.getRootElement(); // 获取根节点
            logger.info("根节点：" + rootElt.getName()); // 拿到根节点的名称
//          Iterator iterss = rootElt.elementIterator("Elt"); ///获取根节点下的子节点body
            Element bodyElt = rootElt.element("body");
            logger.info("bodyElt：" + bodyElt.getName()); // 拿到根节点的名称
            Element reportCardsElt = bodyElt.element("reportCards");
            Iterator iterss = reportCardsElt.elementIterator("reportCard");
            Element employingUnits = bodyElt.element("employingUnits");
            logger.info("bodyElt：" + employingUnits.getName()); // 拿到根节点的名称
            Iterator employingUnitIte = employingUnits.elementIterator("employingUnit");
            // 遍历reportCard节点
             String codeResult = "";
             String hosIdResult = "";
             String nameResult= "";
             String idcardResult= "";
             String bodyCheckTypeResult= "";
             String bodyCheckTimeResult="";
             String sexCodeResult= "";
             String birthdayResult= "";
             String hazardCodeResult= "";//多种结果之间使用英文逗号（,）分隔
             String hazardYearResult= "";
             String hazardMonthResult= "";
             String sysPressResult= "";
             String diasPressResult= "";
             String ECGCodeResult= "";//多种结果之间使用英文逗号（,）分隔
             String conclusionsCodeResult= "";
             String orgCodeResult= "";
             String employerNameResult= "";
             String telPhoneResult= "";
             String seniorityYearResult= "";
             String seniorityMonthResult= "";
             String exposureYearResult= "";
             String exposureMonthResult= "";
             String workShopResult= "";
             String jobCodeResult= "";
             String sysPressUnitNameResult= "";
             String diasPressUnitNameResult= "";
             String WBCResult= "";
             String WBCUnitNameResult= "";
             String WBCMiniRangeResult= "";
             String WBCMaxRangeResult= "";
             String RBCResult= "";
             String RBCUnitNameResult= "";
             String RBCMiniRangeResult= "";
             String RBCMaxRangeResult= "";
             String HbResult= "";
             String HbUnitNameResult= "";
             String HbMiniRangeResult= "";
             String HbMaxRangeResult= "";
             String PLTResult= "";
             String PLTUnitNameResult= "";
             String PLTMiniRangeResult= "";
             String PLTMaxRangeResult= "";
             String BGLUResult= "";
             String BGLUUnitName= "";
             String BGLUMiniRange= "";
             String BGLUMaxRange= "";
             String GLUResult= "";
             String GLUUnitNameResult= "";
             String GLUMiniRangeResult= "";
             String GLUMaxRangeResult= "";
             String PROResult= "";
             String PROUnitNameResult= "";
             String PROMiniRangeResult= "";
             String PROMaxRangeResult= "";
             String UWBCResult= "";
             String UWBCUnitNameResult= "";
             String UWBCMiniRangeResult= "";
             String UWBCMaxRangeResult= "";
             String BLDResult= "";
             String BLDUnitNameResult= "";
             String BLDMiniRangeResult= "";
             String BLDMaxRangeResult= "";
             String ALTResult= "";
             String ALTUnitNameResult= "";
             String ALTMiniRangeResult= "";
             String ALTMaxRangeResult= "";
             String CHESTCodeResult = "";
             String FVCResult= "";
             String FVCUnitNameResult= "";
             String FVCMiniRangeResult= "";
             String FVCMaxRangeResult= "";
             String FEV1Result= "";
             String FEV1UnitNameResult= "";
             String FEV1MiniRangeResult= "";
             String FEV1MaxRangeResult= "";
             String FEV1FVCResult= "";
             String FEV1FVCUnitNameResult= "";
             String FEV1FVCMiniRangeResult= "";
             String FEV1FVCMaxRangeResult= "";
             String BLeadResult= "";
             String BLeadUnitNameResult= "";
             String BLeadMiniRangeResult= "";
             String BLeadMaxRangeResult= "";
             String ULeadResult= "";
             String ULeadUnitNameResult= "";
             String ULeadMiniRangeResult= "";
             String ULeadMaxRangeResult= "";
             String ZPPResult= "";
             String ZPPUnitNameResult= "";
             String ZPPMiniRangeResult= "";
             String ZPPMaxRangeResult= "";
             String NeutResult= "";
             String NeutUnitNameResult= "";
             String NeutMiniRangeResult= "";
             String NeutMaxRangeResult= "";
             String hearingReuslt= "";
             String hearingUnitNameResult= "";
             String hearingMiniRangeResult= "";
             String hearingMaxRangeResult= "";
             String RPBTCodeResult= "";
             String wrightCodeResult= "";
             //用人单位
            String creditCode = "";
            String employerCode = "";
            String employerNameEN = "";
            String employerDesc = "";
            String areaStandard = "";
            String areaAddress = "";
            String economicCode = "";
            String industryCateCode = "";
            String enterpriseCode = "";
            String secondEmployerCode = "";
            String secondEmployerName = "";
            String postAddress = "";
            String zipCode = "";
            String contactPerson = "";
            String contactPhone = "";
            String monitorOrgCode = "";
            String monitorOrgName = "";
            String remarks = "";
            SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDD");
            while (iterss.hasNext()) {
                //数据校验实体
                BodyDataEntity bodyDataEntity = new BodyDataEntity();
                BodyDataEntity zybYrdwDateStr = new BodyDataEntity();
                Element recordEless = (Element) iterss.next();
                //数据入库实体
                codeResult = recordEless.elementTextTrim("code");
                bodyDataEntity.setCode(codeResult);
                hosIdResult = recordEless.elementTextTrim("hosId");
                bodyDataEntity.setHosId(hosIdResult);
                nameResult = recordEless.elementTextTrim("name");
                bodyDataEntity.setName(nameResult);
                idcardResult = recordEless.elementTextTrim("idCard");
                logger.info("--idcardResult--"+idcardResult);
                bodyDataEntity.setIdcard(idcardResult);
                bodyCheckTypeResult = recordEless.elementTextTrim("bodyCheckType");
                bodyDataEntity.setBodyCheckType(bodyCheckTypeResult);
                bodyCheckTimeResult = recordEless.elementTextTrim("bodyCheckTime");
                bodyDataEntity.setBodyCheckTime(bodyCheckTimeResult);
                sexCodeResult = recordEless.elementTextTrim("sexCode");
                bodyDataEntity.setSexCode(sexCodeResult);
                birthdayResult = recordEless.elementTextTrim("birthday");
                bodyDataEntity.setBirthday(birthdayResult);
                hazardCodeResult = recordEless.elementTextTrim("hazardCode");//多种结果之间使用英文逗号（,）分隔
                bodyDataEntity.setHazardCode(hazardCodeResult);
                hazardYearResult = recordEless.elementTextTrim("hazardYear");
                bodyDataEntity.setHazardYear(hazardYearResult);
                hazardMonthResult = recordEless.elementTextTrim("hazardMonth");
                bodyDataEntity.setHazardMonth(hazardMonthResult);
                sysPressResult = recordEless.elementTextTrim("sysPressResult");
                bodyDataEntity.setSysPressResult(sysPressResult);
                diasPressResult = recordEless.elementTextTrim("diasPressResult");
                bodyDataEntity.setDiasPressResult(diasPressResult);
                ECGCodeResult = recordEless.elementTextTrim("ECGCode");//多种结果之间使用英文逗号（,）分隔
                bodyDataEntity.setECGCode(ECGCodeResult);
                conclusionsCodeResult = recordEless.elementTextTrim("conclusionsCode");
                bodyDataEntity.setConclusionsCode(conclusionsCodeResult);
                telPhoneResult = recordEless.elementTextTrim("telPhone");
                bodyDataEntity.setTelPhone(telPhoneResult);
                seniorityYearResult = recordEless.elementTextTrim("seniorityYear");
                bodyDataEntity.setSeniorityYear(seniorityYearResult);
                seniorityMonthResult = recordEless.elementTextTrim("seniorityMonth");
                bodyDataEntity.setSeniorityMonth(seniorityMonthResult);
                exposureYearResult = recordEless.elementTextTrim("exposureYear");
                bodyDataEntity.setExposureYear(exposureYearResult);
                exposureMonthResult = recordEless.elementTextTrim("exposureMonth");
                bodyDataEntity.setExposureMonth(exposureMonthResult);
                workShopResult = recordEless.elementTextTrim("workShop");
                bodyDataEntity.setWorkShop(workShopResult);
                jobCodeResult = recordEless.elementTextTrim("jobCode");
                bodyDataEntity.setJobCode(jobCodeResult);
                sysPressUnitNameResult = recordEless.elementTextTrim("sysPressUnitName");
                bodyDataEntity.setSysPressUnitName(sysPressUnitNameResult);
                diasPressUnitNameResult = recordEless.elementTextTrim("diasPressUnitName");
                bodyDataEntity.setDiasPressUnitName(diasPressUnitNameResult);
                WBCResult = recordEless.elementTextTrim("WBCResult");
                bodyDataEntity.setWBCResult(WBCResult);
                WBCUnitNameResult = recordEless.elementTextTrim("WBCUnitName");
                bodyDataEntity.setWBCUnitName(WBCUnitNameResult);
                WBCMiniRangeResult = recordEless.elementTextTrim("WBCMiniRange");
                bodyDataEntity.setWBCMiniRange(WBCMiniRangeResult);
                WBCMaxRangeResult = recordEless.elementTextTrim("WBCMaxRange");
                bodyDataEntity.setWBCMaxRange(WBCMaxRangeResult);
                RBCResult = recordEless.elementTextTrim("RBCResult");
                bodyDataEntity.setRBCResult(RBCResult);
                RBCUnitNameResult = recordEless.elementTextTrim("RBCUnitName");
                bodyDataEntity.setRBCUnitName(RBCUnitNameResult);
                RBCMiniRangeResult = recordEless.elementTextTrim("RBCMiniRange");
                bodyDataEntity.setRBCMiniRange(RBCMiniRangeResult);
                RBCMaxRangeResult = recordEless.elementTextTrim("RBCMaxRange");
                bodyDataEntity.setRBCMaxRange(RBCMaxRangeResult);
                HbResult = recordEless.elementTextTrim("HbResult");
                bodyDataEntity.setHbResult(HbResult);
                HbUnitNameResult = recordEless.elementTextTrim("HbUnitName");
                bodyDataEntity.setHbUnitName(HbUnitNameResult);
                HbMiniRangeResult = recordEless.elementTextTrim("HbMiniRange");
                bodyDataEntity.setHbMiniRange(HbMiniRangeResult);
                HbMaxRangeResult = recordEless.elementTextTrim("HbMaxRange");
                bodyDataEntity.setHbMaxRange(HbMaxRangeResult);
                PLTResult = recordEless.elementTextTrim("PLTResult");
                bodyDataEntity.setPLTResult(PLTResult);
                PLTUnitNameResult = recordEless.elementTextTrim("PLTUnitName");
                bodyDataEntity.setPLTUnitName(PLTUnitNameResult);
                PLTMiniRangeResult = recordEless.elementTextTrim("PLTMiniRange");
                bodyDataEntity.setPLTMiniRange(PLTMiniRangeResult);
                PLTMaxRangeResult = recordEless.elementTextTrim("PLTMaxRange");
                bodyDataEntity.setPLTMaxRange(PLTMaxRangeResult);
                BGLUResult = recordEless.elementTextTrim("BGLUResult");
                bodyDataEntity.setBGLUResult(BGLUResult);
                BGLUUnitName = recordEless.elementTextTrim("BGLUUnitName");
                bodyDataEntity.setBGLUUnitName(BGLUUnitName);
                BGLUMiniRange = recordEless.elementTextTrim("BGLUMiniRange");
                bodyDataEntity.setBGLUMiniRange(BGLUMiniRange);
                BGLUMaxRange = recordEless.elementTextTrim("BGLUMaxRange");
                bodyDataEntity.setBGLUMaxRange(BGLUMaxRange);
                GLUResult = recordEless.elementTextTrim("GLUResult");
                bodyDataEntity.setGLUResult(GLUResult);
                GLUUnitNameResult = recordEless.elementTextTrim("GLUUnitName");
                bodyDataEntity.setGLUUnitName(GLUUnitNameResult);
                GLUMiniRangeResult = recordEless.elementTextTrim("GLUMiniRange");
                bodyDataEntity.setGLUMiniRange(GLUMiniRangeResult);
                GLUMaxRangeResult = recordEless.elementTextTrim("GLUMaxRange");
                bodyDataEntity.setGLUMaxRange(GLUMaxRangeResult);
                PROResult = recordEless.elementTextTrim("PROResult");
                bodyDataEntity.setPROResult(PROResult);
                PROUnitNameResult = recordEless.elementTextTrim("PROUnitName");
                bodyDataEntity.setPROUnitName(PROUnitNameResult);
                PROMiniRangeResult = recordEless.elementTextTrim("PROMiniRange");
                bodyDataEntity.setPROMiniRange(PROMiniRangeResult);
                PROMaxRangeResult = recordEless.elementTextTrim("PROMaxRange");
                bodyDataEntity.setPROMaxRange(PROMaxRangeResult);
                UWBCResult = recordEless.elementTextTrim("UWBCResult");
                bodyDataEntity.setUWBCResult(UWBCResult);
                UWBCUnitNameResult = recordEless.elementTextTrim("UWBCUnitName");
                bodyDataEntity.setUWBCUnitName(UWBCUnitNameResult);
                UWBCMiniRangeResult = recordEless.elementTextTrim("UWBCMiniRange");
                bodyDataEntity.setUWBCMiniRange(UWBCMiniRangeResult);
                UWBCMaxRangeResult = recordEless.elementTextTrim("UWBCMaxRange");
                bodyDataEntity.setUWBCMaxRange(UWBCMaxRangeResult);
                BLDResult = recordEless.elementTextTrim("BLDResult");
                bodyDataEntity.setBLDResult(BLDResult);
                BLDUnitNameResult = recordEless.elementTextTrim("BLDUnitName");
                bodyDataEntity.setBLDUnitName(BLDUnitNameResult);
                BLDMiniRangeResult = recordEless.elementTextTrim("BLDMiniRange");
                bodyDataEntity.setBLDMiniRange(BLDMiniRangeResult);
                BLDMaxRangeResult = recordEless.elementTextTrim("BLDMaxRange");
                bodyDataEntity.setBLDMaxRange(BLDMaxRangeResult);
                ALTResult = recordEless.elementTextTrim("ALTResult");
                bodyDataEntity.setALTResult(ALTResult);
                ALTUnitNameResult = recordEless.elementTextTrim("ALTUnitName");
                bodyDataEntity.setALTUnitName(ALTUnitNameResult);
                ALTMiniRangeResult = recordEless.elementTextTrim("ALTMiniRange");
                bodyDataEntity.setALTMiniRange(ALTMiniRangeResult);
                ALTMaxRangeResult = recordEless.elementTextTrim("ALTMaxRange");
                bodyDataEntity.setALTMaxRange(ALTMaxRangeResult);
                CHESTCodeResult = recordEless.elementTextTrim("CHESTCode");
                bodyDataEntity.setCHESTCode(CHESTCodeResult);
                FVCResult = recordEless.elementTextTrim("FVCResult");
                bodyDataEntity.setFVCResult(FVCResult);
                FVCUnitNameResult = recordEless.elementTextTrim("FVCUnitName");
                bodyDataEntity.setFVCUnitName(FVCUnitNameResult);
                FVCMiniRangeResult = recordEless.elementTextTrim("FVCMiniRange");
                bodyDataEntity.setFVCMiniRange(FVCMiniRangeResult);
                FVCMaxRangeResult = recordEless.elementTextTrim("FVCMaxRange");
                bodyDataEntity.setFVCMaxRange(FVCMaxRangeResult);
                FEV1Result = recordEless.elementTextTrim("FEV1Result");
                bodyDataEntity.setFEV1Result(FEV1Result);
                FEV1UnitNameResult = recordEless.elementTextTrim("FEV1UnitName");
                bodyDataEntity.setFEV1UnitName(FEV1UnitNameResult);
                FEV1MiniRangeResult = recordEless.elementTextTrim("FEV1MiniRange");
                bodyDataEntity.setFEV1MiniRange(FEV1MiniRangeResult);
                FEV1MaxRangeResult = recordEless.elementTextTrim("FEV1MaxRange");
                bodyDataEntity.setFEV1MaxRange(FEV1MaxRangeResult);
                FEV1FVCResult = recordEless.elementTextTrim("FEV1FVCResult");
                bodyDataEntity.setFEV1FVCResult(FEV1FVCResult);
                FEV1FVCUnitNameResult = recordEless.elementTextTrim("FEV1FVCUnitName");
                bodyDataEntity.setFEV1FVCUnitName(FEV1FVCUnitNameResult);
                FEV1FVCMiniRangeResult = recordEless.elementTextTrim("FEV1FVCMiniRange");
                bodyDataEntity.setFEV1FVCMiniRange(FEV1FVCMiniRangeResult);
                FEV1FVCMaxRangeResult = recordEless.elementTextTrim("FEV1FVCMaxRange");
                bodyDataEntity.setFEV1FVCMaxRange(FEV1FVCMaxRangeResult);
                BLeadResult = recordEless.elementTextTrim("BLeadResult");
                bodyDataEntity.setBLeadResult(BLeadResult);
                BLeadUnitNameResult = recordEless.elementTextTrim("BLeadUnitName");
                bodyDataEntity.setBLeadUnitName(BLeadUnitNameResult);
                BLeadMiniRangeResult = recordEless.elementTextTrim("BLeadMiniRange");
                bodyDataEntity.setBLDMiniRange(BLeadMiniRangeResult);
                BLeadMaxRangeResult = recordEless.elementTextTrim("BLeadMaxRange");
                bodyDataEntity.setBLeadMaxRange(BLeadMaxRangeResult);
                ULeadResult = recordEless.elementTextTrim("ULeadResult");
                bodyDataEntity.setULeadResult(ULeadResult);
                ULeadUnitNameResult = recordEless.elementTextTrim("ULeadUnitName");
                bodyDataEntity.setULeadUnitName(ULeadUnitNameResult);
                ULeadMiniRangeResult = recordEless.elementTextTrim("ULeadMiniRange");
                bodyDataEntity.setULeadMiniRange(ULeadMiniRangeResult);
                ULeadMaxRangeResult = recordEless.elementTextTrim("ULeadMaxRange");
                bodyDataEntity.setULeadMaxRange(ULeadMaxRangeResult);
                ZPPResult = recordEless.elementTextTrim("ZPPResult");
                bodyDataEntity.setZPPResult(ZPPResult);
                ZPPUnitNameResult = recordEless.elementTextTrim("ZPPUnitName");
                bodyDataEntity.setZPPUnitName(ZPPUnitNameResult);
                ZPPMiniRangeResult = recordEless.elementTextTrim("ZPPMiniRange");
                bodyDataEntity.setZPPMiniRange(ZPPMiniRangeResult);
                ZPPMaxRangeResult = recordEless.elementTextTrim("ZPPMaxRange");
                bodyDataEntity.setZPPMaxRange(ZPPMaxRangeResult);
                NeutResult = recordEless.elementTextTrim("NeutResult");
                bodyDataEntity.setNeutResult(NeutResult);
                NeutUnitNameResult = recordEless.elementTextTrim("NeutUnitName");
                bodyDataEntity.setNeutUnitName(NeutUnitNameResult);
                NeutMiniRangeResult = recordEless.elementTextTrim("NeutMiniRange");
                bodyDataEntity.setNeutMiniRange(NeutMiniRangeResult);
                NeutMaxRangeResult = recordEless.elementTextTrim("NeutMaxRange");
                bodyDataEntity.setNeutMaxRange(NeutMaxRangeResult);
                hearingReuslt = recordEless.elementTextTrim("hearingReuslt");
                bodyDataEntity.setHearingReuslt(hearingReuslt);
                hearingUnitNameResult = recordEless.elementTextTrim("hearingUnitName");
                bodyDataEntity.setHearingUnitName(hearingUnitNameResult);
                hearingMiniRangeResult = recordEless.elementTextTrim("hearingMiniRange");
                bodyDataEntity.setHearingMiniRange(hearingMiniRangeResult);
                hearingMaxRangeResult = recordEless.elementTextTrim("hearingMaxRange");
                bodyDataEntity.setHearingMaxRange(hearingMaxRangeResult);
                RPBTCodeResult = recordEless.elementTextTrim("RPBTCode");
                bodyDataEntity.setRPBTCode(RPBTCodeResult);
                wrightCodeResult = recordEless.elementTextTrim("wrightCode");
                bodyDataEntity.setWrightCode(wrightCodeResult);
                orgCodeResult = recordEless.elementTextTrim("orgCode");
                bodyDataEntity.setOrgCode(orgCodeResult);
                employerNameResult = recordEless.elementTextTrim("employerName");
                bodyDataEntity.setEmployerName(employerNameResult);
                if(StringUtils.isEmpty(codeResult)){
                    returnCode.setText("105");
                    message.setText("个案卡请求参数code为空值!");
                    logger.info("个案卡请求参数code为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                if(StringUtils.isEmpty(hosIdResult)){
                    returnCode.setText("105");
                    message.setText("个案卡请求参数hosId为空值!");
                    logger.info("个案卡请求参数hosId为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                if(StringUtils.isEmpty(nameResult)){
                    returnCode.setText("105");
                    message.setText("个案卡请求参数name为空值!");
                    logger.info("个案卡请求参数name为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                if(StringUtils.isEmpty(idcardResult)){
                    returnCode.setText("105");
                    message.setText("个案卡请求参数idcard为空值!");
                    logger.info("个案卡请求参数idcard为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                if(StringUtils.isEmpty(bodyCheckTypeResult)){
                    returnCode.setText("105");
                    message.setText("个案卡请求参数bodyCheckType为空值!");
                    logger.info("个案卡请求参数bodyCheckType为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                if(StringUtils.isEmpty(bodyCheckTimeResult)){
                    returnCode.setText("105");
                    message.setText("个案卡请求参数bodyCheckTime为空值!");
                    logger.info("个案卡请求参数code为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                if(StringUtils.isEmpty(sexCodeResult)){
                    returnCode.setText("105");
                    message.setText("个案卡请求参数sexCode为空值!");
                    logger.info("个案卡请求参数code为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                if(StringUtils.isEmpty(birthdayResult)){
                    returnCode.setText("105");
                    message.setText("个案卡请求参数birthday为空值!");
                    logger.info("个案卡请求参数code为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                if(StringUtils.isEmpty(hazardCodeResult)){
                    returnCode.setText("105");
                    message.setText("个案卡请求参数hazardCode为空值!");
                    logger.info("个案卡请求参数code为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                if(StringUtils.isEmpty(hazardYearResult)){
                    returnCode.setText("105");
                    message.setText("个案卡请求参数hazardYear为空值!");
                    logger.info("个案卡请求参数hazardYear为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                if(StringUtils.isEmpty(hazardMonthResult)){
                    returnCode.setText("105");
                    message.setText("个案卡请求参数code为空值!!");
                    logger.info("个案卡请求参数code为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                if(StringUtils.isEmpty(sysPressResult)){
                    returnCode.setText("105");
                    message.setText("个案卡请求参数sysPressResult为空值!");
                    logger.info("个案卡请求参数sysPressResult为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                if(StringUtils.isEmpty(diasPressResult)){
                    returnCode.setText("105");
                    message.setText("个案卡请求参数diasPressResult为空值!!");
                    logger.info("个案卡请求参数diasPressResult为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                if(StringUtils.isEmpty(ECGCodeResult)){
                    returnCode.setText("105");
                    message.setText("个案卡请求参数ECGCode为空值!!");
                    logger.info("个案卡请求参数ECGCode为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                if(StringUtils.isEmpty(conclusionsCodeResult)){
                    returnCode.setText("105");
                    message.setText("个案卡请求参数conclusionsCode为空值!!");
                    logger.info("个案卡请求参数conclusionsCode为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                if(StringUtils.isEmpty(orgCodeResult)){
                    returnCode.setText("105");
                    message.setText("个案卡请求参数orgCode为空值!!");
                    logger.info("个案卡请求参数orgCode为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                if(StringUtils.isEmpty(employerNameResult)){
                    returnCode.setText("105");
                    message.setText("个案卡请求参数employerName为空值!!");
                    logger.info("个案卡请求参数employerName为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                ZybGak zybGakBykey = zybGakServiceImpl.selectByPrimaryKey(codeResult+hosIdResult);
                if(zybGakBykey!=null){
                    logger.info("更新个案卡数据");
                    dateStrUpdate.add(bodyDataEntity);
                    boolean dateStrUpdteFg= isDateFormate(dateStrUpdate,dataInfo);
                    dateStrFlag.add(dateStrUpdteFg);
                    if(dateStrUpdteFg){
                        ZybGak zybGak = new ZybGak();
                        //id 为唯一标识codeResult+hosIdResult
                        zybGak.setId(codeResult+hosIdResult);
                        zybGak.setCode(codeResult);
                        zybGak.setHosId(hosIdResult);
                        zybGak.setBirthday(sdf.parse(birthdayResult));
                        zybGak.setOrgCode(orgCodeResult);
                        zybGak.setEmployerName(employerNameResult);
                        zybGak.setName(nameResult);
                        zybGak.setIdcard(idcardResult);
                        zybGak.setBodyCheckType(Short.parseShort(bodyCheckTypeResult));
                        zybGak.setBodyCheckTime(sdf.parse(bodyCheckTimeResult));
                        zybGak.setSexCode(Short.parseShort(sexCodeResult));
                        zybGak.setHazardCode(hazardCodeResult);
                        zybGak.setHazardYear(Short.parseShort(hazardYearResult));
                        zybGak.setHazardMonth(Short.parseShort(hazardMonthResult));
                        zybGak.setSysPressResult(new BigDecimal(sysPressResult));
                        zybGak.setDiasPressResult(new BigDecimal(diasPressResult));
                        zybGak.setEcgCode(ECGCodeResult);
                        zybGak.setConclusionsCode(Short.parseShort(conclusionsCodeResult));
                        zybGak.setTelPhone(telPhoneResult);
                        if(!StringUtils.isEmpty(seniorityYearResult)){
                            zybGak.setSeniorityYear(Short.parseShort(seniorityYearResult));
                        }
                        if(!StringUtils.isEmpty(seniorityMonthResult)){
                            zybGak.setSeniorityMonth(Short.parseShort(seniorityMonthResult));
                        }
                        if(!StringUtils.isEmpty(exposureYearResult)){
                            zybGak.setSeniorityMonth(Short.parseShort(exposureYearResult));
                        }
                        if(!StringUtils.isEmpty(exposureMonthResult)){
                            zybGak.setExposureMonth(Short.parseShort(exposureMonthResult));
                        }
                        zybGak.setWorkShop(workShopResult);
                        zybGak.setJobCode(jobCodeResult);
                        zybGak.setSysPressUnitName(sysPressUnitNameResult);
                        zybGak.setDiasPressUnitName(diasPressUnitNameResult);
                        zybGak.setWbcResult(WBCResult);
                        zybGak.setWbcUnitName(WBCUnitNameResult);
                        zybGak.setWbcMiniRange(WBCMiniRangeResult);
                        zybGak.setWbcMaxRange(WBCMaxRangeResult);
                        zybGak.setRbcResult(RBCResult);
                        zybGak.setRbcMiniRange(RBCMiniRangeResult);
                        zybGak.setRbcMaxRange(RBCMaxRangeResult);
                        zybGak.setRbcUnitName(RBCUnitNameResult);
                        zybGak.setHbResult(HbResult);
                        zybGak.setHbUnitName(HbUnitNameResult);
                        zybGak.setHbMaxRange(HbMaxRangeResult);
                        zybGak.setHbMiniRange(HbMiniRangeResult);
                        zybGak.setPltResult(PLTResult);
                        zybGak.setPltUnitName(PLTUnitNameResult);
                        zybGak.setPltMaxRange(PLTMaxRangeResult);
                        zybGak.setPltMiniRange(PLTMiniRangeResult);
                        zybGak.setBgluResult(BGLUResult);
                        zybGak.setBgluUnitName(BGLUUnitName);
                        zybGak.setBgluMaxRange(BGLUMaxRange);
                        zybGak.setBgluMiniRange(BGLUMiniRange);
                        zybGak.setGluResult(GLUResult);
                        zybGak.setGluUnitName(GLUUnitNameResult);
                        zybGak.setGluMiniRange(GLUMiniRangeResult);
                        zybGak.setGluMaxRange(GLUMaxRangeResult);
                        zybGak.setProResult(PROResult);
                        zybGak.setProUnitName(PROUnitNameResult);
                        zybGak.setProMiniRange(PROMiniRangeResult);
                        zybGak.setProMaxRange(PROMaxRangeResult);
                        zybGak.setUwbcResult(UWBCResult);
                        zybGak.setUwbcUnitName(UWBCUnitNameResult);
                        zybGak.setUwbcMiniRange(UWBCMiniRangeResult);
                        zybGak.setUwbcMaxRange(UWBCMaxRangeResult);
                        zybGak.setBldResult(BLDResult);
                        zybGak.setBldUnitName(BLDUnitNameResult);
                        zybGak.setBldMiniRange(BLDMiniRangeResult);
                        zybGak.setBldMaxRange(BLDMaxRangeResult);
                        zybGak.setAltResult(ALTResult);
                        zybGak.setAltUnitName(ALTUnitNameResult);
                        zybGak.setAltMiniRange(ALTMiniRangeResult);
                        zybGak.setAltMaxRange(ALTMaxRangeResult);
                        zybGak.setChestCode(CHESTCodeResult);
                        zybGak.setFvcResult(FVCResult);
                        zybGak.setFvcUnitName(FVCUnitNameResult);
                        zybGak.setFvcMiniRange(FVCMiniRangeResult);
                        zybGak.setFvcMaxRange(FVCMaxRangeResult);
                        zybGak.setFev1Result(FEV1Result);
                        zybGak.setFev1UnitName(FEV1UnitNameResult);
                        zybGak.setFev1MiniRange(FEV1MiniRangeResult);
                        zybGak.setFev1MaxRange(FEV1MaxRangeResult);
                        zybGak.setFev1fvcResult(FEV1FVCResult);
                        zybGak.setFev1fvcUnitName(FEV1FVCUnitNameResult);
                        zybGak.setFev1fvcMiniRange(FEV1FVCMiniRangeResult);
                        zybGak.setFev1fvcMaxRange(FEV1FVCMaxRangeResult);
                        zybGak.setbLeadResult(BLeadResult);
                        zybGak.setbLeadUnitName(BLeadUnitNameResult);
                        zybGak.setbLeadMiniRange(BLeadMiniRangeResult);
                        zybGak.setbLeadMaxRange(BLeadMaxRangeResult);
                        zybGak.setuLeadResult(ULeadResult);
                        zybGak.setuLeadUnitName(ULeadUnitNameResult);
                        zybGak.setuLeadMiniRange(ULeadMiniRangeResult);
                        zybGak.setuLeadMaxRange(ULeadMaxRangeResult);
                        zybGak.setZppResult(ZPPResult);
                        zybGak.setZppUnitName(ZPPUnitNameResult);
                        zybGak.setZppMiniRange(ZPPMiniRangeResult);
                        zybGak.setZppMaxRange(ZPPMaxRangeResult);
                        zybGak.setNeutResult(NeutResult);
                        zybGak.setNeutUnitName(NeutUnitNameResult);
                        zybGak.setNeutMiniRange(NeutMiniRangeResult);
                        zybGak.setNeutUnitName(NeutMaxRangeResult);
                        zybGak.setHearingReuslt(hearingReuslt);
                        zybGak.setHearingUnitName(hearingUnitNameResult);
                        zybGak.setHearingMiniRange(hearingMiniRangeResult);
                        zybGak.setHearingMaxRange(hearingMaxRangeResult);
                        if(!StringUtils.isEmpty(RPBTCodeResult)){
                            zybGak.setRpbtCode(Short.parseShort(RPBTCodeResult));
                        }
                        //1	已审核2	未审核3	全部m4	已删除
                        zybGak.setShbz("2");
                        zybGak.setGluResult(wrightCodeResult);
                        zybGak.setGluResult(conclusionsCodeResult);
                        //获取当前系统时间
                        Date currentTime = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String dateString = formatter.format(currentTime);
                        Date currentDate = formatter.parse(dateString);
                        zybGak.setLogsj(currentDate);
                        zybGakServiceImpl.updateByPrimaryKey(zybGak);
                    }
                    dateStrUpdate.clear();
                }else{
                    dateStr.add(bodyDataEntity);
                    boolean dateStrFg = isDateFormate(dateStr,dataInfo);
                    dateStrFlag.add(dateStrFg);
                    if(dateStrFg){
                        ZybGak zybGak = new ZybGak();
                        //id 为唯一标识codeResult+hosIdResult
                        zybGak.setId(codeResult+hosIdResult);
                        zybGak.setCode(codeResult);
                        zybGak.setHosId(hosIdResult);
                        zybGak.setBirthday(sdf.parse(birthdayResult));
                        zybGak.setOrgCode(orgCodeResult);
                        zybGak.setEmployerName(employerNameResult);
                        zybGak.setName(nameResult);
                        zybGak.setIdcard(idcardResult);
                        zybGak.setBodyCheckType(Short.parseShort(bodyCheckTypeResult));
                        zybGak.setBodyCheckTime(sdf.parse(bodyCheckTimeResult));
                        zybGak.setSexCode(Short.parseShort(sexCodeResult));
                        zybGak.setHazardCode(hazardCodeResult);
                        zybGak.setHazardYear(Short.parseShort(hazardYearResult));
                        zybGak.setHazardMonth(Short.parseShort(hazardMonthResult));
                        zybGak.setSysPressResult(new BigDecimal(sysPressResult));
                        zybGak.setDiasPressResult(new BigDecimal(diasPressResult));
                        zybGak.setEcgCode(ECGCodeResult);
                        zybGak.setConclusionsCode(Short.parseShort(conclusionsCodeResult));
                        zybGak.setTelPhone(telPhoneResult);
                        if(!StringUtils.isEmpty(seniorityYearResult)){
                            zybGak.setSeniorityYear(Short.parseShort(seniorityYearResult));
                        }
                        if(!StringUtils.isEmpty(seniorityMonthResult)){
                            zybGak.setSeniorityMonth(Short.parseShort(seniorityMonthResult));
                        }
                        if(!StringUtils.isEmpty(exposureYearResult)){
                            zybGak.setSeniorityMonth(Short.parseShort(exposureYearResult));
                        }
                        if(!StringUtils.isEmpty(exposureMonthResult)){
                            zybGak.setExposureMonth(Short.parseShort(exposureMonthResult));
                        }
                        zybGak.setWorkShop(workShopResult);
                        zybGak.setJobCode(jobCodeResult);
                        zybGak.setSysPressUnitName(sysPressUnitNameResult);
                        zybGak.setDiasPressUnitName(diasPressUnitNameResult);
                        zybGak.setWbcResult(WBCResult);
                        zybGak.setWbcUnitName(WBCUnitNameResult);
                        zybGak.setWbcMiniRange(WBCMiniRangeResult);
                        zybGak.setWbcMaxRange(WBCMaxRangeResult);
                        zybGak.setRbcResult(RBCResult);
                        zybGak.setRbcMiniRange(RBCMiniRangeResult);
                        zybGak.setRbcMaxRange(RBCMaxRangeResult);
                        zybGak.setRbcUnitName(RBCUnitNameResult);
                        zybGak.setHbResult(HbResult);
                        zybGak.setHbUnitName(HbUnitNameResult);
                        zybGak.setHbMaxRange(HbMaxRangeResult);
                        zybGak.setHbMiniRange(HbMiniRangeResult);
                        zybGak.setPltResult(PLTResult);
                        zybGak.setPltUnitName(PLTUnitNameResult);
                        zybGak.setPltMaxRange(PLTMaxRangeResult);
                        zybGak.setPltMiniRange(PLTMiniRangeResult);
                        zybGak.setBgluResult(BGLUResult);
                        zybGak.setBgluUnitName(BGLUUnitName);
                        zybGak.setBgluMaxRange(BGLUMaxRange);
                        zybGak.setBgluMiniRange(BGLUMiniRange);
                        zybGak.setGluResult(GLUResult);
                        zybGak.setGluUnitName(GLUUnitNameResult);
                        zybGak.setGluMiniRange(GLUMiniRangeResult);
                        zybGak.setGluMaxRange(GLUMaxRangeResult);
                        zybGak.setProResult(PROResult);
                        zybGak.setProUnitName(PROUnitNameResult);
                        zybGak.setProMiniRange(PROMiniRangeResult);
                        zybGak.setProMaxRange(PROMaxRangeResult);
                        zybGak.setUwbcResult(UWBCResult);
                        zybGak.setUwbcUnitName(UWBCUnitNameResult);
                        zybGak.setUwbcMiniRange(UWBCMiniRangeResult);
                        zybGak.setUwbcMaxRange(UWBCMaxRangeResult);
                        zybGak.setBldResult(BLDResult);
                        zybGak.setBldUnitName(BLDUnitNameResult);
                        zybGak.setBldMiniRange(BLDMiniRangeResult);
                        zybGak.setBldMaxRange(BLDMaxRangeResult);
                        zybGak.setAltResult(ALTResult);
                        zybGak.setAltUnitName(ALTUnitNameResult);
                        zybGak.setAltMiniRange(ALTMiniRangeResult);
                        zybGak.setAltMaxRange(ALTMaxRangeResult);
                        zybGak.setChestCode(CHESTCodeResult);
                        zybGak.setFvcResult(FVCResult);
                        zybGak.setFvcUnitName(FVCUnitNameResult);
                        zybGak.setFvcMiniRange(FVCMiniRangeResult);
                        zybGak.setFvcMaxRange(FVCMaxRangeResult);
                        zybGak.setFev1Result(FEV1Result);
                        zybGak.setFev1UnitName(FEV1UnitNameResult);
                        zybGak.setFev1MiniRange(FEV1MiniRangeResult);
                        zybGak.setFev1MaxRange(FEV1MaxRangeResult);
                        zybGak.setFev1fvcResult(FEV1FVCResult);
                        zybGak.setFev1fvcUnitName(FEV1FVCUnitNameResult);
                        zybGak.setFev1fvcMiniRange(FEV1FVCMiniRangeResult);
                        zybGak.setFev1fvcMaxRange(FEV1FVCMaxRangeResult);
                        zybGak.setbLeadResult(BLeadResult);
                        zybGak.setbLeadUnitName(BLeadUnitNameResult);
                        zybGak.setbLeadMiniRange(BLeadMiniRangeResult);
                        zybGak.setbLeadMaxRange(BLeadMaxRangeResult);
                        zybGak.setuLeadResult(ULeadResult);
                        zybGak.setuLeadUnitName(ULeadUnitNameResult);
                        zybGak.setuLeadMiniRange(ULeadMiniRangeResult);
                        zybGak.setuLeadMaxRange(ULeadMaxRangeResult);
                        zybGak.setZppResult(ZPPResult);
                        zybGak.setZppUnitName(ZPPUnitNameResult);
                        zybGak.setZppMiniRange(ZPPMiniRangeResult);
                        zybGak.setZppMaxRange(ZPPMaxRangeResult);
                        zybGak.setNeutResult(NeutResult);
                        zybGak.setNeutUnitName(NeutUnitNameResult);
                        zybGak.setNeutMiniRange(NeutMiniRangeResult);
                        zybGak.setNeutUnitName(NeutMaxRangeResult);
                        zybGak.setHearingReuslt(hearingReuslt);
                        zybGak.setHearingUnitName(hearingUnitNameResult);
                        zybGak.setHearingMiniRange(hearingMiniRangeResult);
                        zybGak.setHearingMaxRange(hearingMaxRangeResult);
                        if(!StringUtils.isEmpty(RPBTCodeResult)){
                            zybGak.setRpbtCode(Short.parseShort(RPBTCodeResult));
                        }
                        //1	已审核2	未审核3	全部m4	已删除
                        zybGak.setShbz("2");
                        zybGak.setGluResult(wrightCodeResult);
                        zybGak.setGluResult(conclusionsCodeResult);
                        //获取当前系统时间
                        Date currentTime = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String dateString = formatter.format(currentTime);
                        Date currentDate = formatter.parse(dateString);
                        zybGak.setLogsj(currentDate);
                        zybGakList.add(zybGak);
                    }
                    dateStr.clear();
                }

            }
           while(employingUnitIte.hasNext()){
                ZybYrdw zybYrdw = new ZybYrdw();
                //数据校验实体
                BodyDataEntity zybYrdwDataEntity = new BodyDataEntity();
                Element employingUnit = (Element) employingUnitIte.next();
                creditCode = employingUnit.elementTextTrim("creditCode");
               zybYrdwDataEntity.setCreditCode(creditCode);
                employerCode = employingUnit.elementTextTrim("employerCode");
               zybYrdwDataEntity.setEmployerCode(employerCode);
                employerNameEN = employingUnit.elementTextTrim("employerName");
               zybYrdwDataEntity.setEmployerNameEN(employerNameEN);
                employerDesc= employingUnit.elementTextTrim("employerDesc");
               zybYrdwDataEntity.setEmployerDesc(employerDesc);
                areaStandard = employingUnit.elementTextTrim("areaStandard");
               zybYrdwDataEntity.setAreaStandard(areaStandard);
                areaAddress = employingUnit.elementTextTrim("areaAddress");
               zybYrdwDataEntity.setAreaAddress(areaAddress);
                economicCode = employingUnit.elementTextTrim("economicCode");
               zybYrdwDataEntity.setEconomicCode(economicCode);
                industryCateCode = employingUnit.elementTextTrim("industryCateCode");
               zybYrdwDataEntity.setIndustryCateCode(industryCateCode);
                enterpriseCode = employingUnit.elementTextTrim("enterpriseCode");
               zybYrdwDataEntity.setEnterpriseCode(enterpriseCode);
                secondEmployerCode = employingUnit.elementTextTrim("secondEmployerCode");
               zybYrdwDataEntity.setSecondEmployerCode(secondEmployerCode);
                secondEmployerName = employingUnit.elementTextTrim("secondEmployerName");
               zybYrdwDataEntity.setSecondEmployerName(secondEmployerName);
                postAddress = employingUnit.elementTextTrim("postAddress");
               zybYrdwDataEntity.setPostAddress(postAddress);
                zipCode = employingUnit.elementTextTrim("zipCode");
               zybYrdwDataEntity.setZipCode(zipCode);
                contactPerson = employingUnit.elementTextTrim("contactPerson");
               zybYrdwDataEntity.setContactPerson(contactPerson);
                contactPhone = employingUnit.elementTextTrim("contactPhone");
               zybYrdwDataEntity.setContactPhone(contactPhone);
                monitorOrgCode = employingUnit.elementTextTrim("monitorOrgCode");
               zybYrdwDataEntity.setMonitorOrgCode(monitorOrgCode);
                monitorOrgName = employingUnit.elementTextTrim("monitorOrgName");
               zybYrdwDataEntity.setMonitorOrgName(monitorOrgName);
                remarks = employingUnit.elementTextTrim("remarks");
               zybYrdwDataEntity.setRemarks(remarks);
                if(StringUtils.isEmpty(creditCode)){
                    returnCode.setText("105");
                    message.setText("用人单位请求参数ecreditCode为空值!");
                    logger.info("用人单位请求参数ecreditCode为空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
               if(StringUtils.isEmpty(employerCode)){
                   returnCode.setText("105");
                   message.setText("用人单位请求参数employerCode为空值!");
                   logger.info("用人单位请求参数employerCode为空值:" + retDoc.asXML());
                   return retDoc.asXML();
               }
               if(StringUtils.isEmpty(employerNameEN)){
                   returnCode.setText("105");
                   message.setText("用人单位请求参数employerName为空值!");
                   logger.info("请求参数employerName为空值:" + retDoc.asXML());
                   return retDoc.asXML();
               }
               if(StringUtils.isEmpty(areaStandard)){
                   returnCode.setText("105");
                   message.setText("用人单位请求参数areaStandard为空值!");
                   logger.info("用人单位请求参数areaStandard为空值:" + retDoc.asXML());
                   return retDoc.asXML();
               }
               if(StringUtils.isEmpty(areaAddress)){
                   returnCode.setText("105");
                   message.setText("用人单位请求参数areaAddress为空值!");
                   logger.info("用人单位请求参数areaAddress为空值:" + retDoc.asXML());
                   return retDoc.asXML();
               }
               if(StringUtils.isEmpty(economicCode)){
                   returnCode.setText("105");
                   message.setText("用人单位请求参数economicCode为空值!");
                   logger.info("用人单位请求参数economicCode为空值:" + retDoc.asXML());
                   return retDoc.asXML();
               }
               if(StringUtils.isEmpty(industryCateCode)){
                   returnCode.setText("105");
                   message.setText("用人单位请求参数industryCateCode为空值!");
                   logger.info("用人单位请求参数industryCateCode为空值:" + retDoc.asXML());
                   return retDoc.asXML();
               }
               if(StringUtils.isEmpty(enterpriseCode)){
                   returnCode.setText("105");
                   message.setText("用人单位请求参数enterpriseCode为空值!");
                   logger.info("用人单位请求参数enterpriseCode为空值:" + retDoc.asXML());
                   return retDoc.asXML();
               }
               if(StringUtils.isEmpty(postAddress)){
                   returnCode.setText("105");
                   message.setText("用人单位请求参数postAddress为空值!");
                   logger.info("用人单位请求参数ecreditCode为空值:" + retDoc.asXML());
                   return retDoc.asXML();
               }
               if(StringUtils.isEmpty(zipCode)){
                   returnCode.setText("105");
                   message.setText("用人单位请求参数zipCode为空值!");
                   logger.info("用人单位请求参数zipCode为空值:" + retDoc.asXML());
                   return retDoc.asXML();
               }
               if(StringUtils.isEmpty(contactPerson)){
                   returnCode.setText("105");
                   message.setText("用人单位请求参数contactPerson为空值!");
                   logger.info("用人单位请求参数contactPerson为空值:" + retDoc.asXML());
                   return retDoc.asXML();
               }
               if(StringUtils.isEmpty(contactPhone)){
                   returnCode.setText("105");
                   message.setText("用人单位请求参数contactPhone为空值!");
                   logger.info("用人单位请求参数contactPhone为空值:" + retDoc.asXML());
                   return retDoc.asXML();
               }
               //economicCode经济类型编码
               List<CodeInfo>  economicCodeList = codeInfoServiceImpl.selectByCodeInfoId(new BigDecimal(906));
               boolean economicCodeflag=false;
               for(CodeInfo codeInfo : economicCodeList){
                   if(economicCode.equals(codeInfo.getCode())){
                       economicCodeflag=true;
                       break;
                   }
               }
               if(!economicCodeflag){
                   returnCode.setText("102");
                   message.setText("无法找到经济类型编码!");
                   logger.info("无法找到经济类型编码!");
                   logger.info("无法找到经济类型编码:" + retDoc.asXML());
                   return retDoc.asXML();
               }
               //enterpriseCode企业规模编码
               List<CodeInfo>  enterpriseCodeList = codeInfoServiceImpl.selectByCodeInfoId(new BigDecimal(905));
               boolean enterpriseCodeflag=false;
               for(CodeInfo codeInfo : enterpriseCodeList){
                   if(enterpriseCode.equals(codeInfo.getCode())){
                       enterpriseCodeflag=true;
                       break;
                   }
               }
               if(!enterpriseCodeflag){
                   returnCode.setText("102");
                   message.setText("无法找到企业规模编码!");
                   logger.info("无法找到企业规模编码!");
                   logger.info("无法找到企业规模编码:" + retDoc.asXML());
                   return retDoc.asXML();
               }
                zybYrdw.setCreditCode(creditCode);
                zybYrdw.setEmployerCode(employerCode);
                zybYrdw.setEmployerName(employerNameEN);
                zybYrdw.setEmployerDesc(employerDesc);
                zybYrdw.setAreaStandard(areaStandard);
                zybYrdw.setAreaAddress(areaAddress);
                zybYrdw.setEconomicCode(economicCode);
                zybYrdw.setIndustryCateCode(industryCateCode);
                zybYrdw.setEnterpriseCode(enterpriseCode);
                if(!StringUtils.isEmpty(secondEmployerCode)){
                    zybYrdw.setSecondEmployerCode(secondEmployerCode);
                }
                zybYrdw.setSecondEmployerName(secondEmployerName);
                zybYrdw.setPostAddress(postAddress);
                zybYrdw.setZipCode(zipCode);
                zybYrdw.setContactPerson(contactPerson);
                zybYrdw.setContactPhone(contactPhone);
                zybYrdw.setMonitorOrgCode(monitorOrgCode);
                zybYrdw.setMonitorOrgName(monitorOrgName);
                zybYrdw.setRemarks(remarks);
                //获取当前系统时间
                Date currentTime = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateString = formatter.format(currentTime);
                Date currentDate = formatter.parse(dateString);
                zybYrdw.setLogsj(currentDate);
                ZybYrdw zybYrdwBykey = zybYrdwServiceImpl.selectByPrimaryKey(employerCode);
                if(zybYrdwBykey!=null){
                    logger.info("更新用人单位数据");
                    empDateStrUpdate.add(zybYrdwDataEntity);
                    boolean empDateStrUpdteFg= isDateFormatEmployer(empDateStrUpdate,dataInfo);
                    empDataStrFlag.add(empDateStrUpdteFg);
                    if(empDateStrUpdteFg){
                        zybYrdwServiceImpl.updateByPrimaryKey(zybYrdw);
                    }
                    empDateStrUpdate.clear();
                }else{
                    empDateStr.add(zybYrdwDataEntity);
                    boolean empDataStrFg = isDateFormatEmployer(empDateStr,dataInfo);
                    empDataStrFlag.add(empDataStrFg);
                    if(empDataStrFg){
                        zybYrdwList.add(zybYrdw);
                    }
                    empDateStr.clear();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            returnCode.setText("103");
            message.setText("xml数据解析失败!");
            logger.info("xml数据解析失败:" + retDoc.asXML());
            return retDoc.asXML();
        }
        if(zybYrdwList.size()>0 && zybYrdwList!=null){
            zybYrdwServiceImpl.insertBatch(zybYrdwList);
        }
        if(zybGakList.size()>0 && zybGakList!=null){
            zybGakServiceImpl.insertBatch(zybGakList);
        }
       if(!(empDataStrFlag.contains(false)||dateStrFlag.contains(false))){
            returnCode.setText("0");
            message.setText("成功!");
       }
        return retDoc.asXML();
    }

    //参数判空
    public boolean checkStringIsNull(String... value) {
        int count = 0;
        for (int i = 0; i <  value.length; i++) {
            //遍历字符数组所有的参数，发现某个为 null 或者 "" ,则跳出
            if (StringUtils.isEmpty(value[i])) {
                return true;
            }
            count++;
        }
        if (count == value.length) {
            return false;
        }
        return true;
    }

    //参数格式判断 暂时先不用 字段还没写完
    private Boolean isDateFormate(List<BodyDataEntity> dateStr, Element dataInfo) {
        boolean flag = true;
        dataInfo.element("returnCode").setText("-1");
        dataInfo.element("message").setText("部分数据格式有误!");
        Element errorDatas = dataInfo.addElement("errorDatas");
        Element errorReportCards = errorDatas.addElement("errorReportCards");
        //参数日期格式判断
        SimpleDateFormat format = new SimpleDateFormat("YYYYMMdd");
        for (BodyDataEntity reportCard : dateStr) {
            //判断code格式
                if(!StringUtils.isEmpty(reportCard.getCode())) {
                    boolean formatCodeFlag = CommonUtils.isLetterDigit(reportCard.getCode());
                    boolean formatCodeLenFlag = CommonUtils.isMaxLength(reportCard.getCode(), 16);
                    if (!(formatCodeFlag && formatCodeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("报告编号格式不正确!");
                        flag = false;

                    }
                }

                //判断医院hosId格式
                if (!StringUtils.isEmpty(reportCard.getHosId())) {
                    Boolean digitLenFlag = CommonUtils.isDigist(reportCard.getHosId());
                    Boolean isLength = CommonUtils.isLength(reportCard.getHosId(), 9);
                    if (!(digitLenFlag && isLength)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("医院编号格式不正确!");
                        flag = false;
                    }
                }

                //判断orgCode格式
                if (!StringUtils.isEmpty(reportCard.getOrgCode())) {
                    boolean formatOrgCodeFlag = CommonUtils.isOrgCode(reportCard.getOrgCode());
                    //第九位字符为"-"
                    if (!formatOrgCodeFlag ) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("用人单位编码格式不正确!");
                        flag = false;
                    }
                }
                //判断employerName格式
                if (!StringUtils.isEmpty(reportCard.getEmployerName())) {
                    boolean formatEmployerNameFlag = CommonUtils.isCNChar(reportCard.getEmployerName());
                    logger.info("---reportCard.getEmployerName()---"+reportCard.getEmployerName());
                    logger.info("---formatEmployerNameFlag---"+String.valueOf(formatEmployerNameFlag));
                    boolean formatEmployerNameLenFlag = CommonUtils.isMaxLength(reportCard.getEmployerName(), 64);
                    if (!(formatEmployerNameFlag && formatEmployerNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("用人单位名称格式不正确!");
                        flag = false;

                    }
                }
                //判断name格式
                if (!StringUtils.isEmpty(reportCard.getName())) {
                    boolean formatNameFlag = CommonUtils.isCNChar(reportCard.getName());
                    boolean formatNameLenFlag = CommonUtils.isMaxLength(reportCard.getEmployerName(), 16);
                    if (!(formatNameFlag && formatNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("体检人员姓名格式不正确!");
                        flag = false;
                    }
                }
                if (!StringUtils.isEmpty(reportCard.getIdcard())) {
                    //判断idcard格式
                    boolean formatIdcardFlag = CommonUtils.isLetterDigit(reportCard.getIdcard());
                    boolean formatIdcardLenFlag = CommonUtils.isLength(reportCard.getIdcard(), 18);
                    if (!(formatIdcardFlag && formatIdcardLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("身份证号码格式不正确!");
                        flag = false;
                    }
                }
                //判断telPhone格式
                if (!StringUtils.isEmpty(reportCard.getTelPhone())) {
                    boolean formatTelPhoneFlag = CommonUtils.isDigist(reportCard.getTelPhone());
                    boolean formatTelPhoneLenFlag = CommonUtils.isMaxLength(reportCard.getTelPhone(), 16);
                    if (!(formatTelPhoneFlag && formatTelPhoneLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("个人联系电话格式不正确!");
                        flag = false;
                    }
                }
                //判断bodyCheckTime格式
                if (!StringUtils.isEmpty(reportCard.getBodyCheckTime())) {
                    boolean bodyCheckTimeFlag = CommonUtils.isDateStr(reportCard.getBodyCheckTime(), "YYYYMMdd");
                    if (!bodyCheckTimeFlag) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("体检时间格式不正确!");
                        flag = false;
                    }
                }
                //判断bodyCheckType格式
                if (!StringUtils.isEmpty(reportCard.getBodyCheckType())) {
                    boolean formatBodyCheckTypeFlag = CommonUtils.isDigist(reportCard.getBodyCheckType());
                    boolean formatBodyCheckTypeLenFlag = CommonUtils.isLength(reportCard.getBodyCheckType(), 1);
                    List<CodeInfo> CodeInfoList = codeInfoServiceImpl.selectByCodeInfoId(new BigDecimal(901));
                    boolean bodyCheckInflag = false;
                    if (!(formatBodyCheckTypeFlag && formatBodyCheckTypeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("体检类型编码格式不正确!");
                        flag = false;
                    }
                    //判断bodyCheckType是否在字典值域内
                    for (CodeInfo codeInfo : CodeInfoList) {
                        if (reportCard.getBodyCheckType().equals(codeInfo.getCode())) {
                            bodyCheckInflag = true;
                            break;
                        }
                    }
                    if (!bodyCheckInflag) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("体检类型编码值不正确!");
                        flag = false;
                    }
                }
                //性别编码sexCode格式判断
                if(!StringUtils.isEmpty(reportCard.getSexCode())){
                    boolean formatSexCodeFlag = CommonUtils.isDigist(reportCard.getSexCode());
                    boolean formatSexCodeLenFlag = CommonUtils.isLength(reportCard.getSexCode(), 1);
                    List<CodeInfo> sexCodeList = codeInfoServiceImpl.selectByCodeInfoId(new BigDecimal(124));
                    boolean sexCodeflag = false;
                    if (!(formatSexCodeFlag && formatSexCodeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("性别编码格式不正确!");
                        flag = false;
                    }
                    for (CodeInfo codeInfo : sexCodeList) {
                        if (reportCard.getSexCode().equals(codeInfo.getCode())) {
                            sexCodeflag = true;
                            break;
                        }
                    }
                    if (!sexCodeflag) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("性别编码值不正确!");
                        flag = false;
                    }
                }
                //校验出生日期格式
                if(!StringUtils.isEmpty(reportCard.getBirthday())){
                    boolean birthdayFlag = CommonUtils.isDateStr(reportCard.getBirthday(), "YYYYMMdd");
                    if (!birthdayFlag) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("出生日期格式有误,格式应为YYYYMMDD.");
                        flag = false;
                    }
                }
                //判断总工龄年seniorityYear格式
                if (!StringUtils.isEmpty(reportCard.getSeniorityYear())) {
                    boolean formatSeniorityYearFlag = CommonUtils.isDigist(reportCard.getSeniorityYear());
                    boolean formatSeniorityYearLenFlag = CommonUtils.isMaxLength(reportCard.getSeniorityYear(), 2);
                    if (!(formatSeniorityYearFlag && formatSeniorityYearLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("总工龄年格式不正确!");
                        flag = false;
                    }
                }
                //判断总工龄月seniorityMonth格式
                if (!StringUtils.isEmpty(reportCard.getSeniorityMonth())) {
                    boolean formatSeniorityMonthFlag = CommonUtils.isDigist(reportCard.getSeniorityMonth());
                    boolean formatSeniorityMonthLenFlag = CommonUtils.isMaxLength(reportCard.getSeniorityMonth(), 2);
                    if (!(formatSeniorityMonthFlag && formatSeniorityMonthLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("总工龄月格式不正确!");
                        flag = false;
                    }
                }
                //判断接触监测的主要职业病危害因素编码hazardCode格式
                String hazardCode = reportCard.getHazardCode();
                //保存危害因素后面校验用
                List<String> hazardList = new ArrayList<String>();
                //判断hazardCodeList是否在字典值域内
                List<CodeInfo> hazardCodeList = codeInfoServiceImpl.selectByCodeInfoId(new BigDecimal(900));
                boolean hazardCodeInflag = false;
                if (!StringUtils.isEmpty(hazardCode)) {
                        if (hazardCode.indexOf(",") == -1) {
                            hazardList.add(hazardCode);
                            boolean formatHazardCodeFlag = CommonUtils.isLetterDigit(hazardCode);
                            boolean formatHazardCodeLenFlag = CommonUtils.isMaxLength(hazardCode, 32);
                            if (!(formatHazardCodeFlag && formatHazardCodeLenFlag)) {
                                Element errorData = errorReportCards.addElement("errorData");
                                Element errorMessage = errorData.addElement("errorMessage");
                                Element reportCardId = errorData.addElement("reportCard");
                                reportCardId.setText(reportCard.getCode());
                                errorMessage.setText("接触监测的主要职业病危害因素编码格式不正确!");
                                flag = false;
                            }
                            for (CodeInfo codeInfo : hazardCodeList) {
                                if (reportCard.getHazardCode().equals(codeInfo.getCode())) {
                                    hazardCodeInflag = true;
                                    break;
                                }
                            }

                        } else {
                                logger.info("===hazardCodSize==="+hazardCode);
                                String[] hazardCodSize = hazardCode.split(",");
                                boolean formatHazardCodeLenFlag = CommonUtils.isMaxLength(hazardCode, 32);
                                for (int i = 0; i < hazardCodSize.length; i++) {
                                    hazardList.add(hazardCodSize[i]);
                                    boolean formatHazardCodeFlag = CommonUtils.isLetterDigit(hazardCodSize[i]);
                                    if (!(formatHazardCodeFlag && formatHazardCodeLenFlag)) {
                                        Element errorData = errorReportCards.addElement("errorData");
                                        Element errorMessage = errorData.addElement("errorMessage");
                                        Element reportCardId = errorData.addElement("reportCard");
                                        reportCardId.setText(reportCard.getCode());
                                        errorMessage.setText("接触监测的主要职业病危害因素编码格式不正确!");
                                        flag = false;
                                        break;
                                    }
                                    for (CodeInfo codeInfo : hazardCodeList) {
                                        if (hazardCodSize[i].equals(codeInfo.getCode())) {
                                            hazardCodeInflag = true;
                                            break;
                                        }
                                    }
                                }

                        }
                    if (!hazardCodeInflag) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("接触监测的主要职业病危害因素编码值不正确!");
                        flag = false;
                    }


                }
                //判断职业危害接触工龄年exposureYear格式
                if (!StringUtils.isEmpty(reportCard.getExposureYear())) {
                    boolean formatExposureYearFlag = CommonUtils.isDigist(reportCard.getExposureYear());
                    boolean formatExposureYearLenFlag = CommonUtils.isMaxLength(reportCard.getExposureYear(), 2);
                    if (!(formatExposureYearFlag && formatExposureYearLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("职业危害接触工龄年格式不正确!");
                        flag = false;
                    }
                }
                //判断职业危害接触工龄月exposureMonth格式
                if (!StringUtils.isEmpty(reportCard.getExposureMonth())) {
                    boolean formatExposureMonthFlag = CommonUtils.isDigist(reportCard.getExposureMonth());
                    boolean formatExposureMonthLenFlag = CommonUtils.isMaxLength(reportCard.getExposureMonth(), 2);
                    if (!(formatExposureMonthFlag && formatExposureMonthLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("职业危害接触工龄月格式不正确!");
                        flag = false;
                    }
                }
                //判断接触所监测危害因素工龄年hazardYear格式
                if(!StringUtils.isEmpty(reportCard.getHazardYear())){
                    boolean formatHazardYearFlag = CommonUtils.isDigist(reportCard.getHazardYear());
                    boolean formatHazardYearLenFlag = CommonUtils.isMaxLength(reportCard.getHazardYear(), 2);
                    if (!(formatHazardYearFlag && formatHazardYearLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("接触所监测危害因素工龄年格式不正确!");
                        flag = false;
                    }
                }
                //判断hazardMonth接触所监测危害因素工龄月格式
                if(!StringUtils.isEmpty(reportCard.getHazardMonth())){
                    boolean formatHazardMonthFlag = CommonUtils.isDigist(reportCard.getHazardMonth());
                    boolean formatHazardMonthLenFlag = CommonUtils.isMaxLength(reportCard.getHazardMonth(), 2);
                    if (!(formatHazardMonthFlag && formatHazardMonthLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("接触所监测危害因素工龄月格式不正确!");
                        flag = false;
                    }
                }
                //判断workShop工作车间名称格式
                if (!StringUtils.isEmpty(reportCard.getWorkShop())) {
                    boolean formatWorkShopFlag = CommonUtils.isCNChar(reportCard.getWorkShop());
                    boolean formatWorkShopLenFlag = CommonUtils.isMaxLength(reportCard.getWorkShop(), 10);
                    if (!(formatWorkShopFlag && formatWorkShopLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("工作车间名称格式不正确!");
                        flag = false;
                    }
                }
                //判断jobCode工种编码格式
                if (!StringUtils.isEmpty(reportCard.getJobCode())) {
                    boolean formatJobCodeFlag = CommonUtils.isLetterDigit(reportCard.getJobCode());
                    boolean formatJobCodeLenFlag = CommonUtils.isLength(reportCard.getJobCode(), 6);
                    if (!(formatJobCodeFlag && formatJobCodeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("工种编码格式不正确!");
                        flag = false;
                    }
                }
                //判断sysPressResult血压收缩压结果格式
                if(!StringUtils.isEmpty(reportCard.getSysPressResult())){
                    boolean formatSysPressResultFlag = CommonUtils.isDouNot(reportCard.getSysPressResult());
                    boolean formatSysPressResultLenFlag = CommonUtils.isMaxLength(reportCard.getSysPressResult(), 6);
                    logger.info("------formatSysPressResultFlag------"+String.valueOf(formatSysPressResultFlag)+"-----formatSysPressResultLenFlag-------"+formatSysPressResultLenFlag);
                    if (!(formatSysPressResultFlag && formatSysPressResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血压收缩压结果格式不正确!");
                        flag = false;
                    }
                }
                //判断sysPressUnitName血压收缩压剂量单位名称
                if (!StringUtils.isEmpty(reportCard.getSysPressUnitName())) {
                    boolean formatSysPressUnitNameFlag = CommonUtils.isCNChar(reportCard.getSysPressUnitName());
                    boolean formatSysPressUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getSysPressUnitName(), 8);
                    if (!(formatSysPressUnitNameFlag && formatSysPressUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血压收缩压剂量单位名称格式不正确!");
                        flag = false;
                    }
                }
                //判断diasPressResult血压舒张压结果格式
                if(!StringUtils.isEmpty(reportCard.getDiasPressResult())){
                    boolean formatDiasPressResultFlag = CommonUtils.isDouNot(reportCard.getDiasPressResult());
                    if (!formatDiasPressResultFlag ) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血压舒张压结果格式不正确!");
                        flag = false;
                    }

                }
                //判断diasPressUnitName血压舒张压计量单位名称
                if (!StringUtils.isEmpty(reportCard.getDiasPressUnitName())) {
                    boolean formatSysPressUnitNameFlag = CommonUtils.isCNChar(reportCard.getDiasPressUnitName());
                    boolean formatSysPressUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getDiasPressUnitName(), 8);
                    if (!(formatSysPressUnitNameFlag && formatSysPressUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血压舒张压计量单位名称格式不正确!");
                        flag = false;
                    }

                }
                //判断WBCResult血常规白细胞计数（WBC）结果
                if(!StringUtils.isEmpty(reportCard.getWBCResult())){
                    boolean formatWBCResultFlag = CommonUtils.isCNChar(reportCard.getWBCResult());
                    boolean formatWBCResultLenFlag = CommonUtils.isMaxLength(reportCard.getWBCResult(), 8);
                    if (!(formatWBCResultFlag && formatWBCResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规白细胞计数(WBC)结果格式不正确!");
                        flag = false;
                    }
                } else{
                        for(String hazard:hazardList){
                            logger.info("====hazard===="+hazard);
                            if("铅".equals(hazard) || "苯".equals(hazard) || "布鲁氏菌".equals(hazard)) {
                                logger.info("血常规白细胞计数(WBC)结果不能为空!");
                                Element errorData = errorReportCards.addElement("errorData");
                                Element errorMessage = errorData.addElement("errorMessage");
                                Element reportCardId = errorData.addElement("reportCard");
                                reportCardId.setText(reportCard.getCode());
                                errorMessage.setText("血常规白细胞计数(WBC)结果不能为空!");
                                flag = false;
                                break;
                            }
                        }
                }
                //判断WBCUnitName血常规白细胞计数（WBC）计量单位名称
                if (!StringUtils.isEmpty(reportCard.getWBCUnitName())) {
                    boolean formatWBCUnitNameFlag = CommonUtils.isCNChar(reportCard.getWBCUnitName());
                    boolean formatWBCUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getWBCUnitName(), 16);
                    if (!(formatWBCUnitNameFlag && formatWBCUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规白细胞计数(WBC)计量单位名称格式不正确!");
                        flag = false;
                    }

                }
                //判断WBCMiniRange血常规白细胞计数（WBC）参考范围最小值格式
                if (!StringUtils.isEmpty(reportCard.getWBCMiniRange())) {
                    boolean formatWBCMiniRangeFlag = CommonUtils.isCNChar(reportCard.getWBCMiniRange());
                    boolean formatWBCMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getWBCMiniRange(), 16);
                    if (!(formatWBCMiniRangeFlag && formatWBCMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规白细胞计数(WBC)参考范围最小值格式不正确!");
                        flag = false;
                    }

                }
                //判断WBCMaxRange血常规白细胞计数（WBC）参考范围最大值格式
                if (!StringUtils.isEmpty(reportCard.getWBCMaxRange())) {
                    boolean formatWBCMaxRangeFlag = CommonUtils.isCNChar(reportCard.getWBCMaxRange());
                    boolean formatWBCMaxRangeLenFlag = CommonUtils.isMaxLength(reportCard.getWBCMaxRange(), 16);
                    if (!(formatWBCMaxRangeFlag && formatWBCMaxRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规白细胞计数（WBC）参考范围最大值格式不正确!");
                        flag = false;
                    }

                }
                //判断RBCResult血常规红细胞计数（RBC）结果
                if(!StringUtils.isEmpty(reportCard.getRBCResult())){
                    boolean formatRBCResultFlag = CommonUtils.isCNChar(reportCard.getRBCResult());
                    boolean formatRBCResultLenFlag = CommonUtils.isMaxLength(reportCard.getRBCResult(), 8);
                    if (!(formatRBCResultFlag && formatRBCResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规红细胞计数(RBC)结果格式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("铅".equals(hazard) || "苯".equals(hazard) || "布鲁氏菌".equals(hazard)) {
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("血常规红细胞计数(RBC)结果不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //判断RBCUnitName血常规红细胞计数（RBC）计量单位名称
                if (!StringUtils.isEmpty(reportCard.getRBCUnitName())) {
                    boolean formatRBCUnitNameFlag = CommonUtils.isCNChar(reportCard.getRBCUnitName());
                    boolean formatRBCUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getRBCUnitName(), 16);
                    if (!(formatRBCUnitNameFlag && formatRBCUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规红细胞计数（RBC）计量单位名称格式不正确!");
                        flag = false;
                    }

                }
                //判断RBCMiniRange血常规红细胞计数（RBC）参考范围最小值
                if (!StringUtils.isEmpty(reportCard.getRBCMiniRange())) {
                    boolean formatRBCMiniRangeFlag = CommonUtils.isCNChar(reportCard.getRBCMiniRange());
                    boolean formatRBCMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getRBCMiniRange(), 16);
                    if (!(formatRBCMiniRangeFlag && formatRBCMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规红细胞计数（RBC）参考范围最小值格式不正确!");
                        flag = false;
                    }

                }
                //判断RBCMaxRange血常规红细胞计数（RBC）参考范围最大值
                if (!StringUtils.isEmpty(reportCard.getRBCMaxRange())) {
                    boolean formatRBCMaxRangeFlag = CommonUtils.isCNChar(reportCard.getRBCMaxRange());
                    boolean formatRBCMaxRangeLenFlag = CommonUtils.isMaxLength(reportCard.getRBCMaxRange(), 16);
                    if (!(formatRBCMaxRangeFlag && formatRBCMaxRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规红细胞计数（RBC）参考范围最大值格式不正确!");
                        flag = false;
                    }

                }
                //判断HbResult血常规血红蛋白（Hb）结果格式
                if (!StringUtils.isEmpty(reportCard.getHbResult())) {
                    boolean formatHbResultFlag = CommonUtils.isCNChar(reportCard.getHbResult());
                    boolean formatHbResultLenFlag = CommonUtils.isMaxLength(reportCard.getHbResult(), 8);
                    if (!(formatHbResultFlag && formatHbResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规血红蛋白（Hb）结果格式格式不正确!");
                        flag = false;
                    }

                }
                //判断HbUnitName血常规血红蛋白（Hb）计量单位名称格式
                if (!StringUtils.isEmpty(reportCard.getHbUnitName())) {
                    boolean formatHbUnitNameFlag = CommonUtils.isCNChar(reportCard.getHbUnitName());
                    boolean formatHbUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getHbUnitName(), 8);
                    if (!(formatHbUnitNameFlag && formatHbUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规血红蛋白（Hb）计量单位名称格式不正确!");
                        flag = false;
                    }
                }
                //判断HbMiniRange血常规血红蛋白（Hb）参考范围最小值
                if (!StringUtils.isEmpty(reportCard.getHbMiniRange())) {
                    boolean formatHbMiniRangeFlag = CommonUtils.isCNChar(reportCard.getHbMiniRange());
                    boolean formatHHbMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getHbMiniRange(), 16);
                    if (!(formatHbMiniRangeFlag && formatHHbMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规血红蛋白（Hb）参考范围最小值格式不正确!");
                        flag = false;
                    }
                }
                //判断HbMaxRange血常规血红蛋白（Hb）参考范围最大值格式
                if (!StringUtils.isEmpty(reportCard.getHbMaxRange())) {
                    boolean formatHbMiniRangeFlag = CommonUtils.isCNChar(reportCard.getHbMaxRange());
                    boolean formatHHbMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getHbMaxRange(), 16);
                    if (!(formatHbMiniRangeFlag && formatHHbMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规血红蛋白（Hb）参考范围最大值格式格式不正确!");
                        flag = false;
                    }
                }
                //判断PLTResult血常规血小板计数（PLT）结果
                if(!StringUtils.isEmpty(reportCard.getPLTResult())){
                    boolean formatPLTResultFlag = CommonUtils.isCNChar(reportCard.getPLTResult());
                    boolean formatPLTResultLenFlag = CommonUtils.isMaxLength(reportCard.getPLTResult(), 8);
                    if (!(formatPLTResultFlag && formatPLTResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规血小板计数（PLT）结果格式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("铅".equals(hazard) || "苯".equals(hazard) || "布鲁氏菌".equals(hazard)) {
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("血常规血小板计数（PLT）结果不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //判断PLTUnitName血常规血小板计数（PLT）计量单位名称
                if (!StringUtils.isEmpty(reportCard.getPLTUnitName())) {
                    boolean formatPLTUnitNameFlag = CommonUtils.isCNChar(reportCard.getPLTUnitName());
                    boolean formatPLTUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getPLTUnitName(), 16);
                    if (!(formatPLTUnitNameFlag && formatPLTUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规血小板计数（PLT）计量单位名称格式不正确!");
                        flag = false;
                    }
                }
                //判断PLTMiniRange血常规血小板计数（PLT）参考范围最小值
                if (!StringUtils.isEmpty(reportCard.getPLTMiniRange())) {
                    boolean formatPLTMiniRangeFlag = CommonUtils.isCNChar(reportCard.getPLTMiniRange());
                    boolean formatPLTMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getPLTMiniRange(), 16);
                    if (!(formatPLTMiniRangeFlag && formatPLTMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规血小板计数（PLT）参考范围最小值格式不正确!");
                        flag = false;
                    }
                }
                //判断PLTMaxRange血常规血小板计数（PLT）参考范围最大值
                if (!StringUtils.isEmpty(reportCard.getPLTMaxRange())) {
                    boolean formatPLTMaxRangeFlag = CommonUtils.isCNChar(reportCard.getPLTMaxRange());
                    boolean formatPLTMaxRangeLenFlag = CommonUtils.isMaxLength(reportCard.getPLTMaxRange(), 16);
                    if (!(formatPLTMaxRangeFlag && formatPLTMaxRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规血小板计数（PLT）参考范围最大值格式不正确!");
                        flag = false;
                    }
                }
                //判断BGLUResult血常规血糖（GLU）结果
                if(!StringUtils.isEmpty(reportCard.getBGLUResult())){
                    boolean formatPLTResultFlag = CommonUtils.isCNChar(reportCard.getPLTResult());
                    boolean formatPLTResultLenFlag = CommonUtils.isMaxLength(reportCard.getPLTResult(), 8);
                    if (!(formatPLTResultFlag && formatPLTResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规血糖（GLU）结果格式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("正己烷".equals(hazard) || "高温".equals(hazard)) {
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("血常规血糖（GLU）结果不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //BGLUUnitName血常规血糖（GLU）计量单位名称
                if (!StringUtils.isEmpty(reportCard.getBGLUUnitName())) {
                    boolean formatBGLUUnitNameFlag = CommonUtils.isCNChar(reportCard.getBGLUUnitName());
                    boolean formatBGLUUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getBGLUUnitName(), 16);
                    if (!(formatBGLUUnitNameFlag && formatBGLUUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规血糖（GLU）计量单位名称格式不正确!");
                        flag = false;
                    }
                }
                //BGLUMiniRange血常规血糖（GLU）参考范围最小值
                if (!StringUtils.isEmpty(reportCard.getBGLUMiniRange())) {
                    boolean formatBGLUMiniRangeFlag = CommonUtils.isCNChar(reportCard.getBGLUMiniRange());
                    boolean formatBGLUMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getBGLUMiniRange(), 16);
                    if (!(formatBGLUMiniRangeFlag && formatBGLUMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规血糖（GLU）参考范围最小值格式不正确!");
                        flag = false;
                    }
                }
                //BGLUMaxRange血常规血糖（GLU）参考范围最大值
                if (!StringUtils.isEmpty(reportCard.getBGLUMaxRange())) {
                    boolean formatBGLUMaxRangeFlag = CommonUtils.isCNChar(reportCard.getBGLUMaxRange());
                    boolean formatBGLUMaxRangeLenFlag = CommonUtils.isMaxLength(reportCard.getBGLUMaxRange(), 16);
                    if (!(formatBGLUMaxRangeFlag && formatBGLUMaxRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规血糖（GLU）参考范围最大值格式不正确!");
                        flag = false;
                    }
                }
                //GLUResult尿常规尿糖（GLU）结果
                if(!StringUtils.isEmpty(reportCard.getGLUResult())){
                    boolean formatGLUResultFlag = CommonUtils.isCNChar(reportCard.getGLUResult());
                    boolean formatGLUResultLenFlag = CommonUtils.isMaxLength(reportCard.getGLUResult(), 8);
                    if (!(formatGLUResultFlag && formatGLUResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿常规尿糖（GLU）结果格式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("正己烷".equals(hazard) || "高温".equals(hazard)) {
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("尿常规尿糖（GLU）结果不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //GLUUnitName尿常规尿糖（GLU）计量单位
                if (!StringUtils.isEmpty(reportCard.getGLUUnitName())) {
                    boolean formatGLUUnitNameFlag = CommonUtils.isCNChar(reportCard.getGLUUnitName());
                    boolean formatGLUUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getGLUUnitName(), 16);
                    if (!(formatGLUUnitNameFlag && formatGLUUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿常规尿糖（GLU）计量单位格式不正确!");
                        flag = false;
                    }
                }
                //GLUMiniRange尿常规尿糖（GLU）参考范围最小值
                if (!StringUtils.isEmpty(reportCard.getGLUMiniRange())) {
                    boolean formatGLUMiniRangeFlag = CommonUtils.isCNChar(reportCard.getGLUMiniRange());
                    boolean formatGLUMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getGLUMiniRange(), 16);
                    if (!(formatGLUMiniRangeFlag && formatGLUMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿常规尿糖（GLU）参考范围最小值格式不正确!");
                        flag = false;
                    }
                }
                //GLUMaxRange尿常规尿糖（GLU）参考范围最大值
                if (!StringUtils.isEmpty(reportCard.getGLUMaxRange())) {
                    boolean formatGLUMaxRangeFlag = CommonUtils.isCNChar(reportCard.getGLUMaxRange());
                    boolean formatGLUMaxRangeLenFlag = CommonUtils.isMaxLength(reportCard.getGLUMaxRange(), 16);
                    if (!(formatGLUMaxRangeFlag && formatGLUMaxRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿常规尿糖（GLU）参考范围最大值格式不正确!");
                        flag = false;
                    }
                }
                //PROResult尿常规尿蛋白（PRO）结果
                if(!StringUtils.isEmpty(reportCard.getPROResult())){
                    boolean formatPROResultFlag = CommonUtils.isCNChar(reportCard.getPROResult());
                    boolean formatPROResultLenFlag = CommonUtils.isMaxLength(reportCard.getPROResult(), 8);
                    if (!(formatPROResultFlag && formatPROResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿常规尿蛋白（PRO）结果格式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("铅".equals(hazard) || "布鲁氏菌".equals(hazard)|| "苯".equals(hazard)|| "正己烷".equals(hazard) || "高温".equals(hazard)) {
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("尿常规尿蛋白（PRO）结果不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //PROUnitName尿常规尿蛋白（PRO）计量单位名称
                if (!StringUtils.isEmpty(reportCard.getPROUnitName())) {
                    boolean formatPROUnitNameFlag = CommonUtils.isCNChar(reportCard.getPROUnitName());
                    boolean formatPROUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getPROUnitName(), 16);
                    if (!(formatPROUnitNameFlag && formatPROUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿常规尿蛋白（PRO）计量单位名称格式不正确!");
                        flag = false;
                    }
                }
                //PROMiniRange尿常规尿蛋白（PRO）参考范围最小值
                if (!StringUtils.isEmpty(reportCard.getPROMiniRange())) {
                    boolean formatPROMiniRangeFlag = CommonUtils.isCNChar(reportCard.getPROMiniRange());
                    boolean formatPROMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getPROMiniRange(), 16);
                    if (!(formatPROMiniRangeFlag && formatPROMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿常规尿蛋白（PRO）参考范围最小值格式不正确!");
                        flag = false;
                    }
                }
                //PROMaxRange尿常规尿蛋白（PRO）参考范围最大值
                if (!StringUtils.isEmpty(reportCard.getPROMaxRange())) {
                    boolean formatPROMaxRangeFlag = CommonUtils.isCNChar(reportCard.getPROMaxRange());
                    boolean formatPROMaxRangeLenFlag = CommonUtils.isMaxLength(reportCard.getPROMaxRange(), 16);
                    if (!(formatPROMaxRangeFlag && formatPROMaxRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿常规尿蛋白（PRO）参考范围最大值格式不正确!");
                        flag = false;
                    }
                }
                //UWBCResult尿常规白细胞（WBC）结果
                if(!StringUtils.isEmpty(reportCard.getUWBCResult())){
                    boolean formatUWBCResultFlag = CommonUtils.isCNChar(reportCard.getUWBCResult());
                    boolean formatUWBCResultLenFlag = CommonUtils.isMaxLength(reportCard.getUWBCResult(), 8);
                    if (!(formatUWBCResultFlag && formatUWBCResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿常规白细胞（WBC）结果格式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("铅".equals(hazard) || "布鲁氏菌".equals(hazard)|| "苯".equals(hazard)|| "正己烷".equals(hazard) || "高温".equals(hazard)) {
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("尿常规白细胞（WBC）结果不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //UWBCUnitName尿常规白细胞（WBC）计量单位名称
                if (!StringUtils.isEmpty(reportCard.getUWBCUnitName())) {
                    boolean formatUWBCUnitNameFlag = CommonUtils.isCNChar(reportCard.getUWBCUnitName());
                    boolean formatUWBCUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getUWBCUnitName(), 16);
                    if (!(formatUWBCUnitNameFlag && formatUWBCUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿常规白细胞（WBC）计量单位名称格式不正确!");
                        flag = false;
                    }
                }
                //UWBCMiniRange尿常规白细胞（WBC）参考范围最小值
                if (!StringUtils.isEmpty(reportCard.getUWBCMiniRange())) {
                    boolean formatUWBCMiniRangeFlag = CommonUtils.isCNChar(reportCard.getUWBCMiniRange());
                    boolean formatUWBCMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getUWBCMiniRange(), 16);
                    if (!(formatUWBCMiniRangeFlag && formatUWBCMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿常规白细胞（WBC）参考范围最小值格式不正确!");
                        flag = false;
                    }
                }
                //UWBCMaxRange尿常规白细胞（WBC）参考范围最大值
                if (!StringUtils.isEmpty(reportCard.getUWBCMaxRange())) {
                    boolean formatUWBCMaxRangeFlag = CommonUtils.isCNChar(reportCard.getUWBCMaxRange());
                    boolean formatUWBCMaxRangeLenFlag = CommonUtils.isMaxLength(reportCard.getUWBCMaxRange(), 16);
                    if (!(formatUWBCMaxRangeFlag && formatUWBCMaxRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿常规白细胞（WBC）参考范围最大值格式不正确!");
                        flag = false;
                    }
                }
                //BLDResult尿常规尿潜血（BLD）结果
                if(!StringUtils.isEmpty(reportCard.getBLDResult())){
                    boolean formatBLDResultFlag = CommonUtils.isCNChar(reportCard.getBLDResult());
                    boolean formatBLDResultLenFlag = CommonUtils.isMaxLength(reportCard.getBLDResult(), 8);
                    if (!(formatBLDResultFlag && formatBLDResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿常规尿潜血（BLD）结果格式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("铅".equals(hazard) || "布鲁氏菌".equals(hazard)|| "苯".equals(hazard)|| "正己烷".equals(hazard) || "高温".equals(hazard)) {
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("尿常规尿潜血（BLD）结果不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //BLDUnitName尿常规尿潜血（BLD）计量单位名称
                if (!StringUtils.isEmpty(reportCard.getBLDUnitName())) {
                    boolean formatBLDUnitNameFlag = CommonUtils.isCNChar(reportCard.getBLDUnitName());
                    boolean formatBLDUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getBLDUnitName(), 16);
                    if (!(formatBLDUnitNameFlag && formatBLDUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿常规尿潜血（BLD）计量单位名称格式不正确!");
                        flag = false;
                    }
                }
                //BLDMiniRange尿常规尿潜血（BLD）参考范围最小值
                if (!StringUtils.isEmpty(reportCard.getBLDMiniRange())) {
                    boolean formatBLDMiniRangeFlag = CommonUtils.isCNChar(reportCard.getBLDMiniRange());
                    boolean formatBLDMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getBLDMiniRange(), 16);
                    if (!(formatBLDMiniRangeFlag && formatBLDMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿常规尿潜血（BLD）参考范围最小值格式不正确!");
                        flag = false;
                    }
                }
                //BLDMaxRange尿常规尿潜血（BLD）参考范围最大值
                if (!StringUtils.isEmpty(reportCard.getBLDMaxRange())) {
                    boolean formatBLDMaxRangeFlag = CommonUtils.isCNChar(reportCard.getBLDMaxRange());
                    boolean formatBLDMaxRangeLenFlag = CommonUtils.isMaxLength(reportCard.getBLDMaxRange(), 16);
                    if (!(formatBLDMaxRangeFlag && formatBLDMaxRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿常规尿潜血（BLD）参考范围最大值格式不正确!");
                        flag = false;
                    }
                }
                //ALTResult肝功能谷丙转氨酶（ALT）结果
                if(!StringUtils.isEmpty(reportCard.getALTResult())){
                    boolean formatALTResultFlag = CommonUtils.isCNChar(reportCard.getALTResult());
                    boolean formatALTResultLenFlag = CommonUtils.isMaxLength(reportCard.getALTResult(), 8);
                    if (!(formatALTResultFlag && formatALTResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("肝功能谷丙转氨酶（ALT）结果格式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("布鲁氏菌".equals(hazard)|| "苯".equals(hazard)|| "正己烷".equals(hazard) || "高温".equals(hazard)) {
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("尿常规尿潜血（BLD）结果不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //ALTUnitName肝功能谷丙转氨酶（ALT）计量单位名称
                if (!StringUtils.isEmpty(reportCard.getALTUnitName())) {
                    boolean formatALTUnitNameFlag = CommonUtils.isCNChar(reportCard.getALTUnitName());
                    boolean formatALTUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getALTUnitName(), 16);
                    if (!(formatALTUnitNameFlag && formatALTUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("肝功能谷丙转氨酶（ALT）计量单位名称格式不正确!");
                        flag = false;
                    }
                }
                //ALTMiniRange肝功能谷丙转氨酶（ALT）参考范围最小值
                if (!StringUtils.isEmpty(reportCard.getALTMiniRange())) {
                    boolean formatALTMiniRangeFlag = CommonUtils.isCNChar(reportCard.getALTMiniRange());
                    boolean formatALTMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getALTMiniRange(), 16);
                    if (!(formatALTMiniRangeFlag && formatALTMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("肝功能谷丙转氨酶（ALT）参考范围最小值格式不正确!");
                        flag = false;
                    }
                }
                //ALTMaxRange肝功能谷丙转氨酶（ALT）参考范围最大值
                if (!StringUtils.isEmpty(reportCard.getALTMaxRange())) {
                    boolean formatALTMaxRangeFlag = CommonUtils.isCNChar(reportCard.getALTMaxRange());
                    boolean formatALTMaxRangeLenFlag = CommonUtils.isMaxLength(reportCard.getALTMaxRange(), 16);
                    if (!(formatALTMaxRangeFlag && formatALTMaxRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("肝功能谷丙转氨酶（ALT）参考范围最大值格式不正确!");
                        flag = false;
                    }
                }
                //ECGCode心电图编码
                String eCGCode = reportCard.getECGCode();
                List<CodeInfo> eCGCodeList = codeInfoServiceImpl.selectByCodeInfoId(new BigDecimal(902));
                if (eCGCode.indexOf(",") == -1) {
                    boolean cHESTCodeListInflag = false;
                    for (CodeInfo codeInfo : eCGCodeList) {
                        if (eCGCode.equals(codeInfo.getCode())) {
                            cHESTCodeListInflag = true;
                            break;
                        }
                    }
                    if (!cHESTCodeListInflag) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("心电图编码格式不正确!");
                        flag = false;
                    }
                } else {
                    String[] eCGCodeSize = eCGCode.split(",");
                    boolean formatHazardCodeLenFlag = CommonUtils.isMaxLength(eCGCode, 255);
                    if(formatHazardCodeLenFlag){
                        boolean[] cHESTCodeListInflag = new boolean[eCGCodeSize.length];
                        for (int i = 0; i < eCGCodeSize.length; i++) {
                            cHESTCodeListInflag[i]=false;
                            for (CodeInfo codeInfo : eCGCodeList) {
                                if (eCGCodeSize[i].equals(codeInfo.getCode())) {
                                    cHESTCodeListInflag[i] = true;
                                    break;
                                }
                            }
                        }
                        boolean CHESTCodeflag = true;
                        for(Boolean cHESTCodeflag:cHESTCodeListInflag){
                            if(!cHESTCodeflag){
                                CHESTCodeflag=false;
                                break;
                            }
                        }
                        if(!CHESTCodeflag){
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("心电图编码格式不正确!");
                            flag = false;
                            break;
                        }
                    }else{
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("心电图编码格式不正确!");
                        flag = false;
                    }
                }

                //CHESTCode胸片编码
                String cHESTCode = reportCard.getCHESTCode();
                List<CodeInfo> cHESTCodeList = codeInfoServiceImpl.selectByCodeInfoId(new BigDecimal(903));
                if(!StringUtils.isEmpty(cHESTCode)){
                    if(cHESTCode.indexOf(",") == -1){
                        boolean cHESTCodeListInflag = false;
                        for (CodeInfo codeInfo : cHESTCodeList) {
                            if (cHESTCode.equals(codeInfo.getCode())) {
                                cHESTCodeListInflag = true;
                                break;
                            }
                        }
                        if (!cHESTCodeListInflag) {
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("胸片编码格式不正确!");
                            flag = false;
                        }
                    } else {
                        String[] cHESTCodeSize = cHESTCode.split(",");
                        boolean formatHazardCodeLenFlag = CommonUtils.isMaxLength(eCGCode, 255);
                        if(formatHazardCodeLenFlag){
                            boolean[] cHESTCodeListInflag = new boolean[cHESTCodeSize.length];
                            for (int i = 0; i < cHESTCodeSize.length; i++) {
                                cHESTCodeListInflag[i]=false;
                                for (CodeInfo codeInfo : cHESTCodeList) {
                                    if (cHESTCodeSize[i].equals(codeInfo.getCode())) {
                                        cHESTCodeListInflag[i] = true;
                                        break;
                                    }
                                }
                            }
                            boolean CHESTCodeflag = true;
                            for(Boolean cHESTCodeflag:cHESTCodeListInflag){
                                if(!cHESTCodeflag){
                                    CHESTCodeflag=false;
                                    break;
                                }
                            }
                            if(!CHESTCodeflag){
                                Element errorData = errorReportCards.addElement("errorData");
                                Element errorMessage = errorData.addElement("errorMessage");
                                Element reportCardId = errorData.addElement("reportCard");
                                reportCardId.setText(reportCard.getCode());
                                errorMessage.setText("胸片编码格式不正确!");
                                flag = false;
                                break;
                            }
                        }else{
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("胸片编码格式格式不正确!");
                            flag = false;
                        }
                    }
                }else{
                    for(String hazard:hazardList){
                        if("矽尘".equals(hazard)|| "煤尘(煤矽尘)".equals(hazard)|| "石棉".equals(hazard) || "电焊烟尘".equals(hazard)) {
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("尿常规尿潜血（BLD）结果不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //FVCResult肺功能FVC结果
                if(!StringUtils.isEmpty(reportCard.getFVCResult())){
                    boolean formatFVCResultFlag = CommonUtils.isCNChar(reportCard.getFVCResult());
                    boolean formatFVCResultLenFlag = CommonUtils.isMaxLength(reportCard.getFVCResult(), 8);
                    if (!(formatFVCResultFlag && formatFVCResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("肺功能FVC结果格式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("矽尘".equals(hazard) || "煤尘(煤矽尘)".equals(hazard)|| "石棉".equals(hazard)|| "电焊烟尘".equals(hazard)) {
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("肺功能FVC结果不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //FVCUnitName肺功能FVC计量单位名称
                if (!StringUtils.isEmpty(reportCard.getFVCUnitName())) {
                    boolean formatFVCUnitNameFlag = CommonUtils.isCNChar(reportCard.getFVCUnitName());
                    boolean formatFVCUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getFVCUnitName(), 16);
                    if (!(formatFVCUnitNameFlag && formatFVCUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("肺功能FVC计量单位名称格式不正确!");
                        flag = false;
                    }
                }
                //FVCMiniRange肺功能FVC参考范围最小值
                if (!StringUtils.isEmpty(reportCard.getFVCMiniRange())) {
                    boolean formatFVCMiniRangeFlag = CommonUtils.isCNChar(reportCard.getFVCMiniRange());
                    boolean formatFVCMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getFVCUnitName(), 16);
                    if (!(formatFVCMiniRangeFlag && formatFVCMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("肺功能FVC参考范围最小值格式不正确!");
                        flag = false;
                    }
                }
                //FVCMaxRange肺功能FVC参考范围最大值
                if (!StringUtils.isEmpty(reportCard.getFVCMaxRange())) {
                    boolean formatFVCMaxRangeFlag = CommonUtils.isCNChar(reportCard.getFVCMaxRange());
                    boolean formatFVCMaxRangeLenFlag = CommonUtils.isMaxLength(reportCard.getFVCMaxRange(), 16);
                    if (!(formatFVCMaxRangeFlag && formatFVCMaxRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("肺功能FVC参考范围最大值格式不正确!");
                        flag = false;
                    }
                }
                //FEV1Result肺功能FEV1结果
                if(!StringUtils.isEmpty(reportCard.getFEV1Result())){
                    boolean formatFEV1ResultFlag = CommonUtils.isCNChar(reportCard.getFEV1Result());
                    boolean formatFEV1ResultLenFlag = CommonUtils.isMaxLength(reportCard.getFEV1Result(), 8);
                    if (!(formatFEV1ResultFlag && formatFEV1ResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("肺功能FEV1结果格式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("矽尘".equals(hazard) || "煤尘(煤矽尘)".equals(hazard)|| "石棉".equals(hazard)|| "电焊烟尘".equals(hazard)) {
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("肺功能FEV1结果不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //FEV1UnitName肺功能FEV1计量单位名称
                if (!StringUtils.isEmpty(reportCard.getFEV1UnitName())) {
                    boolean formatFEV1UnitNameFlag = CommonUtils.isCNChar(reportCard.getFEV1UnitName());
                    boolean formatFEV1UnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getFEV1UnitName(), 16);
                    if (!(formatFEV1UnitNameFlag && formatFEV1UnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("肺功能FEV1计量单位名称格式不正确!");
                        flag = false;
                    }
                }
                //FEV1MiniRange肺功能FEV1参考范围最小值
                if (!StringUtils.isEmpty(reportCard.getFEV1MiniRange())) {
                    boolean formatFEV1MiniRangeFlag = CommonUtils.isCNChar(reportCard.getFEV1MiniRange());
                    boolean formatFEV1MiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getFEV1MiniRange(), 16);
                    if (!(formatFEV1MiniRangeFlag && formatFEV1MiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("肺功能FEV1参考范围最小值格式不正确!");
                        flag = false;
                    }
                }
                //FEV1MaxRange肺功能FEV1参考范围最大值
                if (!StringUtils.isEmpty(reportCard.getFEV1MaxRange())) {
                    boolean formatFEV1MaxRangeFlag = CommonUtils.isCNChar(reportCard.getFEV1MaxRange());
                    boolean formatFEV1MaxRangeLenFlag = CommonUtils.isMaxLength(reportCard.getFEV1MaxRange(), 16);
                    if (!(formatFEV1MaxRangeFlag && formatFEV1MaxRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("肺功能FEV1参考范围最大值格式不正确!");
                        flag = false;
                    }
                }
                //FEV1FVCResult肺功能FEV1/FVC结果
                if(!StringUtils.isEmpty(reportCard.getFEV1FVCResult())){
                    boolean formatFEV1FVCResultFlag = CommonUtils.isCNChar(reportCard.getFEV1FVCResult());
                    boolean formatFEV1FVCResultLenFlag = CommonUtils.isMaxLength(reportCard.getFEV1FVCResult(), 8);
                    if (!(formatFEV1FVCResultFlag && formatFEV1FVCResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("肺功能FEV1/FVC结果格式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("矽尘".equals(hazard) || "煤尘(煤矽尘)".equals(hazard)|| "石棉".equals(hazard)|| "电焊烟尘".equals(hazard)) {
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("肺功能FEV1/FVC结果不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //FEV1FVCUnitName肺功能FEV1/FVC计量
                if (!StringUtils.isEmpty(reportCard.getFEV1FVCUnitName())) {
                    boolean formatFEV1FVCUnitNameFlag = CommonUtils.isCNChar(reportCard.getFEV1FVCUnitName());
                    boolean formatFEV1FVCUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getFEV1FVCUnitName(), 16);
                    if (!(formatFEV1FVCUnitNameFlag && formatFEV1FVCUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("肺功能FEV1/FVC计量格式不正确!");
                        flag = false;
                    }
                }
                //FEV1FVCMiniRange肺功能FEV1/FVC参考范围最小值
                if (!StringUtils.isEmpty(reportCard.getFEV1FVCMiniRange())) {
                    boolean formatFEV1FVCMiniRangeFlag = CommonUtils.isCNChar(reportCard.getFEV1FVCMiniRange());
                    boolean formatFEV1FVCMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getFEV1FVCMiniRange(), 16);
                    if (!(formatFEV1FVCMiniRangeFlag && formatFEV1FVCMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("肺功能FEV1/FVC参考范围最小值格式不正确!");
                        flag = false;
                    }
                }
                //FEV1FVCMaxRange肺功能FEV1/FVC参考范围最大值
                if (!StringUtils.isEmpty(reportCard.getFEV1FVCMaxRange())) {
                    boolean formatFEV1FVCMaxRangeFlag = CommonUtils.isCNChar(reportCard.getFEV1FVCMaxRange());
                    boolean formatFEV1FVCMaxRangeLenFlag = CommonUtils.isMaxLength(reportCard.getFEV1FVCMaxRange(), 16);
                    if (!(formatFEV1FVCMaxRangeFlag && formatFEV1FVCMaxRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("肺功能FEV1/FVC参考范围最大值格式不正确!");
                        flag = false;
                    }
                }
                //BLeadResult血铅结果
                if(!StringUtils.isEmpty(reportCard.getBLeadResult())){
                    boolean formatBLeadResultFlag = CommonUtils.isCNChar(reportCard.getBLeadResult());
                    boolean formatBLeadResultLenFlag = CommonUtils.isMaxLength(reportCard.getBLeadResult(), 8);
                    if (!(formatBLeadResultFlag && formatBLeadResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血铅结果格式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("铅".equals(hazard)){
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("血铅结果不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //BLeadUnitName血铅计量单位名称
                if (!StringUtils.isEmpty(reportCard.getBLeadUnitName())) {
                    boolean formatBLeadUnitNameFlag = CommonUtils.isCNChar(reportCard.getBLeadUnitName());
                    boolean formatBLeadUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getBLeadUnitName(), 16);
                    if (!(formatBLeadUnitNameFlag && formatBLeadUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血铅计量单位名称格式不正确!");
                        flag = false;
                    }
                }
                //BLeadMiniRange血铅参考范围最小值
                if (!StringUtils.isEmpty(reportCard.getBLeadMiniRange())) {
                    boolean formatBLeadMiniRangeFlag = CommonUtils.isCNChar(reportCard.getBLeadMiniRange());
                    boolean formatBLeadMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getBLeadMiniRange(), 16);
                    if (!(formatBLeadMiniRangeFlag && formatBLeadMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血铅参考范围最小值格式不正确!");
                        flag = false;
                    }
                }
                //BLeadMaxRange血铅参考范围最大值
                if (!StringUtils.isEmpty(reportCard.getBLeadMaxRange())) {
                    boolean formatBLeadMaxRangeFlag = CommonUtils.isCNChar(reportCard.getBLeadMaxRange());
                    boolean formatBLeadMaxRangeLenFlag = CommonUtils.isMaxLength(reportCard.getBLeadMaxRange(), 16);
                    if (!(formatBLeadMaxRangeFlag && formatBLeadMaxRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血铅参考范围最大值格式不正确!");
                        flag = false;
                    }
                }
                //ULeadResult尿铅结果
                if(!StringUtils.isEmpty(reportCard.getULeadResult())){
                    boolean formatULeadResultFlag = CommonUtils.isCNChar(reportCard.getULeadResult());
                    boolean formatULeadResultLenFlag = CommonUtils.isMaxLength(reportCard.getULeadResult(), 8);
                    if (!(formatULeadResultFlag && formatULeadResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿铅结果式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("铅".equals(hazard)){
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("尿铅结果不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //ULeadUnitName尿铅计量单位名称
                if (!StringUtils.isEmpty(reportCard.getULeadUnitName())) {
                    boolean formatULeadUnitNameFlag = CommonUtils.isCNChar(reportCard.getULeadUnitName());
                    boolean formatULeadUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getULeadUnitName(), 16);
                    if (!(formatULeadUnitNameFlag && formatULeadUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿铅计量单位名称格式不正确!");
                        flag = false;
                    }
                }
                //ULeadMiniRange尿铅参考范围最小值
                if (!StringUtils.isEmpty(reportCard.getULeadMiniRange())) {
                    boolean formatULeadMiniRangeFlag = CommonUtils.isCNChar(reportCard.getULeadMiniRange());
                    boolean formatULeadMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getULeadMiniRange(), 16);
                    if (!(formatULeadMiniRangeFlag && formatULeadMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿铅参考范围最小值格式不正确!");
                        flag = false;
                    }
                }
                //ULeadMaxRange尿铅参考范围最大值
                if (!StringUtils.isEmpty(reportCard.getULeadMaxRange())) {
                    boolean formatULeadMaxRangeFlag = CommonUtils.isCNChar(reportCard.getULeadMaxRange());
                    boolean formatULeadMaxRangeLenFlag = CommonUtils.isMaxLength(reportCard.getULeadMaxRange(), 16);
                    if (!(formatULeadMaxRangeFlag && formatULeadMaxRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("尿铅参考范围最大值格式不正确!");
                        flag = false;
                    }
                }
                //ZPPResult铅红细胞锌原卟啉（ZPP）结果
                if(!StringUtils.isEmpty(reportCard.getZPPResult())){
                    boolean formatZPPResultFlag = CommonUtils.isCNChar(reportCard.getZPPResult());
                    boolean formatZPPResultLenFlag = CommonUtils.isMaxLength(reportCard.getZPPResult(), 8);
                    if (!(formatZPPResultFlag && formatZPPResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("铅红细胞锌原卟啉（ZPP）结果格式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("铅".equals(hazard)){
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("铅红细胞锌原卟啉（ZPP）结果不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //ZPPUnitName铅红细胞锌原卟啉（ZPP）计量单位名称
                if (!StringUtils.isEmpty(reportCard.getZPPUnitName())) {
                    boolean formatZPPUnitNameFlag = CommonUtils.isCNChar(reportCard.getZPPUnitName());
                    boolean formatZPPUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getZPPUnitName(), 16);
                    if (!(formatZPPUnitNameFlag && formatZPPUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("铅红细胞锌原卟啉（ZPP）计量单位名称格式不正确!");
                        flag = false;
                    }
                }
                //ZPPMiniRange铅红细胞锌原卟啉（ZPP）参考范围最小值
                if (!StringUtils.isEmpty(reportCard.getZPPMiniRange())) {
                    boolean formatZPPMiniRangeFlag = CommonUtils.isCNChar(reportCard.getZPPMiniRange());
                    boolean formatZPPMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getZPPMiniRange(), 16);
                    if (!(formatZPPMiniRangeFlag && formatZPPMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("铅红细胞锌原卟啉（ZPP）参考范围最小值格式不正确!");
                        flag = false;
                    }
                }
                //ZPPMaxRange铅红细胞锌原卟啉（ZPP）参考范围最大值
                if (!StringUtils.isEmpty(reportCard.getZPPMaxRange())) {
                    boolean formatZPPMaxRangeFlag = CommonUtils.isCNChar(reportCard.getZPPMaxRange());
                    boolean formatZPPMaxRangeLenFlag = CommonUtils.isMaxLength(reportCard.getZPPMaxRange(), 8);
                    if (!(formatZPPMaxRangeFlag && formatZPPMaxRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("铅红细胞锌原卟啉（ZPP）参考范围最大值格式不正确!");
                        flag = false;
                    }
                }
                //neutResult苯血常规（中性粒细胞绝对值<Neut#>）结果
                if(!StringUtils.isEmpty(reportCard.getNeutResult())){
                    boolean formatgetNeutResultFlag = CommonUtils.isCNChar(reportCard.getNeutResult());
                    boolean formatgetNeutResultLenFlag = CommonUtils.isMaxLength(reportCard.getNeutResult(), 8);
                    if (!(formatgetNeutResultFlag && formatgetNeutResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("苯血常规（中性粒细胞绝对值<Neut#>）结果格式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("苯".equals(hazard)){
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("苯血常规（中性粒细胞绝对值<Neut#>）结果不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //neutUnitName苯血常规（中性粒细胞绝对值<Neut#>）计量单位名称
                if (!StringUtils.isEmpty(reportCard.getNeutUnitName())) {
                    boolean formatNeutUnitNameFlag = CommonUtils.isCNChar(reportCard.getNeutUnitName());
                    boolean formatNeutUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getNeutUnitName(), 16);
                    if (!(formatNeutUnitNameFlag && formatNeutUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("苯血常规(中性粒细胞绝对值<Neut#>)计量单位名称格式不正确!");
                        flag = false;
                    }
                }
                //neutMiniRange血常规（中性粒细胞绝对值<Neut#>）参考范围最小值
                if (!StringUtils.isEmpty(reportCard.getNeutMiniRange())) {
                    boolean formatNeutMiniRangeFlag = CommonUtils.isCNChar(reportCard.getNeutMiniRange());
                    boolean formatNeutMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getNeutMiniRange(), 16);
                    if (!(formatNeutMiniRangeFlag && formatNeutMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("血常规（中性粒细胞绝对值<Neut#>）参考范围最小值格式不正确!");
                        flag = false;
                    }
                }

                //neutMaxRange苯血常规（中性粒细胞绝对值<Neut#>）参考范围最大值
                if (!StringUtils.isEmpty(reportCard.getNeutMiniRange())) {
                    boolean formatNeutMaxRangeFlag = CommonUtils.isCNChar(reportCard.getNeutMaxRange());
                    boolean formatNeutMaxRangeLenFlag = CommonUtils.isMaxLength(reportCard.getNeutMaxRange(), 16);
                    if (!(formatNeutMaxRangeFlag && formatNeutMaxRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("苯血常规（中性粒细胞绝对值<Neut#>）参考范围最大值格式不正确!");
                        flag = false;
                    }
                }
                //hearingResult噪声双耳高频平均听阈（校正值）结果
                if(!StringUtils.isEmpty(reportCard.getHearingReuslt())){
                    boolean formatHearingResultFlag = CommonUtils.isCNChar(reportCard.getHearingReuslt());
                    boolean formatHearingResultLenFlag = CommonUtils.isMaxLength(reportCard.getNeutResult(), 8);
                    if (!(formatHearingResultFlag && formatHearingResultLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("噪声双耳高频平均听阈（校正值）结果格式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("噪声".equals(hazard)){
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("噪声双耳高频平均听阈（校正值）结果不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }

                //hearingUnitName噪声双耳高频平均听阈（校正值）计量单位名称
                if (!StringUtils.isEmpty(reportCard.getHearingUnitName())) {
                    boolean formatHearingUnitNameFlag = CommonUtils.isCNChar(reportCard.getHearingUnitName());
                    boolean formatHearingUnitNameLenFlag = CommonUtils.isMaxLength(reportCard.getHearingUnitName(), 16);
                    if (!(formatHearingUnitNameFlag && formatHearingUnitNameLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("噪声双耳高频平均听阈（校正值）计量单位名称格式不正确!");
                        flag = false;
                    }
                }

                //hearingMiniRange噪声双耳高频平均听阈（校正值）参考范围最小值
                if (!StringUtils.isEmpty(reportCard.getHearingMiniRange())) {
                    boolean formatHearingMiniRangeFlag = CommonUtils.isCNChar(reportCard.getHearingMiniRange());
                    boolean formatHearingMiniRangeLenFlag = CommonUtils.isMaxLength(reportCard.getHearingMiniRange(), 16);
                    if (!(formatHearingMiniRangeFlag && formatHearingMiniRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("噪声双耳高频平均听阈（校正值）参考范围最小值格式不正确!");
                        flag = false;
                    }
                }
                //hearingMaxRange噪声双耳高频平均听阈（校正值）参考范围最大值
                if (!StringUtils.isEmpty(reportCard.getHearingMaxRange())) {
                    boolean formatHearingMaxRangeFlag = CommonUtils.isCNChar(reportCard.getHearingMaxRange());
                    boolean formatHearingMaxRangeLenFlag = CommonUtils.isMaxLength(reportCard.getHearingMaxRange(), 16);
                    if (!(formatHearingMaxRangeFlag && formatHearingMaxRangeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("噪声双耳高频平均听阈（校正值）参考范围最大值格式不正确!");
                        flag = false;
                    }
                }

                //RPBTCode布鲁菌属虎红缓冲液玻片凝3集实验（RPBT）编码
                if(!StringUtils.isEmpty(reportCard.getRPBTCode())){
                    boolean formatRPBTCodeFlag = CommonUtils.isDigist(reportCard.getRPBTCode());
                    boolean formatRPBTCodeLenFlag = CommonUtils.isLength(reportCard.getRPBTCode(), 1);
                    if (!(formatRPBTCodeFlag && formatRPBTCodeLenFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("布鲁菌属虎红缓冲液玻片凝3集实验（RPBT）编码格式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("布鲁氏菌".equals(hazard)){
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("布鲁菌属虎红缓冲液玻片凝3集实验（RPBT）编码不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //wrightCode布鲁菌属试管凝集反应（Wright）(1:100) 编码
                if(!StringUtils.isEmpty(reportCard.getWrightCode())){
                    boolean formatWrightCodeFlag = CommonUtils.idOneNot(reportCard.getWrightCode());
                    if (!(formatWrightCodeFlag)) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("布鲁菌属试管凝集反应（Wright）(1:100)编码格式不正确!");
                        flag = false;
                    }
                } else{
                    for(String hazard:hazardList){
                        if("布鲁氏菌".equals(hazard)){
                            Element errorData = errorReportCards.addElement("errorData");
                            Element errorMessage = errorData.addElement("errorMessage");
                            Element reportCardId = errorData.addElement("reportCard");
                            reportCardId.setText(reportCard.getCode());
                            errorMessage.setText("布鲁菌属试管凝集反应（Wright）(1:100) 编码不能为空!");
                            flag = false;
                            break;
                        }
                    }
                }
                //conclusionsCode体检结论编码
                List<CodeInfo> conclusionsCodeList = codeInfoServiceImpl.selectByCodeInfoId(new BigDecimal(904));
                if(!StringUtils.isEmpty(reportCard.getConclusionsCode())){
                    boolean conclusionsCodeListInflag = false;
                    for (CodeInfo codeInfo : conclusionsCodeList) {
                        logger.info("===codeInfo.getCode()==="+codeInfo.getCode());
                        if (reportCard.getConclusionsCode().equals(codeInfo.getCode())) {
                            conclusionsCodeListInflag = true;
                            logger.info("====reportCard.getConclusionsCode()====="+reportCard.getConclusionsCode()+"===codeInfo.getCode()==="+codeInfo.getCode());
                            break;
                        }
                    }
                    logger.info("===conclusionsCodeListInflag==="+conclusionsCodeListInflag+"===reportCard.getCode()==="+reportCard.getCode());
                    if (!conclusionsCodeListInflag) {
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("体检结论编码不正确!");
                        flag = false;
                    }
                }
            }
        if(flag==true){
            dataInfo.remove(errorDatas);
        }
           return flag;
    }

    //用人单位数据个格式校验
    private boolean isDateFormatEmployer(List<BodyDataEntity> dateStr, Element dataInfo){
        boolean flag = true;
        dataInfo.element("returnCode").setText("-1");
        dataInfo.element("message").setText("部分数据格式有误!");
        Element errorDatas = dataInfo.addElement("errorDatas");
        Element errorEmployingUnits = errorDatas.addElement("errorEmployingUnits");
        //参数日期格式判断
        SimpleDateFormat format = new SimpleDateFormat("YYYYMMdd");
            for (BodyDataEntity reportCard : dateStr) {
            //用人单位数据格式校验
            //creditCode统一社会信用代码
            if (!StringUtils.isEmpty(reportCard.getCreditCode())) {
                boolean formatCreditCodeFlag = CommonUtils.isCNChar(reportCard.getCreditCode());
                boolean formatCreditCodeLenFlag = CommonUtils.isMaxLength(reportCard.getCreditCode(), 18);
                if (!(formatCreditCodeFlag && formatCreditCodeLenFlag)) {
                    Element errorData = errorEmployingUnits.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element employingUnit = errorData.addElement("employingUnit");
                    employingUnit.setText(reportCard.getEmployerCode());
                    errorMessage.setText("统一社会信用代码格式不正确!");
                    flag = false;
                }
            }

            //employerCode用人单位编码
            if (!StringUtils.isEmpty(reportCard.getEmployerCode())) {
                boolean formatEmployerCode = CommonUtils.isOrgCode(reportCard.getEmployerCode());
                if (!formatEmployerCode ) {
                    Element errorData = errorEmployingUnits.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element employingUnit = errorData.addElement("employingUnit");
                    employingUnit.setText(reportCard.getEmployerCode());
                    errorMessage.setText("用人单位编码格式不正确!");
                    flag = false;
                }
            }

            //employerDesc单位描述信息
            if (!StringUtils.isEmpty(reportCard.getEmployerDesc())) {
                boolean formaEmployerDescFlag = CommonUtils.isCNChar(reportCard.getEmployerDesc());
                boolean formatEmployerDescLenFlag = CommonUtils.isMaxLength(reportCard.getEmployerDesc(), 255);
                if (!(formaEmployerDescFlag && formatEmployerDescLenFlag)) {
                    Element errorData = errorEmployingUnits.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element employingUnit = errorData.addElement("employingUnit");
                    employingUnit.setText(reportCard.getEmployerCode());
                    errorMessage.setText("单位描述信息格式不正确!");
                    flag = false;
                }
            }
            //areaStandard所属地区国标
            if (!StringUtils.isEmpty(reportCard.getAreaStandard())) {
                boolean formaAreaStandardFlag = CommonUtils.isDigist(reportCard.getAreaStandard());
                boolean formatAreaStandardLenFlag = CommonUtils.isLength(reportCard.getAreaStandard(), 8);
                if (!(formaAreaStandardFlag && formatAreaStandardLenFlag)) {
                    Element errorData = errorEmployingUnits.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element employingUnit = errorData.addElement("employingUnit");
                    employingUnit.setText(reportCard.getEmployerCode());
                    errorMessage.setText("所属地区国标格式不正确!");
                    flag = false;
                }
            }
            //areaAddress所属地区详细地址
            if (!StringUtils.isEmpty(reportCard.getAreaAddress())) {
                boolean formaAreaAddressFlag = CommonUtils.isCNChar(reportCard.getAreaAddress());
                boolean formatAreaAddressLenFlag = CommonUtils.isMaxLength(reportCard.getAreaAddress(), 64);
                if (!(formaAreaAddressFlag && formatAreaAddressLenFlag)) {
                    Element errorData = errorEmployingUnits.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element employingUnit = errorData.addElement("employingUnit");
                    employingUnit.setText(reportCard.getEmployerCode());
                    errorMessage.setText("所属地区详细地址格式不正确!");
                    flag = false;
                }
            }
            //economicCode经济类型编码
            if (!StringUtils.isEmpty(reportCard.getEconomicCode())) {
                boolean formaAreaAddressFlag = CommonUtils.isLetterDigit(reportCard.getEconomicCode());
                boolean formatAreaAddressLenFlag = CommonUtils.isMaxLength(reportCard.getEconomicCode(), 4);
                if (!(formaAreaAddressFlag && formatAreaAddressLenFlag)) {
                    Element errorData = errorEmployingUnits.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element employingUnit = errorData.addElement("employingUnit");
                    employingUnit.setText(reportCard.getEmployerCode());
                    errorMessage.setText("经济类型编码格式不正确!");
                    flag = false;
                }
            }
            //industryCateCode行业编码
            if (!StringUtils.isEmpty(reportCard.getIndustryCateCode())) {
                boolean formaIndustryCateCodeFlag = CommonUtils.isDigist(reportCard.getIndustryCateCode());
                boolean formatIndustryCateCodeLenFlag = CommonUtils.isMaxLength(reportCard.getIndustryCateCode(), 4);
                if (!(formaIndustryCateCodeFlag && formatIndustryCateCodeLenFlag)) {
                    Element errorData = errorEmployingUnits.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element employingUnit = errorData.addElement("employingUnit");
                    employingUnit.setText(reportCard.getEmployerCode());
                    errorMessage.setText("行业编码格式不正确!");
                    flag = false;
                }
            }
            //enterpriseCode企业规模编码
            if (!StringUtils.isEmpty(reportCard.getEnterpriseCode())) {
                boolean formaEnterpriseCodeFlag = CommonUtils.isDigist(reportCard.getEnterpriseCode());
                boolean formatEnterpriseCodeLenFlag = CommonUtils.isLength(reportCard.getEnterpriseCode(), 3);
                if (!(formaEnterpriseCodeFlag && formatEnterpriseCodeLenFlag)) {
                    Element errorData = errorEmployingUnits.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element employingUnit = errorData.addElement("employingUnit");
                    employingUnit.setText(reportCard.getEmployerCode());
                    errorMessage.setText("企业规模编码格式不正确!");
                    flag = false;
                }
            }
            //secondEmployerCode二级单位编码
            if (!StringUtils.isEmpty(reportCard.getSecondEmployerCode())) {
                boolean formaEnterpriseCodeFlag = CommonUtils.isDigist(reportCard.getSecondEmployerCode());
                boolean formatEnterpriseCodeLenFlag = CommonUtils.isMaxLength(reportCard.getSecondEmployerCode(), 16);
                if (!(formaEnterpriseCodeFlag && formatEnterpriseCodeLenFlag)) {
                    Element errorData = errorEmployingUnits.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element employingUnit = errorData.addElement("employingUnit");
                    employingUnit.setText(reportCard.getEmployerCode());
                    errorMessage.setText("二级单位编码格式不正确!");
                    flag = false;
                }
            }

            //secondEmployerName二级单位编码
            if (!StringUtils.isEmpty(reportCard.getSecondEmployerName())) {
                boolean formaSecondEmployerNameFlag = CommonUtils.isCNChar(reportCard.getSecondEmployerName());
                boolean formatSecondEmployerNameLenFlag = CommonUtils.isMaxLength(reportCard.getSecondEmployerName(), 32);
                if (!(formaSecondEmployerNameFlag && formatSecondEmployerNameLenFlag)) {
                    Element errorData = errorEmployingUnits.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element employingUnit = errorData.addElement("employingUnit");
                    employingUnit.setText(reportCard.getEmployerCode());
                    errorMessage.setText("二级单位编码格式不正确!");
                    flag = false;
                }
            }
            //postAddress通讯地址
            if (!StringUtils.isEmpty(reportCard.getPostAddress())) {
                boolean formaPostAddressFlag = CommonUtils.isCNChar(reportCard.getPostAddress());
                boolean formatPostAddressLenFlag = CommonUtils.isMaxLength(reportCard.getPostAddress(), 64);
                if (!(formaPostAddressFlag && formatPostAddressLenFlag)) {
                    Element errorData = errorEmployingUnits.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element employingUnit = errorData.addElement("employingUnit");
                    employingUnit.setText(reportCard.getEmployerCode());
                    errorMessage.setText("通讯地址格式不正确!");
                    flag = false;
                }
            }

            //zipCode邮编
            if (!StringUtils.isEmpty(reportCard.getZipCode())) {
                boolean formaZipCodeFlag = CommonUtils.isDigist(reportCard.getZipCode());
                boolean formatZipCodeLenFlag = CommonUtils.isLength(reportCard.getZipCode(), 6);
                if (!(formaZipCodeFlag && formatZipCodeLenFlag)) {
                    Element errorData = errorEmployingUnits.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element employingUnit = errorData.addElement("employingUnit");
                    employingUnit.setText(reportCard.getEmployerCode());
                    errorMessage.setText("邮编格式不正确!");
                    flag = false;
                }
            }

            //contactPerson联系人
            if (!StringUtils.isEmpty(reportCard.getContactPerson())) {
                boolean formaContactPersonFlag = CommonUtils.isCNChar(reportCard.getContactPerson());
                boolean formatContactPersonLenFlag = CommonUtils.isMaxLength(reportCard.getContactPerson(), 32);
                if (!(formaContactPersonFlag && formatContactPersonLenFlag)) {
                    Element errorData = errorEmployingUnits.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element employingUnit = errorData.addElement("employingUnit");
                    employingUnit.setText(reportCard.getEmployerCode());
                    errorMessage.setText("联系人格式不正确!");
                    flag = false;
                }
            }
            //contactPerson联系电话
            if (!StringUtils.isEmpty(reportCard.getContactPhone())) {
                boolean formaContactPhoneFlag = CommonUtils.isDigist(reportCard.getContactPhone());
                boolean formatContactPhoneLenFlag = CommonUtils.isMaxLength(reportCard.getContactPhone(), 18);
                if (!(formaContactPhoneFlag && formatContactPhoneLenFlag)) {
                    Element errorData = errorEmployingUnits.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element employingUnit = errorData.addElement("employingUnit");
                    employingUnit.setText(reportCard.getEmployerCode());
                    errorMessage.setText("联系电话格式不正确!");
                    flag = false;
                }
            }
            //monitorOrgCode监测单位编码
            if (!StringUtils.isEmpty(reportCard.getMonitorOrgCode())) {
                boolean formaMonitorOrgCodeFlag = CommonUtils.isLetterDigit(reportCard.getMonitorOrgCode());
                boolean formatMonitorOrgCodeLenFlag = CommonUtils.isMaxLength(reportCard.getMonitorOrgCode(), 9);
                if (!(formaMonitorOrgCodeFlag && formatMonitorOrgCodeLenFlag)) {
                    Element errorData = errorEmployingUnits.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element employingUnit = errorData.addElement("employingUnit");
                    employingUnit.setText(reportCard.getEmployerCode());
                    errorMessage.setText("监测单位编码格式不正确!");
                    flag = false;
                }
            }
            //monitorOrgName监测单位名称
            if (!StringUtils.isEmpty(reportCard.getMonitorOrgName())) {
                boolean formaMonitorOrgNameFlag = CommonUtils.isCNChar(reportCard.getMonitorOrgName());
                boolean formatMonitorOrgNameLenFlag = CommonUtils.isMaxLength(reportCard.getMonitorOrgName(), 64);
                if (!(formaMonitorOrgNameFlag && formatMonitorOrgNameLenFlag)) {
                    Element errorData = errorEmployingUnits.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element employingUnit = errorData.addElement("employingUnit");
                    employingUnit.setText(reportCard.getEmployerCode());
                    errorMessage.setText("监测单位名称格式不正确!");
                    flag = false;
                }
            }
            //remarks备注
            if (!StringUtils.isEmpty(reportCard.getRemarks())) {
                boolean formaRemarksFlag = CommonUtils.isCNChar(reportCard.getRemarks());
                boolean formatRemarksLenFlag = CommonUtils.isMaxLength(reportCard.getRemarks(), 255);
                if (!(formaRemarksFlag && formatRemarksLenFlag)) {
                    Element errorData = errorEmployingUnits.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element employingUnit = errorData.addElement("employingUnit");
                    employingUnit.setText(reportCard.getEmployerCode());
                    errorMessage.setText("备注格式不正确!");
                    flag = false;
                }
            }
        }
        if(flag==true){
            dataInfo.remove(errorDatas);
        }
        return flag;
    }
    //数据唯一性校验 暂时先不用校验
    private Boolean isOnlyData(HashMap<String,String> hashMap){
        ZybGak zybGak = zybGakServiceImpl.selectByCodeAndHosId(hashMap);
        if(zybGak==null){
            return true;
        }
        return false;
    }
    //用人单位code和名称至少一项必填
    private Boolean checkEmpStringIsNull(String orgCode,String employerName){
        if(StringUtils.isEmpty(orgCode) && StringUtils.isEmpty(employerName)){
            return true;
        }
        return false;
    }
}