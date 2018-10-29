package com.yevhenii.service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class Document<T> {
    private String id;
    private T content;
    private Long version;

    public Document(String id, T content, long version) {
        this.id = id;
        this.content = content;
        this.version = version;
    }
}
