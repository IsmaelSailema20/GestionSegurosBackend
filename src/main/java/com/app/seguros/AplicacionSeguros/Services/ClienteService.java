package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.ClienteDTO;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class ClienteService {
    @Autowired
    ClienteRepository clienteRepository;


    public ClienteDTO saveCliente(ClienteDTO clienteDTO) {
        // Validar unicidad de cédula y email
        clienteRepository.findByCedula(clienteDTO.getCedula())
                .ifPresent(cliente -> {
                    throw new RuntimeException("La cédula ya está registrada");
                });
        clienteRepository.findByEmail(clienteDTO.getEmail())
                .ifPresent(cliente -> {
                    throw new RuntimeException("El email ya está registrado");
                });

        Cliente cliente = Cliente.builder()
                .nombre(clienteDTO.getNombre())
                .apellido(clienteDTO.getApellido())
                .cedula(clienteDTO.getCedula())
                .direccion(clienteDTO.getDireccion())
                .telefono(clienteDTO.getTelefono())
                .email(clienteDTO.getEmail())
                .genero(clienteDTO.getGenero())
                .fecha_nacimiento(clienteDTO.getFecha_nacimiento())
                .estado(clienteDTO.getEstado())
                .build();

        cliente = clienteRepository.save(cliente);
        clienteDTO.setId_Cliente(cliente.getId_Cliente());
        return clienteDTO;
    }

    public Optional<ClienteDTO> getCliente(Long id) {
        return clienteRepository.findById(id)
                .map(cliente -> {
                    ClienteDTO dto = new ClienteDTO(Estado.Activo);
                    dto.setId_Cliente(cliente.getId_Cliente());
                    dto.setNombre(cliente.getNombre());
                    dto.setApellido(cliente.getApellido());
                    dto.setCedula(cliente.getCedula());
                    dto.setDireccion(cliente.getDireccion());
                    dto.setTelefono(cliente.getTelefono());
                    dto.setEmail(cliente.getEmail());
                    dto.setGenero(cliente.getGenero());
                    dto.setFecha_nacimiento(cliente.getFecha_nacimiento());
                    dto.setEstado(cliente.getEstado());
                    return dto;
                });
    }

    public List<ClienteDTO> getAllClientes() {
        return clienteRepository.findAll()
                .stream()
                .map(cliente -> {
                    ClienteDTO dto = new ClienteDTO(Estado.Activo);
                    dto.setId_Cliente(cliente.getId_Cliente());
                    dto.setNombre(cliente.getNombre());
                    dto.setApellido(cliente.getApellido());
                    dto.setCedula(cliente.getCedula());
                    dto.setDireccion(cliente.getDireccion());
                    dto.setTelefono(cliente.getTelefono());
                    dto.setEmail(cliente.getEmail());
                    dto.setGenero(cliente.getGenero());
                    dto.setFecha_nacimiento(cliente.getFecha_nacimiento());
                    dto.setEstado(cliente.getEstado());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public ClienteDTO updateCliente(Long id, ClienteDTO clienteDTO) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Validar unicidad de cédula y email si cambiaron
        if (!cliente.getCedula().equals(clienteDTO.getCedula())) {
            clienteRepository.findByCedula(clienteDTO.getCedula())
                    .ifPresent(c -> {
                        throw new RuntimeException("La cédula ya está registrada");
                    });
        }
        if (!cliente.getEmail().equals(clienteDTO.getEmail())) {
            clienteRepository.findByEmail(clienteDTO.getEmail())
                    .ifPresent(c -> {
                        throw new RuntimeException("El email ya está registrado");
                    });
        }

        cliente.setNombre(clienteDTO.getNombre());
        cliente.setApellido(clienteDTO.getApellido());
        cliente.setCedula(clienteDTO.getCedula());
        cliente.setDireccion(clienteDTO.getDireccion());
        cliente.setTelefono(clienteDTO.getTelefono());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setGenero(clienteDTO.getGenero());
        cliente.setFecha_nacimiento(clienteDTO.getFecha_nacimiento());
        cliente.setEstado(clienteDTO.getEstado());

        cliente = clienteRepository.save(cliente);
        clienteDTO.setId_Cliente(cliente.getId_Cliente());
        return clienteDTO;
    }

    public void deleteCliente(Long id) {
        clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        clienteRepository.deleteById(id);
    }


}
