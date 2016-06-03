# Tozny Java SDK

Library interface to the [Tozny][] authentication service. Use this Library to:

- enroll or manage users
- verify logins
- manage user authentication devices
- push challenge questions to user devices

[Tozny]: https://tozny.com/

[![Release](https://jitpack.io/v/tozny/sdk-java.svg)](https://jitpack.io/#tozny/sdk-java)

## API Documentation

https://jitpack.io/com/github/tozny/sdk-java/2.1.6/javadoc/

## Installing

The SDK can be installed via a Maven artifact, via the JitPack repository.
If you use Gradle, add this configuration to your `build.gradle` configuration:

```groovy
repositories {
  jcenter()
  maven { url "https://jitpack.io" }
}
dependencies {
  compile 'com.github.tozny:sdk-java:2.1.6'
}
```

## Usage

Create an instance of `RealmApi` to make API calls to your Tozny realm.

```java
import com.tozny.sdk.RealmApi;
import com.tozny.sdk.realm.RealmConfig;

// ...

RealmConfig config = new RealmConfig("YOUR_REALM_KEY_ID", "YOUR_REALM_KEY_SECRET");
RealmApi realm = new RealmApi(config);
```

Realm key id and secret are found in the realm key management section in the Tozny admin dashboard.

`RealmApi` exposes a number of methods interact with your realm. Many of these
translate to HTTP calls to the Tozny API. For details, see the [API documentation][].

[API documentation]: https://jitpack.io/com/github/tozny/sdk-java/2.1.6/javadoc/

For a working example of SDK use, see the [secretmessage][] example app. In
particular, the [`SessionResource`][SessionResource] class demonstrates how to
verify login requests.

[secretmessage]: examples/secretmessage
[SessionResource]: examples/secretmessage/src/main/java/com/tozny/sdk/example/secretmessage/SessionResource.java
