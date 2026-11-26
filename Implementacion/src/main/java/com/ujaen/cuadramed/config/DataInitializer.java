package com.ujaen.cuadramed.config;

import com.ujaen.cuadramed.model.Especialidad;
import com.ujaen.cuadramed.model.Grupo;
import com.ujaen.cuadramed.model.Rol;
import com.ujaen.cuadramed.model.Usuario;
import com.ujaen.cuadramed.repository.GrupoRepository;
import com.ujaen.cuadramed.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {
    @Bean
    @Order(1)
    CommandLineRunner crearUsuariosDePrueba(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            crearSiNoExiste(repo, encoder, "Admin", "663465813","admin@cuadramed.es", "admin123", Set.of(Rol.ADMINISTRADOR));
            crearSiNoExiste(repo, encoder, "Ana Responsable", "643156843","responsable@cuadramed.es", "resp123", Set.of(Rol.PLANIFICADOR, Rol.MEDICO));
            crearSiNoExiste(repo, encoder, "Marcos Médico", "691345655","medico@cuadramed.es", "medico123", Set.of(Rol.MEDICO));
            crearSiNoExiste(repo, encoder, "Laura Responsable", "622114477","responsable2@cuadramed.es", "resp123", Set.of(Rol.PLANIFICADOR, Rol.MEDICO));
            crearSiNoExiste(repo, encoder, "Pedro Médico", "677889900","medico2@cuadramed.es", "medico123", Set.of(Rol.MEDICO));
             };
    }

    @Bean
    @Order(2)
    @Profile("!test")
    CommandLineRunner crearGrupoDePrueba(UsuarioRepository repo, GrupoRepository repoGrupo) {
        return args -> {
            Usuario ana = repo.findByEmail("responsable@cuadramed.es").orElseThrow();
            crearGrupoSiNoExiste(repoGrupo, "Urgencias", Especialidad.URGENCIAS, ana);
        };
    }

    private void crearSiNoExiste(UsuarioRepository repo, PasswordEncoder encoder,
                                 String nombre, String telefono,String email, String password, Set<Rol> roles) {
        if (!repo.existsByEmail(email)) {
            repo.save(new Usuario(nombre, telefono, email, encoder.encode(password), roles));
        }
    }

    private void crearGrupoSiNoExiste(GrupoRepository repoGrupo, String nombre, Especialidad especialidad,Usuario responsable){
        if(!repoGrupo.existsByEspecialidad(especialidad)){
            Grupo nuevogrupo = new Grupo(nombre,especialidad);
            nuevogrupo.asignarResponsable(responsable);
            repoGrupo.save(nuevogrupo);
        }
    }
}
