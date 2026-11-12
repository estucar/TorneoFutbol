package vistas;

import controlador.GestorSistema;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class FormularioLogin extends JFrame {

    private final GestorSistema gestor;
    private JLabel lblLogo;
    private JTextField txtEmailLogin;
    private JPasswordField txtPassLogin;
    private JComboBox<String> cmbAccesoRapido;


    private JComboBox<String> cmbRolRegistro;
    private JTextField txtDniReg;
    private JTextField txtNombreReg;
    private JTextField txtPosicionReg;
    private JTextField txtEmailReg;
    private JPasswordField txtPassReg;
    private JLabel lblPosicion;

    public FormularioLogin() {
        this.gestor = GestorSistema.getInstancia();
        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setTitle("Sistema de Gestión de Torneo de Fútbol Amateur - UGD (Acceso y Registro)");
        setSize(880, 640);
        setMinimumSize(new Dimension(750, 550));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void inicializarComponentes() {
        JPanel panelHeader = new JPanel(new BorderLayout(15, 10));
        panelHeader.setBackground(UtilidadesImagen.VERDE_OSCURO);
        panelHeader.setBorder(new EmptyBorder(16, 22, 16, 22));

        lblLogo = new JLabel(UtilidadesImagen.cargarLogoSistema(82, 82));
        panelHeader.add(lblLogo, BorderLayout.WEST);

        JPanel panelTextos = new JPanel(new GridLayout(3, 1, 2, 2));
        panelTextos.setOpaque(false);
        JLabel lblTitulo = new JLabel("TORNEO DE FÚTBOL AMATEUR - UGD");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Iniciá sesión con tu cuenta o registrate según tu rol en el torneo");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblSub.setForeground(new Color(220, 235, 225));

        String estadoBD = gestor.isBdConectada()
                ? " Conectado a MySQL (torneo_futbol)"
                : "️ Modo Memoria Local (Verificá que MySQL esté corriendo en localhost:3306)";
        JLabel lblBD = new JLabel(estadoBD);
        lblBD.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblBD.setForeground(UtilidadesImagen.DORADO_ACENTO);

        panelTextos.add(lblTitulo);
        panelTextos.add(lblSub);
        panelTextos.add(lblBD);
        panelHeader.add(panelTextos, BorderLayout.CENTER);


        add(panelHeader, BorderLayout.NORTH);

        JTabbedPane pestañas = new JTabbedPane();
        pestañas.setFont(new Font("SansSerif", Font.BOLD, 14));
        pestañas.addTab(" Iniciar Sesión", crearPanelLogin());
        pestañas.addTab(" Registrarse (Crear Cuenta)", crearPanelRegistro(pestañas));

        add(pestañas, BorderLayout.CENTER);
    }

    private JPanel crearPanelLogin() {
        JPanel contenedor = new JPanel(new BorderLayout(20, 15));
        contenedor.setBackground(UtilidadesImagen.FONDO_CLARO);
        contenedor.setBorder(new EmptyBorder(20, 35, 20, 35));

        // Tarjeta central de login
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 205), 1),
                new EmptyBorder(20, 28, 20, 28)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblBienvenida = new JLabel("Ingresá tus credenciales o elegí una cuenta de prueba por rol:");
        lblBienvenida.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblBienvenida.setForeground(UtilidadesImagen.VERDE_OSCURO);
        card.add(lblBienvenida, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.3;
        card.add(new JLabel(" Autocompletar Rol:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbAccesoRapido = new JComboBox<>(new String[]{
            " Administrador (admin@ugd.edu.ar / admin123)",
            " Organizador (organizador@ugd.edu.ar / 1234)",
            " Árbitro - Horacio Elizondo (arbitro@ugd.edu.ar / 1234)"
        });
        cmbAccesoRapido.addActionListener(e -> autocompletarSegunCombo());
        card.add(cmbAccesoRapido, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        card.add(new JLabel("Correo Electrónico (Email):"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtEmailLogin = new JTextField("admin@ugd.edu.ar");
        txtEmailLogin.setFont(new Font("SansSerif", Font.PLAIN, 14));
        card.add(txtEmailLogin, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        card.add(new JLabel("Contraseña:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtPassLogin = new JPasswordField("admin123");
        txtPassLogin.setFont(new Font("SansSerif", Font.PLAIN, 14));
        card.add(txtPassLogin, gbc);

        // Botón de ingreso (Entrar sin cuenta / Público general REMOVIDO)
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelBotones.setOpaque(false);

        JButton btnIngresar = UtilidadesImagen.crearBotonEstilizado(" Iniciar Sesión", UtilidadesImagen.VERDE_CANCHA, Color.WHITE);
        btnIngresar.setPreferredSize(new Dimension(240, 42));
        btnIngresar.addActionListener(e -> ejecutarLogin());

        panelBotones.add(btnIngresar);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        card.add(panelBotones, gbc);

        // Resumen de permisos por rol abajo (Desktop: Administrador, Organizador y Árbitro)
        JTextArea txtInfoRoles = new JTextArea(
                "• Administrador: Control total de Usuarios, Torneos, Canchas y supervisión completa.\n" +
                "• Organizador: Registro de equipos y jugadores, evaluar inscripciones, programar partidos, sanciones y reportes.\n" +
                "• Árbitro: Cargar resultados de partidos, registrar incidentes, consultar fixture y aplicar sanciones.\n" +
                "• Nota Importante: Los perfiles de Capitán, Jugador y Público General acceden exclusivamente mediante la plataforma WEB."
        );
        txtInfoRoles.setEditable(false);
        txtInfoRoles.setBackground(new Color(235, 244, 238));
        txtInfoRoles.setFont(new Font("SansSerif", Font.PLAIN, 12));
        txtInfoRoles.setBorder(new EmptyBorder(10, 12, 10, 12));

        JScrollPane scrollCard = new JScrollPane(card);
        scrollCard.setBorder(null);
        contenedor.add(scrollCard, BorderLayout.CENTER);
        contenedor.add(txtInfoRoles, BorderLayout.SOUTH);
        return contenedor;
    }

    private void autocompletarSegunCombo() {
        int idx = cmbAccesoRapido.getSelectedIndex();
        switch (idx) {
            case 0:
                txtEmailLogin.setText("admin@ugd.edu.ar");
                txtPassLogin.setText("admin123");
                break;
            case 1:
                txtEmailLogin.setText("organizador@ugd.edu.ar");
                txtPassLogin.setText("1234");
                break;
            case 2:
                txtEmailLogin.setText("arbitro@ugd.edu.ar");
                txtPassLogin.setText("1234");
                break;
        }
    }

    private JPanel crearPanelRegistro(JTabbedPane pestañas) {
        JPanel contenedor = new JPanel(new BorderLayout(15, 15));
        contenedor.setBackground(UtilidadesImagen.FONDO_CLARO);
        contenedor.setBorder(new EmptyBorder(18, 35, 18, 35));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 205), 1),
                new EmptyBorder(18, 25, 18, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;

        int r = 0;


        gbc.gridx = 0; gbc.gridy = r; gbc.weightx = 0.35;
        form.add(new JLabel("Rol a Registrar (Desktop):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        cmbRolRegistro = new JComboBox<>(new String[]{"Administrador", "Organizador", "Árbitro"});
        form.add(cmbRolRegistro, gbc);
        r++;

        gbc.gridx = 0; gbc.gridy = r; gbc.weightx = 0.35;
        form.add(new JLabel("DNI (solo números):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtDniReg = new JTextField();
        form.add(txtDniReg, gbc);
        r++;


        gbc.gridx = 0; gbc.gridy = r; gbc.weightx = 0.35;
        form.add(new JLabel("Apellido y Nombre:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtNombreReg = new JTextField();
        form.add(txtNombreReg, gbc);
        r++;


        gbc.gridx = 0; gbc.gridy = r; gbc.weightx = 0.35;
        form.add(new JLabel("Correo Electrónico (Email):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtEmailReg = new JTextField();
        form.add(txtEmailReg, gbc);
        r++;

        gbc.gridx = 0; gbc.gridy = r; gbc.weightx = 0.35;
        form.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtPassReg = new JPasswordField();
        form.add(txtPassReg, gbc);
        r++;

        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 2;
        JLabel lblAclaracionReg = new JLabel("<html><i>* Nota: En Desktop podés registrar cuentas de Administrador, Organizador o Árbitro.<br>Los Jugadores y Capitanes son dados de alta directamente en sus equipos por el Administrador u Organizador.</i></html>");
        lblAclaracionReg.setForeground(new Color(80, 90, 85));
        lblAclaracionReg.setFont(new Font("SansSerif", Font.PLAIN, 12));
        form.add(lblAclaracionReg, gbc);
        r++;

        JButton btnRegistrar = UtilidadesImagen.crearBotonEstilizado(" Crear Cuenta", UtilidadesImagen.VERDE_CANCHA, Color.WHITE);
        btnRegistrar.addActionListener(e -> {
            try {
                String rol = (String) cmbRolRegistro.getSelectedItem();
                if (rol == null) rol = "Árbitro";
                long dni = Long.parseLong(txtDniReg.getText().trim());
                String apNom = txtNombreReg.getText().trim();
                String pos = "";
                String mail = txtEmailReg.getText().trim();
                String pass = new String(txtPassReg.getPassword()).trim();

                String rta = gestor.registrarNuevoUsuarioConEquipo(rol, dni, apNom, pos, mail, pass, null);
                JOptionPane.showMessageDialog(this, rta, "Registro de Cuenta", JOptionPane.INFORMATION_MESSAGE);
                if (rta.startsWith("OK:")) {
                    txtEmailLogin.setText(mail);
                    txtPassLogin.setText(pass);
                    pestañas.setSelectedIndex(0);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingresá un número de DNI válido (sin puntos).", "Dato inválido", JOptionPane.WARNING_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 2;
        form.add(btnRegistrar, gbc);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        contenedor.add(scroll, BorderLayout.CENTER);
        return contenedor;
    }

    private void ejecutarLogin() {
        String email = txtEmailLogin.getText().trim();
        String pass = new String(txtPassLogin.getPassword()).trim();

        if (gestor.iniciarSesion(email, pass)) {
            String rol = gestor.getRolActual();
            if ("JUGADOR".equals(rol) || "CAPITAN".equals(rol)) {
                gestor.cerrarSesion();
                JOptionPane.showMessageDialog(this,
                        "Acceso restringido en la versión Desktop:\n\n"
                        + "Los Jugadores y Capitanes utilizarán exclusivamente la plataforma WEB.\n"
                        + "Esta versión Desktop está destinada a Administradores, Organizadores y Árbitros.",
                        "Acceso Reservado para Web", JOptionPane.WARNING_MESSAGE);
                return;
            }
            abrirMenuPrincipal();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Credenciales incorrectas o usuario inactivo.\nVerificá el email y contraseña o registrate en la pestaña 'Registrarse'.",
                    "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirMenuPrincipal() {
        MenuPrincipal menu = new MenuPrincipal();
        menu.setVisible(true);
        this.dispose();
    }
}
