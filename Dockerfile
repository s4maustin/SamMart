FROM tomcat:9.0-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY target/sammart.war /usr/local/tomcat/webapps/sammart.war
EXPOSE 8080
ENV CATALINA_OPTS="-Dsammart.data=/usr/local/tomcat/data"
