package com.campusconnect.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

  @NotBlank
  private String fullName;

  @NotBlank
  @Email
  private String email;

  @NotBlank
  @Size(min = 6, max = 100)
  private String password;

  private String course;

  @NotNull
  @Min(1)
  @Max(10)
  private Integer yearOfStudy;
}
