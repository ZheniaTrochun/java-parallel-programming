package com.yevhenii.service.dao;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.AsyncN1qlQueryResult;
import com.couchbase.client.java.query.AsyncN1qlQueryRow;
import com.couchbase.client.java.query.Statement;
import com.yevhenii.service.models.Document;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.*;
import rx.functions.Func1;

import java.util.function.Function;

import static com.couchbase.client.java.query.Select.select;

public class ReactiveCouchbaseDao<T> implements ReactiveDao<String, Document<T>> {

    private final int PAGE_SIZE = 1000;

    private AsyncBucket bucket;
    private final Cluster cluster;
    private final Class<T> clazz;

    public ReactiveCouchbaseDao(Cluster cluster,
                                String bucketName,
                                Class<T> clazz) {

        this.cluster = cluster;
        this.bucket = cluster.openBucket(bucketName).async();
        this.clazz = clazz;
    }

    @Override
    public Maybe<Document<T>> findById(String id) {
        return RxJavaInterop.toV2Maybe(
                bucket.get(id)
                .map(doc -> Document.read(doc, clazz))
                .toSingle()
        );

    }

    @Override
    public Flowable<Document<T>> findAll() {
        Statement select = select("META(b).id, b.*, META(b).cas")
                .from(bucket.name())
                .as("b");

        return RxJavaInterop.toV2Flowable(
                bucket.query(select)
                        .flatMap(AsyncN1qlQueryResult::rows)
                        .map(AsyncN1qlQueryRow::value)
                        .map(obj -> Document.read(obj, clazz))
        );
    }

    @Override
    public Observable<Document<T>> findAll(int page) {
        return findAll((page - 1) * getPageSize(), getPageSize());
    }

    @Override
    public Observable<Document<T>> findAll(int offset, int size) {

        Statement select = select("META(b).id, b.*, META(b).cas")
                .from(bucket.name())
                .as("b")
                .limit(size)
                .offset(offset);

        return RxJavaInterop.toV2Observable(
                bucket.query(select)
                        .flatMap(AsyncN1qlQueryResult::rows)
                        .map(AsyncN1qlQueryRow::value)
                        .map(obj -> Document.read(obj, clazz))
        );
    }

    @Override
    public Single<Document<T>> insert(Document<T> entity) {
        return RxJavaInterop.toV2Single(
                bucket.insert(entity.toJsonDocument())
                        .map(doc -> Document.read(doc, clazz))
                        .toSingle()
        );
    }

    @Override
    public Single<Document<T>> update(Document<T> entity) {
        return RxJavaInterop.toV2Single(
                bucket.replace(entity.toJsonDocument())
                        .map(doc -> Document.read(doc, clazz))
                        .toSingle()
        );
    }

    @Override
    public Completable delete(String id) {
        return RxJavaInterop.toV2Completable(
                bucket.remove(id)
                        .toCompletable()
        );
    }

    @Override
    public int getPageSize() {
        return PAGE_SIZE;
    }

    @Override
    public Observable<Boolean> close() {
        return RxJavaInterop.toV2Observable(bucket.close());
    }

    @Override
    public Observable<Boolean> closeCurrentBucket() {
        Observable<Boolean> res = RxJavaInterop.toV2Observable(bucket.close());
        res.subscribe(r -> this.bucket = cluster.openBucket().async());
        return res;
    }
}
