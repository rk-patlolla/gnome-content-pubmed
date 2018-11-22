package com.gnomecontent.pubmed.service;

import java.io.IOException;

public interface PubmedService {
	
	public String getXmlData();
	public String downloadAndExtractGzFiles();
	
	public String searchAndSaveArticlesInMongo();
	
	public String extractText() throws IOException;
	public String getXmlDataBySaxParser();
	
	

}
