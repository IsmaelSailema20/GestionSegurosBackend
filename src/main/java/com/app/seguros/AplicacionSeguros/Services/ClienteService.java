package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.*;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteService {
    @Autowired
    ClienteRepository clienteRepository;
    @Autowired private CondicionMedicaService condService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private EmailService emailService;
    @Transactional
    public ClienteRespuestaDTO saveCliente(ClienteDTO clienteDTO) {
        // Validar unicidad de cédula y email
        clienteRepository.findByCedula(clienteDTO.getCedula())
                .ifPresent(cliente -> {
                    throw new RuntimeException(MensajeError.CEDULA_REGISTRADA.getMensaje());
                });
        clienteRepository.findByEmail(clienteDTO.getEmail())
                .ifPresent(cliente -> {
                    throw new RuntimeException(MensajeError.EMAIL_REGISTRADO.getMensaje());
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
                .numeroCuenta(clienteDTO.getNumeroCuenta())
                .build();
        cliente = clienteRepository.save(cliente);



        if (clienteDTO.getCondicionesMedicas() != null) {
            Cliente finalCliente = cliente;
            clienteDTO.getCondicionesMedicas().forEach(cm -> {
                if (cm.getId_condicion() == null) {
                    cm = condService.post(cm);
                }
                condService.postRelacion(cm,finalCliente);
            });
        }

        usuarioService.register(
                UsuarioCrearRequest.builder()
                        .apellido(cliente.getApellido())
                        .email(cliente.getEmail())
                        .nombre(cliente.getNombre())
                        .rol(Rol.Cliente)
                        .password(cliente.getCedula())
                        .build()
        );

        // Enviar correo al cliente con sus credenciales y enlace
        String destinatario = cliente.getEmail();
        String asunto = "Bienvenido a Walk Seguros - Tus Credenciales";
        String cuerpo = "Estimado " + cliente.getNombre() + " " + cliente.getApellido() + ",\n\n" +
                "Gracias por registrarte en Walk Seguros. A continuación, tus credenciales:\n\n" +
                "Usuario: " + cliente.getEmail() + "\n" +
                "Contraseña: " + cliente.getCedula() + "\n\n" +
                "Por favor, inicia sesión en nuestra aplicación en el siguiente enlace:\n" +
                "http://localhost:5173\n\n" +
                "Saludos,\nEquipo Walk Seguros";
        emailService.enviarCorreo(destinatario, asunto, cuerpo);

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
                .apellido(clienteDTO.getApellido())
                .email(clienteDTO.getEmail())
                .estado(clienteDTO.getEstado())
                .nombre(clienteDTO.getNombre())
                .numeroCuenta(clienteDTO.getNumeroCuenta())
                .build();
    }


    public Optional<ClienteRespuestaDTO> getCliente(Long id) {
        return clienteRepository.findById(id)
                .map(cliente -> ClienteRespuestaDTO.builder()
                            .estaturaMetros(cliente.getEstaturaMetros())
                            .fecha_nacimiento(cliente.getFecha_nacimiento())
                            .direccion(cliente.getDireccion())
                            .id_Cliente(cliente.getId())
                            .genero(cliente.getGenero())
                            .cedula(cliente.getCedula())
                            .pesoKg(cliente.getPesoKg())
                            .telefono(cliente.getTelefono())
                            .condicionesMedicas(condService.getAll((int) cliente.getId()))
                            .apellido(cliente.getApellido())
                            .email(cliente.getEmail())
                            .estado(cliente.getEstado())
                            .nombre(cliente.getNombre())
                            .numeroCuenta(cliente.getNumeroCuenta())
                            .build()
                );
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
                            .apellido(cliente.getApellido())
                            .email(cliente.getEmail())
                            .estado(cliente.getEstado())
                            .nombre(cliente.getNombre())
                            .numeroCuenta(cliente.getNumeroCuenta())
                            .build();
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public ClienteRespuestaDTO updateCliente(Long id, ClienteDTO clienteDTO) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(MensajeError.CLIENTE_NO_ENCONTRADO.getMensaje()));

        // Validar unicidad de cédula y email si cambiaron
        if (!cliente.getCedula().equals(clienteDTO.getCedula())) {
            clienteRepository.findByCedula(clienteDTO.getCedula())
                    .ifPresent(c -> {
                        throw new RuntimeException(MensajeError.CEDULA_REGISTRADA.getMensaje());
                    });
        }
        if (!cliente.getEmail().equals(clienteDTO.getEmail())) {
            clienteRepository.findByEmail(clienteDTO.getEmail())
                    .ifPresent(c -> {
                        throw new RuntimeException(MensajeError.EMAIL_REGISTRADO.getMensaje());
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
        cliente.setNumeroCuenta(clienteDTO.getNumeroCuenta());
        cliente = clienteRepository.save(cliente);

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
                .apellido(cliente.getApellido())
                .email(cliente.getEmail())
                .estado(cliente.getEstado())
                .nombre(cliente.getNombre())
                .numeroCuenta(cliente.getNumeroCuenta())
                .build();
    }


    public ClienteRespuestaDTO actualizarEstado(Long id,EstadoDTO estado){
        Cliente cliente=clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(MensajeError.CLIENTE_NO_ENCONTRADO.getMensaje()));
        cliente.setEstado(estado.getEstado());
        clienteRepository.save(cliente);
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
                .apellido(cliente.getApellido())
                .email(cliente.getEmail())
                .estado(cliente.getEstado())
                .nombre(cliente.getNombre())
                .numeroCuenta(cliente.getNumeroCuenta())
                .build();
    }
    public String getCorreoCliente(Long idCliente) {
        Optional<Cliente> cliente = clienteRepository.findById(idCliente);
        return cliente.map(Cliente::getEmail)
                .orElseThrow(() -> new RuntimeException(MensajeError.CLIENTE_NO_ENCONTRADO.getMensaje()+" ID: " + idCliente));
    }

}
