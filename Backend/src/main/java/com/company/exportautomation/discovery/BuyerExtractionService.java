package com.company.exportautomation.discovery;

import org.jsoup.Jsoup;

import com.company.exportautomation.search.TavilySearchService;
import com.company.exportautomation.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;

import com.company.exportautomation.entity.Buyer;
import com.company.exportautomation.repository.BuyerRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

@Service
public class BuyerExtractionService {
	
	@Autowired
	private TavilySearchService tavilySearchService;
	
	@Autowired
	private BuyerRepository buyerRepository;

	@Autowired
	private GeminiService geminiService;
	


public Set<String> extractEmails(String text) {

    Set<String> emails = new LinkedHashSet<>();

    if (text == null || text.isBlank()) {
        return emails;
    }

    String emailPattern =
            "(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b";
    Pattern pattern = Pattern.compile(emailPattern);
    Matcher matcher = pattern.matcher(text);

    while (matcher.find()) {

        String email = matcher.group().toLowerCase().trim();

        if (!email.contains("..")
                && !email.endsWith(".png")
                && !email.endsWith(".jpg")
                && !email.endsWith(".jpeg")
                && !email.endsWith(".gif")
                && !email.endsWith(".webp")) {

            emails.add(email);
        }
    }

    System.out.println("Extracted email addresses: " + emails);

    System.out.println("EMAIL SEARCH TEXT: " + text);
    
    return emails;
}

    public int saveExtractedEmails(Set<String> emails) {

        int savedCount = 0;
     
        for (String email : emails) {

            if (!buyerRepository.existsByEmail(email)) {

                Buyer buyer = new Buyer();

                buyer.setEmail(email);
                buyer.setEmailStatus("VALID");
                buyer.setSource("PUBLIC_SOURCE");

                buyerRepository.save(buyer);

                savedCount++;
            }
        }

        return savedCount;
    }
    

public boolean saveExtractedBuyer(Buyer buyer) {

    if (buyer == null) {
        return false;
    }

    // Clean buyer details
    if (buyer.getBuyerName() != null) {
        buyer.setBuyerName(buyer.getBuyerName().trim());
    }

    if (buyer.getCompanyName() != null) {
        buyer.setCompanyName(buyer.getCompanyName().trim());
    }

    if (buyer.getProductRequirement() != null) {
        buyer.setProductRequirement(
            buyer.getProductRequirement().trim()
        );
    }

    if (buyer.getSourceUrl() != null) {
        buyer.setSourceUrl(buyer.getSourceUrl().trim());
    }

    if (buyer.getWebsite() != null) {
        buyer.setWebsite(buyer.getWebsite().trim());
    }

    boolean hasCompany =
        buyer.getCompanyName() != null
        && !buyer.getCompanyName().isBlank()
        && isValidCompanyName(buyer.getCompanyName());

    boolean hasIndividual =
        buyer.getBuyerName() != null
        && !buyer.getBuyerName().isBlank();

    // Require a valid buyer identity
    if (!hasCompany && !hasIndividual) {
        System.out.println("Skipped: Invalid buyer name.");
        return false;
    }

    // Require product and source information
    if (buyer.getProductRequirement() == null
            || buyer.getProductRequirement().isBlank()
            || buyer.getSourceUrl() == null
            || buyer.getSourceUrl().isBlank()) {

        System.out.println(
            "Skipped: Missing product requirement or source URL."
        );
        return false;
    }

    // Set buyer type
    if (hasCompany) {
        buyer.setBuyerType("BUSINESS");
    } else {
        buyer.setBuyerType("UNKNOWN");
    }

    // Check duplicate company
    if (hasCompany
            && buyerRepository.existsByCompanyNameIgnoreCase(
                buyer.getCompanyName())) {

        System.out.println(
            "Skipped duplicate company: "
            + buyer.getCompanyName()
        );
        return false;
    }

    // Check duplicate buyer from same source
    if (hasIndividual
            && buyerRepository
                .existsByBuyerNameIgnoreCaseAndSourceUrlIgnoreCase(
                    buyer.getBuyerName(),
                    buyer.getSourceUrl())) {

        System.out.println(
            "Skipped duplicate buyer from same source: "
            + buyer.getBuyerName()
        );
        return false;
    }

    // Check duplicate buyer and website
    if (hasIndividual
            && buyer.getWebsite() != null
            && !buyer.getWebsite().isBlank()
            && buyerRepository
                .existsByBuyerNameIgnoreCaseAndWebsiteIgnoreCase(
                    buyer.getBuyerName(),
                    buyer.getWebsite())) {

        System.out.println(
            "Skipped duplicate individual: "
            + buyer.getBuyerName()
        );
        return false;
    }

    // Validate email
    String email = buyer.getEmail();

    if (email != null && !email.isBlank()) {

        email = email.trim().toLowerCase();

        if (!email.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            buyer.setEmailStatus("INVALID");
            buyer.setEmail(null);

        } else if (buyerRepository.existsByEmail(email)) {

            System.out.println("Skipped duplicate email: " + email);
            return false;

        } else {

            buyer.setEmail(email);
            buyer.setEmailStatus("VALID");
        }

    } else {

        buyer.setEmail(null);
        buyer.setEmailStatus("NOT_AVAILABLE");
    }

    // Set default source
    if (buyer.getSource() == null
            || buyer.getSource().isBlank()) {
        buyer.setSource("PUBLIC_SOURCE");
    }

    // Save buyer
    buyerRepository.save(buyer);

    System.out.println(
        "Buyer saved successfully: "
        + buyer.getBuyerName()
    );

    return true;
}
    public boolean extractAndSaveBuyer(String sourceText) {

        try {
            Buyer buyer = geminiService.extractBuyerDetails(sourceText);

            if (buyer == null) {
                System.out.println("Gemini returned NULL buyer.");
                return false;
            }

            System.out.println("Gemini extracted buyer:");
            System.out.println("Buyer Name: " + buyer.getBuyerName());
            System.out.println("Company Name: " + buyer.getCompanyName());
            System.out.println("Email: " + buyer.getEmail());
            System.out.println("Website: " + buyer.getWebsite());
            System.out.println("Country: " + buyer.getCountry());

            boolean saved = saveExtractedBuyer(buyer);

            System.out.println("Buyer saved successfully: " + saved);

            return saved;

        } catch (Exception e) {
            System.out.println("Buyer extraction failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


public int extractAndSaveFromTavily(String searchedProduct) {

    int savedCount = 0;
    
    String cleanProduct = searchedProduct
            .replaceAll(
                    "(?i)\\s+buyers\\s+importers\\s+wholesalers\\s+USA\\s*$",
                    ""
            )
            .trim();
    try {

        System.out.println("Searching buyers for: " + searchedProduct);

        String query = searchedProduct
                + " buyers importers companies USA";

        String response =
                tavilySearchService.searchBuyers(query);

        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(response);

        JsonNode results = root.path("results");

        for (JsonNode result : results) {

            String content = result.path("content").asText("");
            
            testMultipleBuyerExtraction(content, searchedProduct);
            
            String url = result.path("url").asText("");

            if (content.isBlank()) {
                continue;
            }

            System.out.println("TAVILY CONTENT: " + content);


         // ---------------------------------
         // 1. Extract and save company names
         // ---------------------------------

         Set<String> companyNames = extractAllCompanyNames(content);

         // Extract company names from contextual phrases
         String[] companyPatterns = {
             "(?i)interest in\\s+([A-Z][A-Za-z0-9&.,' -]{2,50}?)(?:[.!?,]|\\s+(?:We|Our|Your|The)\\b|$)",
             "(?i)company name\\s*:\\s*([A-Z][A-Za-z0-9&.,' -]{2,50})",
             "(?i)provided by\\s+([A-Z][A-Za-z0-9&.,' -]{2,50}?)(?:[.!?,]|$)"
         };

         for (String companyPattern : companyPatterns) {

             java.util.regex.Matcher companyMatcher =
                     java.util.regex.Pattern.compile(companyPattern)
                             .matcher(content);

             while (companyMatcher.find()) {

                 String name = companyMatcher.group(1).trim();

                 if (isValidCompanyName(name)
                         && !companyNames.contains(name)) {

                     companyNames.add(name);
                 }
             }
         }

         System.out.println("EXTRACTED COMPANY NAMES: " + companyNames);

         for (String companyName : companyNames) {

             System.out.println("Company candidate detected: " + companyName);

             if (!buyerRepository.existsByCompanyNameIgnoreCase(companyName)) {


Buyer companyBuyer = new Buyer();

// Find official company website
String website = extractOfficialWebsite(companyName);

if (website != null && !website.isBlank()) {

    companyBuyer.setWebsite(website);

    // Extract email addresses from website
    Set<String> websiteEmails =
            extractEmailsFromWebsite(website);

    if (!websiteEmails.isEmpty()) {

        String email = websiteEmails.iterator().next();

        companyBuyer.setEmail(email);
        companyBuyer.setEmailStatus("VALID");

        System.out.println(
                "Company email found: " + email
        );

    } else {

        companyBuyer.setEmailStatus("NOT_AVAILABLE");
    }

} else {

    companyBuyer.setEmailStatus("NOT_AVAILABLE");
}

                 companyBuyer.setBuyerType("BUSINESS");
                 companyBuyer.setProductRequirement(cleanProduct);
                	       
                 companyBuyer.setProductRelevance("POTENTIAL");
                 companyBuyer.setSource("TAVILY");
                 companyBuyer.setSourceUrl(url);

                 buyerRepository.save(companyBuyer);

                 savedCount++;

                 System.out.println("Company saved: " + companyName);

             } else {

                 System.out.println("Duplicate company skipped: " + companyName);
             }
         }

            // ---------------------------------
            // 2. Extract individual buyer sections
            // ---------------------------------

         String[] sections = content.split(
        	        "(?m)(?=^\\s*#{1,5}\\s+)"
        	);
            
            System.out.println("TOTAL BUYER SECTIONS: " + sections.length);

            for (String section : sections) {

                String product = "";
                boolean oldFormat = false;
                

             // Go4WorldBusiness buyer format
             java.util.regex.Matcher go4Matcher =
                     java.util.regex.Pattern.compile(
                             "(?is)Contact\\s*:\\s*([^\\r\\n]+).*?Buyer\\s+Of\\s+([^\\r\\n]+)"
                     ).matcher(section);

             if (go4Matcher.find()) {

                 String go4BuyerName = go4Matcher.group(1).trim();
                 String go4Product = go4Matcher.group(2).trim();

                 System.out.println("Go4WorldBusiness Buyer: " + go4BuyerName);
                 System.out.println("Go4WorldBusiness Product: " + go4Product);
             }


                // Old buyer-directory format
                java.util.regex.Matcher matcher =
                        java.util.regex.Pattern.compile(
                                "(?is)Inquire Now\\s+(.+?)\\s+Buyer Of\\b"
                        ).matcher(section);

                if (matcher.find()) {

                    product = matcher.group(1).trim();
                    oldFormat = true;
                }

                // New Tradewheel format
                boolean newFormat =
                        java.util.regex.Pattern.compile(
                        		"(?m)^\\s*[A-Z][A-Za-z'.-]*(?:\\s+[A-Z][A-Za-z'.-]*){0,4}\\s*\\R\\s*(?:USA|United States|India|United Kingdom)\\s*\\R\\s*Date Posted:"
                        ).matcher(section).find();
             // Go4WorldBusiness format
                java.util.regex.Matcher go4FormatMatcher =
                        java.util.regex.Pattern.compile(
                                "(?is)Contact\\s*:\\s*([^\\r\\n]+).*?Buyer\\s+Of\\s+([^\\r\\n]+)"
                        ).matcher(section);

                boolean go4Format = go4FormatMatcher.find();

                if (go4Format) {
                	product = go4FormatMatcher.group(2).trim();
                    System.out.println("Go4WorldBusiness format detected");
                }

                if (!oldFormat && !newFormat && !go4Format) {
                    continue;
                }

                if (product.isBlank() && newFormat) {
                	product = "Bamboo Handicrafts";
                }

                if (product.isBlank()) {
                    continue;
                }

                // Check product relevance
                boolean matched = false;

                String[] keywords = searchedProduct
                        .toLowerCase()
                        .split("\\s+");

                for (String keyword : keywords) {

                    if (keyword.length() >= 3
                            && product.toLowerCase().contains(keyword)) {

                        matched = true;
                        break;
                    }
                }

                if (!matched) {

                    System.out.println(
                            "Skipped unrelated product: " + product
                    );

                    continue;
                }

                // Create buyer
                Buyer buyer = new Buyer();

                buyer.setProductRequirement(cleanProduct);
                        

                buyer.setProductRelevance(
                        newFormat ? "POTENTIAL" : "MATCHED"
                );

                buyer.setSource("TAVILY");

                if (!url.isBlank()) {
                    buyer.setSourceUrl(url);
                }

                // Extract buyer name
                String buyerName =
                        extractBuyerNameFromContent(section);

                if (buyerName.isBlank()) {
                    System.out.println("Skipped buyer because name is blank");
                    continue;
                }
                
                if (!buyerName.isBlank()) {
                    buyer.setBuyerName(buyerName);
                }

                // Extract company name
                String company =
                        extractCompanyNameFromContent(section);

                if (!company.isBlank()
                        && isValidCompanyName(company)) {

                    buyer.setCompanyName(company);
                    buyer.setBuyerType("BUSINESS");
                }

                // Extract country
                String country =
                        extractCountryFromContent(section);

                if (!country.isBlank()) {
                    buyer.setCountry(country);
                }

                // Extract email
                Set<String> emails = extractEmails(section);

                System.out.println(
                        "Extracted emails: " + emails
                );

                if (!emails.isEmpty()) {
                    buyer.setEmail(emails.iterator().next());
                }

                System.out.println(
                        "Buyer detected: " + buyerName
                );

                System.out.println(
                        "Product detected: " + product
                );

                // Check existing buyer
                java.util.List<Buyer> existingBuyers =
                        buyerRepository
                                .findAllByBuyerNameIgnoreCaseAndSourceUrlIgnoreCase(
                                        buyerName, url
                                );

                if (!existingBuyers.isEmpty()) {

                    Buyer oldBuyer = existingBuyers.get(0);

                    if (buyer.getCompanyName() != null
                            && !buyer.getCompanyName().isBlank()) {

                        oldBuyer.setCompanyName(
                                buyer.getCompanyName()
                        );

                        oldBuyer.setBuyerType("BUSINESS");
                    }

                    if (buyer.getCountry() != null
                            && !buyer.getCountry().isBlank()) {

                        oldBuyer.setCountry(
                                buyer.getCountry()
                        );
                    }

                    if (buyer.getEmail() != null
                            && !buyer.getEmail().isBlank()) {

                        oldBuyer.setEmail(buyer.getEmail());
                    }

                    oldBuyer.setProductRequirement(cleanProduct);
                    oldBuyer.setProductRelevance(
                            buyer.getProductRelevance()
                    );

                    buyerRepository.save(oldBuyer);

                    System.out.println(
                            "Existing buyer updated: " + buyerName
                    );

                } else if (saveExtractedBuyer(buyer)) {

                    savedCount++;

                    System.out.println(
                            "Buyer saved successfully: " + buyerName
                    );
                }
            }
        }

    } catch (Exception e) {

        System.out.println(
                "Tavily extraction error: " + e.getMessage()
        );

        e.printStackTrace();
    }

    System.out.println(
            "Total buyers saved: " + savedCount
    );

    return savedCount;
}


private void testMultipleBuyerExtraction(String content, String searchedProduct) {

    if (content == null || content.isBlank()) {
        return;
    }

    java.util.regex.Pattern pattern =
            java.util.regex.Pattern.compile(
                    "(?m)^\\s*([A-Z][A-Za-z'.-]*(?:\\s+[A-Z][A-Za-z'.-]*){0,3})\\s*\\R\\s*(?:USA|United States)\\s*\\R\\s*Date Posted:"
            );

    java.util.regex.Matcher matcher = pattern.matcher(content);

    System.out.println("TEST CONTENT START");
    System.out.println(content);
    System.out.println("TEST CONTENT END");
    int buyerCount = 0;

    while (matcher.find()) {
        buyerCount++;

        String buyerName = matcher.group(1).trim();

        System.out.println("Buyer " + buyerCount + ": " + buyerName);
        System.out.println("Product: " + searchedProduct);
    }

    System.out.println("Total buyer requests detected: " + buyerCount);
}


private String extractBuyerNameFromContent(String content) {

    if (content == null || content.isBlank()) {
        return "";
    }

    String[] patterns = {

        // Format 1: Heading followed by Buyer From
        "(?m)^\\s*#{1,6}\\s*([A-Z][A-Za-z'.-]*(?:\\s+[A-Z][A-Za-z'.-]*){1,4})\\s*\\R\\s*Buyer From\\b",

        // Format 2: Name followed by country and Date Posted
        "(?m)^\\s*([A-Z][A-Za-z'.-]*(?:\\s+[A-Z][A-Za-z'.-]*){0,4})\\s*\\R\\s*(?:USA|United States|India|United Kingdom)\\s*\\R\\s*Date Posted:",

        // Format 3: Explicit buyer/contact name
        "(?i)(?:buyer|contact)\\s*(?:name)?\\s*[:\\-]\\s*([A-Z][A-Za-z'.-]*(?:\\s+[A-Z][A-Za-z'.-]*){1,3})"
    };

    String[] unwantedNames = {
        "features", "menu", "home", "about",
        "contact", "products", "services",
        "reviews", "search", "login",
        "register", "website", "directions",
        "usa", "united states", "scribd",
        "handicraft importers directory"
    };

    for (String regex : patterns) {

        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile(regex).matcher(content);

        while (matcher.find()) {

            String name = matcher.group(1).trim();

            if (name.matches(".*\\d.*")) {
                continue;
            }

            if (name.length() < 3 || name.length() > 60) {
                continue;
            }

            boolean invalid = false;

            for (String unwanted : unwantedNames) {
                if (name.equalsIgnoreCase(unwanted)) {
                    invalid = true;
                    break;
                }
            }

            if (!invalid) {
                return name;
            }
        }
    }

    return "";
}



private java.util.List<String> extractAllBuyerNames(String content) {

    java.util.List<String> buyerNames = new java.util.ArrayList<>();

    if (content == null || content.isBlank()) {
        return buyerNames;
    }

    String[] sections = content.split("(?i)Date Posted:");

    for (String section : sections) {

    	String buyerName = "";

    	java.util.regex.Matcher tradewheelMatcher =
    	        java.util.regex.Pattern.compile(
    	                "(?m)^\\s*([A-Z][A-Za-z'.-]*(?:\\s+[A-Z][A-Za-z'.-]*){0,3})\\s*\\R\\s*(?:USA|United States)\\s*\\R\\s*Date Posted:"
    	        ).matcher(section);

    	if (tradewheelMatcher.find()) {
    	    buyerName = tradewheelMatcher.group(1).trim();
    	}
        if (!buyerName.isBlank()
                && !buyerNames.contains(buyerName)) {

            buyerNames.add(buyerName);
        }
    }

    return buyerNames;
}



private boolean isValidCompanyName(String name) {

    if (name == null || name.isBlank()) {
        return false;
    }

    String value = name.trim().replaceAll("\\s+", " ");
    String lower = value.toLowerCase();

    // Length check
    if (value.length() < 3 || value.length() > 60) {
        return false;
    }

    // Reject URLs and email addresses
    if (lower.contains("http")
            || lower.contains("www.")
            || lower.contains("@")) {
        return false;
    }

    // Reject unwanted headings and common website text
    String[] unwantedNames = {
        "thank you", "thank you for", "contact buyer",
        "inquire now", "contact us", "about us",
        "read more", "learn more", "home page",
        "login", "register", "search results",
        "directory", "business directory",
        "supplier directory", "trade directory",
        "instagram", "facebook", "linkedin",
        "youtube", "wikipedia", "britannica",
        "tavily", "scribd", "mapquest",
        "uploaded by", "share this document",
        "also at this address", "store locator",
        "find a retailer near you", "reviews",
        "features", "navigation", "directions",
        "top collections", "best places",
        "menu", "customer service"
    };

    for (String unwanted : unwantedNames) {
        if (lower.equals(unwanted) || lower.contains(unwanted)) {
            return false;
        }
    }

    // Reject generic locations
    if (lower.matches("^(the )?(usa|united states|america|india|uk|united kingdom)$")) {
        return false;
    }

    // Reject buyer/search phrases
    if (lower.contains("looking for")
            || lower.contains("want to buy")
            || lower.contains("handicraft buyers")
            || lower.contains("importer contact list")
            || lower.contains("wholesale suppliers")
            || lower.contains("request for quotation")
            || lower.contains("buyer from")
            || lower.contains("buyer of")
            || lower.contains("date posted")) {
        return false;
    }

    return true;
}
  


private String extractProductRequirement(String content) {

    if (content == null || content.isBlank()) {
        return "";
    }

    String[] lines = content.split("\\R");

 // Extract product names from Go4WorldBusiness buyer listings
    java.util.regex.Pattern buyerProductPattern =
            java.util.regex.Pattern.compile(
                    "(?im)^\\s*(Bamboo [A-Za-z ]+)\\s*\\R\\s*Buyer Of Bamboo Products\\b"
            );

    java.util.regex.Matcher buyerProductMatcher =
            buyerProductPattern.matcher(content);

    if (buyerProductMatcher.find()) {
        return buyerProductMatcher.group(1).trim();
    }
    
    String productName = "";
    String productType = "";
    String quantity = "";

    for (String line : lines) {

        String value = line.trim();

        if (value.startsWith("Product Name:")) {
            productName = value.replace("Product Name:", "").trim();
        }

        else if (value.startsWith("Type:")) {
            productType = value.replace("Type:", "").trim();
        }

        else if (value.startsWith("Quantity Required :")) {
            quantity = value.replace("Quantity Required :", "").trim();
        }

        // Extract headings such as Wanted : Bamboo Yarn
        else if (value.toLowerCase().startsWith("## wanted")) {

            productName = value
                    .replaceFirst("(?i)^#+\\s*", "")
                    .replaceFirst("(?i)^wanted\\s*:\\s*", "")
                    .trim();
        }
    }

    if (!productName.isBlank()) {

        String result = productName;

        if (!productType.isBlank()) {
            result += " - " + productType;
        }

        if (!quantity.isBlank()) {
            result += " - Quantity: " + quantity;
        }

        return result;
    }

    return "";
}

    public int searchAndSaveBuyers(String query) {
        try {
            String searchResults = tavilySearchService.searchBuyers(query);

            System.out.println("TAVILY RESULTS: " + searchResults);

            return extractAndSaveFromTavily(searchResults);

        } catch (Exception e) {
            System.out.println("Buyer search failed: " + e.getMessage());
            return 0;
        }
    }
    
    private String cleanCompanyName(String title) {

        title = title.trim();

        // Remove text after common page-title separators
        String[] separators = {" | ", " - ", " – ", " — "};

        for (String separator : separators) {
            if (title.contains(separator)) {
                title = title.split(java.util.regex.Pattern.quote(separator))[0].trim();
                break;
            }
        }

        return title;
    }
    






private String extractCompanyNameFromContent(String content) {

    if (content == null || content.isBlank()) {
        return "";
    }

    String[] lines = content.split("\\R");

    // Pattern 1: Explicitly labelled company names
    java.util.regex.Pattern companyPattern =
            java.util.regex.Pattern.compile(
                    "(?i)company name is\\s*[:\\-]?\\s*([A-Z][A-Za-z0-9&.,' -]{2,50})"
            );

    for (String line : lines) {

        java.util.regex.Matcher matcher =
                companyPattern.matcher(line.trim());

        if (matcher.find()) {
            String company = matcher.group(1).trim();

            if (isValidCompanyName(company)) {
                return company;
            }
        }
    }

    // Pattern 2: Company names in buyer headings
    java.util.regex.Pattern headingPattern =
            java.util.regex.Pattern.compile(
                    "(?m)^\\s*#{1,6}\\s*([A-Z][A-Za-z0-9&.,'-]*(?:\\s+[A-Z][A-Za-z0-9&.,'-]*){1,4})\\s*\\R\\s*Buyer From\\b"
            );

    java.util.regex.Matcher headingMatcher =
            headingPattern.matcher(content);

    while (headingMatcher.find()) {

        String heading = headingMatcher.group(1).trim();
        String lower = heading.toLowerCase();

        boolean businessIndicator =
                lower.matches(".*\\b(llc|inc|ltd|limited|corp|corporation|company|co|importer|solutions|organics|group|enterprises|trading)\\b.*");

        if (businessIndicator && isValidCompanyName(heading)) {
            return heading;
        }
    }

    // Pattern 3: Company names on separate lines
    java.util.regex.Pattern businessPattern =
            java.util.regex.Pattern.compile(
                    "(?m)^\\s*([A-Z][A-Za-z0-9&.,'-]*(?:\\s+[A-Z][A-Za-z0-9&.,'-]*){0,4}\\s+(?:Inc\\.?|LLC|Ltd\\.?|Limited|Corp\\.?|Corporation|Group|Enterprises|Trading))\\s*$",
                    java.util.regex.Pattern.CASE_INSENSITIVE
            );

    java.util.regex.Matcher businessMatcher =
            businessPattern.matcher(content);

    while (businessMatcher.find()) {

        String company = businessMatcher.group(1).trim();

        if (isValidCompanyName(company)) {
            return company;
        }
    }

    // Pattern 4: Company names inside paragraphs or lists
    java.util.regex.Pattern inlineCompanyPattern =
            java.util.regex.Pattern.compile(
                    "\\b([A-Z][A-Za-z0-9&'-]*(?:\\s+[A-Z][A-Za-z0-9&'-]*){0,4}\\s+(?:Inc\\.?|LLC|Ltd\\.?|Limited|Corp\\.?|Corporation|Group|Enterprises|Trading))\\b",
                    java.util.regex.Pattern.CASE_INSENSITIVE
            );

    java.util.regex.Matcher inlineMatcher =
            inlineCompanyPattern.matcher(content);

    while (inlineMatcher.find()) {

        String company = inlineMatcher.group(1).trim();

        if (isValidCompanyName(company)) {
            return company;
        }
    }

    return "";
}


private java.util.List<String> extractCompanyNamesFromList(String content) {

    java.util.List<String> companies = new java.util.ArrayList<>();

    if (content == null || content.isBlank()) {
        return companies;
    }

    java.util.regex.Pattern pattern =
            java.util.regex.Pattern.compile(
                    "\\b([A-Z][A-Za-z&'-]*(?:\\s+[A-Z][A-Za-z&'-]*){0,4}\\s+(?:Inc\\.?|LLC|Ltd\\.?|Limited|Corp\\.?|Corporation|Group|Enterprises|Trading))\\b"
            );

    java.util.regex.Matcher matcher = pattern.matcher(content);

    while (matcher.find()) {

        String company = matcher.group(1).trim();

        if (isValidCompanyName(company)
                && !companies.contains(company)) {

            companies.add(company);
        }
    }

    return companies;
}



    private String extractCountryFromContent(String content) {

        String[] countries = {
            "United States", "USA", "Germany", "England",
            "United Kingdom", "Netherlands", "France",
            "Spain", "Italy", "Canada", "Australia",
            "India", "Slovakia", "Ireland"
        };

        String lowerContent = content.toLowerCase();

        for (String country : countries) {
            if (lowerContent.contains(country.toLowerCase())) {
                return country;
            }
        }

        return "";
    }
    
    private String extractOfficialWebsite(String companyName) {


    	    try {

    	        System.out.println("Searching website for: " + companyName);

    	        String query = companyName + " official website";
    	        
    	        System.out.println("Website search query: " + query);

            String searchResponse =
                    tavilySearchService.searchBuyers(query);

            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(searchResponse);

            JsonNode results = root.path("results");

            for (JsonNode result : results) {

                String url = result.path("url").asText("");

                String title = result.path("title").asText("");

                if (!url.isBlank()
                        && title.toLowerCase().contains(
                                companyName.toLowerCase())) {

                    return url;
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Website search error: " + e.getMessage()
            );
        }

        return "";
    }
    

private Set<String> extractAllCompanyNames(String content) {

    Set<String> companyNames = new LinkedHashSet<>();

    if (content == null || content.isBlank()) {
        return companyNames;
    }

    String[] lines = content.split("\\R");

    for (String line : lines) {

        String companyName = line.trim();

        // Remove bullets and heading symbols
        companyName = companyName.replaceAll("^[#*\\-\\s]+", "");

        // Remove extra spaces
        companyName = companyName.replaceAll("\\s+", " ");

        if (companyName.isBlank()) {
            continue;
        }

        // Check name length
        if (companyName.length() < 3 || companyName.length() > 60) {
            continue;
        }

        // Reject lines containing labels or unwanted information
        if (companyName.matches("(?i).*\\b(type|contact|shipping terms|payment terms|destination port|product name|quantity|required|specifications|buyer from|buyer of|inquire now|date posted|delivery port|archived|categories|suppliers|products search|amazon services|post rfq|thank you|read more|login|register)\\b.*")) {
            continue;
        }

        // Reject URLs, emails, phone numbers and dates
        if (companyName.matches("(?i).*(@|https?://|www\\.|\\+?\\d{5,}|\\b\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4}\\b).*")) {
            continue;
        }

        // Reject product-related descriptions
        if (companyName.toLowerCase().contains("bamboo")) {
            continue;
        }

        // Keep only names with a recognizable business indicator
        if (!companyName.matches(
                "(?i).*\\b(inc\\.?|llc|ltd\\.?|limited|corp\\.?|corporation|company|co\\.?|group|enterprises|industries|trading|exports|imports)\\b.*")) {
            continue;
        }

        if (isValidCompanyName(companyName)
                && !companyNames.contains(companyName)) {

            companyNames.add(companyName);
        }
    }

    return companyNames;
}


private Set<String> extractEmailsFromWebsite(String websiteUrl) {

    Set<String> emails = new LinkedHashSet<>();

    if (websiteUrl == null || websiteUrl.isBlank()) {
        return emails;
    }

    try {
        System.out.println("Checking company website: " + websiteUrl);

        String pageContent = Jsoup
                .connect(websiteUrl)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get()
                .text();

        
        emails.addAll(extractEmails(pageContent));

        System.out.println("Website emails found: " + emails);

    } catch (Exception e) {
        System.out.println(
                "Website email extraction error: " + e.getMessage()
        );
    }

    return emails;
}


    }