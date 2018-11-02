package com.gnomecontent.pubmed.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gnomecontent.pubmed.service.PubmedService;

@RestController
public class PubmedController {

	@Autowired
	private PubmedService pubService;

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
}
