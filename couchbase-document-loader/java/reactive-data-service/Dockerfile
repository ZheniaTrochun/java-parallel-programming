FROM gradle:latest

ADD --chown=gradle . /app

WORKDIR /app

CMD ["gradle", "run"]

EXPOSE 8080
