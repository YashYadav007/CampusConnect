package com.campusconnect.lostfound.dto;

import com.campusconnect.enums.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLostFoundPostRequest {

  @NotNull
  private PostType type;

  @NotBlank
  @Size(min = 5, max = 200)
  private String title;

  @Size(max = 20000)
  private String description;

  @Size(max = 2048)
  private String imageUrl;

  @NotBlank
  @Size(min = 2, max = 255)
  private String location;

  @NotNull
  private LocalDate dateOfIncident;
}
