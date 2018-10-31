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

import static com.couchbase.client.java.query.Select.*;


public class CouchbaseDao<T> implements Dao<String, Document<T>> {

    private Bucket bucket;
    private final String bucketName;
    private final Cluster cluster;
    private final int PAGE_SIZE = 1000;

    private final Class<T> clazz;

//    @Autowired
    public CouchbaseDao(Cluster cluster,
                        String bucketName,
                        Class<T> clazz) {

        this.cluster = cluster;
        this.bucket = cluster.openBucket(bucketName);
        this.bucketName = bucketName;
        this.clazz = clazz;
    }

    @Override
    public Optional<Document<T>> findById(String id) {

        return Optional.ofNullable(bucket.get(id))
                .map(doc -> Document.read(doc, clazz));
    }

//    todo think about maps
    @Override
    public List<Document<T>> findAll() {
//        with map/reduce index
//        return bucket.query(ViewQuery.from("design", "view"))
//                .allRows()
//                .stream()
//                .map(ViewRow::document)
//                .map(toEntityConverter)
//                .collect(Collectors.toList());

        Statement select = select("META(b).id, b.*, META(b).cas")
                .from(bucket.name())
                .as("b");

        return bucket.query(select)
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

        Statement select = select("META(b).id, b.*, META(b).cas")
                .from(bucket.name())
                .as("b")
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
        close();
        cluster.clusterManager().removeBucket(bucketName);
        cluster.clusterManager().insertBucket(DefaultBucketSettings.builder().name(bucketName).quota(500).build());
        bucket = cluster.openBucket(bucketName);
        return bucket.query(N1qlQuery.simple("create primary index on `" + bucket.name() + "`")).finalSuccess();
    }

    @Override
    public int getSize() {
        N1qlQuery query = N1qlQuery.simple("SELECT COUNT(*) AS size FROM `" + bucket.name() + "`");

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
