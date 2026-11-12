package servicios;

import modelo.Arbitro;
import modelo.Equipo;
import modelo.EstadisticaEquipo;
import modelo.Jugador;
import modelo.Partido;
import modelo.Torneo;

public class ServicioPDF {

    public boolean exportarTablaPosicionesPDF(Torneo torneo, java.util.List<Partido> partidos, String rutaDestino) {
        java.util.List<EstadisticaEquipo> tabla = torneo.calcularTablaPosiciones(partidos);
        try {
            com.itextpdf.text.Document documento = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(rutaDestino));
            documento.open();
            
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("TABLA DE POSICIONES - " + torneo.getNombre());
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            documento.add(titulo);
            
            com.itextpdf.text.pdf.PdfPTable tablaPdf = new com.itextpdf.text.pdf.PdfPTable(4);
            tablaPdf.addCell("Equipo");
            tablaPdf.addCell("PJ");
            tablaPdf.addCell("DG");
            tablaPdf.addCell("Pts");
            
            for (EstadisticaEquipo est : tabla) {
                tablaPdf.addCell(est.getEquipo().getNombre());
                tablaPdf.addCell(String.valueOf(est.getPj()));
                tablaPdf.addCell(String.valueOf(est.getDg()));
                tablaPdf.addCell(String.valueOf(est.getPuntos()));
            }
            
            documento.add(tablaPdf);
            documento.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean exportarReporteGoleadoresPDF(Torneo torneo, java.util.List<Partido> partidos, String rutaDestino) {
        java.util.List<Jugador> goleadores = torneo.calcularGoleadores(partidos);
        try {
            com.itextpdf.text.Document documento = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(rutaDestino));
            documento.open();
            
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("REPORTE DE GOLEADORES - " + torneo.getNombre());
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            documento.add(titulo);
            
            com.itextpdf.text.pdf.PdfPTable tablaPdf = new com.itextpdf.text.pdf.PdfPTable(3);
            tablaPdf.addCell("Jugador");
            tablaPdf.addCell("DNI");
            tablaPdf.addCell("Goles");
            
            for (Jugador j : goleadores) {
                tablaPdf.addCell(j.getNombre());
                tablaPdf.addCell(String.valueOf(j.getDni()));
                tablaPdf.addCell(String.valueOf(j.getGoles()));
            }
            
            documento.add(tablaPdf);
            documento.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean exportarReporteSancionadosPDF(Torneo torneo, java.util.List<Equipo> equipos, String rutaDestino) {
        java.util.List<Jugador> sancionados = torneo.generarReporteJugadoresMasSancionados(equipos);
        try {
            com.itextpdf.text.Document documento = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(rutaDestino));
            documento.open();
            
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("JUGADORES MAS SANCIONADOS - " + torneo.getNombre());
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            documento.add(titulo);
            
            com.itextpdf.text.pdf.PdfPTable tablaPdf = new com.itextpdf.text.pdf.PdfPTable(3);
            tablaPdf.addCell("Jugador");
            tablaPdf.addCell("DNI");
            tablaPdf.addCell("Cant. Sanciones");
            
            for (Jugador j : sancionados) {
                tablaPdf.addCell(j.getNombre());
                tablaPdf.addCell(String.valueOf(j.getDni()));
                tablaPdf.addCell(String.valueOf(j.getSanciones().size()));
            }
            
            documento.add(tablaPdf);
            documento.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean exportarReporteArbitrosPDF(Torneo torneo, java.util.List<Partido> partidos, String rutaDestino) {
        try {
            com.itextpdf.text.Document documento = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(rutaDestino));
            documento.open();
            
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("REPORTE DE ARBITROS - " + torneo.getNombre());
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            documento.add(titulo);
            
            java.util.Map<Arbitro, Integer> conteoArbitros = new java.util.HashMap<>();
            if (partidos != null) {
                for (Partido p : partidos) {
                    if (p.getArbitro() != null) {
                        Arbitro a = p.getArbitro();
                        conteoArbitros.put(a, conteoArbitros.getOrDefault(a, 0) + 1);
                    }
                }
            }

            com.itextpdf.text.pdf.PdfPTable tablaPdf = new com.itextpdf.text.pdf.PdfPTable(3);
            tablaPdf.addCell("Matricula");
            tablaPdf.addCell("Nombre");
            tablaPdf.addCell("Partidos Dirigidos");
            
            for (java.util.Map.Entry<Arbitro, Integer> entry : conteoArbitros.entrySet()) {
                tablaPdf.addCell(String.valueOf(entry.getKey().getMatricula()));
                tablaPdf.addCell(entry.getKey().getNombre());
                tablaPdf.addCell(String.valueOf(entry.getValue()));
            }
            
            documento.add(tablaPdf);
            documento.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean exportarHistorialJugadorPDF(Jugador jugador, java.util.List<Torneo> todosLosTorneos, String rutaDestino) {
        java.util.List<Torneo> historial = jugador.consultarHistorialTorneos(todosLosTorneos);
        try {
            com.itextpdf.text.Document documento = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(rutaDestino));
            documento.open();
            
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("HISTORIAL DE TORNEOS - JUGADOR: " + jugador.getNombre());
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            documento.add(titulo);
            
            com.itextpdf.text.pdf.PdfPTable tablaPdf = new com.itextpdf.text.pdf.PdfPTable(2);
            tablaPdf.addCell("Torneo");
            tablaPdf.addCell("Fase Alcanzada / Estado");
            
            for (Torneo t : historial) {
                tablaPdf.addCell(t.getNombre());
                tablaPdf.addCell(t.getFaseActual());
            }
            
            documento.add(tablaPdf);
            documento.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean exportarHistorialEquipoPDF(Equipo equipo, java.util.List<Torneo> todosLosTorneos, String rutaDestino) {
        java.util.List<Torneo> historial = equipo.consultarHistorialTorneos(todosLosTorneos);
        try {
            com.itextpdf.text.Document documento = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(rutaDestino));
            documento.open();
            
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("HISTORIAL DE TORNEOS - EQUIPO: " + equipo.getNombre());
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            documento.add(titulo);
            
            com.itextpdf.text.pdf.PdfPTable tablaPdf = new com.itextpdf.text.pdf.PdfPTable(2);
            tablaPdf.addCell("Torneo");
            tablaPdf.addCell("Fase Alcanzada / Estado");
            
            for (Torneo t : historial) {
                tablaPdf.addCell(t.getNombre());
                tablaPdf.addCell(t.getFaseActual());
            }
            
            documento.add(tablaPdf);
            documento.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}