package com.example.mt.springairag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpringAIRagController {

	private final ChatModel chatModel;
	private final VectorStore vectorStore;

	private String prompt = "Your task is to answer the questions about US holidays."
			+ " Use the information from the DOCUMENTS section to provide accurate answer. If unsure"
			+ "or if the answer is not found in the DOCUMENTS section, simply state that you don't "
			+ "know the answer."
			+ "QUESTION : {input}"
			+ "DOCUMENTS: {documents}";

	public SpringAIRagController(ChatModel chatModel, VectorStore vectorStore) {
		this.chatModel = chatModel;
		this.vectorStore = vectorStore;
	}

	@GetMapping("/")
	public String simplify(
			@RequestParam(value = "question",
			defaultValue = "List all United States 2024 Holidays") String question) {

		PromptTemplate template = new PromptTemplate(prompt);
		Map<String, Object> promptsParams = new HashMap<String, Object>();
		promptsParams.put("input", question);
		promptsParams.put("documents", findSimilarData(question));
		
		
		return chatModel.call(template.create(promptsParams))
				.getResult()
				.getOutput()
				.getText();
	}

	private String findSimilarData(String question) {
		List<Document> documents = vectorStore.similaritySearch(question);

		return documents.stream().
				map(document -> document.getFormattedContent())
				.collect(Collectors.joining());
		
	}
}
