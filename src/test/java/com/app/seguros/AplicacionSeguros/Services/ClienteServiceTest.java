package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.*;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

    @InjectMocks
    private ClienteService clienteService;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CondicionMedicaService condService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private EmailService emailService;

    @Mock
    private UsuariosRepository usuariosRepository;

    private Cliente cliente;
    private ClienteDTO clienteDTO;
    private CondicionMedica condicionMedica;
    private Date fechaNacimiento;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        // Setup test data
        fechaNacimiento = Date.from(LocalDate.of(1990, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .cedula("1234567890")
                .email("juan@example.com")
                .direccion("Calle 123")
                .telefono("123456789")
                .genero("Masculino")
                .fecha_nacimiento(fechaNacimiento)
                .estado(Estado.Activo)
                .pesoKg(70.0)
                .estaturaMetros(1.75)
                .numeroCuenta("1234567890123456")
                .build();

        clienteDTO = new ClienteDTO(Estado.Activo);
        clienteDTO.setId_Cliente(1L);
        clienteDTO.setNombre("Juan");
        clienteDTO.setApellido("Perez");
        clienteDTO.setCedula("1234567890");
        clienteDTO.setEmail("juan@example.com");
        clienteDTO.setDireccion("Calle 123");
        clienteDTO.setTelefono("123456789");
        clienteDTO.setGenero("Masculino");
        clienteDTO.setFecha_nacimiento(fechaNacimiento);
        clienteDTO.setEstado(Estado.Activo);
        clienteDTO.setPesoKg(70.0);
        clienteDTO.setEstaturaMetros(1.75);
        clienteDTO.setNumeroCuenta("1234567890123456");

        condicionMedica = CondicionMedica.builder()
                .id_condicion(1)
                .diagnostico("Hipertension")
                .build();

        usuario = Usuario.builder()
                .ID_Usuario(1)
                .nombre("Juan")
                .apellido("Perez")
                .email("juan@example.com")
                .contraseña("1234567890")
                .rol(Rol.Cliente)
                .build();
    }

    @Test
    void testSaveCliente_Success() {
        // Arrange
        clienteDTO.setCondicionesMedicas(List.of(condicionMedica));
        when(clienteRepository.findByCedula("1234567890")).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail("juan@example.com")).thenReturn(Optional.empty());
        when(clienteRepository.findByNumeroCuenta("1234567890123456")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        lenient().when(condService.post(any(CondicionMedica.class))).thenReturn(condicionMedica);
        when(condService.getAll(1)).thenReturn(List.of(condicionMedica));
        when(usuarioService.register(any(UsuarioCrearRequest.class))).thenReturn(null); // Mock register to return null
        doNothing().when(emailService).enviarCorreo(anyString(), anyString(), anyString());

        // Act
        ClienteRespuestaDTO result = clienteService.saveCliente(clienteDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId_Cliente());
        assertEquals("Juan", result.getNombre());
        assertEquals("1234567890", result.getCedula());
        assertEquals("juan@example.com", result.getEmail());
        assertEquals("1234567890123456", result.getNumeroCuenta());
        assertEquals(1, result.getCondicionesMedicas().size());
        assertEquals("Hipertension", result.getCondicionesMedicas().get(0).getDiagnostico());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
        verify(condService, times(1)).postRelacion(any(CondicionMedica.class), any(Cliente.class));
        verify(usuarioService, times(1)).register(any(UsuarioCrearRequest.class));
        verify(emailService, times(1)).enviarCorreo(anyString(), anyString(), anyString());
    }

    @Test
    void testSaveCliente_DuplicateCedula() {
        // Arrange
        when(clienteRepository.findByCedula("1234567890")).thenReturn(Optional.of(cliente));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clienteService.saveCliente(clienteDTO));
        assertEquals(MensajeError.CEDULA_REGISTRADA.getMensaje(), exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void testSaveCliente_DuplicateEmail() {
        // Arrange
        when(clienteRepository.findByCedula("1234567890")).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(cliente));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clienteService.saveCliente(clienteDTO));
        assertEquals(MensajeError.EMAIL_REGISTRADO.getMensaje(), exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void testGetCliente_Success() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(condService.getAll(1)).thenReturn(List.of(condicionMedica));

        // Act
        Optional<ClienteRespuestaDTO> result = clienteService.getCliente(1L);

        // Assert
        assertTrue(result.isPresent());
        ClienteRespuestaDTO dto = result.get();
        assertEquals(1L, dto.getId_Cliente());
        assertEquals("Juan", dto.getNombre());
        assertEquals("1234567890", dto.getCedula());
        assertEquals(1, dto.getCondicionesMedicas().size());
        assertEquals("Hipertension", dto.getCondicionesMedicas().get(0).getDiagnostico());
    }

    @Test
    void testGetCliente_NotFound() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<ClienteRespuestaDTO> result = clienteService.getCliente(1L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllClientes_Success() {
        // Arrange
        when(clienteRepository.findAll()).thenReturn(List.of(cliente));
        when(condService.getAll(1)).thenReturn(List.of(condicionMedica));

        // Act
        List<ClienteRespuestaDTO> result = clienteService.getAllClientes();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ClienteRespuestaDTO dto = result.get(0);
        assertEquals(1L, dto.getId_Cliente());
        assertEquals("Juan", dto.getNombre());
        assertEquals("1234567890", dto.getCedula());
        assertEquals(1, dto.getCondicionesMedicas().size());
    }

    @Test
    void testUpdateCliente_ok() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        lenient().when(clienteRepository.findByCedula(clienteDTO.getCedula())).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail(clienteDTO.getEmail())).thenReturn(Optional.empty());
        lenient().when(clienteRepository.findByNumeroCuenta(clienteDTO.getNumeroCuenta())).thenReturn(Optional.empty());
        when(usuariosRepository.findByEmail(cliente.getEmail())).thenReturn(Optional.of(usuario));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));
        when(condService.getAll(1)).thenReturn(Collections.emptyList());

        ClienteRespuestaDTO result = clienteService.updateCliente(1L, clienteDTO);

        assertNotNull(result);
        assertEquals(clienteDTO.getNombre(), result.getNombre());
        assertEquals(clienteDTO.getApellido(), result.getApellido());
        assertEquals(clienteDTO.getEmail(), result.getEmail());
        assertEquals(clienteDTO.getNumeroCuenta(), result.getNumeroCuenta());

        verify(emailService).enviarCorreo(
                eq(clienteDTO.getEmail()),
                contains("Actualización de correo"),
                contains("Usuario: " + clienteDTO.getEmail())
        );
        verify(usuariosRepository).save(any(Usuario.class));
        verify(condService).deleteByClienteId(1);
    }

    @Test
    void testUpdateCliente_ClienteNotFound() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clienteService.updateCliente(1L, clienteDTO));
        assertEquals(MensajeError.CLIENTE_NO_ENCONTRADO.getMensaje(), exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void testUpdateCliente_DuplicateCedula() {
        // Arrange
        ClienteDTO updatedDTO = new ClienteDTO(Estado.Activo);
        updatedDTO.setCedula("0987654321");
        updatedDTO.setEmail("juan@example.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.findByCedula("0987654321")).thenReturn(Optional.of(new Cliente()));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clienteService.updateCliente(1L, updatedDTO));
        assertEquals(MensajeError.CEDULA_REGISTRADA.getMensaje(), exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void testUpdateCliente_DuplicateEmail() {
        // Arrange
        ClienteDTO updatedDTO = new ClienteDTO(Estado.Activo);
        updatedDTO.setCedula("1234567890");
        updatedDTO.setEmail("carlos@example.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        lenient().when(clienteRepository.findByCedula("1234567890")).thenReturn(Optional.of(cliente));
        when(clienteRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(new Cliente()));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clienteService.updateCliente(1L, updatedDTO));
        assertEquals(MensajeError.EMAIL_REGISTRADO.getMensaje(), exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void testActualizarEstado_Success() {
        // Arrange
        EstadoDTO estadoDTO = new EstadoDTO();
        estadoDTO.setEstado(Estado.Inactivo);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(condService.getAll(1)).thenReturn(List.of(condicionMedica));

        // Act
        ClienteRespuestaDTO result = clienteService.actualizarEstado(1L, estadoDTO);

        // Assert
        assertNotNull(result);
        assertEquals(Estado.Inactivo, result.getEstado());
        assertEquals("Juan", result.getNombre());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void testActualizarEstado_ClienteNotFound() {
        // Arrange
        EstadoDTO estadoDTO = new EstadoDTO();
        estadoDTO.setEstado(Estado.Inactivo);
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clienteService.actualizarEstado(1L, estadoDTO));
        assertEquals(MensajeError.CLIENTE_NO_ENCONTRADO.getMensaje(), exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void testGetCorreoCliente_Success() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        // Act
        String result = clienteService.getCorreoCliente(1L);

        // Assert
        assertNotNull(result);
        assertEquals("juan@example.com", result);
        verify(clienteRepository, times(1)).findById(1L);
    }

    @Test
    void testGetCorreoCliente_NotFound() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> clienteService.getCorreoCliente(1L));
        assertEquals(MensajeError.CLIENTE_NO_ENCONTRADO.getMensaje() + " ID: 1", exception.getMessage());
        verify(clienteRepository, times(1)).findById(1L);
    }
}