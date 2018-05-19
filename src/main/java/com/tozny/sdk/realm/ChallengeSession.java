package com.tozny.sdk.realm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tozny.sdk.ToznyApiException.ToznyApiError;
import com.tozny.sdk.ToznyApiResponse;
import com.tozny.sdk.realm.config.ToznyRealmKeyId;

import java.util.Date;
import java.util.List;

/**
 * Holds the Tozny API response for a Session challenge.
 *
 * An example realm.question_challenge response:
 * <pre>
 * {@code
 * {
 *     "return": "ok",
 *     "status_code": 200,
 *     "session_id": "acd8280fb2ef27d3c876c1aabe10ec3ee7ba0372ca3858125909e05b3f3324da",
 *     "realm_key_id": "sid_52fa6d0a3a290",
 *     "device_info": [],
 *     "risk_factor: "high",
 *     "created_at": 1425670790
 * }
 * }
 * </pre>
 */
public class ChallengeSession extends ToznyApiResponse<Void> {

    private final String realm_key_id;
    private final String session_id;
    private final DeviceInfo device_info;
    private final String risk_factor;
    private final Date created_at;

    @JsonCreator
    public ChallengeSession(
            @JsonProperty("return")  String ret,
            @JsonProperty("count")   Integer count,
            @JsonProperty("total")   Integer total,
            @JsonProperty("errors")  List<ToznyApiError> errors,

            @JsonProperty("realm_key_id") String realm_key_id,
            @JsonProperty("session_id")   String session_id,
            @JsonProperty("device_info")  DeviceInfo device_info,
            @JsonProperty("risk_factor")  String risk_factor,
            @JsonProperty("created_at")   Date created_at) {
        super(ret, null, null, count, total, errors);

        this.realm_key_id = realm_key_id;
        this.session_id = session_id;
        this.device_info = device_info;
        this.risk_factor = risk_factor;
        this.created_at = created_at;
    }


    public ToznyRealmKeyId getRealmKeyId() {
        return new ToznyRealmKeyId(realm_key_id);
    }

    public String getSessionId() {
        return session_id;
    }

    public DeviceInfo getDeviceInfo() {
        return device_info;
    }

    public String getRiskFactor() {
        return risk_factor;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    @Override
    public String toString() {
        return "Session{" +
                "realm_key_id='" + realm_key_id + '\'' +
                ", session_id='" + session_id + '\'' +
                ", device_info=" + device_info +
                ", risk_factor=" + risk_factor +
                ", created_at=" + created_at +
                '}';
    }

}
