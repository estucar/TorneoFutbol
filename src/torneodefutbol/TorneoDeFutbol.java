package torneodefutbol;

import controlador.GestorSistema;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import vistas.FormularioLogin;

public class TorneoDeFutbol {

    public static void main(String[] args) {
        // Configurar estilo visual moderno (Nimbus)
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        // Inicializar GestorSistema (conecta a MySQL, crea las tablas si no existen y crea el Administrador por defecto)
        GestorSistema.getInstancia();

        // Lanzar la ventana de Iniciar Sesión / Registrarse
        SwingUtilities.invokeLater(() -> {
            FormularioLogin login = new FormularioLogin();
            login.setVisible(true);
        });
    }
}
