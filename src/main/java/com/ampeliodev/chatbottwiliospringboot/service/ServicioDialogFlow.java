package com.ampeliodev.chatbottwiliospringboot.service;

import com.ampeliodev.chatbottwiliospringboot.domain.EntidadCita;
import com.ampeliodev.chatbottwiliospringboot.repository.IDaoCitas;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.v2.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.google.protobuf.Value;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServicioDialogFlow {

    @Autowired
    private IDaoCitas daoCitas;

    private final SessionsClient sessionsClient;
    private final String projectId = "dialogflowagentchatboot-jcjp";
    private final String sessionId = "default-session";

    /*
    public ServicioDialogFlow() throws IOException {
        String credentialsJson = System.getenv("GOOGLE_CREDENTIALS_JSON");
        if (credentialsJson == null || credentialsJson.isEmpty()) {
            throw new IllegalStateException("No se encontró la variable de entorno GOOGLE_CREDENTIALS_JSON o está vacía.");
        }

        try (InputStream credentialsStream = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            SessionsSettings sessionsSettings = SessionsSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();
            this.sessionsClient = SessionsClient.create(sessionsSettings);
        }
    }*/


    // para desarrollo

    //Constructor para crear sesión
    public ServicioDialogFlow() throws Exception {
        InputStream credentialsStream = new ClassPathResource("dialogflowagentchatboot.json").getInputStream();
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
        SessionsSettings sessionsSettings = SessionsSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();
        this.sessionsClient = SessionsClient.create(sessionsSettings);
    }

    //metodo para obtener mensaje enviado en la petición (body)
    public DetectIntentResponse detectIntent(String message) {

        SessionName session = SessionName.of(projectId, sessionId);
        TextInput.Builder textInput = TextInput.newBuilder().setText(message).setLanguageCode("es");
        QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

        DetectIntentRequest request = DetectIntentRequest.newBuilder()
                .setSession(session.toString())
                .setQueryInput(queryInput)
                .build();

         return  sessionsClient.detectIntent(request);
    }

    //metodo para obtener únicamente la respuesta del intent
    public String obtenerRespuestaTextoCompleto(DetectIntentResponse respuesta) {
        return respuesta.getQueryResult().getFulfillmentText();
    }

    //Metodo para obtener determinados parámetros
    public Map<String, String> obtenerParametrosIntent(DetectIntentResponse respuesta) {
        List<Context> contextos = respuesta.getQueryResult().getOutputContextsList();

        Map<String, Value> campos = new HashMap<>();
        Map<String, String> valores = new HashMap<>();
        for (Context contexto : contextos) {

            if (contexto.getName().contains("definir_dia_cita")) {
                campos = contexto.getParameters().getFieldsMap();
                String dia = campos.get("diacita").getStringValue();
                String mes = campos.get("mes").getStringValue();

                valores.put("dia", dia);
                valores.put("mes", mes);
                System.out.println("mes desde contexto: " + mes + ", día: " + dia);
            }else if (contexto.getName().contains("definir_hora_cita")) {
                campos = contexto.getParameters().getFieldsMap();
                String hora = campos.get("horacita").getStringValue();
                valores.put("dia", hora);

                System.out.println("hora desde contexto: " + hora);

            } else if (contexto.getName().contains("confirmacion_cita")) {
                campos = contexto.getParameters().getFieldsMap();
                String dia = campos.get("diacita").getStringValue();
                String mes = campos.get("mes").getStringValue();
                String nombre = campos.get("nombreusuario").getStringValue();
                valores.put("dia", dia);
                valores.put("mes", mes);
                valores.put("nombre", nombre);

                System.out.println("mes desde contexto: " + mes + ", día: " + dia);
            }



        }

        return valores;
    }

    //metodo para guardar cita
    public void guardarCita(Map<String, String> parametros) {

        EntidadCita entidadCita = new EntidadCita();

        entidadCita.setNombre(parametros.get("nombre"));
        entidadCita.setServicio(parametros.get("servicio"));

        daoCitas.save(entidadCita);
    }

    //metodo para convertir dia y mes enviado en fecha y consultar en la BD
    public List<EntidadCita> consultarHorariosDisponibles(int dia, int mes, int anio) {

        LocalDate fecha = LocalDate.of(anio, mes, dia);

        return daoCitas.findByFechaAndDisponibleTrue(fecha);

    }

}
