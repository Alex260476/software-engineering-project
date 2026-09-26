package com.cinema.ebook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * CesApplication
 *
 * Entry point for the Cinema E-Booking System backend.
 * Enables MongoDB auditing so Movie createdAt/updatedAt are set automatically.
 *
 * @author Team 3 Backend
 * @version 1.0
 */
@SpringBootApplication
@EnableMongoAuditing
public class CesApplication {

    public static void main(String[] args) {
        SpringApplication.run(CesApplication.class, args);
    }
}
