package com.tozny.sdk.realm.methods;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tozny.sdk.ToznyApiException.ToznyApiError;
import com.tozny.sdk.ToznyApiResponse;
import com.tozny.sdk.realm.DeviceInfo;
import com.tozny.sdk.realm.config.ToznyRealmKeyId;

import java.util.Date;
import java.util.List;

/**
 * Holds the Tozny API response for a dynamically-authenticated session.
 *
 * An example realm.dynamic_auth_challenge response:
 * <pre>
 * {@code
 * {
 *     "return": "ok",
 *     "status_code": 200,
 *     "status": "push-pending"
 * }
 * }
 * </pre>
 */
public class AuthStatus extends ToznyApiResponse<Void> {

    private final String realm_key_id;
    private final String status;

    @JsonCreator
    public AuthStatus(
            @JsonProperty("return")  String ret,
            @JsonProperty("count")   Integer count,
            @JsonProperty("total")   Integer total,
            @JsonProperty("errors")  List<ToznyApiError> errors,

            @JsonProperty("realm_key_id") String realm_key_id,
            @JsonProperty("status")       String status) {
        super(ret, null, null, count, total, errors);

        this.realm_key_id = realm_key_id;
        this.status = status;
    }


    public ToznyRealmKeyId getRealmKeyId() {
        return new ToznyRealmKeyId(realm_key_id);
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "AuthStatus{" +
                "realm_key_id='" + realm_key_id + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

}
