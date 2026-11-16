package pe.edu.upeu.moliselva.application.auth.usecase;

import pe.edu.upeu.moliselva.domain.auth.Usuario;

public record Sesion(String token, Usuario usuario) {}
