package org.automobilecreator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestAutomobileCreatorApplication {

	public static void main(String[] args) {
		SpringApplication.from(AutomobileCreatorApplication::main).with(TestAutomobileCreatorApplication.class).run(args);
	}

}
