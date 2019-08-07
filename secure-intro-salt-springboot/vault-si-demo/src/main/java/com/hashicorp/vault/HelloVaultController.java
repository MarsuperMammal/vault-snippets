package com.hashicorp.vault;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HelloVaultController {
	
	@Value("${password}")
	String password;

	@RequestMapping(method = RequestMethod.GET)
	public String sayHello() {
		return "Your Vault secret is: " + password;
	}

}
