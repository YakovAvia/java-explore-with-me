package ru.practicum.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"ru.practicum.main", "ru.practicum.stats.client"})
public class MainExploreApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainExploreApplication.class, args);
    }
}
