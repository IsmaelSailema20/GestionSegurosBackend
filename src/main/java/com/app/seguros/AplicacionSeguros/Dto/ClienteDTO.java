package com.app.seguros.AplicacionSeguros.Dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class ClienteDTO {
    private Long id;
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;
    @NotBlank(message = "La direeccion es obligattoria")
    private String direccion;
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;

    private String telefono;
    @NotBlank(message = "El genero es obligatorio")
    private String genero;
    @NotBlank(message = "La cédula es obligatoria")
    private String ceduña;

    @NotBlank(message = "La fecha de nacimeinto es obligatoria")
    private Date fecha_nacimiento;

}
