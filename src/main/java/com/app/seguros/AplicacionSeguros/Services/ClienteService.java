package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.ClienteDTO;
import com.app.seguros.AplicacionSeguros.Dto.ClienteRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Condiciones_Clientes;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Repositories.BeneficiarioRepository;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.CondicionMedicaClienteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteService {
    @Autowired
    ClienteRepository clienteRepository;
    @Autowired private BeneficiarioRepository benRepo;
    @Autowired private CondicionMedicaService condService;
    @Transactional
    public ClienteRespuestaDTO saveCliente(ClienteDTO clienteDTO) {
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
                .pesoKg(clienteDTO.getPesoKg())
                .estaturaMetros(clienteDTO.getEstaturaMetros())
                .build();
        cliente = clienteRepository.save(cliente);

        if (clienteDTO.getBeneficiarios() != null) {
            Cliente finalCliente = cliente;
            clienteDTO.getBeneficiarios().forEach(b -> {
                b.setCliente(finalCliente);
                benRepo.save(b);
            });
        }

        if (clienteDTO.getCondicionesMedicas() != null) {
            Cliente finalCliente = cliente;
            clienteDTO.getCondicionesMedicas().forEach(cm -> {
                if (cm.getId_condicion() == null) {
                    cm = condService.post(cm);
                }
                condService.postRelacion(cm,finalCliente);
            });
        }

        return ClienteRespuestaDTO.builder()
                .estaturaMetros(clienteDTO.getEstaturaMetros())
                .fecha_nacimiento(clienteDTO.getFecha_nacimiento())
                .direccion(clienteDTO.getDireccion())
                .id_Cliente(cliente.getId())
                .genero(clienteDTO.getGenero())
                .cedula(clienteDTO.getCedula())
                .pesoKg(clienteDTO.getPesoKg())
                .telefono(clienteDTO.getTelefono())
                .condicionesMedicas(condService.getAll((int) cliente.getId()))
                .beneficiarios(benRepo.findByClienteId((int)cliente.getId()))
                .apellido(clienteDTO.getApellido())
                .email(clienteDTO.getEmail())
                .estado(clienteDTO.getEstado())
                .nombre(clienteDTO.getNombre())
                .build();
    }

    public Optional<ClienteRespuestaDTO> getCliente(Long id) {
        return clienteRepository.findById(id)
                .map(cliente -> {
                    ClienteRespuestaDTO dto = ClienteRespuestaDTO.builder()
                            .estaturaMetros(cliente.getEstaturaMetros())
                            .fecha_nacimiento(cliente.getFecha_nacimiento())
                            .direccion(cliente.getDireccion())
                            .id_Cliente(cliente.getId())
                            .genero(cliente.getGenero())
                            .cedula(cliente.getCedula())
                            .pesoKg(cliente.getPesoKg())
                            .telefono(cliente.getTelefono())
                            .condicionesMedicas(condService.getAll((int) cliente.getId()))
                            .beneficiarios(benRepo.findByClienteId((int)cliente.getId()))
                            .apellido(cliente.getApellido())
                            .email(cliente.getEmail())
                            .estado(cliente.getEstado())
                            .nombre(cliente.getNombre())
                            .build();
                    return dto;
                });
    }

    public List<ClienteRespuestaDTO> getAllClientes() {
        return clienteRepository.findAll()
                .stream()
                .map(cliente -> {
                    ClienteRespuestaDTO dto = ClienteRespuestaDTO.builder()
                            .estaturaMetros(cliente.getEstaturaMetros())
                            .fecha_nacimiento(cliente.getFecha_nacimiento())
                            .direccion(cliente.getDireccion())
                            .id_Cliente(cliente.getId())
                            .genero(cliente.getGenero())
                            .cedula(cliente.getCedula())
                            .pesoKg(cliente.getPesoKg())
                            .telefono(cliente.getTelefono())
                            .condicionesMedicas(condService.getAll((int) cliente.getId()))
                            .beneficiarios(benRepo.findByClienteId((int)cliente.getId()))
                            .apellido(cliente.getApellido())
                            .email(cliente.getEmail())
                            .estado(cliente.getEstado())
                            .nombre(cliente.getNombre())
                            .build();
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public ClienteRespuestaDTO updateCliente(Long id, ClienteDTO clienteDTO) {
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
        cliente.setPesoKg(clienteDTO.getPesoKg());
        cliente.setEstaturaMetros(clienteDTO.getEstaturaMetros());
        cliente = clienteRepository.save(cliente);

        benRepo.deleteByClienteId((int)cliente.getId());
        if (clienteDTO.getBeneficiarios() != null) {
            Cliente finalCliente = cliente;
            clienteDTO.getBeneficiarios().forEach(b -> {
                b.setCliente(finalCliente);
                benRepo.save(b);
            });
        }
        condService.deleteByClienteId((int)cliente.getId());
        if (clienteDTO.getCondicionesMedicas() != null) {
            Cliente finalCliente = cliente;
            clienteDTO.getCondicionesMedicas().forEach(cm -> {
                if (cm.getId_condicion() == null) {
                    cm = condService.post(cm);
                }
                condService.postRelacion(cm,finalCliente);
            });
        }
        return ClienteRespuestaDTO.builder()
                .estaturaMetros(cliente.getEstaturaMetros())
                .fecha_nacimiento(cliente.getFecha_nacimiento())
                .direccion(cliente.getDireccion())
                .id_Cliente(cliente.getId())
                .genero(cliente.getGenero())
                .cedula(cliente.getCedula())
                .pesoKg(cliente.getPesoKg())
                .telefono(cliente.getTelefono())
                .condicionesMedicas(condService.getAll((int) cliente.getId()))
                .beneficiarios(benRepo.findByClienteId((int)cliente.getId()))
                .apellido(cliente.getApellido())
                .email(cliente.getEmail())
                .estado(cliente.getEstado())
                .nombre(cliente.getNombre())
                .build();
    }

    public void deleteCliente(Long id) {
        clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        condService.deleteByClienteId(id.intValue());
        benRepo.deleteByClienteId(id.intValue());

        clienteRepository.deleteById(id);
    }


}
