# 使用官方OpenJDK运行时作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 将jar文件复制到容器中
COPY target/x-file-service.jar app.jar

# 暴露端口
EXPOSE 8092

# 运行应用程序
ENTRYPOINT ["java", "-jar", "app.jar"]