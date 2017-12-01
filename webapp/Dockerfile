FROM openjdk:8-slim

RUN mkdir -p /usr/local/slipo-workbench/webapp
WORKDIR /usr/local/slipo-workbench/webapp

EXPOSE 8080

ENV PROFILE="development"
ENV DB_HOST="localhost" DB_PORT="5432" DB_NAME="slipo-workbench" DB_USERNAME="slipo" DB_PASSWORD_FILE="/etc/secrets/db-password"
ENV RPC_SERVER="http://localhost:9080"

VOLUME ["/usr/local/slipo-workbench/webapp/logs"]

RUN mkdir config && touch config/application.properties

COPY workbench-webapp.jar docker-entrypoint.sh ./
RUN chmod +x docker-entrypoint.sh

CMD ["./docker-entrypoint.sh"]
