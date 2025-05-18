package com.ampeliodev.chatbottwiliospringboot.service;

import com.ampeliodev.chatbottwiliospringboot.dto.DtoCita;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ServicioSesion {

    private Map<String, DtoCita> sesiones = new ConcurrentHashMap<>();

    public void guardarSesion(String numeroUsuario, DtoCita datos) {
        sesiones.put(numeroUsuario, datos);
    }

    public DtoCita obtenerSesion(String numeroUsuario) {

        return sesiones.get(numeroUsuario);
    }

    public void eliminarSesion(String numeroUsuario) {
        sesiones.remove(numeroUsuario);
    }

    public boolean tieneSesion(String numeroUsuario) {
        return sesiones.containsKey(numeroUsuario);
    }

}
