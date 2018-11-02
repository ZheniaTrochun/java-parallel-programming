package com.yevhenii.service.configs;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.yevhenii.service.dao.CouchbaseDao;
import com.yevhenii.service.dao.ReactiveCouchbaseDao;
import com.yevhenii.service.models.DataObject;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
@EnableConfigurationProperties(AppPropertyHolder.class)
public class BeanCreator {

    @Bean
    public Cluster cluster(AppPropertyHolder properties) {
        String node = properties.getCouchbase().getCluster().getNode();

        CouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder()
                .connectTimeout(properties.getCouchbase().getTimeout())
                .kvTimeout(properties.getCouchbase().getKvTimeout())
                .requestBufferSize(properties.getCouchbase().getBufferSize())
                .responseBufferSize(properties.getCouchbase().getBufferSize())
                .build();

        return CouchbaseCluster.create(env, node)
                .authenticate(properties.getCouchbase().getCluster().getUsername(),
                        properties.getCouchbase().getCluster().getPassword());
    }

    @Bean
    public CouchbaseDao<DataObject> dataObjectDao(Cluster cluster, AppPropertyHolder properties) {
        return new CouchbaseDao<>(
                cluster,
                properties.getCouchbase().getBucket().getName(),
                properties.getCouchbase().getBucket().getQuota(),
                properties.getPageSize(),
                DataObject.class
        );
    }

    @Bean
    public ReactiveCouchbaseDao<DataObject> dataObjectReactiveDao(Cluster cluster, AppPropertyHolder properties) {
        return new ReactiveCouchbaseDao<>(
                cluster,
                properties.getCouchbase().getBucket().getName(),
                properties.getPageSize(),
                DataObject.class
        );
    }
}


