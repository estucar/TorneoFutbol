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
import modelo.Equipo;
import modelo.Partido;
import modelo.Torneo;

public class VentanaTorneos extends JDialog {

    private final GestorSistema gestor;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");


    private DefaultTableModel modeloTorneos;
    private JTable tablaTorneos;
    private JLabel lblEstadoTorneoSel;

    private JTextField txtNuevoNombre;
    private JTextField txtNuevaFecha;
    private DefaultTableModel modeloSeleccionEquipos;
    private JTable tablaSeleccionEquipos;
    private final List<Equipo> equiposHabilitadosParaTorneo = new ArrayList<>();

    private JComboBox<Torneo> cmbTorneoEquipos;
    private DefaultTableModel modeloEquiposTorneo;
    private JTable tablaEquiposTorneo;
    private JComboBox<Equipo> cmbEquiposDisponiblesAgregar;

    public VentanaTorneos(Frame parent) {
        super(parent, "Gestión de Torneos, Sorteo Automático y Equipos", true);
        this.gestor = GestorSistema.getInstancia();
        setSize(1060, 680);
        setMinimumSize(new Dimension(880, 550));
        setLocationRelativeTo(parent);
        setResizable(true);
        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
        cargarDatos();
    }

    private void inicializarComponentes() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UtilidadesImagen.VERDE_OSCURO);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel lblTit = new JLabel("🏆 GESTIÓN DE TORNEOS, FASES AUTOMÁTICAS Y EQUIPOS");
        lblTit.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTit.setForeground(Color.WHITE);
        header.add(lblTit, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JTabbedPane pestañas = new JTabbedPane();
        pestañas.setFont(new Font("SansSerif", Font.BOLD, 13));

        pestañas.addTab("🏆 Torneos Creados y Avance de Fases", crearPanelTorneosActivos());
        pestañas.addTab("➕ Crear Nuevo Torneo (Paso a Paso)", crearPanelCrearTorneo(pestañas));
        pestañas.addTab("🛡️ Administrar Equipos del Torneo (Altas y Bajas)", crearPanelGestionarEquiposTorneo());

        add(pestañas, BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton btnCerrar = UtilidadesImagen.crearBotonEstilizado("Cerrar", new Color(90, 95, 100), Color.WHITE);
        btnCerrar.addActionListener(e -> dispose());
        panelSur.add(btnCerrar);
        add(panelSur, BorderLayout.SOUTH);
    }

    private JPanel crearPanelTorneosActivos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        modeloTorneos = new DefaultTableModel(new Object[]{"ID", "Nombre del Torneo", "Fecha Inicio", "Fase Actual", "Equipos", "Campeón"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaTorneos = new JTable(modeloTorneos);
        tablaTorneos.setRowHeight(28);
        tablaTorneos.getSelectionModel().addListSelectionListener(e -> actualizarDetalleTorneoSeleccionado());
        panel.add(new JScrollPane(tablaTorneos), BorderLayout.CENTER);


        JPanel panelAcciones = new JPanel(new GridLayout(0, 1, 8, 8));
        panelAcciones.setBorder(BorderFactory.createTitledBorder("Acciones sobre el Torneo Seleccionado"));

        lblEstadoTorneoSel = new JLabel("Seleccioná un torneo de la lista para gestionar sus fases.");
        lblEstadoTorneoSel.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblEstadoTorneoSel.setForeground(UtilidadesImagen.VERDE_OSCURO);
        panelAcciones.add(lblEstadoTorneoSel);

        JPanel pAvanzar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnAvanzar = UtilidadesImagen.crearBotonEstilizado("⏩ Avanzar Fase Automáticamente (Grupos -> Octavos -> Cuartos -> Semis -> Final)", UtilidadesImagen.AZUL_ACCION, Color.WHITE);
        btnAvanzar.addActionListener(e -> {
            Torneo t = obtenerTorneoSeleccionado();
            if (t == null) return;
            String rta = gestor.avanzarFaseTorneo(t);
            JOptionPane.showMessageDialog(this, rta, "Avance de Fase", JOptionPane.INFORMATION_MESSAGE);
            cargarDatos();
        });
        pAvanzar.add(btnAvanzar);
        panelAcciones.add(pAvanzar);

        JPanel pInfoAuto = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel lblInfoAuto = new JLabel("ℹ️ Sistema Automático: Los grupos y fixture se sortearon al crear el torneo. Al finalizar el partido de la Final, el Campeón se consagrará automáticamente.");
        lblInfoAuto.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblInfoAuto.setForeground(new Color(40, 80, 60));
        pInfoAuto.add(lblInfoAuto);
        panelAcciones.add(pInfoAuto);

        panel.add(panelAcciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelCrearTorneo(JTabbedPane pestañas) {
        JPanel contenedor = new JPanel(new BorderLayout(14, 14));
        contenedor.setBorder(new EmptyBorder(16, 22, 16, 22));

        JPanel panelDatos = new JPanel(new GridLayout(2, 2, 12, 10));
        panelDatos.setBorder(BorderFactory.createTitledBorder("Paso 1: Datos Generales del Torneo"));

        panelDatos.add(new JLabel("Nombre del Torneo:"));
        txtNuevoNombre = new JTextField("Torneo Clausura UGD 2026");
        panelDatos.add(txtNuevoNombre);

        panelDatos.add(new JLabel("Fecha de Inicio (dd/MM/yyyy):"));
        txtNuevaFecha = new JTextField(sdf.format(new Date()));
        panelDatos.add(txtNuevaFecha);

        contenedor.add(panelDatos, BorderLayout.NORTH);


        JPanel panelEquipos = new JPanel(new BorderLayout(6, 6));
        panelEquipos.setBorder(BorderFactory.createTitledBorder("Paso 2: Seleccioná los Equipos Involucrados en este Torneo"));

        modeloSeleccionEquipos = new DefaultTableModel(new Object[]{"Inscribir", "Equipo", "Categoría", "Sede Habitual", "Capitán"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 0;
            }
        };
        tablaSeleccionEquipos = new JTable(modeloSeleccionEquipos);
        tablaSeleccionEquipos.setRowHeight(26);
        tablaSeleccionEquipos.getColumnModel().getColumn(0).setMaxWidth(80);

        JPanel barraSel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        JButton btnTodos = new JButton("Marcar Todos");
        btnTodos.addActionListener(e -> setCheckTodos(true));
        JButton btnNinguno = new JButton("Desmarcar Todos");
        btnNinguno.addActionListener(e -> setCheckTodos(false));
        barraSel.add(btnTodos);
        barraSel.add(btnNinguno);

        panelEquipos.add(barraSel, BorderLayout.NORTH);
        panelEquipos.add(new JScrollPane(tablaSeleccionEquipos), BorderLayout.CENTER);

        contenedor.add(panelEquipos, BorderLayout.CENTER);

        JPanel panelCrear = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnCrearYsortear = UtilidadesImagen.crearBotonEstilizado("🏆 Crear Torneo y Sortear Fase de Grupos Automáticamente", UtilidadesImagen.VERDE_CANCHA, Color.WHITE);
        btnCrearYsortear.setPreferredSize(new Dimension(520, 44));
        btnCrearYsortear.setFont(new Font("SansSerif", Font.BOLD, 15));
        btnCrearYsortear.addActionListener(e -> {
            String nom = txtNuevoNombre.getText().trim();
            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingresá un nombre para el torneo.", "Campo Requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Date fInicio;
            try {
                fInicio = sdf.parse(txtNuevaFecha.getText().trim());
            } catch (Exception ex) {
                fInicio = new Date();
            }

            List<Equipo> seleccionados = new ArrayList<>();
            for (int i = 0; i < modeloSeleccionEquipos.getRowCount(); i++) {
                Boolean marcado = (Boolean) modeloSeleccionEquipos.getValueAt(i, 0);
                if (Boolean.TRUE.equals(marcado) && i < equiposHabilitadosParaTorneo.size()) {
                    seleccionados.add(equiposHabilitadosParaTorneo.get(i));
                }
            }

            if (seleccionados.size() < 2) {
                JOptionPane.showMessageDialog(this,
                        "Debés seleccionar al menos 2 equipos para poder crear el torneo y sortear el fixture.",
                        "Equipos Insuficientes", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String rta = gestor.altaTorneoPersonalizado(nom, fInicio, seleccionados);
            JOptionPane.showMessageDialog(this, rta, "Torneo Creado", JOptionPane.INFORMATION_MESSAGE);
            cargarDatos();
            pestañas.setSelectedIndex(0);
        });
        panelCrear.add(btnCrearYsortear);
        contenedor.add(panelCrear, BorderLayout.SOUTH);

        return contenedor;
    }

    private void setCheckTodos(boolean valor) {
        for (int i = 0; i < modeloSeleccionEquipos.getRowCount(); i++) {
            modeloSeleccionEquipos.setValueAt(valor, i, 0);
        }
    }

    private JPanel crearPanelGestionarEquiposTorneo() {
        JPanel contenedor = new JPanel(new BorderLayout(12, 12));
        contenedor.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel barraSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        barraSuperior.add(new JLabel("Seleccionar Torneo a Gestionar:"));
        cmbTorneoEquipos = new JComboBox<>();
        cmbTorneoEquipos.setPreferredSize(new Dimension(350, 32));
        cmbTorneoEquipos.addActionListener(e -> refrescarTablaEquiposDelTorneo());
        barraSuperior.add(cmbTorneoEquipos);
        contenedor.add(barraSuperior, BorderLayout.NORTH);

        modeloEquiposTorneo = new DefaultTableModel(new Object[]{"ID", "Equipo", "Categoría", "Sede Habitual", "Capitán", "Estado Inscripción"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaEquiposTorneo = new JTable(modeloEquiposTorneo);
        tablaEquiposTorneo.setRowHeight(26);
        contenedor.add(new JScrollPane(tablaEquiposTorneo), BorderLayout.CENTER);

        JPanel panelAcciones = new JPanel(new GridLayout(2, 1, 8, 8));
        panelAcciones.setBorder(BorderFactory.createTitledBorder("Modificar Equipos Participantes"));

        JPanel pAgregar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pAgregar.add(new JLabel("Sumar Nuevo Equipo al Torneo:"));
        cmbEquiposDisponiblesAgregar = new JComboBox<>();
        cmbEquiposDisponiblesAgregar.setPreferredSize(new Dimension(300, 30));
        pAgregar.add(cmbEquiposDisponiblesAgregar);

        JButton btnSumar = UtilidadesImagen.crearBotonEstilizado("➕ Agregar al Torneo", UtilidadesImagen.VERDE_CANCHA, Color.WHITE);
        btnSumar.addActionListener(e -> {
            Torneo t = (Torneo) cmbTorneoEquipos.getSelectedItem();
            Equipo eq = (Equipo) cmbEquiposDisponiblesAgregar.getSelectedItem();
            if (t == null || eq == null) {
                JOptionPane.showMessageDialog(this, "Seleccioná un torneo y un equipo para agregar.");
                return;
            }
            String rta = gestor.agregarEquipoATorneo(t, eq);
            JOptionPane.showMessageDialog(this, rta);
            cargarDatos();
        });
        pAgregar.add(btnSumar);
        panelAcciones.add(pAgregar);

        JPanel pQuitar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        JButton btnQuitar = UtilidadesImagen.crearBotonEstilizado("➖ Quitar Equipo Seleccionado (Dar de Baja del Torneo)", new Color(185, 28, 28), Color.WHITE);
        btnQuitar.addActionListener(e -> {
            Torneo t = (Torneo) cmbTorneoEquipos.getSelectedItem();
            int f = tablaEquiposTorneo.getSelectedRow();
            if (t == null || f < 0 || f >= t.getEquipos().size()) {
                JOptionPane.showMessageDialog(this, "Seleccioná un equipo de la tabla para darlo de baja del torneo.");
                return;
            }
            Equipo eq = t.getEquipos().get(f);
            int conf = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de que deseás dar de baja al equipo '" + eq.getNombre() + "' del torneo '" + t.getNombre() + "'?",
                    "Confirmar Baja de Equipo", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                String rta = gestor.darDeBajaEquipoDeTorneo(t, eq);
                JOptionPane.showMessageDialog(this, rta);
                cargarDatos();
            }
        });
        pQuitar.add(btnQuitar);
        panelAcciones.add(pQuitar);

        contenedor.add(panelAcciones, BorderLayout.SOUTH);
        return contenedor;
    }

    private void actualizarDetalleTorneoSeleccionado() {
        Torneo t = obtenerTorneoSeleccionado();
        if (t != null) {
            int cantEq = (t.getEquipos() != null) ? t.getEquipos().size() : 0;
            String camp = (t.getCampeon() != null) ? " | Campeón: 🏆 " + t.getCampeon().getNombre() : "";
            lblEstadoTorneoSel.setText("Torneo seleccionado: " + t.getNombre() + " (" + t.getFaseActual() + " - " + cantEq + " equipos)" + camp);
        }
    }

    private Torneo obtenerTorneoSeleccionado() {
        int f = tablaTorneos.getSelectedRow();
        if (f >= 0 && f < gestor.getTorneos().size()) {
            return gestor.getTorneos().get(f);
        }
        if (!gestor.getTorneos().isEmpty()) {
            return gestor.getTorneos().get(0);
        }
        return null;
    }

    private void refrescarTablaEquiposDelTorneo() {
        modeloEquiposTorneo.setRowCount(0);
        Torneo t = (Torneo) cmbTorneoEquipos.getSelectedItem();
        if (t == null) return;

        if (t.getEquipos() != null) {
            for (Equipo eq : t.getEquipos()) {
                String nomCap = (eq.getCapitan() != null) ? eq.getCapitan().getNombre() : "-";
                String estIns = (eq.getInscripcion() != null && eq.getInscripcion().getEstado() != null)
                        ? eq.getInscripcion().getEstado() : "Pendiente";
                modeloEquiposTorneo.addRow(new Object[]{
                    eq.getId(), eq.getNombre(), eq.getCategoria(), eq.getSedeHabitual(), nomCap, estIns
                });
            }
        }

        cmbEquiposDisponiblesAgregar.removeAllItems();
        for (Equipo eq : gestor.getEquipos()) {
            if (t.getEquipos() == null || !t.getEquipos().contains(eq)) {
                if (eq.getInscripcion() != null && "Habilitado".equalsIgnoreCase(eq.getInscripcion().getEstado())) {
                    cmbEquiposDisponiblesAgregar.addItem(eq);
                }
            }
        }
    }

    private void cargarDatos() {
        modeloTorneos.setRowCount(0);
        for (Torneo t : gestor.getTorneos()) {
            String camp = (t.getCampeon() != null) ? "🏆 " + t.getCampeon().getNombre() : "En disputa";
            int cantEq = (t.getEquipos() != null) ? t.getEquipos().size() : 0;
            String strFecha = (t.getFechaInicio() != null) ? sdf.format(t.getFechaInicio()) : "-";
            modeloTorneos.addRow(new Object[]{
                t.getId(), t.getNombre(), strFecha, t.getFaseActual(), cantEq + " equipos", camp
            });
        }

        equiposHabilitadosParaTorneo.clear();
        modeloSeleccionEquipos.setRowCount(0);
        for (Equipo eq : gestor.getEquipos()) {
            if (eq.getInscripcion() != null && "Habilitado".equalsIgnoreCase(eq.getInscripcion().getEstado())) {
                equiposHabilitadosParaTorneo.add(eq);
                String cap = (eq.getCapitan() != null) ? eq.getCapitan().getNombre() : "-";
                modeloSeleccionEquipos.addRow(new Object[]{
                    Boolean.TRUE, eq.getNombre(), eq.getCategoria(), eq.getSedeHabitual(), cap
                });
            }
        }

        cmbTorneoEquipos.removeAllItems();
        for (Torneo t : gestor.getTorneos()) {
            cmbTorneoEquipos.addItem(t);
        }
        refrescarTablaEquiposDelTorneo();
    }
}
