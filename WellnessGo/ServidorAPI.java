package main;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;

public class ServidorAPI {

	private static BasicDataSource bds;
	private static DataSource ds;
	Connection conn;
	int puerto;
	protected static final Gson gson = new Gson();
	private static final String LOG_FILE = "log_api.txt";
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	// Configuración base de datos
	public static void main(String[] args) throws Exception {
		ServidorAPI app = new ServidorAPI();
		int puerto = 8080;
		app.gestorBD();

		// Crea servidor HTTP en el puerto 8080

		HttpServer server = HttpServer.create(new InetSocketAddress(puerto), 0);
		server.createContext("/usuarios", new UsuarioHandler());
		server.createContext("/citas", new CitasHandler());
		server.setExecutor(null);
		System.out.println("Servidor iniciado en http://localhost:8080");
		server.start();
	}

	/**
	 * Establece una conexión con la base de datos
	 */
	protected void gestorBD() {

		/*
		 * Extraemos variables de configuración del fichero config.propierties. El
		 * fichero debe estar en la carpeta raíz del proyecto, al nivel de bin y src
		 */

		String rutaFichero = "config.properties";

		try (BufferedReader lector = new BufferedReader(new FileReader(rutaFichero))) {
			String linea;

			// Mapa para almacenar las configuraciones
			Map<String, String> varConexion = new HashMap<>();
			// Leer cada línea del archivo de configuración
			while ((linea = lector.readLine()) != null) {
				// Dividir la línea en clave=valor
				String[] partes = linea.split("=", 2);
				if (partes.length == 2) {
					varConexion.put(partes[0].trim(), partes[1].trim());
				}
			}
// Con esto creamos un pool de conexiones
			bds = new BasicDataSource();
			bds.setUrl(varConexion.get("db.url"));
			bds.setUsername(varConexion.get("db.user"));
			bds.setPassword(varConexion.get("db.password"));
			bds.setDriverClassName("com.mysql.cj.jdbc.Driver");
			this.puerto = Integer.parseUnsignedInt(varConexion.get("puerto"));

			// Configuración del pool
			bds.setInitialSize(2);
			bds.setMaxTotal(5);
			bds.setMinIdle(2);
			bds.setMaxIdle(5);

			ds = bds;
			System.out.println("Conectando a BD: " + varConexion.get("db.url"));
			System.out.println("Usuario: " + varConexion.get("db.user"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Escribe logs en un fichero de texto
	 * 
	 * @param exception
	 * @param urlCompleta
	 */
	private static void logApi(Exception exception, String urlCompleta) {
		String fechaHora = LocalDateTime.now().format(FORMATTER);
		String mensajeEx = (exception != null) ? exception.getMessage() : "OK";
		String lineaLog = fechaHora + " - " + mensajeEx + " - " + urlCompleta;

		try (FileWriter fw = new FileWriter(LOG_FILE, true); PrintWriter pw = new PrintWriter(fw)) {
			pw.println(lineaLog);
		} catch (IOException e) {
			e.printStackTrace(); // fallo al escribir el log
		}
	}

	/**
	 * Envía respuesta HttpExchange
	 * 
	 * @param exchange
	 * @param status
	 * @param response
	 * @throws IOException
	 */
	static void sendResponse(HttpExchange exchange, int status, String response) throws IOException {
		exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
		byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(status, bytes.length);
		try (OutputStream os = exchange.getResponseBody()) {
			os.write(bytes);
		}
	}

	/**
	 * Manejador principal de /usuarios
	 * 
	 */
	static class UsuarioHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String method = exchange.getRequestMethod();
			String path = exchange.getRequestURI().getPath(); // ejemplo: /citas/dni/12345678A

			switch (method) {
			case "GET":
				handleGet(exchange, path);
				break;
			case "POST":
				handlePostUsario(exchange);
				break;
			default:
				exchange.sendResponseHeaders(405, -1); // Método no permitido
				break;
			}
		}

		/**
		 * Obtiene datos de un usuario por GET
		 * 
		 * @param exchange
		 * @throws IOException
		 */
		private void handleGet(HttpExchange exchange, String path) throws IOException {
			String[] partes = path.split("/"); // ["", "citas", "dni", "12345678A"]
			if (partes.length == 4) {

				switch (partes[2]) {
				case "dni":
					handleGetUsrDni(exchange, partes[3]);
					break;
				case "email":
					handleGetUsrEmail(exchange, partes[3]);
					break;
				case "telefono":
					handleGetUsrtelefono(exchange, partes[3]);
					break;
				default:
					sendResponse(exchange, 404, "Parámetro no contemplado");
					break;
				}
			} else if (partes.length == 2 || partes.length == 3) {
				handleGetUsuarios(exchange);
			} else {
				sendResponse(exchange, 404, "Ruta no encontrada");
			}
		}

		private void handleGetUsrDni(HttpExchange exchange, String string) {
			// TODO Auto-generated method stub

		}

		private void handleGetUsrEmail(HttpExchange exchange, String string) {
			// TODO Auto-generated method stub

		}

		private void handleGetUsrtelefono(HttpExchange exchange, String string) {
			// TODO Auto-generated method stub

		}

		/**
		 * Devuelve todos los usuarios de la base de datos
		 * 
		 * @param exchange
		 * @throws IOException
		 */
		private static void handleGetUsuarios(HttpExchange exchange) throws IOException {
			try (

					Connection conn = ds.getConnection();
					Statement st = conn.createStatement();
					ResultSet rs = st.executeQuery("SELECT * FROM usuarios");) {

				List<Map<String, Object>> usuarios = new ArrayList<>();

				while (rs.next()) {
					Map<String, Object> fila = new HashMap<>();
					fila.put("dni", rs.getString("dni"));
					fila.put("nombre", rs.getString("nombre"));
					fila.put("apellidos", rs.getString("apellidos"));
					fila.put("email", rs.getString("email"));
					fila.put("direccion", rs.getString("direccion"));
					fila.put("Cod_postal", rs.getString("cod_postal"));
					fila.put("telefono", rs.getString("telefono"));
					usuarios.add(fila);
				}

				String json = gson.toJson(usuarios); // Recibe un objeto y devuelve un string

				sendResponse(exchange, 200, json);

			} catch (SQLException e) {
				e.printStackTrace(System.err);
				sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
			}

		}

		/**
		 * Manda datos por POST
		 * 
		 * @param exchange
		 * @throws IOException
		 */
		private static void handlePostUsario(HttpExchange exchange) throws IOException {
			String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
			UsrForJson user = gson.fromJson(body, UsrForJson.class); // Recibe un objeto JSON y devuelve un objeto
																		// Usuario

			// Aquí podrías guardar la cita en la BD o devolverla como confirmación
			String response = "Cita recibida:\n" + gson.toJson(user); // Parsea un objeto a JSON

			System.out.println(body);

			// Comprobación de campos obligatorios
			if (user.getDni() == null) {
				sendResponse(exchange, 400, "{\"error\":\"Falta el campo DNI\"}");
				return;
			}
			if (user.getNombre() == null) {
				sendResponse(exchange, 400, "{\"error\":\"Falta el campo Nombre\"}");
				return;
			}

			if (user.getEmail() == null) {
				sendResponse(exchange, 400, "{\"error\":\"Falta el campo Apellidos\"}");
				return;
			}
			if (user.getTelefono() == null) {
				sendResponse(exchange, 400, "{\"error\":\"Falta el campo Telefono\"}");
				return;
			}

			try {
				insertaUsuario(user);
				sendResponse(exchange, 201, "{\"Resultado\": \"Éxito\"}");
			} catch (IOException e) {
				sendResponse(exchange, 500, "{\"Error\":\"Error insertando usuario\"}");
				e.printStackTrace();
			}
		}

		/**
		 * Inserta en la base de datos los datos de un usuario
		 * 
		 * @param user JSON El usuario
		 * @return true si éxito
		 */
		private static boolean insertaUsuario(UsrForJson user) {
			try (Connection conn = ds.getConnection();
					PreparedStatement ps = conn.prepareStatement(
							"INSERT INTO usuarios (dni, nombre, apellidos, email, direccion, telefono, cod_postal) VALUES (?, ?, ?, ?, ?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS)) {

				ps.setString(1, user.getDni());
				ps.setString(2, user.getNombre());
				ps.setString(3, user.getApellidos());
				ps.setString(4, user.getEmail());
				ps.setString(5, user.getDireccion());
				ps.setString(6, user.getTelefono());
				ps.setString(7, user.getCod_postal());

				ps.executeUpdate();

				return true;

			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
			return false;
		}
	}

	static class CitasHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String method = exchange.getRequestMethod();
			String path = exchange.getRequestURI().getPath(); // ejemplo: /citas/dni/12345678A

			if (method.equals("GET")) {
				handleGet(exchange, path);
			} else if (method.equals("POST")) {
				handlePostCita(exchange);
			} else {
				exchange.sendResponseHeaders(405, -1); // Método no permitido
			}
		}

		private void handleGet(HttpExchange exchange, String path) throws IOException {

			try {
				String[] param = path.split("/"); // ["", "citas", "cliente", "dni", "12345678A", "PENDIENTE"]

				switch (param.length) {
				case 2:
					// Todas las citas
					handleGetCitas(exchange);
					break;
				case 3:
					// Todas las citas de un tipo de usuario
					handleGetCitas(exchange, param[2]);
					break;
				case 4:
					// Todas las citas del tipo de usuario cuyo dni se pasa por parámetro
					handleGetCitas(exchange, param[2], param[3]);
					break;
				case 5:
					// Todas las citas del tipo de usuario cuyo dni se pasa por parámetro
					// y estado en "ACEPTADA","PENDIENTE","CANCELADA"
					Set<String> estadoValido = Set.of("ACEPTADA", "PENDIENTE", "CANCELADA");
					if (estadoValido.contains(param[4]))
						handleGetCitas(exchange, param[2], param[3], param[4]);
					break;
				default:
					sendResponse(exchange, 404, "Ruta no encontrada");
					break;
				}
			} catch (Exception e) {
				e.getMessage();
				e.printStackTrace();
				sendResponse(exchange, 500, "Error no controlado");
			}
		}

		/**
		 * Devuelve sendResponse con todas las citas
		 * 
		 * @param exchange
		 * @throws IOException
		 */
		private void handleGetCitas(HttpExchange exchange) throws IOException {
			// Para escribir en el log
			String urlCompleta = exchange.getRequestURI().toString();
			Exception ex = null;

			try (

					Connection conn = ds.getConnection();
					Statement st = conn.createStatement();
					ResultSet rs = st.executeQuery("SELECT * FROM citas");) {

				List<Map<String, Object>> usuarios = new ArrayList<>();

				while (rs.next()) {
					Map<String, Object> fila = new HashMap<>();
					fila.put("id", rs.getString("id_cita"));
					fila.put("cliente", rs.getString("id_cliente"));
					fila.put("profesional", rs.getString("id_profesional"));
					fila.put("fecha", rs.getString("fecha"));
					fila.put("hora", rs.getString("hora"));
					fila.put("direccion", rs.getString("direccion"));
					fila.put("localidad", rs.getString("localidad"));
					fila.put("cp", rs.getString("cp"));
					fila.put("provincia", rs.getString("provincia"));
					fila.put("estado", rs.getString("estado"));
					usuarios.add(fila);
				}

				String json = gson.toJson(usuarios); // Recibe un objeto y devuelve un string

				sendResponse(exchange, 200, json);

			} catch (SQLException e) {
				e.printStackTrace(System.err);
				sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
			} finally {
				logApi(ex, urlCompleta);
			}
		}

		/**
		 * Devuelve sendResponse con las citas de un tipo de usuario
		 * 
		 * @param exchange
		 * @param string   El tipo de usuario (cliente o profesional)
		 * @throws IOException
		 * @throws SQLException
		 */
		private void handleGetCitas(HttpExchange exchange, String tipoUsr) throws IOException, SQLException {
			// Para escribir en el log
			String urlCompleta = exchange.getRequestURI().toString();
			Exception ex = null;

			List<Map<String, Object>> citas = new ArrayList<>();
			PreparedStatement ps = null;
			try (Connection conn = ds.getConnection();) {

				String tabla;
				switch (tipoUsr) {
				case "cliente":
					tabla = "vista_citas_cliente";
					break;
				case "profesional":
					tabla = "vista_citas_profesional";
					break;
				default:
					throw new IllegalArgumentException("Tipo de usuario inválido");
				}

				// Crear la consulta
				String sql = "SELECT * FROM " + tabla;

				// Preparar y ejecutar la consulta
				ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					// Obtenemos nombres de columnas y valores
					ResultSetMetaData meta = rs.getMetaData(); // Obtiene metadatos de la BD
					int columnas = meta.getColumnCount();

					// Obtenemos los nombres y vaores de los campos de la tabla
					Map<String, Object> c = new LinkedHashMap<>();

					for (int i = 1; i <= columnas; i++) {
						String key = meta.getColumnName(i);
						Object valor = rs.getObject(key);

						c.put(key, valor);
					}
					citas.add(c);
				}
			} catch (SQLException e) {

				e.printStackTrace(System.err);
				sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
				return;
			} finally {
				logApi(ex, urlCompleta);
				ps.close();
			}

			sendResponse(exchange, 200, gson.toJson(citas));
		}

		/**
		 * Devuelve sendResponse con las citas de un usuario determinado
		 * 
		 * @param exchange
		 * @param tipoUsr  El tipo de usuario: Cliente o profesional
		 * @param dni      El dni del ususario
		 * @throws IOException
		 * @throws SQLException
		 */
		private void handleGetCitas(HttpExchange exchange, String tipoUsr, String dni)
				throws IOException, SQLException {
			// Para escribir en el log
			String urlCompleta = exchange.getRequestURI().toString();
			Exception ex = null;

			List<Map<String, Object>> citas = new ArrayList<>();
			PreparedStatement ps = null;
			try (Connection conn = ds.getConnection();) {

				String tabla;
				String usuario;

				switch (tipoUsr) {
				case "cliente":
					tabla = "vista_citas_cliente";
					usuario = "id_cliente";
					break;
				case "profesional":
					tabla = "vista_citas_profesional";
					usuario = "id_profesional";
					break;
				default:
					throw new IllegalArgumentException("Tipo de usuario inválido");
				}

				// Crear la consulta
				String sql = "SELECT * FROM " + tabla + " WHERE " + usuario + " = ?";

				// Preparar y ejecutar la consulta
				ps = conn.prepareStatement(sql);
				ps.setString(1, dni); // Parámetro
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					// Obtenemos nombres de columnas y valores
					ResultSetMetaData meta = rs.getMetaData(); // Obtiene metadatos de la BD
					int columnas = meta.getColumnCount();

					// Obtenemos los nombres y vaores de los campos de la tabla
					Map<String, Object> c = new LinkedHashMap<>();

					for (int i = 1; i <= columnas; i++) {
						String key = meta.getColumnName(i);
						Object valor = rs.getObject(key);

						c.put(key, valor);
					}
					citas.add(c);
				}
			} catch (SQLException e) {
				e.printStackTrace(System.err);
				sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
				return;
			} finally {
				logApi(ex, urlCompleta);
				ps.close();
			}

			sendResponse(exchange, 200, gson.toJson(citas));
		}

		/**
		 * 
		 * Obtiene las citas de un usuario. Puede indicarse en qué estado deben estar
		 * las citas. El usuario puede ser cliente o profesional. El esatado puede ser
		 * "PENDIENTE", "ACEPTADA", "CANCELADA"
		 * 
		 * @param exchange
		 * @param dni      El dni del usuario
		 * @param tipoUsr  El tipo de usuario
		 * @param estado   El estado de la cita
		 * @throws IOException
		 * @throws SQLException
		 */
		private void handleGetCitas(HttpExchange exchange, String tipoUsr, String dni, String estado)
				throws IOException, SQLException {
			// Para escribir en el log
			String urlCompleta = exchange.getRequestURI().toString();
			Exception ex = null;

			List<Map<String, Object>> citas = new ArrayList<>();
			PreparedStatement ps = null;
			try (Connection conn = ds.getConnection();) {

				String tabla;
				String usuario;

				switch (tipoUsr) {
				case "cliente":
					tabla = "vista_citas_cliente";
					usuario = "id_cliente";
					break;
				case "profesional":
					tabla = "vista_citas_profesional";
					usuario = "id_profesional";
					break;
				default:
					throw new IllegalArgumentException("Tipo de usuario inválido");
				}

				// Crear la consulta
				String sql = "SELECT * FROM " + tabla + " WHERE " + usuario + " = ? AND estado = ?";

				// Preparar y ejecutar la consulta
				ps = conn.prepareStatement(sql);
				ps.setString(1, dni);
				ps.setString(2, estado);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					// Obtenemos nombres de columnas y valores
					ResultSetMetaData meta = rs.getMetaData(); // Obtiene metadatos de la BD
					int columnas = meta.getColumnCount();

					// Obtenemos los nombres y vaores de los campos de la tabla
					Map<String, Object> c = new LinkedHashMap<>();

					for (int i = 1; i <= columnas; i++) {
						String key = meta.getColumnName(i);
						Object valor = rs.getObject(key);

						c.put(key, valor);
					}
					citas.add(c);
				}
			} catch (SQLException e) {
				e.printStackTrace(System.err);
				sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
				return;
			} finally {
				ps.close();
			}

			sendResponse(exchange, 200, gson.toJson(citas));
		}

		/**
		 * Manejador de citas POST
		 * 
		 * @param exchange
		 * @throws IOException
		 */
		private void handlePostCita(HttpExchange exchange) throws IOException {
			// Para escribir en el log
			String urlCompleta = exchange.getRequestURI().toString();
			Exception ex = null;

			try {
				String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
				System.out.println("Body recibido: " + body);

				CitaForJson cita = null;
				try {
					cita = gson.fromJson(body, CitaForJson.class);
				} catch (Exception e) {
					sendResponse(exchange, 400, "{\"error\":\"JSON mal formado\"}");
					return;
				}

				if (cita == null) {
					sendResponse(exchange, 400, "{\"error\":\"No se pudo parsear el JSON\"}");
					return;
				}

				System.out.println("Cita recibida: " + gson.toJson(cita));

				// Comprobación de campos obligatorios
				if (cita.getCliente() == null) {
					sendResponse(exchange, 400, "{\"error\":\"Falta el DNI del cliente\"}");
					return;
				}
				if (cita.getProfesional() == null) {
					sendResponse(exchange, 400, "{\"error\":\"Falta el DNI del profesional\"}");
					return;
				}
				if (cita.getFecha() == null) {
					sendResponse(exchange, 400, "{\"error\":\"Falta la fecha de la cita\"}");
					return;
				}
				if (cita.getHora() == null) {
					sendResponse(exchange, 400, "{\"error\":\"Falta la hora de la cita\"}");
					return;
				}
				if (cita.getDireccion() == null) {
					sendResponse(exchange, 400, "{\"error\":\"Falta la dirección\"}");
					return;
				}
				if (cita.getLocalidad() == null) {
					sendResponse(exchange, 400, "{\"error\":\"Falta la dirección\"}");
					return;
				}
				if (cita.getProvincia() == null) {
					sendResponse(exchange, 400, "{\"error\":\"Falta la dirección\"}");
					return;
				}
				if (cita.getCp() == null) {
					sendResponse(exchange, 400, "{\"error\":\"Falta lel código postal\"}");
					return;
				}
				// Ponemos la cita en 'PEDNEINTE'
				cita.setEstado("PENDIENTE");

				insertaCita(cita);
				sendResponse(exchange, 201, "{\"Resultado\": \"Éxito\"}");

			} catch (IOException e) {
				sendResponse(exchange, 500, "{\"Error\":\"Error insertando cita\"}");
				e.printStackTrace();
			} finally {
				logApi(ex, urlCompleta);
			}
		}

		private boolean insertaCita(CitaForJson cita) {
			// Para escribir en el log

			try (Connection conn = ds.getConnection();
					PreparedStatement ps = conn.prepareStatement(
							"INSERT INTO citas (id_cliente, id_profesional, fecha, hora, direccion, cp, localidad, provincia, estado)"
									+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS)) {

				ps.setString(1, cita.getCliente());
				ps.setString(2, cita.getProfesional());
				ps.setString(3, cita.getFecha());
				ps.setString(4, cita.getHora());
				ps.setString(5, cita.getDireccion());
				ps.setString(6, cita.getCp());
				ps.setString(7, cita.getLocalidad());
				ps.setString(8, cita.getProvincia());
				ps.setString(9, cita.getEstado());

				ps.executeUpdate();
				return true;

			} catch (SQLException e) {
				System.out.println(e.getMessage());
				return false;
			}
		}

		private void handleGetProfesional(HttpExchange exchange, String dni) throws IOException {
			// Para escribir en el log
			String urlCompleta = exchange.getRequestURI().toString();
			Exception ex = null;
			try {
				sendResponse(exchange, 200, "Buscando citas del Profesional: " + dni);
			} finally {
				logApi(ex, urlCompleta);
			}
		}

		private void handleGetFecha(HttpExchange exchange, String fecha) throws IOException {
			// Para escribir en el log
			String urlCompleta = exchange.getRequestURI().toString();
			Exception ex = null;
			try {

				sendResponse(exchange, 200, "Buscando citas para la fecha: " + fecha);
			} finally {
				logApi(ex, urlCompleta);
			}
		}
	}

	/**
	 * Clase auxiliar para representar datos JSON
	 */
	static class CitaForJson {
		String id;
		String fecha;
		String hora;
		String cp;
		String direccion;
		String localidad;
		String provincia;
		String cliente;
		String profesional;
		String estado;

		protected String getId() {
			return id;
		}

		protected void setId(String id) {
			this.id = id;
		}

		protected String getFecha() {
			return fecha;
		}

		protected void setFecha(String fecha) {
			this.fecha = fecha;
		}

		protected String getHora() {
			return hora;
		}

		protected void setHora(String hora) {
			this.hora = hora;
		}

		protected String getCp() {
			return cp;
		}

		protected void setCp(String cp) {
			this.cp = cp;
		}

		protected String getDireccion() {
			return direccion;
		}

		protected void setDireccion(String direccion) {
			this.direccion = direccion;
		}

		protected String getLocalidad() {
			return localidad;
		}

		protected void setLocalidad(String localidad) {
			this.localidad = localidad;
		}

		protected String getProvincia() {
			return provincia;
		}

		protected void setProvincia(String provincia) {
			this.provincia = provincia;
		}

		protected String getCliente() {
			return cliente;
		}

		protected void setCliente(String cliente) {
			this.cliente = cliente;
		}

		protected String getProfesional() {
			return profesional;
		}

		protected void setProfesional(String profesional) {
			this.profesional = profesional;
		}

		protected String getEstado() {
			return estado;
		}

		protected void setEstado(String estado) {
			this.estado = estado;
		}
	}

	/**
	 * Clase auxiliar para representar datos JSON
	 */
	static class UsrForJson {
		String dni;
		String nombre;
		String apellidos;
		String contrasena;
		String email;
		String direccion;
		String cod_postal;
		String telefono;

		protected String getDni() {
			return dni;
		}

		protected void setDni(String dni) {
			this.dni = dni;
		}

		protected String getNombre() {
			return nombre;
		}

		protected void setNombre(String nombre) {
			this.nombre = nombre;
		}

		protected String getApellidos() {
			return apellidos;
		}

		protected void setApellidos(String apellidos) {
			this.apellidos = apellidos;
		}

		protected String getContrasena() {
			return contrasena;
		}

		protected void setContrasena(String contrasena) {
			this.contrasena = contrasena;
		}

		protected String getEmail() {
			return email;
		}

		protected void setEmail(String email) {
			this.email = email;
		}

		protected String getDireccion() {
			return direccion;
		}

		protected void setDireccion(String direccion) {
			this.direccion = direccion;
		}

		protected String getCod_postal() {
			return cod_postal;
		}

		protected void setCod_postal(String cod_postal) {
			this.cod_postal = cod_postal;
		}

		protected String getTelefono() {
			return telefono;
		}

		protected void setTelefono(String telefono) {
			this.telefono = telefono;
		}

	}

}
