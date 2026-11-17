
package com.company.exportautomation.search;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.stereotype.Service;

@Service
public class TavilySearchService {

    public String searchBuyers(String query) throws Exception {

        String apiKey = System.getenv("TAVILY_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "TAVILY_API_KEY is not configured"
            );
        }

        HttpClient client = HttpClient.newHttpClient();

        String requestBody = """
            {
              "query": "%s",
              "max_results": 5
            }
            """.formatted(query.replace("\"", "\\\""));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.tavily.com/search"))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        HttpResponse<String> response = client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        return response.body();
    }
}
