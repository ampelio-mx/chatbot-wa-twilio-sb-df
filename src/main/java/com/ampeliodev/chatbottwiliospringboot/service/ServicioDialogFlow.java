package com.ampeliodev.chatbottwiliospringboot.service;

import com.ampeliodev.chatbottwiliospringboot.domain.EntidadCita;
import com.ampeliodev.chatbottwiliospringboot.dto.DtoCita;
import com.ampeliodev.chatbottwiliospringboot.repository.IDaoCitas;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.v2.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.google.protobuf.Value;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Month;
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


    public ServicioDialogFlow() throws IOException {
        String credentialsJson = System.getenv("GOOGLE_CREDENTIALS_JSON");
        if (credentialsJson == null) {
            throw new IllegalStateException("No se encontró la variable de entorno GOOGLE_CREDENTIALS_JSON");
        }

        InputStream credentialsStream = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

        SessionsSettings sessionsSettings = SessionsSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        this.sessionsClient = SessionsClient.create(sessionsSettings);
    }


    /* para desarrollo

    public ServicioDialogFlow() throws Exception {
        InputStream credentialsStream = new ClassPathResource("dialogflowagentchatboot.json").getInputStream();
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
        SessionsSettings sessionsSettings = SessionsSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();
        this.sessionsClient = SessionsClient.create(sessionsSettings);
    }*/

    //metodo para obtener respuesta
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
    //metodo para obtener el nombre del intent
    public String obtenerRespuestaTextoCompleto(DetectIntentResponse respuesta) {
        return respuesta.getQueryResult().getFulfillmentText();
    }

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
            }

        }

        return valores;
    }


    public void guardarCita(Map<String, String> parametros) {

        EntidadCita entidadCita = new EntidadCita();

        entidadCita.setNombre(parametros.get("nombre"));
        entidadCita.setServicio(parametros.get("servicio"));

        daoCitas.save(entidadCita);
    }

    public List<EntidadCita> consultarHorariosDisponibles(String dia, String mes) {

        int ano = 2025;

        Map<String, Integer> mesANumero = new HashMap<>();
        mesANumero.put("enero",1);
        mesANumero.put("febrero",2);
        mesANumero.put("marzo",3);
        mesANumero.put("april",4);
        mesANumero.put("mayo",5);
        mesANumero.put("junio",6);
        mesANumero.put("julio",7);
        mesANumero.put("augusto",8);
        mesANumero.put("septiembre",9);
        mesANumero.put("octubre",10);
        mesANumero.put("noviembre",11);
        mesANumero.put("diciembre",12);

        int mesInt = mesANumero.get(mes.toLowerCase());

        LocalDate fecha = LocalDate.of(ano, mesInt, Integer.parseInt(dia));

        return daoCitas.findByFechaAndDisponibleTrue(fecha);

    }

}
