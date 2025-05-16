package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.ClienteDTO;
import com.app.seguros.AplicacionSeguros.Dto.ClienteRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Repositories.BeneficiarioRepository;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceUnitariasTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private BeneficiarioRepository benRepo;

    @Mock
    private CondicionMedicaService condService;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteDTO createDTO(Long id) {
        ClienteDTO dto = new ClienteDTO(Estado.Activo);
        dto.setId_Cliente(id);
        dto.setNombre("Juan");
        dto.setApellido("Pérez");
        dto.setCedula("1234567890");
        dto.setDireccion("Calle Principal 123");
        dto.setTelefono("987654321");
        dto.setEmail("juan.perez@example.com");
        dto.setGenero("Masculino");
        dto.setFecha_nacimiento(new Date(1990 - 1900, 4, 15));
        dto.setEstado(Estado.Activo);
        return dto;
    }

    private Cliente createEntity(Long id) {
        return Cliente.builder()
                .id(id)
                .nombre("Juan")
                .apellido("Pérez")
                .cedula("1234567890")
                .direccion("Calle Principal 123")
                .telefono("987654321")
                .email("juan.perez@example.com")
                .genero("Masculino")
                .fecha_nacimiento(new Date(1990 - 1900, 4, 15))
                .estado(Estado.Activo)
                .build();
    }

    @Test
    void saveCliente() {
        ClienteDTO dto = createDTO(null);
        Cliente cliente = createEntity(1L);

        when(clienteRepository.findByCedula(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(benRepo.findByClienteId(anyInt())).thenReturn(List.of());
        when(condService.getAll(anyInt())).thenReturn(List.of());

        ClienteRespuestaDTO result = clienteService.saveCliente(dto);

        assertEquals(1L, result.getId_Cliente());
        assertEquals("Juan", result.getNombre());

        verify(clienteRepository).findByCedula(anyString());
        verify(clienteRepository).findByEmail(anyString());
        verify(clienteRepository).save(any(Cliente.class));
        verify(benRepo).findByClienteId(anyInt());
        verify(condService).getAll(anyInt());
    }

    @Test
    void saveClienteCedulaDuplicada() {
        ClienteDTO dto = createDTO(null);
        when(clienteRepository.findByCedula(anyString())).thenReturn(Optional.of(createEntity(1L)));

        assertThrows(RuntimeException.class, () -> clienteService.saveCliente(dto));
        verify(clienteRepository).findByCedula(anyString());
    }

    @Test
    void saveClienteEmailDuplicado() {
        ClienteDTO dto = createDTO(null);
        when(clienteRepository.findByCedula(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.of(createEntity(1L)));

        assertThrows(RuntimeException.class, () -> clienteService.saveCliente(dto));
        verify(clienteRepository).findByEmail(anyString());
    }

    @Test
    void getCliente() {
        Cliente cliente = createEntity(1L);

        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));
        when(benRepo.findByClienteId(anyInt())).thenReturn(List.of());
        when(condService.getAll(anyInt())).thenReturn(List.of());

        Optional<ClienteRespuestaDTO> result = clienteService.getCliente(1L);

        assertTrue(result.isPresent());
        assertEquals("Juan", result.get().getNombre());
    }

    @Test
    void getClienteNotFound() {
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<ClienteRespuestaDTO> result = clienteService.getCliente(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void getAllClientes() {
        List<Cliente> clientes = List.of(createEntity(1L), createEntity(2L));
        when(clienteRepository.findAll()).thenReturn(clientes);
        when(condService.getAll(anyInt())).thenReturn(List.of());
        when(benRepo.findByClienteId(anyInt())).thenReturn(List.of());

        List<ClienteRespuestaDTO> result = clienteService.getAllClientes();

        assertEquals(2, result.size());
        verify(clienteRepository).findAll();
    }

    @Test
    void updateCliente() {
        Cliente cliente = createEntity(1L);
        ClienteDTO dto = createDTO(1L);
        dto.setCedula("1122334455");
        dto.setEmail("updated.client@example.com");

        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));
        when(clienteRepository.findByCedula(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(benRepo.findByClienteId(anyInt())).thenReturn(List.of());
        when(condService.getAll(anyInt())).thenReturn(List.of());

        ClienteRespuestaDTO result = clienteService.updateCliente(1L, dto);

        assertEquals("Juan", result.getNombre());
        verify(clienteRepository).findById(anyLong());
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void updateClienteCedulaModificada() {
        Cliente cliente = createEntity(1L);
        ClienteDTO dto = createDTO(1L);
        dto.setCedula("nuevaCedula");
        dto.setEmail("nuevo@email.com");

        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));
        when(clienteRepository.findByCedula(anyString())).thenReturn(Optional.of(cliente));

        assertThrows(RuntimeException.class, () -> clienteService.updateCliente(1L, dto));
    }

    @Test
    void updateClienteEmailModificado() {
        Cliente cliente = createEntity(1L);
        ClienteDTO dto = createDTO(1L);
        dto.setCedula("nuevaCedula");
        dto.setEmail("nuevo@email.com");

        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));
        when(clienteRepository.findByCedula(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.of(cliente));

        assertThrows(RuntimeException.class, () -> clienteService.updateCliente(1L, dto));
    }

    @Test
    void updateClienteNotFound() {
        ClienteDTO dto = createDTO(1L);
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> clienteService.updateCliente(1L, dto));
    }

    @Test
    void deleteCliente() {
        Cliente cliente = createEntity(1L);
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));

        clienteService.deleteCliente(1L);

        verify(clienteRepository).deleteById(anyLong());
    }

    @Test
    void deleteClienteNotFound() {
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> clienteService.deleteCliente(1L));
    }
}
