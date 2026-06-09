package pizzasoft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JOptionPane;

public class RespaldoSQL {

    public void crearBackup() throws IOException {
        // Ruta al ejecutable mysqldump
        String dumpPath = "C:\\PizzaSoft\\mysql-8.4.5-winx64\\bin\\mysqldump.exe";
        File dumpExe = new File(dumpPath);
        if (!dumpExe.exists() || !dumpExe.isFile()) {
            String msg = "No se encontró mysqldump en: " + dumpPath;
            System.err.println(msg);
            JOptionPane.showMessageDialog(null, msg, "Error ruta mysqldump", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] comando = {
            dumpPath,
            "-u", "root",
            "-p1234567",
            "pizzeria"
        };

        Process proceso = null;
        FileOutputStream archivo = null;
        try {
            proceso = new ProcessBuilder(comando)
                    .redirectErrorStream(false)
                    .start();

            // Ruta absoluta al escritorio con carpeta "backups"
            String rutaCarpeta = System.getProperty("user.home") + "/Desktop/backups";
            File carpeta = new File(rutaCarpeta);

// Crear la carpeta si no existe
            if (!carpeta.exists()) {
                if (carpeta.mkdirs()) {
                    System.out.println("Carpeta 'backups' creada exitosamente en el escritorio.");
                } else {
                    System.out.println("No se pudo crear la carpeta 'backups' en el escritorio.");
                }
            }

// Guardar el archivo dentro de la carpeta
            String rutaArchivo = rutaCarpeta + "/Respaldo_automatico.sql";
            archivo = new FileOutputStream(rutaArchivo);

// Volcado del proceso al archivo
            InputStream entrada = proceso.getInputStream();
            byte[] buffer = new byte[8192];
            int bytesLeidos;
            while ((bytesLeidos = entrada.read(buffer)) > 0) {
                archivo.write(buffer, 0, bytesLeidos);
            }
            archivo.flush();

            // Espera a que termine
            int exitCode = proceso.waitFor();

            // Captura los errores
            InputStream errores = proceso.getErrorStream();
            byte[] errorBuffer = errores.readAllBytes();
            String err = new String(errorBuffer);

            // Filtrar la advertencia de contraseña
            String warningPassword = "Using a password on the command line interface can be insecure";
            if (err.isEmpty()
                    || (err.contains("[Warning]") && err.contains(warningPassword) && err.trim().endsWith(warningPassword))) {
                // Sólo vino la advertencia de contraseña: la ignoramos
                String msgExito = "Información guardada correctamente (ignorada advertencia inseguridad contraseña).";
                System.out.println(msgExito);
                JOptionPane.showMessageDialog(null, msgExito, "Backup exitoso", JOptionPane.INFORMATION_MESSAGE);
            } else if (exitCode != 0) {
                // Hubo otro tipo de fallo
                String msg = "mysqldump terminó con código de salida: " + exitCode
                        + "\nErrores:\n" + err;
                System.err.println(msg);
                JOptionPane.showMessageDialog(null, msg, "Error mysqldump", JOptionPane.ERROR_MESSAGE);
            } else {
                // Salió bien y sin errores
                String msg = "Datos guardados";
                System.out.println(msg);
                JOptionPane.showMessageDialog(null, msg, "Backup exitoso", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            String msg = "El proceso de backup fue interrumpido.";
            System.err.println(msg);
            JOptionPane.showMessageDialog(null, msg, "Backup interrumpido", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (archivo != null) {
                try {
                    archivo.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

}
