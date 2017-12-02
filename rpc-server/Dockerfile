FROM openjdk:8-slim

RUN mkdir -p /usr/local/slipo-workbench/rpc-server
WORKDIR /usr/local/slipo-workbench/rpc-server

EXPOSE 8080

ENV PROFILE "development"
ENV DB_HOST="localhost" DB_PORT="5432" DB_NAME="slipo-workbench" DB_USERNAME="slipo" DB_PASSWORD_FILE="/etc/secrets/db-password"

VOLUME ["/usr/local/slipo-workbench/rpc-server/logs"]

RUN mkdir config && touch config/application.properties

COPY workbench-rpc-server.jar docker-entrypoint.sh ./
RUN chmod +x docker-entrypoint.sh

CMD ["./docker-entrypoint.sh"]
