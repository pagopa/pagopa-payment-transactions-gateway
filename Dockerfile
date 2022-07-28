FROM openjdk:17-slim 
WORKDIR /workspace/app

COPY . .
RUN ./mvnw package  -Pcloud

ENTRYPOINT ["java","-jar","./target/pagopa-payment-transactions-gateway-0.0.0.jar"]