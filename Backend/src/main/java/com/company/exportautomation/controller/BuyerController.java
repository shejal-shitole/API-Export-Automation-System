package com.company.exportautomation.controller;


import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.company.exportautomation.service.BuyerPdfService;

import com.company.exportautomation.discovery.BuyerExtractionService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.company.exportautomation.entity.Buyer;
import com.company.exportautomation.service.BuyerService;
import com.company.exportautomation.service.GeminiService;
import com.company.exportautomation.discovery.BuyerExtractionService;

@RestController
@RequestMapping("/api/buyers")
@CrossOrigin(origins = "*")
public class BuyerController {

    private final BuyerService buyerService;
    private final BuyerPdfService buyerPdfService;
    private final GeminiService geminiService;
    private final BuyerExtractionService extractionService;


public BuyerController(
        BuyerService buyerService,
        GeminiService geminiService,
        BuyerExtractionService extractionService,
        BuyerPdfService buyerPdfService) {

    this.buyerService = buyerService;
    this.geminiService = geminiService;
    this.extractionService = extractionService;
    this.buyerPdfService = buyerPdfService;
}

    // Add buyer
    @PostMapping
    public Buyer addBuyer(@RequestBody Buyer buyer) {
        return buyerService.saveBuyer(buyer);
    }

    // Get all buyers
    @GetMapping
    public List<Buyer> getAllBuyers() {
        return buyerService.getAllBuyers();
        
    }

    @PutMapping("/{id}")
    public Buyer updateBuyer(@PathVariable Long id, @RequestBody Buyer buyer) {
        return buyerService.updateBuyer(id, buyer);
    }
    
    // Get buyer by ID
    @GetMapping("/{id}")
    public ResponseEntity<Buyer> getBuyerById(@PathVariable Long id) {
        Optional<Buyer> buyer = buyerService.getBuyerById(id);
        return buyer.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    // Delete buyer
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBuyer(@PathVariable Long id) {
        if (buyerService.getBuyerById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        buyerService.deleteBuyer(id);
        return ResponseEntity.noContent().build();
    }

    // Classify buyer using Gemini AI
    @PostMapping("/{id}/classify")
    public ResponseEntity<String> classifyBuyer(@PathVariable Long id) {
        Optional<Buyer> optionalBuyer = buyerService.getBuyerById(id);

        if (optionalBuyer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Buyer buyer = optionalBuyer.get();

        String buyerDetails =
                "Buyer Name: " + buyer.getBuyerName()
                + ", Company Name: " + buyer.getCompanyName()
                + ", Email: " + buyer.getEmail()
                + ", Website: " + buyer.getWebsite()
                + ", Country: " + buyer.getCountry();

        String result = geminiService.classifyBuyer(buyerDetails);
        buyer.setBuyerType(result);
        buyerService.saveBuyer(buyer);

        return ResponseEntity.ok(result);
    }

    // Extract emails and save new records
    @PostMapping("/extract-emails")
    public ResponseEntity<Map<String, Object>> extractEmails(
            @RequestBody String text) {

        Set<String> emails = extractionService.extractEmails(text);

        int savedCount = extractionService.saveExtractedEmails(emails);

        return ResponseEntity.ok(Map.of(
                "extractedEmails", emails,
                "savedCount", savedCount
        ));
    }
    
    @PostMapping("/extract-buyer-details")
    public ResponseEntity<String> extractBuyerDetails(
            @RequestBody String sourceText) {

        boolean saved = extractionService.extractAndSaveBuyer(sourceText);

        if (saved) {
            return ResponseEntity.ok("Buyer details extracted and saved successfully.");
        }

        return ResponseEntity.badRequest()
                .body("Buyer details could not be saved. Check the source text and email.");
    }
    

@GetMapping("/export-pdf")
public ResponseEntity<byte[]> exportBuyersPdf() throws Exception {

    byte[] pdfData = buyerPdfService.generateBuyerPdf();

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=API_Export_Buyers.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfData);
}

}
