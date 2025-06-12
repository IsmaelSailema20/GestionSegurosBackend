package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Config.AESUtil;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Tarjeta;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class TarjetaServiceTest {
    @InjectMocks
    TarjetaService tarjetaService;

    @Mock
    ClienteRepository clienteRepository;

    private Tarjeta tarjeta;
    private Cliente cliente;
    private AESUtil aesUtil;

    @BeforeEach
    void setup(){
        //Intancia para crear el cliente
        cliente = new Cliente();
        cliente.setCedula("180521840");
        cliente.setEstado(Estado.Activo);
        cliente.setNombre("Test");
        cliente.setEmail("test@test.com");
        cliente.setDireccion("Ambato");
        cliente.setApellido("Prueba");
        cliente.setGenero("Masculino");
        cliente.setTelefono("1234567980");
        cliente.setId(1L);
        cliente.setEstaturaMetros(1.7);
        LocalDate fch = LocalDate.of(2003,11,05);
        Date fechaNacimiento = Date.from(fch.atStartOfDay(ZoneId.systemDefault()).toInstant());
        cliente.setFecha_nacimiento(fechaNacimiento);
        //Instacia para crear la tarjeta test
        tarjeta = new Tarjeta();
        tarjeta.setTipo("Visa");
        tarjeta.setNumero("1234567890123");
        LocalDate vencimiento = LocalDate.of(2026,12,1);
        tarjeta.setVencimiento(vencimiento);
        tarjeta.setCvv("156");
        tarjeta.setId(1L);
        tarjeta.setCliente(cliente);
    }
}

