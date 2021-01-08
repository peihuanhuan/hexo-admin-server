package net.peihuan.newblog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class NewblogApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewblogApplication.class, args);
	}

}
