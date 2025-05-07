package com.ampeliodev.chatbottwiliospringboot.controller;

import com.ampeliodev.chatbottwiliospringboot.domain.EntidadCita;
import com.ampeliodev.chatbottwiliospringboot.service.ServicioDialogFlow;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.protobuf.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Message;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/twilio")
public class ControladorTwilio {

    private final ServicioDialogFlow servicioDialogFlow;

    public ControladorTwilio(ServicioDialogFlow servicioDialogFlow) {
        this.servicioDialogFlow = servicioDialogFlow;
    }



     //probar número de twilio al backend
    @PostMapping("/webhook")
    public ResponseEntity<String> recibbirMensajeWebhook(@RequestParam Map<String, String> params) {

        String mensaje = params.get("Body");
        String numero = params.get("From");

        System.out.println("Mensaje recibido de " + numero + ": " + mensaje);

        String respuesta = "<Response><Message>¡Hola! Gracias por tu mensaje 😊</Message></Response>";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(respuesta);
    }

    //integrar WA-Twilio - backend y DF
    @PostMapping(value = "/dialogflowwebhook", produces = "application/xml")
    public ResponseEntity<String> receiveMessage(@RequestParam Map<String, String> body) {
        System.out.println("mensaje desde el controlador");

        String bodyText = body.get("Body"); // Texto del mensaje
        DetectIntentResponse respuesta = servicioDialogFlow.detectIntent(bodyText);
        String intentName = respuesta.getQueryResult().getIntent().getDisplayName();

        String respuestaTexto;
        switch (intentName) {
            case "establecer_dia_cita":
                Map<String, String> parametroscita = servicioDialogFlow.obtenerParametrosIntent(respuesta);

                String dia = parametroscita.get("dia");
                String mes = parametroscita.get("mes");

                System.out.println("mes: " + mes + "día" + dia);
                List<EntidadCita> horarios = servicioDialogFlow.consultarHorariosDisponibles(dia, mes);

                StringBuilder mensaje = new StringBuilder();

                if (horarios.isEmpty()) {
                    mensaje.append("Lo siento, no hay horarios disponibles para esa fecha.");
                } else {
                    mensaje.append("Los horarios disponibles para esa fecha son:\n" + "\n");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

                    for (EntidadCita cita : horarios) {
                        LocalTime hora = cita.getHora();

                        // Formatear la hora
                        String horaFormateada = hora.format(formatter);

                        mensaje.append("• ").append(horaFormateada).append("\n");
                    }
                }
                String respuestaDialogFlow = servicioDialogFlow.obtenerRespuestaTextoCompleto(respuesta);
                String mensajeHorarios = mensaje.toString();
                respuestaTexto =  respuestaDialogFlow + "\n" + "\n" +mensajeHorarios +"\n" + "\n"+ "Índicame por favor, ¿A qué hora te gustaría tu cita?";


                break;
            case "saludo_inicial":
                System.out.println("mensaje desde el controlador saludo inicial");
                respuestaTexto = servicioDialogFlow.obtenerRespuestaTextoCompleto(respuesta);
                System.out.println(respuestaTexto);
                break;
            default:
                respuestaTexto = servicioDialogFlow.obtenerRespuestaTextoCompleto(respuesta);
                break;
        }

        Body bodyXml = new Body.Builder(respuestaTexto).build();
        Message message = new Message.Builder().body(bodyXml).build();
        MessagingResponse twiml = new MessagingResponse.Builder().message(message).build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(twiml.toXml());
    }
}
