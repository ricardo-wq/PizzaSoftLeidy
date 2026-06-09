/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vista;

import Conexion.GestionPizzasConexion;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ascen
 */
public class reportes extends javax.swing.JFrame {

    /**
     * Creates new form reportes
     */
    public reportes() {
        initComponents();
        ponerFechaHoy();
        cargarVentasPorFecha();
        this.setLocationRelativeTo(null);
        ImageIcon iconoApp = new ImageIcon(getClass().getResource("/img/footer.png"));
        setIconImage(iconoApp.getImage());
    }

    public void ponerFechaHoy() {
        if (jDateChooser_fecha != null) {
            jDateChooser_fecha.setDate(new java.util.Date());

        } else {
            System.out.println("Error: jDateChooser_fecha no está inicializado.");
        }
    }

    public void cargarVentasPorFecha() {
        // Validar si se ha seleccionado una fecha
        if (jDateChooser_fecha.getDate() == null) {
            JOptionPane.showMessageDialog(null, "Por favor seleccione una fecha.", "Fecha no seleccionada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Formatear la fecha al formato adecuado para SQL
        java.util.Date fechaSeleccionada = jDateChooser_fecha.getDate();
        java.sql.Date fechaSQL = new java.sql.Date(fechaSeleccionada.getTime());

        // Definir el modelo de la tabla
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.setColumnIdentifiers(new Object[]{
            "Nombre Cliente", "Fecha Venta", "Hora Pedido", "Nombre Pizza",
            "Tamaño Pizza", "Ingredientes", "Cantidad", "Precio Pizza",
            "Complementos", "Precio Complemento", "Total Venta"
        });

        Connection conexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // Conexión a la base de datos
            GestionPizzasConexion con = new GestionPizzasConexion();
            conexion = con.Conectar();

            String sql = "SELECT nombre_cliente, fecha_venta, hora_pedido, nombre_pizza, tamanio_pizza, "
                    + "ingredientes, cantidad_pizzas, precio_pizza, complementos, precio_complemento, total_venta "
                    + "FROM ventas WHERE fecha_venta = ? and estado = 'Entregado'";

            ps = conexion.prepareStatement(sql);
            ps.setDate(1, fechaSQL);
            rs = ps.executeQuery();

            // Cargar datos en el modelo
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getString("nombre_cliente"),
                    rs.getDate("fecha_venta").toString(),
                    rs.getTime("hora_pedido").toString(),
                    rs.getString("nombre_pizza"),
                    rs.getString("tamanio_pizza"),
                    rs.getString("ingredientes"),
                    rs.getInt("cantidad_pizzas"),
                    rs.getBigDecimal("precio_pizza").toString(),
                    rs.getString("complementos"),
                    rs.getBigDecimal("precio_complemento").toString(),
                    rs.getBigDecimal("total_venta").toString()
                });
            }

            jTable_ventas.setModel(modelo);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar datos de ventas: " + e.getMessage(), "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Cerrar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (conexion != null) {
                    conexion.close();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error al cerrar la conexión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jDateChooser_fecha = new com.toedter.calendar.JDateChooser();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable_ventas = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Reporte de ventas");
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(229, 242, 211));
        jPanel1.setFocusable(false);
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jDateChooser_fecha.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jPanel1.add(jDateChooser_fecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(1130, 70, 170, 30));

        jButton1.setBackground(new java.awt.Color(204, 204, 255));
        jButton1.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/actualizar.png"))); // NOI18N
        jButton1.setText("Actualizar tabla");
        jButton1.setBorderPainted(false);
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 110, -1, 30));

        jTable_ventas.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jTable_ventas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable_ventas);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, 1070, 490));

        jButton2.setBackground(new java.awt.Color(255, 102, 102));
        jButton2.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(0, 0, 0));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/archivo-pdf.png"))); // NOI18N
        jButton2.setText("Generar reporte");
        jButton2.setBorderPainted(false);
        jButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1120, 510, -1, 40));

        jLabel1.setFont(new java.awt.Font("Poppins", 1, 24)); // NOI18N
        jLabel1.setText("Reportes de ventas");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, -1, -1));

        jLabel2.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        jLabel2.setText("Seleccionar fecha:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1130, 50, -1, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1320, 590));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        cargarVentasPorFecha();


    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        generarReporteDesdeTabla();        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    public void generarReporteDesdeTabla() {
        // Ruta de la carpeta de reportes en el Escritorio
        String escritorio = System.getProperty("user.home") + File.separator + "Desktop";
        File carpeta = new File(escritorio + File.separator + "reportdes");
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        // Nombre del archivo con la fecha actual
        LocalDate hoy = LocalDate.now();
        String fechaStr = hoy.format(DateTimeFormatter.ISO_DATE);
        String nombreArchivo = "reporte_" + fechaStr + ".pdf";
        String rutaPDF = carpeta.getAbsolutePath() + File.separator + nombreArchivo;

        double totalIngresos = 0.0;

        try {
            // Configurar el documento PDF
            Document documento = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(documento, new FileOutputStream(rutaPDF));
            documento.open();

            // Fuentes
            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(149, 201, 74));
            Font encabezadoFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

            // Título
            Paragraph titulo = new Paragraph("Reporte de Ventas - Pizzería Carpizzio\n", tituloFont);
            titulo.setAlignment(Paragraph.ALIGN_CENTER);
            documento.add(titulo);

            // Fecha del reporte
            Paragraph fechaPar = new Paragraph("Fecha: " + fechaStr + "\n\n", cellFont);
            fechaPar.setAlignment(Paragraph.ALIGN_RIGHT);
            documento.add(fechaPar);

            // Crear tabla con 11 columnas
            PdfPTable tabla = new PdfPTable(11);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{3f, 4f, 3f, 4f, 3f, 5f, 2f, 2f, 5f, 3f, 4f});

            // Encabezados
            String[] headers = {
                "Cliente", "Fecha", "Hora Pedido", "Pizza", "Tamaño",
                "Ingredientes", "Cant.", "Precio Pizza", "Complementos", "Precio Compl.",
                "Total Venta"
            };
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, encabezadoFont));
                cell.setBackgroundColor(new BaseColor(149, 201, 74));

                cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                tabla.addCell(cell);
            }

            // Rellenar filas desde jTable
            DefaultTableModel model = (DefaultTableModel) jTable_ventas.getModel();
            int rowCount = model.getRowCount();

            for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    Object value = model.getValueAt(i, j);
                    tabla.addCell(new Phrase(value != null ? value.toString() : "", cellFont));

                    // Acumular el total desde la columna "Total Venta" (índice 10)
                    if (j == 10 && value != null) {
                        try {
                            totalIngresos += Double.parseDouble(value.toString());
                        } catch (NumberFormatException ex) {
                            // Ignorar errores de formato numérico
                        }
                    }
                }
            }

            documento.add(tabla);

            // Total de ingresos
            Paragraph totalPar = new Paragraph(
                    "\nTotal de Ingresos: $" + String.format("%.2f", totalIngresos) + "\n", tituloFont);
            totalPar.setAlignment(Paragraph.ALIGN_RIGHT);
            documento.add(totalPar);

            documento.close();

            // Abrir automáticamente el PDF generado
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(rutaPDF));
            }

            JOptionPane.showMessageDialog(null,
                    "Reporte generado correctamente:\n" + rutaPDF,
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

        } catch (DocumentException | IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error al generar el reporte:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private com.toedter.calendar.JDateChooser jDateChooser_fecha;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable_ventas;
    // End of variables declaration//GEN-END:variables
}
