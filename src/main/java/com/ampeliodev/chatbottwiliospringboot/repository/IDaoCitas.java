package com.ampeliodev.chatbottwiliospringboot.repository;

import com.ampeliodev.chatbottwiliospringboot.domain.EntidadCita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IDaoCitas extends JpaRepository<EntidadCita, Integer> {

    List<EntidadCita> findByFechaAndDisponibleTrue(LocalDate fecha);
}
