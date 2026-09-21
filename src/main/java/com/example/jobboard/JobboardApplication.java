package com.example.jobboard;

import com.example.jobboard.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootApplication
public class JobboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobboardApplication.class, args);
	}
}