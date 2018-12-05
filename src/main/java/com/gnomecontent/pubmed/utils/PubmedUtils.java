package com.gnomecontent.pubmed.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gnomecontent.pubmed.documents.PubmedArticles;

public class PubmedUtils {

	public static String getArticleFullTextUrl(String pmid) throws IOException {

		String url = "https://www.ncbi.nlm.nih.gov/pubmed/?term=" + pmid;
		Document doc = Jsoup.connect(url).get();
		String textUrl = null;
		try {
			
			Elements links = doc.select("div.icons.portlet a");
			if(links.size()>0) {	
				for (Element link : links) {
					textUrl = link.attr("abs:href");	
					} 
			}else {
				textUrl="NA";
			}
			
		}catch(Exception e) {
			textUrl="NA";
		}
			
			
		return textUrl;
	}

	public static String getArticleFullText(String articleTextUrl) throws IOException {

		String fullText = null;
		try {
			if (articleTextUrl.contains("www.ncbi.nlm.nih.gov/pmc/articles/pmid")) {
				Document doc = Jsoup.connect(articleTextUrl).get();
				fullText = doc.select("div.jig-ncbiinpagenav").text();

			} else {
				fullText = "NA";
			}

		} catch (NullPointerException e) {
			fullText = "NA";
		}
		return fullText;
	}
	public static List<String> getCitations(String citationId) throws IOException {

		String url="https://api.ncbi.nlm.nih.gov/lit/ctxp/v1/pmc/?format=citation&id="+citationId;
		List<String> citationList=new ArrayList<String>();
		try {		
			String citationData = Jsoup.connect(url).ignoreContentType(true).execute().body();
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(citationData);
				Iterator keys = jsonObject.keys();
				while (keys.hasNext()) {
					String citation = (String)keys.next();
					if(!citation.equals("id")) {
						citationList.add(citation);
					}
					//System.out.println(keys.next());	
				}		
			} catch (JSONException e) {
				e.printStackTrace();
			}		
			//System.out.println(body);

		} catch (NullPointerException e) {
			
		}
		return citationList;
	}
	public static String getText() throws IOException {

		String url = "https://www.ncbi.nlm.nih.gov/pmc/articles/pmid/29144725/";
		Document doc = Jsoup.connect(url).get();

		String text = doc.select("div.jig-ncbiinpagenav").text();
		System.out.println(text);

		return text;

	}

	public static String readByStaxParser() {

		boolean bPmid = false;
		boolean bPbsDate = false;
		/* boolean bMedlineDate = false; */
		boolean bTitle = false;
		boolean bArticleTitle = false;
		boolean bAbstractText = false;
		boolean bLastName = false;
		boolean bForName = false;
		boolean bInitials = false;
		boolean bLanguage = false;
		boolean bPbsStatus = false;
		boolean bPmcId=false;

		int pmidCount = 0;
		int totalCount = 0;
		int engLangCount = 0;
		int skipArticlesCount = 0;
		int pmcIdCount=0;

		String pmid = null;
		String publishedYear = null;
		String journalTitle = null;
		String articleTitle = null;
		String fullAbstractText = "";
		String authName = null;
		String language = null;
		String publicationStatus = null;
		String pmcId=null;

		List<String> aList = new ArrayList<>();

		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader streamReader = factory
					.createXMLStreamReader(new FileInputStream("D:/pubmed/pubmed18n0928.xml"));
			while (streamReader.hasNext()) {
				int eventType = streamReader.next();
				switch (eventType) {
				case XMLStreamConstants.START_ELEMENT:
					String qName = streamReader.getLocalName();
					if (qName.equalsIgnoreCase("PMID")) {
						bPmid = true;
					} else if (qName.equalsIgnoreCase("PubDate")) {
						bPbsDate = true;
					}
					/*
					 * else if (qName.equalsIgnoreCase("MedlineDate")) { bMedlineDate = true; }
					 */else if (qName.equalsIgnoreCase("Title")) {
						bTitle = true;
					} else if (qName.equalsIgnoreCase("ArticleTitle")) {
						bArticleTitle = true;
					} else if (qName.equalsIgnoreCase("AbstractText")) {
						bAbstractText = true;
					} else if (qName.equalsIgnoreCase("LastName")) {
						bLastName = true;
					} else if (qName.equalsIgnoreCase("ForeName")) {
						bForName = true;
					} else if (qName.equalsIgnoreCase("Initials")) {
						bInitials = true;
					} else if (qName.equalsIgnoreCase("Language")) {
						bLanguage = true;
					} else if (qName.equalsIgnoreCase("PublicationStatus")) {
						bPbsStatus = true;
					}
					 else if (qName.equalsIgnoreCase("ArticleId")) {
						 bPmcId=true;
						
						}
					break;
				case XMLStreamConstants.CHARACTERS:
					String characters = streamReader.getText();
					if (bPmid) {

						if (pmidCount == 0) {
							pmid = characters;
							// System.out.println("Pmid: " + characters);
							pmidCount++;
							bPmid = false;
						}
					}
					if (bPbsDate) {
						publishedYear = characters;
						// System.out.println("date: " + characters);

						bPbsDate = false;
					}
					/*
					 * if (bMedlineDate) {
					 * 
					 * System.out.println("Medlinedate: " + characters);
					 * 
					 * bMedlineDate = false; }
					 */
					if (bTitle) {
						journalTitle = characters;
						// System.out.println("JTitle: " + characters);
						bTitle = false;
					}
					if (bArticleTitle) {
						articleTitle = characters;
						// System.out.println("Atitle: " + characters);
						bArticleTitle = false;
					}
					if (bAbstractText) {
						fullAbstractText = fullAbstractText + characters;
						// System.out.println("AbstractText: " + characters);
						bAbstractText = false;
					}
					if (bLastName) {
						authName = characters;
						// System.out.println("LastName: " + characters);
						bLastName = false;
					}
					if (bForName) {
						authName = authName + " " + characters;
						// System.out.println("ForName: " + characters);
						bForName = false;
					}
					if (bInitials) {
						authName = authName + " " + characters;
						// System.out.println("Initials: " + characters);
						aList.add(authName);
						bInitials = false;
					}
					if (bLanguage) {
						language = characters;
						// System.out.println("Language: " + characters);
						bLanguage = false;
					}
					if (bPbsStatus) {
						publicationStatus = characters;
						// System.out.println("Publication Status: " + characters);
						bPbsStatus = false;
					}
					if (bPmcId) {
						//publicationStatus = characters;
						  String id = streamReader.getText();
						  if(id.contains("PMC")){
							  pmcId=id;
							  pmcIdCount++;
							System.out.println("PMC ID: " + pmcId);
						  }
						
						bPmcId = false;
					}
					break;
				case XMLStreamConstants.END_ELEMENT:
					String endName = streamReader.getName().getLocalPart();
					if (endName.equalsIgnoreCase("PubmedArticle")) {
						System.out.println("End Element :" + endName);
						System.out.println("PMID :" + pmid);
						/*System.out.println("Published Year :" + publishedYear);
						System.out.println("Journal Title :" + journalTitle);
						System.out.println("Article Title :" + articleTitle);
						System.out.println("Abstract Text :" + fullAbstractText);
						System.out.println("AuthList :" + aList);
						System.out.println("Language :" + language);
						System.out.println("Publication Status :" + publicationStatus);*/
						int pyear = Integer.parseInt(publishedYear.substring(0, 4));
						String articleFullText = null;
						PubmedArticles pubmed=null;
						String articleFullTextUrl=null;
						List<String> citations=null;
						if (pyear >= 2008) {
							if (language.equals("eng")) {
								pubmed=new PubmedArticles();
								try {
									articleFullTextUrl = getArticleFullTextUrl(pmid);
									if (articleFullTextUrl != null && !articleFullTextUrl.isEmpty()) {
										if (articleFullTextUrl.contains("www.ncbi.nlm.nih.gov/pmc/articles/pmid")) {
											articleFullText = getArticleFullText(articleFullTextUrl);
										}
									}
									System.out.println("URL : " + articleFullTextUrl);
									if(pmcId!=null && !pmcId.isEmpty()) {
										String citationId=pmcId.substring(3);
										 citations = getCitations(citationId);
																			
									}
									
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							} else {
								engLangCount++;
							}
							totalCount++;
							pmidCount = 0;
							aList.clear();
							fullAbstractText = "";
							pmcId=null;
							System.out.println();
						} else {
							skipArticlesCount++;
						}
						
						pubmed.setPmid(pmid);
						pubmed.setJournalTitle(journalTitle);
						pubmed.setArticleTitle(articleTitle);
						pubmed.setAuthers(aList);
						pubmed.setPublishedDate(publishedYear);
						pubmed.setPublicationStatus(publicationStatus);
						pubmed.setAbstractText(fullAbstractText);
						pubmed.setArticleFullText(articleFullText);
						pubmed.setArticleTextUrl(articleFullTextUrl);
						pubmed.setPmcId(pmcId);
						pubmed.setCitationList(citations);
						
					}
					
					
					break;
				}
			}
		} catch (FileNotFoundException | XMLStreamException e) {
			e.printStackTrace();
		}
		System.out.println("Toatl Count :" + totalCount);
		System.out.println("PMC Count :" + pmcIdCount);
		System.out.println("Skipped Article Count :" + skipArticlesCount);
		System.out.println("Language otherthan English count :" + engLangCount);
		return "s";

	}

}
