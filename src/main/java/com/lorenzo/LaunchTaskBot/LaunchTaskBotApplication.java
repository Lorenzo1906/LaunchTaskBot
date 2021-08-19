package com.lorenzo.LaunchTaskBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class LaunchTaskBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(LaunchTaskBotApplication.class, args);
	}

}
