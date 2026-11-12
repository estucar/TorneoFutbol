package modelo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Torneo implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    @Basic
    private String nombre;
    
    @Temporal(TemporalType.DATE)
    private Date fechaInicio;
    
    @Basic
    private String faseActual;
    
    @ManyToMany(cascade = {javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE})
    private List<Equipo> equipos;
    
    @ManyToMany(cascade = {javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE})
    private List<Cancha> canchas;
    
    @javax.persistence.ManyToOne(cascade = {javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE})
    private Equipo campeon;

    public Torneo() {
        this.equipos = new ArrayList<>();
        this.canchas = new ArrayList<>();
    }

    public Torneo(int id, String nombre, Date fechaInicio, String faseActual) {
        this.id = id;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.faseActual = faseActual;
        this.equipos = new ArrayList<>();
        this.canchas = new ArrayList<>();
    }

    public void registrarAlta(String nombre, java.util.Date fechaInicio) {
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.faseActual = "Fase de Grupos";
    }
    
    public void modificar(String nuevoNombre, java.util.Date nuevaFechaInicio) {
        this.nombre = nuevoNombre;
        this.fechaInicio = nuevaFechaInicio;
    }
    
    public void registrarBaja() {
        this.faseActual = "Cancelado";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFaseActual() {
        return faseActual;
    }

    public void setFaseActual(String faseActual) {
        this.faseActual = faseActual;
    }

    public List<Equipo> getEquipos() {
        return equipos;
    }

    public void setEquipos(List<Equipo> equipos) {
        this.equipos = equipos;
    }

    public List<Cancha> getCanchas() {
        return canchas;
    }

    public void setCanchas(List<Cancha> canchas) {
        this.canchas = canchas;
    }
    
    public java.util.List<EstadisticaEquipo> calcularTablaPosiciones(java.util.List<Partido> partidosFase) {
        java.util.Map<Equipo, EstadisticaEquipo> mapaEstadisticas = new java.util.HashMap<>();
        
        if (partidosFase != null) {
            for (Partido p : partidosFase) {
                if ("Finalizado".equals(p.getEstado()) && p.getResultado() != null && p.getResultado().contains("-")) {
                    String[] partes = p.getResultado().split("-");
                    int golesL = Integer.parseInt(partes[0].trim());
                    int golesV = Integer.parseInt(partes[1].trim());
                    
                    Equipo local = p.getEquipoLocal();
                    Equipo visitante = p.getEquipoVisitante();
                    
                    if (!mapaEstadisticas.containsKey(local)) {
                        EstadisticaEquipo estL = new EstadisticaEquipo();
                        estL.setEquipo(local);
                        mapaEstadisticas.put(local, estL);
                    }
                    if (!mapaEstadisticas.containsKey(visitante)) {
                        EstadisticaEquipo estV = new EstadisticaEquipo();
                        estV.setEquipo(visitante);
                        mapaEstadisticas.put(visitante, estV);
                    }
                    
                    EstadisticaEquipo statsLocal = mapaEstadisticas.get(local);
                    EstadisticaEquipo statsVisitante = mapaEstadisticas.get(visitante);
                    
                    statsLocal.setPj(statsLocal.getPj() + 1);
                    statsVisitante.setPj(statsVisitante.getPj() + 1);
                    
                    statsLocal.setGf(statsLocal.getGf() + golesL);
                    statsLocal.setGc(statsLocal.getGc() + golesV);
                    
                    statsVisitante.setGf(statsVisitante.getGf() + golesV);
                    statsVisitante.setGc(statsVisitante.getGc() + golesL);
                    
                    if (golesL > golesV) {
                        statsLocal.setPg(statsLocal.getPg() + 1);
                        statsLocal.setPuntos(statsLocal.getPuntos() + 3);
                        statsVisitante.setPp(statsVisitante.getPp() + 1);
                    } else if (golesL < golesV) {
                        statsVisitante.setPg(statsVisitante.getPg() + 1);
                        statsVisitante.setPuntos(statsVisitante.getPuntos() + 3);
                        statsLocal.setPp(statsLocal.getPp() + 1);
                    } else {
                        statsLocal.setPe(statsLocal.getPe() + 1);
                        statsLocal.setPuntos(statsLocal.getPuntos() + 1);
                        statsVisitante.setPe(statsVisitante.getPe() + 1);
                        statsVisitante.setPuntos(statsVisitante.getPuntos() + 1);
                    }

                    statsLocal.setDg(statsLocal.getGf() - statsLocal.getGc());
                    statsVisitante.setDg(statsVisitante.getGf() - statsVisitante.getGc());
                }
            }
        }
        
        java.util.List<EstadisticaEquipo> tabla = new java.util.ArrayList<>(mapaEstadisticas.values());
        java.util.Collections.sort(tabla);
        return tabla;
    }

    public java.util.List<Jugador> calcularGoleadores(java.util.List<Partido> partidos) {
        java.util.Map<Jugador, Integer> conteoGoles = new java.util.HashMap<>();
        
        if (partidos != null) {
            for (Partido p : partidos) {
                if ("Finalizado".equals(p.getEstado()) && p.getIncidentes() != null) {
                    for (Incidente inc : p.getIncidentes()) {
                        if ("Gol".equalsIgnoreCase(inc.getTipo()) && inc.getJugador() != null) {
                            Jugador j = inc.getJugador();
                            conteoGoles.put(j, conteoGoles.getOrDefault(j, 0) + 1);
                        }
                    }
                }
            }
        }
        
        java.util.List<Jugador> goleadores = new java.util.ArrayList<>();
        for (java.util.Map.Entry<Jugador, Integer> entry : conteoGoles.entrySet()) {
            Jugador j = entry.getKey();
            j.setGoles(entry.getValue());
            goleadores.add(j);
        }
        
        java.util.Collections.sort(goleadores, new java.util.Comparator<Jugador>() {
            @Override
            public int compare(Jugador j1, Jugador j2) {
                return Integer.compare(j2.getGoles(), j1.getGoles());
            }
        });
        
        return goleadores;
    }

    public java.util.List<Partido> sortearYGenerarFaseDeGrupos(java.util.List<Cancha> canchasDisponibles, java.util.List<Arbitro> arbitrosDisponibles) {
        java.util.List<Partido> partidosGenerados = new java.util.ArrayList<>();
        if (this.equipos == null || this.equipos.size() < 2) {
            return partidosGenerados;
        }

        java.util.List<Equipo> mezcla = new java.util.ArrayList<>(this.equipos);
        java.util.Collections.shuffle(mezcla);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        if (this.fechaInicio != null) {
            cal.setTime(this.fechaInicio);
        } else {
            cal.setTime(new java.util.Date());
        }

        String[] horarios = {"18:00", "19:30", "21:00", "22:30"};
        int matchCount = 0;

        int total = mezcla.size();
        if (total <= 5) {
            // Todos contra todos
            for (int i = 0; i < total; i++) {
                for (int j = i + 1; j < total; j++) {
                    Partido p = crearPartidoFixture(mezcla.get(i), mezcla.get(j), "Fase de Grupos", cal, horarios[matchCount % horarios.length], canchasDisponibles, arbitrosDisponibles, matchCount);
                    partidosGenerados.add(p);
                    matchCount++;
                    if (matchCount % 2 == 0) cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
                }
            }
        } else {
            // Dividir en grupos de 3 o 4 equipos
            int tamGrupo = (total >= 12) ? 4 : 3;
            java.util.List<java.util.List<Equipo>> grupos = new java.util.ArrayList<>();
            java.util.List<Equipo> grupoActual = new java.util.ArrayList<>();
            for (Equipo eq : mezcla) {
                grupoActual.add(eq);
                if (grupoActual.size() == tamGrupo) {
                    grupos.add(grupoActual);
                    grupoActual = new java.util.ArrayList<>();
                }
            }
            if (!grupoActual.isEmpty()) {
                if (grupos.isEmpty()) {
                    grupos.add(grupoActual);
                } else {
                    grupos.get(grupos.size() - 1).addAll(grupoActual);
                }
            }

            for (java.util.List<Equipo> grp : grupos) {
                for (int i = 0; i < grp.size(); i++) {
                    for (int j = i + 1; j < grp.size(); j++) {
                        Partido p = crearPartidoFixture(grp.get(i), grp.get(j), "Fase de Grupos", cal, horarios[matchCount % horarios.length], canchasDisponibles, arbitrosDisponibles, matchCount);
                        partidosGenerados.add(p);
                        matchCount++;
                        if (matchCount % 2 == 0) cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
                    }
                }
            }
        }

        this.faseActual = "Fase de Grupos";
        return partidosGenerados;
    }

    private Partido crearPartidoFixture(Equipo local, Equipo visitante, String fase, java.util.Calendar cal, String hora, java.util.List<Cancha> canchas, java.util.List<Arbitro> arbitros, int idx) {
        Partido p = new Partido();
        p.setEquipoLocal(local);
        p.setEquipoVisitante(visitante);
        p.setFase(fase);
        p.setFecha(cal.getTime());
        p.setHora(hora);
        if (canchas != null && !canchas.isEmpty()) {
            p.setCancha(canchas.get(idx % canchas.size()));
        }
        if (arbitros != null && !arbitros.isEmpty()) {
            p.setArbitro(arbitros.get(idx % arbitros.size()));
        }
        p.programar();
        return p;
    }

    public java.util.List<Partido> avanzarFase(java.util.List<Partido> partidosFaseActual) {
        if (partidosFaseActual == null || partidosFaseActual.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        for (Partido p : partidosFaseActual) {
            if (!"Finalizado".equals(p.getEstado())) {
                return new java.util.ArrayList<>(); 
            }
        }
        
        java.util.List<Partido> nuevosPartidos = new java.util.ArrayList<>();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(new java.util.Date());
        cal.add(java.util.Calendar.DAY_OF_YEAR, 2);
        String[] horarios = {"19:00", "21:00", "22:30"};
        
        if ("Fase de Grupos".equalsIgnoreCase(this.faseActual)) {
            java.util.List<EstadisticaEquipo> tabla = calcularTablaPosiciones(partidosFaseActual);
            
            // Si la tabla no tiene todos los equipos, sumamos los equipos del torneo
            if (tabla.isEmpty() && this.equipos != null) {
                for (Equipo e : this.equipos) {
                    EstadisticaEquipo est = new EstadisticaEquipo();
                    est.setEquipo(e);
                    tabla.add(est);
                }
            }

            int n = tabla.size();
            if (n >= 16) {
                // Octavos de final: 8 partidos (16 equipos)
                for (int i = 0; i < 8; i++) {
                    Equipo e1 = tabla.get(i).getEquipo();
                    Equipo e2 = tabla.get(15 - i).getEquipo();
                    Partido p = new Partido();
                    p.setEquipoLocal(e1);
                    p.setEquipoVisitante(e2);
                    p.setFase("Octavos de Final");
                    p.setFecha(cal.getTime());
                    p.setHora(horarios[i % horarios.length]);
                    p.programar();
                    nuevosPartidos.add(p);
                }
                this.faseActual = "Octavos de Final";
            } else if (n >= 8) {
                // Cuartos de final: 4 partidos (8 equipos)
                for (int i = 0; i < 4; i++) {
                    Equipo e1 = tabla.get(i).getEquipo();
                    Equipo e2 = tabla.get(7 - i).getEquipo();
                    Partido p = new Partido();
                    p.setEquipoLocal(e1);
                    p.setEquipoVisitante(e2);
                    p.setFase("Cuartos de Final");
                    p.setFecha(cal.getTime());
                    p.setHora(horarios[i % horarios.length]);
                    p.programar();
                    nuevosPartidos.add(p);
                }
                this.faseActual = "Cuartos de Final";
            } else if (n >= 4) {
                // Semifinales: 2 partidos (4 equipos)
                Equipo e1 = tabla.get(0).getEquipo();
                Equipo e4 = tabla.get(3).getEquipo();
                Equipo e2 = tabla.get(1).getEquipo();
                Equipo e3 = tabla.get(2).getEquipo();

                Partido s1 = new Partido();
                s1.setEquipoLocal(e1);
                s1.setEquipoVisitante(e4);
                s1.setFase("Semifinal");
                s1.setFecha(cal.getTime());
                s1.setHora("19:30");
                s1.programar();

                Partido s2 = new Partido();
                s2.setEquipoLocal(e2);
                s2.setEquipoVisitante(e3);
                s2.setFase("Semifinal");
                s2.setFecha(cal.getTime());
                s2.setHora("21:30");
                s2.programar();

                nuevosPartidos.add(s1);
                nuevosPartidos.add(s2);
                this.faseActual = "Semifinal";
            } else if (n >= 2) {
                // Final directa: 1 partido (2 equipos)
                Equipo e1 = tabla.get(0).getEquipo();
                Equipo e2 = tabla.get(1).getEquipo();
                Partido fin = new Partido();
                fin.setEquipoLocal(e1);
                fin.setEquipoVisitante(e2);
                fin.setFase("Final");
                fin.setFecha(cal.getTime());
                fin.setHora("20:30");
                fin.programar();

                nuevosPartidos.add(fin);
                this.faseActual = "Final";
            }
        } else if ("Octavos de Final".equalsIgnoreCase(this.faseActual)) {
            java.util.List<Equipo> ganadores = new java.util.ArrayList<>();
            for (Partido p : partidosFaseActual) {
                Equipo g = p.getGanador();
                if (g == null) g = p.getEquipoLocal();
                ganadores.add(g);
            }
            if (ganadores.size() >= 8) {
                for (int i = 0; i < 4; i++) {
                    Partido p = new Partido();
                    p.setEquipoLocal(ganadores.get(i));
                    p.setEquipoVisitante(ganadores.get(7 - i));
                    p.setFase("Cuartos de Final");
                    p.setFecha(cal.getTime());
                    p.setHora(horarios[i % horarios.length]);
                    p.programar();
                    nuevosPartidos.add(p);
                }
                this.faseActual = "Cuartos de Final";
            }
        } else if ("Cuartos de Final".equalsIgnoreCase(this.faseActual)) {
            java.util.List<Equipo> ganadores = new java.util.ArrayList<>();
            for (Partido p : partidosFaseActual) {
                Equipo g = p.getGanador();
                if (g == null) g = p.getEquipoLocal();
                ganadores.add(g);
            }
            if (ganadores.size() >= 4) {
                Partido s1 = new Partido();
                s1.setEquipoLocal(ganadores.get(0));
                s1.setEquipoVisitante(ganadores.get(3));
                s1.setFase("Semifinal");
                s1.setFecha(cal.getTime());
                s1.setHora("19:30");
                s1.programar();

                Partido s2 = new Partido();
                s2.setEquipoLocal(ganadores.get(1));
                s2.setEquipoVisitante(ganadores.get(2));
                s2.setFase("Semifinal");
                s2.setFecha(cal.getTime());
                s2.setHora("21:30");
                s2.programar();

                nuevosPartidos.add(s1);
                nuevosPartidos.add(s2);
                this.faseActual = "Semifinal";
            }
        } else if ("Semifinal".equalsIgnoreCase(this.faseActual)) {
            java.util.List<Equipo> ganadores = new java.util.ArrayList<>();
            for (Partido p : partidosFaseActual) {
                Equipo g = p.getGanador();
                if (g == null) g = p.getEquipoLocal();
                ganadores.add(g);
            }
            if (ganadores.size() >= 2) {
                Partido fin = new Partido();
                fin.setEquipoLocal(ganadores.get(0));
                fin.setEquipoVisitante(ganadores.get(1));
                fin.setFase("Final");
                fin.setFecha(cal.getTime());
                fin.setHora("21:00");
                fin.programar();

                nuevosPartidos.add(fin);
                this.faseActual = "Final";
            }
        } else if ("Final".equalsIgnoreCase(this.faseActual)) {
            if (!partidosFaseActual.isEmpty()) {
                definirCampeon(partidosFaseActual.get(0));
            }
        }
        
        return nuevosPartidos;
    }
    
    public Equipo getCampeon() {
        return campeon;
    }

    public void setCampeon(Equipo campeon) {
        this.campeon = campeon;
    }
    
    public boolean definirCampeon(Partido partidoFinal) {
        if (partidoFinal == null || !"Final".equalsIgnoreCase(partidoFinal.getFase())) {
            return false;
        }
        
        if (!"Finalizado".equals(partidoFinal.getEstado())) {
            return false;
        }
        
        Equipo ganador = partidoFinal.getGanador();
        
        if (ganador != null) {
            this.setCampeon(ganador);
            this.setFaseActual("Finalizado");
            return true;
        }
        
        return false;
    }
    
    public java.util.List<Partido> consultarFixture(java.util.List<Partido> todosLosPartidos, String fase) {
        java.util.List<Partido> fixtureFiltrado = new java.util.ArrayList<>();
        
        if (todosLosPartidos != null) {
            for (Partido p : todosLosPartidos) {
                if (fase == null || fase.trim().isEmpty() || fase.equalsIgnoreCase(p.getFase())) {
                    fixtureFiltrado.add(p);
                }
            }
        }
        return fixtureFiltrado;
    }

    public java.util.List<Jugador> generarReporteJugadoresMasSancionados(java.util.List<Equipo> equipos) {
        java.util.List<Jugador> sancionados = new java.util.ArrayList<>();
        
        if (equipos != null) {
            for (Equipo eq : equipos) {
                if (eq.getPlantel() != null) {
                    for (Jugador j : eq.getPlantel()) {
                        if (j.getSanciones() != null && !j.getSanciones().isEmpty()) {
                            sancionados.add(j);
                        }
                    }
                }
            }
        }
        
        java.util.Collections.sort(sancionados, new java.util.Comparator<Jugador>() {
            @Override
            public int compare(Jugador j1, Jugador j2) {
                return Integer.compare(j2.getSanciones().size(), j1.getSanciones().size());
            }
        });
        
        return sancionados;
    }
    
    public String generarReporteArbitros(java.util.List<Partido> partidosTorneo) {
        java.util.Map<Arbitro, Integer> conteoArbitros = new java.util.HashMap<>();
        
        if (partidosTorneo != null) {
            for (Partido p : partidosTorneo) {
                if (p.getArbitro() != null) {
                    Arbitro a = p.getArbitro();
                    conteoArbitros.put(a, conteoArbitros.getOrDefault(a, 0) + 1);
                }
            }
        }
        
        StringBuilder reporte = new StringBuilder();
        reporte.append("Reporte de Arbitros - ").append(this.nombre).append("\n");
        
        for (java.util.Map.Entry<Arbitro, Integer> entry : conteoArbitros.entrySet()) {
            reporte.append("Árbitro Mat. ").append(entry.getKey().getMatricula())
                   .append(" (").append(entry.getKey().getNombre()).append("): ")
                   .append(entry.getValue()).append(" partidos dirigidos.\n");
        }
        
        return reporte.toString();
    }

    @Override
    public String toString() {
        return (nombre != null ? nombre : "Torneo") + " (" + (faseActual != null ? faseActual : "En curso") + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Torneo)) return false;
        Torneo otro = (Torneo) o;
        if (this.id > 0 && otro.id > 0) return this.id == otro.id;
        if (this.nombre != null && otro.nombre != null) return this.nombre.equalsIgnoreCase(otro.nombre);
        return false;
    }

    @Override
    public int hashCode() {
        if (this.id > 0) return Integer.hashCode(this.id);
        return this.nombre != null ? this.nombre.toLowerCase().hashCode() : 0;
    }
}
