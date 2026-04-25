package com.example.url_shortner.service;

import com.example.url_shortner.entity.ShortUrl;
import com.example.url_shortner.repository.ShortUrlRepository;
import com.example.url_shortner.util.ShortCodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UrlShortenerService {

    private final ShortUrlRepository shortUrlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private static final int MAX_COLLISION_ATTEMPTS = 10;

    public UrlShortenerService(ShortUrlRepository shortUrlRepository, ShortCodeGenerator shortCodeGenerator) {
        this.shortUrlRepository = shortUrlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
    }

    @Transactional
    public ShortUrl createShortUrl(String originalUrl) {
        // Check if URL already exists
        Optional<ShortUrl> existingUrl = shortUrlRepository.findByOriginalUrl(originalUrl);
        if (existingUrl.isPresent()) {
            return existingUrl.get();
        }

        String shortCode = generateUniqueCode();

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl(originalUrl);
        shortUrl.setShortCode(shortCode);

        return shortUrlRepository.save(shortUrl);
    }

 
    @Transactional(readOnly = true)
    public Optional<String> getOriginalUrl(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode)
                .map(ShortUrl::getOriginalUrl);
    }


    private String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_COLLISION_ATTEMPTS; attempt++) {
            String code = shortCodeGenerator.generate();

            Optional<ShortUrl> existing = shortUrlRepository.findByShortCode(code);
            if (existing.isEmpty()) {
                return code;
            }
        }

        throw new IllegalArgumentException("Unable to generate unique short code after " + MAX_COLLISION_ATTEMPTS + " attempts");
    }
}
