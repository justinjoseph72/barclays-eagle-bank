# Eagle Bank

## Starting the app in Local machine

### Prerequisite
- Java 21 
- Docker

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
