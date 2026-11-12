package vistas;

import controlador.GestorSistema;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import modelo.*;

public class VentanaUsuarios extends JDialog {

    private final GestorSistema gestor;
    private DefaultTableModel modeloTabla;
    private JTable tabla;
    private JComboBox<String> cmbTipo;
    private JTextField txtDni, txtNombre, txtPosicion, txtEmail;
    private JPasswordField txtPass;
    private JLabel lblPosicion, lblEquipo;
    private JComboBox<Object> cmbEquipoExistente;

    public VentanaUsuarios(Frame parent) {
        super(parent, "Gestión de Usuarios y Actores del Sistema (ABM)", true);
        this.gestor = GestorSistema.getInstancia();
        setSize(1020, 640);
        setMinimumSize(new Dimension(850, 500));
        setLocationRelativeTo(parent);
        setResizable(true);
        setLayout(new BorderLayout(10, 10));
        inicializarComponentes();
        cargarTabla();
    }

    private void inicializarComponentes() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UtilidadesImagen.VERDE_OSCURO);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));
        JLabel lblTit = new JLabel("GESTIÓN DE USUARIOS Y ACTORES (ADMINISTRADOR)");
        lblTit.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTit.setForeground(Color.WHITE);
        header.add(lblTit, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Alta de Nuevo Usuario / Jugador"));
        form.setPreferredSize(new Dimension(380, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.4;
        form.add(new JLabel("Rol / Tipo:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        cmbTipo = new JComboBox<>(new String[]{"Administrador", "Organizador", "Árbitro", "Capitán", "Jugador"});
        form.add(cmbTipo, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.4;
        form.add(new JLabel("DNI:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        txtDni = new JTextField();
        form.add(txtDni, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.4;
        form.add(new JLabel("Apellido y Nombre:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        txtNombre = new JTextField();
        form.add(txtNombre, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.4;
        lblPosicion = new JLabel("Posición:");
        form.add(lblPosicion, gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        txtPosicion = new JTextField("Mediocampista");
        form.add(txtPosicion, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.4;
        lblEquipo = new JLabel("Equipo a Integrar:");
        form.add(lblEquipo, gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        cmbEquipoExistente = new JComboBox<>();
        actualizarComboEquipos();
        form.add(cmbEquipoExistente, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.4;
        form.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        txtEmail = new JTextField();
        form.add(txtEmail, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.4;
        form.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        txtPass = new JPasswordField("1234");
        form.add(txtPass, gbc);
        row++;

        JButton btnGuardar = UtilidadesImagen.crearBotonEstilizado("➕ Registrar Usuario", UtilidadesImagen.VERDE_CANCHA, Color.WHITE);
        btnGuardar.addActionListener(e -> registrarUsuario());

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        form.add(btnGuardar, gbc);


        cmbTipo.addActionListener(e -> ajustarVisibilidadCampos());
        ajustarVisibilidadCampos();

        JScrollPane scrollForm = new JScrollPane(form);
        scrollForm.setBorder(null);
        add(scrollForm, BorderLayout.WEST);

        modeloTabla = new DefaultTableModel(new Object[]{"Categoría", "DNI", "Apellido y Nombre", "Email", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(26);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));

        JButton btnModificar = UtilidadesImagen.crearBotonEstilizado("️ Modificar Usuario Seleccionado", UtilidadesImagen.AZUL_ACCION, Color.WHITE);
        btnModificar.addActionListener(e -> abrirModificarUsuarioSeleccionado());

        JButton btnBaja = UtilidadesImagen.crearBotonEstilizado("Dar de Baja Seleccionado", new Color(185, 28, 28), Color.WHITE);
        btnBaja.addActionListener(e -> darDeBajaSeleccionado());

        JButton btnCerrar = UtilidadesImagen.crearBotonEstilizado("Cerrar", new Color(90, 95, 100), Color.WHITE);
        btnCerrar.addActionListener(e -> dispose());

        sur.add(btnModificar);
        sur.add(btnBaja);
        sur.add(btnCerrar);
        add(sur, BorderLayout.SOUTH);
    }

    private void actualizarComboEquipos() {
        cmbEquipoExistente.removeAllItems();
        cmbEquipoExistente.addItem("(Sin equipo)");
        for (Equipo eq : gestor.getEquipos()) {
            cmbEquipoExistente.addItem(eq);
        }
    }

    private void ajustarVisibilidadCampos() {
        String rol = (String) cmbTipo.getSelectedItem();
        boolean esJugadorOCapitan = "Jugador".equals(rol) || "Capitán".equals(rol);
        boolean esSoloJugador = "Jugador".equals(rol);

        lblPosicion.setVisible(esJugadorOCapitan);
        txtPosicion.setVisible(esJugadorOCapitan);

        lblEquipo.setVisible(esSoloJugador);
        cmbEquipoExistente.setVisible(esSoloJugador);

        revalidate();
        repaint();
    }

    private void registrarUsuario() {
        try {
            long dni = Long.parseLong(txtDni.getText().trim());
            String rol = (String) cmbTipo.getSelectedItem();
            String apNom = txtNombre.getText().trim();
            String pos = ("Jugador".equals(rol) || "Capitán".equals(rol)) ? txtPosicion.getText().trim() : "";
            String mail = txtEmail.getText().trim();
            String pass = new String(txtPass.getPassword()).trim();

            Equipo eqSeleccionado = null;
            if ("Jugador".equals(rol) && cmbEquipoExistente.getSelectedItem() instanceof Equipo) {
                eqSeleccionado = (Equipo) cmbEquipoExistente.getSelectedItem();
            }

            String rta = gestor.registrarNuevoUsuarioConEquipo(rol, dni, apNom, pos, mail, pass, eqSeleccionado);
            JOptionPane.showMessageDialog(this, rta);
            cargarTabla();
            txtDni.setText("");
            txtNombre.setText("");
            txtEmail.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresá un DNI numérico válido.");
        }
    }

    private void abrirModificarUsuarioSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccioná un usuario en la tabla para modificarlo.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String email = (String) modeloTabla.getValueAt(fila, 3);
        Object personaSeleccionada = null;

        for (Usuario u : gestor.getUsuarios()) {
            if (email != null && email.equalsIgnoreCase(u.getEmail())) {
                personaSeleccionada = u;
                break;
            }
        }
        if (personaSeleccionada == null) {
            for (Jugador j : gestor.getJugadores()) {
                if (email != null && email.equalsIgnoreCase(j.getEmail())) {
                    personaSeleccionada = j;
                    break;
                }
            }
        }

        if (personaSeleccionada != null) {
            VentanaModificarPerfil dlg = new VentanaModificarPerfil(this, personaSeleccionada, () -> {
                cargarTabla();
                actualizarComboEquipos();
            });
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "No se encontró el usuario seleccionado.");
        }
    }

    private void darDeBajaSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccioná una fila de la tabla.");
            return;
        }
        String email = (String) modeloTabla.getValueAt(fila, 3);
        for (Usuario u : gestor.getUsuarios()) {
            if (email != null && email.equalsIgnoreCase(u.getEmail())) {
                u.registrarBaja();
                JOptionPane.showMessageDialog(this, "Usuario marcado como Inactivo.");
                cargarTabla();
                return;
            }
        }
        for (Jugador j : gestor.getJugadores()) {
            if (email != null && email.equalsIgnoreCase(j.getEmail())) {
                j.actualizarEstado("Inactivo");
                JOptionPane.showMessageDialog(this, "Jugador marcado como Inactivo.");
                cargarTabla();
                return;
            }
        }
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        for (Usuario u : gestor.getUsuarios()) {
            String tipo = u.getClass().getSimpleName();
            if (u instanceof Arbitro) {
                tipo = "Árbitro (Mat. " + ((Arbitro) u).getMatricula() + ")";
            }
            String ap = (u.getApellido() != null && !u.getApellido().isEmpty()) ? " " + u.getApellido() : "";
            modeloTabla.addRow(new Object[]{
                tipo, u.getDni(), u.getNombre() + ap,
                u.getEmail(), u.getEstado()
            });
        }
        for (Jugador j : gestor.getJugadores()) {
            Equipo eq = gestor.buscarEquipoDeJugador(j);
            String infoEq = (eq != null) ? " [" + eq.getNombre() + "]" : " [Sin equipo]";
            String tipo = (j instanceof Capitan) ? "Capitán" + infoEq : "Jugador (" + j.getPosicion() + ")" + infoEq;
            modeloTabla.addRow(new Object[]{
                tipo, j.getDni(), j.getNombre(), j.getEmail(), j.getEstado()
            });
        }
    }
}
