
package com.company.exportautomation.controller;

import org.springframework.web.bind.annotation.*;

import com.company.exportautomation.discovery.BuyerExtractionService;
import com.company.exportautomation.search.TavilySearchService;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final TavilySearchService tavilySearchService;
    private final BuyerExtractionService buyerExtractionService;

    public SearchController(
            TavilySearchService tavilySearchService,
            BuyerExtractionService buyerExtractionService) {

        this.tavilySearchService = tavilySearchService;
        this.buyerExtractionService = buyerExtractionService;
    }

    @PostMapping("/buyers")
    public String searchBuyers(@RequestParam String query)
            throws Exception {

        return tavilySearchService.searchBuyers(query);
    }
    
    @PostMapping("/discover-buyers")
    public String discoverBuyers(@RequestParam String query) {

    	int savedCount = buyerExtractionService.extractAndSaveFromTavily(query);
        return "Buyer discovery completed. Saved buyers: " + savedCount;
    }
}
