package controlador;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import modelo.*;
import servicios.ServicioCorreo;
import servicios.ServicioPDF;

public class GestorSistema {

    private static final Logger LOGGER = Logger.getLogger(GestorSistema.class.getName());
    private static GestorSistema instancia;

    private EntityManagerFactory emf;
    private EntityManager em;
    private boolean bdConectada = false;


    private String rolActual = "PUBLICO_GENERAL"; 
    private Usuario usuarioActual;
    private Jugador jugadorActual;

    private Administrador adminMotor;
    private Organizador organizadorMotor;
    private ServicioPDF servicioPDF;
    private ServicioCorreo servicioCorreo;

    private List<Cancha> canchas = new ArrayList<>();
    private List<Torneo> torneos = new ArrayList<>();
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Equipo> equipos = new ArrayList<>();
    private List<Jugador> jugadores = new ArrayList<>();
    private List<Partido> partidos = new ArrayList<>();
    private List<Reclamo> reclamos = new ArrayList<>();
    private List<Sancion> sanciones = new ArrayList<>();

    private GestorSistema() {
        this.servicioPDF = new ServicioPDF();
        this.servicioCorreo = new ServicioCorreo();
        conectarBaseDeDatos();
        cargarDatosDesdeBD();
        asegurarAdministradorYDatosIniciales();
    }

    public static synchronized GestorSistema getInstancia() {
        if (instancia == null) {
            instancia = new GestorSistema();
        }
        return instancia;
    }

    private void conectarBaseDeDatos() {
        try {
            emf = Persistence.createEntityManagerFactory("TorneoDeFutbolPU");
            em = emf.createEntityManager();
            bdConectada = true;
            LOGGER.info("Conexión exitosa a MySQL mediante TorneoDeFutbolPU.");
        } catch (Exception e) {
            bdConectada = false;
            LOGGER.log(Level.WARNING, "No se pudo conectar a MySQL, operando en modo memoria local: " + e.getMessage());
        }
    }

    public boolean isBdConectada() {
        return bdConectada && em != null && em.isOpen();
    }

    public EntityManager getEntityManager() {
        return em;
    }

    public void cargarDatosDesdeBD() {
        if (!isBdConectada()) return;
        try {
            em.clear();
            canchas = new ArrayList<>(em.createQuery("SELECT c FROM Cancha c", Cancha.class).getResultList());
            torneos = new ArrayList<>(em.createQuery("SELECT t FROM Torneo t", Torneo.class).getResultList());
            usuarios = new ArrayList<>(em.createQuery("SELECT u FROM Usuario u", Usuario.class).getResultList());
            equipos = new ArrayList<>(em.createQuery("SELECT e FROM Equipo e", Equipo.class).getResultList());
            jugadores = new ArrayList<>(em.createQuery("SELECT j FROM Jugador j", Jugador.class).getResultList());
            partidos = new ArrayList<>(em.createQuery("SELECT p FROM Partido p", Partido.class).getResultList());
            reclamos = new ArrayList<>(em.createQuery("SELECT r FROM Reclamo r", Reclamo.class).getResultList());
            sanciones = new ArrayList<>(em.createQuery("SELECT s FROM Sancion s", Sancion.class).getResultList());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al refrescar listas desde MySQL: " + e.getMessage());
        }
    }

    private void guardarEntidad(Object entidad) {
        if (!isBdConectada()) return;
        try {
            em.getTransaction().begin();
            em.persist(entidad);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            LOGGER.log(Level.WARNING, "Error al persistir entidad: " + e.getMessage());
        }
    }

    private void actualizarEntidad(Object entidad) {
        if (!isBdConectada()) return;
        try {
            em.getTransaction().begin();
            em.merge(entidad);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            LOGGER.log(Level.WARNING, "Error al actualizar entidad: " + e.getMessage());
        }
    }

    public void eliminarEntidad(Object entidad) {
        if (!isBdConectada() || em == null || entidad == null) return;
        try {
            em.getTransaction().begin();
            Object ref = em.contains(entidad) ? entidad : em.merge(entidad);
            em.remove(ref);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            LOGGER.log(Level.WARNING, "Error al eliminar entidad: " + e.getMessage());
        }
    }

    private void asegurarAdministradorYDatosIniciales() {
        Administrador adminExistente = null;
        Organizador orgExistente = null;
        for (Usuario u : usuarios) {
            if (u instanceof Administrador && adminExistente == null) {
                adminExistente = (Administrador) u;
            } else if (u instanceof Organizador && orgExistente == null) {
                orgExistente = (Organizador) u;
            }
        }

        if (adminExistente == null) {
            adminExistente = new Administrador();
            adminExistente.registrarAlta(40100200L, "Esteban", "Administrador", "admin@ugd.edu.ar", "admin123", "Administrador");
            adminExistente.setNivelAcceso("Total");
            guardarEntidad(adminExistente);
            usuarios.add(adminExistente);
        }
        this.adminMotor = adminExistente;

        if (orgExistente == null) {
            orgExistente = new Organizador();
            orgExistente.registrarAlta(35200300L, "Juan", "Pérez (Organizador)", "organizador@ugd.edu.ar", "1234", "Organizador");
            orgExistente.setNivelAcceso("VIP");
            guardarEntidad(orgExistente);
            usuarios.add(orgExistente);
        }
        this.organizadorMotor = orgExistente;

        Arbitro arbInicial = null;
        for (Usuario u : usuarios) {
            if (u instanceof Arbitro) {
                arbInicial = (Arbitro) u;
                break;
            }
        }
        if (arbInicial == null) {
            arbInicial = new Arbitro();
            arbInicial.registrarAlta(32888999L, "Horacio", "Elizondo", "arbitro@ugd.edu.ar", "1234", "Arbitro");
            arbInicial.setMatricula(9991);
            guardarEntidad(arbInicial);
            usuarios.add(arbInicial);
        }

        if (canchas.isEmpty()) {
            Cancha c1 = new Cancha();
            c1.registrarAlta("Cancha Principal UGD", "Campus Urbano Posadas", true);
            guardarEntidad(c1);
            canchas.add(c1);

            Cancha c2 = new Cancha();
            c2.registrarAlta("Cancha Sintética Norte", "Av. Quaranta y Ruta 12", true);
            guardarEntidad(c2);
            canchas.add(c2);
        }

        if (equipos.isEmpty()) {
            Calendar cal = Calendar.getInstance();
            cal.set(1998, Calendar.JUNE, 24);
            Date fechaNacAdulto = cal.getTime();

            Capitan cap1 = new Capitan();
            cap1.setDni(40111222L);
            cap1.setNombre("Lionel Messi");
            cap1.setPosicion("Delantero");
            cap1.setFechaNacimiento(fechaNacAdulto);
            cap1.setEstado("Activo");
            cap1.setEmail("capitan@ugd.edu.ar");
            cap1.setContrasenia("1234");
            cap1.setNivelAccesoCapitan("Capitán Titular");
            Credencial credCap1 = new Credencial();
            credCap1.generar();
            cap1.setCredencial(credCap1);

            Jugador jug1 = new Jugador();
            jug1.setDni(41222333L);
            jug1.setNombre("Ángel Di María");
            jug1.setPosicion("Extremo");
            jug1.setFechaNacimiento(fechaNacAdulto);
            jug1.setEstado("Activo");
            jug1.setEmail("jugador@ugd.edu.ar");
            jug1.setContrasenia("1234");
            Credencial credJug1 = new Credencial();
            credJug1.generar();
            jug1.setCredencial(credJug1);

            Equipo eq1 = new Equipo();
            eq1.setNombre("La Scaloneta FC");
            eq1.setCategoria("Libres");
            eq1.setColorCamiseta("Celeste y Blanca");
            eq1.setSedeHabitual("Campus UGD");
            eq1.setCapitan(cap1);
            Inscripcion insc1 = new Inscripcion();
            insc1.setFecha(new Date());
            insc1.setEstado("Habilitado");
            insc1.adjuntarDocumentacion("Comprobante_Scaloneta_OK.pdf");
            eq1.setInscripcion(insc1);
            eq1.agregarJugador(cap1);
            eq1.agregarJugador(jug1);

            Capitan cap2 = new Capitan();
            cap2.setDni(39444555L);
            cap2.setNombre("Hugo Lloris");
            cap2.setPosicion("Arquero");
            cap2.setFechaNacimiento(fechaNacAdulto);
            cap2.setEstado("Activo");
            cap2.setEmail("lloris@ugd.edu.ar");
            cap2.setContrasenia("1234");
            cap2.setNivelAccesoCapitan("Capitán Titular");
            Credencial credCap2 = new Credencial();
            credCap2.generar();
            cap2.setCredencial(credCap2);

            Jugador jug2 = new Jugador();
            jug2.setDni(42555666L);
            jug2.setNombre("Kylian Mbappé");
            jug2.setPosicion("Delantero");
            jug2.setFechaNacimiento(fechaNacAdulto);
            jug2.setEstado("Activo");
            jug2.setEmail("mbappe@ugd.edu.ar");
            jug2.setContrasenia("1234");
            Credencial credJug2 = new Credencial();
            credJug2.generar();
            jug2.setCredencial(credJug2);

            Equipo eq2 = new Equipo();
            eq2.setNombre("Los Galos UGD");
            eq2.setCategoria("Libres");
            eq2.setColorCamiseta("Azul Francia");
            eq2.setSedeHabitual("Sede Centro");
            eq2.setCapitan(cap2);
            Inscripcion insc2 = new Inscripcion();
            insc2.setFecha(new Date());
            insc2.setEstado("Habilitado");
            insc2.adjuntarDocumentacion("Planilla_Galos_OK.pdf");
            eq2.setInscripcion(insc2);
            eq2.agregarJugador(cap2);
            eq2.agregarJugador(jug2);

            guardarEntidad(credCap1);
            guardarEntidad(credJug1);
            guardarEntidad(credCap2);
            guardarEntidad(credJug2);
            guardarEntidad(cap1);
            guardarEntidad(jug1);
            guardarEntidad(cap2);
            guardarEntidad(jug2);
            guardarEntidad(insc1);
            guardarEntidad(insc2);
            guardarEntidad(eq1);
            guardarEntidad(eq2);

            jugadores.add(cap1);
            jugadores.add(jug1);
            jugadores.add(cap2);
            jugadores.add(jug2);
            equipos.add(eq1);
            equipos.add(eq2);
        }


        if (torneos.isEmpty()) {
            Torneo t1 = new Torneo();
            t1.registrarAlta("Torneo Clausura UGD 2026", new Date());
            for (Cancha c : canchas) t1.getCanchas().add(c);
            for (Equipo e : equipos) t1.getEquipos().add(e);
            guardarEntidad(t1);
            torneos.add(t1);
        }

        if (partidos.isEmpty() && equipos.size() >= 2 && !canchas.isEmpty() && arbInicial != null) {
            Partido p1 = new Partido();
            p1.setEquipoLocal(equipos.get(0));
            p1.setEquipoVisitante(equipos.get(1));
            p1.setFecha(new Date());
            p1.setHora("20:00");
            p1.setFase("Fase de Grupos");
            p1.setCancha(canchas.get(0));
            p1.setArbitro(arbInicial);
            p1.programar();

            // Cargamos un resultado inicial para que las tablas de posiciones y goleadores tengan datos desde el arranque
            List<Incidente> incIniciales = new ArrayList<>();
            if (!equipos.get(0).getPlantel().isEmpty()) {
                Jugador goleador1 = equipos.get(0).getPlantel().get(0);
                Incidente gol1 = new Incidente(0, "Gol", 18, "Gol de tiro libre al ángulo", goleador1);
                guardarEntidad(gol1);
                incIniciales.add(gol1);
                if (equipos.get(0).getPlantel().size() > 1) {
                    Jugador goleador2 = equipos.get(0).getPlantel().get(1);
                    Incidente gol2 = new Incidente(0, "Gol", 42, "Definición cruzada", goleador2);
                    guardarEntidad(gol2);
                    incIniciales.add(gol2);
                }
            }
            if (!equipos.get(1).getPlantel().isEmpty()) {
                Jugador goleadorV = equipos.get(1).getPlantel().get(0);
                Incidente golV = new Incidente(0, "Gol", 65, "Gol de penal", goleadorV);
                guardarEntidad(golV);
                incIniciales.add(golV);
            }
            p1.cargarResultado(2, 1, incIniciales);
            guardarEntidad(p1);
            partidos.add(p1);

            Partido p2 = new Partido();
            p2.setEquipoLocal(equipos.get(1));
            p2.setEquipoVisitante(equipos.get(0));
            p2.setFecha(new Date());
            p2.setHora("22:00");
            p2.setFase("Fase de Grupos");
            p2.setCancha(canchas.size() > 1 ? canchas.get(1) : canchas.get(0));
            p2.setArbitro(arbInicial);
            p2.programar();
            guardarEntidad(p2);
            partidos.add(p2);
        }
    }

    public boolean iniciarSesion(String email, String contrasenia) {
        if (email == null || contrasenia == null) return false;
        String mailLimpio = email.trim();
        String passLimpia = contrasenia.trim();

        cargarDatosDesdeBD();

        for (Usuario u : usuarios) {
            if (u.getEmail() != null && u.getEmail().equalsIgnoreCase(mailLimpio)
                    && u.getContrasenia() != null && u.getContrasenia().equals(passLimpia)) {
                if ("Inactivo".equalsIgnoreCase(u.getEstado())) return false;
                this.usuarioActual = u;
                this.jugadorActual = null;
                if (u instanceof Administrador) {
                    this.rolActual = "ADMINISTRADOR";
                } else if (u instanceof Organizador) {
                    this.rolActual = "ORGANIZADOR";
                } else if (u instanceof Arbitro) {
                    this.rolActual = "ARBITRO";
                } else if (u instanceof AdministradorVip) {
                    this.rolActual = "ADMINISTRADOR";
                } else {
                    this.rolActual = "ORGANIZADOR";
                }
                return true;
            }
        }

        for (Jugador j : jugadores) {
            if (j.getEmail() != null && j.getEmail().equalsIgnoreCase(mailLimpio)
                    && j.getContrasenia() != null && j.getContrasenia().equals(passLimpia)) {
                this.jugadorActual = j;
                this.usuarioActual = null;
                if (j instanceof Capitan) {
                    this.rolActual = "CAPITAN";
                } else {
                    this.rolActual = "JUGADOR";
                }
                return true;
            }
        }

        return false;
    }

    public void ingresarComoPublicoGeneral() {
        this.usuarioActual = null;
        this.jugadorActual = null;
        this.rolActual = "PUBLICO_GENERAL";
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
        this.jugadorActual = null;
        this.rolActual = "PUBLICO_GENERAL";
    }

    public String registrarNuevoUsuarioConEquipo(String tipoRol, long dni, String apellidoYNombre, String posicion,
                                                String email, String contrasenia, Equipo equipoExistente) {
        if (apellidoYNombre == null || apellidoYNombre.trim().isEmpty() || email == null || email.trim().isEmpty()
                || contrasenia == null || contrasenia.trim().isEmpty()) {
            return "Error: Completá Apellido y Nombre, Email y Contraseña.";
        }

        for (Usuario u : usuarios) {
            if (u.getEmail() != null && u.getEmail().equalsIgnoreCase(email.trim())) {
                return "Error: Ya existe un usuario registrado con ese email.";
            }
        }
        for (Jugador j : jugadores) {
            if (j.getEmail() != null && j.getEmail().equalsIgnoreCase(email.trim())) {
                return "Error: Ya existe un jugador/capitán registrado con ese email.";
            }
        }

        Calendar cal = Calendar.getInstance();
        cal.set(2000, Calendar.JANUARY, 15);
        Date fechaNacDefault = cal.getTime();

        if ("Administrador".equalsIgnoreCase(tipoRol)) {
            Administrador nuevo = new Administrador();
            nuevo.registrarAlta(dni, apellidoYNombre.trim(), "", email.trim(), contrasenia.trim(), "Administrador");
            nuevo.setNivelAcceso("Total");
            guardarEntidad(nuevo);
            usuarios.add(nuevo);
            return "OK: Administrador registrado con éxito en la Base de Datos.";
        } else if ("Organizador".equalsIgnoreCase(tipoRol)) {
            Organizador nuevo = new Organizador();
            nuevo.registrarAlta(dni, apellidoYNombre.trim(), "", email.trim(), contrasenia.trim(), "Organizador");
            nuevo.setNivelAcceso("VIP");
            guardarEntidad(nuevo);
            usuarios.add(nuevo);
            return "OK: Organizador registrado con éxito en la Base de Datos.";
        } else if ("Arbitro".equalsIgnoreCase(tipoRol) || "Árbitro".equalsIgnoreCase(tipoRol)) {
            Arbitro nuevo = new Arbitro();
            nuevo.registrarAlta(dni, apellidoYNombre.trim(), "", email.trim(), contrasenia.trim(), "Arbitro");
            int mat = 1000 + usuarios.size();
            nuevo.setMatricula(mat);
            guardarEntidad(nuevo);
            usuarios.add(nuevo);
            return "OK: Árbitro (Matrícula " + mat + ") registrado con éxito.";
        } else if ("Capitan".equalsIgnoreCase(tipoRol) || "Capitán".equalsIgnoreCase(tipoRol)) {
            if (jugadorYaPerteneceAOtroEquipo(dni, null)) {
                return "Error: El jugador con DNI " + dni + " ya pertenece a otro equipo. No puede estar en 2 o más equipos.";
            }
            Capitan cap = new Capitan();
            cap.setDni(dni);
            cap.setNombre(apellidoYNombre.trim());
            cap.setPosicion(posicion != null && !posicion.trim().isEmpty() ? posicion.trim() : "Capitán");
            cap.setFechaNacimiento(fechaNacDefault);
            cap.setEstado("Activo");
            cap.setEmail(email.trim());
            cap.setContrasenia(contrasenia.trim());
            cap.setNivelAccesoCapitan("Capitán Titular");
            Credencial cred = new Credencial();
            cred.generar();
            cap.setCredencial(cred);
            guardarEntidad(cred);
            guardarEntidad(cap);
            jugadores.add(cap);

            if (equipoExistente != null) {
                equipoExistente.setCapitan(cap);
                equipoExistente.agregarJugador(cap);
                actualizarEntidad(equipoExistente);
            }
            return "OK: Capitán registrado con éxito (con credencial generada).";
        } else {
            // Jugador normal
            if (equipoExistente != null && jugadorYaPerteneceAOtroEquipo(dni, equipoExistente)) {
                return "Error: El jugador con DNI " + dni + " ya pertenece a otro equipo. No puede estar en 2 o más equipos.";
            }
            Jugador jug = new Jugador();
            jug.setDni(dni);
            jug.setNombre(apellidoYNombre.trim());
            jug.setPosicion(posicion != null && !posicion.trim().isEmpty() ? posicion.trim() : "Mediocampista");
            jug.setFechaNacimiento(fechaNacDefault);
            jug.setEstado("Activo");
            jug.setEmail(email.trim());
            jug.setContrasenia(contrasenia.trim());
            Credencial cred = new Credencial();
            cred.generar();
            jug.setCredencial(cred);
            guardarEntidad(cred);
            guardarEntidad(jug);
            jugadores.add(jug);

            if (equipoExistente != null) {
                equipoExistente.agregarJugador(jug);
                actualizarEntidad(equipoExistente);
                return "OK: Jugador registrado con éxito y asignado al equipo '" + equipoExistente.getNombre() + "'.";
            }
            return "OK: Jugador registrado con éxito con su credencial generada.";
        }
    }

    public String registrarNuevaCuenta(String tipoRol, long dni, String nombre, String apellidoOPosicion,
                                       String email, String contrasenia, String datoExtra) {
        if (nombre == null || nombre.trim().isEmpty() || email == null || email.trim().isEmpty()
                || contrasenia == null || contrasenia.trim().isEmpty()) {
            return "Error: Completá Nombre, Email y Contraseña.";
        }

        for (Usuario u : usuarios) {
            if (u.getEmail() != null && u.getEmail().equalsIgnoreCase(email.trim())) {
                return "Error: Ya existe un usuario registrado con ese email.";
            }
        }
        for (Jugador j : jugadores) {
            if (j.getEmail() != null && j.getEmail().equalsIgnoreCase(email.trim())) {
                return "Error: Ya existe un jugador/capitán registrado con ese email.";
            }
        }

        Calendar cal = Calendar.getInstance();
        cal.set(2000, Calendar.JANUARY, 15);
        Date fechaNacDefault = cal.getTime();

        if ("Administrador".equalsIgnoreCase(tipoRol)) {
            Administrador nuevo = new Administrador();
            nuevo.registrarAlta(dni, nombre.trim(), apellidoOPosicion.trim(), email.trim(), contrasenia.trim(), "Administrador");
            nuevo.setNivelAcceso(datoExtra != null && !datoExtra.isEmpty() ? datoExtra : "Total");
            guardarEntidad(nuevo);
            usuarios.add(nuevo);
            return "OK: Administrador registrado con éxito en la Base de Datos.";
        } else if ("Organizador".equalsIgnoreCase(tipoRol)) {
            Organizador nuevo = new Organizador();
            nuevo.registrarAlta(dni, nombre.trim(), apellidoOPosicion.trim(), email.trim(), contrasenia.trim(), "Organizador");
            nuevo.setNivelAcceso("VIP");
            guardarEntidad(nuevo);
            usuarios.add(nuevo);
            return "OK: Organizador registrado con éxito en la Base de Datos.";
        } else if ("Arbitro".equalsIgnoreCase(tipoRol) || "Árbitro".equalsIgnoreCase(tipoRol)) {
            Arbitro nuevo = new Arbitro();
            nuevo.registrarAlta(dni, nombre.trim(), apellidoOPosicion.trim(), email.trim(), contrasenia.trim(), "Arbitro");
            int mat = 1000 + usuarios.size();
            try {
                if (datoExtra != null && !datoExtra.trim().isEmpty()) mat = Integer.parseInt(datoExtra.trim());
            } catch (NumberFormatException ignored) {}
            nuevo.setMatricula(mat);
            guardarEntidad(nuevo);
            usuarios.add(nuevo);
            return "OK: Árbitro (Matrícula " + mat + ") registrado con éxito.";
        } else if ("Capitan".equalsIgnoreCase(tipoRol) || "Capitán".equalsIgnoreCase(tipoRol)) {
            Capitan cap = new Capitan();
            cap.setDni(dni);
            cap.setNombre((nombre + " " + apellidoOPosicion).trim());
            cap.setPosicion("Capitán / Polifuncional");
            cap.setFechaNacimiento(fechaNacDefault);
            cap.setEstado("Activo");
            cap.setEmail(email.trim());
            cap.setContrasenia(contrasenia.trim());
            cap.setNivelAccesoCapitan("Capitán Titular");
            Credencial cred = new Credencial();
            cred.generar();
            cap.setCredencial(cred);
            guardarEntidad(cred);
            guardarEntidad(cap);
            jugadores.add(cap);

            if (datoExtra != null && !datoExtra.trim().isEmpty()) {
                Equipo nuevoEq = new Equipo();
                nuevoEq.setCategoria("Libres");
                nuevoEq.setColorCamiseta("A definir");
                nuevoEq.setSedeHabitual("Campus UGD");
                nuevoEq.registrarEquipo(datoExtra.trim(), cap);
                nuevoEq.agregarJugador(cap);
                if (nuevoEq.getInscripcion() != null) {
                    guardarEntidad(nuevoEq.getInscripcion());
                }
                guardarEntidad(nuevoEq);
                equipos.add(nuevoEq);
            }
            return "OK: Capitán registrado con éxito (con su equipo e inscripción en estado Pendiente).";
        } else {
            Jugador jug = new Jugador();
            jug.setDni(dni);
            jug.setNombre(nombre.trim());
            jug.setPosicion(apellidoOPosicion != null && !apellidoOPosicion.trim().isEmpty() ? apellidoOPosicion.trim() : "Mediocampista");
            jug.setFechaNacimiento(fechaNacDefault);
            jug.setEstado("Activo");
            jug.setEmail(email.trim());
            jug.setContrasenia(contrasenia.trim());
            Credencial cred = new Credencial();
            cred.generar();
            jug.setCredencial(cred);
            guardarEntidad(cred);
            guardarEntidad(jug);
            jugadores.add(jug);

            if (datoExtra != null && !datoExtra.trim().isEmpty()) {
                for (Equipo eq : equipos) {
                    if (eq.getNombre().equalsIgnoreCase(datoExtra.trim())) {
                        eq.agregarJugador(jug);
                        actualizarEntidad(eq);
                        break;
                    }
                }
            }
            return "OK: Jugador registrado con éxito con su credencial generada.";
        }
    }

    public String getRolActual() {
        return rolActual;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public Jugador getJugadorActual() {
        return jugadorActual;
    }

    public String getNombreActorLogueado() {
        if (usuarioActual != null) {
            return (usuarioActual.getNombre() + " " + (usuarioActual.getApellido() != null ? usuarioActual.getApellido() : "")).trim();
        }
        if (jugadorActual != null) {
            Equipo eq = buscarEquipoDeJugador(jugadorActual);
            String infoEq = (eq != null) ? " [" + eq.getNombre() + "]" : "";
            return jugadorActual.getNombre() + infoEq;
        }
        return "Visitante (Público General)";
    }

    public Equipo buscarEquipoDeJugador(Jugador j) {
        if (j == null) return null;
        for (Equipo e : equipos) {
            if (e.getCapitan() != null && (j.equals(e.getCapitan()) || j.getDni() == e.getCapitan().getDni())) return e;
            if (e.getPlantel() != null) {
                for (Jugador pj : e.getPlantel()) {
                    if (j.equals(pj) || j.getDni() == pj.getDni()) return e;
                }
            }
        }
        return null;
    }

    public boolean jugadorYaPerteneceAOtroEquipo(long dni, Equipo equipoDestino) {
        for (Equipo e : equipos) {
            if (equipoDestino != null && e.equals(equipoDestino)) continue;
            if (e.getCapitan() != null && e.getCapitan().getDni() == dni) return true;
            if (e.existeJugador(dni)) return true;
        }
        return false;
    }

    public String cambiarCapitanEquipo(Equipo equipo, Jugador nuevoCapitan) {
        if (equipo == null || nuevoCapitan == null) return "Seleccioná el equipo y el nuevo capitán.";
        Capitan capitanPromovido;
        if (nuevoCapitan instanceof Capitan) {
            capitanPromovido = (Capitan) nuevoCapitan;
        } else {
            capitanPromovido = new Capitan();
            capitanPromovido.setId(nuevoCapitan.getId());
            capitanPromovido.setDni(nuevoCapitan.getDni());
            capitanPromovido.setNombre(nuevoCapitan.getNombre());
            capitanPromovido.setPosicion(nuevoCapitan.getPosicion() != null ? nuevoCapitan.getPosicion() : "Capitán");
            capitanPromovido.setFechaNacimiento(nuevoCapitan.getFechaNacimiento());
            capitanPromovido.setEstado(nuevoCapitan.getEstado() != null ? nuevoCapitan.getEstado() : "Activo");
            capitanPromovido.setEmail(nuevoCapitan.getEmail());
            capitanPromovido.setContrasenia(nuevoCapitan.getContrasenia());
            capitanPromovido.setGoles(nuevoCapitan.getGoles());
            capitanPromovido.setTarjetas(nuevoCapitan.getTarjetas());
            capitanPromovido.setCredencial(nuevoCapitan.getCredencial());
            capitanPromovido.setSanciones(nuevoCapitan.getSanciones());
            capitanPromovido.setNivelAccesoCapitan("Capitán Titular");

            for (int i = 0; i < jugadores.size(); i++) {
                if (jugadores.get(i).getDni() == nuevoCapitan.getDni()) {
                    jugadores.set(i, capitanPromovido);
                    break;
                }
            }
            guardarEntidad(capitanPromovido);
        }

        Capitan anterior = equipo.getCapitan();
        if (anterior != null && anterior.getDni() != capitanPromovido.getDni()) {
            if (!equipo.existeJugador(anterior.getDni())) {
                equipo.agregarJugador(anterior);
            }
        }

        equipo.setCapitan(capitanPromovido);
        if (!equipo.existeJugador(capitanPromovido.getDni())) {
            equipo.agregarJugador(capitanPromovido);
        }
        if (this.jugadorActual != null && (this.jugadorActual.getDni() == capitanPromovido.getDni() || this.jugadorActual.equals(capitanPromovido))) {
            this.jugadorActual = capitanPromovido;
            this.rolActual = "CAPITAN";
        }
        actualizarEntidad(equipo);
        return "¡Capitán actualizado con éxito! " + capitanPromovido.getNombre() + " es ahora el nuevo capitán de " + equipo.getNombre() + ".";
    }

    public String modificarPerfilPersona(Object persona, long dni, String nombreCompleto, String email, String pass, String posicion, boolean esCapitan, Equipo nuevoEquipo) {
        if (persona == null) return "Error: Persona no especificada.";
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            return "Error: Nombre y Email no pueden estar vacíos.";
        }
        String mailLimpio = email.trim();
        String passLimpia = (pass != null && !pass.trim().isEmpty()) ? pass.trim() : "1234";

        if (persona instanceof Usuario) {
            Usuario u = (Usuario) persona;
            for (Usuario ot : usuarios) {
                if (!ot.equals(u) && ot.getEmail() != null && ot.getEmail().equalsIgnoreCase(mailLimpio)) {
                    return "Error: Ya existe otro usuario registrado con ese email.";
                }
            }
            for (Jugador jug : jugadores) {
                if (jug.getEmail() != null && jug.getEmail().equalsIgnoreCase(mailLimpio)) {
                    return "Error: Ya existe un jugador registrado con ese email.";
                }
            }
            u.setDni(dni);
            u.setNombre(nombreCompleto.trim());
            u.setApellido("");
            u.setEmail(mailLimpio);
            u.setContrasenia(passLimpia);
            actualizarEntidad(u);
            return "OK: Perfil de usuario actualizado correctamente.";
        } else if (persona instanceof Jugador) {
            Jugador j = (Jugador) persona;
            for (Usuario ot : usuarios) {
                if (ot.getEmail() != null && ot.getEmail().equalsIgnoreCase(mailLimpio)) {
                    return "Error: Ya existe un usuario registrado con ese email.";
                }
            }
            for (Jugador jug : jugadores) {
                if (!jug.equals(j) && jug.getDni() != j.getDni() && jug.getEmail() != null && jug.getEmail().equalsIgnoreCase(mailLimpio)) {
                    return "Error: Ya existe otro jugador registrado con ese email.";
                }
            }

            j.setDni(dni);
            j.setNombre(nombreCompleto.trim());
            j.setEmail(mailLimpio);
            j.setContrasenia(passLimpia);
            if (posicion != null && !posicion.trim().isEmpty()) {
                j.setPosicion(posicion.trim());
            }

            Equipo eqActual = buscarEquipoDeJugador(j);
            if (nuevoEquipo != null) {
                if (eqActual == null || !eqActual.equals(nuevoEquipo)) {
                    if (eqActual != null) {
                        eqActual.removerJugador(j);
                        if (j.equals(eqActual.getCapitan()) || j.getDni() == (eqActual.getCapitan() != null ? eqActual.getCapitan().getDni() : 0)) {
                            eqActual.setCapitan(null);
                        }
                        actualizarEntidad(eqActual);
                    }
                    nuevoEquipo.agregarJugador(j);
                    actualizarEntidad(nuevoEquipo);
                }
            } else if (eqActual != null) {
                eqActual.removerJugador(j);
                if (j.equals(eqActual.getCapitan()) || j.getDni() == (eqActual.getCapitan() != null ? eqActual.getCapitan().getDni() : 0)) {
                    eqActual.setCapitan(null);
                }
                actualizarEntidad(eqActual);
            }

            Equipo eqFinal = (nuevoEquipo != null) ? nuevoEquipo : eqActual;

            if ((j instanceof Capitan) && !esCapitan) {
                // Democión de Capitán a Jugador normal
                Jugador normal = new Jugador();
                normal.setId(j.getId());
                normal.setDni(dni);
                normal.setNombre(nombreCompleto.trim());
                normal.setEmail(mailLimpio);
                normal.setContrasenia(passLimpia);
                normal.setPosicion(posicion != null && !posicion.trim().isEmpty() ? posicion.trim() : (j.getPosicion() != null ? j.getPosicion() : "Mediocampista"));
                normal.setFechaNacimiento(j.getFechaNacimiento());
                normal.setEstado(j.getEstado() != null ? j.getEstado() : "Activo");
                normal.setGoles(j.getGoles());
                normal.setTarjetas(j.getTarjetas());
                normal.setCredencial(j.getCredencial());
                normal.setSanciones(j.getSanciones());

                // Quitar capitanía en el equipo
                if (eqFinal != null) {
                    if (eqFinal.getCapitan() != null && eqFinal.getCapitan().getDni() == j.getDni()) {
                        eqFinal.setCapitan(null);
                    }
                    eqFinal.removerJugador(j);
                    eqFinal.agregarJugador(normal);
                    actualizarEntidad(eqFinal);
                }

                // Reemplazar en la lista de jugadores
                for (int i = 0; i < jugadores.size(); i++) {
                    if (jugadores.get(i).getDni() == j.getDni() || jugadores.get(i).equals(j)) {
                        jugadores.set(i, normal);
                        break;
                    }
                }

                // Si es el usuario logueado actualmente, actualizar sesión y permisos a JUGADOR
                if (this.jugadorActual != null && (this.jugadorActual.getDni() == j.getDni() || this.jugadorActual.equals(j))) {
                    this.jugadorActual = normal;
                    this.rolActual = "JUGADOR";
                }

                actualizarEntidad(normal);
                return "OK: Perfil actualizado. Categoría cambiada a JUGADOR (permisos de capitán revocados exitosamente).";

            } else if (!(j instanceof Capitan) && esCapitan) {
                Capitan cap = new Capitan();
                cap.setId(j.getId());
                cap.setDni(dni);
                cap.setNombre(nombreCompleto.trim());
                cap.setEmail(mailLimpio);
                cap.setContrasenia(passLimpia);
                cap.setPosicion(posicion != null && !posicion.trim().isEmpty() ? posicion.trim() : (j.getPosicion() != null ? j.getPosicion() : "Capitán / Polifuncional"));
                cap.setFechaNacimiento(j.getFechaNacimiento());
                cap.setEstado(j.getEstado() != null ? j.getEstado() : "Activo");
                cap.setGoles(j.getGoles());
                cap.setTarjetas(j.getTarjetas());
                cap.setCredencial(j.getCredencial());
                cap.setSanciones(j.getSanciones());
                cap.setNivelAccesoCapitan("Capitán Titular");

                if (eqFinal != null) {
                    eqFinal.setCapitan(cap);
                    eqFinal.removerJugador(j);
                    if (!eqFinal.existeJugador(cap.getDni())) {
                        eqFinal.agregarJugador(cap);
                    }
                    actualizarEntidad(eqFinal);
                }

                for (int i = 0; i < jugadores.size(); i++) {
                    if (jugadores.get(i).getDni() == j.getDni() || jugadores.get(i).equals(j)) {
                        jugadores.set(i, cap);
                        break;
                    }
                }

                if (this.jugadorActual != null && (this.jugadorActual.getDni() == j.getDni() || this.jugadorActual.equals(j))) {
                    this.jugadorActual = cap;
                    this.rolActual = "CAPITAN";
                }

                actualizarEntidad(cap);
                return "OK: Perfil actualizado. Categoría promovida a CAPITÁN (permisos de capitán habilitados exitosamente).";

            } else {
                j.setDni(dni);
                j.setNombre(nombreCompleto.trim());
                j.setEmail(mailLimpio);
                j.setContrasenia(passLimpia);
                if (posicion != null && !posicion.trim().isEmpty()) {
                    j.setPosicion(posicion.trim());
                }
                if (esCapitan && j instanceof Capitan && eqFinal != null) {
                    eqFinal.setCapitan((Capitan) j);
                    actualizarEntidad(eqFinal);
                }
                actualizarEntidad(j);
                return "OK: Perfil de " + (esCapitan ? "Capitán" : "Jugador") + " actualizado correctamente.";
            }
        }
        return "Error: Tipo de perfil no reconocido.";
    }

    public String altaCancha(String nombre, String ubicacion, boolean disponible) {
        Cancha c = new Cancha();
        c.registrarAlta(nombre, ubicacion, disponible);
        if (isBdConectada()) {
            Usuario adminRef = (usuarioActual instanceof Administrador) ? usuarioActual : adminMotor;
            ControladorAdministrador ctrl = new ControladorAdministrador(adminRef, em);
            String rta = ctrl.registrarAltaCancha(c);
            canchas.add(c);
            return rta;
        } else {
            c.setId(canchas.size() + 1);
            canchas.add(c);
            return "Cancha registrada en memoria.";
        }
    }

    public String modificarCancha(Cancha c, String nombre, String ubicacion, boolean disponible) {
        c.modificar(nombre, ubicacion, disponible);
        if (isBdConectada()) {
            Usuario adminRef = (usuarioActual instanceof Administrador) ? usuarioActual : adminMotor;
            ControladorAdministrador ctrl = new ControladorAdministrador(adminRef, em);
            return ctrl.modificarCancha(c);
        }
        return "Cancha modificada.";
    }

    public String bajaCancha(Cancha c) {
        c.registrarBaja();
        if (isBdConectada()) {
            actualizarEntidad(c);
        }
        return "Cancha marcada como No Disponible.";
    }


    public String altaTorneo(String nombre, Date fechaInicio) {
        Torneo t = new Torneo();
        t.registrarAlta(nombre, fechaInicio);
        for (Cancha c : canchas) t.getCanchas().add(c);
        for (Equipo e : equipos) t.getEquipos().add(e);
        if (isBdConectada()) {
            Usuario adminRef = (usuarioActual instanceof Administrador) ? usuarioActual : adminMotor;
            ControladorAdministrador ctrl = new ControladorAdministrador(adminRef, em);
            String rta = ctrl.registrarAltaTorneo(t);
            torneos.add(t);
            return rta;
        } else {
            t.setId(torneos.size() + 1);
            torneos.add(t);
            return "Torneo creado.";
        }
    }

    public String modificarTorneo(Torneo t, String nombre, Date fecha, String fase) {
        t.modificar(nombre, fecha);
        if (fase != null && !fase.trim().isEmpty()) {
            t.setFaseActual(fase.trim());
        }
        actualizarEntidad(t);
        return "Torneo actualizado correctamente.";
    }

    public String altaTorneoPersonalizado(String nombre, Date fechaInicio, List<Equipo> equiposSeleccionados) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "Error: Ingresá un nombre para el torneo.";
        }
        if (equiposSeleccionados == null || equiposSeleccionados.size() < 2) {
            return "Error: Debés seleccionar al menos 2 equipos para crear el torneo.";
        }
        for (Equipo e : equiposSeleccionados) {
            if (e.getInscripcion() == null || !"Habilitado".equalsIgnoreCase(e.getInscripcion().getEstado())) {
                return "Error: El equipo '" + e.getNombre() + "' no puede formar parte del torneo porque su inscripción está en estado PENDIENTE. Debe ser aprobado/habilitado primero.";
            }
        }
        Torneo t = new Torneo();
        t.registrarAlta(nombre.trim(), fechaInicio != null ? fechaInicio : new Date());
        for (Cancha c : canchas) t.getCanchas().add(c);
        for (Equipo e : equiposSeleccionados) t.getEquipos().add(e);

        List<Partido> partidosSorteados = t.sortearYGenerarFaseDeGrupos(canchas, getArbitros());
        for (Partido p : partidosSorteados) {
            guardarEntidad(p);
            partidos.add(p);
        }

        if (isBdConectada()) {
            Usuario adminRef = (usuarioActual instanceof Administrador) ? usuarioActual : adminMotor;
            ControladorAdministrador ctrl = new ControladorAdministrador(adminRef, em);
            String rta = ctrl.registrarAltaTorneo(t);
            torneos.add(t);
            return rta + " Torneo creado con " + equiposSeleccionados.size() + " equipos y " + partidosSorteados.size() + " partidos sorteados.";
        } else {
            t.setId(torneos.size() + 1);
            torneos.add(t);
            return "Torneo creado con " + equiposSeleccionados.size() + " equipos y " + partidosSorteados.size() + " partidos sorteados en Fase de Grupos.";
        }
    }

    public String sortearFaseDeGruposTorneo(Torneo t) {
        if (t == null) return "Seleccioná un torneo.";
        if (t.getEquipos() == null || t.getEquipos().size() < 2) {
            return "El torneo debe tener al menos 2 equipos para realizar el sorteo.";
        }
        List<Partido> nuevos = t.sortearYGenerarFaseDeGrupos(canchas, getArbitros());
        for (Partido p : nuevos) {
            guardarEntidad(p);
            partidos.add(p);
        }
        actualizarEntidad(t);
        return "¡Fase de Grupos sorteada con éxito! Se programaron " + nuevos.size() + " partidos automáticamente.";
    }

    public String agregarEquipoATorneo(Torneo t, Equipo e) {
        if (t == null || e == null) return "Seleccioná el torneo y el equipo.";
        if (e.getInscripcion() == null || !"Habilitado".equalsIgnoreCase(e.getInscripcion().getEstado())) {
            return "Error: No se puede sumar el equipo '" + e.getNombre() + "' al torneo porque su inscripción está en estado PENDIENTE. Debe ser aprobado/habilitado por el Administrador u Organizador.";
        }
        if (t.getEquipos().contains(e)) {
            return "El equipo ya forma parte de este torneo.";
        }
        t.getEquipos().add(e);
        actualizarEntidad(t);
        return "Equipo '" + e.getNombre() + "' agregado al torneo '" + t.getNombre() + "'.";
    }

    public String darDeBajaEquipoDeTorneo(Torneo t, Equipo e) {
        if (t == null || e == null) return "Seleccioná el torneo y el equipo.";
        if (!t.getEquipos().contains(e)) {
            return "El equipo no se encuentra en este torneo.";
        }
        t.getEquipos().remove(e);
        actualizarEntidad(t);
        return "Equipo '" + e.getNombre() + "' dado de baja del torneo '" + t.getNombre() + "'.";
    }

    public String avanzarFaseTorneo(Torneo t) {
        if (t == null) return "Seleccioná un torneo.";
        List<Partido> partidosFase = t.consultarFixture(partidos, t.getFaseActual());

        if ("Final".equalsIgnoreCase(t.getFaseActual())) {
            for (Partido p : partidosFase) {
                if ("Finalizado".equals(p.getEstado()) && p.getGanador() != null) {
                    t.definirCampeon(p);
                    actualizarEntidad(t);
                    return "🏆 ¡El torneo '" + t.getNombre() + "' ha finalizado! Campeón dictaminado automáticamente: " + t.getCampeon().getNombre();
                }
            }
            return "El partido de la Final aún no ha finalizado o no cuenta con un ganador para dictaminar al campeón.";
        }

        List<Partido> nuevos = t.avanzarFase(partidosFase);
        if (nuevos.isEmpty()) {
            return "No se pudo avanzar de fase: verificá que todos los partidos de la fase '" + t.getFaseActual()
                    + "' estén Finalizados y haya suficientes equipos clasificados.";
        }
        for (int i = 0; i < nuevos.size(); i++) {
            Partido np = nuevos.get(i);
            if (!canchas.isEmpty() && np.getCancha() == null) {
                np.setCancha(canchas.get(i % canchas.size()));
            }
            List<Arbitro> arbs = getArbitros();
            if (!arbs.isEmpty() && np.getArbitro() == null) {
                np.setArbitro(arbs.get(i % arbs.size()));
            }
            if (np.getFecha() == null) {
                np.setFecha(new Date());
            }
            guardarEntidad(np);
            partidos.add(np);
        }
        actualizarEntidad(t);
        return "¡Fase avanzada a " + t.getFaseActual() + "! Se generaron " + nuevos.size() + " nuevo(s) encuentro(s).";
    }

    public String definirCampeonTorneo(Torneo t, Partido partidoFinal) {
        if (t == null || partidoFinal == null) return "Seleccioná el torneo y el partido final.";
        boolean ok = t.definirCampeon(partidoFinal);
        if (ok) {
            actualizarEntidad(t);
            return "🏆 ¡Campeón definido! El campeón de " + t.getNombre() + " es: " + t.getCampeon().getNombre();
        }
        return "El partido seleccionado debe ser de fase 'Final', estar 'Finalizado' y tener un ganador.";
    }


    public String registrarEquipoNuevo(String nombre, String categoria, String camiseta, String sede, Capitan capitan) {
        return registrarEquipoNuevo(nombre, categoria, camiseta, sede, capitan, null);
    }

    public String registrarEquipoNuevo(String nombre, String categoria, String camiseta, String sede, Capitan capitan, String escudo) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "Error: Ingresá el nombre del equipo.";
        }
        boolean sedeExiste = false;
        if (sede != null) {
            for (Cancha c : canchas) {
                if (c.getNombre() != null && c.getNombre().equalsIgnoreCase(sede.trim())) {
                    sedeExiste = true;
                    break;
                }
            }
        }
        if (!sedeExiste) {
            return "Error: La sede habitual debe ser una cancha previamente registrada en el sistema.";
        }
        Equipo eq = new Equipo();
        eq.setCategoria(categoria);
        eq.setColorCamiseta(camiseta);
        eq.setSedeHabitual(sede);
        if (escudo != null && !escudo.trim().isEmpty()) {
            eq.setEscudo(escudo.trim());
        }
        eq.registrarEquipo(nombre, capitan);
        if (capitan != null && !eq.existeJugador(capitan.getDni())) {
            eq.agregarJugador(capitan);
        }
        if (eq.getInscripcion() != null) {
            guardarEntidad(eq.getInscripcion());
        }
        guardarEntidad(eq);
        equipos.add(eq);
        return "Equipo '" + nombre + "' registrado con inscripción en estado Pendiente.";
    }

    public String modificarEquipo(Equipo eq, String nombre, String categoria, String camiseta, String sede, Jugador capitan, String escudo) {
        if (eq == null) return "Seleccioná un equipo para modificar.";
        if (nombre == null || nombre.trim().isEmpty()) return "Error: Ingresá el nombre del equipo.";
        boolean sedeExiste = false;
        if (sede != null) {
            for (Cancha c : canchas) {
                if (c.getNombre() != null && c.getNombre().equalsIgnoreCase(sede.trim())) {
                    sedeExiste = true;
                    break;
                }
            }
        }
        if (!sedeExiste) {
            return "Error: La sede habitual debe ser una cancha previamente registrada en el sistema.";
        }
        eq.setNombre(nombre.trim());
        if (categoria != null && !categoria.trim().isEmpty()) eq.setCategoria(categoria.trim());
        if (camiseta != null && !camiseta.trim().isEmpty()) eq.setColorCamiseta(camiseta.trim());
        eq.setSedeHabitual(sede.trim());
        if (capitan != null) {
            cambiarCapitanEquipo(eq, capitan);
        }
        if (escudo != null && !escudo.trim().isEmpty()) {
            eq.setEscudo(escudo.trim());
        }
        actualizarEntidad(eq);
        return "¡Equipo '" + eq.getNombre() + "' modificado con éxito!";
    }

    public String eliminarEquipo(Equipo eq) {
        if (eq == null) return "Seleccioná un equipo para eliminar.";
        String nombre = eq.getNombre();
        // 1. Quitar equipo de todos los torneos
        for (Torneo t : torneos) {
            if (t.getEquipos() != null && t.getEquipos().contains(eq)) {
                t.getEquipos().remove(eq);
                actualizarEntidad(t);
            }
        }

        if (eq.getPlantel() != null) {
            eq.getPlantel().clear();
        }

        equipos.remove(eq);

        eliminarEntidad(eq);
        return "Equipo '" + nombre + "' eliminado correctamente del sistema.";
    }

    public String agregarJugadorAEquipo(Equipo equipo, long dni, String nombre, String posicion, Date fechaNac, String email, String pass) {
        return agregarJugadorAEquipo(equipo, dni, nombre, posicion, fechaNac, email, pass, "Activo");
    }

    public String agregarJugadorAEquipo(Equipo equipo, long dni, String nombre, String posicion, Date fechaNac, String email, String pass, String estado) {
        if (equipo == null) return "Seleccioná un equipo.";
        if (jugadorYaPerteneceAOtroEquipo(dni, equipo)) {
            Equipo otro = null;
            for (Equipo e : equipos) {
                if (e.existeJugador(dni) || (e.getCapitan() != null && e.getCapitan().getDni() == dni)) {
                    otro = e;
                    break;
                }
            }
            String nombreOtro = (otro != null) ? otro.getNombre() : "otro equipo";
            return "Error: El jugador con DNI " + dni + " ya está registrado en '" + nombreOtro + "'. No puede pertenecer a 2 o más equipos.";
        }
        Capitan cap = equipo.getCapitan();
        if (cap != null) {
            boolean ok = cap.cargaPlantel(equipo, equipos, dni, nombre, posicion, fechaNac);
            if (!ok) {
                return "No se pudo agregar: verificá que el DNI no esté repetido en este u otro equipo del torneo.";
            }
            Jugador agregado = equipo.getPlantel().get(equipo.getPlantel().size() - 1);
            agregado.setEmail(email != null && !email.isEmpty() ? email : "jug" + dni + "@ugd.edu.ar");
            agregado.setContrasenia(pass != null && !pass.isEmpty() ? pass : "1234");
            if (estado != null && !estado.trim().isEmpty()) {
                agregado.setEstado(estado.trim());
            }
            Credencial cred = new Credencial();
            cred.generar();
            agregado.setCredencial(cred);
            guardarEntidad(cred);
            guardarEntidad(agregado);
            actualizarEntidad(equipo);
            jugadores.add(agregado);
            return "Jugador '" + nombre + "' (" + agregado.getEstado() + ") agregado al plantel de " + equipo.getNombre() + " con credencial " + cred.getCodigoBarras();
        } else {
            Jugador nuevo = new Jugador();
            nuevo.registrarAltaDesktop(dni, nombre, fechaNac, posicion);
            nuevo.setEmail(email != null && !email.isEmpty() ? email : "jug" + dni + "@ugd.edu.ar");
            nuevo.setContrasenia(pass != null && !pass.isEmpty() ? pass : "1234");
            if (estado != null && !estado.trim().isEmpty()) {
                nuevo.setEstado(estado.trim());
            }
            Credencial cred = new Credencial();
            cred.generar();
            nuevo.setCredencial(cred);
            guardarEntidad(cred);
            guardarEntidad(nuevo);
            equipo.agregarJugador(nuevo);
            actualizarEntidad(equipo);
            jugadores.add(nuevo);
            return "Jugador '" + nombre + "' (" + nuevo.getEstado() + ") dado de alta en " + equipo.getNombre();
        }
    }

    public String adjuntarDocumentacionEquipo(Equipo equipo, String rutaOArchivo) {
        if (equipo == null) return "Seleccioná un equipo.";
        if (equipo.getInscripcion() == null) {
            Inscripcion ins = new Inscripcion(0, new Date());
            guardarEntidad(ins);
            equipo.setInscripcion(ins);
        }
        if (equipo.getCapitan() != null) {
            equipo.getCapitan().cargarDocumentacion(equipo, rutaOArchivo);
        } else {
            equipo.getInscripcion().adjuntarDocumentacion(rutaOArchivo);
        }
        actualizarEntidad(equipo.getInscripcion());
        actualizarEntidad(equipo);
        return "Documentación '" + rutaOArchivo + "' adjuntada a la inscripción de " + equipo.getNombre();
    }

    public String evaluarInscripcionEquipo(Equipo equipo, int edadMinima, boolean docValida, String motivoRechazo) {
        if (equipo == null) return "Seleccioná un equipo.";
        boolean exito = organizadorMotor.evaluarInscripcion(equipo, edadMinima, docValida, motivoRechazo);
        if (equipo.getInscripcion() != null) {
            actualizarEntidad(equipo.getInscripcion());
        }
        actualizarEntidad(equipo);
        if (exito) {
            return "Inscripción APROBADA y HABILITADA para '" + equipo.getNombre() + "' (Notificación enviada al Capitán).";
        } else {
            String mot = (equipo.getInscripcion() != null) ? equipo.getInscripcion().consultarEstado() : motivoRechazo;
            return "Inscripción RECHAZADA: " + mot;
        }
    }

    public String programarPartido(Equipo local, Equipo visitante, Date fecha, String hora, String fase, Cancha cancha, Arbitro arbitro) {
        boolean exito = organizadorMotor.programarPartidos(local, visitante, fecha, hora, fase, cancha, arbitro, partidos);
        if (exito) {
            Partido nuevo = partidos.get(partidos.size() - 1);
            guardarEntidad(nuevo);
            return "Partido programado: " + local.getNombre() + " vs " + visitante.getNombre() + " (Correos enviados y guardado en BD).";
        }
        return "No se pudo programar: verificá que ambos equipos estén 'Habilitado', no sean el mismo equipo, y la cancha/árbitro estén disponibles en ese horario.";
    }

    public String postergarPartido(Partido p, String motivo) {
        if (p == null) return "Seleccioná un partido.";
        if (p.postergar(motivo)) {
            actualizarEntidad(p);
            return "Partido postergado correctamente. Motivo: " + motivo;
        }
        return "Solo se pueden postergar partidos en estado 'Pendiente'.";
    }

    public String suspenderPartido(Partido p, String motivo) {
        if (p == null) return "Seleccioná un partido.";
        if (p.suspender(motivo)) {
            actualizarEntidad(p);
            return "Partido suspendido correctamente. Motivo: " + motivo;
        }
        return "No se pudo suspender el partido (verificá que no esté ya Finalizado).";
    }

    public Arbitro buscarArbitroDisponible(Date fecha, String hora) {
        for (Arbitro a : getArbitros()) {
            if (a.consultarDisponibilidad(fecha, hora, partidos)) {
                return a;
            }
        }
        return null;
    }

    public String reprogramarPartido(Partido p, Date nuevaFecha, String nuevaHora, Cancha nuevaCancha) {
        if (p == null) return "Seleccioná un partido.";
        boolean ok = organizadorMotor.reprogramarPartido(p, nuevaFecha, nuevaHora, nuevaCancha, partidos);
        if (ok) {
            actualizarEntidad(p);
            return "Partido reprogramado para las " + nuevaHora + " hs en " + nuevaCancha.getNombre();
        }
        return "No se pudo reprogramar: verificá que la cancha esté libre en esa fecha/hora.";
    }

    public String cargarResultadoPartido(Partido p, int golesL, int golesV, List<Incidente> nuevosIncidentes) {
        if (p == null) return "Seleccioná un partido.";
        if (nuevosIncidentes != null) {
            for (Incidente inc : nuevosIncidentes) {
                guardarEntidad(inc);
                if ("Gol".equalsIgnoreCase(inc.getTipo()) && inc.getJugador() != null) {
                    inc.getJugador().setGoles(inc.getJugador().getGoles() + 1);
                    actualizarEntidad(inc.getJugador());
                }
            }
        }
        boolean ok = p.cargarResultado(golesL, golesV, nuevosIncidentes);
        if (ok) {
            if (nuevosIncidentes != null) {
                for (Incidente inc : nuevosIncidentes) {
                    if (inc.getJugador() != null && inc.getJugador().getSanciones() != null) {
                        for (Sancion s : inc.getJugador().getSanciones()) {
                            if (s.getId() == 0) {
                                guardarEntidad(s);
                                if (!sanciones.contains(s)) sanciones.add(s);
                            }
                        }
                        actualizarEntidad(inc.getJugador());
                    }
                }
            }
            actualizarEntidad(p);

            if ("Final".equalsIgnoreCase(p.getFase()) && p.getGanador() != null) {
                Torneo torneoDestino = null;
                for (Torneo t : torneos) {
                    if (t.getEquipos() != null && (t.getEquipos().contains(p.getEquipoLocal()) || t.getEquipos().contains(p.getEquipoVisitante()))) {
                        if ("Final".equalsIgnoreCase(t.getFaseActual())) {
                            torneoDestino = t;
                            break;
                        } else if (torneoDestino == null) {
                            torneoDestino = t;
                        }
                    }
                }
                if (torneoDestino != null) {
                    torneoDestino.definirCampeon(p);
                    actualizarEntidad(torneoDestino);
                    return "¡Resultado registrado (" + p.getResultado() + ")! 🏆 ¡CAMPEÓN DICTAMINADO AUTOMÁTICAMENTE: "
                            + p.getGanador().getNombre() + "! El torneo '" + torneoDestino.getNombre() + "' ha finalizado.";
                }
            }
            return "Resultado final registrado (" + p.getResultado() + ") e incidentes/sanciones guardados en MySQL.";
        }
        return "No se pudo cargar el resultado: verificá que el partido no esté ya Finalizado.";
    }

    public String buscarNombreTorneoDePartido(Partido p) {
        if (p == null) return "-";
        for (Torneo t : torneos) {
            if (t.getEquipos() != null && (t.getEquipos().contains(p.getEquipoLocal()) || t.getEquipos().contains(p.getEquipoVisitante()))) {
                return t.getNombre();
            }
        }
        if (!torneos.isEmpty()) {
            return torneos.get(0).getNombre();
        }
        return "Torneo General";
    }

    public String aplicarSancionJugador(Jugador j, String tipo, String motivo, Date inicio, Date fin) {
        if (j == null) return "Error: Debés seleccionar un jugador de la lista antes de aplicar una sanción.";
        if (usuarioActual instanceof Arbitro) {
            ((Arbitro) usuarioActual).aplicarSancion(j, tipo, motivo, inicio, fin);
        } else {
            organizadorMotor.aplicarSanciones(j, tipo, motivo, inicio, fin);
        }
        if (j.getSanciones() != null && !j.getSanciones().isEmpty()) {
            Sancion ult = j.getSanciones().get(j.getSanciones().size() - 1);
            guardarEntidad(ult);
            if (!sanciones.contains(ult)) {
                sanciones.add(ult);
            }
        }
        actualizarEntidad(j);
        return "Sanción aplicada a " + j.getNombre() + " (Estado actualizado a Suspendido).";
    }

    public String presentarReclamoCapitan(Capitan capitan, Partido partido, String motivo, Equipo equipo) {
        if (partido == null) return "Seleccioná un partido.";
        if (!"Finalizado".equalsIgnoreCase(partido.getEstado())) {
            return "Solo se pueden realizar reclamos sobre partidos que estén en estado 'Finalizado'.";
        }
        Capitan capRef = capitan != null ? capitan : (equipo != null ? equipo.getCapitan() : null);
        if (capRef == null) return "El equipo debe tener un Capitán asignado.";
        Equipo eqRef = equipo != null ? equipo : buscarEquipoDeJugador(capRef);
        Reclamo nuevo = capRef.realizarReclamo(partido, motivo, eqRef);
        if (nuevo == null) {
            return "No se pudo crear el reclamo: verificá que tu equipo haya participado en ese partido finalizado y hayas escrito un motivo.";
        }
        guardarEntidad(nuevo);
        actualizarEntidad(capRef);
        reclamos.add(nuevo);
        return "Reclamo presentado exitosamente en estado 'Pendiente'.";
    }

    public String examinarReclamo(Reclamo r) {
        if (r == null) return "Seleccioná un reclamo.";
        if (organizadorMotor.examinarReclamo(r)) {
            actualizarEntidad(r);
            return "El reclamo #" + r.getId() + " pasó a estado 'En Revision'.";
        }
        return "El reclamo debe estar en estado 'Pendiente' para pasar a revisión.";
    }

    public String resolverReclamo(Reclamo r, boolean aceptado, String dictamen) {
        if (r == null) return "Seleccioná un reclamo.";
        if (organizadorMotor.resolverReclamo(r, aceptado, dictamen)) {
            actualizarEntidad(r);
            return "Reclamo #" + r.getId() + " resuelto como '" + r.getEstado() + "'.";
        }
        return "Ingresá el dictamen/motivo de resolución.";
    }


    public List<Cancha> getCanchas() { return canchas; }
    public List<Torneo> getTorneos() { return torneos; }
    public List<Usuario> getUsuarios() { return usuarios; }
    public List<Equipo> getEquipos() { return equipos; }
    public List<Jugador> getJugadores() { return jugadores; }
    public List<Partido> getPartidos() { return partidos; }
    public List<Reclamo> getReclamos() { return reclamos; }
    public List<Sancion> getSanciones() { return sanciones; }

    public List<Arbitro> getArbitros() {
        List<Arbitro> lista = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u instanceof Arbitro) lista.add((Arbitro) u);
        }
        return lista;
    }

    public List<Capitan> getCapitanes() {
        List<Capitan> lista = new ArrayList<>();
        for (Jugador j : jugadores) {
            if (j instanceof Capitan) lista.add((Capitan) j);
        }
        return lista;
    }

    public ServicioPDF getServicioPDF() {
        return servicioPDF;
    }
}
