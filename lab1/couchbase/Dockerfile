FROM couchbase/server:latest

COPY init.sh /opt/couchbase

RUN chmod +x /opt/couchbase/init.sh

CMD ["/opt/couchbase/init.sh"]
