package vistas;

import controlador.GestorSistema;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import modelo.Equipo;
import modelo.Jugador;

public class MenuPrincipal extends JFrame {

    private final GestorSistema gestor;
    private JLabel lblLogoHeader;
    private JLabel lblUsuario;
    private JLabel lblPermisos;
    private JPanel grillaTarjetas;

    public MenuPrincipal() {
        this.gestor = GestorSistema.getInstancia();
        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setTitle("Panel Principal — Torneo de Fútbol Amateur UGD [" + gestor.getRolActual() + "]");
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void inicializarComponentes() {
        String rol = gestor.getRolActual();

        JPanel panelTop = new JPanel(new BorderLayout(15, 10));
        panelTop.setBackground(UtilidadesImagen.VERDE_OSCURO);
        panelTop.setBorder(new EmptyBorder(14, 20, 14, 20));

        lblLogoHeader = new JLabel(obtenerIconoHeader(rol));
        panelTop.add(lblLogoHeader, BorderLayout.WEST);

        JPanel panelInfoSesion = new JPanel(new GridLayout(3, 1, 2, 2));
        panelInfoSesion.setOpaque(false);

        JLabel lblTitulo = new JLabel("SISTEMA DE GESTIÓN DE TORNEO DE FÚTBOL AMATEUR — UGD");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 19));
        lblTitulo.setForeground(Color.WHITE);

        lblUsuario = new JLabel("👤 Usuario activo: " + gestor.getNombreActorLogueado() + "   |   Rol: " + rol);
        lblUsuario.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblUsuario.setForeground(UtilidadesImagen.DORADO_ACENTO);

        lblPermisos = new JLabel(obtenerDescripcionPermisos(rol));
        lblPermisos.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblPermisos.setForeground(new Color(220, 238, 225));

        panelInfoSesion.add(lblTitulo);
        panelInfoSesion.add(lblUsuario);
        panelInfoSesion.add(lblPermisos);
        panelTop.add(panelInfoSesion, BorderLayout.CENTER);

        // Botones superiores: Modificar Perfil y Cerrar Sesión
        JPanel panelAccionesTop = new JPanel(new GridLayout(2, 1, 5, 5));
        panelAccionesTop.setOpaque(false);

        // Modificar perfil en ventana nueva
        JButton btnModificarPerfil = UtilidadesImagen.crearBotonEstilizado(" Modificar Mi Perfil", UtilidadesImagen.AZUL_ACCION, Color.WHITE);
        btnModificarPerfil.addActionListener(e -> {
            Object persona = (gestor.getUsuarioActual() != null) ? gestor.getUsuarioActual() : gestor.getJugadorActual();
            if (persona != null) {
                VentanaModificarPerfil dlg = new VentanaModificarPerfil(this, persona, () -> refrescarEncabezadoSesion());
                dlg.setVisible(true);
            }
        });

        JButton btnCerrarSesion = UtilidadesImagen.crearBotonEstilizado(" Cerrar Sesión", new Color(180, 45, 45), Color.WHITE);
        btnCerrarSesion.addActionListener(e -> {
            gestor.cerrarSesion();
            new FormularioLogin().setVisible(true);
            this.dispose();
        });

        panelAccionesTop.add(btnModificarPerfil);
        panelAccionesTop.add(btnCerrarSesion);
        panelTop.add(panelAccionesTop, BorderLayout.EAST);

        add(panelTop, BorderLayout.NORTH);

        // --- CENTRO: GRILLA DE TARJETAS RESPONSIVA ---
        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.setBackground(UtilidadesImagen.FONDO_CLARO);
        panelCentro.setBorder(new EmptyBorder(16, 22, 16, 22));

        grillaTarjetas = new JPanel(new GridLayout(0, 3, 16, 16));
        grillaTarjetas.setBackground(UtilidadesImagen.FONDO_CLARO);

        construirTarjetasPorRol(rol, grillaTarjetas);

        JScrollPane scrollTarjetas = new JScrollPane(grillaTarjetas);
        scrollTarjetas.setBorder(null);
        scrollTarjetas.getViewport().setBackground(UtilidadesImagen.FONDO_CLARO);
        panelCentro.add(scrollTarjetas, BorderLayout.CENTER);

        add(panelCentro, BorderLayout.CENTER);
    }

    private Icon obtenerIconoHeader(String rol) {
        // Requisito 1: Cuando un jugador y un capitán ingresen a su cuenta, el logo debe ser el escudo de su equipo.
        if ("JUGADOR".equals(rol) || "CAPITAN".equals(rol)) {
            Equipo eq = gestor.buscarEquipoDeJugador(gestor.getJugadorActual());
            if (eq != null) {
                return UtilidadesImagen.cargarEscudoEquipo(eq, 70, 70);
            }
            return UtilidadesImagen.cargarLogoSistema(70, 70);
        }
        // Requisito 2: En los perfiles de Administrador, Organizador y Árbitro sacar logo de equipo y mostrar el del sistema.
        return UtilidadesImagen.cargarLogoSistema(70, 70);
    }

    public void refrescarEncabezadoSesion() {
        String rol = gestor.getRolActual();
        lblUsuario.setText(" Usuario activo: " + gestor.getNombreActorLogueado() + "   |    Rol: " + rol);
        lblPermisos.setText(obtenerDescripcionPermisos(rol));
        lblLogoHeader.setIcon(obtenerIconoHeader(rol));
        setTitle("Panel Principal - Torneo de Fútbol Amateur UGD [" + rol + "]");

        // Requisito 7: Al cambiar de categoría (Capitán <-> Jugador), refrescar tarjetas y permisos
        if (grillaTarjetas != null) {
            grillaTarjetas.removeAll();
            construirTarjetasPorRol(rol, grillaTarjetas);
            grillaTarjetas.revalidate();
            grillaTarjetas.repaint();
        }
        revalidate();
        repaint();
    }

    private String obtenerDescripcionPermisos(String rol) {
        switch (rol) {
            case "ADMINISTRADOR":
                return "Permisos de Administrador: ABM de Usuarios, Torneos, Canchas y supervisión completa.";
            case "ORGANIZADOR":
                return "Permisos de Organizador: Evaluar Inscripciones, Programar Partidos, Aplicar Sanciones, Resolver Reclamos y Exportar PDFs.";
            case "ARBITRO":
                return "Permisos de Árbitro: Cargar Resultados, registrar Incidentes, consultar Fixture completo y Aplicar Sanciones.";
            case "CAPITAN":
                return "Permisos de Capitán: Gestionar su Equipo, Cargar Plantel, Adjuntar Documentación, Realizar Reclamos y ver Resultados.";
            case "JUGADOR":
                return "Permisos de Jugador: Ver su Perfil, Credencial, consultar Resultados de su equipo, Posiciones, Goleadores y Sanciones.";
            default:
                return "Modo Consulta: Calendario de Partidos, Tabla de Posiciones, Goleadores e Historial.";
        }
    }

    private void construirTarjetasPorRol(String rol, JPanel grilla) {
        if ("ADMINISTRADOR".equals(rol)) {
            grilla.add(crearTarjeta(" Gestión de Usuarios (ABM)",
                    "Alta, Modificación en ventana nueva y Baja de Administradores, Organizadores, Árbitros, Capitanes y Jugadores.",
                    new Color(22, 101, 52), () -> new VentanaUsuarios(this).setVisible(true)));

            grilla.add(crearTarjeta("🏆 Torneos y Fases (ABM)",
                    "Crear Torneos seleccionando equipos participantes, Sorteo automático de grupos, Avance automático de fases y Campeón.",
                    new Color(180, 120, 15), () -> new VentanaTorneos(this).setVisible(true)));

            grilla.add(crearTarjeta("🏟️ Gestión de Canchas (ABM)",
                    "Registrar nuevas canchas, modificar ubicación y administrar disponibilidad de sedes para los equipos.",
                    new Color(25, 103, 178), () -> new VentanaCanchas(this).setVisible(true)));

            grilla.add(crearTarjeta("🛡️ Equipos e Inscripciones",
                    "Inscribir equipos con sede existente, cambiar capitanes, cargar planteles, subir escudo propio e imprimir credenciales.",
                    new Color(109, 40, 217), () -> new VentanaEquiposInscripciones(this).setVisible(true)));

            grilla.add(crearTarjeta("⚽ Partidos y Resultados",
                    "Programar encuentros con fecha y hora, postergar/reprogramar y confirmar resultados con actas de incidentes.",
                    new Color(15, 118, 110), () -> new VentanaPartidos(this).setVisible(true)));

            grilla.add(crearTarjeta("⚖️ Sanciones y Reclamos",
                    "Aplicar sanciones disciplinarias a jugadores, presentar reclamos de partidos y dictaminar resoluciones.",
                    new Color(185, 28, 28), () -> new VentanaSancionesReclamos(this).setVisible(true)));

            grilla.add(crearTarjeta("📊 Reportes y Exportación PDF",
                    "Tabla de posiciones, goleadores, sancionados, árbitros e historiales con exportación a PDF oficial.",
                    new Color(55, 65, 81), () -> new VentanaReportes(this).setVisible(true)));

        } else if ("ORGANIZADOR".equals(rol)) {
            grilla.add(crearTarjeta("🛡️ Evaluar Inscripciones y Equipos",
                    "Validar edad mínima, sanciones y documentación de equipos con sede existente.",
                    new Color(22, 101, 52), () -> new VentanaEquiposInscripciones(this).setVisible(true)));

            grilla.add(crearTarjeta("⚽ Programar y Reprogramar Partidos",
                    "Programar fixture con fecha y horario validando cancha y árbitro, postergar y reprogramar partidos.",
                    new Color(25, 103, 178), () -> new VentanaPartidos(this).setVisible(true)));

            grilla.add(crearTarjeta("⚖️ Sanciones y Resolver Reclamos",
                    "Aplicar suspensiones a jugadores y examinar/resolver reclamos enviados por los capitanes.",
                    new Color(185, 28, 28), () -> new VentanaSancionesReclamos(this).setVisible(true)));

            grilla.add(crearTarjeta("🏆 Fases del Torneo y Campeón",
                    "Avanzar de Fase automáticamente según resultados (Grupos -> Octavos -> Cuartos -> Semis -> Final).",
                    new Color(180, 120, 15), () -> new VentanaTorneos(this).setVisible(true)));

            grilla.add(crearTarjeta("📊 Reportes y Exportación de PDFs",
                    "Generar Tabla de Posiciones, Goleadores, Sancionados, Árbitros e Historiales en PDF.",
                    new Color(55, 65, 81), () -> new VentanaReportes(this).setVisible(true)));

        } else if ("ARBITRO".equals(rol)) {
            // Requisito 18: Fixture completo para árbitro con partidos pendientes/finalizados/postergados
            grilla.add(crearTarjeta("⚽ Fixture y Calendario de Partidos",
                    "Consultar todos los partidos pendientes, finalizados y postergados del torneo con sus fechas y canchas.",
                    new Color(25, 103, 178), () -> new VentanaPartidos(this).setVisible(true)));

            grilla.add(crearTarjeta("📝 Cargar Resultados e Incidentes",
                    "Registrar goles y tarjetas (amarillas/rojas con suspensión automática) en tus partidos asignados.",
                    new Color(22, 101, 52), () -> new VentanaPartidos(this).setVisible(true)));

            // Requisito 6: Los árbitros también pueden aplicar sanciones
            grilla.add(crearTarjeta("🟥 Consultar y Aplicar Sanciones",
                    "Consultar suspensiones vigentes y aplicar sanciones disciplinarias directamente a jugadores.",
                    new Color(185, 28, 28), () -> new VentanaSancionesReclamos(this).setVisible(true)));

            grilla.add(crearTarjeta("📊 Posiciones, Goleadores y Reportes",
                    "Consultar tabla de posiciones, goleadores y reporte de partidos dirigidos por árbitros.",
                    new Color(109, 40, 217), () -> new VentanaReportes(this).setVisible(true)));

        } else if ("CAPITAN".equals(rol)) {
            grilla.add(crearTarjeta("🛡️ Mi Equipo, Plantel e Inscripción",
                    "Cargar jugadores a tu plantel, adjuntar documentación de inscripción, subir escudo propio e imprimir credenciales.",
                    new Color(22, 101, 52), () -> new VentanaEquiposInscripciones(this).setVisible(true)));

            grilla.add(crearTarjeta("⚖️ Presentar Reclamos de Partidos",
                    "Realizar reclamos formales sobre partidos finalizados de tu equipo y seguir su dictamen oficial.",
                    new Color(185, 28, 28), () -> new VentanaSancionesReclamos(this).setVisible(true)));

            grilla.add(crearTarjeta("⚽ Ver Resultados y Calendario",
                    "Consultar las fechas, horarios, canchas y resultados de los partidos de tu equipo y del torneo.",
                    new Color(25, 103, 178), () -> new VentanaPartidos(this).setVisible(true)));

            grilla.add(crearTarjeta("📊 Posiciones, Goleadores e Historial",
                    "Ver la tabla de posiciones en vivo, goleadores e historial de tu equipo (con exportación PDF).",
                    new Color(109, 40, 217), () -> new VentanaReportes(this).setVisible(true)));

        } else if ("JUGADOR".equals(rol)) {
            // Requisitos 19 y 20: Cuenta de jugador con datos de sí mismo y de su equipo
            grilla.add(crearTarjeta("🪪 Mi Perfil y Mi Credencial",
                    "Ver tu ficha personal, posición, modificar tu perfil e imprimir tu Credencial oficial con código de barras.",
                    new Color(22, 101, 52), () -> new VentanaEquiposInscripciones(this).setVisible(true)));

            grilla.add(crearTarjeta("⚽ Partidos y Resultados de Mi Equipo",
                    "Consultar las fechas, horarios y canchas donde juega tu equipo y los resultados e incidentes.",
                    new Color(25, 103, 178), () -> new VentanaPartidos(this).setVisible(true)));

            grilla.add(crearTarjeta("📊 Mi Posición, Mis Goles y Mi Historial",
                    "Ver las estadísticas de tu equipo, tus goles convertidos, historial y reporte arbitral de tu equipo.",
                    new Color(109, 40, 217), () -> new VentanaReportes(this).setVisible(true)));

            grilla.add(crearTarjeta("🟥 Consultar Mis Sanciones",
                    "Verificar si contás con tarjetas acumuladas o sanciones disciplinarias vigentes en el torneo.",
                    new Color(185, 28, 28), () -> new VentanaSancionesReclamos(this).setVisible(true)));
        }
    }

    private JPanel crearTarjeta(String titulo, String descripcion, Color colorBorde, Runnable accion) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(5, 1, 1, 1, colorBorde),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTit.setForeground(colorBorde);

        JTextArea txtDesc = new JTextArea(descripcion);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setLineWrap(true);
        txtDesc.setEditable(false);
        txtDesc.setOpaque(false);
        txtDesc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtDesc.setForeground(new Color(70, 75, 80));

        JButton btnAbrir = UtilidadesImagen.crearBotonEstilizado("Ingresar al Módulo →", colorBorde, Color.WHITE);
        btnAbrir.addActionListener(e -> accion.run());

        card.add(lblTit, BorderLayout.NORTH);
        card.add(txtDesc, BorderLayout.CENTER);
        card.add(btnAbrir, BorderLayout.SOUTH);
        return card;
    }
}
