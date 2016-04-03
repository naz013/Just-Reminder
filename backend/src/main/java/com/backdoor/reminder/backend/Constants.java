package com.backdoor.reminder.backend;

/**
 * Contains the client IDs and scopes for allowed clients consuming the helloworld API.
 */
public class Constants {
  public static final String WEB_CLIENT_ID = "257660336951-hmpoomki6j6v4ksvqa3t0ojuaije8db2.apps.googleusercontent.com";
  public static final String ANDROID_CLIENT_ID = "257660336951-urfa5m6oqiuqapa4rflaq6cahk9lsu3t.apps.googleusercontent.com";
  public static final String IOS_CLIENT_ID = "replace this with your iOS client ID";
  public static final String ANDROID_AUDIENCE = WEB_CLIENT_ID;

  public static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
}
