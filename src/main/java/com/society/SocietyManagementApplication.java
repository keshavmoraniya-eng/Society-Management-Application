package com.society;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableJpaAuditing
@ComponentScan(basePackages = "com.society")
public class SocietyManagementApplication {

	public static void main(String[] args) {

        SpringApplication.run(SocietyManagementApplication.class, args);

	}

}
