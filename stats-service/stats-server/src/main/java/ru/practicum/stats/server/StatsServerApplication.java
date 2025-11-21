package ru.practicum.stats.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"ru.practicum.stats.server", "ru.practicum.server.stats"})
@EnableJpaRepositories("ru.practicum.server.stats.repository")
public class StatsServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(StatsServerApplication.class, args);
    }
}
