package com.wondersgroup.tzscws1.util;





import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import java.util.Date;

/**
 * @ClassName: SoapWebServiceClient.java
 * @Description:Soap WebService 封装类
 * @author 夏柏林
 * @date 2019年2月21日 下午4:50:33
 */

public class SoapWebServiceClient {
	/**
	 * 默认的连接时间
	 */
	final private long DEF_CONN_TIMEOUT = 10000;

	/**
	 * 
	 *
	 * @Title: createClientWebService
	 * @Description:创建soap 客户端调用对象
	 * @author:夏柏林
	 * @date: 2019年2月21日 下午5:34:27
	 * @return
	 *
	 */
	public Client createClientWebService(String address) {
		return createClientWebService(address, DEF_CONN_TIMEOUT);
	}

	/**
	 * 
	 *
	 * @Title: createClientWebService
	 * @Description: 创建soap 客户端调用对象
	 * @author:夏柏林
	 * @date: 2019年2月21日 下午5:33:57
	 * @return
	 *
	 */
	public Client createClientWebService(String address, long timeout) {
		JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
		Client client = dcf.createClient(address);
		// 设置超时单位为毫秒
		HTTPConduit conduit = (HTTPConduit) client.getConduit();
		HTTPClientPolicy policy = new HTTPClientPolicy();
		policy.setConnectionTimeout(timeout);
		policy.setAllowChunking(false);
		policy.setReceiveTimeout(timeout * 15);
		conduit.setClient(policy);
		return client;
	}

	/**
	 * 
	 *
	 * @Title: callWebService
	 * @Description:调用Soap形式的WebService
	 * @author:夏柏林
	 * @date: 2019年2月21日 下午5:35:02
	 * @return
	 *
	 */
	public Object callWebService(String address, String methodName, String paramXml) throws Exception {
		return callWebService(address, methodName, paramXml, DEF_CONN_TIMEOUT);
	}

	/**
	 * 
	 *
	 * @Title: callWebService
	 * @Description:调用Soap形式的WebService
	 * @author:夏柏林
	 * @date: 2019年2月21日 下午5:24:31
	 * @return
	 * @throws Exception
	 *
	 */
	public Object callWebService(String address, String methodName, String paramXml, long timeout) throws Exception {
		Client client = createClientWebService(address, timeout);
		if (client != null) {
			return client.invoke(methodName, paramXml);
		} else {
			return null;
		}
	}

	public static void main(String[] args) throws Exception {
        Date date = new Date();
		String xmls = "<data>" +
				"<header>" +
				"<eventId>test</eventId>" +
				"<hosId>123</hosId>" +
				"<requestTime>"+date+"</requestTime>" +
				"<headSign>123456</headSign>" +
				"<bodySign>25463</bodySign>" +
				"</header>" +
				"<body>" +
				"<reportCards>" +
				"<reportCard>" +
				"<code>2</code>" +
				"<hosId>331003001</hosId>" +
				"<name>gfdgfg</name>" +
				"<idCard>430124199702184017</idCard>" +
				"<bodyCheckType>1</bodyCheckType>" +
				"<bodyCheckTime>20180423</bodyCheckTime>"+
				"<orgCode>12365421-8</orgCode>"+
				"<employerName>zhonyi</employerName>"+
				"<hazardCode>5,90</hazardCode>"+
				"<sexCode>2</sexCode>" +
				"<birthday>19910112</birthday>" +
                 "<diasPressUnitName>你好</diasPressUnitName>"+
				"<FVCMaxRange>55</FVCMaxRange>"+
				"<FEV1MaxRange>99</FEV1MaxRange>"+
				"<hazardYear>60</hazardYear>" +
				"<hazardMonth>12</hazardMonth>" +
				"<sysPressResult>dweq3</sysPressResult>" +
				"<diasPressResult>3r353df</diasPressResult>" +
				"<RBCResult>wed</RBCResult>"+
				"<ECGCode>0001</ECGCode>" +
				"<CHESTCode>1</CHESTCode>"+
				"<FVCResult>dsds</FVCResult>"+
				"<HBResult>dwq</HBResult>"+
				"<PLTResult>vbf</PLTResult>"+
				"<GLUResult>erfd</GLUResult>"+
				"<PROResult>vfgh</PROResult>"+
				"<UWBCResult>dcge</UWBCResult>"+
				"<BLDResult>uiop</BLDResult>"+
				"<ALTResult>gbht</ALTResult>"+
				"<NeutResult>22</NeutResult>"+
				"<FEV1Result>2</FEV1Result>"+
				"<FEV1FVCMaxRange>25</FEV1FVCMaxRange>"+
				"<FEV1FVCUnitName>dfed</FEV1FVCUnitName>"+
				"<FEV1UnitName>1234</FEV1UnitName>"+
				"<FEV1FVCMiniRange>10</FEV1FVCMiniRange>" +
				"<FEV1MiniRange>5</FEV1MiniRange>"+
				"<FVCUnitName>5678</FVCUnitName>"+
				"<conclusionsCode>1990112</conclusionsCode>" +
				"<WBCResult>2</WBCResult>"+
				"</reportCard>" +
				"<reportCard>" +
				"<code>788</code>" +
				"<hosId>22434</hosId>" +
				"<name>gfdgfg</name>" +
				"<idCard>430124198902284018</idCard>" +
				"<bodyCheckType>1</bodyCheckType>" +
				"<bodyCheckTime>20180423</bodyCheckTime>"+
				"<orgCode>12365421-6</orgCode>"+
				"<employerName>xiyi</employerName>"+
				"<hazardCode>5,90</hazardCode>"+
				"<sexCode>2</sexCode>" +
				"<birthday>19910112</birthday>" +
				"<diasPressUnitName>你好</diasPressUnitName>"+
				"<FVCMaxRange>55</FVCMaxRange>"+
				"<FEV1MaxRange>99</FEV1MaxRange>"+
				"<hazardYear>60</hazardYear>" +
				"<hazardMonth>12</hazardMonth>" +
				"<sysPressResult>dweq3</sysPressResult>" +
				"<diasPressResult>3r353df</diasPressResult>" +
				"<RBCResult>wed</RBCResult>"+
				"<ECGCode>0001</ECGCode>" +
				"<CHESTCode>1</CHESTCode>"+
				"<FVCResult>dsds</FVCResult>"+
				"<HBResult>dwq</HBResult>"+
				"<PLTResult>vbf</PLTResult>"+
				"<GLUResult>erfd</GLUResult>"+
				"<PROResult>vfgh</PROResult>"+
				"<UWBCResult>dcge</UWBCResult>"+
				"<BLDResult>uiop</BLDResult>"+
				"<ALTResult>gbht</ALTResult>"+
				"<NeutResult>22</NeutResult>"+
				"<FEV1Result>2</FEV1Result>"+
				"<FEV1FVCMaxRange>25</FEV1FVCMaxRange>"+
				"<FEV1FVCUnitName>dfed</FEV1FVCUnitName>"+
				"<FEV1UnitName>1234</FEV1UnitName>"+
				"<FEV1FVCMiniRange>10</FEV1FVCMiniRange>"+
				"<FEV1MiniRange>5</FEV1MiniRange>"+
				"<FVCUnitName>5678</FVCUnitName>"+
				"<conclusionsCode>1990112</conclusionsCode>" +
				"<WBCResult>2</WBCResult>"+
				"</reportCard>" +
				"</reportCards>" +
				"</body>" +
				"</data>";
		SoapWebServiceClient soap = new SoapWebServiceClient();
		StringBuilder buf = new StringBuilder();
		buf.append("<Body ><Request  OrgCode='47171930' OperType='8.2'></Request></Body>");
		String paramXml = buf.toString();
		Object rs =  soap.callWebService("https://www.xiaoyisheng.net.cn/ws_data/ws/TJ?wsdl", "transport",
				xmls);
		System.out.println(">>>>>>>>>"+rs);
		JsonConfig jsonConfig = new JsonConfig();

		jsonConfig.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);

		JSONArray json = JSONArray.fromObject(rs, jsonConfig);
        System.out.println(json);


	}

	public String Object2Json(Object obj){
		JSONObject json = JSONObject.fromObject(obj);//将java对象转换为json对象
		String str = json.toString();//将json对象转换为字符串

		return str;
	}

}
