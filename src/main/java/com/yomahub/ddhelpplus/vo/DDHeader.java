package com.yomahub.ddhelpplus.vo;

public class DDHeader {

    private String ddmcDeviceId;

    private String cookie;

    private String ddmcLongitude;

    private String ddmcLatitude;

    private String ddmcUid;

    private String userAgent;

    public String getDdmcDeviceId() {
        return ddmcDeviceId;
    }

    public void setDdmcDeviceId(String ddmcDeviceId) {
        this.ddmcDeviceId = ddmcDeviceId;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getDdmcLongitude() {
        return ddmcLongitude;
    }

    public void setDdmcLongitude(String ddmcLongitude) {
        this.ddmcLongitude = ddmcLongitude;
    }

    public String getDdmcLatitude() {
        return ddmcLatitude;
    }

    public void setDdmcLatitude(String ddmcLatitude) {
        this.ddmcLatitude = ddmcLatitude;
    }

    public String getDdmcUid() {
        return ddmcUid;
    }

    public void setDdmcUid(String ddmcUid) {
        this.ddmcUid = ddmcUid;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
