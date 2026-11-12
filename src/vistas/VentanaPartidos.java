package vistas;

import controlador.GestorSistema;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import modelo.*;

public class VentanaPartidos extends JDialog {

    private final GestorSistema gestor;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private DefaultTableModel modeloPartidos;
    private JTable tablaPartidos;

    private JComboBox<String> cmbFiltroEstado;

    private JComboBox<Equipo> cmbLocal, cmbVisitante;
    private JComboBox<Cancha> cmbCancha;
    private JComboBox<Arbitro> cmbArbitro;
    private JComboBox<String> cmbFase;
    private JTextField txtFechaProg, txtHora;

    private JTextField txtGolesLocal, txtGolesVisitante, txtMinutoInc, txtDescInc;
    private JComboBox<String> cmbTipoInc;
    private JComboBox<Jugador> cmbJugadorInc;
    private DefaultListModel<Incidente> modeloIncidentesTemp;
    private List<Incidente> listaIncidentesTemp = new ArrayList<>();

    public VentanaPartidos(Frame parent) {
        super(parent, "Fixture, Programación de Partidos y Resultados", true);
        this.gestor = GestorSistema.getInstancia();
        setSize(1120, 720);
        setMinimumSize(new Dimension(900, 560));
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
        JLabel lblTit = new JLabel("⚽ FIXTURE DE PARTIDOS, RESULTADOS E INCIDENTES [" + rol + "]");
        lblTit.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTit.setForeground(Color.WHITE);
        header.add(lblTit, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel barraFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        barraFiltros.setBackground(Color.WHITE);
        barraFiltros.add(new JLabel("Filtrar Fixture por Estado:"));

        String[] opcionesFiltro;
        if ("ARBITRO".equals(rol)) {
            opcionesFiltro = new String[]{"Todos los Partidos", "Pendientes", "Finalizados", "Postergados", "Suspendidos", "Mis Partidos Asignados"};
        } else if ("JUGADOR".equals(rol) || "CAPITAN".equals(rol)) {
            opcionesFiltro = new String[]{"Partidos de Mi Equipo", "Todos los Partidos", "Pendientes", "Finalizados", "Postergados", "Suspendidos"};
        } else {
            opcionesFiltro = new String[]{"Todos los Partidos", "Pendientes", "Finalizados", "Postergados", "Suspendidos"};
        }
        cmbFiltroEstado = new JComboBox<>(opcionesFiltro);
        cmbFiltroEstado.addActionListener(e -> cargarDatos());
        barraFiltros.add(cmbFiltroEstado);

        modeloPartidos = new DefaultTableModel(new Object[]{
            "ID", "Torneo", "Fase", "Fecha", "Hora", "Equipo Local", "Resultado", "Equipo Visitante", "Cancha", "Árbitro", "Estado", "Incidentes"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaPartidos = new JTable(modeloPartidos);
        tablaPartidos.setRowHeight(26);
        tablaPartidos.getSelectionModel().addListSelectionListener(e -> actualizarComboJugadoresPartido());

        JPanel panelTabla = new JPanel(new BorderLayout(6, 6));
        panelTabla.add(barraFiltros, BorderLayout.NORTH);
        panelTabla.add(new JScrollPane(tablaPartidos), BorderLayout.CENTER);

        JPanel panelCentro = new JPanel(new BorderLayout(8, 8));
        panelCentro.setBorder(new EmptyBorder(8, 14, 8, 14));
        panelCentro.add(panelTabla, BorderLayout.CENTER);

        // Pestañas inferiores según Rol
        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(0, 310));

        if ("ORGANIZADOR".equals(rol) || "ADMINISTRADOR".equals(rol)) {
            tabs.addTab("Programar / Postergar / Suspender / Reprogramar Partido", crearPanelProgramar());
        }
        if ("ARBITRO".equals(rol) || "ADMINISTRADOR".equals(rol)) {
            tabs.addTab(" Cargar Resultado y Registrar Incidentes (Árbitro)", crearPanelArbitro());
        }

        if (tabs.getTabCount() > 0) {
            panelCentro.add(tabs, BorderLayout.SOUTH);
        }

        add(panelCentro, BorderLayout.CENTER);

        // Barra inferior: Botón directo para Ver Acta de Incidentes (Requisito 16)
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));

        JButton btnVerActa = UtilidadesImagen.crearBotonEstilizado(" Ver Acta de Incidentes del Partido", UtilidadesImagen.AZUL_ACCION, Color.WHITE);
        btnVerActa.addActionListener(e -> mostrarActaIncidentesDirecta());
        panelSur.add(btnVerActa);

        if ("CAPITAN".equals(rol)) {
            JButton btnIrReclamo = UtilidadesImagen.crearBotonEstilizado("️ Presentar Reclamo de Partido", new Color(185, 28, 28), Color.WHITE);
            btnIrReclamo.addActionListener(e -> new VentanaSancionesReclamos((Frame) getParent()).setVisible(true));
            panelSur.add(btnIrReclamo);
        }

        JButton btnCerrar = UtilidadesImagen.crearBotonEstilizado("Cerrar", new Color(90, 95, 100), Color.WHITE);
        btnCerrar.addActionListener(e -> dispose());
        panelSur.add(btnCerrar);

        add(panelSur, BorderLayout.SOUTH);
    }

    private JPanel crearPanelProgramar() {
        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(new EmptyBorder(10, 14, 10, 14));
        principal.setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 8, 5, 8);


        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.15;
        form.add(new JLabel("Equipo Local:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.35;
        cmbLocal = new JComboBox<>();
        form.add(cmbLocal, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        form.add(new JLabel("Equipo Visitante:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.35;
        cmbVisitante = new JComboBox<>();
        form.add(cmbVisitante, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.15;
        form.add(new JLabel("Cancha:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.35;
        cmbCancha = new JComboBox<>();
        form.add(cmbCancha, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        form.add(new JLabel("Fase del Torneo:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.35;
        cmbFase = new JComboBox<>(new String[]{"Fase de Grupos", "Octavos de Final", "Cuartos de Final", "Semifinal", "Final"});
        form.add(cmbFase, gbc);


        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.15;
        form.add(new JLabel("Fecha (dd/MM/YYYY):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.35;
        txtFechaProg = new JTextField(sdf.format(new Date()));
        form.add(txtFechaProg, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        form.add(new JLabel("Horario (HH:mm):"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.35;
        txtHora = new JTextField("21:00");
        form.add(txtHora, gbc);


        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.15;
        form.add(new JLabel("Árbitro Designado:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.35;
        cmbArbitro = new JComboBox<>();
        form.add(cmbArbitro, gbc);

        gbc.gridx = 2; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 0.50;
        JButton btnAutoArbitro = UtilidadesImagen.crearBotonEstilizado(" Asignar Árbitro Disponible Automáticamente", UtilidadesImagen.AZUL_ACCION, Color.WHITE);
        btnAutoArbitro.addActionListener(e -> {
            Date fProg;
            try {
                fProg = sdf.parse(txtFechaProg.getText().trim());
            } catch (Exception ex) {
                fProg = new Date();
            }
            String hora = txtHora.getText().trim();
            Arbitro arb = gestor.buscarArbitroDisponible(fProg, hora);
            if (arb != null) {
                cmbArbitro.setSelectedItem(arb);
                JOptionPane.showMessageDialog(this, "Se asignó automáticamente el árbitro disponible:\n" + arb.getNombre(), "Árbitro Asignado", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró ningún árbitro libre para la fecha " + sdf.format(fProg) + " a las " + hora + " hs.", "Sin Disponibilidad", JOptionPane.WARNING_MESSAGE);
            }
        });
        form.add(btnAutoArbitro, gbc);

        principal.add(form, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        panelBotones.setOpaque(false);

        JButton btnProgramar = UtilidadesImagen.crearBotonEstilizado(" Programar Partido", UtilidadesImagen.VERDE_CANCHA, Color.WHITE);
        btnProgramar.addActionListener(e -> {
            Date fProg;
            try {
                fProg = sdf.parse(txtFechaProg.getText().trim());
            } catch (Exception ex) {
                fProg = new Date();
            }
            String rta = gestor.programarPartido(
                    (Equipo) cmbLocal.getSelectedItem(),
                    (Equipo) cmbVisitante.getSelectedItem(),
                    fProg,
                    txtHora.getText().trim(),
                    (String) cmbFase.getSelectedItem(),
                    (Cancha) cmbCancha.getSelectedItem(),
                    (Arbitro) cmbArbitro.getSelectedItem()
            );
            JOptionPane.showMessageDialog(this, rta);
            cargarDatos();
        });
        panelBotones.add(btnProgramar);

        JButton btnPostergar = UtilidadesImagen.crearBotonEstilizado("️ Postergar con Motivo", new Color(180, 120, 15), Color.WHITE);
        btnPostergar.addActionListener(e -> {
            Partido sel = obtenerPartidoSeleccionado();
            if (sel == null) return;
            String mot = JOptionPane.showInputDialog(this, "Ingresá el motivo de postergación:", "Lluvia intensa");
            if (mot != null && !mot.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, gestor.postergarPartido(sel, mot.trim()));
                cargarDatos();
            }
        });
        panelBotones.add(btnPostergar);

        JButton btnSuspender = UtilidadesImagen.crearBotonEstilizado(" Suspender con Motivo", new Color(185, 28, 28), Color.WHITE);
        btnSuspender.addActionListener(e -> {
            Partido sel = obtenerPartidoSeleccionado();
            if (sel == null) return;
            String mot = JOptionPane.showInputDialog(this, "Ingresá el motivo de suspensión:", "Falta de garantías de seguridad");
            if (mot != null && !mot.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, gestor.suspenderPartido(sel, mot.trim()));
                cargarDatos();
            }
        });
        panelBotones.add(btnSuspender);

        JButton btnReprogramar = UtilidadesImagen.crearBotonEstilizado(" Reprogramar Seleccionado", UtilidadesImagen.AZUL_ACCION, Color.WHITE);
        btnReprogramar.addActionListener(e -> {
            Partido sel = obtenerPartidoSeleccionado();
            if (sel == null) return;
            Date nuevaFecha;
            try {
                nuevaFecha = sdf.parse(txtFechaProg.getText().trim());
            } catch (Exception ex) {
                nuevaFecha = new Date();
            }
            String rta = gestor.reprogramarPartido(sel, nuevaFecha, txtHora.getText().trim(), (Cancha) cmbCancha.getSelectedItem());
            JOptionPane.showMessageDialog(this, rta);
            cargarDatos();
        });
        panelBotones.add(btnReprogramar);

        principal.add(panelBotones, BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(principal);
        scroll.setBorder(null);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel crearPanelArbitro() {
        JPanel p = new JPanel(new BorderLayout(10, 8));
        p.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel formIzq = new JPanel(new GridLayout(4, 4, 8, 6));
        formIzq.add(new JLabel("Goles Local:"));
        txtGolesLocal = new JTextField("1");
        formIzq.add(txtGolesLocal);

        formIzq.add(new JLabel("Goles Visitante:"));
        txtGolesVisitante = new JTextField("0");
        formIzq.add(txtGolesVisitante);

        formIzq.add(new JLabel("Tipo de Incidente:"));
        cmbTipoInc = new JComboBox<>(new String[]{"Gol", "Tarjeta Amarilla", "Tarjeta Roja"});
        formIzq.add(cmbTipoInc);

        formIzq.add(new JLabel("Minuto (0-120):"));
        txtMinutoInc = new JTextField("45");
        formIzq.add(txtMinutoInc);

        formIzq.add(new JLabel("Jugador Involucrado:"));
        cmbJugadorInc = new JComboBox<>();
        formIzq.add(cmbJugadorInc);

        formIzq.add(new JLabel("Descripción:"));
        txtDescInc = new JTextField("Jugada oficial");
        formIzq.add(txtDescInc);

        JButton btnSumarInc = UtilidadesImagen.crearBotonEstilizado(" Añadir Incidente al Acta", UtilidadesImagen.AZUL_ACCION, Color.WHITE);
        btnSumarInc.addActionListener(e -> {
            try {
                Jugador j = (Jugador) cmbJugadorInc.getSelectedItem();
                int min = Integer.parseInt(txtMinutoInc.getText().trim());
                String tipo = (String) cmbTipoInc.getSelectedItem();
                Incidente inc = new Incidente(0, tipo, min, txtDescInc.getText().trim(), j);
                listaIncidentesTemp.add(inc);
                modeloIncidentesTemp.addElement(inc);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingresá un minuto numérico válido.");
            }
        });
        formIzq.add(btnSumarInc);

        JButton btnGuardarResultado = UtilidadesImagen.crearBotonEstilizado("Confirmar Resultado Final", UtilidadesImagen.VERDE_CANCHA, Color.WHITE);
        btnGuardarResultado.addActionListener(e -> {
            Partido sel = obtenerPartidoSeleccionado();
            if (sel == null) return;
            try {
                int gl = Integer.parseInt(txtGolesLocal.getText().trim());
                int gv = Integer.parseInt(txtGolesVisitante.getText().trim());
                String rta = gestor.cargarResultadoPartido(sel, gl, gv, new ArrayList<>(listaIncidentesTemp));
                JOptionPane.showMessageDialog(this, rta);
                listaIncidentesTemp.clear();
                modeloIncidentesTemp.clear();
                cargarDatos();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingresá goles numéricos válidos.");
            }
        });
        formIzq.add(btnGuardarResultado);

        p.add(formIzq, BorderLayout.CENTER);

        modeloIncidentesTemp = new DefaultListModel<>();
        JList<Incidente> listaVisual = new JList<>(modeloIncidentesTemp);
        JScrollPane scrollInc = new JScrollPane(listaVisual);
        scrollInc.setBorder(BorderFactory.createTitledBorder("Incidentes a registrar en este partido"));
        scrollInc.setPreferredSize(new Dimension(300, 0));
        p.add(scrollInc, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(p);
        scroll.setBorder(null);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    // Acta completa directa (Requisito 16: elimina pantalla intermedia innecesaria)
    private void mostrarActaIncidentesDirecta() {
        Partido sel = obtenerPartidoSeleccionado();
        if (sel == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("           ACTA OFICIAL DE PARTIDO — UGD\n");
        sb.append("═══════════════════════════════════════════════════════════\n\n");
        sb.append("Partido: ").append(sel.toString()).append("\n");
        String fStr = (sel.getFecha() != null) ? sdf.format(sel.getFecha()) : "-";
        sb.append("Fecha: ").append(fStr).append(" | Hora: ").append(sel.getHora()).append("\n");
        sb.append("Cancha: ").append(sel.getCancha() != null ? sel.getCancha().getNombre() : "-").append("\n");
        sb.append("Árbitro: ").append(sel.getArbitro() != null ? sel.getArbitro().getNombre() : "-").append("\n");
        sb.append("Estado actual: ").append(sel.getEstado()).append("\n");
        if (sel.getMotivoPostergacion() != null && !sel.getMotivoPostergacion().isEmpty()) {
            sb.append("Motivo postergación: ").append(sel.getMotivoPostergacion()).append("\n");
        }
        sb.append("\n───────────────────────────────────────────────────────────\n");
        sb.append("INCIDENTES REGISTRADOS:\n");
        sb.append("───────────────────────────────────────────────────────────\n");
        if (sel.getIncidentes() == null || sel.getIncidentes().isEmpty()) {
            sb.append("  (No hay incidentes registrados en este partido)\n");
        } else {
            for (Incidente inc : sel.getIncidentes()) {
                sb.append("  • ").append(inc.toString()).append("\n");
            }
        }
        sb.append("═══════════════════════════════════════════════════════════\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Acta de Incidentes de Partido", JOptionPane.INFORMATION_MESSAGE);
    }

    private Partido obtenerPartidoSeleccionado() {
        int f = tablaPartidos.getSelectedRow();
        if (f >= 0) {
            int idPartido = (int) modeloPartidos.getValueAt(f, 0);
            for (Partido p : gestor.getPartidos()) {
                if (p.getId() == idPartido) return p;
            }
        }
        if (!gestor.getPartidos().isEmpty()) {
            return gestor.getPartidos().get(0);
        }
        JOptionPane.showMessageDialog(this, "Seleccioná primero un partido en la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
        return null;
    }

    private void actualizarComboJugadoresPartido() {
        if (cmbJugadorInc == null) return;
        cmbJugadorInc.removeAllItems();
        int f = tablaPartidos.getSelectedRow();
        if (f >= 0) {
            int idPartido = (int) modeloPartidos.getValueAt(f, 0);
            Partido pSel = null;
            for (Partido p : gestor.getPartidos()) {
                if (p.getId() == idPartido) {
                    pSel = p;
                    break;
                }
            }
            if (pSel != null) {
                if (pSel.getEquipoLocal() != null && pSel.getEquipoLocal().getPlantel() != null) {
                    for (Jugador j : pSel.getEquipoLocal().getPlantel()) cmbJugadorInc.addItem(j);
                }
                if (pSel.getEquipoVisitante() != null && pSel.getEquipoVisitante().getPlantel() != null) {
                    for (Jugador j : pSel.getEquipoVisitante().getPlantel()) cmbJugadorInc.addItem(j);
                }
            }
        }
        if (cmbJugadorInc.getItemCount() == 0) {
            for (Jugador j : gestor.getJugadores()) cmbJugadorInc.addItem(j);
        }
    }

    private void cargarDatos() {
        modeloPartidos.setRowCount(0);
        String filtro = (cmbFiltroEstado != null) ? (String) cmbFiltroEstado.getSelectedItem() : "Todos los Partidos";
        String rol = gestor.getRolActual();
        Usuario usr = gestor.getUsuarioActual();
        Jugador jug = gestor.getJugadorActual();
        Equipo miEquipo = gestor.buscarEquipoDeJugador(jug);

        for (Partido p : gestor.getPartidos()) {
            // Aplicar filtros según estado y rol
            if ("Pendientes".equalsIgnoreCase(filtro) && !"Pendiente".equalsIgnoreCase(p.getEstado())) {
                continue;
            }
            if ("Finalizados".equalsIgnoreCase(filtro) && !"Finalizado".equalsIgnoreCase(p.getEstado())) {
                continue;
            }
            if ("Postergados".equalsIgnoreCase(filtro) && !"Postergado".equalsIgnoreCase(p.getEstado())) {
                continue;
            }
            if ("Suspendidos".equalsIgnoreCase(filtro) && !"Suspendido".equalsIgnoreCase(p.getEstado())) {
                continue;
            }
            if ("Mis Partidos Asignados".equalsIgnoreCase(filtro)) {
                if (usr instanceof Arbitro && !usr.equals(p.getArbitro())) {
                    continue;
                }
            }
            if ("Partidos de Mi Equipo".equalsIgnoreCase(filtro) && miEquipo != null) {
                if (!miEquipo.equals(p.getEquipoLocal()) && !miEquipo.equals(p.getEquipoVisitante())) {
                    continue;
                }
            }

            String loc = p.getEquipoLocal() != null ? p.getEquipoLocal().getNombre() : "-";
            String vis = p.getEquipoVisitante() != null ? p.getEquipoVisitante().getNombre() : "-";
            String res = p.getResultado() != null ? p.getResultado() : "vs";
            String can = p.getCancha() != null ? p.getCancha().getNombre() : "-";
            String arb = p.getArbitro() != null ? p.getArbitro().getNombre() : "-";
            String fechaStr = p.getFecha() != null ? sdf.format(p.getFecha()) : "-";
            int cantInc = p.getIncidentes() != null ? p.getIncidentes().size() : 0;
            String torneoNom = gestor.buscarNombreTorneoDePartido(p);

            modeloPartidos.addRow(new Object[]{
                p.getId(), torneoNom, p.getFase(), fechaStr, p.getHora(), loc, res, vis, can, arb, p.getEstado(), cantInc + " inc."
            });
        }

        if (cmbLocal != null) {
            cmbLocal.removeAllItems();
            cmbVisitante.removeAllItems();
            for (Equipo e : gestor.getEquipos()) {
                cmbLocal.addItem(e);
                cmbVisitante.addItem(e);
            }
            if (cmbVisitante.getItemCount() > 1) cmbVisitante.setSelectedIndex(1);

            cmbCancha.removeAllItems();
            for (Cancha c : gestor.getCanchas()) cmbCancha.addItem(c);

            cmbArbitro.removeAllItems();
            for (Arbitro a : gestor.getArbitros()) cmbArbitro.addItem(a);
        }
        actualizarComboJugadoresPartido();
    }
}
