package com.fintrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа в приложение.
 *
 * @SpringBootApplication — это три аннотации в одной:
 *   @Configuration        — этот класс является источником бинов Spring
 *   @EnableAutoConfiguration — Spring Boot автоматически настраивает компоненты
 *                             на основе того, что есть в classpath (увидел PostgreSQL-драйвер
 *                             и JPA — сам настроит DataSource и EntityManager)
 *   @ComponentScan        — Spring сканирует пакет com.fintrack и все вложенные,
 *                             подбирая @Controller, @Service, @Repository и т.д.
 */
@SpringBootApplication
public class FinTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinTrackApplication.class, args);
    }
}
