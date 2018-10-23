package com.yevhenii.service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataObject {
    private String name;
    private Integer age;
    private String secretStr;
    private Integer secretNum;
}
