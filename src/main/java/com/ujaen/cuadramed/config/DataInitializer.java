package com.ujaen.cuadramed.config;

import com.ujaen.cuadramed.model.Rol;
import com.ujaen.cuadramed.model.Usuario;
import com.ujaen.cuadramed.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner crearUsuariosDePrueba(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            crearSiNoExiste(repo, encoder, "Admin", "663465813","admin@cuadramed.es", "admin123", Rol.ADMINISTRADOR);
            crearSiNoExiste(repo, encoder, "Ana Responsable", "643156843","responsable@cuadramed.es", "resp123", Rol.RESPONSABLE);
            crearSiNoExiste(repo, encoder, "Marcos Médico", "691345655","medico@cuadramed.es", "medico123", Rol.MEDICO);
        };
    }

    private void crearSiNoExiste(UsuarioRepository repo, PasswordEncoder encoder,
                                 String nombre, String telefono,String email, String password, Rol rol) {
        if (!repo.existsByEmail(email)) {
            repo.save(new Usuario(nombre, telefono, email, encoder.encode(password), rol));
        }
    }
}
