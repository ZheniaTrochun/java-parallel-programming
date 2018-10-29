package com.yevhenii.service.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application")
@Configuration
public class AppPropertyHolder {

    private Couchbase couchbase;

    private Integer parallelism;
    private String datafile;
    private Integer pageSize;

    public AppPropertyHolder() {
    }

    public AppPropertyHolder(Couchbase couchbase, Integer parallelism, String datafile, Integer pageSize) {
        this.couchbase = couchbase;
        this.parallelism = parallelism;
        this.datafile = datafile;
        this.pageSize = pageSize;
    }

    public Couchbase getCouchbase() {
        return couchbase;
    }

    public void setCouchbase(Couchbase couchbase) {
        this.couchbase = couchbase;
    }

    public Integer getParallelism() {
        return parallelism;
    }

    public void setParallelism(Integer parallelism) {
        this.parallelism = parallelism;
    }

    public String getDatafile() {
        return datafile;
    }

    public void setDatafile(String datafile) {
        this.datafile = datafile;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    class Couchbase {

        private Cluster cluster;

        private String bucket;
        private Integer timeout;
        private Integer kvTimeout;

        public Couchbase(Cluster cluster, String bucket, Integer timeout, Integer kvTimeout) {
            this.cluster = cluster;
            this.bucket = bucket;
            this.timeout = timeout;
            this.kvTimeout = kvTimeout;
        }

        public Couchbase() {
        }

        public Cluster getCluster() {
            return cluster;
        }

        public void setCluster(Cluster cluster) {
            this.cluster = cluster;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public Integer getKvTimeout() {
            return kvTimeout;
        }

        public void setKvTimeout(Integer kvTimeout) {
            this.kvTimeout = kvTimeout;
        }

        class Cluster {
            private String url;
            private String username;
            private String password;
            private String node;

            public Cluster(String url, String username, String password, String node) {
                this.url = url;
                this.username = username;
                this.password = password;
                this.node = node;
            }

            public Cluster() {
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }

            public String getNode() {
                return node;
            }

            public void setNode(String node) {
                this.node = node;
            }
        }
    }
}
