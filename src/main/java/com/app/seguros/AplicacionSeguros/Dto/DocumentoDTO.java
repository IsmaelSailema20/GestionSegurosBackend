package com.app.seguros.AplicacionSeguros.Dto;

import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.TipoDocumento;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class DocumentoDTO {
    private Long idDocumento;
    private String nombreArchivo;
    private EstadoDocumento estado;
    private TipoDocumento tipo;
    private LocalDateTime fechaSubida;
    private Long idCliente;
    private Long idContrato;
    //private String urlDescarga;
}
