package com.GAKOM_ECOTACNA.ECOTACNA.dto;

import com.GAKOM_ECOTACNA.ECOTACNA.model.CompanyType;
import com.GAKOM_ECOTACNA.ECOTACNA.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotNull(message = "El RUC es obligatorio")
    @Pattern(regexp = "\\d{11}", message = "El RUC debe constar de 11 dígitos numéricos")
    private String ruc;

    @NotNull(message = "El email corporativo es obligatorio")
    @Email(message = "El formato de email es inválido")
    @Size(max = 150)
    private String email;

    @NotNull(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 50, message = "La contraseña debe tener entre 8 y 50 caracteres")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "La contraseña debe contener al menos una letra y un número")
    private String password;

    @NotNull(message = "La confirmación de contraseña es obligatoria")
    private String confirmPassword;

    @NotNull(message = "El nombre es obligatorio")
    @Pattern(
        regexp = "^(?!.*\\s{2,})(?!.*\\.{2,})(?![.\\-'\\s])(?!.*[.\\-'\\s]$)[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+(?:[ .'-][A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)*$",
        message = "Ingrese un nombre de contacto válido. Use solo letras, espacios, punto, guion o apóstrofo."
    )
    @Size(min = 3, max = 80, message = "La persona de contacto debe tener entre 3 y 80 caracteres.")
    private String firstName;

    @NotNull(message = "El apellido es obligatorio")
    @Size(max = 100)
    private String lastName;

    @NotNull(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^9\\d{8}$", message = "El teléfono debe ser un celular peruano válido de 9 dígitos que empiece con 9.")
    private String phone;

    @NotNull(message = "El rol de la empresa es obligatorio")
    private Role role;
}
