package com.here.backend.Security.payload.response;

public class JwtResponse {
  private String token;
  private String type = "Bearer";
  private String id;
  private String name;
  private String email;

  public JwtResponse(String accessToken, String long1, String username, String email) {
    this.token = accessToken;
    this.id = long1;
    this.name = username;
    this.email = email;
  }

  public String getAccessToken() {
    return token;
  }

  public void setAccessToken(String accessToken) {
    this.token = accessToken;
  }

  public String getTokenType() {
    return type;
  }

  public void setTokenType(String tokenType) {
    this.type = tokenType;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void getName(String username) {
    this.name = username;
}
}