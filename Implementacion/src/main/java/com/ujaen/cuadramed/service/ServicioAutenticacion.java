package com.ujaen.cuadramed.service;

import com.ujaen.cuadramed.dto.LoginDTO;
import com.ujaen.cuadramed.dto.RegistroDTO;
import com.ujaen.cuadramed.dto.RespuestaAutenticacionDTO;
import com.ujaen.cuadramed.excepciones.CredencialesInvalidasExcep;
import com.ujaen.cuadramed.model.Rol;
import com.ujaen.cuadramed.model.Usuario;
import com.ujaen.cuadramed.repository.UsuarioRepository;
import com.ujaen.cuadramed.seguridad.JwtUtils;
import com.ujaen.cuadramed.seguridad.ConfigSeguridad;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.cert.CertificateRevokedException;
import java.util.Set;

@Service
public class ServicioAutenticacion {
    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    ConfigSeguridad configSeguridad;

    @Autowired
    UsuarioRepository repositorioUsuario;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private ServicioMedico servicioMedico;


    public RespuestaAutenticacionDTO login(@Valid LoginDTO loginDTO){
        Usuario usuario = repositorioUsuario.findByEmail(loginDTO.email())
                .orElseThrow(() -> new CredencialesInvalidasExcep("El usuario o la contraseña no son válidos"));

        if(!passwordEncoder.matches(loginDTO.password(), usuario.getContrasena())){
            throw new CredencialesInvalidasExcep("El usuario o la contraseña no son válidos");
        }

        if (!usuario.isActivo()) {
            throw new CredencialesInvalidasExcep("El usuario no se encuentra activo");
        }

        String token = jwtUtils.generateToken(usuario.getId(), usuario.getRoles(), usuario.getEmail());

        return new RespuestaAutenticacionDTO(usuario.getId(),usuario.getRoles(),token);
    }

    public Usuario registro(@Valid RegistroDTO registroDTO){
        String contrasenaEncriptada = passwordEncoder.encode(registroDTO.contrasena());
        Usuario nuevoUsuario = new Usuario(registroDTO.nombre(), registroDTO.telefono(), registroDTO.email(),contrasenaEncriptada, Set.of(Rol.MEDICO));
        servicioMedico.anadirMedico(nuevoUsuario);
        return nuevoUsuario;
    }
}
