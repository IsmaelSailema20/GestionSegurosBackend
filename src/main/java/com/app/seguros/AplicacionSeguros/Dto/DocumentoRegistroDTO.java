package com.app.seguros.AplicacionSeguros.Dto;

import com.app.seguros.AplicacionSeguros.Entities.Enums.TipoDocumento;
import lombok.Data;

@Data
public class DocumentoRegistroDTO {
    private Long idCliente;
    private Long idContrato;
    private TipoDocumento tipo;
}
