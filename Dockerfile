FROM ubuntu

RUN apt-get update
RUN apt-get upgrade -y

RUN apt-get install openjdk-8-jre -y
RUN apt-get install mariadb-common mariadb-server mariadb-client -y

COPY run.sh /usr/sbin/

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

CMD ["/usr/sbin/run.sh"]
