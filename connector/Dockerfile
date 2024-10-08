# -buster is required to have apt available
FROM openjdk:17-slim-buster

# by default curl is not available, so install it
RUN apt update && apt install curl -y; apt-get install -y gettext-base

WORKDIR /app


# Copy config
COPY ./resources /resources
# Copy jar
COPY ./build/libs/connector.jar /app

ENV EDC_FS_CONFIG = $EDC_FS_CONFIG

CMD java -jar connector.jar
