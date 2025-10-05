package com.example.localityconnector.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GeoTestController {

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    @GetMapping("/geo-test")
    public String geoTest(Model model) {
        model.addAttribute("googleKey", googleMapsApiKey);
        return "geo-test";
    }
}




