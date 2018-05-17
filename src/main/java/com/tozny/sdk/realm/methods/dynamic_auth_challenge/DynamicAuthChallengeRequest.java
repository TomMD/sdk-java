package com.tozny.sdk.realm.methods.dynamic_auth_challenge;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tozny.sdk.ToznyApiRequest;

import javax.annotation.Nullable;

/**
 * Authenticate a given challenge session dynamically, using push if possible and falling back to SMS/TTS.
 */
@JsonAutoDetect(getterVisibility=JsonAutoDetect.Visibility.NONE)
public class DynamicAuthChallengeRequest implements ToznyApiRequest {

    @JsonProperty(required=true) private String method = "realm.dynamic_auth_challenge";
    @JsonProperty private String session_id;

    public DynamicAuthChallengeRequest(@Nullable String session_id) {
        this.session_id = session_id;
    }

    public String getMethod() {
        return method;
    }

    @Nullable
    public String getSessionId() {
        return session_id;
    }

}
