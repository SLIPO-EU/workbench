FROM tomcat:8.5-jre8

EXPOSE 8080 8009

ENV DB_HOST="localhost" DB_PORT="5432" DB_NAME="slipo-workbench" DB_USERNAME="slipo" DB_PASSWORD_FILE="/etc/secrets/db-password"

VOLUME ["/usr/local/tomcat/logs"]

COPY dependency/*.jar lib/
COPY conf/*.xml conf/db.properties conf/ 

RUN mv webapps/ROOT webapps/welcome
RUN mv webapps/docs webapps/tomcat-docs
COPY workbench-webapp.war webapps/ROOT.war

RUN cat conf/db.properties >> conf/catalina.properties

COPY docker-entrypoint.sh .
RUN chmod +x docker-entrypoint.sh

CMD ["./docker-entrypoint.sh"]
