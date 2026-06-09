package Conexion;

import java.sql.*;
import javax.swing.JOptionPane;

/**
 *
 * @author © Angel Ascencio
 */
public class GestionPizzasConexion {

    Connection conectar;
    String usuario = "root";
    String password = "";
    String bd = "pizzeria";
    String ip = "LocalHost";
    String puerto = "3306";

    
    //String url = "jdbc:mysql://" + ip + ":" + puerto + "/" + bd + "?useSSL=false";
    String url = "jdbc:mysql://" + ip + ":" + puerto + "/" + bd + "?serverTimezone=America/Mexico_City"; // Zona horaria especificada"

    public Connection Conectar() {
        try {
            //Ruta del driver:
            Class.forName("com.mysql.cj.jdbc.Driver");
            conectar = DriverManager.getConnection(url, usuario, password);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Error al conectar:" + e);
        }
        return conectar;
    }

    public void CerrarConexion() {
        try {
            //Si conectar es diferente a vacio (conexion establecida), entonces cerrarla:
            if (conectar != null) {
                conectar.close();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cerrar la conexión: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
