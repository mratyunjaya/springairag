package com.example.mt.springairag;

import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DataLoader {

	
	private final VectorStore vectorStore;
	private final JdbcClient jdbcClient;
	
	@Value("classpath:/us-holidays-2024-list-classic-en-us.pdf")
	private Resource pdfResource;

	public DataLoader(VectorStore vectorStore, JdbcClient jdbcClient) {
		this.vectorStore = vectorStore;
		this.jdbcClient = jdbcClient;
	}
	
	@PostConstruct
	public void init() {
		 Integer count = jdbcClient.sql("select count(*) from vector_store")
				 .query(Integer.class)
				 .single();
		 
		 System.out.println("The number of records in pgvector :" + count);
		 
		 if(count == 0) {
			System.out.println("Loading US holidays in the PG vector store");
			
			PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder().withPagesPerDocument(1).build();
			
			PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfResource, config);
			
			var textSplitter = new TokenTextSplitter();
			vectorStore.accept(textSplitter.apply(reader.get()));
			
			System.out.println("Application is ready to serve");
		 }
	}
	
}
