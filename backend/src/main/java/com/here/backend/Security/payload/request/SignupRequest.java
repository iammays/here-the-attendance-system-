package com.here.backend.Security.payload.request;

// import java.time.LocalDate;
// import java.time.Period;
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

  // private String ProfilePic;

  // private String Bio;

  // private LocalDate DOB;

  // private String Location;

  // private int age;

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


  // public SignupRequest(@NotBlank @Size(min = 3, max = 20) String username,
  //   @NotBlank @Size(max = 50) @Email String email, @NotBlank @Size(min = 6, max = 40) String password,String ProfilePic,String Bio,LocalDate DOB,String Location) {
  //   this.username = username;
  //   this.email = email;
  //   this.password = password;
  //   this.Bio= Bio;
  //   this.ProfilePic= ProfilePic;
  //   this.DOB = DOB;
  //   this.Location = Location;
  //   this.age=getAge();
  // }

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

  // public String getProfilePic() {
  //   return ProfilePic;
  // }

  // public void setProfilePic(String profilePic) {
  //   this.ProfilePic = profilePic;
  // }

  // public String getBio() {
  //   return Bio;
  // }

  // public void setBio(String bio) {
  //   this.Bio = bio;
  // }

  // public LocalDate getDOB() {
  //   return DOB;
  // }

  // public void setDOB(LocalDate dOB) {
  //   this.DOB = dOB;
  // }

  // public String getLocation() {
  //   return Location;
  // }

  // public void setLocation(String location) {
  //   this.Location = location;
  // }

  // public void setAge(int age) {
  //   this.age = age;
  // }

  // public int getAge() {
  //   if (DOB != null) {
  //     return Period.between(DOB, LocalDate.now()).getYears();
  //   } else {
  //     return 0;
  //   }
  // }
}