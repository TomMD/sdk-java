package com.tozny.sdk.realm.methods.create_challenge;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tozny.sdk.ToznyApiRequest;

import javax.annotation.Nullable;

/**
 * Constructs a Request for invoking the "realm.create_challenge" method.
 */
@JsonAutoDetect(getterVisibility=JsonAutoDetect.Visibility.NONE)
public class CreateChallengeRequest implements ToznyApiRequest {

    @JsonProperty(required=true) private String method = "realm.create_challenge";
    @JsonProperty private String user_id;

    public CreateChallengeRequest(@Nullable String user_id) {
        this.user_id = user_id;
    }

    public String getMethod() {
        return method;
    }

    @Nullable
    public String getUserId() {
        return user_id;
    }

}
