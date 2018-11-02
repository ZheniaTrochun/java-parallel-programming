package com.yevhenii.service.configs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:application.yaml")
@ConfigurationProperties(prefix = "application")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppPropertyHolder {

    private Couchbase couchbase;

    private Integer parallelism;
    private String datafile;
    private Integer pageSize;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Couchbase {

        private Cluster cluster;

        private Bucket bucket;
        private Integer timeout;
        private Integer kvTimeout;
        private Integer bufferSize;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Cluster {
            private String url;
            private String username;
            private String password;
            private String node;
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Bucket {
            private String name;
            private Integer quota;
        }
    }
}
