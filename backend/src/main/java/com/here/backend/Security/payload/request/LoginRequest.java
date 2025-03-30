
package com.here.backend.Security.payload.request;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
	@NotBlank
  private String name;

	@NotBlank
	private String password;

	public String getName() {
		return name;
	}

	public LoginRequest(@NotBlank String username, @NotBlank String password) {
		this.name = username;
		this.password = password;
	}

	public void getName(String username) {
		this.name = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}