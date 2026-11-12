package vistas;

import controlador.GestorSistema;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import modelo.Cancha;

public class VentanaCanchas extends JDialog {

    private final GestorSistema gestor;
    private DefaultTableModel modeloTabla;
    private JTable tabla;
    private JTextField txtNombre, txtUbicacion;
    private JCheckBox chkDisponible;

    public VentanaCanchas(Frame parent) {
        super(parent, "Gestión de Canchas y Sedes (ABM)", true);
        this.gestor = GestorSistema.getInstancia();
        setSize(880, 540);
        setMinimumSize(new Dimension(750, 480));
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
        JLabel lblTit = new JLabel("🏟️ GESTIÓN DE CANCHAS DEL TORNEO");
        lblTit.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTit.setForeground(Color.WHITE);
        header.add(lblTit, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(6, 1, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Datos de la Cancha"));
        form.setPreferredSize(new Dimension(300, 0));

        form.add(new JLabel("Nombre de la Cancha:"));
        txtNombre = new JTextField();
        form.add(txtNombre);

        form.add(new JLabel("Ubicación / Dirección:"));
        txtUbicacion = new JTextField();
        form.add(txtUbicacion);

        chkDisponible = new JCheckBox("Cancha Disponible / Habilitada", true);
        form.add(chkDisponible);

        JButton btnAlta = UtilidadesImagen.crearBotonEstilizado("➕ Guardar Nueva Cancha", UtilidadesImagen.VERDE_CANCHA, Color.WHITE);
        btnAlta.addActionListener(e -> {
            if (txtNombre.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingresá el nombre de la cancha.");
                return;
            }
            String rta = gestor.altaCancha(txtNombre.getText().trim(), txtUbicacion.getText().trim(), chkDisponible.isSelected());
            JOptionPane.showMessageDialog(this, rta);
            txtNombre.setText("");
            txtUbicacion.setText("");
            cargarTabla();
        });
        form.add(btnAlta);

        JScrollPane scrollForm = new JScrollPane(form);
        scrollForm.setBorder(null);
        add(scrollForm, BorderLayout.WEST);

        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Nombre de Cancha", "Ubicación", "Disponible"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(26);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int f = tabla.getSelectedRow();
            if (f >= 0 && f < gestor.getCanchas().size()) {
                Cancha c = gestor.getCanchas().get(f);
                txtNombre.setText(c.getNombre());
                txtUbicacion.setText(c.getUbicacion());
                chkDisponible.setSelected(c.isDisponible());
            }
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnMod = UtilidadesImagen.crearBotonEstilizado("️ Modificar Seleccionada", UtilidadesImagen.AZUL_ACCION, Color.WHITE);
        btnMod.addActionListener(e -> {
            int f = tabla.getSelectedRow();
            if (f < 0) return;
            Cancha c = gestor.getCanchas().get(f);
            String rta = gestor.modificarCancha(c, txtNombre.getText().trim(), txtUbicacion.getText().trim(), chkDisponible.isSelected());
            JOptionPane.showMessageDialog(this, rta);
            cargarTabla();
        });

        JButton btnBaja = UtilidadesImagen.crearBotonEstilizado(" Dar de Baja Cancha", new Color(185, 28, 28), Color.WHITE);
        btnBaja.addActionListener(e -> {
            int f = tabla.getSelectedRow();
            if (f < 0) return;
            Cancha c = gestor.getCanchas().get(f);
            JOptionPane.showMessageDialog(this, gestor.bajaCancha(c));
            cargarTabla();
        });

        JButton btnCerrar = UtilidadesImagen.crearBotonEstilizado("Cerrar", new Color(90, 95, 100), Color.WHITE);
        btnCerrar.addActionListener(e -> dispose());

        sur.add(btnMod);
        sur.add(btnBaja);
        sur.add(btnCerrar);
        add(sur, BorderLayout.SOUTH);
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        for (Cancha c : gestor.getCanchas()) {
            modeloTabla.addRow(new Object[]{
                c.getId(), c.getNombre(), c.getUbicacion(), c.isDisponible() ? "Sí (Disponible)" : "No (De Baja)"
            });
        }
    }
}
