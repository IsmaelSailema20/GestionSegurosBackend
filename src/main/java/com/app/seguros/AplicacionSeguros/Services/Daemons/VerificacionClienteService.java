package com.app.seguros.AplicacionSeguros.Services.Daemons;

import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.ContratoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.DocumentoRepository;
import com.app.seguros.AplicacionSeguros.Services.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class VerificacionClienteService {

    private final ClienteRepository clienteRepository;
    private final ContratoRepository contratoRepository;
    private final DocumentoRepository documentoRepository;
    private final EmailService emailService;

    public VerificacionClienteService(
            ClienteRepository clienteRepository,
            ContratoRepository contratoRepository,
            DocumentoRepository documentoRepository,
            EmailService emailService
    ) {
        this.clienteRepository = clienteRepository;
        this.contratoRepository = contratoRepository;
        this.documentoRepository = documentoRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void verificarClientesInactivos() {
        List<Cliente> clientes = clienteRepository.findAll();

        for (Cliente cliente : clientes) {
            if (cliente.getEstado() != Estado.Activo) continue;
            if (cliente.getFechaCreacion() == null) continue;

            // ¿Han pasado más de 2 días desde la creación?
            LocalDate fechaCreacion = cliente.getFechaCreacion().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            if (fechaCreacion.plusDays(2).isAfter(LocalDate.now())) continue;

            Optional<Contrato> contrato = contratoRepository.findByClienteId(cliente.getId());
            if (contrato.isEmpty() || contrato.get().getEstado() != EstadoContrato.PENDIENTE) continue;

            boolean tieneDocumentos = documentoRepository.existsByClienteIdAndEstado(cliente.getId(), EstadoDocumento.Pendiente);
            if (tieneDocumentos) continue;

            // Si no cumple con las condiciones, desactivar
            cliente.setEstado(Estado.Inactivo);
            clienteRepository.save(cliente);

            emailService.enviarCorreoDesactivacion(cliente.getEmail());
        }
    }
}
