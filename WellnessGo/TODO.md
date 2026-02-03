## TO DO - Tareas por hacer

_Hecho_ - Archivo Settings en cada app con los datos de configuración. Incluyendo este fichero en los mains de las distintas aplicaciones podemos cambiar de base de datos, entornos, ... con solo modificar Settings. Definir constantes
_Hecho_ - ¿Texto o XML?  Texto plano

### Persistencia de datos

**Datos de cada aplicación (local)**
- Base de datos SQLite
- Preferencias del usuario
- Disponibilidad profesional

**Datos en la plataforma**
- IP servidor, cadena de conexión, usuario y passwd de la base de datos (Settings). 
    - Para pruebas locales, establecer datos para base local. En pre y producción, cambiar ip- cadena de conexión al servidor de la aplicación
_Hecho_: - Elección y configuración del servidor: Se configura servidor de pruebas con dns dinámica, con XAMP
_Hecho_- Configurar rutas y puertos: 53306 para MySQL Server, 8080 para API, 8098 para phpMyAdmin

- Base de datos MySQL
    - Diseñar tablas según diagrama de clases
    - Diseñar procedimientos almacenados

- ¿WebServices o ataques directos a la base?
  _Hecho_ - Por seguridad, secrea API en Java. Intercambio de datos formato JSON

**Tomas de decisión**
- Altas, bajas, modificaciones clientes y profesionales
- Mantenimiento de la aplicación
