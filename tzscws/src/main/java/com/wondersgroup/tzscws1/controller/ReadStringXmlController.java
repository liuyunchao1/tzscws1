package com.wondersgroup.tzscws1.controller;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.wondersgroup.tzscws1.entity.BodyDataEntity;
import com.wondersgroup.tzscws1.entity.HeaderDataEntty;
import com.wondersgroup.tzscws1.model.CodeInfo;
import com.wondersgroup.tzscws1.model.ZybGak;
import com.wondersgroup.tzscws1.model.ZybYrdw;
import com.wondersgroup.tzscws1.service.CodeInfoService;
import com.wondersgroup.tzscws1.service.ZybGakService;
import com.wondersgroup.tzscws1.service.ZybGakServiceImpl;
import com.wondersgroup.tzscws1.service.ZybYrdwService;
import com.wondersgroup.tzscws1.verification.Verification;
import jdk.internal.dynalink.beans.StaticClass;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;


@Controller
public class ReadStringXmlController {
    /**
     * 测试解析xml字符串
     *
     * @param args
     */
    @Autowired
    private ZybGakService zybGakService;
    @Autowired
    private ZybYrdwService zybYrdwService;
    @Autowired
    private CodeInfoService codeInfoService;
    private String errorCode;
    public static final String SUCCED = "suceed";

    public static void main(String[] args) {

        Document retDoc = DocumentHelper.createDocument();
        retDoc.setXMLEncoding("UTF-8");
        Element dataInfo = retDoc.addElement("data");
        Element returnCode = dataInfo.addElement("returnCode");
        Element message = dataInfo.addElement("message");
        ReadStringXmlController readxml = new ReadStringXmlController();
        String xml = "<data>" +
                "<header>" +
                "<eventId>业务请求类型编码</eventId>" +
                "<hosId>医院编码</hosId>" +
                "<requestTime>请求时间</requestTime>" +
                "<headSign>用户秘钥</headSign>" +
                "<bodySign>数据签名</bodySign>" +
                "</header>" +
                "<body>" +
                "<reportCards>" +
                "<reportCard>" +
                "<code>2</code>" +
                "<hosId>331003001</hosId>" +
                "<name>gfdgfg</name>" +
                "<idcard>43243253</idcard>" +
                "<bodyCheckType>1</bodyCheckType>" +
                "<sexCode>2</sexCode>" +
                "<birthday>199101-12</birthday>" +
                "<hazardCode>4324354</hazardCode>" +
                "<hazardYear>1991</hazardYear>" +
                "<hazardMonth>12</hazardMonth>" +
                "<sysPressResult>dweq3</sysPressResult>" +
                "<diasPressResult>3r353df</diasPressResult>" +
                "<ECGCode>19912</ECGCode>" +
                "<conclusionsCode>1990112</conclusionsCode>" +
                "</reportCard>" +
                "<reportCard>" +
                "<code>788</code>" +
                "<hosId>22434</hosId>" +
                "<name>gfdgfg</name>" +
                "<idcard>43243253</idcard>" +
                "<bodyCheckType>1</bodyCheckType>" +
                "<sexCode>2</sexCode>" +
                "<birthday>199101-12</birthday>" +
                "<hazardCode>4324354</hazardCode>" +
                "<hazardYear>1991</hazardYear>" +
                "<hazardMonth>12</hazardMonth>" +
                "<sysPressResult>dweq3</sysPressResult>" +
                "<diasPressResult>3r353df</diasPressResult>" +
                "<ECGCode>19912</ECGCode>" +
                "<conclusionsCode>1990112</conclusionsCode>" +
                "</reportCard>" +
                "</reportCards>" +
                "</body>" +
                "</data>";
        //解析xml头部
        String bodyDatas = readxml.readStringXml(xml);

    }

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
            // 下面的是通过解析xml字符串的
            doc = DocumentHelper.parseText(xml); // 将字符串转为XML
            Element rootElt = doc.getRootElement(); // 获取根节点
            System.out.println("根节点：" + rootElt.getName()); // 拿到根节点的名称
            Iterator iter = rootElt.elementIterator("header"); // 获取根节点下的子节点header

            // 遍历header节点
            while (iter.hasNext()) {
                Element recordEle = (Element) iter.next();
                String nodeName = recordEle.getName();
                System.out.println(nodeName);
//                String title = recordEle.elementTextTrim("title"); // 拿到head节点下的子节点title值
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


    public String readStringXml(String xml) {
        //返回xml格式字符串
        System.out.println(xml);
        Document retDoc = DocumentHelper.createDocument();
        retDoc.setXMLEncoding("UTF-8");
        Element dataInfo = retDoc.addElement("data");
        Element returnCode = dataInfo.addElement("returnCode");
        Element message = dataInfo.addElement("message");
        HeaderDataEntty headerDatas = readStringHeader(xml);
        boolean flag = false;
        if (headerDatas != null) {
            Verification verification = new Verification();
            flag = verification.verHeadSign(headerDatas);
            System.out.println(flag);
        }
        if (flag) {
            returnCode.setText("201");
            message.setText("秘钥校验错误!");
            System.out.println("秘钥校验错误!");
            return retDoc.toString();
        }
        Document doc = null;

        try {

            // 读取并解析XML文档
            // SAXReader就是一个管道，用一个流的方式，把xml文件读出来
            //
            // SAXReader reader = new SAXReader(); //User.hbm.xml表示你要解析的xml文档
            // Document document = reader.read(new File("User.hbm.xml"));
            // 下面的是通过解析xml字符串的
            doc = DocumentHelper.parseText(xml); // 将字符串转为XML
            Element rootElt = doc.getRootElement(); // 获取根节点
            System.out.println("根节点：" + rootElt.getName()); // 拿到根节点的名称
//          Iterator iterss = rootElt.elementIterator("Elt"); ///获取根节点下的子节点body
            Element bodyElt = rootElt.element("body");
            System.out.println("bodyElt：" + bodyElt.getName()); // 拿到根节点的名称
            Element reportCardsElt = bodyElt.element("reportCards");
            Iterator iterss = reportCardsElt.elementIterator("reportCard");
            // 遍历reportCard节点
            String codeResult = "";
            String hosIdResult = "";
            String nameResult = "";
            String idcardResult = "";
            String bodyCheckTypeResult = "";
            String sexCodeResult = "";
            String birthdayResult = "";
            String hazardCodeResult = "";//多种结果之间使用英文逗号（,）分隔
            String hazardYearResult = "";
            String hazardMonthResult = "";
            String sysPressResult = "";
            String diasPressResult = "";
            String ECGCodeResult = "";//多种结果之间使用英文逗号（,）分隔
            String conclusionsCodeResult="";
            String orgCode = "";
            String employerName = "";
            List<BodyDataEntity> emtList = new ArrayList<BodyDataEntity>();
            List<ZybGak> errorList = new ArrayList<ZybGak>();
            while (iterss.hasNext()) {
                Element recordEless = (Element) iterss.next();
                //字段空值校验
                BodyDataEntity bodyDataEntity = new BodyDataEntity();
                codeResult = recordEless.elementTextTrim("code");
                System.out.println("codeResult:"+codeResult);
                hosIdResult = recordEless.elementTextTrim("hosId");
                nameResult = recordEless.elementTextTrim("name");
                idcardResult = recordEless.elementTextTrim("idcard");
                bodyCheckTypeResult = recordEless.elementTextTrim("bodyCheckType");
                sexCodeResult = recordEless.elementTextTrim("sexCode");
                birthdayResult = recordEless.elementTextTrim("birthday");
                hazardCodeResult = recordEless.elementTextTrim("hazardCode");//多种结果之间使用英文逗号（,）分隔
                hazardYearResult = recordEless.elementTextTrim("hazardYear");
                hazardMonthResult = recordEless.elementTextTrim("hazardMonth");
                sysPressResult = recordEless.elementTextTrim("sysPressResult");
                diasPressResult = recordEless.elementTextTrim("diasPressResult");
                ECGCodeResult = recordEless.elementTextTrim("ECGCode");//多种结果之间使用英文逗号（,）分隔
                conclusionsCodeResult = recordEless.elementTextTrim("conclusionsCode");
                boolean empFlag = checkStringIsNull( codeResult,hosIdResult, nameResult, idcardResult, bodyCheckTypeResult, sexCodeResult, birthdayResult, hazardCodeResult, hazardYearResult, hazardMonthResult, sysPressResult, diasPressResult, ECGCodeResult, conclusionsCodeResult);
                System.out.println("必填请求参数有空值:" + empFlag);
                if (empFlag) {
                    returnCode.setText("105");
                    message.setText("必填请求参数有空值!");
                    System.out.println("必填请求参数有空值!");
                    System.out.println("必填请求参数有空值:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                orgCode = recordEless.elementTextTrim("orgCode");
                employerName = recordEless.elementTextTrim("employerName");
                //用人单位code和名称至少一项必填
                boolean employeeFlag = checkEmpStringIsNull(orgCode,employerName);
                if(employeeFlag){
                    returnCode.setText("105");
                    message.setText("用人单位编码与用人单位名称至少有一个不为空!");
                    System.out.println("用人单位编码与用人单位名称至少有一个不为空!");
                    System.out.println("用人单位编码与用人单位名称至少有一个不为空！:" + retDoc.asXML());
                    return retDoc.asXML();
                }
                if(StringUtils.isEmpty(orgCode)){
                    //如果orgCode为空则查询employerName，否则查询orgCode
                    List<ZybYrdw> zybYrdwList = zybYrdwService.selectByEmployerName(employerName);
                    if(null==zybYrdwList || zybYrdwList.size()==0){
                        returnCode.setText("105");
                        message.setText("用人单位数据不存在!");
                        System.out.println("用人单位数据不存在!");
                        System.out.println("用人单位数据不存在！:" + retDoc.asXML());
                        return retDoc.asXML();
                    }
                }else{
                    ZybYrdw zybYrdw = zybYrdwService.selectByPrimaryKey(orgCode);
                    if(null==zybYrdw){
                        returnCode.setText("105");
                        message.setText("用人单位数据不存在!");
                        System.out.println("用人单位数据不存在!");
                        System.out.println("用人单位数据不存在！:" + retDoc.asXML());
                        return retDoc.asXML();
                    }
                }
                bodyDataEntity.setCode(codeResult);
                bodyDataEntity.setBirthday(birthdayResult);
                bodyDataEntity.setHosId(hosIdResult);
                bodyDataEntity.setName(nameResult);
                bodyDataEntity.setIdcard(idcardResult);
                bodyDataEntity.setBodyCheckType(bodyCheckTypeResult);
                bodyDataEntity.setSexCode(sexCodeResult);
                bodyDataEntity.setHazardCode(hazardCodeResult);
                bodyDataEntity.setHazardYear(hazardYearResult);
                bodyDataEntity.setHazardMonth(hazardMonthResult);
                bodyDataEntity.setSysPressResult(sysPressResult);
                bodyDataEntity.setDiasPressResult(diasPressResult);
                bodyDataEntity.setECGCode(ECGCodeResult);
                bodyDataEntity.setConclusionsCode(conclusionsCodeResult);
                emtList.add(bodyDataEntity);
            }
            //格式校验
            boolean formatFlag = isDateFormate(emtList, dataInfo);
            if (!formatFlag) {
                System.out.println("数据格式或code不唯一有误:" + retDoc.asXML());
                return retDoc.asXML();
            }


        } catch (DocumentException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();

        }
        return SUCCED;
    }

    //参数判空
    public boolean checkStringIsNull(String... value) {
        int count = 0;
        System.out.println("value.length:" +value.length);
        for (int i = 0; i <  value.length; i++) {
            //遍历字符数组所有的参数，发现某个为 null 或者 "" ,则跳出
            System.out.println("value[i]:" +value[0]);
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

    //参数格式判断
    private Boolean isDateFormate(List<BodyDataEntity> dateStr, Element dataInfo) {

        boolean flag = true;
        for (BodyDataEntity reportCard : dateStr) {
            //参数日期格式判断
            SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
            dataInfo.element("returnCode").setText("-1");
            dataInfo.element("message").setText("部分数据格式有误!");
            Element errorDatas = dataInfo.addElement("errorDatas");
            Element errorReportCards = errorDatas.addElement("errorReportCards");
            try {
                Date date = format.parse(reportCard.getBirthday());
            } catch (ParseException e) {
                Element errorData = errorReportCards.addElement("errorData");
                Element errorMessage = errorData.addElement("errorMessage");
                Element reportCardId = errorData.addElement("reportCard");
                reportCardId.setText(reportCard.getCode());
                errorMessage.setText("出生日期格式有误,格式应为YYYY-MM-DD.");
                flag = false;
            }
            //判断code是否唯一
            HashMap<String,String> hashMap = new HashMap<String,String>();
            hashMap.put("code",reportCard.getCode());
            hashMap.put("hosId",reportCard.getHosId());
            System.out.println("code:" +reportCard.getCode());
            System.out.println("hosId:" +reportCard.getHosId());
            boolean isOnlyDataFlag = isOnlyData(hashMap);
            if(!isOnlyDataFlag){
                Element errorData = errorReportCards.addElement("errorData");
                Element errorMessage = errorData.addElement("errorMessage");
                Element reportCardId = errorData.addElement("reportCard");
                reportCardId.setText(reportCard.getCode());
                errorMessage.setText("报告编号在该医院已经存在!");
                flag = false;
            }
            //判断code格式
            boolean formatCodeFlag= isLetterDigit(reportCard.getCode());
            boolean formatCodeLenFlag= isMaxLength(reportCard.getCode(),16);
            if(!(formatCodeFlag && formatCodeLenFlag)){
                Element errorData = errorReportCards.addElement("errorData");
                Element errorMessage = errorData.addElement("errorMessage");
                Element reportCardId = errorData.addElement("reportCard");
                reportCardId.setText(reportCard.getCode());
                errorMessage.setText("报告编号格式不正确!");
                flag = false;
            }
            //判断医院hosId格式
            Boolean digitLenFlag = isDigist(reportCard.getHosId());
            Boolean isLength = isLength(reportCard.getHosId(),9);
            if(!(digitLenFlag && isLength)){
                Element errorData = errorReportCards.addElement("errorData");
                Element errorMessage = errorData.addElement("errorMessage");
                Element reportCardId = errorData.addElement("reportCard");
                reportCardId.setText(reportCard.getCode());
                errorMessage.setText("医院编号格式不正确!");
                flag = false;
            }
            //判断orgCode格式
            if(!StringUtils.isEmpty(reportCard.getOrgCode())){
                boolean formatOrgCodeFlag = isLetterDigit(reportCard.getOrgCode());
                boolean formatOrgCodeLenFlag = isMaxLength(reportCard.getOrgCode(),32);
                if(!(formatOrgCodeFlag&&formatOrgCodeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("用人单位编码格式不正确!");
                    flag = false;
                }
            }
            //判断employerName格式
            if(!StringUtils.isEmpty(reportCard.getEmployerName())){
                boolean formatEmployerNameFlag = isCNChar(reportCard.getEmployerName());
                boolean formatEmployerNameLenFlag = isMaxLength(reportCard.getEmployerName(),64);
                if(!(formatEmployerNameFlag&&formatEmployerNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("用人单位名称格式不正确!");
                    flag = false;

                }
            }
            //判断name格式
            boolean formatNameFlag = isCNChar(reportCard.getName());
            boolean formatNameLenFlag = isMaxLength(reportCard.getEmployerName(),16);
            if(!(formatNameFlag&&formatNameLenFlag)){
                Element errorData = errorReportCards.addElement("errorData");
                Element errorMessage = errorData.addElement("errorMessage");
                Element reportCardId = errorData.addElement("reportCard");
                reportCardId.setText(reportCard.getCode());
                errorMessage.setText("体检人员姓名格式不正确!");
                flag = false;
            }
            //判断idcard格式
            boolean formatIdcardFlag = isLetterDigit(reportCard.getIdcard());
            boolean formatIdcardLenFlag = isLength(reportCard.getIdcard(),15);
            if(!(formatIdcardFlag&&formatIdcardLenFlag)){
                Element errorData = errorReportCards.addElement("errorData");
                Element errorMessage = errorData.addElement("errorMessage");
                Element reportCardId = errorData.addElement("reportCard");
                reportCardId.setText(reportCard.getCode());
                errorMessage.setText("身份证号码格式不正确!");
                flag = false;
            }
            //判断telPhone格式
            if(StringUtils.isEmpty(reportCard.getTelPhone())){
                boolean formatTelPhoneFlag = isDigist(reportCard.getTelPhone());
                boolean formatTelPhoneLenFlag =  isLength(reportCard.getTelPhone(),15);
                if(!(formatTelPhoneFlag&&formatTelPhoneLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("个人联系电话格式不正确!");
                    flag = false;
                }
            }
            //判断bodyCheckType格式
            boolean formatBodyCheckTypeFlag = isDigist(reportCard.getBodyCheckType());
            boolean formatBodyCheckTypeLenFlag = isLength(reportCard.getBodyCheckType(),1);
            List<CodeInfo>  CodeInfoList = codeInfoService.selectByCodeInfoId(new BigDecimal(901));
            boolean bodyCheckInflag=false;
            if(!(formatBodyCheckTypeFlag&&formatBodyCheckTypeLenFlag)){
                Element errorData = errorReportCards.addElement("errorData");
                Element errorMessage = errorData.addElement("errorMessage");
                Element reportCardId = errorData.addElement("reportCard");
                reportCardId.setText(reportCard.getCode());
                errorMessage.setText("体检类型编码格式不正确!");
                flag = false;
            }
            //判断bodyCheckType是否在字典值域内
            for(CodeInfo codeInfo : CodeInfoList){
                if(reportCard.getBodyCheckType().equals(codeInfo.getCode())){
                    bodyCheckInflag=true;
                    break;
                }
            }
            if(!bodyCheckInflag){
                Element errorData = errorReportCards.addElement("errorData");
                Element errorMessage = errorData.addElement("errorMessage");
                Element reportCardId = errorData.addElement("reportCard");
                reportCardId.setText(reportCard.getCode());
                errorMessage.setText("体检类型编码值不正确!");
                flag = false;
            }
            //性别编码sexCode格式判断
            boolean formatSexCodeFlag = isDigist(reportCard.getSexCode());
            boolean formatSexCodeLenFlag = isLength(reportCard.getSexCode(),1);
            List<CodeInfo>  sexCodeList = codeInfoService.selectByCodeInfoId(new BigDecimal(124));
            boolean sexCodeflag=false;
            if(!(formatSexCodeFlag&&formatSexCodeLenFlag)){
                Element errorData = errorReportCards.addElement("errorData");
                Element errorMessage = errorData.addElement("errorMessage");
                Element reportCardId = errorData.addElement("reportCard");
                reportCardId.setText(reportCard.getCode());
                errorMessage.setText("性别编码格式不正确!");
                flag = false;
            }
            for(CodeInfo codeInfo : sexCodeList){
                if(reportCard.getSexCode().equals(codeInfo.getCode())){
                    bodyCheckInflag=true;
                    break;
                }
            }
            if(!sexCodeflag){
                Element errorData = errorReportCards.addElement("errorData");
                Element errorMessage = errorData.addElement("errorMessage");
                Element reportCardId = errorData.addElement("reportCard");
                reportCardId.setText(reportCard.getCode());
                errorMessage.setText("性别编码值不正确!");
                flag = false;
            }
            //判断总工龄年seniorityYear格式
            if(!StringUtils.isEmpty(reportCard.getSeniorityYear())){
                boolean formatSeniorityYearFlag = isDigist(reportCard.getSeniorityYear());
                boolean formatSeniorityYearLenFlag = isLength(reportCard.getSeniorityYear(),2);
                if(!(formatSeniorityYearFlag&&formatSeniorityYearLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("总工龄年格式不正确!");
                    flag = false;
                }
            }
            //判断总工龄月seniorityMonth格式
            if(!StringUtils.isEmpty(reportCard.getSeniorityMonth())){
                boolean formatSeniorityMonthFlag = isDigist(reportCard.getSeniorityMonth());
                boolean formatSeniorityMonthLenFlag = isLength(reportCard.getSeniorityMonth(),2);
                if(!(formatSeniorityMonthFlag&&formatSeniorityMonthLenFlag)){
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
            if(hazardCode.indexOf(",")==-1){
                boolean formatHazardCodeFlag = isLetterDigit(hazardCode);
                boolean formatHazardCodeLenFlag = isMaxLength(hazardCode,32);
                if(!(formatHazardCodeFlag&&formatHazardCodeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("接触监测的主要职业病危害因素编码格式不正确!");
                    flag = false;
                }

            }else{
                String[] hazardCodeList = hazardCode.split(",");
                boolean formatHazardCodeLenFlag = isMaxLength(hazardCode,32);
                for(int i=0;i<hazardCodeList.length;i++){
                    boolean formatHazardCodeFlag = isLetterDigit(hazardCodeList[i]);
                    if(!(formatHazardCodeFlag&&formatHazardCodeLenFlag)){
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("接触监测的主要职业病危害因素编码格式不正确!");
                        flag = false;
                        break;
                    }
                }
            }
            //判断hazardCodeList是否在字典值域内
            List<CodeInfo>  hazardCodeList = codeInfoService.selectByCodeInfoId(new BigDecimal(900));
            boolean hazardCodeInflag = false;
            for(CodeInfo codeInfo : hazardCodeList){
                if(reportCard.getHazardCode().equals(codeInfo.getCode())){
                    hazardCodeInflag=true;
                    break;
                }
            }
            if(!hazardCodeInflag){
                Element errorData = errorReportCards.addElement("errorData");
                Element errorMessage = errorData.addElement("errorMessage");
                Element reportCardId = errorData.addElement("reportCard");
                reportCardId.setText(reportCard.getCode());
                errorMessage.setText("接触监测的主要职业病危害因素编码值不正确!");
                flag = false;
            }
            //判断职业危害接触工龄年exposureYear格式
            if(!StringUtils.isEmpty(reportCard.getExposureYear())){
                boolean formatExposureYearFlag = isDigist(reportCard.getExposureYear());
                boolean formatExposureYearLenFlag = isMaxLength(reportCard.getExposureYear(),2);
                if(!(formatExposureYearFlag&&formatExposureYearLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("职业危害接触工龄年格式不正确!");
                    flag = false;
                }
            }
            //判断职业危害接触工龄月exposureMonth格式
            if(!StringUtils.isEmpty(reportCard.getExposureMonth())){
                boolean formatExposureMonthFlag = isDigist(reportCard.getExposureMonth());
                boolean formatExposureMonthLenFlag = isMaxLength(reportCard.getExposureMonth(),2);
                if(!(formatExposureMonthFlag&&formatExposureMonthLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("职业危害接触工龄月格式不正确!");
                    flag = false;
                }
            }
            //判断接触所监测危害因素工龄年hazardYear格式
            boolean formatHazardYearFlag = isDigist(reportCard.getHazardYear());
            boolean formatHazardYearLenFlag = isMaxLength(reportCard.getHazardYear(),2);
            if(!(formatHazardYearFlag&&formatHazardYearLenFlag)){
                Element errorData = errorReportCards.addElement("errorData");
                Element errorMessage = errorData.addElement("errorMessage");
                Element reportCardId = errorData.addElement("reportCard");
                reportCardId.setText(reportCard.getCode());
                errorMessage.setText("接触所监测危害因素工龄年格式不正确!");
                flag = false;
            }
            //判断hazardMonth接触所监测危害因素工龄月格式
            boolean formatHazardMonthFlag = isDigist(reportCard.getHazardMonth());
            boolean formatHazardMonthLenFlag = isMaxLength(reportCard.getHazardMonth(),2);
            if(!(formatHazardMonthFlag&&formatHazardMonthLenFlag)){
                Element errorData = errorReportCards.addElement("errorData");
                Element errorMessage = errorData.addElement("errorMessage");
                Element reportCardId = errorData.addElement("reportCard");
                reportCardId.setText(reportCard.getCode());
                errorMessage.setText("接触所监测危害因素工龄月格式不正确!");
                flag = false;
            }
            //判断workShop工作车间名称格式
            if(!StringUtils.isEmpty(reportCard.getWorkShop())){
                boolean formatWorkShopFlag = isCNChar(reportCard.getWorkShop());
                boolean formatWorkShopLenFlag = isMaxLength(reportCard.getWorkShop(),10);
                if(!(formatWorkShopFlag&&formatWorkShopLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("工作车间名称格式不正确!");
                    flag = false;
                }
            }
            //判断jobCode工种编码格式
            if(!StringUtils.isEmpty(reportCard.getJobCode())){
                boolean formatJobCodeFlag = isLetterDigit(reportCard.getJobCode());
                boolean formatJobCodeLenFlag = isLength(reportCard.getJobCode(),6);
                if(!(formatJobCodeFlag&&formatJobCodeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("工种编码格式不正确!");
                    flag = false;
                }
            }
            //判断sysPressResult血压收缩压结果格式
            boolean formatSysPressResultFlag = isDouNot(reportCard.getSysPressResult());
            boolean formatSysPressResultLenFlag = isLength(reportCard.getSysPressResult(),6);
            if(!(formatSysPressResultFlag&&formatSysPressResultLenFlag)){
                Element errorData = errorReportCards.addElement("errorData");
                Element errorMessage = errorData.addElement("errorMessage");
                Element reportCardId = errorData.addElement("reportCard");
                reportCardId.setText(reportCard.getCode());
                errorMessage.setText("血压收缩压结果格式不正确!");
                flag = false;
            }
            //判断sysPressUnitName血压收缩压剂量单位名称
            if(!StringUtils.isEmpty(reportCard.getSysPressUnitName())){
                boolean formatSysPressUnitNameFlag = isCNChar(reportCard.getSysPressUnitName());
                boolean formatSysPressUnitNameLenFlag = isMaxLength(reportCard.getSysPressUnitName(),8);
                if(!(formatSysPressUnitNameFlag&&formatSysPressUnitNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血压收缩压剂量单位名称格式不正确!");
                    flag = false;
                }
            }
            //判断diasPressResult血压舒张压结果格式
            boolean formatDiasPressResultFlag = isDouNot(reportCard.getDiasPressResult());
            boolean formatDiasPressResultLenFlag = isLength(reportCard.getDiasPressResult(),6);
            if(!(formatDiasPressResultFlag&&formatDiasPressResultLenFlag)){
                Element errorData = errorReportCards.addElement("errorData");
                Element errorMessage = errorData.addElement("errorMessage");
                Element reportCardId = errorData.addElement("reportCard");
                reportCardId.setText(reportCard.getCode());
                errorMessage.setText("血压舒张压结果格式不正确!");
                flag = false;
            }
            //判断diasPressUnitName血压舒张压计量单位名称
            if(!StringUtils.isEmpty(reportCard.getDiasPressUnitName())){
                boolean formatSysPressUnitNameFlag = isCNChar(reportCard.getDiasPressUnitName());
                boolean formatSysPressUnitNameLenFlag = isMaxLength(reportCard.getDiasPressUnitName(),8);
                if(!(formatSysPressUnitNameFlag&&formatSysPressUnitNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血压舒张压计量单位名称格式不正确!");
                    flag = false;
                }

            }
            //判断WBCResult血常规白细胞计数（WBC）结果
            if(!(hazardCodeInflag && ("铅".equals(reportCard.getWBCResult())||"苯".equals(reportCard.getWBCResult())||"布鲁氏菌".equals(reportCard.getWBCResult())))){
                boolean formatWBCResultFlag = isCNChar(reportCard.getWBCResult());
                boolean formatWBCResultLenFlag = isMaxLength(reportCard.getWBCResult(),8);
                if(!(formatWBCResultFlag&&formatWBCResultLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血常规白细胞计数(WBC)结果格式不正确!");
                    flag = false;
                }
            }
            //判断WBCUnitName血常规白细胞计数（WBC）计量单位名称
            if(!StringUtils.isEmpty(reportCard.getWBCUnitName())){
                boolean formatWBCUnitNameFlag = isCNChar(reportCard.getWBCUnitName());
                boolean formatWBCUnitNameLenFlag = isMaxLength(reportCard.getWBCUnitName(),16);
                if(!(formatWBCUnitNameFlag&&formatWBCUnitNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血常规白细胞计数(WBC)计量单位名称格式不正确!");
                    flag = false;
                }

            }
            //判断WBCMiniRange血常规白细胞计数（WBC）参考范围最小值格式
            if(!StringUtils.isEmpty(reportCard.getWBCMiniRange())){
                boolean formatWBCMiniRangeFlag = isCNChar(reportCard.getWBCMiniRange());
                boolean formatWBCMiniRangeLenFlag = isMaxLength(reportCard.getWBCMiniRange(),16);
                if(!(formatWBCMiniRangeFlag&&formatWBCMiniRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血常规白细胞计数(WBC)参考范围最小值格式不正确!");
                    flag = false;
                }

            }
            //判断WBCMaxRange血常规白细胞计数（WBC）参考范围最大值格式
            if(!StringUtils.isEmpty(reportCard.getWBCMaxRange())){
                boolean formatWBCMaxRangeFlag = isCNChar(reportCard.getWBCMaxRange());
                boolean formatWBCMaxRangeLenFlag = isMaxLength(reportCard.getWBCMaxRange(),16);
                if(!(formatWBCMaxRangeFlag&&formatWBCMaxRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血常规白细胞计数（WBC）参考范围最大值格式不正确!");
                    flag = false;
                }

            }
            //判断RBCResult血常规红细胞计数（RBC）结果
            if(!(hazardCodeInflag && ("铅".equals(reportCard.getRBCResult())||"苯".equals(reportCard.getRBCResult())||"布鲁氏菌".equals(reportCard.getRBCResult())))){
                boolean formatRBCResultFlag = isCNChar(reportCard.getRBCResult());
                boolean formatRBCResultLenFlag = isMaxLength(reportCard.getRBCResult(),8);
                if(!(formatRBCResultFlag&&formatRBCResultLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血常规红细胞计数(RBC)结果格式不正确!");
                    flag = false;
                }
            }
            //判断RBCUnitName血常规红细胞计数（RBC）计量单位名称
            if(!StringUtils.isEmpty(reportCard.getRBCUnitName())){
                boolean formatRBCUnitNameFlag = isCNChar(reportCard.getRBCUnitName());
                boolean formatRBCUnitNameLenFlag = isMaxLength(reportCard.getRBCUnitName(),16);
                if(!(formatRBCUnitNameFlag&&formatRBCUnitNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血常规红细胞计数（RBC）计量单位名称格式不正确!");
                    flag = false;
                }

            }
            //判断RBCMiniRange血常规红细胞计数（RBC）参考范围最小值
            if(!StringUtils.isEmpty(reportCard.getRBCMiniRange())){
                boolean formatRBCMiniRangeFlag = isCNChar(reportCard.getRBCMiniRange());
                boolean formatRBCMiniRangeLenFlag = isMaxLength(reportCard.getRBCMiniRange(),16);
                if(!(formatRBCMiniRangeFlag&&formatRBCMiniRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血常规红细胞计数（RBC）参考范围最小值格式不正确!");
                    flag = false;
                }

            }
            //判断RBCMaxRange血常规红细胞计数（RBC）参考范围最大值
            if(!StringUtils.isEmpty(reportCard.getRBCMaxRange())){
                boolean formatRBCMaxRangeFlag = isCNChar(reportCard.getRBCMaxRange());
                boolean formatRBCMaxRangeLenFlag = isMaxLength(reportCard.getRBCMaxRange(),16);
                if(!(formatRBCMaxRangeFlag&&formatRBCMaxRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血常规红细胞计数（RBC）参考范围最大值格式不正确!");
                    flag = false;
                }

            }
            //判断HbResult血常规血红蛋白（Hb）结果格式
            if(!StringUtils.isEmpty(reportCard.getHbResult())){
                boolean formatHbResultFlag = isCNChar(reportCard.getHbResult());
                boolean formatHbResultLenFlag = isMaxLength(reportCard.getHbResult(),8);
                if(!(formatHbResultFlag&&formatHbResultLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血常规血红蛋白（Hb）结果格式格式不正确!");
                    flag = false;
                }

            }
            //判断HbUnitName血常规血红蛋白（Hb）计量单位名称格式
            if(!StringUtils.isEmpty(reportCard.getHbUnitName())){
                boolean formatHbUnitNameFlag = isCNChar(reportCard.getHbUnitName());
                boolean formatHbUnitNameLenFlag = isMaxLength(reportCard.getHbUnitName(),8);
                if(!(formatHbUnitNameFlag&&formatHbUnitNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血常规血红蛋白（Hb）计量单位名称格式不正确!");
                    flag = false;
                }
            }
            //判断HbMiniRange血常规血红蛋白（Hb）参考范围最小值
            if(!StringUtils.isEmpty(reportCard.getHbMiniRange())){
                boolean formatHbMiniRangeFlag = isCNChar(reportCard.getHbMiniRange());
                boolean formatHHbMiniRangeLenFlag = isMaxLength(reportCard.getHbMiniRange(),16);
                if(!(formatHbMiniRangeFlag&&formatHHbMiniRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血常规血红蛋白（Hb）参考范围最小值格式不正确!");
                    flag = false;
                }
            }
            //判断HbMaxRange血常规血红蛋白（Hb）参考范围最大值格式
            if(!StringUtils.isEmpty(reportCard.getHbMaxRange())){
                boolean formatHbMiniRangeFlag = isCNChar(reportCard.getHbMaxRange());
                boolean formatHHbMiniRangeLenFlag = isMaxLength(reportCard.getHbMaxRange(),16);
                if(!(formatHbMiniRangeFlag&&formatHHbMiniRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血常规血红蛋白（Hb）参考范围最大值格式格式不正确!");
                    flag = false;
                }
            }
            //判断PLTResult血常规血小板计数（PLT）结果
            if(!(hazardCodeInflag && ("铅".equals(reportCard.getPLTResult())||"苯".equals(reportCard.getPLTResult())||"布鲁氏菌".equals(reportCard.getPLTResult())))){
                boolean formatPLTResultFlag = isCNChar(reportCard.getPLTResult());
                boolean formatPLTResultLenFlag = isMaxLength(reportCard.getPLTResult(),8);
                if(!(formatPLTResultFlag&&formatPLTResultLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血常规血小板计数（PLT）结果格式不正确!");
                    flag = false;
                }
            }
            //判断PLTUnitName血常规血小板计数（PLT）计量单位名称
            if(!StringUtils.isEmpty(reportCard.getPLTUnitName())){
                boolean formatPLTUnitNameFlag = isCNChar(reportCard.getPLTUnitName());
                boolean formatPLTUnitNameLenFlag = isMaxLength(reportCard.getPLTUnitName(),16);
                if(!(formatPLTUnitNameFlag&&formatPLTUnitNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血常规血小板计数（PLT）计量单位名称格式不正确!");
                    flag = false;
                }
            }
            //判断PLTMiniRange血常规血小板计数（PLT）参考范围最小值
            if(!StringUtils.isEmpty(reportCard.getPLTMiniRange())){
                boolean formatPLTMiniRangeFlag = isCNChar(reportCard.getPLTMiniRange());
                boolean formatPLTMiniRangeLenFlag = isMaxLength(reportCard.getPLTMiniRange(),16);
                if(!(formatPLTMiniRangeFlag&&formatPLTMiniRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血常规血小板计数（PLT）参考范围最小值格式不正确!");
                    flag = false;
                }
            }
            //判断PLTMaxRange血常规血小板计数（PLT）参考范围最大值
            if(!StringUtils.isEmpty(reportCard.getPLTMaxRange())){
                boolean formatPLTMaxRangeFlag = isCNChar(reportCard.getPLTMaxRange());
                boolean formatPLTMaxRangeLenFlag = isMaxLength(reportCard.getPLTMaxRange(),16);
                if(!(formatPLTMaxRangeFlag&&formatPLTMaxRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血常规血小板计数（PLT）参考范围最大值格式不正确!");
                    flag = false;
                }
            }
            //GLUResult尿常规尿糖（GLU）结果
            if(!(hazardCodeInflag && ("铅".equals(reportCard.getGLUResult())||"苯".equals(reportCard.getGLUResult())||"布鲁氏菌".equals(reportCard.getGLUResult())))){
                boolean formatGLUResultFlag = isCNChar(reportCard.getGLUResult());
                boolean formatGLUResultLenFlag = isMaxLength(reportCard.getGLUResult(),8);
                if(!(formatGLUResultFlag&&formatGLUResultLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血常规血小板计数（PLT）结果格式不正确!");
                    flag = false;
                }

            }
            //GLUUnitName尿常规尿糖（GLU）计量单位
            if(!StringUtils.isEmpty(reportCard.getGLUUnitName())){
                boolean formatGLUUnitNameFlag = isCNChar(reportCard.getGLUUnitName());
                boolean formatGLUUnitNameLenFlag = isMaxLength(reportCard.getGLUUnitName(),16);
                if(!(formatGLUUnitNameFlag&&formatGLUUnitNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("尿常规尿糖（GLU）计量单位格式不正确!");
                    flag = false;
                }
            }
            //GLUMiniRange尿常规尿糖（GLU）参考范围最小值
            if(!StringUtils.isEmpty(reportCard.getGLUMiniRange())){
                boolean formatGLUMiniRangeFlag = isCNChar(reportCard.getGLUMiniRange());
                boolean formatGLUMiniRangeLenFlag = isMaxLength(reportCard.getGLUMiniRange(),16);
                if(!(formatGLUMiniRangeFlag&&formatGLUMiniRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("尿常规尿糖（GLU）参考范围最小值格式不正确!");
                    flag = false;
                }
            }
            //GLUMaxRange尿常规尿糖（GLU）参考范围最大值
            if(!StringUtils.isEmpty(reportCard.getGLUMaxRange())){
                boolean formatGLUMaxRangeFlag = isCNChar(reportCard.getGLUMaxRange());
                boolean formatGLUMaxRangeLenFlag = isMaxLength(reportCard.getGLUMaxRange(),16);
                if(!(formatGLUMaxRangeFlag&&formatGLUMaxRangeLenFlag)){
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
                boolean formatPROResultFlag = isCNChar(reportCard.getPROResult());
                boolean formatPROResultLenFlag = isMaxLength(reportCard.getPROResult(),16);
                if(!(formatPROResultFlag&&formatPROResultLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("尿常规尿蛋白（PRO）结果格式不正确!");
                    flag = false;
                }
            }
            //PROUnitName尿常规尿蛋白（PRO）计量单位名称
            if(!StringUtils.isEmpty(reportCard.getPROUnitName())){
                boolean formatPROUnitNameFlag = isCNChar(reportCard.getPROUnitName());
                boolean formatPROUnitNameLenFlag = isMaxLength(reportCard.getPROUnitName(),16);
                if(!(formatPROUnitNameFlag&&formatPROUnitNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("尿常规尿蛋白（PRO）结果格式不正确!");
                    flag = false;
                }
            }
            //PROMiniRange尿常规尿蛋白（PRO）参考范围最小值
            if(!StringUtils.isEmpty(reportCard.getPROMiniRange())){
                boolean formatPROMiniRangeFlag = isCNChar(reportCard.getPROMiniRange());
                boolean formatPROMiniRangeLenFlag = isMaxLength(reportCard.getPROMiniRange(),16);
                if(!(formatPROMiniRangeFlag&&formatPROMiniRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("尿常规尿蛋白（PRO）参考范围最小值格式不正确!");
                    flag = false;
                }
            }
            //PROMaxRange尿常规尿蛋白（PRO）参考范围最大值
            if(!StringUtils.isEmpty(reportCard.getPROMaxRange())){
                boolean formatPROMaxRangeFlag = isCNChar(reportCard.getPROMaxRange());
                boolean formatPROMaxRangeLenFlag = isMaxLength(reportCard.getPROMaxRange(),16);
                if(!(formatPROMaxRangeFlag&&formatPROMaxRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("尿常规尿蛋白（PRO）参考范围最大值格式不正确!");
                    flag = false;
                }
            }
            //UWBCResult尿常规白细胞（WBC）结果
            if(!(hazardCodeInflag && ("铅".equals(reportCard.getUWBCResult())||"苯".equals(reportCard.getUWBCResult())||"布鲁氏菌".equals(reportCard.getUWBCResult())))){
                boolean formatUWBCResultFlag = isCNChar(reportCard.getUWBCResult());
                boolean formatUWBCResultLenFlag = isMaxLength(reportCard.getUWBCResult(),8);
                if(!(formatUWBCResultFlag&&formatUWBCResultLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("尿常规白细胞（WBC）结果格式不正确!");
                    flag = false;
                }

            }
            //UWBCUnitName尿常规白细胞（WBC）计量单位名称
            if(!StringUtils.isEmpty(reportCard.getUWBCUnitName())){
                boolean formatUWBCUnitNameFlag = isCNChar(reportCard.getUWBCUnitName());
                boolean formatUWBCUnitNameLenFlag = isMaxLength(reportCard.getUWBCUnitName(),16);
                if(!(formatUWBCUnitNameFlag&&formatUWBCUnitNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("尿常规白细胞（WBC）计量单位名称格式不正确!");
                    flag = false;
                }
            }
            //UWBCMiniRange尿常规白细胞（WBC）参考范围最小值
            if(!StringUtils.isEmpty(reportCard.getUWBCMiniRange())){
                boolean formatUWBCMiniRangeFlag = isCNChar(reportCard.getUWBCMiniRange());
                boolean formatUWBCMiniRangeLenFlag = isMaxLength(reportCard.getUWBCMiniRange(),16);
                if(!(formatUWBCMiniRangeFlag&&formatUWBCMiniRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("尿常规白细胞（WBC）参考范围最小值格式不正确!");
                    flag = false;
                }
            }
            //UWBCMaxRange尿常规白细胞（WBC）参考范围最大值
            if(!StringUtils.isEmpty(reportCard.getUWBCMaxRange())){
                boolean formatUWBCMaxRangeFlag = isCNChar(reportCard.getUWBCMaxRange());
                boolean formatUWBCMaxRangeLenFlag = isMaxLength(reportCard.getUWBCMaxRange(),16);
                if(!(formatUWBCMaxRangeFlag&&formatUWBCMaxRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("尿常规白细胞（WBC）参考范围最大值格式不正确!");
                    flag = false;
                }
            }
            //BLDResult尿常规尿潜血（BLD）结果
            if(!(hazardCodeInflag && ("铅".equals(reportCard.getBLDResult())||"苯".equals(reportCard.getBLDResult())||"布鲁氏菌".equals(reportCard.getBLDResult())))){
                boolean formatUWBCResultFlag = isCNChar(reportCard.getUWBCResult());
                boolean formatUWBCResultLenFlag = isMaxLength(reportCard.getUWBCResult(),8);
                if(!(formatUWBCResultFlag&&formatUWBCResultLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("尿常规白细胞（WBC）结果格式不正确!");
                    flag = false;
                }

            }
            //BLDUnitName尿常规尿潜血（BLD）计量单位名称
            if(!StringUtils.isEmpty(reportCard.getBLDUnitName())){
                boolean formatBLDUnitNameFlag = isCNChar(reportCard.getBLDUnitName());
                boolean formatBLDUnitNameLenFlag = isMaxLength(reportCard.getBLDUnitName(),16);
                if(!(formatBLDUnitNameFlag&&formatBLDUnitNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("尿常规尿潜血（BLD）计量单位名称格式不正确!");
                    flag = false;
                }
            }
            //BLDMiniRange尿常规尿潜血（BLD）参考范围最小值
            if(!StringUtils.isEmpty(reportCard.getBLDMiniRange())){
                boolean formatBLDMiniRangeFlag = isCNChar(reportCard.getBLDMiniRange());
                boolean formatBLDMiniRangeLenFlag = isMaxLength(reportCard.getBLDMiniRange(),16);
                if(!(formatBLDMiniRangeFlag&&formatBLDMiniRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("尿常规尿潜血（BLD）参考范围最小值格式不正确!");
                    flag = false;
                }
            }
            //BLDMaxRange尿常规尿潜血（BLD）参考范围最大值
            if(!StringUtils.isEmpty(reportCard.getBLDMaxRange())){
                boolean formatBLDMaxRangeFlag = isCNChar(reportCard.getBLDMaxRange());
                boolean formatBLDMaxRangeLenFlag = isMaxLength(reportCard.getBLDMaxRange(),16);
                if(!(formatBLDMaxRangeFlag&&formatBLDMaxRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("尿常规尿潜血（BLD）参考范围最大值格式不正确!");
                    flag = false;
                }
            }
            //ALTResult肝功能谷丙转氨酶（ALT）结果
            if(!(hazardCodeInflag && ("铅".equals(reportCard.getALTResult())||"苯".equals(reportCard.getALTResult())||"布鲁氏菌".equals(reportCard.getALTResult())))){
                boolean formatALTResultFlag = isCNChar(reportCard.getALTResult());
                boolean formatALTResultLenFlag = isMaxLength(reportCard.getALTResult(),8);
                if(!(formatALTResultFlag&&formatALTResultLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("肝功能谷丙转氨酶（ALT）结果格式不正确!");
                    flag = false;
                }
            }
            //ALTUnitName肝功能谷丙转氨酶（ALT）计量单位名称
            if(!StringUtils.isEmpty(reportCard.getALTUnitName())){
                boolean formatALTUnitNameFlag = isCNChar(reportCard.getALTUnitName());
                boolean formatALTUnitNameLenFlag = isMaxLength(reportCard.getALTUnitName(),16);
                if(!(formatALTUnitNameFlag&&formatALTUnitNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("肝功能谷丙转氨酶（ALT）计量单位名称格式不正确!");
                    flag = false;
                }
            }
            //ALTMiniRange肝功能谷丙转氨酶（ALT）参考范围最小值
            if(!StringUtils.isEmpty(reportCard.getALTMiniRange())){
                boolean formatALTMiniRangeFlag = isCNChar(reportCard.getALTMiniRange());
                boolean formatALTMiniRangeLenFlag = isMaxLength(reportCard.getALTMiniRange(),16);
                if(!(formatALTMiniRangeFlag&&formatALTMiniRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("肝功能谷丙转氨酶（ALT）参考范围最小值格式不正确!");
                    flag = false;
                }
            }
            //ALTMaxRange肝功能谷丙转氨酶（ALT）参考范围最大值
            if(!StringUtils.isEmpty(reportCard.getALTMaxRange())){
                boolean formatALTMaxRangeFlag = isCNChar(reportCard.getALTMaxRange());
                boolean formatALTMaxRangeLenFlag = isMaxLength(reportCard.getALTMaxRange(),16);
                if(!(formatALTMaxRangeFlag&&formatALTMaxRangeLenFlag)){
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
            if(eCGCode.indexOf(",")==-1){
                boolean formatHECGCodeFlag = isLetterDigit(eCGCode);
                boolean formatECGCodeLenFlag = isMaxLength(eCGCode,255);
                if(!(formatHECGCodeFlag&&formatECGCodeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("心电图编码格式不正确!");
                    flag = false;
                }

            }else{
                String[] eCGCodeList = eCGCode.split(",");
                boolean formatHazardCodeLenFlag = isMaxLength(eCGCode,255);
                for(int i=0;i<eCGCodeList.length;i++){
                    boolean formatHazardCodeFlag = isLetterDigit(eCGCodeList[i]);
                    if(!(formatHazardCodeFlag&&formatHazardCodeLenFlag)){
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("心电图编码格式不正确!");
                        flag = false;
                        break;
                    }
                }
            }
            //判断ECGCodeList是否在字典值域内
            List<CodeInfo>  eCGCodeList = codeInfoService.selectByCodeInfoId(new BigDecimal(902));
            boolean eCGCodeInflag = false;
            for(CodeInfo codeInfo : hazardCodeList){
                if(reportCard.getHazardCode().equals(codeInfo.getCode())){
                    eCGCodeInflag=true;
                    break;
                }
            }
            if(!hazardCodeInflag){
                Element errorData = errorReportCards.addElement("errorData");
                Element errorMessage = errorData.addElement("errorMessage");
                Element reportCardId = errorData.addElement("reportCard");
                reportCardId.setText(reportCard.getCode());
                errorMessage.setText("ECGCode心电图编码值不正确!");
                flag = false;
            }
            //CHESTCode胸片编码
            String cHESTCode = reportCard.getCHESTCode();
            if(!(hazardCodeInflag && ("矽尘".equals(reportCard.getCHESTCode())||"煤尘(煤矽尘)".equals(reportCard.getCHESTCode())||"石棉".equals(reportCard.getCHESTCode()))))if(eCGCode.indexOf(",")==-1){
                if(cHESTCode.indexOf(",")==-1){
                    boolean formatHECGCodeFlag = isLetterDigit(cHESTCode);
                    boolean formatECGCodeLenFlag = isMaxLength(cHESTCode,255);
                    if(!(formatHECGCodeFlag&&formatECGCodeLenFlag)){
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("胸片编码格式不正确!");
                        flag = false;
                    }
                }
            }else{
                String[] cHESTCodeList = cHESTCode.split(",");
                boolean formatHazardCodeLenFlag = isMaxLength(eCGCode,255);
                for(int i=0;i<cHESTCodeList.length;i++){
                    boolean formatHazardCodeFlag = isLetterDigit(cHESTCodeList[i]);
                    if(!(formatHazardCodeFlag&&formatHazardCodeLenFlag)){
                        Element errorData = errorReportCards.addElement("errorData");
                        Element errorMessage = errorData.addElement("errorMessage");
                        Element reportCardId = errorData.addElement("reportCard");
                        reportCardId.setText(reportCard.getCode());
                        errorMessage.setText("胸片编码格式格式不正确!");
                        flag = false;
                        break;
                    }
                }
            }
            //判断ECGCodeList是否在字典值域内
            List<CodeInfo>  cHESTCodeList = codeInfoService.selectByCodeInfoId(new BigDecimal(903));
            boolean cHESTCodeListInflag = false;
            for(CodeInfo codeInfo : cHESTCodeList){
                if(reportCard.getHazardCode().equals(codeInfo.getCode())){
                    cHESTCodeListInflag=true;
                    break;
                }
            }
            if(!hazardCodeInflag){
                Element errorData = errorReportCards.addElement("errorData");
                Element errorMessage = errorData.addElement("errorMessage");
                Element reportCardId = errorData.addElement("reportCard");
                reportCardId.setText(reportCard.getCode());
                errorMessage.setText("ECGCode心电图编码值不正确!");
                flag = false;
            }
            //FVCResult肺功能FVC结果
            if(!(hazardCodeInflag && ("矽尘".equals(reportCard.getFVCResult())||"煤尘(煤矽尘)".equals(reportCard.getFVCResult())||"石棉".equals(reportCard.getFVCResult())))){
                boolean formatFVCResultFlag = isCNChar(reportCard.getFVCResult());
                boolean formatFVCResultLenFlag = isMaxLength(reportCard.getFVCResult(),8);
                if(!(formatFVCResultFlag&&formatFVCResultLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("肺功能FVC结果格式不正确!");
                    flag = false;
                }
            }
            //FVCUnitName肺功能FVC计量单位名称
            if(!StringUtils.isEmpty(reportCard.getFVCUnitName())){
                boolean formatFVCUnitNameFlag = isCNChar(reportCard.getFVCUnitName());
                boolean formatFVCUnitNameLenFlag = isMaxLength(reportCard.getFVCUnitName(),16);
                if(!(formatFVCUnitNameFlag&&formatFVCUnitNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("肺功能FVC计量单位名称格式不正确!");
                    flag = false;
                }
            }
            //FVCMiniRange肺功能FVC参考范围最小值
            if(!StringUtils.isEmpty(reportCard.getFVCMiniRange())){
                boolean formatFVCMiniRangeFlag = isCNChar(reportCard.getFVCMiniRange());
                boolean formatFVCMiniRangeLenFlag = isMaxLength(reportCard.getFVCUnitName(),16);
                if(!(formatFVCMiniRangeFlag&&formatFVCMiniRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("肺功能FVC参考范围最小值格式不正确!");
                    flag = false;
                }
            }
            //FVCMaxRange肺功能FVC参考范围最大值
            if(!StringUtils.isEmpty(reportCard.getFVCMaxRange())){
                boolean formatFVCMaxRangeFlag = isCNChar(reportCard.getFVCMaxRange());
                boolean formatFVCMaxRangeLenFlag = isMaxLength(reportCard.getFVCMaxRange(),16);
                if(!(formatFVCMaxRangeFlag&&formatFVCMaxRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("肺功能FVC参考范围最大值格式不正确!");
                    flag = false;
                }
            }
            //FEV1Result肺功能FEV1结果
            if(!(hazardCodeInflag && ("矽尘".equals(reportCard.getFEV1Result())||"煤尘(煤矽尘)".equals(reportCard.getFEV1Result())||"石棉".equals(reportCard.getFEV1Result())))){
                boolean formatFEV1ResultFlag = isCNChar(reportCard.getFEV1Result());
                boolean formatFEV1ResultLenFlag = isMaxLength(reportCard.getFEV1Result(),8);
                if(!(formatFEV1ResultFlag&&formatFEV1ResultLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("肺功能FEV1结果格式不正确!");
                    flag = false;
                }
            }
            //FEV1UnitName肺功能FEV1计量单位名称
            if(!StringUtils.isEmpty(reportCard.getFEV1UnitName())){
                boolean formatFEV1UnitNameFlag = isCNChar(reportCard.getFEV1UnitName());
                boolean formatFEV1UnitNameLenFlag = isMaxLength(reportCard.getFEV1UnitName(),16);
                if(!(formatFEV1UnitNameFlag&&formatFEV1UnitNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("肺功能FEV1计量单位名称格式不正确!");
                    flag = false;
                }
            }
            //FEV1MiniRange肺功能FEV1参考范围最小值
            if(!StringUtils.isEmpty(reportCard.getFEV1MiniRange())){
                boolean formatFEV1MiniRangeFlag = isCNChar(reportCard.getFEV1MiniRange());
                boolean formatFEV1MiniRangeLenFlag = isMaxLength(reportCard.getFEV1MiniRange(),16);
                if(!(formatFEV1MiniRangeFlag&&formatFEV1MiniRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("肺功能FEV1参考范围最小值格式不正确!");
                    flag = false;
                }
            }
            //FEV1MaxRange肺功能FEV1参考范围最大值
            if(!StringUtils.isEmpty(reportCard.getFEV1MaxRange())){
                boolean formatFEV1MaxRangeFlag = isCNChar(reportCard.getFEV1MaxRange());
                boolean formatFEV1MaxRangeLenFlag = isMaxLength(reportCard.getFEV1MaxRange(),16);
                if(!(formatFEV1MaxRangeFlag&&formatFEV1MaxRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("肺功能FEV1参考范围最大值格式不正确!");
                    flag = false;
                }
            }
            //FEV1FVCResult肺功能FEV1/FVC结果
            if(!(hazardCodeInflag && ("矽尘".equals(reportCard.getFEV1FVCResult())||"煤尘(煤矽尘)".equals(reportCard.getFEV1FVCResult())||"石棉".equals(reportCard.getFEV1FVCResult())))){
                boolean formatFEV1FVCResultFlag = isCNChar(reportCard.getFEV1FVCResult());
                boolean formatFEV1FVCResultLenFlag = isMaxLength(reportCard.getFEV1FVCResult(),8);
                if(!(formatFEV1FVCResultFlag&&formatFEV1FVCResultLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("肺功能FEV1/FVC结果格式不正确!");
                    flag = false;
                }
            }
            //FEV1FVCUnitName肺功能FEV1/FVC计量
            if(!StringUtils.isEmpty(reportCard.getFEV1FVCUnitName())){
                boolean formatFEV1FVCUnitNameFlag = isCNChar(reportCard.getFEV1FVCUnitName());
                boolean formatFEV1FVCUnitNameLenFlag = isMaxLength(reportCard.getFEV1FVCUnitName(),16);
                if(!(formatFEV1FVCUnitNameFlag&&formatFEV1FVCUnitNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("肺功能FEV1/FVC计量格式不正确!");
                    flag = false;
                }
            }
            //FEV1FVCMiniRange肺功能FEV1/FVC参考范围最小值
            if(!StringUtils.isEmpty(reportCard.getFEV1FVCMiniRange())){
                boolean formatFEV1FVCMiniRangeFlag = isCNChar(reportCard.getFEV1FVCMiniRange());
                boolean formatFEV1FVCMiniRangeLenFlag = isMaxLength(reportCard.getFEV1FVCMiniRange(),16);
                if(!(formatFEV1FVCMiniRangeFlag&&formatFEV1FVCMiniRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("肺功能FEV1/FVC参考范围最小值格式不正确!");
                    flag = false;
                }
            }
            //FEV1FVCMaxRange肺功能FEV1/FVC参考范围最大值
            if(!StringUtils.isEmpty(reportCard.getFEV1FVCMaxRange())){
                boolean formatFEV1FVCMaxRangeFlag = isCNChar(reportCard.getFEV1FVCMaxRange());
                boolean formatFEV1FVCMaxRangeLenFlag = isMaxLength(reportCard.getFEV1FVCMaxRange(),16);
                if(!(formatFEV1FVCMaxRangeFlag&&formatFEV1FVCMaxRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("肺功能FEV1/FVC参考范围最大值格式不正确!");
                    flag = false;
                }
            }
            //BLeadResult血铅结果
            if(!(hazardCodeInflag && ("铅".equals(reportCard.getBLeadResult())))){
                boolean formatBLeadResultFlag = isCNChar(reportCard.getBLeadResult());
                boolean formatBLeadResultLenFlag = isMaxLength(reportCard.getBLeadResult(),8);
                if(!(formatBLeadResultFlag&&formatBLeadResultLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血铅结果格式不正确!");
                    flag = false;
                }
            }
            //BLeadUnitName血铅计量单位名称
            if(!StringUtils.isEmpty(reportCard.getBLeadUnitName())){
                boolean formatBLeadUnitNameFlag = isCNChar(reportCard.getBLeadUnitName());
                boolean formatBLeadUnitNameLenFlag = isMaxLength(reportCard.getBLeadUnitName(),16);
                if(!(formatBLeadUnitNameFlag&&formatBLeadUnitNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血铅计量单位名称格式不正确!");
                    flag = false;
                }
            }
            //BLeadMiniRange血铅参考范围最小值
            if(!StringUtils.isEmpty(reportCard.getBLeadMiniRange())){
                boolean formatBLeadMiniRangeFlag = isCNChar(reportCard.getBLeadMiniRange());
                boolean formatBLeadMiniRangeLenFlag = isMaxLength(reportCard.getBLeadMiniRange(),16);
                if(!(formatBLeadMiniRangeFlag&&formatBLeadMiniRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血铅参考范围最小值格式不正确!");
                    flag = false;
                }
            }
            //BLeadMaxRange血铅参考范围最大值
            if(!StringUtils.isEmpty(reportCard.getBLeadMaxRange())){
                boolean formatBLeadMaxRangeFlag = isCNChar(reportCard.getBLeadMaxRange());
                boolean formatBLeadMaxRangeLenFlag = isMaxLength(reportCard.getBLeadMaxRange(),16);
                if(!(formatBLeadMaxRangeFlag&&formatBLeadMaxRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("血铅参考范围最大值格式不正确!");
                    flag = false;
                }
            }
            //ULeadResult尿铅结果
            if(!(hazardCodeInflag && ("铅".equals(reportCard.getULeadResult())))){
                boolean formatULeadResultFlag = isCNChar(reportCard.getULeadResult());
                boolean formatULeadResultLenFlag = isMaxLength(reportCard.getULeadResult(),8);
                if(!(formatULeadResultFlag&&formatULeadResultLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("尿铅结果式不正确!");
                    flag = false;
                }
            }
            //ULeadUnitName尿铅计量单位名称
            if(!StringUtils.isEmpty(reportCard.getULeadUnitName())){
                boolean formatULeadUnitNameFlag = isCNChar(reportCard.getULeadUnitName());
                boolean formatULeadUnitNameLenFlag = isMaxLength(reportCard.getULeadUnitName(),16);
                if(!(formatULeadUnitNameFlag&&formatULeadUnitNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("尿铅计量单位名称格式不正确!");
                    flag = false;
                }
            }
            //ULeadMiniRange尿铅参考范围最小值
            if(!StringUtils.isEmpty(reportCard.getULeadMiniRange())){
                boolean formatULeadMiniRangeFlag = isCNChar(reportCard.getULeadMiniRange());
                boolean formatULeadMiniRangeLenFlag = isMaxLength(reportCard.getULeadMiniRange(),16);
                if(!(formatULeadMiniRangeFlag&&formatULeadMiniRangeLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("尿铅参考范围最小值格式不正确!");
                    flag = false;
                }
            }
            //ULeadMaxRange尿铅参考范围最大值
            if(!StringUtils.isEmpty(reportCard.getULeadMaxRange())){
                boolean formatULeadMaxRangeFlag = isCNChar(reportCard.getULeadMaxRange());
                boolean formatULeadMaxRangeLenFlag = isMaxLength(reportCard.getULeadMaxRange(),16);
                if(!(formatULeadMaxRangeFlag&&formatULeadMaxRangeLenFlag)){
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
                boolean formatZPPResultFlag = isCNChar(reportCard.getZPPResult());
                boolean formatZPPResultLenFlag = isMaxLength(reportCard.getZPPResult(),8);
                if(!(formatZPPResultFlag&&formatZPPResultLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("铅红细胞锌原卟啉（ZPP）结果格式不正确!");
                    flag = false;
                }
            }
            //ZPPUnitName铅红细胞锌原卟啉（ZPP）计量单位名称
            if(!StringUtils.isEmpty(reportCard.getZPPUnitName())){
                boolean formatZPPUnitNameFlag = isCNChar(reportCard.getZPPUnitName());
                boolean formatZPPUnitNameLenFlag = isMaxLength(reportCard.getZPPUnitName(),16);
                if(!(formatZPPUnitNameFlag&&formatZPPUnitNameLenFlag)){
                    Element errorData = errorReportCards.addElement("errorData");
                    Element errorMessage = errorData.addElement("errorMessage");
                    Element reportCardId = errorData.addElement("reportCard");
                    reportCardId.setText(reportCard.getCode());
                    errorMessage.setText("铅红细胞锌原卟啉（ZPP）计量单位名称格式不正确!");
                    flag = false;
                }
            }

        }

        return flag;
    }
    //数据唯一性校验
    private Boolean isOnlyData(HashMap<String,String> hashMap){
        ZybGak zybGak = zybGakService.selectByCodeAndHosId(hashMap);
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
    //数字字母字符正则判断
    private  boolean isLetterDigit(String str) {
        String regex = "^[a-z0-9A-Z]+$";
        return str.matches(regex);
    }
    //判断字符串最大长度
    private boolean isMaxLength(String str,int len){
        return str.length()<=len;
    }
    //判断数字字符
    private boolean isDigist(String str){
        String regex = "^[0-9]*$";
        return str.matches(regex);
    }
    //判断字符固定长度
    private boolean isLength(String str,int len){
        return str.length()==len;
    }
    //匹配中文字符
    private boolean isCNChar(String str){
        String regex = "[u4e00-u9fa5]";
        return str.matches(regex);
    }
    //数值型, 小数点后保留2位数字
    private boolean isDouNot(String str){
        String regex ="^([1-9][0-9]*)+(.[0-9]{1,2})?$";
        return str.matches(regex);
    }

}