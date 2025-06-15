package com.app.seguros.AplicacionSeguros.Entities.Enums;

public enum TipoDocumento {
    Identificacion,
    Historial_medico,
    Formulario,
    Comprobante_pago,

    //PARA REEMBOLSO DE consultas_medicas
    Factura_consulta,
    Informe_medico,

    //PARA REEMBOLSSO DE examenes_medicos
    Orden_medica,
    Resultado_laboratorio,
    Factura_laboratorio,

    //PARA REEMBOLSO DE internacion
    Factura_hospital,
    Informe_alta,
    autorizacion_seguro,
}
