package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.ClienteDTO;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
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
        dto.setFecha_nacimiento(new Date(1990 - 1900, 5 - 1, 15)); // 1990-05-15
        dto.setEstado(Estado.Activo);
        return dto;
    }

    private Cliente createEntity(Long id) {
        return Cliente.builder()
                .id_Cliente(id)
                .nombre("Juan")
                .apellido("Pérez")
                .cedula("1234567890")
                .direccion("Calle Principal 123")
                .telefono("987654321")
                .email("juan.perez@example.com")
                .genero("Masculino")
                .fecha_nacimiento(new Date(1990 - 1900, 5 - 1, 15))
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

        ClienteDTO result = clienteService.saveCliente(dto);
        assertEquals(1L, result.getId_Cliente());
        verify(clienteRepository, times(1)).findByCedula(anyString());
        verify(clienteRepository, times(1)).findByEmail(anyString());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void saveClienteCedulaDuplicada() {
        ClienteDTO dto = createDTO(null);
        when(clienteRepository.findByCedula(anyString())).thenReturn(Optional.of(createEntity(1L)));
        assertThrows(RuntimeException.class, () -> clienteService.saveCliente(dto));
        verify(clienteRepository, times(1)).findByCedula(anyString());
    }

    @Test
    void saveClienteEmailDuplicado() {
        ClienteDTO dto = createDTO(null);
        when(clienteRepository.findByCedula(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.of(createEntity(1L)));
        assertThrows(RuntimeException.class, () -> clienteService.saveCliente(dto));
        verify(clienteRepository, times(1)).findByEmail(anyString());
    }

    @Test
    void getCliente() {
        Cliente cliente = createEntity(1L);
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));

        Optional<ClienteDTO> result = clienteService.getCliente(1L);
        assertTrue(result.isPresent());
        assertEquals("Juan", result.get().getNombre());
        verify(clienteRepository, times(1)).findById(anyLong());
    }

    @Test
    void getClienteNotFound() {
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<ClienteDTO> result = clienteService.getCliente(1L);
        assertFalse(result.isPresent());
        verify(clienteRepository, times(1)).findById(anyLong());
    }

    @Test
    void getAllClientes() {
        List<Cliente> clientes = List.of(createEntity(1L), createEntity(2L));
        when(clienteRepository.findAll()).thenReturn(clientes);

        List<ClienteDTO> result = clienteService.getAllClientes();
        assertEquals(2, result.size());
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    void updateCliente() {
        Cliente cliente = createEntity(1L);
        ClienteDTO dto = createDTO(1L);
        // Cambiar cédula y email para que se ejecuten las validaciones
        dto.setCedula("1122334455"); // Diferente a "1234567890"
        dto.setEmail("updated.client@example.com"); // Diferente a "juan.perez@example.com"
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));
        when(clienteRepository.findByCedula(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        ClienteDTO result = clienteService.updateCliente(1L, dto);
        assertEquals(1L, result.getId_Cliente());
        verify(clienteRepository, times(1)).findById(anyLong());
        verify(clienteRepository, times(1)).findByCedula(anyString());
        verify(clienteRepository, times(1)).findByEmail(anyString());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void updateClienteNotFound() {
        ClienteDTO dto = createDTO(1L);
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> clienteService.updateCliente(1L, dto));
        verify(clienteRepository, times(1)).findById(anyLong());
    }

    @Test
    void deleteCliente() {
        Cliente cliente = createEntity(1L);
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));
        doNothing().when(clienteRepository).deleteById(anyLong());

        clienteService.deleteCliente(1L);
        verify(clienteRepository, times(1)).findById(anyLong());
        verify(clienteRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void deleteClienteNotFound() {
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> clienteService.deleteCliente(1L));
        verify(clienteRepository, times(1)).findById(anyLong());
    }
}