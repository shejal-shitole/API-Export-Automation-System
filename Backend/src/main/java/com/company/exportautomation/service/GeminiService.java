package com.company.exportautomation.service;

import com.company.exportautomation.entity.Buyer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

@Service
public class GeminiService {

    public String classifyBuyer(String buyerDetails) {

        String prompt = """
                Classify this buyer as BUSINESS or INDIVIDUAL.

                Buyer details:
                """ + buyerDetails + """

                Reply with only one word:
                BUSINESS or INDIVIDUAL.
                """;

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured.");
        }

        Client client = Client.builder()
                .apiKey(apiKey)
                .build();

        GenerateContentResponse response =
        		client.models.generateContent("gemini-3.8-flash", prompt, null);

        return response.text().trim();
    }
    
    public Buyer extractBuyerDetails(String sourceText) throws Exception {

        String prompt = """
                Extract buyer information from the source text below.

                Return ONLY valid JSON with these fields:
                {
                  "buyerName": null,
                  "companyName": null,
                  "email": null,
                  "website": null,
                  "country": null,
                  "source": "PUBLIC_SOURCE"
                }

                Rules:
                1. Extract only information explicitly present in the text.
                2. If information is missing, use null.
                3. Do not guess or invent details.
                4. Return JSON only, without markdown.

                Source text:
                """ + sourceText;

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured.");
        }

        Client client = Client.builder()
                .apiKey(apiKey)
                .build();

        GenerateContentResponse response =
                client.models.generateContent("gemini-3.8-flash", prompt, null);

        String json = response.text().trim();

        System.out.println("Gemini Response: " + json);
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(json, Buyer.class);
    }
}