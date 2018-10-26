FROM hseeberger/scala-sbt

COPY . /app

WORKDIR /app

RUN chmod +x generate.sh

# CMD ["./generate.sh"]
# RUN ./generate.sh

RUN sbt assembly

RUN java -jar target/scala-2.12/generation-tool-assembly-0.1.jar /data/data.txt

CMD ["echo done"]