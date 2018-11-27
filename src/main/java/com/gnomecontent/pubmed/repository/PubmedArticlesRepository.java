package com.gnomecontent.pubmed.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.gnomecontent.pubmed.documents.PubmedArticles;

public interface PubmedArticlesRepository extends ElasticsearchRepository<PubmedArticles, String> {
	
	//@Query("{\"bool\" : {\"must\" : [ {\"match\" : {\"?0\" : \"?1\"}} ]}}")
	  @Query("{\"bool\" : {\"must\" : {\"match\" : {\"articleText\" : \"?0\"}}}}")
	  Page<PubmedArticles> getbyAll(String a, Pageable pageable);

}
