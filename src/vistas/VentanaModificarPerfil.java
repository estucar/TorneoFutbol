package vistas;

import controlador.GestorSistema;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import modelo.Capitan;
import modelo.Equipo;
import modelo.Jugador;
import modelo.Usuario;

public class VentanaModificarPerfil extends JDialog {

    private final GestorSistema gestor;
    private final Object persona;
    private final Runnable callbackActualizacion;

    private JTextField txtDni;
    private JTextField txtNombreCompleto;
    private JTextField txtEmail;
    private JPasswordField txtPass;
    private JTextField txtPosicion;
    private JComboBox<String> cmbCategoria;
    private JComboBox<Object> cmbEquipo;

    public VentanaModificarPerfil(Window parent, Object persona, Runnable callbackActualizacion) {
        super(parent, "Modificar Información del Perfil", ModalityType.APPLICATION_MODAL);
        this.gestor = GestorSistema.getInstancia();
        this.persona = persona;
        this.callbackActualizacion = callbackActualizacion;

        setSize(560, 520);
        setLocationRelativeTo(parent);
        setResizable(true);
        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
        cargarDatosActuales();
    }

    private void inicializarComponentes() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UtilidadesImagen.VERDE_OSCURO);
        header.setBorder(new EmptyBorder(14, 18, 14, 18));

        String nombreTipo = (persona instanceof Usuario) ? persona.getClass().getSimpleName()
                : (persona instanceof Capitan ? "Capitán" : "Jugador");
        JLabel lblTit = new JLabel("️ MODIFICAR PERFIL: " + nombreTipo.toUpperCase());
        lblTit.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblTit.setForeground(Color.WHITE);
        header.add(lblTit, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 24, 16, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        form.add(new JLabel("DNI:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtDni = new JTextField();
        form.add(txtDni, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        form.add(new JLabel("Apellido y Nombre:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtNombreCompleto = new JTextField();
        form.add(txtNombreCompleto, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        form.add(new JLabel("Correo Electrónico (Email):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtEmail = new JTextField();
        form.add(txtEmail, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        form.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtPass = new JPasswordField();
        form.add(txtPass, gbc);
        row++;

        if (persona instanceof Jugador) {
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
            form.add(new JLabel("Posición en la cancha:"), gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            txtPosicion = new JTextField();
            form.add(txtPosicion, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
            form.add(new JLabel("Categoría en el Equipo:"), gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            cmbCategoria = new JComboBox<>(new String[]{"Jugador", "Capitán"});
            form.add(cmbCategoria, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
            form.add(new JLabel("Equipo Asignado:"), gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            cmbEquipo = new JComboBox<>();
            cmbEquipo.addItem("(Sin equipo)");
            for (Equipo eq : gestor.getEquipos()) {
                cmbEquipo.addItem(eq);
            }
            form.add(cmbEquipo, gbc);
            row++;
        }

        JScrollPane scrollForm = new JScrollPane(form);
        scrollForm.setBorder(null);
        add(scrollForm, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton btnGuardar = UtilidadesImagen.crearBotonEstilizado(" Guardar Cambios", UtilidadesImagen.VERDE_CANCHA, Color.WHITE);
        btnGuardar.addActionListener(e -> guardarCambios());

        JButton btnCancelar = UtilidadesImagen.crearBotonEstilizado("Cancelar", new Color(100, 105, 110), Color.WHITE);
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void cargarDatosActuales() {
        if (persona instanceof Usuario) {
            Usuario u = (Usuario) persona;
            txtDni.setText(String.valueOf(u.getDni()));
            String ape = (u.getApellido() != null && !u.getApellido().isEmpty()) ? u.getApellido() : "";
            txtNombreCompleto.setText((u.getNombre() + " " + ape).trim());
            txtEmail.setText(u.getEmail() != null ? u.getEmail() : "");
            txtPass.setText(u.getContrasenia() != null ? u.getContrasenia() : "");
        } else if (persona instanceof Jugador) {
            Jugador j = (Jugador) persona;
            txtDni.setText(String.valueOf(j.getDni()));
            txtNombreCompleto.setText(j.getNombre() != null ? j.getNombre() : "");
            txtEmail.setText(j.getEmail() != null ? j.getEmail() : "");
            txtPass.setText(j.getContrasenia() != null ? j.getContrasenia() : "");
            if (txtPosicion != null) {
                txtPosicion.setText(j.getPosicion() != null ? j.getPosicion() : "");
            }
            if (cmbCategoria != null) {
                if (j instanceof Capitan) {
                    cmbCategoria.setSelectedItem("Capitán");
                } else {
                    cmbCategoria.setSelectedItem("Jugador");
                }
            }
            if (cmbEquipo != null) {
                Equipo miEq = gestor.buscarEquipoDeJugador(j);
                if (miEq != null) {
                    cmbEquipo.setSelectedItem(miEq);
                } else {
                    cmbEquipo.setSelectedIndex(0);
                }
            }
        }
    }

    private void guardarCambios() {
        try {
            long dni = Long.parseLong(txtDni.getText().trim());
            String nombre = txtNombreCompleto.getText().trim();
            String email = txtEmail.getText().trim();
            String pass = new String(txtPass.getPassword()).trim();

            if (nombre.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Completá el Nombre y el Email.", "Campos Requeridos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String posicion = (txtPosicion != null) ? txtPosicion.getText().trim() : null;
            boolean esCap = (cmbCategoria != null && "Capitán".equals(cmbCategoria.getSelectedItem()));
            Equipo equipoSeleccionado = null;
            if (cmbEquipo != null && cmbEquipo.getSelectedItem() instanceof Equipo) {
                equipoSeleccionado = (Equipo) cmbEquipo.getSelectedItem();
            }

            String rta = gestor.modificarPerfilPersona(persona, dni, nombre, email, pass, posicion, esCap, equipoSeleccionado);
            JOptionPane.showMessageDialog(this, rta, "Actualización de Perfil", JOptionPane.INFORMATION_MESSAGE);

            if (rta.startsWith("OK:")) {
                if (callbackActualizacion != null) {
                    callbackActualizacion.run();
                }
                dispose();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresá un número de DNI válido.", "Dato Inválido", JOptionPane.ERROR_MESSAGE);
        }
    }
}
