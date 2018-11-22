package com.gnomecontent.pubmed.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.gnomecontent.pubmed.documents.PubmedArticles;

public interface PubmedArticlesRepository extends ElasticsearchRepository<PubmedArticles, String> {

}
