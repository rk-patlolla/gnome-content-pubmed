package com.gnomecontent.pubmed.documents;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Test extends DefaultHandler{

	
	   boolean pmid = false;
	   boolean jtitle = false;
	   boolean articleTitle = false;
	   boolean language = false;
	   boolean pbsDate = false;
	   boolean pubYear = false;
	   boolean pubMonth = false;
	   boolean pubDay = false;
	   boolean auther=false;
	   boolean lname=false;
	   boolean fname=false;
	   boolean iname=false;
	   boolean absText=false;
	   boolean pubStatus=false;
	   boolean medlineDate=false;
	   
	   int articlesCount=0;
	   int pmidCount=0;
	   
	   String aPmid=null;
	   String aPubYear=null;
	   String aPubMonth=null;
	   String aPubDay=null;
	   
	   String aJournalTitle=null;
	   String aArticleTitle=null;
	   String aLname=null;
	   String aFname=null;
	   String aIname=null;
	 
	   List<String> authersList=new ArrayList<String>();
	   
	   String aLanguage=null;
	   String aAbsractText=null;
	   String aPublcationStatus=null;
	   String autherFullName="";
	   
	   String articlePublishedDate="";
	   String aMedlineDate=null;
	   
	   

	   @Override
	   public void startElement(String uri,  String localName, String qName, Attributes attributes) throws SAXException {
		   
		System.out.println("start element:" + qName);
		if (qName.equalsIgnoreCase("PMID")) {
			pmid = true;

		} 
		 else if (qName.equals("PubDate")) {
				pbsDate = true;
			}
		
		else if (qName.equalsIgnoreCase("Title")) {
			jtitle = true;
		} else if (qName.equalsIgnoreCase("Language")) {
			language = true;
		} else if (qName.equals("PubDate")) {
			pbsDate = true;
		}

		else if (qName.equalsIgnoreCase("Year")) {
			pubYear = true;
		} else if (qName.equalsIgnoreCase("Month")) {
			pubYear = true;
		} else if (qName.equalsIgnoreCase("Day")) {
			pubYear = true;
		}

		else if (qName.equalsIgnoreCase("ArticleTitle")) {
			articleTitle = true;
		}
		else if (qName.equalsIgnoreCase("Author")) {
			auther = true;
		}
		else if (qName.equalsIgnoreCase("LastName")) {
			lname = true;
		}
		else if (qName.equalsIgnoreCase("ForeName")) {
			fname = true;
		}
		else if (qName.equalsIgnoreCase("Initials")) {
			iname = true;
		}
		else if (qName.equalsIgnoreCase("AbstractText")) {
			absText = true;
		}
		else if (qName.equalsIgnoreCase("PublicationStatus")) {
			pubStatus = true;
		}
		/*else if (qName.equalsIgnoreCase("MedlineDate")) {
			medlineDate = true;
		}*/
	}
	   
	@Override
	public void characters(char ch[], int start, int length) throws SAXException {

		if (pmid) {
			if (pmidCount == 0) {
				aPmid = new String(ch, start, length);
				System.out.println("Pmid:... " + new String(ch, start, length));
				pmid = false;
				pmidCount++;
			}

		} 
		/*else if (medlineDate) {
			aMedlineDate = new String(ch, start, length);
			System.out.println("Medline date: " + new String(ch, start, length));
			medlineDate = false;
		}*/
		else if (pubYear) {
				if(pbsDate) {
				aPubYear = new String(ch, start, length);
				articlePublishedDate=articlePublishedDate+aPubYear;
				System.out.println("published Year: " + new String(ch, start, length));
				pubYear = false;
				}
			} 
		else if (pubMonth) {
			if (pbsDate) {
					aPubMonth = new String(ch, start, length);
					articlePublishedDate=articlePublishedDate + "-"+ aPubMonth;
					System.out.println("published Month: " + new String(ch, start, length));
					pubMonth = false;
					
				}
			
		}
		
		else if (pubDay) {
					if (pbsDate) {
					aPubDay = new String(ch, start, length);
					articlePublishedDate=articlePublishedDate+"-"+aPubDay;
					System.out.println("published Day: " + new String(ch, start, length));
					pubDay = false;
					
					}
				}
	
		 else if (jtitle) {
			aJournalTitle = new String(ch, start, length);
			System.out.println("JTitle: " + new String(ch, start, length));
			jtitle = false;
		} else if (articleTitle) {
			aArticleTitle = new String(ch, start, length);
			System.out.println("ATitle: " + new String(ch, start, length));
			articleTitle = false;
		}

		else if (language) {
			aLanguage = new String(ch, start, length);
			System.out.println("Language: " + new String(ch, start, length));
			language = false;
		}
				
		else if (lname) {
			if (auther) {
				aLname = new String(ch, start, length);
			    autherFullName=autherFullName+aLname+" ";
				System.out.println("Lastname: " + new String(ch, start, length));
				lname = false;
			}
				}
		else if (fname) {
			if (auther) {
				aFname = new String(ch, start, length);
			    autherFullName=autherFullName+aFname+" ";
				System.out.println("Forname: " + new String(ch, start, length));
				fname = false;
			}
				}
		else if (iname) {
			if (auther) {
				aIname = new String(ch, start, length);
			    autherFullName=autherFullName+aIname;
				System.out.println("Initial: " + new String(ch, start, length));
				iname = false;
			}
				}
		else if (absText) {
			aAbsractText = new String(ch, start, length);
			System.out.println("AbsText: " + new String(ch, start, length));
			absText = false;
		}
		else if (pubStatus) {
			aPublcationStatus = new String(ch, start, length);
			System.out.println("AbsText: " + new String(ch, start, length));
			pubStatus = false;
		}
		
	
	}

	   @Override
	   public void endElement(String uri, String localName, String qName) throws SAXException {
		   System.out.println("End Element :" + qName); 
		   if (qName.equalsIgnoreCase("Author")) {
			   authersList.add(autherFullName);
			   autherFullName="";
		   }
	  if (qName.equals("PubDate")) {
			   pbsDate=false;
		   }
		   if (qName.equalsIgnoreCase("PubmedArticleSet")) {
			   System.out.println("End Element :" + qName);  
			   articlesCount++;
			   System.out.println(aPmid);
			   System.out.println(aJournalTitle);
			   System.out.println(aArticleTitle);
			   System.out.println(aLanguage);
			   System.out.println(aPublcationStatus);
			   System.out.println(authersList);
			   System.out.println(articlePublishedDate);
			  // System.out.println(aMedlineDate);
			 
			   System.out.println("-------------------------------------");
			   System.out.println("Count..."+articlesCount);
			   System.out.println("-------------------------------------");
		   }
	        
		   
	       
	   }
  

}
