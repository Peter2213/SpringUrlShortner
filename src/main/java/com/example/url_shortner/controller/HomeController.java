package com.example.url_shortner.controller;

import com.example.url_shortner.dto.ShortenUrlRequest;
import com.example.url_shortner.dto.ShortenUrlResponse;
import com.example.url_shortner.entity.ShortUrl;
import com.example.url_shortner.service.UrlShortenerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.validation.Valid;
import java.util.Optional;

@Controller
public class HomeController {

    private final UrlShortenerService urlShortenerService;

    public HomeController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }


    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("shortenUrlRequest", new ShortenUrlRequest());
        return "index";
    }


    @PostMapping("/shorten")
    public String shortenUrl(@Valid ShortenUrlRequest request, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "index";
        }

        try {
            ShortUrl shortUrl = urlShortenerService.createShortUrl(request.getUrl());
            
            ShortenUrlResponse response = new ShortenUrlResponse(
                    shortUrl.getShortCode(),
                    "http://localhost:8080/" + shortUrl.getShortCode(),
                    shortUrl.getOriginalUrl(),
                    shortUrl.getCreatedAt(),
                    shortUrl.getExpiresAt()
            );
            
            model.addAttribute("response", response);
            model.addAttribute("shortenUrlRequest", new ShortenUrlRequest());
            return "result";
        } catch (Exception e) {
            model.addAttribute("error", "Error creating short URL: " + e.getMessage());
            return "index";
        }
    }

    @GetMapping("/{shortCode}")
    public RedirectView redirectToOriginalUrl(@PathVariable String shortCode) {
        Optional<String> originalUrl = urlShortenerService.getOriginalUrl(shortCode);
        
        if (originalUrl.isPresent()) {
            return new RedirectView(originalUrl.get());
        }
        
        RedirectView redirectView = new RedirectView("/");
        redirectView.setStatusCode(org.springframework.http.HttpStatus.NOT_FOUND);
        return redirectView;
    }
}
