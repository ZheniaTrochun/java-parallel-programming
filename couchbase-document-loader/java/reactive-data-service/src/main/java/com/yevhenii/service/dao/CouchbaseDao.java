package com.yevhenii.service.dao;

import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.Delete;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.Statement;
import com.yevhenii.service.converters.DataObjectToJsonDocumentConverter;
import com.yevhenii.service.converters.JsonDocumentToDataObjectConverter;
import com.yevhenii.service.converters.JsonObjectToDataObjectConverter;
import com.yevhenii.service.models.Document;
import org.springframework.core.GenericTypeResolver;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.couchbase.client.java.query.Select.*;

//@Component
public class CouchbaseDao<T> implements Dao<String, Document<T>> {

    private final Function<Document<T>, JsonDocument> toJsonDocumentConverter;
    private final Function<JsonDocument, Document<T>> toEntityConverter;
    private final Function<JsonObject, Document<T>> toEntityFromObjConverter;

    private Bucket bucket;
    private final String bucketName;
    private final Cluster cluster;
    private final int PAGE_SIZE = 1000;

//    @Autowired
    public CouchbaseDao(Cluster cluster,
                        String bucketName,
                        Function<Document<T>, JsonDocument> toJsonDocumentConverter,
                        Function<JsonDocument, Document<T>> toEntityConverter,
                        Function<JsonObject, Document<T>> toEntityFromObjConverter) {

        this.toJsonDocumentConverter = toJsonDocumentConverter;
        this.toEntityConverter = toEntityConverter;
        this.toEntityFromObjConverter = toEntityFromObjConverter;
        this.cluster = cluster;
        this.bucket = cluster.openBucket(bucketName);
        this.bucketName = bucketName;
    }

    @Override
    public Optional<Document<T>> findById(String id) {

        return Optional.ofNullable(bucket.get(id))
                .map(toEntityConverter);
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
                .map(toEntityFromObjConverter)
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
                .map(toEntityFromObjConverter)
                .collect(Collectors.toList());
    }

    @Override
    public Document<T> insert(Document<T> entity) {
        return toEntityConverter.apply(
                bucket.insert(toJsonDocumentConverter.apply(entity))
        );
    }

    @Override
    public Document<T> update(Document<T> entity) {
        return toEntityConverter.apply(
                bucket.replace(toJsonDocumentConverter.apply(entity))
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
