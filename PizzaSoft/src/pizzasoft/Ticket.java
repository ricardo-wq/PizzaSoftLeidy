package gestionpizzeria;

import Conexion.GestionPizzasConexion;

import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.Copies;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.math.BigDecimal;

class Config {

    String ruc;
    String nombre;
    String telefono;
    String direccion;
    String mensaje;
}

public class Ticket {

    // Conexión estática y privada, inicializada una sola vez
    private static Connection conexion;

    static {
        GestionPizzasConexion con = new GestionPizzasConexion(); // Aquí podrías lanzar una excepción runtime o manejar el error según convenga
        conexion = con.Conectar();
    }

    private static Config cargarConfig() throws SQLException {
        String sql = "SELECT nombre, telefono, direccion, mensaje FROM config WHERE id = 1";
        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Config cfg = new Config();
                cfg.nombre = rs.getString("nombre");
                cfg.telefono = rs.getString("telefono");
                cfg.direccion = rs.getString("direccion");
                cfg.mensaje = rs.getString("mensaje");
                return cfg;
            } else {
                throw new SQLException("No existe configuración de empresa (config.id=1).");
            }
        }
    }

    public static String generarTicketPedido(int idPedido) throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            throw new SQLException("Conexión a base de datos no disponible");
        }

        Config cfg = cargarConfig();
        StringBuilder sb = new StringBuilder();

        String sql = """
            SELECT
                c.nombreCliente,
                c.telefono,
                c.direccion,
                p.fecha AS fecha_pedido,
                GROUP_CONCAT(pizzas_numeradas.pizza_detalle SEPARATOR '\n') AS detalles_pizzas,
                GROUP_CONCAT(DISTINCT 
                    CASE 
                        WHEN v.complementos <> 'n/a' THEN v.complementos 
                        ELSE NULL 
                    END 
                    SEPARATOR ', '
                ) AS complementos,
                SUM(v.total_venta) AS total_ventas
            FROM pedido p
            JOIN cliente c ON c.nombreCliente = p.cliente
            JOIN ventas v ON p.id_pedido = v.id_pedido
            JOIN (
                SELECT 
                    id_venta,
                    id_pedido,
                    CONCAT('N.', pizza_num, ': ', nombre_pizza, ', Tam: ', tamanio_pizza, 
                           ',\nIngs: ', IFNULL(ingredientes, 'Ninguno'),
                           ',\nExtras: ', IFNULL(ingredientesExtras, 'Ninguno'),
                           ',\nCant: ', cantidad_pizzas) AS pizza_detalle
                FROM (
                    SELECT 
                        v.*,
                        @rownum := IF(@current_pedido = v.id_pedido, @rownum + 1, 1) AS pizza_num,
                        @current_pedido := v.id_pedido
                    FROM ventas v
                    CROSS JOIN (SELECT @rownum := 0, @current_pedido := 0) vars
                    WHERE v.id_pedido = ?
                    ORDER BY v.id_venta
                ) t
            ) pizzas_numeradas ON pizzas_numeradas.id_pedido = p.id_pedido AND pizzas_numeradas.id_venta = v.id_venta
            WHERE p.id_pedido = ?
            GROUP BY p.id_pedido, c.nombreCliente, c.telefono, c.direccion, p.fecha
            """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            ps.setInt(2, idPedido);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // --- Encabezado empresa ---
                    sb.append(String.format("   *** %s ***\n", cfg.nombre));
                    sb.append(String.format("Tel: %s\n", cfg.telefono));
                    sb.append(cfg.direccion).append("\n");
                    sb.append("--------------------------------\n");

                    // --- Datos del cliente ---
                    sb.append("Cliente: ").append(rs.getString("nombreCliente")).append("\n");
                    sb.append("Tel: ").append(rs.getString("telefono")).append("\n");
                    sb.append("Dir: ").append(rs.getString("direccion")).append("\n");
                    sb.append("Fecha: ").append(rs.getTimestamp("fecha_pedido")).append("\n");
                    sb.append("--------------------------------\n");

                    // --- Detalle del pedido con saltos de línea ---
                    sb.append("Detalle del pedido:\n");
                    String detallePizzas = rs.getString("detalles_pizzas");
                    if (detallePizzas != null && !detallePizzas.isEmpty()) {
                        sb.append(detallePizzas).append("\n");
                    } else {
                        sb.append("No hay pizzas registradas.\n");
                    }
                    sb.append("--------------------------------\n");

                    // --- Complementos ---
                    sb.append("Complementos: ");
                    String complementos = rs.getString("complementos");
                    sb.append(complementos != null && !complementos.isEmpty() ? complementos : "Ninguno").append("\n");
                    sb.append("--------------------------------\n");

                    // --- Total ---
                    BigDecimal total = rs.getBigDecimal("total_ventas");
                    sb.append(String.format("TOTAL: $ %,10.2f\n", total));
                    sb.append("--------------------------------\n");

                    // --- Mensaje final ---
                    sb.append("Gracias por su preferencia!\n");
                    sb.append(cfg.mensaje).append("\n\n\n");
                } else {
                    throw new SQLException("No se encontró el pedido con id: " + idPedido);
                }
            }
        }
        // Mostrar en consola
        System.out.println(sb.toString());
        return sb.toString();
    }

    public static void imprimirTicket(String textoTicket) {
        try (InputStream is = new ByteArrayInputStream(textoTicket.getBytes("UTF8"))) {
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            PrintService service = PrintServiceLookup.lookupDefaultPrintService();

            if (service == null) {
                System.err.println("No se encontró impresora predeterminada.");
                return;
            }

            Doc doc = new SimpleDoc(is, flavor, null);
            DocPrintJob job = service.createPrintJob();
            PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
            attr.add(new Copies(1));

            job.print(doc, attr);
            System.out.println("Ticket enviado a la impresora.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
