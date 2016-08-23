package com.tozny.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.tozny.sdk.internal.ToznyProtocol;
import com.tozny.sdk.user.Challenge;
import com.tozny.sdk.user.Result;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Main entry point to the Tozny API's userland calls
 *
 * To make API calls, you will need a Tozny Realm Key ID, which can be
 * obtained from the Tozny Admin Console (admin.tozny.com).
 */
public class UserApi {

    private final String apiUrl;
    private final String realmKeyId;
    private final Call.Factory client;
    private final ObjectMapper mapper;

    public UserApi(String realmKeyId) {
        this("https://api.tozny.com/", realmKeyId);
    }

    public UserApi(String apiUrl, String realmKeyId) {
        this.apiUrl = apiUrl;
        this.realmKeyId = realmKeyId;
        this.client = new OkHttpClient();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(ToznyProtocol.getJacksonModule());
    }

    /**
     * Send a userland magic link challenge to a specified destination.
     *
     * @param destination Email address or phone number to which we send the challenge
     * @param context     One of "verify," "authenticate," or "enroll"
     *
     * @return The session ID and presence token representing the challenge
     */
    public Challenge linkChallenge(String destination, String context) {
        return linkChallenge(destination, context, null, null);
    }

    /**
     * Send a userland magic link challenge to a specified destination.
     *
     * @param destination Email address or phone number to which we send the challenge
     * @param context     One of "verify," "authenticate," or "enroll"
     * @param callback    Optional callback URL to which the signed OTP will be submitted once complete.
     * @param hostname    Optional hostname alias for challenge.api.tozny.com
     *
     * @return The session ID and presence token representing the challenge
     */
    public Challenge linkChallenge(String destination, String context, String callback, String hostname) {
        Map<String, String> args = new HashMap<String, String>() {{
            put("destination", destination);
            put("context", context);
            put("callback", callback);
            put("hostname", hostname);
        }};

        return this.<Challenge>rawCall("user.link_challenge", args, new TypeReference<Challenge>() {});
    }

    /**
     * Send a userland one-time password (OTP) to the specified destination.
     *
     * @param destination Email address or phone number to which we send the challenge.
     * @param type        One of "sms-otp-6," "sms-otp-8," or "email"
     * @param context     One of "verify," "authenticate," or "enroll"
     *
     * @return The session ID and presence token representing the challenge
     */
    public Challenge otpChallenge(String destination, String type, String context ) {
        return otpChallenge(destination, type, context, null);
    }

    /**
     * Send a userland one-time password (OTP) using a cached type/destination pair
     *
     * @param presence    Optional presence token representing a previously used type/destination pair
     * @param context     One of "verify," "authenticate," or "enroll"
     *
     * @return The session ID and presence token representing the challenge
     */
    public Challenge otpChallenge(String presence, String context) {
        return otpChallenge(null, null, context, presence);
    }

    /**
     * Send a userland one-time password (OTP) to the specified destination.
     *
     * @param destination Email address or phone number to which we send the challenge.
     * @param type        One of "sms-otp-6," "sms-otp-8," or "email"
     * @param context     One of "verify," "authenticate," or "enroll"
     * @param presence    Optional presence token representing a previously used type/destination pair
     *
     * @return The session ID and presence token representing the challenge
     */
    private Challenge otpChallenge(String destination, String type, String context, String presence) {
        Map<String, String> args = new HashMap<String, String>() {{
            put("type", type);
            put("context", context);
            put("destination", destination);
            put("presence", presence);
        }};

        return this.<Challenge>rawCall("user.otp_challenge", args, new TypeReference<Challenge>() {});
    }

    public boolean linkResult() {
        return false;
    }

    public Result otpResult(String otp, String session_id) {
        Map<String, String> args = new HashMap<String, String>() {{
            put("otp", otp);
            put("session_id", session_id);
        }};

        return this.<Result>rawCall("user.otp_result", args, new TypeReference<Result>() {});
    }

    public boolean challengeExchange() {
        return false;
    }

    /**
     * Send a raw method call to the Tozny API
     *
     * @param method       Name of the method to invoke
     * @param parameters   Map of the additional parameters to send with the invocationd
     * @param valueTypeRef The object type to use when deserializing the response
     * @param <T>          Descendant of ToznyAPIResponse
     *
     * @return Instance of the specified data class.
     *
     * @throws ToznyApiException if an I/O or protocol error occurs
     */
    private <T extends ToznyApiResponse<?>> T rawCall(
            String method,
            Map<String, String> parameters,
            TypeReference valueTypeRef) throws ToznyApiException {

        FormBody.Builder requestBuilder = new FormBody.Builder()
                .add("method", method)
                .add("realm_key_id", realmKeyId);

        for(Map.Entry<String, String> entry: parameters.entrySet()) {
            if ( entry.getValue() != null ) {
                requestBuilder.add(entry.getKey(), entry.getValue());
            }
        }

        RequestBody params = requestBuilder.build();

        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Accept", "application/json")
                .post(params)
                .build();

        Response response;
        try {
            response = client.newCall(request).execute();
        }
        catch (IOException e) {
            String message = "While calling " + method + ".";
            throw new ToznyApiException(message, e);
        }

        if (! response.isSuccessful()) {
            response.body().close();
            String message = response.message() != null ? response.message() : "";

            throw new ToznyApiException(Collections.singletonList(
                    new ToznyApiException.ToznyApiError(
                            response.code(), message, method
                    )
            ));
        }

        T apiResponse;
        try {
            apiResponse = mapper.readValue(response.body().byteStream(), valueTypeRef);
        }
        catch (JsonProcessingException e) {
            String message = "While calling "+method+".";
            throw new ToznyApiException(message, e);
        }
        catch (IOException e) {
            String message = "While calling "+method+".";
            throw new ToznyApiException(message, e);
        }
        finally {
            response.body().close();
        }

        if (apiResponse.isError()) {
            throw apiResponse.getException();
        }
        else {
            return apiResponse;
        }
    }
}
