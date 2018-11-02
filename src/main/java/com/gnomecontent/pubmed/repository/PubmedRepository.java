package com.gnomecontent.pubmed.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.gnomecontent.pubmed.model.Pubmed;

public interface PubmedRepository extends MongoRepository<Pubmed, String>{

}
