package com.ampeliodev.chatbottwiliospringboot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/twilio/prueba")
public class ControladorTwilioPrueba {

    //probar número de twilio al backend
    @PostMapping("/webhook")
    public ResponseEntity<String> recibirMensajeWebhook(@RequestParam Map<String, String> params) {

        String mensaje = params.get("Body");
        String numero = params.get("From");

        System.out.println("Mensaje recibido de " + numero + ": " + mensaje);

        String respuesta = "<Response><Message>¡Hola! Gracias por tu mensaje 😊</Message></Response>";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(respuesta);
    }

}
