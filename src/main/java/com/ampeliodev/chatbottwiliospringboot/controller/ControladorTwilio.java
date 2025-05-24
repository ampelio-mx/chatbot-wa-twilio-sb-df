package com.ampeliodev.chatbottwiliospringboot.controller;

import com.ampeliodev.chatbottwiliospringboot.domain.EntidadCita;
import com.ampeliodev.chatbottwiliospringboot.dto.DtoCita;
import com.ampeliodev.chatbottwiliospringboot.service.ServicioDialogFlow;
import com.ampeliodev.chatbottwiliospringboot.service.ServicioSesion;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import com.twilio.rest.api.v2010.account.Message;
import java.util.List;
import java.util.Map;
import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;


@Slf4j
@RestController
@RequestMapping("/api/twilio")
public class ControladorTwilio {

    //VARIABLES TWILIO
    //region
    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;
    //endregion

    //BEANS
    //region
    @Autowired
    private ServicioDialogFlow servicioDialogFlow;

    @Autowired
    private ServicioSesion servicioSesion;
    //endregion

    //CONSTRUCTOR
    //region
    public ControladorTwilio(ServicioDialogFlow servicioDialogFlow) {
        this.servicioDialogFlow = servicioDialogFlow;
    }
    //endregion

    //integrar WA-Twilio - backend y DF
    @PostMapping("/dialogflowwebhook")
    public ResponseEntity<Void> receiveMessage(@RequestParam Map<String, String> bodyTwilio) {

        System.out.println("Controlador recibiendo petición");

        String mensajeUsuario = bodyTwilio.get("Body");
        String telefonoUsuario = bodyTwilio.get("From");

        // Buscar sesión previa
        DtoCita datosSesion = servicioSesion.obtenerSesion(telefonoUsuario);
        String respuestaDevueltaTexto = "";

        //Si sesion no es nula, procesa con el back
        //region

        if (datosSesion != null) {

            DetectIntentResponse mensajeCompletoDialogFlow = servicioDialogFlow.detectIntent(mensajeUsuario);
            String nombreDelIntent = mensajeCompletoDialogFlow.getQueryResult().getIntent().getDisplayName();
            System.out.println("controlador, sesión nula");
            // Flujos según etapa del usuario


            switch (datosSesion.getEtapa()) {

                case "ESPERANDO_MES":

                    int mensajeMes = Integer.parseInt(mensajeUsuario);
                    datosSesion.setMes(mensajeMes);
                    datosSesion.setEtapa("ESPERANDO_DIA");
                    servicioSesion.guardarSesion(telefonoUsuario, datosSesion);
                    respuestaDevueltaTexto = "Bien, indicame por favor el día";

                    break;

                case "ESPERANDO_DIA":

                    int mensajeDia = Integer.parseInt(mensajeUsuario);
                    datosSesion.setDia(mensajeDia);
                    datosSesion.setEtapa("ESPERANDO_ANIO");
                    servicioSesion.guardarSesion(telefonoUsuario, datosSesion);
                    respuestaDevueltaTexto = "Bien, indicame por favor el año";

                    break;

                case "ESPERANDO_ANIO":

                    int mensajeAnio = Integer.parseInt(mensajeUsuario);
                    datosSesion.setAnio(mensajeAnio);

                    servicioSesion.guardarSesion(telefonoUsuario, datosSesion);

                    int dia = datosSesion.getDia();
                    int mes = datosSesion.getMes();
                    int anio = datosSesion.getAnio();

                    List<EntidadCita> horariosDisponibles = servicioDialogFlow.consultarHorariosDisponibles(dia, mes, anio);

                    StringBuilder mensaje = new StringBuilder();

                    if (horariosDisponibles.isEmpty()) {
                        mensaje.append("Lo siento, no hay horarios disponibles para esa fecha.\n\nIngresa otro día, por favor...");
                        datosSesion.setEtapa("ESPERANDO_DIA");
                    } else {
                        datosSesion.setListaHorariosDisponibles(horariosDisponibles);
                        datosSesion.setEtapa("ESPERANDO_HORA");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
                        for (EntidadCita cita : horariosDisponibles) {
                            mensaje.append("• ").append(cita.getHora().format(formatter)).append("\n");
                        }
                        mensaje.append("\n\nIndícame por favor, ¿A qué hora te gustaría tu cita?");
                        mensaje.append("\n\n(Ingresa el horario con formato de 4 dígitos, sin letras (p.e. 09:00 o 14:00)");
                    
                    }

                    respuestaDevueltaTexto = "gracias mensaje enviado prueba";
                        break;

                case "ESPERANDO_HORA":

                    System.out.println("case ESPERANDO_HORA");
                    System.out.println("mensaje del usuario con la hora" + mensajeUsuario);

                    String hora = mensajeUsuario;

                    // Normalizar la cadena (eliminar espacios y convertir a formato 24h si es necesario)
                    hora = hora.replace(" ", ""); // "9AM" → "09:00:00"

                    StringBuilder mensajes = new StringBuilder();

                    // Si tiene AM/PM, convertir a formato 24h
                    if (!hora.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
                        mensajes.append("Formato inválido. Ingresa el horario en formato HH:mm (ej. 09:00 o 14:00).");
                    }

                    // Parsear a LocalTime
                    LocalTime horaR = LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"));

                    datosSesion.setHora(horaR); //como mensajeUsuario se va a asigar a variable LocalTime
                    datosSesion.setEtapa("ESPERANDO_NOMBRE");
                    servicioSesion.guardarSesion(telefonoUsuario, datosSesion);
                    respuestaDevueltaTexto = "Gracias. Ahora dime tu nombre, por favor.";
                    break;

                case "ESPERANDO_NOMBRE":
                    datosSesion.setNombre(mensajeUsuario);
                    datosSesion.setEtapa("ESPERANDO_APELLIDO");
                    servicioSesion.guardarSesion(telefonoUsuario, datosSesion);
                    respuestaDevueltaTexto = "Perfecto. ¿Cuál es tu apellido?";
                    break;

                case "ESPERANDO_APELLIDO":
                    datosSesion.setApellido(mensajeUsuario);
                    datosSesion.setEtapa("ESPERANDO_TELEFONO");
                    servicioSesion.guardarSesion(telefonoUsuario, datosSesion);
                    respuestaDevueltaTexto = "¿Me podrías indica tu teléfono a 10 dígitos?";
                    break;

                case "ESPERANDO_TELEFONO":
                    datosSesion.setTelefono(mensajeUsuario);
                    datosSesion.setEtapa("ESPERANDO_EMAIL");
                    servicioSesion.guardarSesion(telefonoUsuario, datosSesion);
                    respuestaDevueltaTexto = "Por último, ¿Me puedes indicar tu email?";
                    break;

                case "ESPERANDO_EMAIL":
                    datosSesion.setEmail(mensajeUsuario);
                    datosSesion.setEtapa("COMPLETADO");
                    servicioSesion.guardarSesion(telefonoUsuario, datosSesion);

                    StringBuilder mensajeConfirmacionCita = new StringBuilder();
                    mensajeConfirmacionCita.append("\n\n Te confirmo los datos de tu cita.\n\nIngresa otro día, por favor...");
                    String nombre = datosSesion.getNombre();
                    String apellido = datosSesion.getApellido();

                    System.out.println("nombre guardado: " +nombre + "apellido guardado: " + apellido);

                    // Aquí puedes guardar en base de datos
                    // servicioCita.guardar(datosSesion);
                    respuestaDevueltaTexto = "¡Listo! Tu cita ha sido agendada. Gracias.";
                    break;


                default:
                    respuestaDevueltaTexto = "Lo siento, no entendí. ¿Puedes repetirlo?";
                    break;
            }



        } else {

            // Si no hay sesión previa, procesar con Dialogflow
            DetectIntentResponse mensajeCompletoDialogFlow = servicioDialogFlow.detectIntent(mensajeUsuario);
            String nombreDelIntent = mensajeCompletoDialogFlow.getQueryResult().getIntent().getDisplayName();

            switch (nombreDelIntent) {

                case "solicitud_servicio_cita":

                        DtoCita dtoNuevaSesion = new DtoCita();
                        dtoNuevaSesion.setEtapa("ESPERANDO_MES");
                        servicioSesion.guardarSesion(telefonoUsuario, dtoNuevaSesion);

                        StringBuilder mensajeConfirmacionCita = new StringBuilder();
                        mensajeConfirmacionCita.append("\n\nIndícame por favor para qué mes te gustaría tu cita");
                        respuestaDevueltaTexto = mensajeConfirmacionCita.toString();

                    break;

                case "guardar_cita_base_datos":
                    Map<String, String> parametrosGuardar = servicioDialogFlow.obtenerParametrosIntent(mensajeCompletoDialogFlow);
                    String diaCita = parametrosGuardar.get("dia");
                    String mesCita = parametrosGuardar.get("mes");
                    String nombreCita = parametrosGuardar.get("nombre");
                    respuestaDevueltaTexto = servicioDialogFlow.obtenerRespuestaTextoCompleto(mensajeCompletoDialogFlow)
                            + "\nDía: " + diaCita + ", Mes: " + mesCita;
                    break;

                default:
                    respuestaDevueltaTexto = servicioDialogFlow.obtenerRespuestaTextoCompleto(mensajeCompletoDialogFlow);
                    break;
            }
        }

        // Enviar respuesta con Twilio
        Twilio.init(accountSid, authToken);
        Message.creator(
                new PhoneNumber(telefonoUsuario),
                new PhoneNumber(fromNumber),
                respuestaDevueltaTexto
        ).create();

        return ResponseEntity.ok().build();
    }
}
