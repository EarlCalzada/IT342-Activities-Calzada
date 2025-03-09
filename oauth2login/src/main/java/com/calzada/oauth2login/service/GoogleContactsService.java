package com.calzada.oauth2login.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GoogleContactsService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleContactsService.class);

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final WebClient webClient;

    @Autowired
    public GoogleContactsService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
        this.webClient = WebClient.builder().build();
    }

    public Object getContacts(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName());

        if (client == null || client.getAccessToken() == null) {
            logger.error("OAuth2 client or access token is null");
            return null;
        }

        String accessToken = client.getAccessToken().getTokenValue();

        // Google People API endpoint for contacts with phone numbers
        String url = "https://people.googleapis.com/v1/people/me/connections" +
                "?personFields=names,emailAddresses,phoneNumbers" +
                "&pageSize=100";

        try {
            return webClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (Exception e) {
            logger.error("Failed to retrieve contacts", e);
            return null;
        }
    }
}