FROM weihuijie/jdk:17-ea-4-slim

# 设置工作目录
WORKDIR /app

# 将jar文件复制到容器中
COPY ./x-realtime-analysis-service-1.0.0.jar /app/app.jar
RUN chmod 777 /app/app.jar

# 暴露端口
EXPOSE 8040

# 运行应用程序（修复 JSON 数组语法，确保逗号、括号、引号格式正确）
ENTRYPOINT ["java", "-Duser.timezone=GMT+8", "--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED", "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED", "--add-opens=java.base/java.math=ALL-UNNAMED", "--add-opens=java.base/sun.net.util=ALL-UNNAMED", "--add-opens=java.base/java.io=ALL-UNNAMED", "--add-opens=java.base/java.net=ALL-UNNAMED", "--add-opens=java.base/java.nio=ALL-UNNAMED", "--add-opens=java.base/java.security=ALL-UNNAMED", "--add-opens=java.base/java.text=ALL-UNNAMED", "--add-opens=java.base/java.time=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED", "-server", "-Xms256m", "-Xmx512m", "-jar", "app.jar", "--spring.profiles.active=test"]