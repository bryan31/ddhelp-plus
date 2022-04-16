package com.yomahub.ddhelpplus.vo;

public class DDPlusProp {

    private String stationId;

    private String addressId;

    private DDHeader header;

    private DDBody body;

    public DDHeader getHeader() {
        return header;
    }

    public void setHeader(DDHeader header) {
        this.header = header;
    }

    public DDBody getBody() {
        return body;
    }

    public void setBody(DDBody body) {
        this.body = body;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }
}
