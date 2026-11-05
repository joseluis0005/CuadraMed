package com.ujaen.cuadramed.service;

import com.ujaen.cuadramed.dto.LoginDTO;
import com.ujaen.cuadramed.dto.RespuestaAutenticacionDTO;
import com.ujaen.cuadramed.excepciones.CredencialesInvalidasExcep;
import com.ujaen.cuadramed.model.Usuario;
import com.ujaen.cuadramed.repository.UsuarioRepository;
import com.ujaen.cuadramed.seguridad.JwtUtils;
import com.ujaen.cuadramed.seguridad.ConfigSeguridad;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.cert.CertificateRevokedException;

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


    public RespuestaAutenticacionDTO login(@Valid LoginDTO loginDTO){
        String emailUsuario = loginDTO.email();

        if(!repositorioUsuario.existsByEmail(emailUsuario)){
            throw new CredencialesInvalidasExcep("El usuario o la contraseña no son válidos");
        }

        Usuario usuario = repositorioUsuario.findByEmail(emailUsuario).get();

        if(!passwordEncoder.matches(loginDTO.password(), usuario.getContrasena())){
            throw new CredencialesInvalidasExcep("El usuario o la contraseña no son válidos");
        }

        String token = jwtUtils.generateToken(usuario.getId(), usuario.getRol().name(), usuario.getEmail());

        return new RespuestaAutenticacionDTO(usuario.getId(),usuario.getRol(),token);
    }
}
