FROM hseeberger/scala-sbt

COPY . /app

WORKDIR /app

RUN chmod +x *.sh

# CMD ["./generate.sh"]
# RUN ./generate.sh

RUN sbt assembly

RUN mkdir data
RUN mkdir generated

RUN java -jar target/scala-2.12/generation-tool-assembly-0.1.jar data/data.txt

CMD ["./copy.sh"]