FROM ubuntu:latest
LABEL authors="victo"

ENTRYPOINT ["top", "-b"]