FROM openjdk:17-jdk-slim

VOLUME /tmp

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 添加应用 JAR 包
ARG JAR_FILE=target/x-data-parser-service.jar
COPY ${JAR_FILE} app.jar

# 暴露端口
EXPOSE 9207

# 启动应用
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]