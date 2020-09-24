# AI project backend

## Run a complete docker image
There are 2 already compiled docker images. The main difference is the presence of a predefined DB with some users and usefull data.

- Have a look at this file [SQLData](/src/main/resources/data.sql)

### Pulling from dockerHub
- `docker pull sordinho/ai-project-db` for the full db version
- `docker pull sordinho/ai-project` for an empty db version

### Run the pulled docker image
docker run -p 8080:8080 -d --name ai-project sordinho/ai-project-db

## Build the image from sources
For building the image directly from sources we need:
- The frontend repository [HERE](https://github.com/sordinho/Applicazioni-Internet-Frontend)
- The Dockerfile [HERE](Dockerfile)
- The configuration file [HERE](run.sh)

### Steps
1. Build the frontend application:
   - `ng build` inside the frontend repository.
2. Copy the built frontend files inside the `/resources/static` folder of the backend
3. Build the backend to produce a jar file
   - `maven install`
4. Run docker build
   - `docker build -t imageName .`

## API Documentation
Documentation: http://localhost:8080/ai-backend-api-docs.html
