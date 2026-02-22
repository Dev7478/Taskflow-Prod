package com.jbs.tfv3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // For scheduler
public class Taskflowv3Application implements CommandLineRunner {
	
	private static final Logger logger = LoggerFactory.getLogger(Taskflowv3Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Taskflowv3Application.class, args);
		logger.info("\nTaskflow Vr.3 successfully started...");
	}
	
	@Override
	public void run(String... args) throws Exception {
		logger.info("\nHello! form Taskflowv3 CommandLineRunner...");
	}

}
