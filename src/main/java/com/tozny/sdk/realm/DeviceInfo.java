package com.tozny.sdk.realm;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceInfo {
    private final String type;
    private final String operator;
    private final String country;
    private final String technology;
    private final String roaming;
    private final String status;

    @JsonCreator
    public DeviceInfo(
            @JsonProperty("type")       String type,
            @JsonProperty("operator")   String operator,
            @JsonProperty("country")    String country,
            @JsonProperty("technology") String technology,
            @JsonProperty("roaming")    String roaming,
            @JsonProperty("status")     String status) {

        this.type = type;
        this.operator = operator;
        this.country = country;
        this.technology = technology;
        this.roaming = roaming;
        this.status = status;
    }

    public String getType() { return type; }

    public String getOperator() { return operator; }

    public String getCountry() { return country; }

    public String getTechnology() { return technology; }

    public String getRoaming() { return roaming; }

    public String getStatus() { return status; }

    @Override
    public String toString() {
        return "DeviceInfo(" +
                "type='" + type + '\'' +
                ", operator='" + operator + '\'' +
                ", country='" + country + '\'' +
                ", technology='" + technology + '\'' +
                ", roaming='" + roaming + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
