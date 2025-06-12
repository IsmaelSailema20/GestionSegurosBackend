package com.app.seguros.AplicacionSeguros.Dto;

import com.app.seguros.AplicacionSeguros.Entities.Enums.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DocumentoRegistroDTO {
    private Long idCliente;
    private Long idContrato;
    private TipoDocumento tipo;
}
