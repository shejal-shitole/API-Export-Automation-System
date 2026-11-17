
package com.company.exportautomation.service;

import java.util.regex.Pattern;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.company.exportautomation.entity.Buyer;
import com.company.exportautomation.repository.BuyerRepository;

@Service
public class BuyerService {
	
	public Buyer updateBuyer(Long id, Buyer updatedBuyer) {

	    Buyer existingBuyer = buyerRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Buyer not found"));

	    existingBuyer.setBuyerName(updatedBuyer.getBuyerName());
	    existingBuyer.setCompanyName(updatedBuyer.getCompanyName());
	    existingBuyer.setEmail(updatedBuyer.getEmail());
	    existingBuyer.setCountry(updatedBuyer.getCountry());
	    existingBuyer.setWebsite(updatedBuyer.getWebsite());

	    return buyerRepository.save(existingBuyer);
	}

    private final BuyerRepository buyerRepository;

    public BuyerService(BuyerRepository buyerRepository) {
        this.buyerRepository = buyerRepository;
    }

    // Add buyer
    public Buyer saveBuyer(Buyer buyer) {

        String status = validateEmail(buyer.getEmail());

        buyer.setEmailStatus(status);

        return buyerRepository.save(buyer);
    }

    // Get all buyers
    public List<Buyer> getAllBuyers() {
        return buyerRepository.findAll();
    }

    // Get buyer by ID
    public Optional<Buyer> getBuyerById(Long id) {
        return buyerRepository.findById(id);
    }

    // Delete buyer
    public void deleteBuyer(Long id) {
        buyerRepository.deleteById(id);
    }
    

    public String validateEmail(String email) {

    	if (email == null || email.trim().isEmpty()) {
        return "MISSING";
    	}

    	String emailPattern =
    			"^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    	if (Pattern.matches(emailPattern, email.trim())) {
    		return "VALID";
    	}

    	return "INVALID";
}

}
