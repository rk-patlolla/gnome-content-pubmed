package com.gnomecontent.pubmed.service;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.gnomecontent.pubmed.documents.PubmedArticles;
import com.gnomecontent.pubmed.documents.PubmedExcelPhraseQuery;
import com.gnomecontent.pubmed.documents.PubmedUmlsKeywords;
import com.gnomecontent.pubmed.documents.PubmedUmlsPhraseQuery;
import com.gnomecontent.pubmed.documents.Test;
import com.gnomecontent.pubmed.model.Pubmed;
import com.gnomecontent.pubmed.repository.PubmedArticlesRepository;
import com.gnomecontent.pubmed.utils.PubmedUtils;

@Service
public class PubmedServiceImpl implements PubmedService {

	public static final Logger logger = LoggerFactory.getLogger(PubmedServiceImpl.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private PubmedArticlesRepository pubRepository;

	@Override
	public String downloadAndExtractGzFiles() {

		/*
		 * File urlList = new File("D:/pubmed/pubmedUrlsList.html");
		 * org.jsoup.nodes.Document doc; try { doc = Jsoup.parse(urlList, "UTF-8");
		 * org.jsoup.select.Elements links = doc.getElementsByTag("a"); int count = 0;
		 * for (org.jsoup.nodes.Element link : links) {
		 * 
		 * if (count == 1) for downloading 1 File.Total gz files 928 break; String
		 * absLink = link.attr("href"); if (absLink.endsWith(".gz") ||
		 * absLink.endsWith(".GZ")) { logger.info("URL to Download gz Files : " +
		 * absLink); String fname = absLink.substring(absLink.indexOf("pubmed18n"));
		 * logger.info("File name to save : " + fname); URL url = new
		 * URL("ftp://ftp.ncbi.nlm.nih.gov" + absLink); URLConnection con =
		 * url.openConnection(); BufferedInputStream bufferIn = new
		 * BufferedInputStream(con.getInputStream()); FileOutputStream out = new
		 * FileOutputStream("D:/pubmed/" + fname); int i = 0; byte[] bytesIn = new
		 * byte[1024]; while ((i = bufferIn.read(bytesIn)) >= 0) { out.write(bytesIn, 0,
		 * i); } out.close(); bufferIn.close(); count++; }
		 * 
		 * } logger.info("Downloaded Files Count : " + count); } catch (IOException e) {
		 * // TODO Auto-generated catch block e.printStackTrace(); }
		 */

		/* Reading .gz Files from Folder */
		File folder = new File("D:/pubmed");
		File[] listOfFiles = folder.listFiles();
		int j = 0;
		for (int i = 0; i < listOfFiles.length; i++) {
			String filename = listOfFiles[i].getName();
			/*
			 * if (j == 1) break;
			 */
			if (filename.endsWith(".gz") || filename.endsWith(".GZ")) {
				logger.info("File Name : " + filename);
				j++;
				byte[] buffer = new byte[1024];

				/* Extracting .gz Files */
				try {

					GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(folder.getAbsolutePath() + "/" + filename));
					String xmlFileName = filename.substring(filename.indexOf("pubmed18n"), filename.indexOf(".gz"));
					logger.info("XML file name to save : " + xmlFileName);
					FileOutputStream out = new FileOutputStream(folder.getAbsolutePath() + "/" + xmlFileName);
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

		logger.info("Foleder Abs Path : " + folder.getAbsolutePath());
		// logger.info("Foleder Path : "+folder.getPath());
		int xmlCount = 0;
		File[] listOfFiles = folder.listFiles();

		for (int xmlFilesCount = listOfFiles.length - 1; xmlFilesCount < listOfFiles.length; xmlFilesCount--) {
			// if(xmlFilesCount==2) {
			if (xmlCount == 1) {
				break;
			}
			String XmlFileName = listOfFiles[xmlFilesCount].getName();
			// logger.info("XML File Name: "+XmlFileName);

			if ((XmlFileName.endsWith(".xml") || XmlFileName.endsWith(".XML"))&& (XmlFileName.equals("pubmed18n0928.xml"))) {
				try {
					xmlCount++;
					File file = new File(folder.getAbsolutePath() + "/" + XmlFileName);
					DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					Document doc = dBuilder.parse(file);
					doc.getDocumentElement().normalize();

					logger.info("Root element :" + doc.getDocumentElement().getNodeName());

					NodeList nList = doc.getElementsByTagName("PubmedArticle");
					// logger.info("Length :" + nList.getLength());

					int count = 0;
					int langCount = 0;
					int skippedLangCount = 0;
					int skippedArticleCount = 0;
					int skippedPubYrCount = 0;
					int pmcIdCount=0;

					// Pubmed pub = null;
					PubmedArticles pubArticles = null;

					for (int i = 0; i < nList.getLength(); i++) {
						 //if (count == 100) // Retriving 5 Articles. you can change if you want to
						// retrive more than 5.
						// Total Articles(nList.getLength())
						 // break;

						count++;
						Node nNode = nList.item(i);

						// logger.info("Current Element :" + nNode.getNodeName());

						if (nNode.getNodeType() == Node.ELEMENT_NODE) {

							int year = 0;
							String publishedDate = "";
							NodeList nList1 = doc.getElementsByTagName("PubDate");
							for (int m = i; m <= i; m++) {

								Node nNode1 = nList1.item(m);

								if (nNode1.getNodeType() == Node.ELEMENT_NODE) {

									Element eElement1 = (Element) nNode1;
									try {
										year = Integer.parseInt(
												eElement1.getElementsByTagName("Year").item(0).getTextContent());
										// logger.info("Published Year :
										// "+eElement1.getElementsByTagName("Year").item(0).getTextContent());

										try {
											publishedDate += eElement1.getElementsByTagName("Year").item(0)
													.getTextContent();

										} catch (Exception e) {
											// logger.error("No Year");
										}
										try {
											publishedDate += "-"
													+ eElement1.getElementsByTagName("Month").item(0).getTextContent();

										} catch (Exception e) {
											// logger.error("No Month");
										}
										try {
											publishedDate += "-"
													+ eElement1.getElementsByTagName("Day").item(0).getTextContent();

										} catch (Exception e) {
											// logger.error("No Day");
										}

									} catch (Exception e) {
										skippedPubYrCount++;
										// logger.info("Published Year Skipped...");
									}

									try {
										String extractYear = eElement1.getElementsByTagName("MedlineDate").item(0)
												.getTextContent();
										year = Integer.parseInt(extractYear.substring(0, 4));
										// logger.info("MedlineDate....."+extractYear.substring(0, 4));
										publishedDate += extractYear;
										// logger.info("pub date..."+publishedDate);
									} catch (Exception e) {
										// logger.error("No Year");
									}

								}

							}

							// pub = new Pubmed();
							pubArticles = new PubmedArticles();
							List<String> authersList = new ArrayList<String>();
							Element eElement = (Element) nNode;

							if (year >= 2008) {

								String language = eElement.getElementsByTagName("Language").item(0).getTextContent();
								if (language.equals("eng")) {
									langCount++;

									String pmid = eElement.getElementsByTagName("PMID").item(0).getTextContent();
									String journalTitle = eElement.getElementsByTagName("Title").item(0)
											.getTextContent();
									String articleTitle = eElement.getElementsByTagName("ArticleTitle").item(0)
											.getTextContent();
									String articleLanguage = eElement.getElementsByTagName("Language").item(0)
											.getTextContent();
									String publicationStatus = eElement.getElementsByTagName("PublicationStatus")
											.item(0).getTextContent();

									logger.info("PMID : " + pmid);
									// logger.info("Published Year : " + publishedDate);
									// logger.info("Journal Tittle : " + journalTitle);
									// logger.info("Article Tittle : " + articleTitle);
									// logger.info("Language : " + articleLanguage);
									// logger.info("PublicationStatus : " + publicationStatus);

									try {
										if (eElement.getElementsByTagName("LastName").item(0).getTextContent() != null)
											authersList.add(
													eElement.getElementsByTagName("LastName").item(0).getTextContent()
															+ " "
															+ eElement.getElementsByTagName("ForeName").item(0)
																	.getTextContent()
															+ " " + eElement.getElementsByTagName("Initials").item(0)
																	.getTextContent());
										// logger.info("Authers : "+
										// eElement.getElementsByTagName("LastName").item(0).getTextContent()+ " "+
										// eElement.getElementsByTagName("ForeName").item(0).getTextContent()+ " "+
										// eElement.getElementsByTagName("Initials").item(0).getTextContent());
									} catch (Exception e) {
										// logger.error("No Authers");
									}
									try {
										if (eElement.getElementsByTagName("LastName").item(1).getTextContent() != null)
											authersList.add(
													eElement.getElementsByTagName("LastName").item(1).getTextContent()
															+ " "
															+ eElement.getElementsByTagName("ForeName").item(1)
																	.getTextContent()
															+ " " + eElement.getElementsByTagName("Initials").item(1)
																	.getTextContent());
										// logger.info("Authers : "+
										// eElement.getElementsByTagName("LastName").item(1).getTextContent()+ " "+
										// eElement.getElementsByTagName("ForeName").item(1).getTextContent()+ " "+
										// eElement.getElementsByTagName("Initials").item(1).getTextContent());

									} catch (Exception e) {
										// logger.error("No Authers");
									}
									try {
										if (eElement.getElementsByTagName("LastName").item(2).getTextContent() != null)
											authersList.add(
													eElement.getElementsByTagName("LastName").item(2).getTextContent()
															+ " "
															+ eElement.getElementsByTagName("ForeName").item(2)
																	.getTextContent()
															+ " " + eElement.getElementsByTagName("Initials").item(2)
																	.getTextContent());
										// logger.info("Authers : "+
										// eElement.getElementsByTagName("LastName").item(2).getTextContent()+ " "+
										// eElement.getElementsByTagName("ForeName").item(2).getTextContent()+ " "+
										// eElement.getElementsByTagName("Initials").item(2).getTextContent());

									} catch (Exception e) {
										// logger.error("No Authers");
									}
									String abstractText = null;
									try {
										abstractText = eElement.getElementsByTagName("AbstractText").item(0)
												.getTextContent();
										// logger.info("Abstract Text : " +
										// eElement.getElementsByTagName("AbstractText").item(0).getTextContent());
									} catch (Exception e) {
										abstractText = "NA";
										// logger.error("No Abstract Text");
									}
									
									
									try {
										String pmcId = eElement.getElementsByTagName("ArticleIdList").item(2).getTextContent();
										 logger.info("PMC ID : "+pmcId);
										 pmcIdCount++;
									}catch(Exception e) {
										// logger.error("No PMC ID");
									}
									
									
									pubArticles.setPmid(pmid);
									pubArticles.setJournalTitle(journalTitle);
									pubArticles.setArticleTitle(articleTitle);
									pubArticles.setAuthers(authersList);
									pubArticles.setAbstractText(abstractText);
									pubArticles.setLanguage(articleLanguage);
									pubArticles.setPublishedDate(publishedDate);
									pubArticles.setPublicationStatus(publicationStatus);

									//String articleFullTextUrl = PubmedUtils.getArticleFullTextUrl(pmid);
									//String articleFullText = PubmedUtils.getArticleFullText(articleFullTextUrl);
									// logger.info("Url...."+articleFullTextUrl);

									//pubArticles.setArticleTextUrl(articleFullTextUrl);
									//pubArticles.setArticleFullText(articleFullText);

									// pubRepository.save(pubArticles);
									authersList.clear();
									/* Pubmed pubObj = mongoTemplate.save(pub); */

									/**************
									 * Comparing ArticleText with Eftech Utils Url ArticleText Start
									 *******************************/

									/*
									 * String eFtechUrl=
									 * "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id="+
									 * pmid+"&retmode=xml"; DocumentBuilderFactory dbf =
									 * DocumentBuilderFactory.newInstance(); DocumentBuilder db =
									 * dbf.newDocumentBuilder(); Document doc1 = db.parse(new
									 * URL(eFtechUrl).openStream()); doc1.getDocumentElement().normalize(); NodeList
									 * pubList = doc1.getElementsByTagName("PubmedArticle");
									 * 
									 * String eFtechPmidText=null; int eFetchCount=0; for (int temp = 0; temp <
									 * pubList.getLength(); temp++) {
									 * 
									 * eFetchCount++; Node pubNode = pubList.item(temp);
									 * 
									 * System.out.println("\nCurrent Element :" + pubNode.getNodeName());
									 * 
									 * if (pubNode.getNodeType() == Node.ELEMENT_NODE) {
									 * 
									 * Element pubElement = (Element) nNode; try {
									 * eFtechPmidText=pubElement.getElementsByTagName("AbstractText").item(0).
									 * getTextContent();
									 * logger.info("Abstract Text : "+pubElement.getElementsByTagName("AbstractText"
									 * ).item(0).getTextContent()); }catch(Exception e) { eFtechPmidText="NA";
									 * logger.error("No Abstract Text"); }
									 * 
									 * } } logger.info("eFetch Count  : "+eFetchCount); if(!articleText.equals("NA")
									 * && !eFtechPmidText.equals("NA")) { if(articleText.equals(eFtechPmidText)) {
									 * logger.info("ArticleText : "+articleText);
									 * logger.info("Eftech text : "+eFtechPmidText); }
									 * 
									 * }
									 */
									/**************
									 * Comparing ArticleText with Eftech Utils Url ArticleText End
									 *******************************/
								} else {
									skippedLangCount++;
									logger.info("Language other than English PMID : "
											+ eElement.getElementsByTagName("PMID").item(0).getTextContent());
									logger.info("Langauge : " + language);
								}

							} else {
								skippedArticleCount++;
								logger.info("Skiped Article PMID : "
										+ eElement.getElementsByTagName("PMID").item(0).getTextContent());
								logger.info("Skiped Article Year  : " + year);

							}

						}

					}

					logger.info("Count...." + count);
					logger.info("English Language articles count...." + langCount);
					logger.info("Language other than English count...." + skippedLangCount);
					logger.info("Skiped Articlecount...." + skippedArticleCount);
					logger.info("Skiped Published Year Count...." + skippedPubYrCount);
					logger.info("PMC Count...." + pmcIdCount);

				} catch (Exception e) {
					logger.error("..............." + e.getMessage());
				}
			}
			// }
		}
		return "success";
	}

	@Override
	public String searchAndSaveArticlesInMongo() {

		String fileName = "D:/excel-keywords.txt";
		File file = new File(fileName);
		FileReader fr;
		Pubmed pub = null;
		try {
			fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String keyword;
			try {
				while ((keyword = br.readLine()) != null) {
					// process the line
					System.out.println(keyword);
					Iterable<PubmedArticles> aList = pubRepository.search(queryStringQuery(keyword));
					List<String> keywordsList = null;
					for (PubmedArticles articles : aList) {
						Pubmed findById = mongoTemplate.findById(articles.getPmid(), Pubmed.class);
						if (findById == null) {
							pub = new Pubmed();
							keywordsList = new ArrayList<>();
							keywordsList.add(keyword);
							pub.setPmid(articles.getPmid());
							pub.setJournalTitle(articles.getJournalTitle());
							pub.setArticleTitle(articles.getArticleTitle());
							pub.setKeywords(keywordsList);
							pub.setAuthers(articles.getAuthers());
							pub.setAbstarctText(articles.getAbstractText());
							pub.setLanguage(articles.getLanguage());
							pub.setPublishedDate(articles.getPublishedDate());
							pub.setPublicationStatus(articles.getPublicationStatus());
							pub.setArticleTextUrl(articles.getArticleTextUrl());

							mongoTemplate.save(pub);
							System.out.println("Article Saved...");
							keywordsList.clear();
						} else {

							// System.out.println(articles.getPmid());
							Query query = new Query().addCriteria(Criteria.where("pmid").is(articles.getPmid()));
							Pubmed pubArticleObj = mongoTemplate.findOne(query, Pubmed.class);
							List<String> existKeyword = pubArticleObj.getKeywords();

							if (!existKeyword.contains(keyword)) {
								existKeyword.add(keyword);
								logger.info("Keywords..." + existKeyword);
								pubArticleObj.setKeywords(existKeyword);
								mongoTemplate.save(pubArticleObj);
							} else {
								logger.info(keyword + " already exists w.r.to PMID " + articles.getPmid());
							}

						}
					}

				}
			} catch (IOException e) {

				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}

		return "Excel Sheet Articles save into MongoDb Successfully";
	}

	@Override
	public String extractText() throws IOException {

		String aText = PubmedUtils.getText();
		String c="5652085";
        //PubmedUtils.getCitations(c);
		return aText;
	}

	@Override
	public String getXmlDataBySaxParser() {

		/*
		 * try { File inputFile = new File("D:/pubmed/pubmed18n0927.xml");
		 * SAXParserFactory factory = SAXParserFactory.newInstance(); SAXParser
		 * saxParser = factory.newSAXParser(); Test userhandler = new Test();
		 * saxParser.parse(inputFile, userhandler); } catch (Exception e) {
		 * e.printStackTrace(); }
		 */
		//PubmedUtils.readByStaxParser();
		
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
						String articleFullText = "";
						PubmedArticles pubmed;
						String articleFullTextUrl="";
						List<String> citations=null;
						if (pyear >= 2008) {
							if (language.equals("eng")) {
								pubmed=new PubmedArticles();
								try {
									articleFullTextUrl = PubmedUtils.getArticleFullTextUrl(pmid);
									System.out.println("URL : " + articleFullTextUrl);
									if (articleFullTextUrl != null && !articleFullTextUrl.isEmpty()) {
										if (articleFullTextUrl.contains("www.ncbi.nlm.nih.gov/pmc/articles/pmid")) {
											articleFullText = PubmedUtils.getArticleFullText(articleFullTextUrl);
										}
										else {
											articleFullText=articleFullTextUrl;
										}
									}
								
									if(pmcId!=null && !pmcId.isEmpty()) {
										String citationId=pmcId.substring(3);
										 citations =PubmedUtils.getCitations(citationId);
																			
									}else {
										pmcId="NA";
										citations=new ArrayList<>();
										citations.add("NA");
										
									}
									
								} catch (IOException e) {
								
									e.printStackTrace();
								}

								if(!articleFullTextUrl.equals("NA") && articleFullTextUrl!=null) {
									pubmed.setPmid(pmid);
									pubmed.setJournalTitle(journalTitle);
									pubmed.setArticleTitle(articleTitle);
									pubmed.setAuthers(aList);
									pubmed.setPublishedDate(publishedYear);
									pubmed.setPublicationStatus(publicationStatus);
									pubmed.setAbstractText(fullAbstractText);
									pubmed.setArticleFullText(articleFullText);
									pubmed.setArticleTextUrl(articleFullTextUrl);
									pubmed.setLanguage(language);
									pubmed.setPmcId(pmcId);
									pubmed.setCitationList(citations);
									
									pubRepository.save(pubmed);
								}
								
							} else {
								engLangCount++;
							}
							totalCount++;
							pmidCount = 0;
							aList.clear();
							fullAbstractText = "";
							pmcId=null;
						
						} else {
							skipArticlesCount++;
						}
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
	
		return "STAX Parser Success";
	}

	@Override
	public String searchAndSaveUmlArticlesInMongo() {

		String fileName = "D:/umls-keywords.txt";
		File file = new File(fileName);
		FileReader fr;
		PubmedUmlsKeywords pub = null;
		try {
			fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String keyword;
			try {
				while ((keyword = br.readLine()) != null) {
					// process the line
					System.out.println(keyword);

					// Iterable<PubmedArticles> aList =
					// pubRepository.search(queryStringQuery(keyword));

					BoolQueryBuilder bquery = QueryBuilders.boolQuery().should(QueryBuilders.queryStringQuery(keyword)
							.lenient(true).field("articleTitle").field("abstractText").field("articleText"));

					// SearchQuery searchQuery = new
					// NativeSearchQueryBuilder().withQuery(bquery).build();
					Iterable<PubmedArticles> searchList = pubRepository.search(bquery);

					/*
					 * List<PubmedArticles> pubList=new ArrayList<>();
					 * search.forEach(list->pubList.add(list));
					 * System.out.println("Size...."+pubList.size());
					 */
					List<String> keywordsList = null;
					for (PubmedArticles articles : searchList) {
						PubmedUmlsKeywords findById = mongoTemplate.findById(articles.getPmid(),
								PubmedUmlsKeywords.class);
						if (findById == null) {
							pub = new PubmedUmlsKeywords();
							keywordsList = new ArrayList<>();
							keywordsList.add(keyword);
							pub.setPmid(articles.getPmid());
							pub.setJournalTitle(articles.getJournalTitle());
							pub.setArticleTitle(articles.getArticleTitle());
							pub.setKeywords(keywordsList);
							pub.setAuthers(articles.getAuthers());
							pub.setAbstarctText(articles.getAbstractText());
							pub.setLanguage(articles.getLanguage());
							pub.setPublishedDate(articles.getPublishedDate());
							pub.setPublicationStatus(articles.getPublicationStatus());
							pub.setArticleTextUrl(articles.getArticleTextUrl());
							pub.setPmcId(articles.getPmcId());
							pub.setCitationsList(articles.getCitationList());

							mongoTemplate.save(pub);
							System.out.println("Article Saved...");
							keywordsList.clear();
						} else {

							System.out.println(articles.getPmid());
							Query query1 = new Query().addCriteria(Criteria.where("pmid").is(articles.getPmid()));
							PubmedUmlsKeywords pubArticleObj = mongoTemplate.findOne(query1, PubmedUmlsKeywords.class);
							List<String> existKeyword = pubArticleObj.getKeywords();

							if (!existKeyword.contains(keyword)) {
								existKeyword.add(keyword);
								// logger.info("Keywords..."+existKeyword);
								pubArticleObj.setKeywords(existKeyword);
								mongoTemplate.save(pubArticleObj);
							} else {
								logger.info(keyword + " already exists w.r.to PMID " + articles.getPmid());
							}

						}
					}

				}
			} catch (IOException e) {

				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}

		return "UMLS Articles save into MongoDb Successfully";
	}

	@Override
	public String searchUmlsKeywordsByPhraseQuery() {

		String fileName = "D:/umls-keywords.txt";
		File file = new File(fileName);
		FileReader fr;
		PubmedUmlsPhraseQuery pub = null;
		try {
			fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String keyword;
			try {
				while ((keyword = br.readLine()) != null) {
					// process the line
					System.out.println(keyword);

					// Iterable<PubmedArticles> aList =
					// pubRepository.search(queryStringQuery(keyword));
					BoolQueryBuilder query = QueryBuilders.boolQuery();
					query.should(QueryBuilders.matchPhraseQuery("articleTitle", keyword));
					query.should(QueryBuilders.matchPhraseQuery("abstractText", keyword));
					query.should(QueryBuilders.matchPhraseQuery("articleText", keyword));

					// SearchQuery searchQuery = new
					// NativeSearchQueryBuilder().withQuery(bquery).build();
					Iterable<PubmedArticles> searchList = pubRepository.search(query);
					if (searchList != null) {
						List<String> keywordsList = null;
						for (PubmedArticles articles : searchList) {
							PubmedUmlsPhraseQuery findById = mongoTemplate.findById(articles.getPmid(),
									PubmedUmlsPhraseQuery.class);
							if (findById == null) {
								pub = new PubmedUmlsPhraseQuery();
								keywordsList = new ArrayList<>();
								keywordsList.add(keyword);
								pub.setPmid(articles.getPmid());
								pub.setJournalTitle(articles.getJournalTitle());
								pub.setArticleTitle(articles.getArticleTitle());
								pub.setKeywords(keywordsList);
								pub.setAuthers(articles.getAuthers());
								pub.setAbstarctText(articles.getAbstractText());
								pub.setLanguage(articles.getLanguage());
								pub.setPublishedDate(articles.getPublishedDate());
								pub.setPublicationStatus(articles.getPublicationStatus());
								pub.setArticleTextUrl(articles.getArticleTextUrl());
								pub.setPmcId(articles.getPmcId());
								pub.setCitationsList(articles.getCitationList());

								mongoTemplate.save(pub);
								System.out.println("Article Saved...");
								keywordsList.clear();
							} else {

								System.out.println(articles.getPmid());
								Query query1 = new Query().addCriteria(Criteria.where("pmid").is(articles.getPmid()));
								PubmedUmlsPhraseQuery pubArticleObj = mongoTemplate.findOne(query1,
										PubmedUmlsPhraseQuery.class);
								List<String> existKeyword = pubArticleObj.getKeywords();

								if (!existKeyword.contains(keyword)) {
									existKeyword.add(keyword);
									// logger.info("Keywords..."+existKeyword);
									pubArticleObj.setKeywords(existKeyword);
									mongoTemplate.save(pubArticleObj);
								} else {
									logger.info(keyword + " already exists w.r.to PMID " + articles.getPmid());
								}

							}
						}

					} else {
						System.out.println("No data found for keyword..." + keyword);
					}
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		return "UMLS Phrase Query saved into MongoDb Successfully";
	}

	@Override
	public String searchExcelKeywordsByPhraseQuery() {

		String fileName = "D:/excel-keywords.txt";
		File file = new File(fileName);
		FileReader fr;
		PubmedExcelPhraseQuery pub = null;
		try {
			fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String keyword;
			try {
				while ((keyword = br.readLine()) != null) {
					// process the line
					System.out.println(keyword);

					// Iterable<PubmedArticles> aList =
					// pubRepository.search(queryStringQuery(keyword));

					BoolQueryBuilder query = QueryBuilders.boolQuery();
					query.should(QueryBuilders.matchPhraseQuery("articleTitle", keyword));
					query.should(QueryBuilders.matchPhraseQuery("abstractText", keyword));
					query.should(QueryBuilders.matchPhraseQuery("articleText", keyword));

					// SearchQuery searchQuery = new
					// NativeSearchQueryBuilder().withQuery(bquery).build();
					Iterable<PubmedArticles> searchList = pubRepository.search(query);
					if (searchList != null) {
						List<String> keywordsList = null;
						for (PubmedArticles articles : searchList) {
							PubmedExcelPhraseQuery findById = mongoTemplate.findById(articles.getPmid(),
									PubmedExcelPhraseQuery.class);
							if (findById == null) {
								pub = new PubmedExcelPhraseQuery();
								keywordsList = new ArrayList<>();
								keywordsList.add(keyword);
								pub.setPmid(articles.getPmid());
								pub.setJournalTitle(articles.getJournalTitle());
								pub.setArticleTitle(articles.getArticleTitle());
								pub.setKeywords(keywordsList);
								pub.setAuthers(articles.getAuthers());
								pub.setAbstarctText(articles.getAbstractText());
								pub.setLanguage(articles.getLanguage());
								pub.setPublishedDate(articles.getPublishedDate());
								pub.setPublicationStatus(articles.getPublicationStatus());
								pub.setArticleTextUrl(articles.getArticleTextUrl());
								pub.setPmcId(articles.getPmcId());
								pub.setCitationsList(articles.getCitationList());

								mongoTemplate.save(pub);
								System.out.println("Article Saved...");
								keywordsList.clear();
							} else {

								System.out.println(articles.getPmid());
								Query query1 = new Query().addCriteria(Criteria.where("pmid").is(articles.getPmid()));
								PubmedExcelPhraseQuery pubArticleObj = mongoTemplate.findOne(query1,
										PubmedExcelPhraseQuery.class);
								List<String> existKeyword = pubArticleObj.getKeywords();

								if (!existKeyword.contains(keyword)) {
									existKeyword.add(keyword);
									// logger.info("Keywords..."+existKeyword);
									pubArticleObj.setKeywords(existKeyword);
									mongoTemplate.save(pubArticleObj);
								} else {
									logger.info(keyword + " already exists w.r.to PMID " + articles.getPmid());
								}

							}
						}

					} else {
						System.out.println("No data found for keyword..." + keyword);
					}
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}

		return "Excel Phrase Query Articles saved into MongoDb Successfully";
	}

}
