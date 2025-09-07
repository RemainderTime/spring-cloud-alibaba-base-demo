package com.xf.clouduser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.xf.cloudcommon", "com.xf.clouduser"})
public class CloudUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudUserApplication.class, args);
	}

}
