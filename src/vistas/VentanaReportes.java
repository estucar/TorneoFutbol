package vistas;

import controlador.GestorSistema;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import modelo.*;

public class VentanaReportes extends JDialog {

    private final GestorSistema gestor;
    private DefaultTableModel modeloPosiciones, modeloGoleadores, modeloSancionados;
    private JTextArea txtArbitrosYHistorial;
    private JComboBox<Equipo> cmbEquipoHist;
    private JComboBox<Jugador> cmbJugadorHist;

    public VentanaReportes(Frame parent) {
        super(parent, "Estadísticas Oficiales, Historiales y Exportación PDF (iText)", true);
        this.gestor = GestorSistema.getInstancia();
        setSize(1060, 680);
        setMinimumSize(new Dimension(860, 520));
        setLocationRelativeTo(parent);
        setResizable(true);
        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
        calcularYMostrarTodo();
    }

    private void inicializarComponentes() {
        String rol = gestor.getRolActual();

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UtilidadesImagen.VERDE_OSCURO);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));
        JLabel lblTit = new JLabel(" REPORTES OFICIALES, HISTORIALES Y POSICIONES [" + rol + "]");
        lblTit.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTit.setForeground(Color.WHITE);
        header.add(lblTit, BorderLayout.WEST);

        JButton btnExportarTodos = UtilidadesImagen.crearBotonEstilizado("Exportar Reportes a PDF", UtilidadesImagen.DORADO_ACENTO, Color.BLACK);
        btnExportarTodos.addActionListener(e -> exportarTodosLosPDFs());
        header.add(btnExportarTodos, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));

        // Tab 1: Tabla de Posiciones
        modeloPosiciones = new DefaultTableModel(new Object[]{"Pos", "Equipo", "PJ", "PG", "PE", "PP", "GF", "GC", "DG", "Puntos"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tPos = new JTable(modeloPosiciones);
        tPos.setRowHeight(26);
        tabs.addTab("Tabla de Posiciones", new JScrollPane(tPos));

        // Tab 2: Tabla de Goleadores
        modeloGoleadores = new DefaultTableModel(new Object[]{"Puesto", "Jugador", "DNI", "Posición", "Goles Convertidos"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tGol = new JTable(modeloGoleadores);
        tGol.setRowHeight(26);
        tabs.addTab("Tabla de Goleadores", new JScrollPane(tGol));

        // Tab 3: Jugadores Más Sancionados
        modeloSancionados = new DefaultTableModel(new Object[]{"Jugador", "DNI", "Estado Actual", "Cantidad de Sanciones"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tSan = new JTable(modeloSancionados);
        tSan.setRowHeight(26);
        tabs.addTab(" Sancionados", new JScrollPane(tSan));

        // Tab 4: Reporte de Árbitros e Historial
        JPanel panelHist = new JPanel(new BorderLayout(10, 10));
        panelHist.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel barraConsultas = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        barraConsultas.add(new JLabel("Equipo:"));
        cmbEquipoHist = new JComboBox<>();
        barraConsultas.add(cmbEquipoHist);

        JButton btnHistEq = UtilidadesImagen.crearBotonEstilizado("Consultar Historial Equipo", UtilidadesImagen.VERDE_CANCHA, Color.WHITE);
        btnHistEq.addActionListener(e -> mostrarHistorialEquipo());
        barraConsultas.add(btnHistEq);

        barraConsultas.add(new JLabel("Jugador:"));
        cmbJugadorHist = new JComboBox<>();
        barraConsultas.add(cmbJugadorHist);

        JButton btnHistJug = UtilidadesImagen.crearBotonEstilizado("Consultar Historial Jugador", UtilidadesImagen.AZUL_ACCION, Color.WHITE);
        btnHistJug.addActionListener(e -> mostrarHistorialJugador());
        barraConsultas.add(btnHistJug);

        panelHist.add(barraConsultas, BorderLayout.NORTH);

        txtArbitrosYHistorial = new JTextArea();
        txtArbitrosYHistorial.setEditable(false);
        txtArbitrosYHistorial.setFont(new Font("Monospaced", Font.PLAIN, 13));
        panelHist.add(new JScrollPane(txtArbitrosYHistorial), BorderLayout.CENTER);

        tabs.addTab("Reporte de Árbitros e Historiales", panelHist);

        add(tabs, BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        JButton btnCerrar = UtilidadesImagen.crearBotonEstilizado("Cerrar", new Color(90, 95, 100), Color.WHITE);
        btnCerrar.addActionListener(e -> dispose());
        panelSur.add(btnCerrar);
        add(panelSur, BorderLayout.SOUTH);
    }

    private void calcularYMostrarTodo() {
        if (gestor.getTorneos().isEmpty()) return;
        Torneo t = gestor.getTorneos().get(0);
        String rol = gestor.getRolActual();
        boolean esJugador = "JUGADOR".equals(rol);
        Jugador yo = gestor.getJugadorActual();
        Equipo miEquipo = gestor.buscarEquipoDeJugador(yo);

        // 1. Posiciones (Requisito 19 & 20: si es jugador, solo su equipo)
        modeloPosiciones.setRowCount(0);
        List<EstadisticaEquipo> tabla = t.calcularTablaPosiciones(gestor.getPartidos());
        int pos = 1;
        for (EstadisticaEquipo est : tabla) {
            if (esJugador && miEquipo != null) {
                if (est.getEquipo() != null && est.getEquipo().equals(miEquipo)) {
                    modeloPosiciones.addRow(new Object[]{
                        pos, est.getEquipo().getNombre(), est.getPj(), est.getPg(), est.getPe(),
                        est.getPp(), est.getGf(), est.getGc(), est.getDg(), est.getPuntos()
                    });
                }
            } else {
                modeloPosiciones.addRow(new Object[]{
                    pos, est.getEquipo().getNombre(), est.getPj(), est.getPg(), est.getPe(),
                    est.getPp(), est.getGf(), est.getGc(), est.getDg(), est.getPuntos()
                });
            }
            pos++;
        }

        // 2. Goleadores (Requisito 19 & 20: si es jugador, solo él)
        modeloGoleadores.setRowCount(0);
        List<Jugador> goleadores = t.calcularGoleadores(gestor.getPartidos());
        int puesto = 1;
        for (Jugador j : goleadores) {
            if (esJugador && yo != null) {
                if (j.equals(yo) || j.getDni() == yo.getDni()) {
                    modeloGoleadores.addRow(new Object[]{
                        puesto, j.getNombre(), j.getDni(), j.getPosicion(), j.getGoles()
                    });
                }
            } else {
                modeloGoleadores.addRow(new Object[]{
                    puesto, j.getNombre(), j.getDni(), j.getPosicion(), j.getGoles()
                });
            }
            puesto++;
        }
        if (esJugador && modeloGoleadores.getRowCount() == 0 && yo != null) {
            modeloGoleadores.addRow(new Object[]{1, yo.getNombre(), yo.getDni(), yo.getPosicion(), yo.getGoles()});
        }

        // 3. Sancionados (Requisito 19 & 20: si es jugador, solo él)
        modeloSancionados.setRowCount(0);
        List<Jugador> sancionados = t.generarReporteJugadoresMasSancionados(gestor.getEquipos());
        for (Jugador j : sancionados) {
            if (esJugador && yo != null) {
                if (j.equals(yo) || j.getDni() == yo.getDni()) {
                    modeloSancionados.addRow(new Object[]{
                        j.getNombre(), j.getDni(), j.getEstado(), j.getSanciones().size()
                    });
                }
            } else {
                modeloSancionados.addRow(new Object[]{
                    j.getNombre(), j.getDni(), j.getEstado(), j.getSanciones().size()
                });
            }
        }

        // 4. Combos e informe arbitral (Requisito 19 & 20)
        cmbEquipoHist.removeAllItems();
        cmbJugadorHist.removeAllItems();

        if (esJugador) {
            if (miEquipo != null) {
                cmbEquipoHist.addItem(miEquipo);
            }
            if (yo != null) {
                cmbJugadorHist.addItem(yo);
            }

            // Reporte arbitral filtrado a los partidos de su equipo
            List<Partido> partidosEquipo = new ArrayList<>();
            for (Partido p : gestor.getPartidos()) {
                if (miEquipo != null && (miEquipo.equals(p.getEquipoLocal()) || miEquipo.equals(p.getEquipoVisitante()))) {
                    partidosEquipo.add(p);
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("=== REPORTE DE ÁRBITROS PARA MI EQUIPO: ").append(miEquipo != null ? miEquipo.getNombre() : "Sin equipo").append(" ===\n\n");
            sb.append(t.generarReporteArbitros(partidosEquipo));
            txtArbitrosYHistorial.setText(sb.toString());

        } else {
            for (Equipo e : gestor.getEquipos()) cmbEquipoHist.addItem(e);
            for (Jugador j : gestor.getJugadores()) cmbJugadorHist.addItem(j);
            txtArbitrosYHistorial.setText(t.generarReporteArbitros(gestor.getPartidos()));
        }
    }

    private void mostrarHistorialEquipo() {
        Equipo eq = (Equipo) cmbEquipoHist.getSelectedItem();
        if (eq == null) return;
        List<Torneo> hist = eq.consultarHistorialTorneos(gestor.getTorneos());
        StringBuilder sb = new StringBuilder("=== HISTORIAL DE TORNEOS DEL EQUIPO: " + eq.getNombre() + " ===\n");
        sb.append(eq.verPerfil()).append("\n\n");
        for (Torneo t : hist) {
            sb.append("• Torneo: ").append(t.getNombre()).append(" | Fase actual: ").append(t.getFaseActual()).append("\n");
        }
        txtArbitrosYHistorial.setText(sb.toString());
    }

    private void mostrarHistorialJugador() {
        Jugador j = (Jugador) cmbJugadorHist.getSelectedItem();
        if (j == null) return;
        List<Torneo> hist = j.consultarHistorialTorneos(gestor.getTorneos());
        StringBuilder sb = new StringBuilder("=== HISTORIAL DE TORNEOS DEL JUGADOR: " + j.getNombre() + " ===\n");
        sb.append(j.verPerfil()).append("\n\n");
        for (Torneo t : hist) {
            sb.append("• Torneo: ").append(t.getNombre()).append(" | Fase actual: ").append(t.getFaseActual()).append("\n");
        }
        txtArbitrosYHistorial.setText(sb.toString());
    }

    private void exportarTodosLosPDFs() {
        if (gestor.getTorneos().isEmpty()) return;
        Torneo t = gestor.getTorneos().get(0);
        Equipo eq = !gestor.getEquipos().isEmpty() ? gestor.getEquipos().get(0) : null;
        Jugador jug = !gestor.getJugadores().isEmpty() ? gestor.getJugadores().get(0) : null;

        boolean ok1 = gestor.getServicioPDF().exportarTablaPosicionesPDF(t, gestor.getPartidos(), "1_TablaPosiciones_UGD.pdf");
        boolean ok2 = gestor.getServicioPDF().exportarReporteGoleadoresPDF(t, gestor.getPartidos(), "2_Goleadores_UGD.pdf");
        boolean ok3 = gestor.getServicioPDF().exportarReporteSancionadosPDF(t, gestor.getEquipos(), "3_JugadoresMasSancionados_UGD.pdf");
        boolean ok4 = gestor.getServicioPDF().exportarReporteArbitrosPDF(t, gestor.getPartidos(), "4_ReporteArbitros_UGD.pdf");
        boolean ok5 = (eq != null) && gestor.getServicioPDF().exportarHistorialEquipoPDF(eq, gestor.getTorneos(), "5_Historial_Equipo_UGD.pdf");
        boolean ok6 = (jug != null) && gestor.getServicioPDF().exportarHistorialJugadorPDF(jug, gestor.getTorneos(), "6_Historial_Jugador_UGD.pdf");

        File carpeta = new File(".");
        JOptionPane.showMessageDialog(this,
                "¡Reportes PDF generados en la carpeta del proyecto!\n\n" +
                "• 1_TablaPosiciones_UGD.pdf (" + (ok1 ? "OK" : "Error") + ")\n" +
                "• 2_Goleadores_UGD.pdf (" + (ok2 ? "OK" : "Error") + ")\n" +
                "• 3_JugadoresMasSancionados_UGD.pdf (" + (ok3 ? "OK" : "Error") + ")\n" +
                "• 4_ReporteArbitros_UGD.pdf (" + (ok4 ? "OK" : "Error") + ")\n" +
                "• 5_Historial_Equipo_UGD.pdf (" + (ok5 ? "OK" : "Error") + ")\n" +
                "• 6_Historial_Jugador_UGD.pdf (" + (ok6 ? "OK" : "Error") + ")\n\n" +
                "Ruta: " + carpeta.getAbsolutePath(),
                "Exportación PDF Completada", JOptionPane.INFORMATION_MESSAGE);
    }
}
