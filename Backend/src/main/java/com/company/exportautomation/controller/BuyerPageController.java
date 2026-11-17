
package com.company.exportautomation.controller;

import com.company.exportautomation.entity.Buyer;
import com.company.exportautomation.service.BuyerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PathVariable;
import com.company.exportautomation.entity.Buyer;
import com.company.exportautomation.service.BuyerService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class BuyerPageController {

    private final BuyerService buyerService;

    public BuyerPageController(BuyerService buyerService) {
        this.buyerService = buyerService;
    }

    @GetMapping("/buyers")
    public String showBuyers(Model model) {

        model.addAttribute("buyers", buyerService.getAllBuyers());

        return "buyers";
    }

    @PostMapping("/buyers/save")
    public String saveBuyer(@ModelAttribute Buyer buyer) {

        buyerService.saveBuyer(buyer);

        return "redirect:/buyers";
    }


@PostMapping("/buyers/delete/{id}")
public String deleteBuyer(@PathVariable Long id) {

    buyerService.deleteBuyer(id);

    return "redirect:/buyers";
}

@GetMapping("/buyers/edit/{id}")
public String editBuyer(@PathVariable Long id, Model model) {

    Buyer buyer = buyerService.getBuyerById(id)
            .orElseThrow(() -> new RuntimeException("Buyer not found"));

    model.addAttribute("buyer", buyer);

    return "edit-buyer";
}

@PostMapping("/buyers/update")
public String updateBuyer(@ModelAttribute Buyer buyer) {

    buyerService.saveBuyer(buyer);

    return "redirect:/buyers";
}

}
