FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/paintings2.jar /paintings2/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/paintings2/app.jar"]
