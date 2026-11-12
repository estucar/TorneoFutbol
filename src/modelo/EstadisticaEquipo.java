package modelo;

public class EstadisticaEquipo implements java.lang.Comparable<EstadisticaEquipo> {

    private Equipo equipo;
    private int pj;
    private int pg;
    private int pe;
    private int pp;
    private int gf;
    private int gc;
    private int dg;
    private int puntos;

    public EstadisticaEquipo() {
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public int getPj() {
        return pj;
    }

    public void setPj(int pj) {
        this.pj = pj;
    }

    public int getPg() {
        return pg;
    }

    public void setPg(int pg) {
        this.pg = pg;
    }

    public int getPe() {
        return pe;
    }

    public void setPe(int pe) {
        this.pe = pe;
    }

    public int getPp() {
        return pp;
    }

    public void setPp(int pp) {
        this.pp = pp;
    }

    public int getGf() {
        return gf;
    }

    public void setGf(int gf) {
        this.gf = gf;
    }

    public int getGc() {
        return gc;
    }

    public void setGc(int gc) {
        this.gc = gc;
    }

    public int getDg() {
        return dg;
    }

    public void setDg(int dg) {
        this.dg = dg;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    @Override
    public int compareTo(EstadisticaEquipo otra) {
        if (this.puntos != otra.getPuntos()) {
            return Integer.compare(otra.getPuntos(), this.puntos);
        }
        if (this.dg != otra.getDg()) {
            return Integer.compare(otra.getDg(), this.dg);
        }
        return Integer.compare(otra.getGf(), this.gf);
    }
}