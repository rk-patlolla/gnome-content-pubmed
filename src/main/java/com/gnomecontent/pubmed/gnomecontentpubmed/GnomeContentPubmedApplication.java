package com.gnomecontent.pubmed.gnomecontentpubmed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.gnomecontent.pubmed")
@EntityScan(basePackages="com.gnomecontent.pubmed")
public class GnomeContentPubmedApplication {

	public static void main(String[] args) {
		SpringApplication.run(GnomeContentPubmedApplication.class, args);
	}
}
