package it.polito.ai.backend.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ManagementRequest {
    @NotBlank String teacherId;
}
