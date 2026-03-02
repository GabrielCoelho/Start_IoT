package br.edu.fatec.startiot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StartIotApplication {

    public static void main(String[] args) {
        SpringApplication.run(StartIotApplication.class, args);
    }
}
