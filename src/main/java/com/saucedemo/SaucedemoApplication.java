package com.saucedemo;

import me.paulschwarz.springdotenv.spring.DotenvApplicationInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SaucedemoApplication {

    public static void main(String[] args) {
        // spring-dotenv 5.x ya no se auto-registra (no trae spring.factories), y Spring Boot 4
        // elimino el soporte de spring.factories: hay que enganchar el initializer a mano para
        // que se cargue el archivo .env (variable DB_PASSWORD).
        SpringApplication app = new SpringApplication(SaucedemoApplication.class);
        app.addInitializers(new DotenvApplicationInitializer());
        app.run(args);
    }

}
