# Slipo Workbench -- RPC Service

## Quickstart ##

Copy configuration examples from `config-examples/` into `/src/main/resources/config/`, and edit to adjust to your needs.

    cp config-example/* /src/main/resources/config/

Build and package the application:

    mvn package

Run the application:

    java -jar target/workbench-rpc-server.jar


## Build docker image ##

We use a dedicated Ant task `prepare-docker-build` to prepare a folder `target/docker-build` holding the necessary build context for a fοllowing `docker-build` command. 
The only reason for not building the image directly from project's root directory is to avoid a time-consuming scan of the entire source tree (just to exclude 
it via dockerignore).

Prepare build context:

    ant prepare-docker-build

Build the docker image, say `local/slipo-workbench-rpc-server:0.1`:

    docker build -t local/slipo-workbench-rpc-server:0.1 target/docker-build

Run a container based on the newly created image:

    docker run --name slipo-workbench-rpc-server-1 -p 9080:8080 --link postgres-1:postgres-1 \
        --volume "$(pwd)/.secrets/db-password:/etc/secrets/db-password" \
        --volume "/mnt/nfs-1/app-data/slipo-workbench:/mnt/nfs-1/app-data/slipo-workbench" \
        -e DB_HOST=postgres-1 -e DB_USERNAME=slipo -e DB_PASSWORD_FILE=/etc/secrets/db-password \
        local/slipo-workbench-rpc-server:0.1


