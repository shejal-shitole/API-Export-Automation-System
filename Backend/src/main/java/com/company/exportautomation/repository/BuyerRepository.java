package com.company.exportautomation.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.company.exportautomation.entity.Buyer;

public interface BuyerRepository extends JpaRepository<Buyer, Long> {

    boolean existsByEmail(String email);
    
    boolean existsByWebsite(String website);
    
    boolean existsByCompanyNameIgnoreCase(String companyName);

    boolean existsByBuyerNameIgnoreCaseAndWebsiteIgnoreCase(
            String buyerName, String website);
    
    boolean existsByBuyerNameIgnoreCaseAndSourceUrlIgnoreCase(
            String buyerName, String sourceUrl);
    
    List<Buyer> findAllByBuyerNameIgnoreCaseAndSourceUrlIgnoreCase(
            String buyerName, String sourceUrl);
}
