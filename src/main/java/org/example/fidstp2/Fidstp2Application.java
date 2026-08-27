package org.example.fidstp2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Fidstp2Application {

    public static void main(String[] args) {
        SpringApplication.run(Fidstp2Application.class, args);
    }

}
