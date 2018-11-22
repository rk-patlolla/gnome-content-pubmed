package com.gnomecontent.pubmed.controller;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gnomecontent.pubmed.documents.PubmedArticles;
import com.gnomecontent.pubmed.repository.PubmedArticlesRepository;
import com.gnomecontent.pubmed.service.PubmedService;

@RestController
public class PubmedController {

	@Autowired
	private PubmedService pubService;
	
	@Autowired
	private PubmedArticlesRepository pubRepository;

	@GetMapping(value = "/")
	public String index() {
		return "Welcome Gnome Content";

	}

	@GetMapping(value = "/downloadFiles")
	public String downloadGzFiles() throws IOException {

		String downloadAndExtractGzFiles = pubService.downloadAndExtractGzFiles();

		return downloadAndExtractGzFiles;
	}

	@GetMapping(value = "/getXmlData")
	public String getDataFromXml() throws IOException {

		String xmlData = pubService.getXmlData();

		return xmlData;
	}
	
	@GetMapping(value = "/searchByKeywords")
	public String getArticlesByKeywords() throws IOException {

		String xmlData = pubService.searchAndSaveArticlesInMongo();

		return xmlData;
	}
	
	@GetMapping(value = "/extractText")
	public String getText() throws IOException {

		String xmlData = pubService.extractText();

		return xmlData;
	}
	
	@GetMapping(value = "/getXmlDataBySaxParser")
	public String getXmlDataBySaxParser() throws IOException {

		String xmlData = pubService.getXmlDataBySaxParser();

		return xmlData;
	}
	@GetMapping(value = "/test")
	public Iterable<PubmedArticles>  test() throws IOException {

		Iterable<PubmedArticles> findAll = pubRepository.search(queryStringQuery("java"));

		return findAll;
	}
	
	
}
