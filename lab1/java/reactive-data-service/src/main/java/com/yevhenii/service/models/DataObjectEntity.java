package com.yevhenii.service.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class DataObjectEntity {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private Integer age;
    private String str;
    private Integer num;
}
