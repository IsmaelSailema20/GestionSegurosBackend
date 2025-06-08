package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.TarjetaResgistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.TarjetaRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Tarjeta;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.TarjetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TarjetaService {
    @Autowired
    private TarjetaRepository tarjetaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    public TarjetaRespuestaDTO crearTarjeta(TarjetaResgistroDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumero(dto.getNumero());
        tarjeta.setNombreTitular(dto.getNombreTitular());
        tarjeta.setVencimiento(dto.getVencimiento());
        tarjeta.setCvv(dto.getCvv());
        tarjeta.setTipo(dto.getTipo());
        tarjeta.setCliente(cliente);

        tarjeta = tarjetaRepository.save(tarjeta);
        return mapToDTO(tarjeta);
    }

    public TarjetaRespuestaDTO mapToDTO(Tarjeta tarjeta) {
        TarjetaRespuestaDTO dto = new TarjetaRespuestaDTO();
        dto.setId(tarjeta.getId());
        dto.setNumero(tarjeta.getNumero());
        dto.setNombreTitular(tarjeta.getNombreTitular());
        dto.setVencimiento(tarjeta.getVencimiento());
        dto.setTipo(tarjeta.getTipo());
        dto.setIdCliente(tarjeta.getCliente().getId());
        return dto;
    }
}
