package com.tozny.sdk;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.tozny.sdk.realm.RealmConfig;
import com.tozny.sdk.realm.Session;
import com.tozny.sdk.realm.User;
import com.tozny.sdk.realm.config.ToznyRealmKeyId;
import com.tozny.sdk.realm.config.ToznyRealmSecret;

/**
 * Unit test for simple App.
 */
public class RealmApiTest {
    private RealmConfig realmConfig;
    private RealmApi realmApi;

    // Test variables taken from the environment.
    private String realmKeyId;
    private String realmSecret;
    private String userId;
    private String userEmail;
    private String userPhone;
    private String badPhone;

    @Before
    public void init() throws Exception {
        String testPropertiesFile = System.getProperty("testProperties");
        if (testPropertiesFile == null) {
            testPropertiesFile = "test.properties";
        }

        InputStream is = new FileInputStream(testPropertiesFile);
        Properties props = new Properties();
        props.load(is);

        this.realmKeyId = props.getProperty("realmKey");;
        this.realmSecret = props.getProperty("realmSecret");
        this.userId = props.getProperty("userId");
        this.userEmail = props.getProperty("userEmail");
        this.userPhone = props.getProperty("userPhone");

        this.realmConfig = new RealmConfig(
            new ToznyRealmKeyId(this.realmKeyId),
            new ToznyRealmSecret(this.realmSecret)
        );

        this.realmApi = new RealmApi(realmConfig);
    }

    @Test
    public void testUserGet() throws IOException {
        User user = this.realmApi.userGet(this.userId);
        assertEquals(this.userEmail, user.getMeta().get("email"));
        user = this.realmApi.userGetByEmail(this.userEmail);
        assertEquals(this.userId, user.getUserId());
    }

    @Test
    public void testUsersGet() throws IOException {
        Map<String,User> users = this.realmApi.usersGet(null, null, null, 1);
        assertTrue(users.size() > 0);
    }

    @Test
    public void testUsersGetByTerm() throws IOException {
        Map<String,User> users = this.realmApi.usersGetByTerm(this.userEmail);
        assertTrue(users.size() == 1);
        assertEquals(this.userId, users.keySet().toArray()[0]);
    }

    @Test
    public void testUsersGetByMetaAdvanced() throws IOException {
        Map<String, String> phone = new HashMap<String, String>();
        phone.put("field", "phone");
        phone.put("operator", "is_exactly");
        phone.put("value", this.userPhone);

        List<Map<String, String>> queries = new ArrayList<Map<String, String>>();
        queries.add(phone);

        Map<String,User> users = this.realmApi.usersGetByMetaAdvanced(queries);
        assertTrue(users.size() == 1);
        assertEquals(this.userId, users.keySet().toArray()[0]);
    }

    @Test
    public void testUsersGetByMetaAdvanced_NoUser() throws IOException {
        Map<String, String> phone = new HashMap<String, String>();
        phone.put("field", "phone");
        phone.put("operator", "is_exactly");
        phone.put("value", this.badPhone);

        List<Map<String, String>> queries = new ArrayList<Map<String, String>>();
        queries.add(phone);

        Map<String,User> users = this.realmApi.usersGetByMetaAdvanced(queries);
        assertTrue(users.size() == 0);
    }

    @Test
    public void testUsersGetById() throws IOException {
        Map<String,User> users = this.realmApi.usersGetByUserIDs(Arrays.asList(this.userId));
        assertTrue(users.size() == 1);
        assertEquals(this.userId, users.keySet().toArray()[0]);
    }

    @Test
    public void testUserExists() throws IOException {
        boolean exists = this.realmApi.userExists(this.userId);
        assertTrue(exists);
        exists = this.realmApi.userExistsByEmail(this.userEmail);
        assertTrue(exists);
    }

    @Test
    public void testQuestionChallenge() throws IOException {
        Session session = this.realmApi.questionChallenge("Do you like Java?");

        assertNotNull(session.getChallenge());
        assertNotNull(session.getRealmKeyId());
        assertNotNull(session.getSessionId());
        assertNotNull(session.getQrUrl());
        assertNotNull(session.getMobileUrl());
        assertNotNull(session.getPresence());
        assertNotNull(session.getCreatedAt());
    }

    @Test
    public void testCheckValidLogin() throws IOException {
        try {
            boolean isValid = this.realmApi.checkValidLogin("bad", "worse");
            fail("bad session and user_id values should throw an exception");
        }
        catch (ToznyApiException e ) { assertTrue(true); }
    }
}
