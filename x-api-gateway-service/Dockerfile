# 使用官方OpenJDK运行时作为基础镜像
FROM weihuijie/jdk:17-ea-4-slim

# 设置工作目录
WORKDIR /app

# 将jar文件复制到容器中
COPY target/x-api-gateway-service-1.0.0.jar app.jar

# 暴露端口
EXPOSE 9100

# 运行应用程序
ENTRYPOINT ["java", "-jar", "app.jar"]