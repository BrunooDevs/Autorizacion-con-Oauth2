#el nombre en ADD debe de ser igual al nombre del proyecto para que se genere bien el archivo jar
FROM openjdk:11
VOLUME /tmp
EXPOSE 9100
ADD ./target/springboot-servicio-usuarios-oauth2-0.0.1-SNAPSHOT.jar servicio-oauth2.jar
ENTRYPOINT ["java","-jar","/servicio-oauth2.jar"]