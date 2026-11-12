package vistas;

import controlador.GestorSistema;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import modelo.*;

public class VentanaSancionesReclamos extends JDialog {

    private final GestorSistema gestor;
    private DefaultTableModel modeloSanciones, modeloReclamos;
    private JTable tablaSanciones, tablaReclamos;

    private JComboBox<Partido> cmbPartidoReclamo;
    private JComboBox<Equipo> cmbEquipoReclamo;
    private JTextField txtMotivoReclamo;

    private JTextField txtDictamen;

    private JComboBox<Jugador> cmbJugadorSancion;
    private JComboBox<String> cmbTipoSancion;
    private JTextField txtMotivoSancion;
    private JTextField txtDiasSancion;

    public VentanaSancionesReclamos(Frame parent) {
        super(parent, "Tribunal de Disciplina: Sanciones y Reclamos", true);
        this.gestor = GestorSistema.getInstancia();
        setSize(1020, 640);
        setMinimumSize(new Dimension(850, 520));
        setLocationRelativeTo(parent);
        setResizable(true);
        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
        cargarDatos();
    }

    private void inicializarComponentes() {
        String rol = gestor.getRolActual();

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UtilidadesImagen.VERDE_OSCURO);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));
        JLabel lblTit = new JLabel("⚖️ RECLAMOS DE PARTIDOS Y SANCIONES DISCIPLINARIAS [" + rol + "]");
        lblTit.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTit.setForeground(Color.WHITE);
        header.add(lblTit, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JTabbedPane pestañas = new JTabbedPane();
        pestañas.setFont(new Font("SansSerif", Font.BOLD, 13));

        if ("CAPITAN".equals(rol)) {
            pestañas.addTab("️ Mis Reclamos de Partidos", crearPanelReclamos(rol));
            pestañas.addTab("Consulta de Jugadores Sancionados", crearPanelSanciones(rol));
        } else {
            pestañas.addTab("Sanciones a Jugadores", crearPanelSanciones(rol));
            pestañas.addTab("Reclamos de Capitanes y Resoluciones", crearPanelReclamos(rol));
        }

        add(pestañas, BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        JButton btnCerrar = UtilidadesImagen.crearBotonEstilizado("Cerrar", new Color(90, 95, 100), Color.WHITE);
        btnCerrar.addActionListener(e -> dispose());
        panelSur.add(btnCerrar);
        add(panelSur, BorderLayout.SOUTH);
    }

    private JPanel crearPanelReclamos(String rol) {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(12, 15, 12, 15));

        modeloReclamos = new DefaultTableModel(new Object[]{
            "ID", "Fecha", "Partido Reclamado", "Capitán", "Motivo / Fundamento", "Estado", "Resolución / Dictamen"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaReclamos = new JTable(modeloReclamos);
        tablaReclamos.setRowHeight(26);
        p.add(new JScrollPane(tablaReclamos), BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new GridLayout(0, 1, 8, 8));

        // Panel para presentar reclamo (Capitán o Admin)
        if ("CAPITAN".equals(rol) || "ADMINISTRADOR".equals(rol)) {
            JPanel formCap = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            formCap.setBorder(BorderFactory.createTitledBorder("Presentar Nuevo Reclamo sobre Partido Finalizado"));

            formCap.add(new JLabel("Mi Equipo:"));
            cmbEquipoReclamo = new JComboBox<>();
            formCap.add(cmbEquipoReclamo);

            formCap.add(new JLabel("Partido Finalizado:"));
            cmbPartidoReclamo = new JComboBox<>();
            formCap.add(cmbPartidoReclamo);

            formCap.add(new JLabel("Motivo:"));
            txtMotivoReclamo = new JTextField("Mala inclusión de jugador sancionado", 22);
            formCap.add(txtMotivoReclamo);

            JButton btnEnviarReclamo = UtilidadesImagen.crearBotonEstilizado("📨 Enviar Reclamo", UtilidadesImagen.VERDE_CANCHA, Color.WHITE);
            btnEnviarReclamo.addActionListener(e -> {
                Equipo eq = (Equipo) cmbEquipoReclamo.getSelectedItem();
                Partido part = (Partido) cmbPartidoReclamo.getSelectedItem();
                Capitan cap = (gestor.getJugadorActual() instanceof Capitan) ? (Capitan) gestor.getJugadorActual() : (eq != null ? eq.getCapitan() : null);
                String rta = gestor.presentarReclamoCapitan(cap, part, txtMotivoReclamo.getText().trim(), eq);
                JOptionPane.showMessageDialog(this, rta);
                cargarDatos();
            });
            formCap.add(btnEnviarReclamo);
            panelSur.add(formCap);
        }

        // Panel para examinar y resolver reclamo (Organizador o Admin)
        if ("ORGANIZADOR".equals(rol) || "ADMINISTRADOR".equals(rol)) {
            JPanel formOrg = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            formOrg.setBorder(BorderFactory.createTitledBorder("Dictaminar Reclamo Seleccionado (Organizador)"));

            JButton btnExaminar = UtilidadesImagen.crearBotonEstilizado("🔍 Pasar a 'En Revision'", UtilidadesImagen.AZUL_ACCION, Color.WHITE);
            btnExaminar.addActionListener(e -> {
                Reclamo r = obtenerReclamoSeleccionado();
                if (r == null) return;
                JOptionPane.showMessageDialog(this, gestor.examinarReclamo(r));
                cargarDatos();
            });
            formOrg.add(btnExaminar);

            formOrg.add(new JLabel("Dictamen Oficial:"));
            txtDictamen = new JTextField("Se verificó planilla oficial y corresponde dar lugar.", 24);
            formOrg.add(txtDictamen);

            JButton btnAceptar = UtilidadesImagen.crearBotonEstilizado("Aceptar Reclamo", UtilidadesImagen.VERDE_CANCHA, Color.WHITE);
            btnAceptar.addActionListener(e -> {
                Reclamo r = obtenerReclamoSeleccionado();
                if (r == null) return;
                JOptionPane.showMessageDialog(this, gestor.resolverReclamo(r, true, txtDictamen.getText().trim()));
                cargarDatos();
            });
            formOrg.add(btnAceptar);

            JButton btnRechazar = UtilidadesImagen.crearBotonEstilizado("Rechazar Reclamo", new Color(185, 28, 28), Color.WHITE);
            btnRechazar.addActionListener(e -> {
                Reclamo r = obtenerReclamoSeleccionado();
                if (r == null) return;
                JOptionPane.showMessageDialog(this, gestor.resolverReclamo(r, false, txtDictamen.getText().trim()));
                cargarDatos();
            });
            formOrg.add(btnRechazar);

            panelSur.add(formOrg);
        }

        p.add(panelSur, BorderLayout.SOUTH);
        return p;
    }

    private JPanel crearPanelSanciones(String rol) {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(12, 15, 12, 15));

        modeloSanciones = new DefaultTableModel(new Object[]{
            "Jugador", "DNI", "Estado Actual", "Tipo de Sanción", "Motivo", "Vigente Hoy"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaSanciones = new JTable(modeloSanciones);
        tablaSanciones.setRowHeight(26);
        p.add(new JScrollPane(tablaSanciones), BorderLayout.CENTER);

        // Árbitro también puede aplicar sanción (Requisito 6)
        if ("ORGANIZADOR".equals(rol) || "ADMINISTRADOR".equals(rol) || "ARBITRO".equals(rol)) {
            JPanel formSancion = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            formSancion.setBorder(BorderFactory.createTitledBorder("Aplicar Nueva Sanción Disciplinaria (" + rol + ")"));

            formSancion.add(new JLabel("Jugador:"));
            cmbJugadorSancion = new JComboBox<>();
            formSancion.add(cmbJugadorSancion);

            formSancion.add(new JLabel("Tipo:"));
            cmbTipoSancion = new JComboBox<>(new String[]{"Suspension", "Multa", "Inhabilitacion", "Amonestacion"});
            formSancion.add(cmbTipoSancion);

            formSancion.add(new JLabel("Duración (Días):"));
            txtDiasSancion = new JTextField("7", 4);
            formSancion.add(txtDiasSancion);

            formSancion.add(new JLabel("Motivo / Detalle:"));
            txtMotivoSancion = new JTextField("Conducta antideportiva", 18);
            formSancion.add(txtMotivoSancion);

            JButton btnSancionar = UtilidadesImagen.crearBotonEstilizado("Aplicar Sanción Disciplinaria", new Color(185, 28, 28), Color.WHITE);
            btnSancionar.addActionListener(e -> {
                if (cmbJugadorSancion == null || cmbJugadorSancion.getSelectedItem() == null) {
                    JOptionPane.showMessageDialog(this,
                            "Error: Debés seleccionar obligatoriamente un jugador de la lista antes de aplicar una sanción.",
                            "Jugador no seleccionado", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Jugador j = (Jugador) cmbJugadorSancion.getSelectedItem();
                int dias = 7;
                try {
                    dias = Integer.parseInt(txtDiasSancion.getText().trim());
                } catch (Exception ex) {
                    dias = 7;
                }
                Calendar cal = Calendar.getInstance();
                Date inicio = cal.getTime();
                cal.add(Calendar.DAY_OF_YEAR, dias);
                Date fin = cal.getTime();
                String rta = gestor.aplicarSancionJugador(j, (String) cmbTipoSancion.getSelectedItem(), txtMotivoSancion.getText().trim(), inicio, fin);
                JOptionPane.showMessageDialog(this, rta);
                cargarDatos();
            });
            formSancion.add(btnSancionar);

            JScrollPane scroll = new JScrollPane(formSancion);
            scroll.setBorder(null);
            p.add(scroll, BorderLayout.SOUTH);
        }
        return p;
    }

    private Reclamo obtenerReclamoSeleccionado() {
        int f = tablaReclamos.getSelectedRow();
        if (f >= 0 && f < gestor.getReclamos().size()) {
            return gestor.getReclamos().get(f);
        }
        JOptionPane.showMessageDialog(this, "Seleccioná un reclamo de la tabla.");
        return null;
    }

    private void cargarDatos() {
        String rol = gestor.getRolActual();
        Jugador yo = gestor.getJugadorActual();

        if (modeloReclamos != null) {
            modeloReclamos.setRowCount(0);
            for (Reclamo r : gestor.getReclamos()) {
                String part = r.getPartido() != null ? r.getPartido().toString() : "-";
                String cap = r.getCapitan() != null ? r.getCapitan().getNombre() : "-";
                String res = r.getMotivoResolucion() != null ? r.getMotivoResolucion() : "Pendiente de dictamen";
                modeloReclamos.addRow(new Object[]{
                    r.getId(), r.getFecha(), part, cap, r.getFundamento(), r.getEstado(), res
                });
            }
        }

        if (modeloSanciones != null) {
            modeloSanciones.setRowCount(0);
            boolean soloMias = "JUGADOR".equals(rol);

            for (Jugador j : gestor.getJugadores()) {
                if (soloMias && yo != null && !j.equals(yo) && j.getDni() != yo.getDni()) {
                    continue; // Requisitos 19 & 20: solo datos del jugador
                }
                if (j.getSanciones() != null && !j.getSanciones().isEmpty()) {
                    for (Sancion s : j.getSanciones()) {
                        modeloSanciones.addRow(new Object[]{
                            j.getNombre(), j.getDni(), j.getEstado(), s.getTipo(), s.getMotivo(),
                            s.verificarVigencia() ? "Sí (Vigente)" : "No (Cumplida)"
                        });
                    }
                }
            }
        }

        if (cmbEquipoReclamo != null) {
            cmbEquipoReclamo.removeAllItems();
            Equipo miEq = gestor.buscarEquipoDeJugador(gestor.getJugadorActual());
            if (miEq != null) {
                cmbEquipoReclamo.addItem(miEq);
            } else {
                for (Equipo e : gestor.getEquipos()) cmbEquipoReclamo.addItem(e);
            }
        }

        if (cmbPartidoReclamo != null) {
            cmbPartidoReclamo.removeAllItems();
            for (Partido p : gestor.getPartidos()) {
                cmbPartidoReclamo.addItem(p);
            }
        }

        if (cmbJugadorSancion != null) {
            cmbJugadorSancion.removeAllItems();
            for (Jugador j : gestor.getJugadores()) cmbJugadorSancion.addItem(j);
        }
    }
}
