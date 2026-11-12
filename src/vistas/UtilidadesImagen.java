package vistas;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import modelo.Equipo;

public class UtilidadesImagen {

    public static final Color VERDE_OSCURO = new Color(15, 76, 42);
    public static final Color VERDE_CANCHA = new Color(26, 115, 64);
    public static final Color DORADO_ACENTO = new Color(212, 160, 23);
    public static final Color FONDO_CLARO = new Color(244, 247, 245);
    public static final Color AZUL_ACCION = new Color(25, 103, 178);

    public static File obtenerCarpetaImagenes() {
        File dir = new File("imagenes");
        if (dir.exists() && dir.isDirectory()) {
            return dir;
        }
        File f1 = new File("corregido" + File.separator + "imagenes");
        if (f1.exists() && f1.isDirectory()) {
            return f1;
        }
        File f2 = new File(".." + File.separator + "imagenes");
        if (f2.exists() && f2.isDirectory()) {
            return f2;
        }
        File f3 = new File("TorneoDeFutbol" + File.separator + "imagenes");
        if (f3.exists() && f3.isDirectory()) {
            return f3;
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static ImageIcon cargarLogoSistema(int ancho, int alto) {
        try {
            File dir = obtenerCarpetaImagenes();
            File archivo = new File(dir, "logo_torneo.png");
            if (archivo.exists()) {
                BufferedImage img = ImageIO.read(archivo);
                if (img != null) {
                    return new ImageIcon(img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH));
                }
            }
        } catch (Exception ignored) {
        }
        return crearIconoRespaldo("UGD", ancho, alto);
    }

    public static ImageIcon cargarImagenEscalada(String nombreArchivo, int ancho, int alto) {
        try {
            File dir = obtenerCarpetaImagenes();
            if ("logo_torneo.png".equals(nombreArchivo)) {
                File custom = new File(dir, "logo_personalizado.png");
                if (custom.exists()) {
                    BufferedImage img = ImageIO.read(custom);
                    if (img != null) {
                        return new ImageIcon(img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH));
                    }
                }
            }
            File archivo = new File(dir, nombreArchivo);
            if (archivo.exists()) {
                BufferedImage img = ImageIO.read(archivo);
                if (img != null) {
                    return new ImageIcon(img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH));
                }
            }
        } catch (Exception ignored) {
        }
        return crearIconoRespaldo("UGD", ancho, alto);
    }

    public static ImageIcon cargarEscudoEquipo(Equipo eq, int ancho, int alto) {
        if (eq != null) {
            File dir = obtenerCarpetaImagenes();
            // 1. Escudo propio personalizado
            if (eq.getEscudo() != null && !eq.getEscudo().trim().isEmpty()) {
                File arch = new File(dir, eq.getEscudo().trim());
                if (arch.exists()) {
                    try {
                        BufferedImage img = ImageIO.read(arch);
                        if (img != null) {
                            return new ImageIcon(img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH));
                        }
                    } catch (Exception ignored) {}
                }
            }
            // 2. Si no tiene escudo propio, buscar por id o nombre
            String nombreSugerido = "escudo_eq_" + eq.getId() + ".png";
            File archSugerido = new File(dir, nombreSugerido);
            if (archSugerido.exists()) {
                try {
                    BufferedImage img = ImageIO.read(archSugerido);
                    if (img != null) {
                        return new ImageIcon(img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH));
                    }
                } catch (Exception ignored) {}
            }
        }
        // 3. Si el equipo no tiene imagen propia, devolver null para que no aparezca ninguna foto por defecto
        return null;
    }

    public static boolean seleccionarYGuardarEscudoEquipo(Component parent, Equipo eq) {
        if (eq == null) {
            JOptionPane.showMessageDialog(parent, "Seleccioná primero un equipo.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccioná el escudo para '" + eq.getNombre() + "' (.png, .jpg, .jpeg)");
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes (PNG, JPG, JPEG, GIF)", "png", "jpg", "jpeg", "gif"));
        int res = chooser.showOpenDialog(parent);
        if (res == JFileChooser.APPROVE_OPTION) {
            File seleccionada = chooser.getSelectedFile();
            try {
                File dir = obtenerCarpetaImagenes();
                String ext = "png";
                String origName = seleccionada.getName();
                int dot = origName.lastIndexOf('.');
                if (dot > 0) ext = origName.substring(dot + 1).toLowerCase();

                String nombreDestino = "escudo_eq_" + (eq.getId() > 0 ? eq.getId() : eq.getNombre().replaceAll("[^a-zA-Z0-9]", "_")) + "." + ext;
                File destino = new File(dir, nombreDestino);
                Files.copy(seleccionada.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                eq.setEscudo(nombreDestino);

                JOptionPane.showMessageDialog(parent,
                        "¡Escudo de '" + eq.getNombre() + "' guardado y aplicado con éxito!\nGuardado como: " + nombreDestino,
                        "Escudo Actualizado", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent,
                        "Error al subir el escudo: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    public static String copiarEscudoArchivo(Component parent, File seleccionada, String nombreEquipo) {
        if (seleccionada == null || !seleccionada.exists()) return null;
        try {
            File dir = obtenerCarpetaImagenes();
            String ext = "png";
            String origName = seleccionada.getName();
            int dot = origName.lastIndexOf('.');
            if (dot > 0) ext = origName.substring(dot + 1).toLowerCase();

            String slug = (nombreEquipo != null && !nombreEquipo.trim().isEmpty())
                    ? nombreEquipo.trim().replaceAll("[^a-zA-Z0-9]", "_")
                    : "nuevo_equipo";
            String nombreDestino = "escudo_" + slug + "_" + System.currentTimeMillis() + "." + ext;
            File destino = new File(dir, nombreDestino);
            Files.copy(seleccionada.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return nombreDestino;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "Error al copiar el escudo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static boolean seleccionarYGuardarImagen(Component parent, String nombreDestino) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccioná una imagen descargada en tu PC (.png, .jpg, .jpeg)");
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes (PNG, JPG, JPEG, GIF)", "png", "jpg", "jpeg", "gif"));
        int res = chooser.showOpenDialog(parent);
        if (res == JFileChooser.APPROVE_OPTION) {
            File seleccionada = chooser.getSelectedFile();
            try {
                File destino = new File(obtenerCarpetaImagenes(), nombreDestino);
                Files.copy(seleccionada.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(parent,
                        "¡Imagen guardada en 'imagenes/" + nombreDestino + "' con éxito!",
                        "Imagen Actualizada", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent,
                        "Error al copiar la imagen: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    private static ImageIcon crearIconoRespaldo(String texto, int ancho, int alto) {
        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(VERDE_CANCHA);
        g2.fillOval(2, 2, ancho - 4, alto - 4);
        g2.setColor(DORADO_ACENTO);
        g2.setStroke(new BasicStroke(3f));
        g2.drawOval(2, 2, ancho - 4, alto - 4);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, ancho / 3)));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(texto, (ancho - fm.stringWidth(texto)) / 2, (alto + fm.getAscent()) / 2 - 2);
        g2.dispose();
        return new ImageIcon(img);
    }

    public static JButton crearBotonEstilizado(String texto, Color colorFondo, Color colorTexto) {
        JButton btn = new JButton(texto);
        btn.setBackground(colorFondo);
        btn.setForeground(colorTexto);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorFondo.darker(), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
