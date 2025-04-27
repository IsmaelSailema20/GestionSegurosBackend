package com.app.seguros.AplicacionSeguros.Dto;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

@Data
public class ClienteDTO {
    private Long id_Cliente;
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "La direeccion es obligattoria")
    private String direccion;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;

    @Size(max = 10, message = "El teléfono no puede exceder 10 caracteres")
    private String telefono;

    @NotBlank(message = "El genero es obligatorio")
    private String genero;

    @NotBlank(message = "La cédula es obligatoria")
    @Size(max = 10, message = "La cedula no deebe tener mas de 10 caracteres")
    private String cedula;

    @NotBlank(message = "La fecha de nacimeinto es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private Date fecha_nacimiento;

    @Nonnull
    private Estado estado;

}
