package com.gnomecontent.pubmed.documents;


import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pubmed-umls-phrase")
public class PubmedUmlsPhraseQuery {
	@Id
	private String pmid;
	private String journalTitle;
	private String articleTitle;
	private List<String> authers;
	private String abstarctText;
	private String articleTextUrl;
	private List<String> keywords;
	private String language;
	private String publishedDate;
	private String publicationStatus;
	private String pmcId;
	private List<String> citationsList;

	public String getPmid() {
		return pmid;
	}

	public String getPmcId() {
		return pmcId;
	}

	public List<String> getCitationsList() {
		return citationsList;
	}

	public void setPmcId(String pmcId) {
		this.pmcId = pmcId;
	}

	public void setCitationsList(List<String> citationsList) {
		this.citationsList = citationsList;
	}

	public String getJournalTitle() {
		return journalTitle;
	}

	public String getArticleTitle() {
		return articleTitle;
	}

	public List<String> getAuthers() {
		return authers;
	}

	public String getAbstarctText() {
		return abstarctText;
	}

	public String getArticleTextUrl() {
		return articleTextUrl;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public String getLanguage() {
		return language;
	}

	public String getPublishedDate() {
		return publishedDate;
	}

	public String getPublicationStatus() {
		return publicationStatus;
	}

	public void setPmid(String pmid) {
		this.pmid = pmid;
	}

	public void setJournalTitle(String journalTitle) {
		this.journalTitle = journalTitle;
	}

	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}

	public void setAuthers(List<String> authers) {
		this.authers = authers;
	}

	public void setAbstarctText(String abstarctText) {
		this.abstarctText = abstarctText;
	}

	public void setArticleTextUrl(String articleTextUrl) {
		this.articleTextUrl = articleTextUrl;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setPublishedDate(String publishedDate) {
		this.publishedDate = publishedDate;
	}

	public void setPublicationStatus(String publicationStatus) {
		this.publicationStatus = publicationStatus;
	}

}

