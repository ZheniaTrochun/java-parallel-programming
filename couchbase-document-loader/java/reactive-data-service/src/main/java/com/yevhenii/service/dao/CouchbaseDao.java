package com.yevhenii.service.dao;

import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.Statement;
import com.yevhenii.service.models.Document;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


public class CouchbaseDao<T> implements Dao<String, Document<T>> {

    private Bucket bucket;
    private final String bucketName;
    private final Cluster cluster;
    private final int PAGE_SIZE;

    private final Class<T> clazz;

    public CouchbaseDao(Cluster cluster, String bucketName, int pageSize, Class<T> clazz) {
        this.cluster = cluster;
        this.bucket = cluster.openBucket(bucketName);
        this.bucketName = bucketName;
        this.clazz = clazz;
        PAGE_SIZE = pageSize;
    }

    @Override
    public Optional<Document<T>> findById(String id) {

        return Optional.ofNullable(bucket.get(id))
                .map(doc -> Document.read(doc, clazz));
    }

    @Override
    public List<Document<T>> findAll() {

        return bucket.query(Queries.buildSelectStatement(bucketName))
                .allRows()
                .stream()
                .map(N1qlQueryRow::value)
                .map(obj -> Document.read(obj, clazz))
                .collect(Collectors.toList());

    }

    @Override
    public List<Document<T>> findAll(int page) {
        return findAll((page - 1) * getPageSize(), getPageSize());
    }

    @Override
    public List<Document<T>> findAll(int offset, int size) {

        Statement select = Queries.buildSelectStatement(bucketName)
                .limit(size)
                .offset(offset);

        return bucket.query(select)
                .allRows()
                .stream()
                .map(N1qlQueryRow::value)
                .map(obj -> Document.read(obj, clazz))
                .collect(Collectors.toList());
    }

    @Override
    public Document<T> insert(Document<T> entity) {
        return Document.read(
                bucket.insert(entity.toJsonDocument()),
                clazz
        );
    }

    @Override
    public Document<T> update(Document<T> entity) {
        return Document.read(
                bucket.replace(entity.toJsonDocument()),
                clazz
        );
    }

    @Override
    public boolean delete(String id) {
        try {
            bucket.remove(id);
            return true;
        } catch (CouchbaseException e) {
            return false;
        }
    }

    @Override
    public boolean deleteAll() {
//        close();

//        cluster.clusterManager().removeBucket(bucketName);
//        cluster.clusterManager().insertBucket(DefaultBucketSettings.builder().name(bucketName).quota(500).build());
//        bucket = cluster.openBucket(bucketName);

//        return bucket.query(
//                N1qlQuery.simple(String.format(Queries.CREATE_INDEX, bucketName))
//        ).finalSuccess();

        return bucket.query(N1qlQuery.simple(String.format(Queries.DELETE_ALL, bucketName))).finalSuccess();
    }

    @Override
    public int getSize() {
        N1qlQuery query = N1qlQuery.simple(String.format(Queries.COUNT_ALL, bucketName));

        return bucket.query(query)
                .allRows()
                .get(0)
                .value()
                .getInt("size");
    }

    @Override
    public int getPageSize() {
        return PAGE_SIZE;
    }

    @Override
    public boolean close() {
        return bucket.close();
    }

    @Override
    public boolean closeCurrentBucket() {
        boolean res = bucket.close();
        bucket = cluster.openBucket(bucketName);

        return res;
    }

    private <R> R withBucket(Function<Bucket, R> function) {
        Bucket bucket = cluster.openBucket(bucketName);
        R result = function.apply(bucket);
        bucket.close();

        return result;
    }
}
