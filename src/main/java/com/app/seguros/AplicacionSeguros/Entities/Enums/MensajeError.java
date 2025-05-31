package com.app.seguros.AplicacionSeguros.Entities.Enums;

public enum MensajeError {
    AGENTE_NO_ENCONTRADO("Agente no encontrado"),
    CEDULA_REGISTRADA("La cédula ya está registrada"),
    EMAIL_REGISTRADO("El email ya está registrado"),
    CLIENTE_NO_ENCONTRADO("Cliente no encontrado"),
    CONDICION_MEDICA_NO_ENCONTRADA("Condición médica no encontrada"),
    SEGURO_NO_ENCONTRADO("Seguro no encontrado"),
    BENEFICIARIO_NO_ENCONTRADO("Beneficiario no encontrado"),
    CONTRATO_NO_ENCONTRADO("Contrato no encontrado"),
    CONTRATO_NO_PENDIENTE("Contrato no esta en pendiente"),
    DOCUMENTO_NO_ENCONTRADO("Documento no encontrado"),
    ERROR_ELIMINAR_ARCHIVO("Error al eliminar el archivo"),
    EERROR_ENVIAR_CORREO("Error al enviar correo"),
    USUARIO_NO_ENCONTRADO("Usuario no encontrado"),
    BENEFICIO_NO_ENCONTRADO("Beneficio no encontrado"),
    SEGURO_CON_CONTRATOS("El Seguro tiene contratos Activos")
    ;

    private final String mensaje;

    MensajeError(String mensaje){
        this.mensaje=mensaje;
    }
    public String getMensaje() {
        return mensaje;
    }
}
