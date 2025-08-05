# Eagle Bank

### Prerequisite
- Java 21
- Postgres instance (can use docker container)

## Starting the app in Local machine

Following script can boot up the app in Linux and MacOs


Start local postgres in Docker container
```bash
./setupPostgres.sh
```
__Note__: Running the script again will remove the existing database.

Start the app
```bash
./run.sh
```

This will open a swagger ui in the browser at http://localhost:8080/swagger-ui/index.html


## Starting the app in a Docker container

build a docker image using the following command

```bash
docker build -t justin-eagle-bank-img:latest -f Dockerfile . 
```

Start and instance of postgres database using the command
```bash
./setupPostgres.sh
```

Create a container of the app using the following command
```bash
docker run --rm --name justin-eagle-bank-cont -p 8080:8080 justin-eagle-bank-img:latest
```
__Note__: the docker container uses a profile with name docker and the postgres host is set to 172.17.0.1 which is the gateway for the network bridge. Although I have run and tested this in Linux OS, I am not sure if this port will be the same in the Mac and Window OS. Kindly update the host ip to the correct ip and then build the image.