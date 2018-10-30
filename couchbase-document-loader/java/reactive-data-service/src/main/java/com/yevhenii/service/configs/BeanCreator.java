package com.yevhenii.service.configs;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.yevhenii.service.converters.DataObjectToJsonDocumentConverter;
import com.yevhenii.service.converters.JsonDocumentToDataObjectConverter;
import com.yevhenii.service.converters.JsonObjectToDataObjectConverter;
import com.yevhenii.service.dao.CouchbaseDao;
import com.yevhenii.service.dao.ReactiveCouchbaseDao;
import com.yevhenii.service.models.DataObject;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BeanCreator {

    private static final Object MONITOR = new Object();

    private final Map<String, Cluster> clusters = new HashMap<>();

    //    @Bean
    public Cluster cluster(AppPropertyHolder properties) {
        String node = "localhost";
//        String node = properties.getCouchbase().getCluster().getNode();
        Cluster cluster = clusters.get(node);
        if (cluster == null) {
            synchronized (MONITOR) {
                cluster = clusters.get(node);
                if (cluster == null) {

                    CouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder()
                            .connectTimeout(10000)
                            .kvTimeout(3000)
                            .requestBufferSize(131072)
                            .responseBufferSize(131072)
                            .build();

                    cluster = CouchbaseCluster.create(env, node);

                    clusters.put(node, cluster);
                }
            }
        }

        return cluster;
    }

    @Bean
    public CouchbaseDao<DataObject> dataObjectDao(AppPropertyHolder properties) {
        return new CouchbaseDao<>(
                cluster(properties)
                        .authenticate("admin", "admin123"),
    "DataObject",
//                        .openBucket("DataObject"),
//                cluster(properties)
//                        .authenticate(properties.getCouchbase().getCluster().getUsername(), properties.getCouchbase().getCluster().getPassword())
//                        .openBucket(properties.getCouchbase().getBucket()),
                new DataObjectToJsonDocumentConverter(),
                new JsonDocumentToDataObjectConverter(),
                new JsonObjectToDataObjectConverter()
        );
    }

    @Bean
    public ReactiveCouchbaseDao<DataObject> dataObjectReactiveDao(AppPropertyHolder properties) {
        return new ReactiveCouchbaseDao<>(
                cluster(properties)
                        .authenticate("admin", "admin123"),
                "DataObject",
//                        .openBucket("DataObject").async(),
//                cluster(properties)
//                        .authenticate(properties.getCouchbase().getCluster().getUsername(), properties.getCouchbase().getCluster().getUsername())
//                        .openBucket(properties.getCouchbase().getBucket()).async(),
                new DataObjectToJsonDocumentConverter(),
                new JsonDocumentToDataObjectConverter(),
                new JsonObjectToDataObjectConverter()
        );
    }
}
