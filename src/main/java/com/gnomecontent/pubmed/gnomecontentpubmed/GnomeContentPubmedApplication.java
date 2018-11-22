package com.gnomecontent.pubmed.gnomecontentpubmed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@ComponentScan("com.gnomecontent.pubmed")
@EntityScan(basePackages="com.gnomecontent.pubmed")
@EnableElasticsearchRepositories(basePackages = "com.gnomecontent.pubmed")
public class GnomeContentPubmedApplication {

	public static void main(String[] args) {
		SpringApplication.run(GnomeContentPubmedApplication.class, args);
	}
}
