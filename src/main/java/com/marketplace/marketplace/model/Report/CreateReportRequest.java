package com.marketplace.marketplace.model.Report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReportRequest {
   @NotBlank(message = "")
   @Size(min = 5, max = 100, message = "")
   private String title;

   @NotBlank(message = "")
   @Size(min = 10, max = 150, message = "")
   private String description;

   private String reporterId;
   private String violatorId;
   private String itemId;
}