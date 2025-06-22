package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.DocumentoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.PagoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.PagoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.SeguroDTO;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Documento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MetodoPago;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Tipo;
import com.app.seguros.AplicacionSeguros.Entities.Pago;
import com.app.seguros.AplicacionSeguros.Entities.PagoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Seguro;
import com.app.seguros.AplicacionSeguros.Repositories.ContratoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.PagoDocumentoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.PagoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private SeguroService seguroService;

    @Mock
    private DocumentoService documentoService;

    @Mock
    private PagoDocumentoRepository pagoDocumentoRepository;

    @InjectMocks
    private PagoService pagoService;

    private Contrato contrato;
    private SeguroDTO seguroDTO;
    private Pago pago;
    private PagoRegistroDTO pagoRegistroDTO;

    @BeforeEach
    void setUp() {
        contrato = new Contrato();
        contrato.setId(1);
        Seguro seguro = new Seguro();
        seguro.setIdSeguro(1);
        contrato.setSeguro(seguro);

        seguroDTO = new SeguroDTO(Tipo.Vida, Estado.Activo);
        seguroDTO.setId(1);
        seguroDTO.setPrecio(1000.0);
        seguroDTO.setTipo(Tipo.Vida);
        seguroDTO.setEstado(Estado.Activo);
        seguroDTO.setNombre("Seguro Auto");
        seguroDTO.setDescripcion("Cobertura completa");
        seguroDTO.setCobertura("Daños y robo");

        pago = new Pago();
        pago.setId(1L);
        pago.setContrato(contrato);
        pago.setMonto(BigDecimal.valueOf(1000.0));
        pago.setFechaPago(LocalDate.now());
        pago.setMetodo(MetodoPago.Tarjeta);
        pago.setConfirmado(EstadoDocumento.Aprobado);

        pagoRegistroDTO = PagoRegistroDTO.builder()
                .contratoId(1L)
                .metodo(MetodoPago.Tarjeta)
                .build();
    }

    @Test
    void registrarPago_Success() {
        when(contratoRepository.findById(1)).thenReturn(Optional.of(contrato));
        when(seguroService.getSeguro(1)).thenReturn(Optional.of(seguroDTO));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);

        PagoRespuestaDTO result = pagoService.registrarPago(pagoRegistroDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BigDecimal.valueOf(1000.0), result.getMonto());
        assertEquals(EstadoDocumento.Aprobado, result.getConfirmado());
        verify(pagoRepository).save(any(Pago.class));
    }

    @Test
    void registrarPago_ContratoNotFound_ThrowsException() {
        when(contratoRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pagoService.registrarPago(pagoRegistroDTO));
    }

    @Test
    void registrarPago_SeguroNotFound_ThrowsException() {
        when(contratoRepository.findById(1)).thenReturn(Optional.of(contrato));
        when(seguroService.getSeguro(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pagoService.registrarPago(pagoRegistroDTO));
    }

    @Test
    void registrarPago_NullPrecio_ThrowsException() {
        seguroDTO.setPrecio(null);
        when(contratoRepository.findById(1)).thenReturn(Optional.of(contrato));
        when(seguroService.getSeguro(1)).thenReturn(Optional.of(seguroDTO));

        assertThrows(NullPointerException.class, () -> pagoService.registrarPago(pagoRegistroDTO));
    }

    @Test
    void registrarPagoConComprobante_Success() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        DocumentoRegistroDTO documentoDTO = new DocumentoRegistroDTO();
        Documento documento = new Documento();
        documento.setIdDocumento(1L);

        when(contratoRepository.findById(1)).thenReturn(Optional.of(contrato));
        when(seguroService.getSeguro(1)).thenReturn(Optional.of(seguroDTO));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);
        when(documentoService.subirDocumento(any(), any())).thenReturn(documento);
        when(pagoDocumentoRepository.save(any(PagoDocumento.class))).thenReturn(new PagoDocumento());

        PagoRespuestaDTO result = pagoService.registrarPagoConComprobante(pagoRegistroDTO, mockFile, documentoDTO);

        assertNotNull(result);
        assertEquals(EstadoDocumento.Aprobado, result.getConfirmado());
        verify(pagoRepository).save(any(Pago.class));
        verify(documentoService).subirDocumento(any(), any());
        verify(pagoDocumentoRepository).save(any(PagoDocumento.class));
    }

    @Test
    void listarPagosPorContrato_Success() {
        List<Pago> pagos = Arrays.asList(pago);
        when(pagoRepository.findByContratoId(1L)).thenReturn(pagos);

        List<PagoRespuestaDTO> result = pagoService.listarPagosPorContrato(1L);

        assertEquals(1, result.size());
        assertEquals(pago.getId(), result.get(0).getId());
        verify(pagoRepository).findByContratoId(1L);
    }

    @Test
    void listarPagosPendientesPorContrato_Success() {
        pago.setConfirmado(EstadoDocumento.Pendiente);
        List<Pago> pagos = Arrays.asList(pago);
        when(contratoRepository.findById(1)).thenReturn(Optional.of(contrato));
        when(pagoRepository.findByContratoId(1L)).thenReturn(pagos);

        List<PagoRespuestaDTO> result = pagoService.listarPagosPendientesPorContrato(1L);

        assertEquals(1, result.size());
        assertEquals(EstadoDocumento.Pendiente, result.get(0).getConfirmado());
    }

    @Test
    void obtenerEstadoUltimoPagoPorContrato_Success() {
        when(pagoRepository.findFirstByContratoIdOrderByIdDesc(1L)).thenReturn(Optional.of(pago));

        EstadoDocumento result = pagoService.obtenerEstadoUltimoPagoPorContrato(1L);

        assertEquals(EstadoDocumento.Aprobado, result);
    }

    @Test
    void confirmarPago_Success() {
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);

        pagoService.confirmarPago(1L);

        verify(pagoRepository).save(pago);
        assertEquals(EstadoDocumento.Aprobado, pago.getConfirmado());
    }

    @Test
    void confirmarPagoDoc_Success() {
        PagoDocumento relacion = new PagoDocumento();
        relacion.setPago(pago);
        relacion.setDocumento(new Documento());
        when(pagoDocumentoRepository.findByDocumentoIdDocumento(1L)).thenReturn(relacion);
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);

        Pago result = pagoService.confirmarPagoDoc(1L);

        assertEquals(EstadoDocumento.Aprobado, result.getConfirmado());
        verify(pagoRepository).save(any(Pago.class));
    }


    @Test
    void rechazarPago_Success() {
        PagoDocumento relacion = new PagoDocumento();
        Documento documento = new Documento();
        documento.setIdDocumento(1L);
        relacion.setPago(pago);
        relacion.setDocumento(documento);
        when(pagoDocumentoRepository.findByDocumentoIdDocumento(1L)).thenReturn(relacion);
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);

        Pago result = pagoService.rechazarPago(1L);

        assertEquals(EstadoDocumento.Rechazado, result.getConfirmado());
        verify(pagoDocumentoRepository).delete(relacion);
        verify(documentoService).rechazarDocumento(1L);
    }
}