package com.gnomecontent.pubmed.documents;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "gnome-content", type = "pubmed")
public class PubmedArticles {

	@Id
	private String pmid;
	private String journalTitle;
	private String articleTitle;
	private List<String> authers;
	private String abstractText;
	private String articleTextUrl;
	private String articleFullText;
	private String language;
	private String publishedDate;
	private String publicationStatus;
	private String pmcId;
	private List<String> citationList;

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

	public String getAbstractText() {
		return abstractText;
	}

	public String getArticleFullText() {
		return articleFullText;
	}

	public void setArticleFullText(String articleFullText) {
		this.articleFullText = articleFullText;
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

	public String getArticleTextUrl() {
		return articleTextUrl;
	}

	public void setArticleTextUrl(String articleTextUrl) {
		this.articleTextUrl = articleTextUrl;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
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

	public String getPmcId() {
		return pmcId;
	}

	public List<String> getCitationList() {
		return citationList;
	}

	public void setPmcId(String pmcId) {
		this.pmcId = pmcId;
	}

	public void setCitationList(List<String> citationList) {
		this.citationList = citationList;
	}

}
