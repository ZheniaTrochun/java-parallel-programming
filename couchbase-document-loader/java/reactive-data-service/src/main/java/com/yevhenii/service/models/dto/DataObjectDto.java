package com.yevhenii.service.models.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataObjectDto {
    private Long id;
    private String name;
    private Integer age;
    private String randomString;
    private Integer randomNumber;
}
