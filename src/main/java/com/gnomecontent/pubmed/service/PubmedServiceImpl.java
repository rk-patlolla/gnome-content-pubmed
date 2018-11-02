package com.gnomecontent.pubmed.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gnomecontent.pubmed.model.Pubmed;


@Service
public class PubmedServiceImpl implements PubmedService {
	
	public static final Logger logger = LoggerFactory.getLogger(PubmedServiceImpl.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public String downloadAndExtractGzFiles() {

		File urlList = new File("D:/pubmed/pubmedUrlsList.html");
		org.jsoup.nodes.Document doc;
		try {
			doc = Jsoup.parse(urlList, "UTF-8");
			org.jsoup.select.Elements links = doc.getElementsByTag("a");
			int count = 0;
			for (org.jsoup.nodes.Element link : links) {

				if (count == 1) /*for downloading 1 File.Total gz files 928 */
					break;
				String absLink = link.attr("href");
				if (absLink.endsWith(".gz") || absLink.endsWith(".GZ")) {
					logger.info("URL to Download gz Files : " + absLink);
					String fname = absLink.substring(absLink.indexOf("pubmed18n"));
					logger.info("File name to save : " + fname);
					URL url = new URL("ftp://ftp.ncbi.nlm.nih.gov" + absLink);
					URLConnection con = url.openConnection();
					BufferedInputStream bufferIn = new BufferedInputStream(con.getInputStream());
					FileOutputStream out = new FileOutputStream("D:/pubmed/" + fname);
					int i = 0;
					byte[] bytesIn = new byte[3000000];
					while ((i = bufferIn.read(bytesIn)) >= 0) {
						out.write(bytesIn, 0, i);
					}
					out.close();
					bufferIn.close();
					count++;
				}

			}
			logger.info("Downloaded Files Count : " + count);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* Reading .gz Files from Folder */
		File folder = new File("D:/pubmed");
		File[] listOfFiles = folder.listFiles();
		int j = 0;
		for (int i = 0; i < listOfFiles.length; i++) {
			String filename = listOfFiles[i].getName();
			if (j == 1)
				break;
			if (filename.endsWith(".gz") || filename.endsWith(".GZ")) {
				logger.info("File Name : " + filename);
				j++;
				byte[] buffer = new byte[1024];

				/* Extracting .gz Files */
				try {

					GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(folder.getAbsolutePath()+"/"+ filename));
					String xmlFileName = filename.substring(filename.indexOf("pubmed18n"), filename.indexOf(".gz"));
					logger.info("XML file name to save : " + xmlFileName);
					FileOutputStream out = new FileOutputStream(folder.getAbsolutePath() +"/"+ xmlFileName);
					int len;
					while ((len = gzis.read(buffer)) > 0) {
						out.write(buffer, 0, len);
					}

					gzis.close();
					out.close();

					logger.info("File saved success...");

				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}

		return "Files downloaded and Extracted successfully";
	}

	@Override
	public String getXmlData() {
		
		File folder = new File("D:/pubmed");
		
		logger.info("Foleder Abs Path : "+folder.getAbsolutePath());
		//logger.info("Foleder Path : "+folder.getPath());
		
		File[] listOfFiles = folder.listFiles();
		for (int xmlFilesCount = 0; xmlFilesCount < listOfFiles.length; xmlFilesCount++) {
			String XmlFileName = listOfFiles[xmlFilesCount].getName();
			if (XmlFileName.endsWith(".xml") || XmlFileName.endsWith(".XML")) {
		try {

			File file = new File(folder.getAbsolutePath()+"/"+XmlFileName);
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();
			
			logger.info("Root element :" + doc.getDocumentElement().getNodeName());

			NodeList nList = doc.getElementsByTagName("PubmedArticle");				
			int count=0;
			int langCount=0;
			
			Pubmed pub=null;
			
			for (int i = 0; i < nList.getLength(); i++) {			
				if(count==5) /* Retriving 5 Articles. you can change if you want to retrive more than 5. Total Articles(nList.getLength())*/
						break;
					count++;
				Node nNode = nList.item(i);					
				logger.info("\nCurrent Element :" + nNode.getNodeName());
		
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					
					pub=new Pubmed();
					List<String> authersList=new ArrayList<String>();
					Element eElement = (Element) nNode;		
					

					String language=eElement.getElementsByTagName("Language").item(0).getTextContent();
				if(language.equals("eng")) {
					langCount++;
					
					
					String pmid=eElement.getElementsByTagName("PMID").item(0).getTextContent();
					String completedDate=eElement.getElementsByTagName("Day").item(0).getTextContent()+"-"+eElement.getElementsByTagName("Month").item(0).getTextContent()+"-"+eElement.getElementsByTagName("Year").item(0).getTextContent();
					String publishedDate=eElement.getElementsByTagName("Day").item(3).getTextContent()+"-"+eElement.getElementsByTagName("Month").item(3).getTextContent()+"-"+eElement.getElementsByTagName("Year").item(3).getTextContent();
					String journalTitle=eElement.getElementsByTagName("Title").item(0).getTextContent();
					String articleTitle=eElement.getElementsByTagName("ArticleTitle").item(0).getTextContent();
					String articleLanguage=eElement.getElementsByTagName("Language").item(0).getTextContent();
					String publicationStatus=eElement.getElementsByTagName("PublicationStatus").item(0).getTextContent();
					
					logger.info("PMID : " +pmid);
					logger.info("Date Completed Year : " + completedDate);
					logger.info("Published Year : " + publishedDate);
					logger.info("Journal Tittle : " + journalTitle);
					logger.info("Article Tittle : " + articleTitle);
					logger.info("Language : " +articleLanguage );
					logger.info("PublicationStatus : " +publicationStatus );
					
					try {
					if(eElement.getElementsByTagName("LastName").item(0).getTextContent()!=null) 
						authersList.add(eElement.getElementsByTagName("LastName").item(0).getTextContent()+" "+eElement.getElementsByTagName("ForeName").item(0).getTextContent()+" "+eElement.getElementsByTagName("Initials").item(0).getTextContent());
					logger.info("Authers : " + eElement.getElementsByTagName("LastName").item(0).getTextContent()+" "+eElement.getElementsByTagName("ForeName").item(0).getTextContent()+" "+eElement.getElementsByTagName("Initials").item(0).getTextContent());
					}catch(Exception e) {
						logger.error("No Authers");
					}
					try {
					if(eElement.getElementsByTagName("LastName").item(1).getTextContent()!=null)
						authersList.add(eElement.getElementsByTagName("LastName").item(1).getTextContent()+" "+eElement.getElementsByTagName("ForeName").item(1).getTextContent()+" "+eElement.getElementsByTagName("Initials").item(1).getTextContent());
					logger.info("Authers : " + eElement.getElementsByTagName("LastName").item(1).getTextContent()+" "+eElement.getElementsByTagName("ForeName").item(1).getTextContent()+" "+eElement.getElementsByTagName("Initials").item(1).getTextContent());
					
					
					}catch(Exception e) {
						logger.error("No Authers");
					}
					try {
						if(eElement.getElementsByTagName("LastName").item(2).getTextContent()!=null)
							authersList.add(eElement.getElementsByTagName("LastName").item(2).getTextContent()+" "+eElement.getElementsByTagName("ForeName").item(2).getTextContent()+" "+eElement.getElementsByTagName("Initials").item(2).getTextContent());
						logger.info("Authers : " + eElement.getElementsByTagName("LastName").item(2).getTextContent()+" "+eElement.getElementsByTagName("ForeName").item(2).getTextContent()+" "+eElement.getElementsByTagName("Initials").item(2).getTextContent());
						
						
						}catch(Exception e) {
							logger.error("No Authers");
						}
					String articleText=null;
					try {
						articleText=eElement.getElementsByTagName("AbstractText").item(0).getTextContent();
						logger.info("Abstract Text : "+eElement.getElementsByTagName("AbstractText").item(0).getTextContent());
					}catch(Exception e) {
						articleText="NA";
						logger.error("No Abstract Text");
					}
					pub.setPmid(pmid);
					pub.setJournalTitle(journalTitle);
					pub.setArticleTitle(articleTitle);
					pub.setAuthers(authersList);
					pub.setArticleText(articleText);
					pub.setLanguage(articleLanguage);
					pub.setCompletedDate(completedDate);
					pub.setPublishedDate(publishedDate);
					pub.setPublicationStatus(publicationStatus);
					
					//Pubmed pubObj = mongoTemplate.save(pub);
					
					/************** Comparing ArticleText with Eftech Utils Url ArticleText Start*******************************/
							
					String eFtechUrl="https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id="+pmid+"&retmode=xml";
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					Document doc1 = db.parse(new URL(eFtechUrl).openStream());
					doc1.getDocumentElement().normalize();
					NodeList pubList = doc1.getElementsByTagName("PubmedArticle");	
					
					String eFtechPmidText=null;
					int eFetchCount=0;
					for (int temp = 0; temp < pubList.getLength(); temp++) {
						
						eFetchCount++;
						Node pubNode = pubList.item(temp);
								
						System.out.println("\nCurrent Element :" + pubNode.getNodeName());
								
						if (pubNode.getNodeType() == Node.ELEMENT_NODE) {
							
							Element pubElement = (Element) nNode;
							try {
								eFtechPmidText=pubElement.getElementsByTagName("AbstractText").item(0).getTextContent();
								logger.info("Abstract Text : "+pubElement.getElementsByTagName("AbstractText").item(0).getTextContent());
							}catch(Exception e) {
								eFtechPmidText="NA";
								logger.error("No Abstract Text");
							}

						}
					}
					logger.info("eFetch Count  : "+eFetchCount);
					if(!articleText.equals("NA") && !eFtechPmidText.equals("NA")) {
					if(articleText.equals(eFtechPmidText)) {
						logger.info("ArticleText : "+articleText);
						logger.info("Eftech text : "+eFtechPmidText);
					}
					
					}
					/************** Comparing ArticleText with Eftech Utils Url ArticleText End*******************************/		
				}
				else {
					logger.info("Language other than English PMID : "+eElement.getElementsByTagName("PMID").item(0).getTextContent());
					logger.info("Langauge : "+language );
				}
				}
				
			}
				logger.info("Count...."+count);
				logger.info("English Language articles count...."+langCount);
		
		    } catch (Exception e) {
		    	logger.error("..............."+e.getMessage());
		    }
		}
		}
		return "success";
	}


}
