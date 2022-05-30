package com.nttbootcamp.msappmobileyanki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEurekaClient
public class MsAppMobileYankiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsAppMobileYankiApplication.class, args);
	}

}
