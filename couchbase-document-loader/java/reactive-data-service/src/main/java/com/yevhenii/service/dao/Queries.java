package com.yevhenii.service.dao;

import com.couchbase.client.java.query.dsl.path.HintPath;

import static com.couchbase.client.java.query.Select.select;

public class Queries {

    public static final String COUNT_ALL = "SELECT COUNT(*) AS size FROM `%s`";
    public static final String CREATE_INDEX = "create primary index on `%s`";

    private static final String SELECT_ALL = "META(b).id, b.*, META(b).cas";

    public static HintPath buildSelectStatement(String bucketName) {
        return select(Queries.SELECT_ALL)
                .from(bucketName)
                .as("b");
    }
}
