package com.gnomecontent.pubmed.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pubmed")
public class Pubmed {

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

	public String getAbstarctText() {
		return abstarctText;
	}

	public String getArticleTextUrl() {
		return articleTextUrl;
	}


	public void setAbstarctText(String abstarctText) {
		this.abstarctText = abstarctText;
	}

	public void setArticleTextUrl(String articleTextUrl) {
		this.articleTextUrl = articleTextUrl;
	}


	

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public String getPmid() {
		return pmid;
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
