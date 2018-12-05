package com.gnomecontent.pubmed.controller;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

	@GetMapping(value = "/searchByUmlsKeywords")
	public String getArticlesByUmlsKeywords() throws IOException {

		String xmlData = pubService.searchAndSaveUmlArticlesInMongo();

		return xmlData;
	}

	@GetMapping(value = "/umlsPhraseQuery")
	public String umlsPhraseQueryArticles() throws IOException {

		String xmlData = pubService.searchUmlsKeywordsByPhraseQuery();

		return xmlData;
	}

	@GetMapping(value = "/excelPhraseQuery")
	public String excelPhraseQueryArticles() throws IOException {

		String xmlData = pubService.searchExcelKeywordsByPhraseQuery();

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
	public Page<PubmedArticles> test(Pageable pageable) throws IOException {

		Page<PubmedArticles> getbyAll = pubRepository.getbyAll("heart", pageable);
		System.out.println("Total Elements....." + getbyAll.getTotalElements());

		// findByNameLike()
		return getbyAll;
	}


}
