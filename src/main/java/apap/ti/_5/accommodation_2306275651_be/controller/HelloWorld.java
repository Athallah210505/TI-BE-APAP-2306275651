package apap.ti._5.accommodation_2306275651_be.controller;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
public class HelloWorld {

    /**
     * Handles GET requests to the root path
     * @param model the Model to add attributes to
     * @return the view name
     */

     @GetMapping("/")
     public String helloWorld(Model model) {
        model.addAttribute("Welcome", "Welcome to Accommodation Booking Service");
        return "beranda"; 
     }
     
     
    
}
