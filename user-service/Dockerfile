FROM ubuntu:18.04 as builder

WORKDIR /opt

RUN apt update && \
    apt install unzip -y

COPY user-service/target/universal .

RUN unzip user-service-*.zip -d build && \
    mv build/user-service* user-service

FROM openjdk:8-jdk-slim

ARG git_commit="unspecified"
ARG git_branch="unspecified"
ARG build_timestamp="1970-01-01T00:00:00Z"
ARG aws_region="ap-southeast-2"

ENV GIT_COMMIT $git_commit
ENV GIT_BRANCH $git_branch
ENV DOCKER_BUILD_TIMESTAMP $build_timestamp
ENV AWS_REGION $aws_region

WORKDIR /opt/user-service

COPY --from=builder /opt/user-service .

EXPOSE 9000

ENTRYPOINT ["bash"]

CMD ["/opt/user-service/bin/user-service", "-Dconfig.file=conf/application.postgresql.conf"]
