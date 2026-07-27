# 只装运行时。构建在宿主机用 mvn 做完 —— 13 个模块如果放进多阶段构建，
# 每次都要重新拉一遍 Maven 仓库，"快速部署" 就无从谈起了。
FROM eclipse-temurin:17-jre

# 哪个服务由 compose 的 build.args 传进来
ARG MODULE
ENV TZ=Asia/Shanghai

WORKDIR /app
COPY ${MODULE}/target/${MODULE}-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]