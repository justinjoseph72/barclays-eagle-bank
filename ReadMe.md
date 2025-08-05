# Eagle Bank

### Prerequisite
- Java 21
- Postgres instance (can use docker container)
- Gradle (can use the built in gradlew wrapper)

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


## Using the apis

The `POST /v1/users` and `POST /v1/users/{userId}/authorize` will not need a bearer authorization token.
Once a user is created using `POST /v1/users` api the userId can be passed to the `POST /v1/users/{userId}/authorize` api to generate the JWT token.
This token needs to be passed as Bearer token in the Authorization header in the following format
```declarative
Authorization: Bearer <JWT token>
```
If the request is issued using Swagger Ui then the generated token needs to be used in the `Authorize` button in the top right corner. The apis will have a Authorization header param in the swagger UI.
A value __must__ be put in this header in order to use. 
I used open api codegen to generate the Controller code an in order to inject the header to the controller method, I added the header to each of the api requiring authorization.
But swagger will not pass the value put in the header field and will use the token used in the Authorize button and inject as an Authroization header. This can be seen in the generated curl.

The `POST /v1/users/{userId}/authorize` api will return a response which has a JWT token configured to be expired in 1 hour from the time its created. We can change the duration by exporting the enviornment variable `AUTH_EXPIRY_SECONDS` before starting the application.



The apis tagged as `not_implemented` would return http status 501 Not Implemented as I could not complete them in the given time.





