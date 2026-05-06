
FROM weihuijie/jdk:17-ea-4-slim

# 设置工作目录
WORKDIR /app

# 将jar文件复制到容器中
COPY ./x-device-access-service-1.0.0.jar /app/app.jar
RUN chmod 777 /app/app.jar

# 暴露端口
EXPOSE 8010

# 运行应用程序
ENTRYPOINT ["java","-Duser.timezone=GMT+8","-server","-Xms256m","-Xmx512m","-jar","app.jar","--spring.profiles.active=test" ]