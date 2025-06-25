package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Dto.UsuarioCrearRequest;
import com.app.seguros.AplicacionSeguros.Repositories.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class UsuariosControllerIntegracionTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuariosRepository usuariosRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper=new ObjectMapper();

    private static Usuario create(int id){
        Usuario u=new Usuario();
        u.setID_Usuario(id);
        u.setApellido("apellido");
        u.setContraseña("contraseña");
        u.setNombre("nombre");
        u.setEstado(Estado.Activo);
        u.setEmail("email");
        u.setRol(Rol.Administrador);
        return u;
    }

    private String token="";
    private int idGenerado=0;

    @BeforeEach
    void setUp(){
        borrarTodo();
        Usuario u= Usuario.builder()
                .rol(Rol.Administrador)
                .nombre("nombre")
                .apellido("apellido")
                .estado(Estado.Activo)
                .email("email")
                .contraseña(passwordEncoder.encode("contraseña"))
                .build();
        idGenerado=usuariosRepository.save(u).getID_Usuario();

        Usuario u1= Usuario.builder()
                .rol(Rol.Administrador)
                .nombre("nombre1")
                .apellido("apellido1")
                .estado(Estado.Activo)
                .email("email1")
                .contraseña(passwordEncoder.encode("contraseña1"))
                .build();
        usuariosRepository.save(u1);
        JwtService jwtService=new JwtService();
        token= jwtService.getToken(u);
    }

    private void borrarTodo() {
        pagoDocumentoRepository.deleteAll();
        documentoRepository.deleteAll();
        beneficiarioContratoRepository.deleteAll();
        condicionMedicaClienteRepository.deleteAll();
        beneficioSeguroRepository.deleteAll();
        reembolsoDocumentoRepository.deleteAll();

        // Entidades dependientes de contrato/cliente/seguro
        pagoRepository.deleteAll();
        reembolsoRepository.deleteAll();
        contratoRepository.deleteAll();
        tarjetaRepository.deleteAll();
        beneficiarioRepository.deleteAll();
        beneficioRepository.deleteAll();
        clienteRepository.deleteAll();

        // Entidades base
        condicionMedicaRepository.deleteAll();
        seguroRepository.deleteAll();
        usuariosRepository.deleteAll();
    }

    @Test
    void postUsuario() throws  Exception{
        UsuarioCrearRequest uGuardar=new UsuarioCrearRequest("emailGuardar","contraseña","nombre","apellido",Rol.Administrador);
        mockMvc.perform(post("/Usuarios/guardar")
                        .header("Authorization","Bearer "+token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uGuardar)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("emailGuardar"));

    }
    @Test
    void postUsuarioNull() throws  Exception{
        mockMvc.perform(post("/Usuarios/guardar")
                        .header("Authorization","Bearer "+token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void getAll() throws Exception{
        mockMvc.perform(get("/Usuarios")
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value("2"));

    }



    @Test
    void getById() throws  Exception{
        mockMvc.perform(get("/Usuarios/"+idGenerado)
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("nombre"));

    }

    @Test
    void getByIdNotFound() throws  Exception{
        mockMvc.perform(get("/Usuarios/15")
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isNotFound());

    }

    @Test
    void deleteById() throws Exception{
        mockMvc.perform(delete("/Usuarios/"+idGenerado)
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isOk());
    }

    @Test
    void deleteByIdNull() throws Exception{
        mockMvc.perform(delete("/Usuarios/15")
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isNotFound());
    }

    @Autowired protected BeneficiarioContratoRepository beneficiarioContratoRepository;
    @Autowired protected BeneficiarioRepository beneficiarioRepository;
    @Autowired protected BeneficioRepository beneficioRepository;
    @Autowired protected BeneficioSeguroRepository beneficioSeguroRepository;
    @Autowired protected ClienteRepository clienteRepository;
    @Autowired protected CondicionMedicaClienteRepository condicionMedicaClienteRepository;
    @Autowired protected CondicionMedicaRepository condicionMedicaRepository;
    @Autowired protected ContratoRepository contratoRepository;
    @Autowired protected DocumentoRepository documentoRepository;
    @Autowired protected PagoDocumentoRepository pagoDocumentoRepository;
    @Autowired protected PagoRepository pagoRepository;
    @Autowired protected ReembolsoDocumentoRepository reembolsoDocumentoRepository;
    @Autowired protected ReembolsoRepository reembolsoRepository;
    @Autowired protected SeguroRepository seguroRepository;
    @Autowired protected TarjetaRepository tarjetaRepository;

}