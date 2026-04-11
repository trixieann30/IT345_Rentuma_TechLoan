package edu.cit.rentuma.techloan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TechloanApplication {
    public static void main(String[] args) {
        SpringApplication.run(TechloanApplication.class, args);
    }
}