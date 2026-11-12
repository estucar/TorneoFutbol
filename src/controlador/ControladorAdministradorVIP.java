package controlador;

import modelo.*;
import java.util.List;

public class ControladorAdministradorVIP {
    
    private List<Usuario> baseUsuarios;
    private Usuario usuarioLogueado;

    public ControladorAdministradorVIP(List<Usuario> baseUsuarios) {
        this.baseUsuarios = baseUsuarios;
    }

    public boolean iniciarSesion(String email, String contrasenia) {
        for (Usuario u : baseUsuarios) {
            if (u.getEmail().equals(email) && u.getContrasenia().equals(contrasenia)) {
                this.usuarioLogueado = u;
                return true;
            }
        }
        return false;
    }

    public Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }

    public void cerrarSesion() {
        this.usuarioLogueado = null;
    }
}