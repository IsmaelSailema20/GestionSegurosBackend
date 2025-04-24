package com.app.seguros.AplicacionSeguros.Entities;

import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioCrearRequest {
    String email;
    String password;
    String nombre;
    String apellido;
    Rol rol;
}
