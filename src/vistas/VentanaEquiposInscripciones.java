package vistas;

import controlador.GestorSistema;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import modelo.*;


public class VentanaEquiposInscripciones extends JDialog {

    private final GestorSistema gestor;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    private JLabel lblEscudoEquipo;
    private JComboBox<Equipo> cmbEquipos;
    private DefaultTableModel modeloPlantel;
    private JTable tablaPlantel;
    private JLabel lblResumenEquipo;


    private JTextField txtNombreEquipo;
    private JTextField txtCategoria;
    private JTextField txtCamiseta;
    private JComboBox<Cancha> cmbSedeHabitual;
    private JLabel lblPreviewEscudoNuevo;
    private String rutaEscudoNuevoEquipo = null;

    private JTextField txtEditNombre;
    private JTextField txtEditCategoria;
    private JTextField txtEditCamiseta;
    private JComboBox<Cancha> cmbEditSedeHabitual;
    private JComboBox<Jugador> cmbEditCapitan;
    private JLabel lblPreviewEscudoEdit;
    private String rutaEscudoEdit = null;

    private JTextField txtDniJug;
    private JTextField txtNomJug;
    private JTextField txtFechaNacJug;
    private JComboBox<String> cmbPosJug;
    private JComboBox<String> cmbEstadoJug;
    private JTextField txtEmailJug;

    private JTextField txtRutaDoc;

    private DefaultTableModel modeloInscripcionesPendientes;
    private JTable tablaInscripcionesPendientes;
    private JLabel lblContadorPendientes;

    public VentanaEquiposInscripciones(Frame parent) {
        super(parent, "Gestión de Equipos, Jugadores e Inscripciones (Desktop — Organizador)", true);
        this.gestor = GestorSistema.getInstancia();
        setSize(1100, 760);
        setMinimumSize(new Dimension(920, 600));
        setLocationRelativeTo(parent);
        setResizable(true);
        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
        refrescarDatos();
    }

    private void inicializarComponentes() {
        String rol = gestor.getRolActual();

        JPanel header = new JPanel(new BorderLayout(15, 10));
        header.setBackground(UtilidadesImagen.VERDE_OSCURO);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));

        lblEscudoEquipo = new JLabel();
        lblEscudoEquipo.setPreferredSize(new Dimension(64, 64));
        lblEscudoEquipo.setVisible(false);
        header.add(lblEscudoEquipo, BorderLayout.WEST);

        JPanel panelTit = new JPanel(new GridLayout(2, 1));
        panelTit.setOpaque(false);
        JLabel lblTit = new JLabel("🛡️ GESTIÓN DE EQUIPOS Y JUGADORES (DESKTOP — ORGANIZADOR / ADMIN)");
        lblTit.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblTit.setForeground(Color.WHITE);

        lblResumenEquipo = new JLabel("Registro, edición de equipos, altas de jugadores, evaluación de inscripciones y credenciales con código de barras.");
        lblResumenEquipo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblResumenEquipo.setForeground(UtilidadesImagen.DORADO_ACENTO);
        panelTit.add(lblTit);
        panelTit.add(lblResumenEquipo);
        header.add(panelTit, BorderLayout.CENTER);

        JButton btnSubirEscudo = UtilidadesImagen.crearBotonEstilizado("🖼️ Cambiar Escudo", UtilidadesImagen.DORADO_ACENTO, Color.BLACK);
        btnSubirEscudo.addActionListener(e -> {
            Equipo eq = (Equipo) cmbEquipos.getSelectedItem();
            if (eq == null) {
                JOptionPane.showMessageDialog(this, "Seleccioná primero un equipo.");
                return;
            }
            if (UtilidadesImagen.seleccionarYGuardarEscudoEquipo(this, eq)) {
                ImageIcon esc = UtilidadesImagen.cargarEscudoEquipo(eq, 64, 64);
                if (esc != null) {
                    lblEscudoEquipo.setIcon(esc);
                    lblEscudoEquipo.setVisible(true);
                } else {
                    lblEscudoEquipo.setIcon(null);
                    lblEscudoEquipo.setVisible(false);
                }
                refrescarDatos();
            }
        });
        header.add(btnSubirEscudo, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        JPanel barraEquipo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        barraEquipo.setBackground(Color.WHITE);
        barraEquipo.add(new JLabel("Equipo Activo:"));
        cmbEquipos = new JComboBox<>();
        cmbEquipos.setPreferredSize(new Dimension(280, 32));
        cmbEquipos.addActionListener(e -> {
            actualizarTablaPlantel();
            cargarDatosEnEdicion();
        });
        barraEquipo.add(cmbEquipos);

        JButton btnImprimirCredencial = UtilidadesImagen.crearBotonEstilizado(" Imprimir Credencial", UtilidadesImagen.AZUL_ACCION, Color.WHITE);
        btnImprimirCredencial.addActionListener(e -> mostrarCredencialJugador());
        barraEquipo.add(btnImprimirCredencial);

        JButton btnModificarPerfil = UtilidadesImagen.crearBotonEstilizado("️ Modificar Jugador", new Color(40, 120, 160), Color.WHITE);
        btnModificarPerfil.addActionListener(e -> abrirModificarPerfilJugador());
        barraEquipo.add(btnModificarPerfil);

        JButton btnCambiarCapitan = UtilidadesImagen.crearBotonEstilizado(" Designar / Cambiar Capitán", new Color(180, 120, 15), Color.WHITE);
        btnCambiarCapitan.addActionListener(e -> cambiarCapitanDelEquipo());
        barraEquipo.add(btnCambiarCapitan);

        JButton btnEliminarEquipo = UtilidadesImagen.crearBotonEstilizado("️ Eliminar Equipo", new Color(185, 28, 28), Color.WHITE);
        btnEliminarEquipo.addActionListener(e -> eliminarEquipoSeleccionado());
        barraEquipo.add(btnEliminarEquipo);

        // --- PANEL CENTRAL CON TABLA DE PLANTEL Y PESTAÑAS DE GESTIÓN ---
        JPanel panelCentral = new JPanel(new BorderLayout(8, 8));
        panelCentral.setBorder(new EmptyBorder(6, 14, 8, 14));
        panelCentral.add(barraEquipo, BorderLayout.NORTH);

        modeloPlantel = new DefaultTableModel(new Object[]{
            "DNI", "Apellido y Nombre", "Posición", "Estado", "Goles", "Sanción Vigente", "Código Credencial"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaPlantel = new JTable(modeloPlantel);
        tablaPlantel.setRowHeight(25);
        panelCentral.add(new JScrollPane(tablaPlantel), BorderLayout.CENTER);

        // --- PESTAÑAS INFERIORES COMPLETAS Y ORDENADAS ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(0, 310));
        tabs.setFont(new Font("SansSerif", Font.BOLD, 12));

        tabs.addTab("️ Inscribir Nuevo Equipo", crearPanelNuevoEquipoOrdenado());
        tabs.addTab("️ Edición de Equipo Existente", crearPanelEdicionEquipoOrdenado());
        tabs.addTab(" Alta de Jugador al Plantel", crearPanelAltaJugadorOrdenado());
        tabs.addTab(" Habilitación / Rechazo de Inscripciones", crearPanelEvaluarInscripcion());
        tabs.addTab(" Adjuntar Documentación", crearPanelDocumentacion());

        panelCentral.add(tabs, BorderLayout.SOUTH);
        add(panelCentral, BorderLayout.CENTER);
    }


    private JPanel crearPanelNuevoEquipoOrdenado() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 20, 12, 20));
        form.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 8, 5, 8);

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.30;
        JLabel lblNom = new JLabel("Nombre del Equipo:");
        lblNom.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblNom, gbc);

        gbc.gridx = 1; gbc.weightx = 0.70;
        txtNombreEquipo = new JTextField(24);
        txtNombreEquipo.setPreferredSize(new Dimension(300, 28));
        form.add(txtNombreEquipo, gbc);
        row++;


        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.30;
        JLabel lblCat = new JLabel("Categoría:");
        lblCat.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblCat, gbc);

        gbc.gridx = 1; gbc.weightx = 0.70;
        txtCategoria = new JTextField("Libres", 24);
        txtCategoria.setPreferredSize(new Dimension(300, 28));
        form.add(txtCategoria, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.30;
        JLabel lblCam = new JLabel("Color de Camiseta:");
        lblCam.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblCam, gbc);

        gbc.gridx = 1; gbc.weightx = 0.70;
        txtCamiseta = new JTextField("Verde y Blanca", 24);
        txtCamiseta.setPreferredSize(new Dimension(300, 28));
        form.add(txtCamiseta, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.30;
        JLabel lblSede = new JLabel("Sede Habitual (Cancha):");
        lblSede.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblSede, gbc);

        gbc.gridx = 1; gbc.weightx = 0.70;
        cmbSedeHabitual = new JComboBox<>();
        cmbSedeHabitual.setPreferredSize(new Dimension(300, 28));
        form.add(cmbSedeHabitual, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.30;
        JLabel lblEsc = new JLabel("Escudo del Equipo (Opcional):");
        lblEsc.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblEsc, gbc);

        gbc.gridx = 1; gbc.weightx = 0.70;
        JPanel panelEscudo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelEscudo.setOpaque(false);
        lblPreviewEscudoNuevo = new JLabel(" Sin Escudo ");
        lblPreviewEscudoNuevo.setHorizontalAlignment(SwingConstants.CENTER);
        lblPreviewEscudoNuevo.setPreferredSize(new Dimension(85, 42));
        lblPreviewEscudoNuevo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblPreviewEscudoNuevo.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        JButton btnElegirEscudo = UtilidadesImagen.crearBotonEstilizado("🖼️ Subir Archivo de Escudo...", UtilidadesImagen.AZUL_ACCION, Color.WHITE);
        btnElegirEscudo.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Seleccionar Escudo para el Nuevo Equipo");
            chooser.setFileFilter(new FileNameExtensionFilter("Imágenes (PNG, JPG, JPEG, GIF)", "png", "jpg", "jpeg", "gif"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File sel = chooser.getSelectedFile();
                String nomEq = txtNombreEquipo.getText().trim();
                String copiado = UtilidadesImagen.copiarEscudoArchivo(this, sel, nomEq);
                if (copiado != null) {
                    rutaEscudoNuevoEquipo = copiado;
                    lblPreviewEscudoNuevo.setText("");
                    lblPreviewEscudoNuevo.setIcon(UtilidadesImagen.cargarImagenEscalada(copiado, 42, 42));
                    JOptionPane.showMessageDialog(this, "¡Escudo cargado correctamente para el nuevo equipo!", "Escudo Cargado", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        panelEscudo.add(lblPreviewEscudoNuevo);
        panelEscudo.add(btnElegirEscudo);
        form.add(panelEscudo, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel lblFlujo = new JLabel("<html><i>* Nota: Al crear el equipo no se asigna capitán. Primero se crea el equipo, luego se agregan los jugadores en la pestaña 'Alta de Jugador al Plantel', y finalmente se elige el Capitán entre los integrantes de dicho plantel.</i></html>");
        lblFlujo.setForeground(new Color(60, 90, 75));
        lblFlujo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        form.add(lblFlujo, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 8, 8, 8);
        JButton btnCrearEq = UtilidadesImagen.crearBotonEstilizado("➕ Inscribir / Registrar Nuevo Equipo", UtilidadesImagen.VERDE_CANCHA, Color.WHITE);
        btnCrearEq.setPreferredSize(new Dimension(360, 36));
        btnCrearEq.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnCrearEq.addActionListener(e -> {
            if (cmbSedeHabitual.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Debe existir al menos una cancha registrada para asignarla como sede.", "Sede Requerida", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Cancha cSel = (Cancha) cmbSedeHabitual.getSelectedItem();
            String rta = gestor.registrarEquipoNuevo(
                    txtNombreEquipo.getText().trim(),
                    txtCategoria.getText().trim(),
                    txtCamiseta.getText().trim(),
                    cSel.getNombre(),
                    null, // Capitán inicialmente null
                    rutaEscudoNuevoEquipo
            );
            JOptionPane.showMessageDialog(this,
                    rta + "\n\nPasos siguientes:\n"
                    + "1. Pasá a la pestaña '⚽ Alta de Jugador al Plantel' para agregar jugadores a este equipo.\n"
                    + "2. Una vez cargado el plantel, hacé clic en '©️ Designar / Cambiar Capitán' para designar al capitán entre sus propios jugadores.",
                    "Equipo Registrado", JOptionPane.INFORMATION_MESSAGE);
            txtNombreEquipo.setText("");
            rutaEscudoNuevoEquipo = null;
            lblPreviewEscudoNuevo.setIcon(null);
            lblPreviewEscudoNuevo.setText(" Sin Escudo ");
            refrescarDatos();
        });
        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBtn.setOpaque(false);
        panelBtn.add(btnCrearEq);
        form.add(panelBtn, gbc);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel crearPanelEdicionEquipoOrdenado() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 20, 12, 20));
        form.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 8, 5, 8);

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.30;
        JLabel lblNom = new JLabel("Nombre del Equipo:");
        lblNom.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblNom, gbc);

        gbc.gridx = 1; gbc.weightx = 0.70;
        txtEditNombre = new JTextField(24);
        txtEditNombre.setPreferredSize(new Dimension(300, 28));
        form.add(txtEditNombre, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.30;
        JLabel lblCat = new JLabel("Categoría:");
        lblCat.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblCat, gbc);

        gbc.gridx = 1; gbc.weightx = 0.70;
        txtEditCategoria = new JTextField(24);
        txtEditCategoria.setPreferredSize(new Dimension(300, 28));
        form.add(txtEditCategoria, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.30;
        JLabel lblCam = new JLabel("Color de Camiseta:");
        lblCam.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblCam, gbc);

        gbc.gridx = 1; gbc.weightx = 0.70;
        txtEditCamiseta = new JTextField(24);
        txtEditCamiseta.setPreferredSize(new Dimension(300, 28));
        form.add(txtEditCamiseta, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.30;
        JLabel lblSede = new JLabel("Sede Habitual (Cancha):");
        lblSede.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblSede, gbc);

        gbc.gridx = 1; gbc.weightx = 0.70;
        cmbEditSedeHabitual = new JComboBox<>();
        cmbEditSedeHabitual.setPreferredSize(new Dimension(300, 28));
        form.add(cmbEditSedeHabitual, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.30;
        JLabel lblCap = new JLabel("Capitán Responsable:");
        lblCap.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblCap, gbc);

        gbc.gridx = 1; gbc.weightx = 0.70;
        cmbEditCapitan = new JComboBox<>();
        cmbEditCapitan.setPreferredSize(new Dimension(300, 28));
        form.add(cmbEditCapitan, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.30;
        JLabel lblEsc = new JLabel("Escudo Actual / Nuevo:");
        lblEsc.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblEsc, gbc);

        gbc.gridx = 1; gbc.weightx = 0.70;
        JPanel panelEscudo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelEscudo.setOpaque(false);
        lblPreviewEscudoEdit = new JLabel(" Sin Escudo ");
        lblPreviewEscudoEdit.setHorizontalAlignment(SwingConstants.CENTER);
        lblPreviewEscudoEdit.setPreferredSize(new Dimension(85, 42));
        lblPreviewEscudoEdit.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblPreviewEscudoEdit.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        JButton btnCambiarEscudo = UtilidadesImagen.crearBotonEstilizado("🖼️ Cambiar Escudo...", UtilidadesImagen.AZUL_ACCION, Color.WHITE);
        btnCambiarEscudo.addActionListener(e -> {
            Equipo eq = (Equipo) cmbEquipos.getSelectedItem();
            if (eq == null) return;
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Seleccionar Nuevo Escudo para '" + eq.getNombre() + "'");
            chooser.setFileFilter(new FileNameExtensionFilter("Imágenes (PNG, JPG, JPEG, GIF)", "png", "jpg", "jpeg", "gif"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File sel = chooser.getSelectedFile();
                String copiado = UtilidadesImagen.copiarEscudoArchivo(this, sel, eq.getNombre());
                if (copiado != null) {
                    rutaEscudoEdit = copiado;
                    lblPreviewEscudoEdit.setText("");
                    lblPreviewEscudoEdit.setIcon(UtilidadesImagen.cargarImagenEscalada(copiado, 42, 42));
                }
            }
        });
        panelEscudo.add(lblPreviewEscudoEdit);
        panelEscudo.add(btnCambiarEscudo);
        form.add(panelEscudo, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 8, 8, 8);
        JButton btnGuardar = UtilidadesImagen.crearBotonEstilizado("💾 Guardar Modificaciones del Equipo", UtilidadesImagen.VERDE_CANCHA, Color.WHITE);
        btnGuardar.setPreferredSize(new Dimension(360, 36));
        btnGuardar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnGuardar.addActionListener(e -> {
            Equipo eq = (Equipo) cmbEquipos.getSelectedItem();
            if (eq == null) {
                JOptionPane.showMessageDialog(this, "Seleccioná primero un equipo de la lista superior.");
                return;
            }
            Cancha cSel = (Cancha) cmbEditSedeHabitual.getSelectedItem();
            Jugador capSel = (Jugador) cmbEditCapitan.getSelectedItem();
            String rta = gestor.modificarEquipo(
                    eq,
                    txtEditNombre.getText().trim(),
                    txtEditCategoria.getText().trim(),
                    txtEditCamiseta.getText().trim(),
                    (cSel != null ? cSel.getNombre() : eq.getSedeHabitual()),
                    capSel,
                    rutaEscudoEdit
            );
            JOptionPane.showMessageDialog(this, rta, "Edición de Equipo", JOptionPane.INFORMATION_MESSAGE);
            refrescarDatos();
        });
        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBtn.setOpaque(false);
        panelBtn.add(btnGuardar);
        form.add(panelBtn, gbc);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private void cargarDatosEnEdicion() {
        Equipo eq = (Equipo) cmbEquipos.getSelectedItem();
        if (eq == null || txtEditNombre == null) return;
        txtEditNombre.setText(eq.getNombre() != null ? eq.getNombre() : "");
        txtEditCategoria.setText(eq.getCategoria() != null ? eq.getCategoria() : "Libres");
        txtEditCamiseta.setText(eq.getColorCamiseta() != null ? eq.getColorCamiseta() : "");

        if (lblPreviewEscudoEdit != null) {
            ImageIcon escEdit = UtilidadesImagen.cargarEscudoEquipo(eq, 42, 42);
            if (escEdit != null) {
                lblPreviewEscudoEdit.setIcon(escEdit);
                lblPreviewEscudoEdit.setText("");
            } else {
                lblPreviewEscudoEdit.setIcon(null);
                lblPreviewEscudoEdit.setText(" Sin Escudo ");
            }
        }
        rutaEscudoEdit = eq.getEscudo();

        if (cmbEditSedeHabitual != null && eq.getSedeHabitual() != null) {
            for (int i = 0; i < cmbEditSedeHabitual.getItemCount(); i++) {
                Cancha c = cmbEditSedeHabitual.getItemAt(i);
                if (c.getNombre() != null && c.getNombre().equalsIgnoreCase(eq.getSedeHabitual())) {
                    cmbEditSedeHabitual.setSelectedIndex(i);
                    break;
                }
            }
        }

        if (cmbEditCapitan != null) {
            cmbEditCapitan.removeAllItems();
            if (eq.getPlantel() != null && !eq.getPlantel().isEmpty()) {
                for (Jugador j : eq.getPlantel()) {
                    cmbEditCapitan.addItem(j);
                }
                if (eq.getCapitan() != null) {
                    for (int i = 0; i < cmbEditCapitan.getItemCount(); i++) {
                        if (cmbEditCapitan.getItemAt(i).getDni() == eq.getCapitan().getDni()) {
                            cmbEditCapitan.setSelectedIndex(i);
                            break;
                        }
                    }
                }
                cmbEditCapitan.setEnabled(true);
            } else {
                cmbEditCapitan.setEnabled(false);
            }
        }
    }

    private JPanel crearPanelAltaJugadorOrdenado() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 20, 12, 20));
        form.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 8, 5, 8);

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.30;
        JLabel lblDni = new JLabel("DNI del Jugador:");
        lblDni.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblDni, gbc);

        gbc.gridx = 1; gbc.weightx = 0.70;
        txtDniJug = new JTextField(24);
        txtDniJug.setPreferredSize(new Dimension(300, 28));
        form.add(txtDniJug, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.30;
        JLabel lblNom = new JLabel("Apellido y Nombre:");
        lblNom.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblNom, gbc);

        gbc.gridx = 1; gbc.weightx = 0.70;
        txtNomJug = new JTextField(24);
        txtNomJug.setPreferredSize(new Dimension(300, 28));
        form.add(txtNomJug, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.30;
        JLabel lblFec = new JLabel("Fecha de Nacimiento (DD/MM/YYYY):");
        lblFec.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblFec, gbc);

        gbc.gridx = 1; gbc.weightx = 0.70;
        txtFechaNacJug = new JTextField("15/05/2000", 24);
        txtFechaNacJug.setPreferredSize(new Dimension(300, 28));
        form.add(txtFechaNacJug, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.30;
        JLabel lblPos = new JLabel("Posición en Campo:");
        lblPos.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblPos, gbc);

        gbc.gridx = 1; gbc.weightx = 0.70;
        cmbPosJug = new JComboBox<>(new String[]{"Arquero", "Defensor", "Mediocampista", "Delantero", "Polifuncional"});
        cmbPosJug.setPreferredSize(new Dimension(300, 28));
        form.add(cmbPosJug, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.30;
        JLabel lblEst = new JLabel("Estado Inicial del Jugador:");
        lblEst.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblEst, gbc);

        gbc.gridx = 1; gbc.weightx = 0.70;
        cmbEstadoJug = new JComboBox<>(new String[]{"Activo", "Suspendido", "Lesionado"});
        cmbEstadoJug.setPreferredSize(new Dimension(300, 28));
        form.add(cmbEstadoJug, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.30;
        JLabel lblMail = new JLabel("Correo Electrónico (Email):");
        lblMail.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblMail, gbc);

        gbc.gridx = 1; gbc.weightx = 0.70;
        txtEmailJug = new JTextField("jugador@ugd.edu.ar", 24);
        txtEmailJug.setPreferredSize(new Dimension(300, 28));
        form.add(txtEmailJug, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 8, 8, 8);
        JButton btnAgregar = UtilidadesImagen.crearBotonEstilizado(" Dar de Alta Jugador al Plantel", UtilidadesImagen.VERDE_CANCHA, Color.WHITE);
        btnAgregar.setPreferredSize(new Dimension(360, 36));
        btnAgregar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAgregar.addActionListener(e -> {
            try {
                Equipo eq = (Equipo) cmbEquipos.getSelectedItem();
                if (eq == null) {
                    JOptionPane.showMessageDialog(this, "Seleccioná primero un equipo en la barra superior.");
                    return;
                }
                long dni = Long.parseLong(txtDniJug.getText().trim());
                Date fNac;
                try {
                    fNac = sdf.parse(txtFechaNacJug.getText().trim());
                } catch (Exception ex) {
                    fNac = new Date(100, 0, 1);
                }
                String pos = (String) cmbPosJug.getSelectedItem();
                String est = (String) cmbEstadoJug.getSelectedItem();

                String rta = gestor.agregarJugadorAEquipo(
                        eq, dni, txtNomJug.getText().trim(), pos, fNac,
                        txtEmailJug.getText().trim(), "1234", est
                );
                JOptionPane.showMessageDialog(this, rta, "Alta de Jugador", JOptionPane.INFORMATION_MESSAGE);
                actualizarTablaPlantel();
                txtDniJug.setText("");
                txtNomJug.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingresá un número de DNI válido.", "Dato Inválido", JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBtn.setOpaque(false);
        panelBtn.add(btnAgregar);
        form.add(panelBtn, gbc);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel crearPanelEvaluarInscripcion() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(8, 12, 8, 12));
        p.setBackground(Color.WHITE);

        JPanel barraAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        barraAcciones.setOpaque(false);
        lblContadorPendientes = new JLabel(" Inscripciones Pendientes a Evaluar: 0");
        lblContadorPendientes.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblContadorPendientes.setForeground(UtilidadesImagen.VERDE_OSCURO);
        barraAcciones.add(lblContadorPendientes);

        JButton btnVerDoc = UtilidadesImagen.crearBotonEstilizado("️ Ver Documento de Inscripción", UtilidadesImagen.AZUL_ACCION, Color.WHITE);
        btnVerDoc.addActionListener(e -> verDocumentoDeEquipoSeleccionado());
        barraAcciones.add(btnVerDoc);

        JButton btnAprobar = UtilidadesImagen.crearBotonEstilizado(" Aprobar y Habilitar Inscripción", UtilidadesImagen.VERDE_CANCHA, Color.WHITE);
        btnAprobar.addActionListener(e -> evaluarSeleccionado(true));
        barraAcciones.add(btnAprobar);

        JButton btnRechazar = UtilidadesImagen.crearBotonEstilizado(" Rechazar Inscripción", new Color(185, 28, 28), Color.WHITE);
        btnRechazar.addActionListener(e -> evaluarSeleccionado(false));
        barraAcciones.add(btnRechazar);

        p.add(barraAcciones, BorderLayout.NORTH);

        modeloInscripcionesPendientes = new DefaultTableModel(
                new Object[]{"ID", "Equipo", "Capitán", "Documento Subido", "Sede Habitual", "Jugadores", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaInscripcionesPendientes = new JTable(modeloInscripcionesPendientes);
        tablaInscripcionesPendientes.setRowHeight(24);
        p.add(new JScrollPane(tablaInscripcionesPendientes), BorderLayout.CENTER);

        return p;
    }

    private JPanel crearPanelDocumentacion() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 18));
        p.setBackground(Color.WHITE);
        p.add(new JLabel("Archivo o Comprobante Digital:"));
        txtRutaDoc = new JTextField("Certificado_Medico_Y_Lista_Buena_Fe.pdf", 28);
        p.add(txtRutaDoc);

        JButton btnBuscarArchivo = new JButton(" Buscar Archivo en mi PC...");
        btnBuscarArchivo.addActionListener(e -> {
            JFileChooser ch = new JFileChooser();
            ch.setDialogTitle("Seleccionar comprobante / certificado médico");
            if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File sel = ch.getSelectedFile();
                try {
                    File dirDocs = new File("documentos");
                    if (!dirDocs.exists()) dirDocs.mkdirs();
                    File dest = new File(dirDocs, sel.getName());
                    java.nio.file.Files.copy(sel.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    txtRutaDoc.setText(sel.getName());
                } catch (Exception ex) {
                    txtRutaDoc.setText(sel.getName());
                }
            }
        });
        p.add(btnBuscarArchivo);

        JButton btnAdjuntar = UtilidadesImagen.crearBotonEstilizado(" Adjuntar y Enviar Documentación", UtilidadesImagen.AZUL_ACCION, Color.WHITE);
        btnAdjuntar.addActionListener(e -> {
            Equipo eq = (Equipo) cmbEquipos.getSelectedItem();
            if (eq == null) {
                JOptionPane.showMessageDialog(this, "Seleccioná primero un equipo en la barra superior.");
                return;
            }
            String doc = txtRutaDoc.getText().trim();
            if (doc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingresá o seleccioná un nombre de archivo.");
                return;
            }
            String rta = gestor.adjuntarDocumentacionEquipo(eq, doc);
            if (eq.getInscripcion() != null) {
                eq.getInscripcion().setEstado("Pendiente");
            }
            JOptionPane.showMessageDialog(this,
                    rta + "\n\n Estado: PENDIENTE. Queda listo para que el Administrador u Organizador evalúe y apruebe tu inscripción.",
                    "Documentación Enviada", JOptionPane.INFORMATION_MESSAGE);
            refrescarDatos();
        });
        p.add(btnAdjuntar);
        return p;
    }

    private void cambiarCapitanDelEquipo() {
        Equipo eq = (Equipo) cmbEquipos.getSelectedItem();
        if (eq == null) {
            JOptionPane.showMessageDialog(this, "Seleccioná primero un equipo.");
            return;
        }
        if (eq.getPlantel() == null || eq.getPlantel().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El equipo '" + eq.getNombre() + "' aún no tiene jugadores en su plantel.\n"
                    + "Para designar un Capitán, primero debés dar de alta al menos un jugador en la pestaña 'Alta de Jugador al Plantel'.",
                    "Plantel Vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JComboBox<Jugador> cmbJugadoresEquipo = new JComboBox<>();
        for (Jugador j : eq.getPlantel()) {
            cmbJugadoresEquipo.addItem(j);
        }
        if (eq.getCapitan() != null) {
            for (int i = 0; i < cmbJugadoresEquipo.getItemCount(); i++) {
                if (cmbJugadoresEquipo.getItemAt(i).getDni() == eq.getCapitan().getDni()) {
                    cmbJugadoresEquipo.setSelectedIndex(i);
                    break;
                }
            }
        }

        int opcion = JOptionPane.showConfirmDialog(this, cmbJugadoresEquipo,
                "Seleccioná el jugador del plantel para designarlo Capitán de '" + eq.getNombre() + "':",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (opcion == JOptionPane.OK_OPTION) {
            Jugador nuevoCap = (Jugador) cmbJugadoresEquipo.getSelectedItem();
            if (nuevoCap != null) {
                String rta = gestor.cambiarCapitanEquipo(eq, nuevoCap);
                JOptionPane.showMessageDialog(this, rta, "Capitán Actualizado", JOptionPane.INFORMATION_MESSAGE);
                refrescarDatos();
            }
        }
    }

    private void eliminarEquipoSeleccionado() {
        Equipo eq = (Equipo) cmbEquipos.getSelectedItem();
        if (eq == null) {
            JOptionPane.showMessageDialog(this, "Seleccioná primero un equipo de la lista.");
            return;
        }
        int resp = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de que deseás eliminar permanentemente al equipo '" + eq.getNombre() + "'?\n\n"
                + "Esta acción dará de baja el equipo, lo quitará de los torneos y eliminará su registro del sistema.",
                "Confirmar Eliminación de Equipo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (resp == JOptionPane.YES_OPTION) {
            String rta = gestor.eliminarEquipo(eq);
            JOptionPane.showMessageDialog(this, rta, "Equipo Eliminado", JOptionPane.INFORMATION_MESSAGE);
            refrescarDatos();
        }
    }

    private void abrirModificarPerfilJugador() {
        Jugador seleccionado = null;
        int fila = tablaPlantel.getSelectedRow();
        Equipo eq = (Equipo) cmbEquipos.getSelectedItem();

        if (fila >= 0 && eq != null && eq.getPlantel() != null && fila < eq.getPlantel().size()) {
            seleccionado = eq.getPlantel().get(fila);
        } else if (gestor.getJugadorActual() != null) {
            seleccionado = gestor.getJugadorActual();
        }

        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccioná un jugador en la tabla para modificar su perfil.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        VentanaModificarPerfil dlg = new VentanaModificarPerfil(this, seleccionado, () -> {
            refrescarDatos();
        });
        dlg.setVisible(true);
    }

    private Equipo obtenerEquipoDeTablaPendientes() {
        if (tablaInscripcionesPendientes == null) return null;
        int row = tablaInscripcionesPendientes.getSelectedRow();
        if (row < 0) return null;
        Object valId = modeloInscripcionesPendientes.getValueAt(row, 0);
        for (Equipo eq : gestor.getEquipos()) {
            if (String.valueOf(eq.getId()).equals(String.valueOf(valId))
                    || eq.getNombre().equals(modeloInscripcionesPendientes.getValueAt(row, 1))) {
                return eq;
            }
        }
        return null;
    }

    private void verDocumentoDeEquipoSeleccionado() {
        Equipo eq = obtenerEquipoDeTablaPendientes();
        if (eq == null) {
            JOptionPane.showMessageDialog(this, "Seleccioná primero un equipo de la lista de pendientes para revisar su documento.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String doc = (eq.getInscripcion() != null) ? eq.getInscripcion().getDocumentoAdjunto() : null;
        verDocumentoInscripcion(doc, eq);
    }

    private void evaluarSeleccionado(boolean aprobar) {
        Equipo eq = obtenerEquipoDeTablaPendientes();
        if (eq == null) {
            JOptionPane.showMessageDialog(this, "Seleccioná un equipo de la tabla de pendientes para evaluar su inscripción.", "Selección Requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (aprobar) {
            String rta = gestor.evaluarInscripcionEquipo(eq, 18, true, "");
            JOptionPane.showMessageDialog(this, rta, "Inscripción Aprobada", JOptionPane.INFORMATION_MESSAGE);
        } else {
            String motivo = JOptionPane.showInputDialog(this, "Ingresá el motivo del rechazo para la inscripción de '" + eq.getNombre() + "':", "Motivo de Rechazo", JOptionPane.QUESTION_MESSAGE);
            if (motivo != null && !motivo.trim().isEmpty()) {
                String rta = gestor.evaluarInscripcionEquipo(eq, 18, false, motivo.trim());
                JOptionPane.showMessageDialog(this, rta, "Inscripción Rechazada", JOptionPane.WARNING_MESSAGE);
            }
        }
        refrescarDatos();
    }

    private void verDocumentoInscripcion(String nombreDoc, Equipo eq) {
        if (nombreDoc == null || nombreDoc.trim().isEmpty() || "-".equals(nombreDoc.trim()) || "(Sin archivo)".equals(nombreDoc.trim())) {
            JOptionPane.showMessageDialog(this, "El equipo '" + (eq != null ? eq.getNombre() : "") + "' no posee un documento adjunto cargado.", "Sin Documento", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File f1 = new File("documentos", nombreDoc);
        File f2 = new File(nombreDoc);
        File f3 = new File(UtilidadesImagen.obtenerCarpetaImagenes(), nombreDoc);
        File objetivo = f1.exists() ? f1 : (f2.exists() ? f2 : (f3.exists() ? f3 : null));

        if (objetivo != null && objetivo.exists()) {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(objetivo);
                    return;
                }
            } catch (Exception ignored) {}
        }

        JDialog dlgVisor = new JDialog(this, "Visor de Documento de Inscripción", true);
        dlgVisor.setSize(560, 400);
        dlgVisor.setLocationRelativeTo(this);
        dlgVisor.setLayout(new BorderLayout(10, 10));

        JPanel headerV = new JPanel(new BorderLayout());
        headerV.setBackground(UtilidadesImagen.VERDE_OSCURO);
        headerV.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel lblV = new JLabel(" DOCUMENTO DIGITAL DE INSCRIPCIÓN");
        lblV.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblV.setForeground(Color.WHITE);
        headerV.add(lblV, BorderLayout.WEST);
        dlgVisor.add(headerV, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new GridLayout(6, 1, 8, 8));
        cuerpo.setBorder(new EmptyBorder(16, 22, 16, 22));
        cuerpo.setBackground(Color.WHITE);

        String capNom = (eq != null && eq.getCapitan() != null)
                ? eq.getCapitan().getNombre() + " (DNI: " + eq.getCapitan().getDni() + ")" : "A designar";
        cuerpo.add(new JLabel("Equipo: " + (eq != null ? eq.getNombre() : "-")));
        cuerpo.add(new JLabel("Capitán Responsable: " + capNom));
        cuerpo.add(new JLabel("Archivo Adjunto: " + nombreDoc));
        cuerpo.add(new JLabel("Certificación: Apto Médico de Competencia y Ficha de Buena Fe"));
        cuerpo.add(new JLabel("Firma Digital: Presentada y registrada para el Torneo UGD"));
        cuerpo.add(new JLabel("Estado de Inscripción: " + (eq != null && eq.getInscripcion() != null ? eq.getInscripcion().getEstado() : "Pendiente")));
        dlgVisor.add(cuerpo, BorderLayout.CENTER);

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        JButton btnCerrarV = new JButton("Cerrar");
        btnCerrarV.addActionListener(ev -> dlgVisor.dispose());
        pBtn.add(btnCerrarV);
        dlgVisor.add(pBtn, BorderLayout.SOUTH);

        dlgVisor.setVisible(true);
    }

    private void mostrarCredencialJugador() {
        Equipo eq = (Equipo) cmbEquipos.getSelectedItem();
        if (eq == null) return;
        Jugador seleccionado = null;
        int f = tablaPlantel.getSelectedRow();
        if (f >= 0 && eq.getPlantel() != null && f < eq.getPlantel().size()) {
            seleccionado = eq.getPlantel().get(f);
        } else if (gestor.getJugadorActual() != null) {
            seleccionado = gestor.getJugadorActual();
        } else if (eq.getPlantel() != null && !eq.getPlantel().isEmpty()) {
            seleccionado = eq.getPlantel().get(0);
        }
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccioná un jugador de la tabla para imprimir su credencial.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nomTorneo = !gestor.getTorneos().isEmpty() ? gestor.getTorneos().get(0).getNombre() : "Torneo Clausura UGD 2026";
        DialogoCredencialImprimible dlg = new DialogoCredencialImprimible(this, seleccionado, eq, nomTorneo);
        dlg.setVisible(true);
    }

    private void actualizarComboSedes() {
        if (cmbSedeHabitual != null) {
            cmbSedeHabitual.removeAllItems();
            for (Cancha c : gestor.getCanchas()) {
                cmbSedeHabitual.addItem(c);
            }
        }
        if (cmbEditSedeHabitual != null) {
            cmbEditSedeHabitual.removeAllItems();
            for (Cancha c : gestor.getCanchas()) {
                cmbEditSedeHabitual.addItem(c);
            }
        }
    }

    private void refrescarDatos() {
        cmbEquipos.removeAllItems();
        for (Equipo e : gestor.getEquipos()) {
            cmbEquipos.addItem(e);
        }

        actualizarComboSedes();
        actualizarTablaPlantel();
        refrescarTablaInscripcionesPendientes();
        cargarDatosEnEdicion();
    }

    private void refrescarTablaInscripcionesPendientes() {
        if (modeloInscripcionesPendientes == null) return;
        modeloInscripcionesPendientes.setRowCount(0);
        int pendientes = 0;
        for (Equipo eq : gestor.getEquipos()) {
            boolean esPendiente = (eq.getInscripcion() == null)
                    || ("Pendiente".equalsIgnoreCase(eq.getInscripcion().getEstado()))
                    || ("Rechazado".equalsIgnoreCase(eq.getInscripcion().getEstado()));
            if (esPendiente) {
                pendientes++;
                String nomCap = (eq.getCapitan() != null) ? eq.getCapitan().getNombre() : "-";
                String doc = (eq.getInscripcion() != null && eq.getInscripcion().getDocumentoAdjunto() != null)
                        ? eq.getInscripcion().getDocumentoAdjunto() : "(Sin archivo)";
                String est = (eq.getInscripcion() != null && eq.getInscripcion().getEstado() != null)
                        ? eq.getInscripcion().getEstado() : "Pendiente";
                int cantJug = (eq.getPlantel() != null) ? eq.getPlantel().size() : 0;
                modeloInscripcionesPendientes.addRow(new Object[]{
                    eq.getId(), eq.getNombre(), nomCap, doc, eq.getSedeHabitual(), cantJug, est
                });
            }
        }
        if (lblContadorPendientes != null) {
            lblContadorPendientes.setText(" Inscripciones Pendientes a Evaluar: " + pendientes);
        }
    }

    private void actualizarTablaPlantel() {
        modeloPlantel.setRowCount(0);
        Equipo eq = (Equipo) cmbEquipos.getSelectedItem();
        if (eq == null) return;

        ImageIcon esc = UtilidadesImagen.cargarEscudoEquipo(eq, 64, 64);
        if (esc != null) {
            lblEscudoEquipo.setIcon(esc);
            lblEscudoEquipo.setVisible(true);
        } else {
            lblEscudoEquipo.setIcon(null);
            lblEscudoEquipo.setVisible(false);
        }

        String docAdj = (eq.getInscripcion() != null && eq.getInscripcion().getDocumentoAdjunto() != null)
                ? " | Doc: " + eq.getInscripcion().getDocumentoAdjunto() : "";
        lblResumenEquipo.setText(eq.verPerfil() + docAdj);

        if (eq.getPlantel() != null) {
            for (Jugador j : eq.getPlantel()) {
                String codCred = (j.getCredencial() != null) ? j.getCredencial().getCodigoBarras() : "CRED-UGD";
                modeloPlantel.addRow(new Object[]{
                    j.getDni(), j.getNombre(), j.getPosicion(), j.getEstado(),
                    j.getGoles(), j.tieneSancionVigente() ? " Sí (Suspendido)" : " No", codCred
                });
            }
        }
    }
}
