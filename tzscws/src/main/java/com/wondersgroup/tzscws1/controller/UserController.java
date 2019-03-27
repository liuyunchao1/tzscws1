package com.wondersgroup.tzscws1.controller;



import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user")
public class UserController {
    @ResponseBody
    @RequestMapping("/add")
    public String addUser(){
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
        System.out.println(xml);
        ReadStringXmlController readStringXmlController =  new ReadStringXmlController();
        readStringXmlController.readStringXml(xml);
        return "hello lyc";
    }

}
