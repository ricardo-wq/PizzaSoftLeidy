package pizzasoft;

import javax.swing.JOptionPane;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;
import vista.Login;
public class InstaladorMySQL {

    // === Variables y Constantes Clave ===
    private static final String MYSQL_BIN_PATH = "C:\\PizzaSoft\\mysql-8.4.5-winx64\\bin";
    private static final String MYSQL_SERVICE_NAME = "MySQL10";
    private static final String NEW_PASSWORD = "";
    private static final String NOMBRE_BD = "pizzeria";
    private static final String SCRIPT_SQL = MYSQL_BIN_PATH + "\\RESPALDO.sql";
    private static final String CONFIG_FILE = "config.properties";
    private static final String CONFIG_KEY_BASE_CREADA = "base_creada";
    
    private static Properties configProps = new Properties();
    
    public static void iniciarInstalador() {
        Login log = new Login(); // Vista creada
        
        log.verificarYCrearPrimerUsuario();// El controlador usa la vista internamente

        log.Actualizarbarra(0, "=== Inicio del instalador MySQL ===");
        try {

            // 1) Validaciones Iniciales Esenciales
            log.Actualizarbarra(5, "1 Validando entorno...");
            validarEntorno();
            log.Actualizarbarra(10, "Entorno validado correctamente.");

            // 2) Comprobación de Conexión Existente y Estado de Instalación
            log.Actualizarbarra(15, "2 Verificando conexión previa y estado de instalación...");
            if (conexionYEstadoPrevio()) {
                
                log.Actualizarbarra(100, "La base de datos ya está configurada y accesible");
                
                log.verificarYCrearPrimerUsuario();
                return;
            }
            
            log.Actualizarbarra(25, "No existe configuración previa o base inaccesible.");

            // 3) Proceso de Instalación y Configuración
            int resp = JOptionPane.showConfirmDialog(null,
                    "Se detectó que la base no está preparada.\n¿Deseas iniciar el proceso automático de instalación/configuración?",
                    "Instalación MySQL", JOptionPane.YES_NO_OPTION);
            if (resp != JOptionPane.YES_OPTION) {
                log.Actualizarbarra(30, "Instalación cancelada por el usuario.");
                JOptionPane.showMessageDialog(null, "Proceso cancelado por el usuario.",
                        "Cancelado", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                log.Actualizarbarra(35, "3.1 Gestionando servicio MySQL...");
                gestionarServicioMySQL();
                log.Actualizarbarra(40, "Servicio gestionado.");
                
                log.Actualizarbarra(45, "3.2 Inicializando MySQL...");
                inicializarMySQL();
                log.Actualizarbarra(50, "MySQL inicializado. TempPass guardada.");
                
                log.Actualizarbarra(55, "3.3 Instalando servicio...");
                instalarServicio();
                log.Actualizarbarra(60, "Servicio instalado.");
                
                log.Actualizarbarra(65, "3.4 Iniciando servicio...");
                iniciarServicio();
                log.Actualizarbarra(70, "Servicio iniciado.");
                
                log.Actualizarbarra(75, "3.5 Cambiando password root...");
                cambiarPasswordRoot();
                log.Actualizarbarra(80, "Password de root cambiada.");
                
                log.Actualizarbarra(85, "3.6  Creando base de datos '" + NOMBRE_BD + "'...");
                crearBaseDatos();
                log.Actualizarbarra(90, "Base de datos creada o ya existente.");
                
                log.Actualizarbarra(95, "3.7 Ejecutando script SQL...");
                ejecutarScriptSQL();
                log.Actualizarbarra(98, "Script SQL ejecutado.");
                
                log.Actualizarbarra(99, "3.8 Actualizando configuración a éxito...");
                actualizarConfig(true);
                
                JOptionPane.showMessageDialog(null, "Instalación y configuración completadas exitosamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                log.Actualizarbarra(100, "=== Instalación completada con éxito ===");
                log.verificarYCrearPrimerUsuario();
                
            } catch (Exception e) {
                log.Actualizarbarra(100, "!!! Error durante la configuración: " + e.getMessage());
                actualizarConfig(false);
                JOptionPane.showMessageDialog(null,
                        "Error durante la configuración:\n" + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            log.Actualizarbarra(100, "!!! Error crítico de validación: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Error crítico de validación:\n" + e.getMessage(),
                    "Error Crítico", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // === Paso 1 ===
    private static void validarEntorno() throws IOException, ClassNotFoundException {
        System.out.println("   - Comprobando existencia de carpeta bin de MySQL: " + MYSQL_BIN_PATH);
        if (!Files.isDirectory(Paths.get(MYSQL_BIN_PATH))) {
            throw new IOException("MYSQL_BIN_PATH no existe o no es accesible: " + MYSQL_BIN_PATH);
        }
        System.out.println("   - Comprobando accesibilidad del script SQL: " + SCRIPT_SQL);
        if (!Files.isReadable(Paths.get(SCRIPT_SQL))) {
            throw new IOException("SCRIPT_SQL no existe o no es legible: " + SCRIPT_SQL);
        }
        System.out.println("   - Verificando driver JDBC...");
        Class.forName("com.mysql.cj.jdbc.Driver");
    }

    // === Paso 2 ===
    private static boolean conexionYEstadoPrevio() {
        // 2.1 Intentar conectar a NOMBRE_BD
        System.out.println("   - Intentando conexión JDBC a la base '" + NOMBRE_BD + "'...");
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/" + NOMBRE_BD + "?useSSL=false", "root", NEW_PASSWORD)) {
            try (Statement st = conn.createStatement()) {
                st.executeQuery("SELECT 1");
            }
            System.out.println("   - Conexión exitosa.");
            return true;
        } catch (SQLException e) {
            System.out.println("   - Conexión fallida o base inexistente: " + e.getMessage());
        }
        // 2.2 Leer config.properties
        System.out.println("   - Leyendo archivo de configuración: " + CONFIG_FILE);
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            configProps.load(fis);
            boolean estado = Boolean.parseBoolean(configProps.getProperty(CONFIG_KEY_BASE_CREADA, "false"));
            System.out.println("   - Estado previo según config: " + estado);
            return estado;
        } catch (IOException e) {
            System.out.println("   - No existe config.properties o no legible, se asume false.");
        }
        return false;
    }

// === Paso Proceso: 3.1 / 3.2 ===
    private static void gestionarServicioMySQL() throws IOException, InterruptedException {
        System.out.println("   - Comprobando si el servicio existe...");
        if (!servicioExiste()) {
            System.out.println("     > Servicio no existe. Se omite gestión previa.");
            return;
        }
        
        System.out.println("     > Servicio '" + MYSQL_SERVICE_NAME + "' detectado.");

        // 1) Asegurarse de que esté en marcha
        try {
            if (!servicioActivo()) {
                System.out.println("       > Servicio detenido. Iniciando servicio...");
                iniciarServicio();
            } else {
                System.out.println("       > Servicio ya está en ejecución.");
            }
        } catch (Exception e) {
            System.out.println("       !!! No se pudo iniciar el servicio: " + e.getMessage());
            System.out.println("       > Eliminando servicio y carpeta data para reinstalar...");

            // elimina servicio
            ejecutarComando("net", "stop", MYSQL_SERVICE_NAME);
            ejecutarComando(MYSQL_BIN_PATH + "\\mysqld", "--remove", MYSQL_SERVICE_NAME);

            // limpia data
            Path dataDir = Paths.get(MYSQL_BIN_PATH).getParent().resolve("data");
            if (Files.exists(dataDir)) {
                deleteRecursively(dataDir.toFile());
                System.out.println("       > Carpeta data eliminada.");
            }

            // reinstala y arranca
            System.out.println("       > Reinstalando servicio MySQL...");
            ejecutarComando(MYSQL_BIN_PATH + "\\mysqld", "--install", MYSQL_SERVICE_NAME);
            iniciarServicio();
            System.out.println("       > Servicio reinstalado y arrancado.");
        }

        // 2) Verificar conexión JDBC
        System.out.println("     > Intentando conexión JDBC para validar servicio...");
        if (intentarConexionRoot()) {
            System.out.println("       > Conexión exitosa. Servicio OK.");
            return;
        }

        // 3) Si la conexión falla, eliminar y limpiar para instalación limpia
        System.out.println("       > Conexión fallida. Limpiando servicio y carpeta data...");
        ejecutarComando("net", "stop", MYSQL_SERVICE_NAME);
        ejecutarComando(MYSQL_BIN_PATH + "\\mysqld", "--remove", MYSQL_SERVICE_NAME);
        
        Path dataDir = Paths.get(MYSQL_BIN_PATH).getParent().resolve("data");
        if (Files.exists(dataDir)) {
            deleteRecursively(dataDir.toFile());
            System.out.println("       > Carpeta data eliminada.");
        }
        System.out.println("     > Servicio y datos eliminados para nueva instalación.");
    }

// Comprueba si el servicio está corriendo
    private static boolean servicioActivo() throws IOException, InterruptedException {
        Process p = new ProcessBuilder("sc", "query", MYSQL_SERVICE_NAME)
                .redirectErrorStream(true).start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().startsWith("STATE") && line.contains("RUNNING")) {
                p.waitFor();
                return true;
            }
        }
        p.waitFor();
        return false;
    }

// Intenta conectar como root: primero con NEW_PASSWORD, luego (si existe)
// con tempPass almacenada en configProps
    private static boolean intentarConexionRoot() {
        String url = "jdbc:mysql://localhost:3306/" + NOMBRE_BD + "?useSSL=false";
        for (String pass : new String[]{NEW_PASSWORD, configProps.getProperty("tempPass")}) {
            if (pass == null) {
                continue;
            }
            try (Connection conn = DriverManager.getConnection(url, "root", pass); Statement st = conn.createStatement()) {
                st.executeQuery("SELECT 1");
                System.out.println("       > Conectado usando contraseña: " + (pass.equals(NEW_PASSWORD) ? "NEW_PASSWORD" : "tempPass"));
                return true;
            } catch (SQLException e) {
                System.out.println("       > Intento con " + (pass.equals(NEW_PASSWORD) ? "NEW_PASSWORD" : "tempPass")
                        + " falló: " + e.getMessage());
            }
        }
        return false;
    }
    
    private static boolean servicioExiste() throws IOException, InterruptedException {
        Process p = new ProcessBuilder("sc", "query", MYSQL_SERVICE_NAME)
                .redirectErrorStream(true).start();
        int code = p.waitFor();
        return code == 0;
    }
    
    private static void deleteRecursively(File file) throws IOException {
        if (file.isDirectory()) {
            for (File c : file.listFiles()) {
                deleteRecursively(c);
            }
        }
        if (!file.delete()) {
            throw new IOException("No se pudo eliminar: " + file.getAbsolutePath());
        }
    }

    // === Paso 3.3 ===
    private static void inicializarMySQL() throws IOException, InterruptedException {
        System.out.println("   - Ejecutando 'mysqld --initialize'...");
        ProcessBuilder pb = new ProcessBuilder(
                MYSQL_BIN_PATH + "\\mysqld", "--initialize", "--console");
        pb.directory(new File(MYSQL_BIN_PATH));
        pb.redirectErrorStream(true);
        Process p = pb.start();

        // capturar contraseña temporal
        String tempPass = null;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                System.out.println("       [mysqld] " + line);
                if (line.contains("temporary password")) {
                    tempPass = line.substring(line.lastIndexOf(":") + 2).trim();
                }
            }
        }
        int code = p.waitFor();
        if (code != 0 || tempPass == null) {
            throw new IOException("Error al inicializar MySQL. Código=" + code);
        }
        System.out.println("   - Contraseña temporal capturada: " + tempPass);
        configProps.setProperty("tempPass", tempPass);
    }

    // === Paso 3.4 ===
    private static void instalarServicio() throws IOException, InterruptedException {
        System.out.println("   - Instalando el servicio MySQL...");
        ejecutarComando(MYSQL_BIN_PATH + "\\mysqld", "--install", MYSQL_SERVICE_NAME);
    }

    // === Paso 3.5 ===
    private static void iniciarServicio() throws IOException, InterruptedException {
        System.out.println("   - Iniciando el servicio MySQL...");
        ejecutarComando("net", "start", MYSQL_SERVICE_NAME);
        // Espera para arranque completo
        Thread.sleep(8000);
    }

    // === Paso 3.6 ===
    // === Paso 3.6 ===
    private static void cambiarPasswordRoot() throws IOException, InterruptedException {
        System.out.println("   - Preparando cambio de contraseña de root...");

        // Obtenemos la contraseña temporal sin escapes
        String tempPass = configProps.getProperty("tempPass");

        // Generamos un .bat que use mysql.exe y ALTER USER
        String batContent = String.format(
                "@echo off%n"
                + "echo Cambiando password de root...%n"
                + "\"%s\\mysql.exe\" -u root --connect-expired-password -p\"%s\" -e \"%s\"%n"
                + "echo Fin de cambio de password.%n",
                MYSQL_BIN_PATH,
                tempPass,
                // El comando SQL que cambia la contraseña
                "ALTER USER 'root'@'localhost' IDENTIFIED BY '" + NEW_PASSWORD + "';"
        );
        
        Path batFile = Paths.get("change_pass.bat").toAbsolutePath();
        System.out.println("       > Generando archivo: " + batFile);
        Files.write(batFile, batContent.getBytes());
        
        System.out.println("       > Ejecutando batch de cambio de password...");
        ejecutarComando("cmd.exe", "/c", batFile.toString());
        System.out.println("   - Cambio de contraseña finalizado.");
    }

    // === Paso 3.7 ===
    private static void crearBaseDatos() throws SQLException {
        System.out.println("   - Conectando para crear base de datos...");
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/?allowMultiQueries=true", "root", NEW_PASSWORD); Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + NOMBRE_BD + ";");
        }
    }

    // === Paso 3.8 ===
    private static void ejecutarScriptSQL() throws IOException, SQLException {
        System.out.println("   - Leyendo contenido del script SQL: " + SCRIPT_SQL);
        String sql = new String(Files.readAllBytes(Paths.get(SCRIPT_SQL)));
        System.out.println("   - Ejecutando script en la base '" + NOMBRE_BD + "'...");
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/" + NOMBRE_BD + "?allowMultiQueries=true",
                "root", NEW_PASSWORD); Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }

    // === Paso 4 ===
    private static void actualizarConfig(boolean exito) {
        System.out.println("   - Actualizando archivo de configuración con éxito=" + exito);
        configProps.setProperty(CONFIG_KEY_BASE_CREADA, Boolean.toString(exito));
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            configProps.store(fos, "Estado de instalación");
        } catch (IOException e) {
            System.out.println("   !!! No se pudo actualizar " + CONFIG_FILE + ": " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "No se pudo actualizar " + CONFIG_FILE + ": " + e.getMessage(),
                    "Error Config", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Utilitario: ejecuta el comando, captura stdout+stderr y lanza excepción con la salida si falla
    private static void ejecutarComando(String... cmd) throws IOException, InterruptedException {
        System.out.println("       > Ejecutando comando: " + String.join(" ", cmd));
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        
        StringBuilder salida = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("         [cmd] " + line);
                salida.append(line).append(System.lineSeparator());
            }
        }
        int code = p.waitFor();
        if (code != 0) {
            throw new IOException(
                    "Comando falló: " + String.join(" ", cmd)
                    + " (code=" + code + ")\n--- Salida del comando ---\n"
                    + salida.toString()
            );
        }
    }
}
