package com.tozny.sdk.realm.methods.users_get;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tozny.sdk.ToznyApiException.ToznyApiError;
import com.tozny.sdk.ToznyApiResponse;
import com.tozny.sdk.realm.User;

import java.util.List;
import java.util.Map;

public class UsersGetResponse extends ToznyApiResponse<Map<String,User>> {

    @JsonCreator
    public UsersGetResponse(
            @JsonProperty("return")  String ret,
            @JsonProperty("results") Map<String,User> results,
            @JsonProperty("count")   Integer count,
            @JsonProperty("total")   Integer total,
            @JsonProperty("errors")  List<ToznyApiError> errors) {
        super(ret, null, results, count, total, errors);
    }
}
