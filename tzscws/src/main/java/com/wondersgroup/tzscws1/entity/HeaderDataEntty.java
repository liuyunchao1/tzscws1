package com.wondersgroup.tzscws1.entity;

public class HeaderDataEntty {

    private String eventId; //业务请求类型编码
    private String hosId;  //医院编码
    private String requestTime;//请求时间
    private String headSign; //用户秘钥
    private String bodySign;//数据签名

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getHosId() {
        return hosId;
    }

    public void setHosId(String hosId) {
        this.hosId = hosId;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getHeadSign() {
        return headSign;
    }

    public void setHeadSign(String headSign) {
        this.headSign = headSign;
    }

    public String getBodySign() {
        return bodySign;
    }

    public void setBodySign(String bodySign) {
        this.bodySign = bodySign;
    }
}
