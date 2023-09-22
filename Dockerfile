# Stage 1 - build the project
FROM amd64/eclipse-temurin:17-jdk-jammy as gradle_build

WORKDIR /opt/FlexSignSources

# Cache gradle dependencies
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .
RUN ./gradlew dependencies
# Build project
COPY src ./src
RUN ./gradlew clean bootJar

# Stage 2 - run the project
FROM amd64/eclipse-temurin:17-jdk-jammy as run_app
# Install required dependencies
RUN apt update
RUN apt install musl-dev -y
RUN ln -s /usr/lib/x86_64-linux-musl/libc.so /lib/libc.musl-x86_64.so.1
# Copy built jar file from the stage 1
WORKDIR /opt/FlexSign
COPY --from=gradle_build /opt/FlexSignSources/build/libs/FlexSign-*.jar ./app.jar
# Run project
EXPOSE 8080
ENTRYPOINT ["java","-jar","./app.jar"]