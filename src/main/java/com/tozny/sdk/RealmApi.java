package com.tozny.sdk;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tozny.sdk.internal.ProtocolHelpers;
import com.tozny.sdk.internal.ToznyProtocol;
import com.tozny.sdk.realm.*;
import com.tozny.sdk.realm.methods.AuthStatus;
import com.tozny.sdk.realm.methods.check_valid_login.CheckValidLoginRequest;
import com.tozny.sdk.realm.methods.create_challenge.CreateChallengeRequest;
import com.tozny.sdk.realm.methods.dynamic_auth_challenge.DynamicAuthChallengeRequest;
import com.tozny.sdk.realm.methods.link_challenge.LinkChallengeRequest;
import com.tozny.sdk.realm.methods.otp_challenge.OTPChallengeRequest;
import com.tozny.sdk.realm.methods.question_challenge.QuestionChallengeRequest;
import com.tozny.sdk.realm.methods.user_exists.UserExistsRequest;
import com.tozny.sdk.realm.methods.user_add.UserAddResponse;
import com.tozny.sdk.realm.methods.user_add.UserAddRequest;
import com.tozny.sdk.realm.methods.user_device_add.UserDeviceAddResponse;
import com.tozny.sdk.realm.methods.user_device_add.UserDeviceAddRequest;
import com.tozny.sdk.realm.methods.user_get.UserGetRequest;
import com.tozny.sdk.realm.methods.user_get.UserGetResponse;
import com.tozny.sdk.realm.methods.user_push.UserPushRequest;
import com.tozny.sdk.realm.methods.users_get.UsersGetRequest;
import com.tozny.sdk.realm.methods.users_get.UsersGetResponse;

import javax.annotation.Nullable;

/**
 * Main entry point to the Tozny API's Realm calls.
 * Realm cals are typically used by Service Providers, web-site owners, or realm operators.
 * To make any Realm API calls, you will need a Tozny Realm Key ID and a Realm Key Secret,
 * which can be obtained from the Tozny Admin Console (admin.tozny.com).
 */
public class RealmApi {

    private final RealmConfig config;
    private final ToznyProtocol protocol;

    /**
     * Builds a RealmApi instance.
     *
     * @param config the Realm's KeyID and Secret to use on all realm calls
     */
    public RealmApi(RealmConfig config) {
        this(config, new ToznyProtocol(config));
    }

    /**
     * Builds a RealmApi instance
     *
     * @param toznyProtocol configured with customized settings
     */
    public RealmApi(RealmConfig config, ToznyProtocol toznyProtocol) {
        this.config = config;
        this.protocol = toznyProtocol;
    }

    /**
     * Verify that the signedData payload's signature matches the given signature.
     * @param signedData the payload to compute a signature with
     * @param signature the proposed signature of the signedData payload.
     * @return True if the signedData, along with the realm key's secret, produced a signature that matched the given signature.
     * @throws ToznyApiException If an error occurs while coputing the signature on the given signedData.
     */
    public boolean verifyLogin (String signedData, String signature) throws ToznyApiException {
        try {
            return ProtocolHelpers.checkSignature(this.config.realmSecret, signature, signedData);
        } catch (InvalidKeyException e) {
            throw new ToznyApiException("While attempting to verify login", e);
        } catch (NoSuchAlgorithmException e) {
            throw new ToznyApiException("While attempting to verify login", e);
        }
    }

    /**
     * Add this user to the given realm.
     *
     * @param defer wether to use deferred enrollment. Defaults to "false"
     * @param metadata arbitrary key-value pairs; some keys have special
     * signifigance, such as "tozny_email"
     */
    public UserAddResponse userAdd(
            boolean defer, Map<String,String> metadata) throws ToznyApiException {
        UserAddRequest req = new UserAddRequest(defer, metadata, null);
        UserAddResponse resp = protocol.<UserAddResponse>dispatch(
                req, new TypeReference<UserAddResponse>() {});
        if (resp.isError()) {
            throw resp.getException();
        }
        else {
            return resp;
        }
    }

    /**
     * Add this user to the given realm and associate with the given email
     * address.
     *
     * @param defer wether to use deferred enrollment.
     * @param email address to associate with user
     */
    public UserAddResponse userAddWithEmail(
            boolean defer, String email) throws ToznyApiException {
        UserAddRequest req = new UserAddRequest(defer, email, null);
        UserAddResponse resp = protocol.<UserAddResponse>dispatch(
                req, new TypeReference<UserAddResponse>() {});
        if (resp.isError()) {
            throw resp.getException();
        }
        else {
            return resp;
        }
    }

    /**
     * Initiate process of registering a new authentication device for a given
     * user. The response includes a secret enrollment URL, that the user can
     * activate on the new device to finalize registration.
     *
     * @param userId of the user account to add a device to
     * @return an instance of <code>UserDeviceAddResponse</code>
     * @throws ToznyApiException If the user does not exist, or if  an error occurs either in communicating, or marshaling a <code>UserDeviceAddResponse</code> response from the Tozny API.
     */
    public UserDeviceAddResponse userDeviceAdd(String userId) throws ToznyApiException {
        UserDeviceAddRequest req = new UserDeviceAddRequest(userId);
        UserDeviceAddResponse resp = protocol.<UserDeviceAddResponse>dispatch(
                req, new TypeReference<UserDeviceAddResponse>() {});
        if (resp.isError()) {
            throw resp.getException();
        }
        else {
            return resp;
        }
    }

    /**
     * Calls 'realm.user_get' to retrieve the user identified by the given userId.
     * @param userId the id of the user to retrieve.
     * @return an instance of <code>User</code>
     * @throws ToznyApiException If a user does not exist, or if an error occurs either in communicating, or marshaling a <code>User</code> response from the Tozny API.
     */
    public User userGet (String userId) throws ToznyApiException {
        UserGetResponse resp = protocol.<UserGetResponse>dispatch(
                new UserGetRequest(userId, null),
                new TypeReference<UserGetResponse>() {});
        return resp.getResult();
    }

    /**
     * Calls 'realm.user_get' to retrieve the user identified by the given email address.
     * @param email the email address of the user to retrieve.
     * @return an instance of <code>User</code>
     * @throws ToznyApiException If a user does not exist, or if an error occurs either in communicating, or marshaling a <code>User</code> response from the Tozny API.
     */
    public User userGetByEmail (String email) throws ToznyApiException {
        UserGetResponse resp = protocol.<UserGetResponse>dispatch(
                new UserGetRequest(null, email),
                new TypeReference<UserGetResponse>() {});
        return resp.getResult();
    }

    /**
     * Calls 'realm.users_get' to retrieve a collection of users given the specified query parameters.
     *
     * @param term A single search term applied against searchable meta fields as defined on the realm.
     * @param meta_advanced A collection of meta fields against which to search. Must include maps of 'field' to {field name}, 'comparator' to {comparison type}, 'value' to {search value}.
     * @param user_ids List of user IDs to return.
     * @param rows Total number of results to return
     * @return Collection of <code>User</code> instances
     * @throws ToznyApiException If an error occurs either in communicating, or marshaling a <code>User</code> response from the Tozny API.
     */
    public Map<String,User> usersGet (String term, List<Map<String,String>> meta_advanced, List<String> user_ids, Integer rows) throws ToznyApiException {
        UsersGetResponse resp = protocol.<UsersGetResponse>dispatch(
                new UsersGetRequest(term, meta_advanced, user_ids, rows),
                new TypeReference<UsersGetResponse>() {});
        return resp.getResult();
    }

    /**
     * Calls 'realm.users_get' to retrieve a collection of users given the specified query parameters.
     *
     * @param term A single search term applied against searchable meta fields as defined on the realm.
     * @return Collection of <code>User</code> instances
     * @throws ToznyApiException If an error occurs either in communicating, or marshaling a <code>User</code> response from the Tozny API.
     */
    public Map<String,User> usersGetByTerm (String term) throws ToznyApiException {
        UsersGetResponse resp = protocol.<UsersGetResponse>dispatch(
                new UsersGetRequest(term, null, null, null),
                new TypeReference<UsersGetResponse>() {});
        return resp.getResult();
    }

    /**
     * Calls 'realm.users_get' to retrieve a collection of users given the specified query parameters.
     *
     * @param meta_advanced A collection of meta fields against which to search. Must include maps of 'field' to {field name}, 'comparator' to {comparison type}, 'value' to {search value}.
     * @return Collection of <code>User</code> instances
     * @throws ToznyApiException If an error occurs either in communicating, or marshaling a <code>User</code> response from the Tozny API.
     */
    public Map<String,User> usersGetByMetaAdvanced (List<Map<String,String>> meta_advanced) throws ToznyApiException {
        UsersGetResponse resp = protocol.<UsersGetResponse>dispatch(
                new UsersGetRequest(null, meta_advanced, null, null),
                new TypeReference<UsersGetResponse>() {});
        return resp.getResult();
    }

    /**
     * Calls 'realm.users_get' to retrieve a collection of users given the specified query parameters.
     *
     * @param user_ids List of user IDs to return.
     * @return Collection of <code>User</code> instances
     * @throws ToznyApiException If an error occurs either in communicating, or marshaling a <code>User</code> response from the Tozny API.
     */
    public Map<String,User> usersGetByUserIDs (List<String> user_ids) throws ToznyApiException {
        UsersGetResponse resp = protocol.<UsersGetResponse>dispatch(
                new UsersGetRequest(null, null, user_ids, null),
                new TypeReference<UsersGetResponse>() {});
        return resp.getResult();
    }

    /**
     * Calls 'realm.user_exists' to test if the user identified by the given userId exists in the realm.
     * @param userId the id of the user to test the existence of.
     * @return true if the user exists
     * @throws ToznyApiException If an error occurs either in communicating, or marshaling a response from the Tozny API.
     */
    public boolean userExists(String userId) throws ToznyApiException {
        UserExistsRequest req = new UserExistsRequest(userId, null);
        return dispatchUserExists(req);
    }

    /**
     * Calls 'realm.user_exists' to test if the user identified by the given email address exists in the realm.
     * @param email the email address of the user to retrieve.
     * @return true if the user exists
     * @throws ToznyApiException If an error occurs either in communicating, or marshaling a response from the Tozny API.
     */
    public Boolean userExistsByEmail(String email) throws ToznyApiException {
        UserExistsRequest req = new UserExistsRequest(null, email);
        return dispatchUserExists(req);
    }

    private boolean dispatchUserExists(UserExistsRequest req) throws ToznyApiException {
        ToznyApiResponse<Boolean> resp = protocol.<ToznyApiResponse<Boolean>>dispatch(
                req, new TypeReference<ToznyApiResponse<Boolean>>() {});
        String ret = resp.getReturn();
        return ret != null && ret.equals("true");
    }

    private boolean dispatchUserPush(UserPushRequest request) throws ToznyApiException {
        ToznyApiResponse<Boolean[]> response = protocol.<ToznyApiResponse<Boolean[]>>dispatch(
                request, new TypeReference<ToznyApiResponse<Boolean[]>>() {});

        Boolean[] results = response.getResult();
        return results != null && results.length >= 1 && results[0] != null && results[0];
    }

    /**
     * Calls 'realm.question_challenge' to create a new question challenge session.
     *
     * @param question the question to ask the user.
     * @return an instance of Session that represents a new challenge session.
     * @throws ToznyApiException If an error occurs either in communicating, or marshaling a response from the Tozny API.
     */
    public Session questionChallenge (String question) throws ToznyApiException {
        return questionChallenge(question, null);
    }

    /**
     * Calls 'realm.question_challenge' to create a new question challenge session and associates it with the given userId.
     *
     * @param question the question to ask the user.
     * @param userId the user to associate a response from.
     * @return an instance of Session that represents a new challenge session.
     * @throws ToznyApiException If an error occurs either in communicating, or marshaling a response from the Tozny API.
     */
    public Session questionChallenge (String question, String userId) throws ToznyApiException {
        QuestionChallengeRequest req = new QuestionChallengeRequest(question, userId);
        return protocol.<Session>dispatch(req, new TypeReference<Session>() {});
    }

    /**
     * Calls 'realm.link_challenge' to create a new magic link challenge session.
     *
     * @param destination Email address or phone number to which we send a challenge
     * @param endpoint    URL endpoint base for the generated magic link
     * @param lifespan    Number of seconds for which the magic link should be valid
     * @param context     One of "verify," "authenticate," or "enroll"
     * @param send        Flag whether or not to send the email. If false, the OTP URL will be returned.
     * @param data        Optional serialized (JSON-encoded) realm data
     *
     * @return Session instance representing the magic link challenge
     *
     * @throws ToznyApiException If an error occurs either in communicating, or marshaling a response from the Tozny API.
     */
    public LinkChallenge linkChallenge (String destination, String endpoint, Integer lifespan, String context, Boolean send, String data) throws ToznyApiException {
        LinkChallengeRequest req = new LinkChallengeRequest(destination, endpoint, lifespan, context, send, data);
        return protocol.<LinkChallenge>dispatch(req, new TypeReference<LinkChallenge>() {});
    }

    /**
     * Calls `realm.otp_challenge` to create a new OTP challenge session
     * @param type One of "sms-otp-6," "sms-otp-8," or "email"
     * @param context One of "verify," "authenticate," or "enroll"
     * @param destination Email address or phone number to which we send a challenge
     * @param presence Optional presence token to re-use a previous destination/type pair
     * @param data Optional serialized (JSON-encoded) realm data
     *
     * @return Session instance representing the OTP challenge
     *
     * @throws ToznyApiException If an error occurs either in communicating, or marshaling a response from the Tozny API.
     */
    public OTPChallenge otpChallenge(String type, String context, String destination, String presence, String data) throws ToznyApiException {
        OTPChallengeRequest req = new OTPChallengeRequest(type, context, destination, presence, data);
        return protocol.<OTPChallenge>dispatch(req, new TypeReference<OTPChallenge>() {});
    }

    /**
     * Calls `realm.user_push` to push an authentication challenge to a given user.
     *
     * One of `userId`, `email`, or `username` must be supplied.
     *
     * @param sessionId The session whose owner we are testing.
     * @param userId    Optional ID of the user to authenticate
     * @param email     Optional email of the user to authenticate
     * @param username  Optional username of the user to authenticate
     *
     * @return true on success
     */
    public Boolean userPush(String sessionId, @Nullable String userId, @Nullable String email, @Nullable String username) {
        UserPushRequest request = new UserPushRequest(sessionId, userId, email, username);
        return dispatchUserPush(request);
    }

    /**
     * Calls `realm.user_push` to push an authentication challenge to a given user.
     *
     * @param sessionId The session whose owner we are testing.
     * @param userId    ID of the user to authenticate
     *
     * @return true on success
     */
    public Boolean userPushById(String sessionId, String userId) {
        UserPushRequest request = new UserPushRequest(sessionId, userId, null, null);
        return dispatchUserPush(request);
    }

    /**
     * Calls `realm.user_push` to push an authentication challenge to a given user.
     *
     * @param sessionId The session whose owner we are testing.
     * @param email     Email of the user to authenticate
     *
     * @return true on success
     */
    public Boolean userPushByEmail(String sessionId, String email) {
        UserPushRequest request = new UserPushRequest(sessionId, null, email, null);
        return dispatchUserPush(request);
    }

    /**
     * Calls `realm.user_push` to push an authentication challenge to a given user.
     *
     * @param sessionId The session whose owner we are testing.
     * @param username  Username of the user to authenticate
     *
     * @return true on success
     */
    public Boolean userPushByUsername(String sessionId, String username) {
        UserPushRequest request = new UserPushRequest(sessionId, null, null, username);
        return dispatchUserPush(request);
    }

    /**
     * Calls `realm.create_challenge` to create an authentication challenge for a given user.
     *
     * @param userId The ID of the user to authenticate
     *
     * @return Data about the challenge session itself and the device being used for authentication
     */
    public ChallengeSession createChallengeSession(String userId) throws ToznyApiException {
        CreateChallengeRequest request = new CreateChallengeRequest(userId);
        return protocol.<ChallengeSession>dispatch(request, new TypeReference<ChallengeSession>() {});
    }

    /**
     * Calls `realm.dynamic_auth_challenge` to authenticate a given challenge session dynamically.
     *
     * @param sessionId The ID of the session to authenticate.
     *
     * @return Information about the session's authentication status
     */
    public AuthStatus dynamicAuthChallenge(String sessionId) throws ToznyApiException {
        DynamicAuthChallengeRequest request = new DynamicAuthChallengeRequest(sessionId);
        return protocol.<AuthStatus>dispatch(request, new TypeReference<AuthStatus>() {});
    }

    /**
     * Calls 'realm.check_valid_login' to test if the given userId owns the given sessionId.
     *
     * @param userId The the proposed owner of the session
     * @param sessionId The session whos owner we are testing.
     * @return True if the given user owen the given session.
     * @throws ToznyApiException  If the session or user does not exist, or if an error occurs either in communicating, or marshaling a response from the Tozny API.
     */
    public boolean checkValidLogin (String userId, String sessionId) throws ToznyApiException {
        CheckValidLoginRequest req = new CheckValidLoginRequest(userId, sessionId);
        ToznyApiResponse<Boolean> resp = protocol.<ToznyApiResponse<Boolean>>dispatch(
                req, new TypeReference<ToznyApiResponse<Boolean>>() {});
        return resp.getResult();
    }

}
