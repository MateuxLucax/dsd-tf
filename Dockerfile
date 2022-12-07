FROM eclipse-temurin:17-jre-jammy

WORKDIR /opt/app

COPY target/dsd-tf-1.0.jar ./
COPY schema.sql ./

RUN apt-get -y update
RUN apt-get -y upgrade
RUN apt-get install -y sqlite3 libsqlite3-dev
RUN /usr/bin/sqlite3 xet.db < schema.sql
RUN mkdir uploads

CMD ["java", "-jar", "/opt/app/dsd-tf-1.0.jar", "8080", "xet.db"]
EXPOSE 8080