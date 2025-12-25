package com.projectnova.meridian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class ProjectNovaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectNovaApplication.class, args);
    }

}
