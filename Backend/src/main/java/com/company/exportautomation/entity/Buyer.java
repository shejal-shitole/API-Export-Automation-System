
package com.company.exportautomation.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "buyers")
public class Buyer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String buyerName;

    private String companyName;

    private String email;
    
    private String emailStatus;
    
    private String buyerType;

    private String website;
    
    private String sourceUrl;

    private String country;

    private String source;

    
    @Column(name = "product_requirement", length = 500)
    private String productRequirement;
    
    @Column(name = "product_relevance")
    private String productRelevance;
    // Default constructor
    public Buyer() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
    
    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    
    public String getEmailStatus() {
        return emailStatus;
    }

    public void setEmailStatus(String emailStatus) {
        this.emailStatus = emailStatus;
    }
    
    public String getBuyerType() {
        return buyerType;
    }

    public void setBuyerType(String buyerType) {
        this.buyerType = buyerType;
    }
    
    public String getProductRequirement() {
        return productRequirement;
    }

    public void setProductRequirement(String productRequirement) {
        this.productRequirement = productRequirement;
    }
    
    public String getProductRelevance() {
        return productRelevance;
    }

    public void setProductRelevance(String productRelevance) {
        this.productRelevance = productRelevance;
    }
}



