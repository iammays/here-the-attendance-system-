package com.here.backend.Security.payload.request;

import jakarta.validation.constraints.*;

public class SignupRequest {
  public SignupRequest() { }

  @NotBlank
  @Size(min = 3, max = 20,message = "The Size of username mustbe between 3 and 20")
  private String name;

  @NotBlank
  @Size(max = 50)
  @Email
  private String email;

  @Size(min = 6, max = 40, message = "The size must be between 6 and 40!")
  @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{6,40}$",
  message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, one special character, and be 6-40 characters long!")
  private String password;

  public SignupRequest(@NotBlank @Size(min = 3, max = 20) String username,
  @NotBlank @Size(max = 50) @Email String email, @NotBlank @Size(min = 6, max = 40) String password) {
    this.name = username;
    this.email = email;
    this.password = password;
  }

  public String getName() {
    return name;
  }

  public void getName(String username) {
    this.name = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}