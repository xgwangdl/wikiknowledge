package com.wikiknowledge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WikiknowledgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(WikiknowledgeApplication.class, args);
    }
}
