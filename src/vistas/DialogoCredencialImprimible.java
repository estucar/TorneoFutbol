package vistas;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import modelo.Capitan;
import modelo.Equipo;
import modelo.Jugador;

/*
 Diálogo visual que genera una credencial física oficial con código de barras
 y soporte de impresión literal mediante la API estándar java.awt.print.PrinterJob.
*/
public class DialogoCredencialImprimible extends JDialog {

    private final Jugador jugador;
    private final Equipo equipo;
    private final String nombreTorneo;
    private PanelCredencialGrafica panelCredencial;

    public DialogoCredencialImprimible(Window parent, Jugador jugador, Equipo equipo, String nombreTorneo) {
        super(parent, "Credencial Oficial Imprimible con Código de Barras Literal", ModalityType.APPLICATION_MODAL);
        this.jugador = jugador;
        this.equipo = equipo;
        this.nombreTorneo = (nombreTorneo != null && !nombreTorneo.isEmpty()) ? nombreTorneo : "Torneo Clausura UGD 2026";

        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setSize(650, 520);
        setMinimumSize(new Dimension(580, 480));
        setLocationRelativeTo(getParent());
        setResizable(true);
        setLayout(new BorderLayout(10, 10));
    }

    private void inicializarComponentes() {
        // Encabezado
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UtilidadesImagen.VERDE_OSCURO);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));
        JLabel lblTit = new JLabel("🪪 CREDENCIAL OFICIAL IMPRIMIBLE — LIGA UGD");
        lblTit.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblTit.setForeground(Color.WHITE);
        header.add(lblTit, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Centro: Panel con la credencial gráfica
        JPanel contenedorCentro = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        contenedorCentro.setBackground(new Color(235, 238, 240));

        panelCredencial = new PanelCredencialGrafica(jugador, equipo, nombreTorneo);
        contenedorCentro.add(panelCredencial);

        JScrollPane scroll = new JScrollPane(contenedorCentro);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // Barra inferior de botones de acción
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        panelAcciones.setBackground(Color.WHITE);

        JButton btnImprimir = UtilidadesImagen.crearBotonEstilizado("🖨️ Imprimir Credencial (Literal)", UtilidadesImagen.VERDE_CANCHA, Color.WHITE);
        btnImprimir.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnImprimir.addActionListener(e -> imprimirLiteral());

        JButton btnGuardarPng = UtilidadesImagen.crearBotonEstilizado("💾 Guardar como Imagen PNG", UtilidadesImagen.AZUL_ACCION, Color.WHITE);
        btnGuardarPng.addActionListener(e -> guardarComoImagen());

        JButton btnCerrar = UtilidadesImagen.crearBotonEstilizado("Cerrar", new Color(100, 105, 110), Color.WHITE);
        btnCerrar.addActionListener(e -> dispose());

        panelAcciones.add(btnGuardarPng);
        panelAcciones.add(btnImprimir);
        panelAcciones.add(btnCerrar);
        add(panelAcciones, BorderLayout.SOUTH);
    }

    /**
     * Imprime literalmente la credencial utilizando el subsistema nativo de impresión de Java (PrinterJob).
     */
    private void imprimirLiteral() {
        PrinterJob job = PrinterJob.getPrinterJob();
        String nomLimpio = (jugador.getNombre() != null) ? jugador.getNombre().replaceAll("[^a-zA-Z0-9]", "_") : "Jugador";
        job.setJobName("Credencial_UGD_" + nomLimpio);

        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                // Escalar suavemente para centrar la tarjeta en la hoja impresa
                double anchoTarjeta = panelCredencial.getWidth();
                double altoTarjeta = panelCredencial.getHeight();
                double escalaX = pageFormat.getImageableWidth() / anchoTarjeta;
                double escalaY = pageFormat.getImageableHeight() / altoTarjeta;
                double escalaFinal = Math.min(escalaX, escalaY);
                if (escalaFinal > 1.0) {
                    escalaFinal = 1.0; // Mantener tamaño real de credencial si entra en la hoja
                }
                g2d.scale(escalaFinal, escalaFinal);

                panelCredencial.printAll(g2d);
                return Printable.PAGE_EXISTS;
            }
        });

        // Diálogo nativo de impresión (selección de impresora física o 'Microsoft Print to PDF')
        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this,
                        "¡La credencial ha sido enviada exitosamente a la impresora!",
                        "Impresión Exitosa", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al imprimir la credencial: " + ex.getMessage(),
                        "Error de Impresión", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Guarda la credencial como imagen PNG en el disco.
     */
    private void guardarComoImagen() {
        try {
            int w = panelCredencial.getWidth();
            int h = panelCredencial.getHeight();
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            panelCredencial.paint(g2d);
            g2d.dispose();

            File carpeta = new File("credenciales_exportadas");
            if (!carpeta.exists()) carpeta.mkdirs();

            String nomLimpio = (jugador.getNombre() != null) ? jugador.getNombre().replaceAll("[^a-zA-Z0-9]", "_") : "Jugador";
            File archivoDestino = new File(carpeta, "credencial_" + jugador.getDni() + "_" + nomLimpio + ".png");
            ImageIO.write(img, "PNG", archivoDestino);

            JOptionPane.showMessageDialog(this,
                    "¡Credencial guardada como imagen con éxito!\nUbicación: " + archivoDestino.getAbsolutePath(),
                    "Credencial Guardada", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar imagen: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // COMPONENTE GRÁFICO DE LA TARJETA CREDENCIAL
    // =========================================================================
    public static class PanelCredencialGrafica extends JPanel {

        private final Jugador jugador;
        private final Equipo equipo;
        private final String nombreTorneo;
        private final String codigoBarras;

        public PanelCredencialGrafica(Jugador jugador, Equipo equipo, String nombreTorneo) {
            this.jugador = jugador;
            this.equipo = equipo;
            this.nombreTorneo = nombreTorneo;
            this.codigoBarras = (jugador.getCredencial() != null && jugador.getCredencial().getCodigoBarras() != null)
                    ? jugador.getCredencial().getCodigoBarras()
                    : "CRED-UGD-" + jugador.getDni() + "-2026";

            setPreferredSize(new Dimension(540, 320));
            setBackground(Color.WHITE);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(15, 76, 42), 3, true),
                    new EmptyBorder(10, 14, 10, 14)
            ));

            construirLayout();
        }

        private void construirLayout() {
            // Banda superior
            JPanel panelTop = new JPanel(new BorderLayout());
            panelTop.setBackground(new Color(15, 76, 42));
            panelTop.setBorder(new EmptyBorder(6, 12, 6, 12));

            JLabel lblTorneo = new JLabel("🏆 " + nombreTorneo.toUpperCase());
            lblTorneo.setFont(new Font("SansSerif", Font.BOLD, 13));
            lblTorneo.setForeground(Color.WHITE);

            JLabel lblHabilitado = new JLabel("OFICIAL — " + (jugador.tieneSancionVigente() ? "SUSPENDIDO" : "HABILITADO"));
            lblHabilitado.setFont(new Font("SansSerif", Font.BOLD, 12));
            lblHabilitado.setForeground(jugador.tieneSancionVigente() ? new Color(255, 120, 120) : new Color(255, 215, 0));

            panelTop.add(lblTorneo, BorderLayout.WEST);
            panelTop.add(lblHabilitado, BorderLayout.EAST);
            add(panelTop, BorderLayout.NORTH);

            // Centro: Escudo a la izquierda + Datos a la derecha
            JPanel panelDatos = new JPanel(new BorderLayout(14, 0));
            panelDatos.setOpaque(false);
            panelDatos.setBorder(new EmptyBorder(10, 8, 8, 8));

            // Escudo del equipo o sigla
            JLabel lblFoto = new JLabel(UtilidadesImagen.cargarEscudoEquipo(equipo, 85, 85));
            lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
            lblFoto.setBorder(BorderFactory.createLineBorder(new Color(200, 205, 210), 1));
            lblFoto.setPreferredSize(new Dimension(95, 95));
            panelDatos.add(lblFoto, BorderLayout.WEST);

            // Datos del jugador
            JPanel panelTextos = new JPanel(new GridLayout(5, 1, 2, 2));
            panelTextos.setOpaque(false);

            String rolCategoria = (jugador instanceof Capitan) ? "CAPITÁN TITULAR ©️" : "JUGADOR DE CAMPO";
            JLabel lblNombre = new JLabel(jugador.getNombre() != null ? jugador.getNombre().toUpperCase() : "-");
            lblNombre.setFont(new Font("SansSerif", Font.BOLD, 16));
            lblNombre.setForeground(new Color(20, 30, 40));

            JLabel lblDni = new JLabel("DNI: " + jugador.getDni() + "   |   Posición: " + (jugador.getPosicion() != null ? jugador.getPosicion() : "Polifuncional"));
            lblDni.setFont(new Font("SansSerif", Font.PLAIN, 12));

            String nomEq = (equipo != null) ? equipo.getNombre() : "UGD Fútbol Amateur";
            JLabel lblEquipo = new JLabel("Equipo: " + nomEq + " (" + rolCategoria + ")");
            lblEquipo.setFont(new Font("SansSerif", Font.BOLD, 12));
            lblEquipo.setForeground(new Color(15, 76, 42));

            JLabel lblGoles = new JLabel("Goles convertidos: " + jugador.getGoles() + "   |   Estado: " + jugador.getEstado());
            lblGoles.setFont(new Font("SansSerif", Font.PLAIN, 12));

            panelTextos.add(lblNombre);
            panelTextos.add(lblDni);
            panelTextos.add(lblEquipo);
            panelTextos.add(lblGoles);
            panelDatos.add(panelTextos, BorderLayout.CENTER);

            add(panelDatos, BorderLayout.CENTER);

            // Pie: Código de barras gráfico literal
            PanelCodigoBarrasGrafico panelBarcode = new PanelCodigoBarrasGrafico(codigoBarras);
            add(panelBarcode, BorderLayout.SOUTH);
        }
    }

    // =========================================================================
    // DIBUJO DEL CÓDIGO DE BARRAS LITERAL (GRÁFICO REAL CON BARRAS VERTICALES)
    // =========================================================================
    public static class PanelCodigoBarrasGrafico extends JPanel {

        private final String textoCodigo;

        public PanelCodigoBarrasGrafico(String textoCodigo) {
            this.textoCodigo = (textoCodigo != null && !textoCodigo.isEmpty()) ? textoCodigo : "CRED-UGD-123456";
            setPreferredSize(new Dimension(500, 85));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Fondo blanco nítido
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, width, height);

            // Borde sutil del área de código
            g2.setColor(new Color(225, 230, 235));
            g2.drawRect(8, 4, width - 16, height - 8);

            // Generador determinístico de barras basado en los caracteres del código
            // Produce barras verticales de distintos anchos (1px, 2px, 3px, 4px) con espacios proporcionales
            int barHeight = 44;
            int startY = 12;

            // Ancho total estimado de barras para centrar en el panel
            int totalBarsWidth = calcularAnchoTotalBarras(textoCodigo);
            int startX = Math.max(20, (width - totalBarsWidth) / 2);

            int curX = startX;

            // Barras de inicio de seguridad (Guard Pattern Code 128 / Code 39)
            g2.setColor(Color.BLACK);
            g2.fillRect(curX, startY, 3, barHeight); curX += 5;
            g2.fillRect(curX, startY, 2, barHeight); curX += 4;
            g2.fillRect(curX, startY, 3, barHeight); curX += 6;

            // Dibujar patrón de barras por cada carácter del código
            byte[] bytes = textoCodigo.getBytes();
            for (int i = 0; i < bytes.length; i++) {
                int b = bytes[i];
                // Generar 3 barras por carácter con anchos basados en los bits
                int w1 = (b % 3 == 0) ? 3 : (b % 2 == 0 ? 2 : 1);
                int s1 = ((b >> 1) % 2 == 0) ? 2 : 3;
                int w2 = ((b >> 2) % 3 == 0) ? 3 : 2;
                int s2 = ((b >> 3) % 2 == 0) ? 2 : 3;
                int w3 = (b % 4 == 0) ? 4 : 2;
                int s3 = 2;

                g2.fillRect(curX, startY, w1, barHeight); curX += w1 + s1;
                g2.fillRect(curX, startY, w2, barHeight); curX += w2 + s2;
                g2.fillRect(curX, startY, w3, barHeight); curX += w3 + s3;
            }

            // Barras de fin de seguridad
            g2.fillRect(curX, startY, 3, barHeight); curX += 5;
            g2.fillRect(curX, startY, 2, barHeight); curX += 4;
            g2.fillRect(curX, startY, 4, barHeight);

            // Texto numérico/alfanumérico debajo del código de barras
            g2.setFont(new Font("Monospaced", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            String label = "* " + textoCodigo + " *";
            int textWidth = fm.stringWidth(label);
            int textX = (width - textWidth) / 2;
            int textY = startY + barHeight + 16;

            g2.setColor(new Color(30, 35, 40));
            g2.drawString(label, textX, textY);
        }

        private int calcularAnchoTotalBarras(String codigo) {
            int len = codigo != null ? codigo.length() : 10;
            // ~14 píxeles por carácter + barras de inicio/fin (~40px)
            return len * 15 + 40;
        }
    }
}
