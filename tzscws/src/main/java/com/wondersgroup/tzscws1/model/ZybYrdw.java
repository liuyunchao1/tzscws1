package com.wondersgroup.tzscws1.model;

import java.util.Date;

public class ZybYrdw {
    /**
     * 社会统一信用代码 ZYB_YRDW.CREDIT_CODE
     */
    private String creditCode;
    /**
     * 用人单位编码 ZYB_YRDW.EMPLOYER_CODE
     */
    private String employerCode;

    /**
     * 用人单位名称 ZYB_YRDW.EMPLOYER_NAME
     */
    private String employerName;

    /**
     * 单位描述信息 ZYB_YRDW.EMPLOYER_DESC
     */
    private String employerDesc;

    /**
     * 所属地区国标 ZYB_YRDW.AREA_STANDARD
     */
    private Integer areaStandard;

    /**
     * 所属地区详细地址 ZYB_YRDW.AREA_ADDRESS
     */
    private String areaAddress;

    /**
     * 经济类型编码 ZYB_YRDW.ECONOMIC_CODE
     */
    private String economicCode;

    /**
     * 行业编码 ZYB_YRDW.INDUSTRY_CATE_CODE
     */
    private Short industryCateCode;

    /**
     * 企业规模编码 ZYB_YRDW.ENTERPRISE_CODE
     */
    private Short enterpriseCode;

    /**
     * 二级单位编码 ZYB_YRDW.SECOND_EMPLOYER_CODE
     */
    private Long secondEmployerCode;

    /**
     * 二级单位名称 ZYB_YRDW.SECOND_EMPLOYER_NAME
     */
    private String secondEmployerName;

    /**
     * 通讯地址 ZYB_YRDW.POST_ADDRESS
     */
    private String postAddress;

    /**
     * 邮编 ZYB_YRDW.ZIP_CODE
     */
    private Integer zipCode;

    /**
     * 联系人 ZYB_YRDW.CONTACT_PERSON
     */
    private String contactPerson;

    /**
     * 联系电话 ZYB_YRDW.CONTACT_PHONE
     */
    private Long contactPhone;

    /**
     * 监测单位编码 ZYB_YRDW.MONITOR_ORG_CODE
     */
    private String monitorOrgCode;

    /**
     * 监测单位名称 ZYB_YRDW.MONITOR_ORG_NAME
     */
    private String monitorOrgName;

    /**
     * 备注 ZYB_YRDW.REMARKS
     */
    private String remarks;

    /**
     * 上报标志(0未上报，1上报省成功，2上报省失败(后期可以添加其他状态)) ZYB_YRDW.SBBZ
     */
    private String sbbz;

    /**
     * 上报失败原因 ZYB_YRDW.SBYY
     */
    private String sbyy;

    /**
     * 上报时间 ZYB_YRDW.SBSJ
     */
    private Date sbsj;

    /**
     * 日志时间 ZYB_YRDW.LOGSJ
     */
    private Date logsj;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.EMPLOYER_CODE
     *
     * @return the value of ZYB_YRDW.EMPLOYER_CODE
     *
     * @mbggenerated
     */
    public String getEmployerCode() {
        return employerCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.EMPLOYER_CODE
     *
     * @param employerCode the value for ZYB_YRDW.EMPLOYER_CODE
     *
     * @mbggenerated
     */
    public void setEmployerCode(String employerCode) {
        this.employerCode = employerCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.EMPLOYER_NAME
     *
     * @return the value of ZYB_YRDW.EMPLOYER_NAME
     *
     * @mbggenerated
     */
    public String getEmployerName() {
        return employerName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.EMPLOYER_NAME
     *
     * @param employerName the value for ZYB_YRDW.EMPLOYER_NAME
     *
     * @mbggenerated
     */
    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.EMPLOYER_DESC
     *
     * @return the value of ZYB_YRDW.EMPLOYER_DESC
     *
     * @mbggenerated
     */
    public String getEmployerDesc() {
        return employerDesc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.EMPLOYER_DESC
     *
     * @param employerDesc the value for ZYB_YRDW.EMPLOYER_DESC
     *
     * @mbggenerated
     */
    public void setEmployerDesc(String employerDesc) {
        this.employerDesc = employerDesc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.AREA_STANDARD
     *
     * @return the value of ZYB_YRDW.AREA_STANDARD
     *
     * @mbggenerated
     */
    public Integer getAreaStandard() {
        return areaStandard;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.AREA_STANDARD
     *
     * @param areaStandard the value for ZYB_YRDW.AREA_STANDARD
     *
     * @mbggenerated
     */
    public void setAreaStandard(Integer areaStandard) {
        this.areaStandard = areaStandard;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.AREA_ADDRESS
     *
     * @return the value of ZYB_YRDW.AREA_ADDRESS
     *
     * @mbggenerated
     */
    public String getAreaAddress() {
        return areaAddress;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.AREA_ADDRESS
     *
     * @param areaAddress the value for ZYB_YRDW.AREA_ADDRESS
     *
     * @mbggenerated
     */
    public void setAreaAddress(String areaAddress) {
        this.areaAddress = areaAddress;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.ECONOMIC_CODE
     *
     * @return the value of ZYB_YRDW.ECONOMIC_CODE
     *
     * @mbggenerated
     */
    public String getEconomicCode() {
        return economicCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.ECONOMIC_CODE
     *
     * @param economicCode the value for ZYB_YRDW.ECONOMIC_CODE
     *
     * @mbggenerated
     */
    public void setEconomicCode(String economicCode) {
        this.economicCode = economicCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.INDUSTRY_CATE_CODE
     *
     * @return the value of ZYB_YRDW.INDUSTRY_CATE_CODE
     *
     * @mbggenerated
     */
    public Short getIndustryCateCode() {
        return industryCateCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.INDUSTRY_CATE_CODE
     *
     * @param industryCateCode the value for ZYB_YRDW.INDUSTRY_CATE_CODE
     *
     * @mbggenerated
     */
    public void setIndustryCateCode(Short industryCateCode) {
        this.industryCateCode = industryCateCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.ENTERPRISE_CODE
     *
     * @return the value of ZYB_YRDW.ENTERPRISE_CODE
     *
     * @mbggenerated
     */
    public Short getEnterpriseCode() {
        return enterpriseCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.ENTERPRISE_CODE
     *
     * @param enterpriseCode the value for ZYB_YRDW.ENTERPRISE_CODE
     *
     * @mbggenerated
     */
    public void setEnterpriseCode(Short enterpriseCode) {
        this.enterpriseCode = enterpriseCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.SECOND_EMPLOYER_CODE
     *
     * @return the value of ZYB_YRDW.SECOND_EMPLOYER_CODE
     *
     * @mbggenerated
     */
    public Long getSecondEmployerCode() {
        return secondEmployerCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.SECOND_EMPLOYER_CODE
     *
     * @param secondEmployerCode the value for ZYB_YRDW.SECOND_EMPLOYER_CODE
     *
     * @mbggenerated
     */
    public void setSecondEmployerCode(Long secondEmployerCode) {
        this.secondEmployerCode = secondEmployerCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.SECOND_EMPLOYER_NAME
     *
     * @return the value of ZYB_YRDW.SECOND_EMPLOYER_NAME
     *
     * @mbggenerated
     */
    public String getSecondEmployerName() {
        return secondEmployerName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.SECOND_EMPLOYER_NAME
     *
     * @param secondEmployerName the value for ZYB_YRDW.SECOND_EMPLOYER_NAME
     *
     * @mbggenerated
     */
    public void setSecondEmployerName(String secondEmployerName) {
        this.secondEmployerName = secondEmployerName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.POST_ADDRESS
     *
     * @return the value of ZYB_YRDW.POST_ADDRESS
     *
     * @mbggenerated
     */
    public String getPostAddress() {
        return postAddress;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.POST_ADDRESS
     *
     * @param postAddress the value for ZYB_YRDW.POST_ADDRESS
     *
     * @mbggenerated
     */
    public void setPostAddress(String postAddress) {
        this.postAddress = postAddress;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.ZIP_CODE
     *
     * @return the value of ZYB_YRDW.ZIP_CODE
     *
     * @mbggenerated
     */
    public Integer getZipCode() {
        return zipCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.ZIP_CODE
     *
     * @param zipCode the value for ZYB_YRDW.ZIP_CODE
     *
     * @mbggenerated
     */
    public void setZipCode(Integer zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.CONTACT_PERSON
     *
     * @return the value of ZYB_YRDW.CONTACT_PERSON
     *
     * @mbggenerated
     */
    public String getContactPerson() {
        return contactPerson;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.CONTACT_PERSON
     *
     * @param contactPerson the value for ZYB_YRDW.CONTACT_PERSON
     *
     * @mbggenerated
     */
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.CONTACT_PHONE
     *
     * @return the value of ZYB_YRDW.CONTACT_PHONE
     *
     * @mbggenerated
     */
    public Long getContactPhone() {
        return contactPhone;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.CONTACT_PHONE
     *
     * @param contactPhone the value for ZYB_YRDW.CONTACT_PHONE
     *
     * @mbggenerated
     */
    public void setContactPhone(Long contactPhone) {
        this.contactPhone = contactPhone;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.MONITOR_ORG_CODE
     *
     * @return the value of ZYB_YRDW.MONITOR_ORG_CODE
     *
     * @mbggenerated
     */
    public String getMonitorOrgCode() {
        return monitorOrgCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.MONITOR_ORG_CODE
     *
     * @param monitorOrgCode the value for ZYB_YRDW.MONITOR_ORG_CODE
     *
     * @mbggenerated
     */
    public void setMonitorOrgCode(String monitorOrgCode) {
        this.monitorOrgCode = monitorOrgCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.MONITOR_ORG_NAME
     *
     * @return the value of ZYB_YRDW.MONITOR_ORG_NAME
     *
     * @mbggenerated
     */
    public String getMonitorOrgName() {
        return monitorOrgName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.MONITOR_ORG_NAME
     *
     * @param monitorOrgName the value for ZYB_YRDW.MONITOR_ORG_NAME
     *
     * @mbggenerated
     */
    public void setMonitorOrgName(String monitorOrgName) {
        this.monitorOrgName = monitorOrgName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.REMARKS
     *
     * @return the value of ZYB_YRDW.REMARKS
     *
     * @mbggenerated
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.REMARKS
     *
     * @param remarks the value for ZYB_YRDW.REMARKS
     *
     * @mbggenerated
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.SBBZ
     *
     * @return the value of ZYB_YRDW.SBBZ
     *
     * @mbggenerated
     */
    public String getSbbz() {
        return sbbz;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.SBBZ
     *
     * @param sbbz the value for ZYB_YRDW.SBBZ
     *
     * @mbggenerated
     */
    public void setSbbz(String sbbz) {
        this.sbbz = sbbz;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.SBYY
     *
     * @return the value of ZYB_YRDW.SBYY
     *
     * @mbggenerated
     */
    public String getSbyy() {
        return sbyy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.SBYY
     *
     * @param sbyy the value for ZYB_YRDW.SBYY
     *
     * @mbggenerated
     */
    public void setSbyy(String sbyy) {
        this.sbyy = sbyy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.SBSJ
     *
     * @return the value of ZYB_YRDW.SBSJ
     *
     * @mbggenerated
     */
    public Date getSbsj() {
        return sbsj;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.SBSJ
     *
     * @param sbsj the value for ZYB_YRDW.SBSJ
     *
     * @mbggenerated
     */
    public void setSbsj(Date sbsj) {
        this.sbsj = sbsj;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZYB_YRDW.LOGSJ
     *
     * @return the value of ZYB_YRDW.LOGSJ
     *
     * @mbggenerated
     */
    public Date getLogsj() {
        return logsj;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZYB_YRDW.LOGSJ
     *
     * @param logsj the value for ZYB_YRDW.LOGSJ
     *
     * @mbggenerated
     */
    public void setLogsj(Date logsj) {
        this.logsj = logsj;
    }

    public String getCreditCode() {
        return creditCode;
    }

    public void setCreditCode(String creditCode) {
        this.creditCode = creditCode;
    }
}