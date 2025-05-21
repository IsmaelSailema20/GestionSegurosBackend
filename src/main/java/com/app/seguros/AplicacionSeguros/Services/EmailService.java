package com.app.seguros.AplicacionSeguros.Services;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    @Value("${mailgun.api-key}")
    private String apiKey;

    @Value("${mailgun.messages-url}")
    private String messagesUrl;

    @Value("${mailgun.from-email}")
    private String fromEmail;

    private final RestTemplate restTemplate;

    public EmailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void enviarCorreo(String destinatario, String asunto, String cuerpo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth("api", apiKey);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("from", fromEmail);
        body.add("to", destinatario);
        body.add("subject", asunto);
        body.add("text", cuerpo);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(messagesUrl, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Correo enviado a: " + destinatario);
            } else {
                System.err.println("Error al enviar correo: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error al enviar correo: " + e.getMessage());
            throw new RuntimeException("Error al enviar correo: " + e.getMessage());
        }
    }
}