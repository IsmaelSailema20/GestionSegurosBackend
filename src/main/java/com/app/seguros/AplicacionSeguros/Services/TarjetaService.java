// TarjetaService.java
package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Config.AESUtil;
import com.app.seguros.AplicacionSeguros.Dto.TarjetaResgistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.TarjetaRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoTarjeta;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
import com.app.seguros.AplicacionSeguros.Entities.Tarjeta;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.TarjetaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TarjetaService {
    @Autowired
    private TarjetaRepository tarjetaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

   // @Autowired
    private AESUtil aesUtil = new AESUtil();

    public TarjetaRespuestaDTO crearTarjeta(TarjetaResgistroDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                .orElseThrow(() -> new RuntimeException(MensajeError.CLIENTE_NO_ENCONTRADO.getMensaje()));

        EstadoTarjeta estado = tarjetaRepository.findByClienteId(cliente.getId()).stream()
                .anyMatch(t -> t.getEstado() == EstadoTarjeta.Principal) ? EstadoTarjeta.Secundaria : EstadoTarjeta.Principal;

        Tarjeta tarjeta = Tarjeta.builder()
                .numero(aesUtil.encrypt(dto.getNumero()))
                .vencimiento(dto.getVencimiento())
                .cvv(aesUtil.encrypt(dto.getCvv()))
                .tipo(dto.getTipo())
                .marca(dto.getMarca())
                .estado(estado)
                .cliente(cliente)
                .build();

        tarjeta = tarjetaRepository.save(tarjeta);
        return mapToDTO(tarjeta);
    }

    public TarjetaRespuestaDTO editarTarjeta(Long id, TarjetaResgistroDTO dto) {
        Tarjeta tarjeta = tarjetaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

        Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                .orElseThrow(() -> new RuntimeException(MensajeError.CLIENTE_NO_ENCONTRADO.getMensaje()));

        tarjeta.setNumero(aesUtil.encrypt(dto.getNumero()));
        tarjeta.setVencimiento(dto.getVencimiento());
        tarjeta.setCvv(aesUtil.encrypt(dto.getCvv()));
        tarjeta.setTipo(dto.getTipo());
        tarjeta.setMarca(dto.getMarca());
        tarjeta.setCliente(cliente);

        tarjeta = tarjetaRepository.save(tarjeta);
        return mapToDTO(tarjeta);
    }
    public void eliminarTarjeta(Long id) {
        Tarjeta tarjeta = tarjetaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

        if (tarjeta.getEstado() == EstadoTarjeta.Principal) {
            throw new RuntimeException("No se puede eliminar una tarjeta Principal");
        }

        tarjetaRepository.delete(tarjeta);
    }

    public TarjetaRespuestaDTO obtenerTarjetaPorId(Long id) {
        Tarjeta tarjeta = tarjetaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));
        return mapToDTO(tarjeta);
    }

    public List<TarjetaRespuestaDTO> obtenerTarjetasPorCliente(Long idCliente) {
        return tarjetaRepository.findByClienteId(idCliente).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TarjetaRespuestaDTO obtenerTarjetaPrincipal(Long idCliente) {
        Tarjeta tarjeta = tarjetaRepository.findByClienteId(idCliente).stream()
                .filter(t -> t.getEstado() == EstadoTarjeta.Principal)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Tarjeta principal no encontrada"));
        return mapToDTO(tarjeta);
    }

    @Transactional
    public void inactivarTarjeta(Long id) {
        Tarjeta tarjeta = tarjetaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));
        tarjeta.setEstado(EstadoTarjeta.Inactivo);
        tarjetaRepository.save(tarjeta);
    }

    @Transactional
    public void cambiarEstadoTarjetaAPrincipal(Long id) {
        Tarjeta nuevaTarjetaPrincipal = tarjetaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada con id: " + id));

        if (nuevaTarjetaPrincipal.getEstado() == EstadoTarjeta.Principal) {
            // Ya es principal, no se hace nada
            return;
        }

        Tarjeta tarjetaActualPrincipal = tarjetaRepository.findByEstado(EstadoTarjeta.Principal);

        if (tarjetaActualPrincipal == null) {
            throw new RuntimeException("No se encontró una tarjeta con estado 'Principal'.");
        }

        // Actualizamos estados
        tarjetaActualPrincipal.setEstado(EstadoTarjeta.Secundaria);
        nuevaTarjetaPrincipal.setEstado(EstadoTarjeta.Principal);

        // Guardamos cambios
        tarjetaRepository.save(tarjetaActualPrincipal);
        tarjetaRepository.save(nuevaTarjetaPrincipal);
    }
    public TarjetaRespuestaDTO mapToDTO(Tarjeta tarjeta) {
        return TarjetaRespuestaDTO.builder()
                .id(tarjeta.getId())
                .numero(enmascararNumeroTarjeta(aesUtil.decrypt(tarjeta.getNumero())))
                .vencimiento(tarjeta.getVencimiento())
                .tipo(tarjeta.getTipo())
                .idCliente(tarjeta.getCliente().getId())
                .estado(tarjeta.getEstado())
                .marca(tarjeta.getMarca())
                .build();
    }
    private String enmascararNumeroTarjeta(String numeroTarjeta) {
        if (numeroTarjeta == null) return "****";

        // Eliminar espacios y caracteres no numéricos
        String soloDigitos = numeroTarjeta.replaceAll("[^0-9]", "");

        if (soloDigitos.length() < 4) {
            return "****";
        }

        String ultimosCuatro = soloDigitos.substring(soloDigitos.length() - 4);
        return "**** **** **** " + ultimosCuatro;
    }
}
