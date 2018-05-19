package com.tozny.sdk.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.tozny.sdk.ToznyApiException;
import com.tozny.sdk.ToznyApiResponse;
import com.tozny.sdk.ToznyApiRequest;
import com.tozny.sdk.realm.RealmConfig;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Low-level methods for constructing and dispatching API requests.
 */
public class ToznyProtocol {

    private final RealmConfig realmConfig;
    private final ObjectMapper mapper;
    private final Call.Factory client;

    /**
     * @param realmConfig provides realm credentials and API URL.
     * @param httpClient the OkHttpClient instance to make calls out to Tozny's API servers with (or another type that implements `Call.Factory`).
     * @param objectMapper the Jackson ObjectMapper instance to use when constructing or manipulating JSON structures.
     */

    public ToznyProtocol(RealmConfig realmConfig, Call.Factory httpClient, ObjectMapper objectMapper) {
        this.realmConfig = realmConfig;
        this.mapper = objectMapper;
        this.client = httpClient;
        this.mapper.registerModule(getJacksonModule());
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
    }

    public ToznyProtocol(RealmConfig realmConfig) {
        this(realmConfig, getDefaultHttpClient(), new ObjectMapper());
    }

    /**
     * Sends an HTTP request based on the given API request object.
     * The Response is them passed to an instance of the given dataClass for
     * marshaling into the result type.
     *
     * @param req The Tozny API Request object
     * @param valueTypeRef The type to use when deserializing a JSON response.
     * @param <T> A descendant of ToznyApiResponse, which will provide helpers for managing ToznyAPI errors.
     * @return an instance of the given dataClass.
     * @throws ToznyApiException if an I/O or protocol error occurs during the API call
     */
    public <T extends ToznyApiResponse<?>> T dispatch(
            ToznyApiRequest req,
            TypeReference valueTypeRef) throws ToznyApiException {

        String signedData;
        String signature;
        try {
            RequestMeta meta = new RequestMeta(
                    ProtocolHelpers.getNonce(),
                    ProtocolHelpers.getExpires(),
                    realmConfig.realmKeyId.value
                    );
            byte[] json = toJson(req, meta);
            signedData = ProtocolHelpers.base64UrlEncode(json);
            signature  = ProtocolHelpers.sign(realmConfig.realmSecret, signedData);
        }
        catch (NoSuchAlgorithmException e) {
            throw new ToznyApiException("SHA1PRNG algorithm is not available.", e);
        }
        catch (InvalidKeyException e) {
            throw new ToznyApiException("There was a problem initializing HmacSHA256.", e);
        }

        RequestBody params = new FormBody.Builder()
            .add("signed_data", signedData)
            .add("signature", signature)
            .build();

        Request request = new Request.Builder()
            .url(realmConfig.apiUrl)
            .header("Accept", "application/json")
            .post(params)
            .build();

        Response response;
        try {
            response = client.newCall(request).execute();
        }
        catch (IOException e) {
            String message = "While calling "+req+".";
            throw new ToznyApiException(message, e);
        }

        if (!response.isSuccessful()) {
            response.body().close();
            String message = response.message() != null ? response.message() : "";
            String location = "";  // TODO
            throw new ToznyApiException(Collections.singletonList(
                        new ToznyApiException.ToznyApiError(
                            response.code(), message, location
                            )
                        ));
        }

        T apiResponse;
        try {
            apiResponse = mapper.readValue(response.body().byteStream(), valueTypeRef);
        }
        catch (JsonProcessingException e) {
            String message = "While calling "+req+".";
            throw new ToznyApiException(message, e);
        }
        catch (IOException e) {
            String message = "While calling "+req+".";
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

    private byte[] toJson(ToznyApiRequest req, RequestMeta meta) throws ToznyApiException {
        ObjectNode merged = mergeJson(req, meta);
        try {
            return mapper.writeValueAsBytes(merged);
        }
        catch (JsonProcessingException e) {
            String message = "While calling "+req+".";
            throw new ToznyApiException(message, e);
        }
    }

    private ObjectNode mergeJson(Object target, Object source) {
        ObjectNode mainNode = mapper.valueToTree(target);
        ObjectNode updateNode = mapper.valueToTree(source);
        Iterator<String> fieldNames = updateNode.fieldNames();

        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode value = updateNode.get(fieldName);
            mainNode.set(fieldName, value);
        }

        return mainNode;
    }

    public static class RequestMeta {
        @JsonProperty private String nonce;
        @JsonProperty private String expires_at;
        @JsonProperty private String realm_key_id;

        public RequestMeta(String nonce, String expires_at, String realm_key_id) {
            this.nonce = nonce;
            this.expires_at = expires_at;
            this.realm_key_id = realm_key_id;
        }
    }

    private static Call.Factory getDefaultHttpClient() {
        return new OkHttpClient();
    }

    private static Module getJacksonModule() {
        Version version = new Version(1, 0, 0, null, "com.github.tozny", "tozny-sdk");
        SimpleModule toznyModule = new SimpleModule("ToznyModule", version);
        toznyModule.addDeserializer(Date.class, new DateDeserializer());
        toznyModule.addSerializer(Map.class, new Base64Serializer());
        return toznyModule;
    }

}
