COI Push Service
===============

COI Push Service


Building
--------

The project uses Gradle as build system. It includes Gradle wrapper, so all build commands are supposed to be issued using `./gradlew`.

### jar

    ./gradlew build

This builds a 'fat' jar. 


### Docker

    ./gradlew buildImage

This builds an image containing the application distribution and prints the image ID:

    > Task :buildImage
    Created image with ID 'cdd5fc70a49b'.

To run that image locally, execute:

    docker run -it --rm -p 80:8080 cdd5fc70a49b
