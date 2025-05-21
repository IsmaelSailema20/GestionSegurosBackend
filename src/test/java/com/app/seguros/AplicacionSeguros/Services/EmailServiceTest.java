package com.app.seguros.AplicacionSeguros.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailService = new EmailService(restTemplate);
        ReflectionTestUtils.setField(emailService, "apiKey", "fake-api-key");
        ReflectionTestUtils.setField(emailService, "messagesUrl", "https://api.mailgun.net/v3/testdomain/messages");
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@test.com");
    }

    @Test
    void enviarCorreo_exito() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>("OK", HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);

        assertDoesNotThrow(() -> {
            emailService.enviarCorreo("test@correo.com", "Prueba", "Cuerpo del correo");
        });

        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void enviarCorreo_fallaHttp() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);

        assertDoesNotThrow(() -> {
            emailService.enviarCorreo("test@correo.com", "Asunto", "Mensaje");
        });

        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void enviarCorreo_lanzaExcepcion() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Fallo al conectar"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailService.enviarCorreo("test@correo.com", "Asunto", "Mensaje");
        });

        assertTrue(exception.getMessage().contains("Error al enviar correo"));
    }
}
