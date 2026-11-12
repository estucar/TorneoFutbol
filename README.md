# TorneoFutbol
Aplicación de escritorio desarrollada en Java para la administración de torneos de futbol, inscripciones, control disciplinario, emisión de credenciales y generación de estadísticas. Proyecto desarrollado para la cátedra de Programación Avanzada II (Ingeniería en Informática - UGD).

Tecnologías y Librerías
- Lenguaje: Java (JDK 8+)
- Interfaz Gráfica: Java Swing / AWT
- Persistencia (ORM): JPA con EclipseLink
- Base de Datos: MySQL (`mysql-connector-java 8.0`)
- Generación de Reportes: iTextPDF (`itextpdf 5.5.9`)
- Notificaciones: JavaMail API (`javax.mail 1.6.2`)

Funcionalidades Principales
- Gestión de Torneos y Canchas: Creación de competencias, administración de sedes, programación de fechas y asignación de árbitros.
- Equipos y Jugadores: Registro de capitanes, inscripción de planteles y validación de listas de buena fe.
- Control Disciplinario: Registro de incidentes en partidos, cómputo automático de sanciones y gestión de reclamos.
- Credenciales Imprimibles: Generación visual de credenciales de jugadores con código de barras gráfico.
- Reportes en PDF y Notificaciones: Exportación automática de tabla de posiciones, tabla de goleadores, jugadores más sancionados, historial por equipo/jugador y envío de notificaciones por correo electrónico.

Instalación y Ejecución
- Java JDK 8 o superior.
- Servidor MySQL activo.
- NetBeans IDE.
