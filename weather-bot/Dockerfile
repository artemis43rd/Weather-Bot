FROM openjdk:21-jdk-slim
ENV JAVA_OPTS=""
COPY ./target/weather-bot-1.0-SNAPSHOT-jar-with-dependencies.jar /app/weather_bot.jar
ENTRYPOINT ["java", "-cp", "/app/weather_bot.jar", "com.telegrambot.Main"]