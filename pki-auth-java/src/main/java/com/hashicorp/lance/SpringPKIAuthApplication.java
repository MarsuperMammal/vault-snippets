package com.hashicorp.lance;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.vault.authentication.SessionManager;

@SpringBootApplication
public class SpringPKIAuthApplication {

	private static final Logger logger = LoggerFactory.getLogger(SpringPKIAuthApplication.class);

	@Autowired
	private SessionManager sessionManager;

	public static void main(String[] args) {
		SpringApplication.run(SpringPKIAuthApplication.class, args);
	}

	@PostConstruct
	public void initIt() throws Exception {
		//Framework
		logger.info("Got Vault Token from : " + sessionManager.getSessionToken().getToken());
		//REST

	}

}
