FROM ubuntu:latest

RUN apt-get update && \
    apt-get install -y stress && \
    apt-get clean

ENTRYPOINT ["stress"]
