package vista;

import Conexion.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;  // Importa solo java.util.Date
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.table.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import gestionpizzeria.Ticket;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import pizzasoft.RespaldoSQL;

/**
 *
 * @author ascen
 */
public class Sistema extends javax.swing.JFrame {

    // Variables de conexión y estado
    private Connection conexion;
    private boolean timerActivo = false;
    private javax.swing.Timer timer;
    private String estado = "Activo";

    // Variables para datos de pedidos
    private String tamanio;
    private String pizza;
    private String ingredientes;
    private String complementosSeleccionados = "";
    private String cliente;
    private String tamanoPizza;
    private String ingredientesExtrasSeleccionados;

    // Variables para precios y contadores
    private double precioComplementos = 0.0;
    private double precioTotal;
    private int contador = 0;
    private double precioExtras = 0;

    // Componentes de modelo
    private DefaultTableModel modeloTabla;

    private Set<JCheckBox> bloqueadosSiempre;
    private boolean permitirCambios = true;
    private int maxSeleccion = 0;
    private boolean esMitadyMitad = false;

    private List<PizzaData> ordenesPendientes = new ArrayList<>();
    int idUsuario = 0;

    /**
     * Creates new form Sistema
     */
    public Sistema(int idUsuario) {
        this.idUsuario = idUsuario;
        initComponents();
        this.setVisible(true);
        ImageIcon iconoApp = new ImageIcon(getClass().getResource("/img/footer.png"));
        setIconImage(iconoApp.getImage());
        setLocationRelativeTo(null);

        modeloTabla = new DefaultTableModel();
        jTable_pedidos_del_dia.setModel(modeloTabla);

        GrupoTamaño.add(jRadioButton1_Chica);
        GrupoTamaño.add(jRadioButton2_Grande);
        GrupoTamaño.add(jRadioButton3_Familiar);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
        jSpinner_cantidad.setModel(spinnerModel);

        cargarDatosEnJList();
        cargarRegistrosDelDia();
        DeshabilitarExtras();
        jButton_levantar_pedido.setEnabled(false);

        configurarRendererMultilinea();
        limpiarCamposParaNuevaPizza();
        calcularIngresosDelDia();
        obtenerPizzaMasVendida();
        obtenerCantidadPedidosDelDia();
        calcularTiempoPromedioEntrega();
        mostrarGrafica();

        ponernombre();
        mostrarFechaEnLabel();
        deshabilitarJCheckBoxIngredientes();

        // --- Configuración de lógica de selección de ingredientes ---
        inicializarBloqueadosSiempre();
        configurarListeners();
        //agregarItemListenersBloqueo();

         inicializarCierre();
    }

    // Dentro de tu clase Sistema (extiende JFrame)

// 1) Método nocerrar() que encapsula toda la lógica de cierre
private void nocerrar() {
    // 1.1) Confirmar deseo de cerrar
    int opc = JOptionPane.showConfirmDialog(
            this,
            "¿Seguro/a que deseas Cerrar Sesión?",
            "Confirmación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
    );
    if (opc != JOptionPane.YES_OPTION) {
        return; // el usuario eligió NO, salimos
    }

    // 1.2) Verificar si hay pedidos activos en la tabla
    boolean hayActivos = false;
    javax.swing.table.TableModel modelo = jTable_pedidos_del_dia.getModel();
    for (int i = 0; i < modelo.getRowCount(); i++) {
        Object estado = modelo.getValueAt(i, 4);
        if ("Activo".equalsIgnoreCase(String.valueOf(estado))) {
            hayActivos = true;
            break;
        }
    }

    if (hayActivos) {
        // 1.3) Si hay activos, advertir y cancelar cierre
        JOptionPane.showMessageDialog(
                this,
                "No se puede cerrar el programa porque hay pedidos activos.",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    // 1.4) Si no hay activos, crear respaldo y salir
    try {
        System.out.println("⚙️ No hay activos, preparo respaldo...");
        RespaldoSQL res = new RespaldoSQL();
        res.crearBackup();
    } catch (Exception ex) {
        // Mostrar mensaje de error si falla el respaldo
        JOptionPane.showMessageDialog(
                this,
                "Error al crear el respaldo:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
        ex.printStackTrace();
        return; // Cancelamos el cierre si el respaldo falla
    }

    // 1.5) Finalmente, cerramos la ventana y salimos
    dispose();
    System.exit(0);
}


// 3) Configuración del cierre de la ventana
public void inicializarCierre() {
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
            nocerrar();
        }
    });
}




// 3) Inicialízalo justo después de initComponents():
    private void inicializarBloqueadosSiempre() {
        bloqueadosSiempre = new HashSet<>(Arrays.asList(
                jCheckBox_Champinon,
                jCheckBox_Pina,
                jCheckBox_hawaiana,
                jCheckBox_carnes_frias,
                jCheckBox_campechana,
                jCheckBox_Pastor,
                jCheckBox_Ingredientes_Surtidos,
                jCheckBox_Ingredientes_Especiales
        ));
    }

// 4) Único punto de registro de listeners:
    private void configurarListeners() {
        List<JCheckBox> checks = Arrays.asList(
                jCheckBox_jamon, jCheckBox_Pepperoni, jCheckBox_Salchicha,
                jCheckBox_Salami, jCheckBox_Chorizo, jCheckBox_Champinon,
                jCheckBox_Pina, jCheckBox_hawaiana, jCheckBox_carnes_frias,
                jCheckBox_campechana, jCheckBox_Pastor,
                jCheckBox_Ingredientes_Surtidos, jCheckBox_Ingredientes_Especiales,
                jCheckBox_Pimiento, jCheckBox_Tocino, jCheckBox_pastor_especial
        );
        for (JCheckBox cb : checks) {
            cb.addItemListener(this::anyCheckBoxItemStateChanged);
        }
    }

// 6) El método unificado:
    private void anyCheckBoxItemStateChanged(ItemEvent e) {
        if (!permitirCambios) {
            return;
        }

        JCheckBox fuente = (JCheckBox) e.getItemSelectable();
        int cambio = e.getStateChange();

        // Ingredientes bloqueados
        if (cambio == ItemEvent.DESELECTED && bloqueadosSiempre.contains(fuente)) {
            permitirCambios = false;
            fuente.setSelected(true);
            permitirCambios = true;
            return;
        }

        // Solo contar si se selecciona
        if (cambio == ItemEvent.SELECTED) {
            int seleccionados = contarSeleccionados();

            // Validación límite normal
            if (!esMitadyMitad && maxSeleccion > 0 && seleccionados > maxSeleccion) {
                permitirCambios = false;
                fuente.setSelected(false);
                permitirCambios = true;
                // 🔒 Mostrar mensaje una sola vez
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Sólo puedes elegir hasta " + maxSeleccion + " ingrediente(s).",
                            "Límite alcanzado",
                            JOptionPane.WARNING_MESSAGE);
                });
                return;
            }

            // Validación límite de mitades
            if (esMitadyMitad && seleccionados > 2) {
                permitirCambios = false;
                fuente.setSelected(false);
                permitirCambios = true;
                // 🔒 Mostrar mensaje una sola vez
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Sólo puedes elegir hasta 2 mitades.",
                            "Límite alcanzado",
                            JOptionPane.WARNING_MESSAGE);
                });
                return;
            }
        }

        // Habilitar controles si se cumplen condiciones
        int sel = contarSeleccionados();
        boolean cumpleNormal = (!esMitadyMitad && sel == maxSeleccion);
        boolean cumpleMitad = (esMitadyMitad && sel == 2);
        boolean habilitar = cumpleNormal || cumpleMitad;

        jRadioButton1_Chica.setEnabled(habilitar);
        jRadioButton2_Grande.setEnabled(habilitar);
        jRadioButton3_Familiar.setEnabled(habilitar);
        jSpinner_cantidad.setEnabled(habilitar);

        jCheckBox1_Orilla_de_Queso.setEnabled(habilitar);
        jCheckBox2_Orden_de_Pan_con_Ajo.setEnabled(habilitar);
        jCheckBox3_Ingrediente_Extra.setEnabled(habilitar);
        jCheckBox4_Refresco.setEnabled(habilitar);
    }

    // Clase interna para almacenar temporalmente los datos de cada pizza
    private static class PizzaData {

        String cliente, nombrePizza, tamanoPizza, ingredientes, complementos, ingredientesExtras, hora, estado;
        double precioPizza, precioComplemento, totalVenta;
        int cantidad;

        PizzaData(String cliente, String nombrePizza, String tamanoPizza,
                String ingredientes, String complementos, String ingredientesExtras,
                double precioPizza, double precioComplemento, int cantidad,
                double totalVenta, String hora, String estado) {
            this.cliente = cliente;
            this.nombrePizza = nombrePizza;
            this.tamanoPizza = tamanoPizza;
            this.ingredientes = ingredientes;
            this.complementos = complementos;
            this.ingredientesExtras = ingredientesExtras;
            this.precioPizza = precioPizza;
            this.precioComplemento = precioComplemento;
            this.cantidad = cantidad;
            this.totalVenta = totalVenta;
            this.hora = hora;
            this.estado = estado;
        }
    }

    // Resto de métodos de la clase...
    void ponernombre() {

        int idObtenido = idUsuario;

        String consulta = "SELECT usuario, rol FROM Usuarios WHERE id_usuario = ?";

        GestionPizzasConexion conexion = new GestionPizzasConexion();
        try {
            Connection connection = conexion.Conectar();
            PreparedStatement pst = connection.prepareStatement(consulta);
            pst.setInt(1, idObtenido);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String nombreUsuario = rs.getString("usuario");
                    String rol = rs.getString("rol");

                    if (rol != null && rol.equals("Asistente")) {
                        jMenuItem_reporte.setEnabled(false);
                        jMenuItem_precios.setEnabled(false);
                        jMenuItem_usuarios.setEnabled(false);
                    }

                    jLabel4.setText("Hola, " + nombreUsuario);
                } else {
                    jLabel4.setText("Usuario no encontrado");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al realizar la consulta: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para obtener la cantidad de pedidos del día
    public void obtenerCantidadPedidosDelDia() {
        Connection conexion = new GestionPizzasConexion().Conectar(); // Utiliza tu método de conexión
        PreparedStatement ps = null;
        ResultSet rs = null;
        int cantidadPedidos = 0;

        try {
            // Obtener la fecha actual
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fechaActual = sdf.format(new java.util.Date()); // Usa java.util.Date

            // Consulta SQL para contar la cantidad de pedidos del día
            String sql = "SELECT COUNT(*) AS cantidad_pedidos FROM Ventas WHERE fecha_venta = ? AND estado = 'Entregado'";

            ps = conexion.prepareStatement(sql);
            ps.setString(1, fechaActual);
            rs = ps.executeQuery();

            // Obtener el resultado
            if (rs.next()) {
                cantidadPedidos = rs.getInt("cantidad_pedidos");
            }

            // Mostrar el resultado en jLabel13
            jLabel13.setText(String.valueOf(cantidadPedidos));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al obtener la cantidad de pedidos del día: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Cerrar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                // No cierres la conexión aquí, ya que se está utilizando fuera de este método
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para calcular los ingresos del día
    void calcularIngresosDelDia() {
        Connection conexion = new GestionPizzasConexion().Conectar(); // Utiliza tu método de conexión
        PreparedStatement ps = null;
        ResultSet rs = null;
        double ingresos = 0;

        try {
            // Obtener la fecha actual
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fechaActual = sdf.format(new Date()); // Asegúrate de que sea java.util.Date

            // Consulta SQL para sumar los totales de las ventas del día
            String sql = "SELECT SUM(total_venta) AS ingresos FROM Ventas WHERE fecha_venta = ? AND estado = 'Entregado'";

            ps = conexion.prepareStatement(sql);
            ps.setString(1, fechaActual);
            rs = ps.executeQuery();

            // Obtener el resultado
            if (rs.next()) {
                ingresos = rs.getDouble("ingresos");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al calcular los ingresos del día: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Cerrar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                // No cierres la conexión aquí, ya que se está utilizando fuera de este método
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        jLabel5.setText(String.valueOf("$" + ingresos));

    }

    // Método para obtener la pizza más vendida
    public void obtenerPizzaMasVendida() {
        Connection conexion = new GestionPizzasConexion().Conectar(); // Utiliza tu método de conexión
        PreparedStatement ps = null;
        ResultSet rs = null;
        String pizzaMasVendida = "";

        try {
            // Consulta SQL para obtener la pizza más vendida
            String sql = "SELECT nombre_pizza, COUNT(*) AS cantidad_ventas FROM Ventas "
                    + "WHERE estado = 'Entregado' "
                    + "GROUP BY nombre_pizza ORDER BY cantidad_ventas DESC LIMIT 1";

            ps = conexion.prepareStatement(sql);
            rs = ps.executeQuery();

            // Obtener el resultado
            if (rs.next()) {
                pizzaMasVendida = rs.getString("nombre_pizza");
            }

            // Mostrar el resultado en jLabel12
            jLabel12.setText(pizzaMasVendida);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al obtener la pizza más vendida: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Cerrar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                // No cierres la conexión aquí, ya que se está utilizando fuera de este método
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void calcularTiempoPromedioEntrega() {
        String query = "SELECT AVG(tiempo_entrega_minutos) as tiempo_promedio FROM Ventas WHERE estado = 'Entregado'";

        try (Connection conexion = new GestionPizzasConexion().Conectar(); Statement stmt = conexion.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int tiempoPromedio = rs.getInt("tiempo_promedio");
                String tiempoFormato = tiempoPromedio + " minutos";

                jLabel11.setText(tiempoFormato);
            } else {
                jLabel11.setText("No hay datos de tiempo de entrega");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al calcular tiempo promedio de entrega: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void cargarDatosEnJList() {
        Connection conexion = new GestionPizzasConexion().Conectar(); // Usa tu método de conexión

        if (conexion == null) {
            JOptionPane.showMessageDialog(null, "No se pudo establecer la conexión a la base de datos.", "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String query = "SELECT nombre FROM Pizzas";
            PreparedStatement pst = conexion.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            DefaultListModel<String> modeloLista = new DefaultListModel<>();

            // Lista de ingredientes a ignorar
            List<String> ignorar = Arrays.asList("Jamon", "Pepperoni", "Salchicha", "Salami");

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                if (!ignorar.contains(nombre)) {
                    modeloLista.addElement(nombre);
                }
            }

            // Verifica si se cargaron elementos
            if (modeloLista.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No hay datos disponibles para mostrar.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }

            jLista.setModel(modeloLista);

            rs.close();
            pst.close();
            conexion.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        GrupoTamaño = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jDateChooser_fecha2 = new com.toedter.calendar.JDateChooser();
        jPanel2 = new javax.swing.JPanel();
        jButton_levantar_pedido = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jCheckBox_Extra_Chorizo = new javax.swing.JCheckBox();
        jCheckBox_Extra_Tocino = new javax.swing.JCheckBox();
        jCheckBox_Extra_Pina = new javax.swing.JCheckBox();
        jCheckBox_Extra_Champinon = new javax.swing.JCheckBox();
        jCheckBox_Extra_Salami = new javax.swing.JCheckBox();
        jCheckBox_Extra_Salchicha = new javax.swing.JCheckBox();
        jCheckBox_Extra_Pepperoni = new javax.swing.JCheckBox();
        jCheckBox_Extra_jamon = new javax.swing.JCheckBox();
        jCheckBox_extra_pastor = new javax.swing.JCheckBox();
        jCheckBox_extra_queso = new javax.swing.JCheckBox();
        jCheckBox_extra_pimiento = new javax.swing.JCheckBox();
        jPanel11 = new javax.swing.JPanel();
        jCheckBox_Chorizo = new javax.swing.JCheckBox();
        jCheckBox_jamon = new javax.swing.JCheckBox();
        jCheckBox_Pepperoni = new javax.swing.JCheckBox();
        jCheckBox_Salchicha = new javax.swing.JCheckBox();
        jCheckBox_Ingredientes_Surtidos = new javax.swing.JCheckBox();
        jCheckBox_Ingredientes_Especiales = new javax.swing.JCheckBox();
        jCheckBox_Pastor = new javax.swing.JCheckBox();
        jCheckBox_Pimiento = new javax.swing.JCheckBox();
        jCheckBox_Tocino = new javax.swing.JCheckBox();
        jCheckBox_Champinon = new javax.swing.JCheckBox();
        jCheckBox_Salami = new javax.swing.JCheckBox();
        jCheckBox_carnes_frias = new javax.swing.JCheckBox();
        jCheckBox_campechana = new javax.swing.JCheckBox();
        jCheckBox_hawaiana = new javax.swing.JCheckBox();
        jCheckBox_pastor_especial = new javax.swing.JCheckBox();
        jCheckBox_Pina = new javax.swing.JCheckBox();
        jPanel13 = new javax.swing.JPanel();
        jCheckBox2_Orden_de_Pan_con_Ajo = new javax.swing.JCheckBox();
        jCheckBox1_Orilla_de_Queso = new javax.swing.JCheckBox();
        jCheckBox3_Ingrediente_Extra = new javax.swing.JCheckBox();
        jCheckBox4_Refresco = new javax.swing.JCheckBox();
        jSpinner_refrescos = new javax.swing.JSpinner();
        jSpinner_cantidad_ajo = new javax.swing.JSpinner();
        jSpinner_cantidad = new javax.swing.JSpinner();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jLista = new javax.swing.JList<>();
        jTextField_nTELEFONO = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        Jtext_direccion = new javax.swing.JTextArea();
        Jtext_nombre = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jCheckBox_Mitad = new javax.swing.JCheckBox();
        jPanel12 = new javax.swing.JPanel();
        jRadioButton2_Grande = new javax.swing.JRadioButton();
        jRadioButton3_Familiar = new javax.swing.JRadioButton();
        jRadioButton1_Chica = new javax.swing.JRadioButton();
        jDateChooser_fecha1 = new com.toedter.calendar.JDateChooser();
        jPanel6 = new javax.swing.JPanel();
        jButton_MARCAR_ENTREGADO = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable_pedidos_del_dia = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTextPane_Datos = new javax.swing.JTextPane();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTextPane_pedido = new javax.swing.JTextPane();
        jLabel3 = new javax.swing.JLabel();
        jLabel_hora = new javax.swing.JLabel();
        jButton_Cancerlar_Pedido = new javax.swing.JButton();
        jTextField_efectivo = new javax.swing.JTextField();
        jTextField_cambio = new javax.swing.JTextField();
        jTextField_total = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jButton_calcular = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        jDateChooser_fecha = new com.toedter.calendar.JDateChooser();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem_reporte = new javax.swing.JMenuItem();
        jMenuItem_precios = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem_usuarios = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Pizzeria Carpizzio");
        setFocusable(false);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(149, 201, 74));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTabbedPane2.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N

        jPanel3.setBackground(new java.awt.Color(229, 242, 211));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel8.setBorder(javax.swing.BorderFactory.createMatteBorder(20, 0, 0, 0, new java.awt.Color(149, 201, 74)));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel10.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Ingresos del día");
        jLabel10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel8.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 40, 300, -1));

        jLabel5.setFont(new java.awt.Font("Poppins", 1, 48)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("0.00");
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel8.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 80, 300, -1));

        jPanel3.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 60, 300, 200));

        jPanel9.setBorder(javax.swing.BorderFactory.createMatteBorder(20, 0, 0, 0, new java.awt.Color(149, 201, 74)));
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel6.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Pedidos del día");
        jPanel9.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 40, 300, 20));

        jLabel13.setFont(new java.awt.Font("Poppins", 1, 48)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("0.00");
        jLabel13.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel9.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, 300, -1));

        jPanel3.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 280, 300, 200));

        jPanel5.setBorder(javax.swing.BorderFactory.createMatteBorder(20, 0, 0, 0, new java.awt.Color(149, 201, 74)));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Mas vendida");
        jPanel5.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 40, 300, -1));

        jLabel12.setFont(new java.awt.Font("Poppins", 1, 36)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("txt");
        jLabel12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel5.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, 300, -1));

        jPanel3.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 280, 300, 200));

        jPanel7.setBorder(javax.swing.BorderFactory.createMatteBorder(20, 0, 0, 0, new java.awt.Color(149, 201, 74)));
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel3.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 60, 620, 200));

        jPanel10.setBorder(javax.swing.BorderFactory.createMatteBorder(20, 0, 0, 0, new java.awt.Color(149, 201, 74)));
        jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel7.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Tiempo promedio de entrega");
        jPanel10.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 40, 300, -1));

        jLabel11.setFont(new java.awt.Font("Poppins", 1, 48)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("0.00");
        jLabel11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel10.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, 300, -1));

        jPanel3.add(jPanel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 280, 300, 200));

        jLabel19.setFont(new java.awt.Font("Poppins", 1, 24)); // NOI18N
        jLabel19.setText("Inicio:");
        jPanel3.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 20, -1, -1));

        jDateChooser_fecha2.setEnabled(false);
        jDateChooser_fecha2.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jPanel3.add(jDateChooser_fecha2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 10, 160, 30));

        jTabbedPane2.addTab("Inicio", jPanel3);

        jPanel2.setBackground(new java.awt.Color(229, 242, 211));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton_levantar_pedido.setBackground(new java.awt.Color(149, 201, 74));
        jButton_levantar_pedido.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jButton_levantar_pedido.setForeground(new java.awt.Color(255, 255, 255));
        jButton_levantar_pedido.setText("Levantar pedido");
        jButton_levantar_pedido.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton_levantar_pedido.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_levantar_pedidoActionPerformed(evt);
            }
        });
        jPanel2.add(jButton_levantar_pedido, new org.netbeans.lib.awtextra.AbsoluteConstraints(1010, 410, 150, 50));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Ings. Extras:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Poppins", 1, 14), new java.awt.Color(149, 201, 74))); // NOI18N
        jPanel4.setForeground(new java.awt.Color(255, 255, 255));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jCheckBox_Extra_Chorizo.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Extra_Chorizo.setText("Chorizo");
        jPanel4.add(jCheckBox_Extra_Chorizo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, -1, -1));

        jCheckBox_Extra_Tocino.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Extra_Tocino.setText("Tocino");
        jPanel4.add(jCheckBox_Extra_Tocino, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, -1, -1));

        jCheckBox_Extra_Pina.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Extra_Pina.setText("Piña");
        jPanel4.add(jCheckBox_Extra_Pina, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, -1, -1));

        jCheckBox_Extra_Champinon.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Extra_Champinon.setText("Champiñón");
        jPanel4.add(jCheckBox_Extra_Champinon, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, -1, -1));

        jCheckBox_Extra_Salami.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Extra_Salami.setText("Salami");
        jPanel4.add(jCheckBox_Extra_Salami, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, -1, -1));

        jCheckBox_Extra_Salchicha.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Extra_Salchicha.setText("Salchicha");
        jPanel4.add(jCheckBox_Extra_Salchicha, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, -1));

        jCheckBox_Extra_Pepperoni.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Extra_Pepperoni.setText("Pepperoni");
        jPanel4.add(jCheckBox_Extra_Pepperoni, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, -1, -1));

        jCheckBox_Extra_jamon.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Extra_jamon.setText("Jamón");
        jPanel4.add(jCheckBox_Extra_jamon, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        jCheckBox_extra_pastor.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_extra_pastor.setText("Pastor");
        jPanel4.add(jCheckBox_extra_pastor, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 260, -1, -1));

        jCheckBox_extra_queso.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_extra_queso.setText("Queso");
        jPanel4.add(jCheckBox_extra_queso, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 290, -1, -1));

        jCheckBox_extra_pimiento.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_extra_pimiento.setText("Pimiento");
        jPanel4.add(jCheckBox_extra_pimiento, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 320, -1, -1));

        jPanel2.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 100, 140, 360));

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));
        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Ingredientes:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Poppins", 1, 14), new java.awt.Color(149, 201, 74))); // NOI18N
        jPanel11.setForeground(new java.awt.Color(255, 255, 255));
        jPanel11.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jCheckBox_Chorizo.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Chorizo.setText("Chorizo");
        jPanel11.add(jCheckBox_Chorizo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, -1, -1));

        jCheckBox_jamon.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_jamon.setText("Jamón");
        jPanel11.add(jCheckBox_jamon, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        jCheckBox_Pepperoni.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Pepperoni.setText("Pepperoni");
        jPanel11.add(jCheckBox_Pepperoni, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, -1, -1));

        jCheckBox_Salchicha.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Salchicha.setText("Salchicha");
        jPanel11.add(jCheckBox_Salchicha, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, -1));

        jCheckBox_Ingredientes_Surtidos.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Ingredientes_Surtidos.setText("Ingredientes Surtidos");
        jCheckBox_Ingredientes_Surtidos.setContentAreaFilled(false);
        jPanel11.add(jCheckBox_Ingredientes_Surtidos, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, -1, -1));

        jCheckBox_Ingredientes_Especiales.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Ingredientes_Especiales.setText("Ingredientes Especiales");
        jCheckBox_Ingredientes_Especiales.setContentAreaFilled(false);
        jPanel11.add(jCheckBox_Ingredientes_Especiales, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 260, -1, -1));

        jCheckBox_Pastor.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Pastor.setText("Pastor");
        jCheckBox_Pastor.setContentAreaFilled(false);
        jPanel11.add(jCheckBox_Pastor, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, -1));

        jCheckBox_Pimiento.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Pimiento.setText("Pimiento");
        jCheckBox_Pimiento.setContentAreaFilled(false);
        jPanel11.add(jCheckBox_Pimiento, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, -1, -1));

        jCheckBox_Tocino.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Tocino.setText("Tocino");
        jCheckBox_Tocino.setContentAreaFilled(false);
        jPanel11.add(jCheckBox_Tocino, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, -1, -1));

        jCheckBox_Champinon.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Champinon.setText("Champiñón");
        jPanel11.add(jCheckBox_Champinon, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, -1, -1));

        jCheckBox_Salami.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Salami.setText("Salami");
        jPanel11.add(jCheckBox_Salami, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, -1));

        jCheckBox_carnes_frias.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_carnes_frias.setText("Carnes Frías");
        jCheckBox_carnes_frias.setContentAreaFilled(false);
        jPanel11.add(jCheckBox_carnes_frias, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 300, 130, -1));

        jCheckBox_campechana.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_campechana.setText("Campechana");
        jCheckBox_campechana.setContentAreaFilled(false);
        jPanel11.add(jCheckBox_campechana, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 320, -1, -1));

        jCheckBox_hawaiana.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_hawaiana.setText("Hawaiana");
        jCheckBox_hawaiana.setContentAreaFilled(false);
        jCheckBox_hawaiana.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_hawaianaActionPerformed(evt);
            }
        });
        jPanel11.add(jCheckBox_hawaiana, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, -1, -1));

        jCheckBox_pastor_especial.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_pastor_especial.setText("Pastor Especial");
        jCheckBox_pastor_especial.setContentAreaFilled(false);
        jPanel11.add(jCheckBox_pastor_especial, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, -1, -1));

        jCheckBox_Pina.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Pina.setText("Piña");
        jPanel11.add(jCheckBox_Pina, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, -1, -1));

        jPanel2.add(jPanel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 110, 220, 350));

        jPanel13.setBackground(new java.awt.Color(255, 255, 255));
        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Ingredientes:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Poppins", 1, 14), new java.awt.Color(149, 201, 74))); // NOI18N
        jPanel13.setForeground(new java.awt.Color(255, 255, 255));
        jPanel13.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jCheckBox2_Orden_de_Pan_con_Ajo.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox2_Orden_de_Pan_con_Ajo.setText("Orden de Pan con Ajo");
        jCheckBox2_Orden_de_Pan_con_Ajo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2_Orden_de_Pan_con_AjoActionPerformed(evt);
            }
        });
        jPanel13.add(jCheckBox2_Orden_de_Pan_con_Ajo, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, -1, 30));

        jCheckBox1_Orilla_de_Queso.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox1_Orilla_de_Queso.setText("Orilla de Queso");
        jPanel13.add(jCheckBox1_Orilla_de_Queso, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, -1, 30));

        jCheckBox3_Ingrediente_Extra.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox3_Ingrediente_Extra.setText("Ingrediente Extra");
        jCheckBox3_Ingrediente_Extra.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBox3_Ingrediente_ExtraStateChanged(evt);
            }
        });
        jPanel13.add(jCheckBox3_Ingrediente_Extra, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, -1, 30));

        jCheckBox4_Refresco.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox4_Refresco.setText("Refresco 1.5 L.");
        jCheckBox4_Refresco.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox4_RefrescoActionPerformed(evt);
            }
        });
        jPanel13.add(jCheckBox4_Refresco, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, -1, 30));

        jSpinner_refrescos.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jPanel13.add(jSpinner_refrescos, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 150, 70, -1));

        jSpinner_cantidad_ajo.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jPanel13.add(jSpinner_cantidad_ajo, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, -1, -1));

        jPanel2.add(jPanel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 270, 200, 190));

        jSpinner_cantidad.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jPanel2.add(jSpinner_cantidad, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 230, 110, 30));

        jLabel16.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        jLabel16.setText("Cantidad de pizzas:");
        jPanel2.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 210, -1, -1));

        jLabel17.setFont(new java.awt.Font("Poppins", 1, 24)); // NOI18N
        jLabel17.setText("Nuevo pedido:");
        jPanel2.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        jPanel14.setBackground(new java.awt.Color(255, 255, 255));
        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Pizzas disponibles", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Poppins", 1, 14), new java.awt.Color(149, 201, 74))); // NOI18N
        jPanel14.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLista.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jLista.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jLista.setSelectionBackground(new java.awt.Color(204, 255, 204));
        jLista.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListaValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jLista);

        jPanel14.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 250, 390));

        jPanel2.add(jPanel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 270, 420));

        jTextField_nTELEFONO.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jTextField_nTELEFONO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_nTELEFONOActionPerformed(evt);
            }
        });
        jTextField_nTELEFONO.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField_nTELEFONOKeyPressed(evt);
            }
        });
        jPanel2.add(jTextField_nTELEFONO, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 90, 200, 40));

        Jtext_direccion.setColumns(20);
        Jtext_direccion.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        Jtext_direccion.setRows(5);
        Jtext_direccion.setEnabled(false);
        jScrollPane2.setViewportView(Jtext_direccion);

        jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 240, 310, 160));

        Jtext_nombre.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        Jtext_nombre.setEnabled(false);
        jPanel2.add(Jtext_nombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 170, 250, 40));

        jLabel23.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jLabel23.setText("Ingrese número de teléfono:");
        jPanel2.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 70, -1, -1));

        jLabel24.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jLabel24.setText("Dirección:");
        jPanel2.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 220, -1, -1));

        jLabel25.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jLabel25.setText("Nombre del Cliente:");
        jPanel2.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 150, -1, -1));

        jCheckBox_Mitad.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jCheckBox_Mitad.setText("Mitad y mitad");
        jCheckBox_Mitad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_MitadActionPerformed(evt);
            }
        });
        jPanel2.add(jCheckBox_Mitad, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 80, -1, -1));

        jPanel12.setBackground(new java.awt.Color(255, 255, 255));
        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Tamaños", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Poppins", 1, 14), new java.awt.Color(149, 201, 74))); // NOI18N
        jPanel12.setForeground(new java.awt.Color(255, 255, 255));
        jPanel12.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jRadioButton2_Grande.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jRadioButton2_Grande.setText("Grande");
        jPanel12.add(jRadioButton2_Grande, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, -1, -1));

        jRadioButton3_Familiar.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jRadioButton3_Familiar.setText("Familiar");
        jPanel12.add(jRadioButton3_Familiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, -1));

        jRadioButton1_Chica.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jRadioButton1_Chica.setText("Chica");
        jPanel12.add(jRadioButton1_Chica, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        jPanel2.add(jPanel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 110, 150, 90));

        jDateChooser_fecha1.setEnabled(false);
        jDateChooser_fecha1.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jPanel2.add(jDateChooser_fecha1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 10, 160, 30));

        jTabbedPane2.addTab("Levantar Pedido", jPanel2);

        jPanel6.setBackground(new java.awt.Color(229, 242, 211));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton_MARCAR_ENTREGADO.setBackground(new java.awt.Color(149, 201, 74));
        jButton_MARCAR_ENTREGADO.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jButton_MARCAR_ENTREGADO.setForeground(new java.awt.Color(255, 255, 255));
        jButton_MARCAR_ENTREGADO.setText("Entregado");
        jButton_MARCAR_ENTREGADO.setBorderPainted(false);
        jButton_MARCAR_ENTREGADO.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton_MARCAR_ENTREGADO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_MARCAR_ENTREGADOActionPerformed(evt);
            }
        });
        jPanel6.add(jButton_MARCAR_ENTREGADO, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 510, 150, 30));

        jTable_pedidos_del_dia.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jTable_pedidos_del_dia.setModel(new javax.swing.table.DefaultTableModel(
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
        jTable_pedidos_del_dia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jTable_pedidos_del_dia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable_pedidos_del_diaMouseClicked(evt);
            }
        });
        jTable_pedidos_del_dia.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTable_pedidos_del_diaKeyPressed(evt);
            }
        });
        jScrollPane3.setViewportView(jTable_pedidos_del_dia);

        jPanel6.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 620, 480));

        jLabel2.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jLabel2.setText("Pedido(s):");
        jPanel6.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 40, -1, -1));

        jTextPane_Datos.setEditable(false);
        jTextPane_Datos.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jScrollPane7.setViewportView(jTextPane_Datos);

        jPanel6.add(jScrollPane7, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 370, 370, 130));

        jTextPane_pedido.setEditable(false);
        jTextPane_pedido.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jScrollPane6.setViewportView(jTextPane_pedido);

        jPanel6.add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 60, 370, 280));

        jLabel3.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jLabel3.setText("Dirección:");
        jPanel6.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 350, -1, -1));

        jLabel_hora.setText("ㅤㅤㅤ");
        jPanel6.add(jLabel_hora, new org.netbeans.lib.awtextra.AbsoluteConstraints(820, 10, -1, -1));

        jButton_Cancerlar_Pedido.setBackground(new java.awt.Color(255, 102, 102));
        jButton_Cancerlar_Pedido.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jButton_Cancerlar_Pedido.setForeground(new java.awt.Color(255, 255, 255));
        jButton_Cancerlar_Pedido.setText("Cancelar");
        jButton_Cancerlar_Pedido.setBorderPainted(false);
        jButton_Cancerlar_Pedido.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton_Cancerlar_Pedido.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_Cancerlar_PedidoActionPerformed(evt);
            }
        });
        jPanel6.add(jButton_Cancerlar_Pedido, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 510, 150, 30));

        jTextField_efectivo.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jTextField_efectivo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField_efectivoKeyPressed(evt);
            }
        });
        jPanel6.add(jTextField_efectivo, new org.netbeans.lib.awtextra.AbsoluteConstraints(1050, 320, 170, 40));

        jTextField_cambio.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jTextField_cambio.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jTextField_cambio.setEnabled(false);
        jPanel6.add(jTextField_cambio, new org.netbeans.lib.awtextra.AbsoluteConstraints(1050, 440, 170, 40));

        jTextField_total.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jTextField_total.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jTextField_total.setEnabled(false);
        jPanel6.add(jTextField_total, new org.netbeans.lib.awtextra.AbsoluteConstraints(1050, 240, 170, 40));

        jLabel8.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jLabel8.setText("Cambio:");
        jPanel6.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(1050, 420, -1, -1));

        jLabel14.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jLabel14.setText("Total a pagar:");
        jPanel6.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(1050, 220, -1, -1));

        jLabel15.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jLabel15.setText("Efectivo:");
        jPanel6.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(1050, 300, -1, -1));

        jButton_calcular.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jButton_calcular.setText("Calcular cambio");
        jButton_calcular.setToolTipText("Calcular cambio");
        jButton_calcular.setBorderPainted(false);
        jButton_calcular.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton_calcular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_calcularActionPerformed(evt);
            }
        });
        jPanel6.add(jButton_calcular, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 370, 160, -1));

        jLabel18.setFont(new java.awt.Font("Poppins", 1, 24)); // NOI18N
        jLabel18.setText("Pedidos del día:");
        jPanel6.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        jDateChooser_fecha.setEnabled(false);
        jDateChooser_fecha.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jPanel6.add(jDateChooser_fecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 10, 160, 30));

        jTabbedPane2.addTab("Pedidos en curso", jPanel6);

        jPanel1.add(jTabbedPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, 1250, 590));

        jLabel1.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Carpizzio");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, -1, 30));

        jLabel4.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/usuario.png"))); // NOI18N
        jLabel4.setText("Hola, Ussername");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(1080, 10, -1, 40));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1250, 620));

        jMenuBar1.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N

        jMenu3.setText("Archivo");

        jMenuItem4.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jMenuItem4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/clientes.png"))); // NOI18N
        jMenuItem4.setText("Consultar Informacion de los clientes");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem4);

        jMenuItem_reporte.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jMenuItem_reporte.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/reporte.png"))); // NOI18N
        jMenuItem_reporte.setText("Gestionar reportes");
        jMenuItem_reporte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_reporteActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem_reporte);

        jMenuItem_precios.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jMenuItem_precios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/precio.png"))); // NOI18N
        jMenuItem_precios.setText("Actualizar precios");
        jMenuItem_precios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_preciosActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem_precios);

        jMenuBar1.add(jMenu3);

        jMenu1.setText("Nuevo");

        jMenuItem2.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/cliente.png"))); // NOI18N
        jMenuItem2.setText("Nuevo Cliente");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem_usuarios.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jMenuItem_usuarios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/llaves.png"))); // NOI18N
        jMenuItem_usuarios.setText("Nuevo Usuario");
        jMenuItem_usuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_usuariosActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem_usuarios);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Usuarios");

        jMenuItem1.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/llaves.png"))); // NOI18N
        jMenuItem1.setText("Cambiar de usuario");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuItem6.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jMenuItem6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/informacion.png"))); // NOI18N
        jMenuItem6.setText("Acerca de");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem6);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Método auxiliar para obtener precio de dos mitades y promediarlas.
     */
    private double obtenerPrecioMitad(Connection con, String campo, String mitad1, String mitad2) throws SQLException {
        String sql = "SELECT " + campo + " FROM Pizzas WHERE nombre = ?";
        PreparedStatement p1 = con.prepareStatement(sql);
        p1.setString(1, mitad1);
        ResultSet r1 = p1.executeQuery();
        if (!r1.next()) {
            throw new SQLException("Pizza " + mitad1 + " no encontrada.");
        }
        double precio1 = r1.getDouble(1);

        PreparedStatement p2 = con.prepareStatement(sql);
        p2.setString(1, mitad2);
        ResultSet r2 = p2.executeQuery();
        if (!r2.next()) {
            throw new SQLException("Pizza " + mitad2 + " no encontrada.");
        }
        double precio2 = r2.getDouble(1);

        return (precio1 + precio2) / 2.0 + 10.0;
    }

    private void jButton_levantar_pedidoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_levantar_pedidoActionPerformed

        // ——— RESET INICIAL ———
        precioComplementos = 0.0;
        precioTotal = 0.0;
        precioExtras = 0.0;
        contador = 0;
        complementosSeleccionados = "";
        ingredientesExtrasSeleccionados = "";
        String nombrePizza = "";

        // ——— VALIDACIONES BÁSICAS ———
        // 1) Pizza seleccionada
        if (esMitadyMitad == false && jLista.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, seleccione una pizza de la lista.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2) Tamaño seleccionado
        if (!jRadioButton1_Chica.isSelected()
                && !jRadioButton2_Grande.isSelected()
                && !jRadioButton3_Familiar.isSelected()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, seleccione un tamaño.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3) Al menos un ingrediente base (cuando aplique)
        if (!(jCheckBox_jamon.isSelected()
                || jCheckBox_Pepperoni.isSelected()
                || jCheckBox_Salchicha.isSelected()
                || jCheckBox_Salami.isSelected()
                || jCheckBox_Champinon.isSelected()
                || jCheckBox_Pina.isSelected()
                || jCheckBox_Tocino.isSelected()
                || jCheckBox_Pimiento.isSelected()
                || jCheckBox_Pastor.isSelected()
                || jCheckBox_pastor_especial.isSelected()
                || jCheckBox_Ingredientes_Especiales.isSelected()
                || jCheckBox_Ingredientes_Surtidos.isSelected()
                || jCheckBox_Chorizo.isSelected()
                || jCheckBox_campechana.isSelected()
                || jCheckBox_carnes_frias.isSelected()
                || jCheckBox_hawaiana.isSelected())) {
            JOptionPane.showMessageDialog(this,
                    "Debes seleccionar al menos un ingrediente.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 5) Datos del cliente obligatorios
        String cliente = Jtext_nombre.getText().trim();
        if (cliente.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Debe ingresar el nombre del cliente.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ——— DETERMINAR TAMAÑO ———
        String campoPrecio = "";
        String tamanioLabel = "";
        if (jRadioButton1_Chica.isSelected()) {
            campoPrecio = "precio_chica";
            tamanioLabel = "Chica";
        } else if (jRadioButton2_Grande.isSelected()) {
            campoPrecio = "precio_grande";
            tamanioLabel = "Grande";
        } else {
            campoPrecio = "precio_familiar";
            tamanioLabel = "Familiar";
        }

        // ——— CONEXIÓN Y PRECIO BASE ———
        Connection conexion = null;
        double precioBasePizza = 0.0;
        try {
            GestionPizzasConexion con = new GestionPizzasConexion();
            conexion = con.Conectar();

            // Lógica para "Mitad y mitad"
            // Construir lista de ingredientes
            StringBuilder sb = new StringBuilder();

            // Verificación de cada JCheckBox en el orden original
            if (jCheckBox_jamon.isSelected()) {
                sb.append("Jamon, ");
                nombrePizza = "Jamon";
            }
            if (jCheckBox_Pepperoni.isSelected()) {
                sb.append("Pepperoni, ");
                nombrePizza = "Pepperoni";
            }
            if (jCheckBox_Salchicha.isSelected()) {
                sb.append("Salchicha, ");
                nombrePizza = "Salchicha";
            }
            if (jCheckBox_Salami.isSelected()) {
                sb.append("Salami, ");
                nombrePizza = "Salami";
            }
            if (jCheckBox_Chorizo.isSelected()) {
                sb.append("Chorizo, ");
                nombrePizza = "Chorizo";
            }
            if (jCheckBox_Champinon.isSelected()) {
                sb.append("Champinon, ");
                nombrePizza = "Champinon";
            }
            if (jCheckBox_Pina.isSelected()) {
                sb.append("Pina, ");
                nombrePizza = "Pina";
            }
            if (jCheckBox_hawaiana.isSelected()) {
                sb.append("Hawaiana, ");
                nombrePizza = "Hawaiana";
            }
            if (jCheckBox_carnes_frias.isSelected()) {
                sb.append("Carnes frias, ");
                nombrePizza = "Carnes frias";
            }
            if (jCheckBox_campechana.isSelected()) {
                sb.append("Campechana, ");
                nombrePizza = "Campechana";
            }
            if (jCheckBox_Pastor.isSelected()) {
                sb.append("Pastor, ");
                nombrePizza = "Pastor";
            }
            if (jCheckBox_pastor_especial.isSelected()) {
                sb.append("Pastor Especial, ");
                nombrePizza = "Pastor Especial";
            }
            if (jCheckBox_Tocino.isSelected()) {
                sb.append("Tocino, ");
                nombrePizza = "Tocino";
            }
            if (jCheckBox_Pimiento.isSelected()) {
                sb.append("Pimiento, ");
                nombrePizza = "Pimiento";
            }
            if (jCheckBox_Ingredientes_Surtidos.isSelected()) {
                sb.append("Pizza Surtida, ");
                nombrePizza = "Pizza Surtida";
            }
            if (jCheckBox_Ingredientes_Especiales.isSelected()) {
                sb.append("Carpizzio Especial, ");
                nombrePizza = "Carpizzio Especial";
            }

            // Validación: al menos un ingrediente
            if (sb.length() == 0) {
                JOptionPane.showMessageDialog(
                        null,
                        "Error: Debe seleccionar al menos un ingrediente.",
                        "Validación de ingredientes",
                        JOptionPane.WARNING_MESSAGE
                );
                return;  // Salimos si no hay selección
            }

            // Quitamos la coma y espacio final
            String listaIngredientes = sb.substring(0, sb.length() - 2);

            if (esMitadyMitad == true) {

                String cadena = sb.toString().replaceAll(", $", "");
                String[] mitades = cadena.split(",\\s*");
                if (mitades.length != 2) {
                    JOptionPane.showMessageDialog(this,
                            "Para 'Mitad y mitad' debes seleccionar exactamente dos ingredientes.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (mitades[0].equalsIgnoreCase(mitades[1])) {
                    JOptionPane.showMessageDialog(this,
                            "No puedes elegir dos veces la misma pizza para 'Mitad y mitad'.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Precios de cada mitad
                precioBasePizza = obtenerPrecioMitad(conexion, campoPrecio, mitades[0], mitades[1]);
                nombrePizza = "Mitad y mitad";

                System.out.println("precio mitad" + precioBasePizza);
            } else {

                nombrePizza = jLista.getSelectedValue();
                System.out.println("nombre" + nombrePizza);

                // Pizza normal: un solo SELECT
                String sql = "SELECT " + campoPrecio + " FROM Pizzas WHERE nombre = ?";
                PreparedStatement ps = conexion.prepareStatement(sql);
                ps.setString(1, nombrePizza);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this,
                            "Pizza \"" + nombrePizza + "\" no encontrada.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                precioBasePizza = rs.getDouble(1);
                System.out.println("precio1 " + precioBasePizza);
            }

            // ——— COMPLEMENTOS ———
            // Orilla de queso
            if (jCheckBox1_Orilla_de_Queso.isSelected()) {
                double p = obtenerPrecioComplemento("Orilla de Queso", campoPrecio);
                precioComplementos += p;
                complementosSeleccionados += "Orilla de Queso, ";
            }

            // Pan con ajo
            if (jCheckBox2_Orden_de_Pan_con_Ajo.isSelected()) {
                jSpinner_cantidad_ajo.setEnabled(true);
                int cant = (int) jSpinner_cantidad_ajo.getValue();
                if (cant <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Debes indicar cantidad de Pan con Ajo.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double pu = obtenerPrecioComplemento("Orden de Pan con Ajo", campoPrecio);
                precioComplementos += pu * cant;
                complementosSeleccionados += String.format("%d x Pan con Ajo, ", cant);
            } else {
                jSpinner_cantidad_ajo.setEnabled(false);
            }

            // Ingredientes extra general
            if (jCheckBox3_Ingrediente_Extra.isSelected()) {
                StringBuilder sbExtra = new StringBuilder();
                if (jCheckBox_Extra_jamon.isSelected()) {
                    sbExtra.append("Jamon, ");
                    contador++;
                }
                if (jCheckBox_Extra_Pepperoni.isSelected()) {
                    sbExtra.append("Pepperoni, ");
                    contador++;
                }
                if (jCheckBox_Extra_Salchicha.isSelected()) {
                    sbExtra.append("Salchicha, ");
                    contador++;
                }
                if (jCheckBox_Extra_Salami.isSelected()) {
                    sbExtra.append("Salami, ");
                    contador++;
                }
                if (jCheckBox_Extra_Champinon.isSelected()) {
                    sbExtra.append("Champinon, ");
                    contador++;
                }
                if (jCheckBox_Extra_Pina.isSelected()) {
                    sbExtra.append("Pina, ");
                    contador++;
                }
                if (jCheckBox_Extra_Tocino.isSelected()) {
                    sbExtra.append("Tocino, ");
                    contador++;
                }
                if (jCheckBox_Extra_Chorizo.isSelected()) {
                    sbExtra.append("Chorizo, ");
                    contador++;
                }
                if (jCheckBox_extra_pastor.isSelected()) {
                    sbExtra.append("Chorizo, ");
                    contador++;
                }
                if (jCheckBox_extra_queso.isSelected()) {
                    sbExtra.append("Chorizo, ");
                    contador++;
                }
                if (jCheckBox_extra_pimiento.isSelected()) {
                    sbExtra.append("Pimiento, ");
                    contador++;
                }

                ingredientesExtrasSeleccionados = sbExtra.toString().replaceAll(", $", "");
                precioExtras = obtenerPrecioComplemento("Ingrediente Extra", campoPrecio);
            } else {
                ingredientesExtrasSeleccionados = "n/a";
            }

            // Refrescos
            if (jCheckBox4_Refresco.isSelected()) {
                jSpinner_refrescos.setEnabled(true);
                int cantRef = (int) jSpinner_refrescos.getValue();
                if (cantRef <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Debes indicar la cantidad de refrescos.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double pr = obtenerPrecioComplemento("Refresco 1.5 L.", campoPrecio);
                precioComplementos += pr * cantRef;
                complementosSeleccionados += String.format("%d x Refresco 1.5L, ", cantRef);
            } else {
                jSpinner_refrescos.setEnabled(false);
            }

            // Eliminar comas finales sobrantes
            complementosSeleccionados = complementosSeleccionados.replaceAll(", $", "");

            // ——— CÁLCULO FINAL ———
            int cantidadPizzas = (int) jSpinner_cantidad.getValue();
            precioTotal = (precioBasePizza * cantidadPizzas)
                    + precioComplementos
                    + (precioExtras * contador);

            String horaActualStr = LocalTime.now().toString();

            // Elimina la coma y el espacio al final si hay ingredientes seleccionados
            String ingredientes = sb.toString().replaceAll(", $", "");

            // ——— CREAR OBJETO PizzaData y PREGUNTAR AGREGAR MÁS ———
            PizzaData pizzaActual = new PizzaData(
                    cliente, nombrePizza, tamanioLabel,
                    ingredientes,
                    complementosSeleccionados,
                    ingredientesExtrasSeleccionados,
                    precioBasePizza, precioComplementos,
                    cantidadPizzas, precioTotal,
                    horaActualStr, estado
            );

            Object[] opciones = {"Sí", "No", "Cancelar orden"};
            int opcion = JOptionPane.showOptionDialog(
                    this,
                    "¿Deseas agregar otra pizza?",
                    "Agregar otra pizza",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opciones,
                    opciones[0]
            );

            boolean ordenCancelada = false; // <--- Nueva variable para controlar cancelación

            if (opcion == 0) { // Sí
                if (pizzaActual != null) {
                    ordenesPendientes.add(pizzaActual);
                    limpiarCamposParaNuevaPizza();
                } else {
                    JOptionPane.showMessageDialog(this, "No hay una pizza válida para agregar.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                return; // <-- salir para que no se guarde aún
            } else if (opcion == 1) { // No
                if (pizzaActual != null) {
                    ordenesPendientes.add(pizzaActual);
                }
                // Continúa para guardar al final
            } else if (opcion == 0) { // Cancelar pedido
                int confirmar = JOptionPane.showConfirmDialog(this,
                        "¿Estás seguro(a) de que deseas cancelar la orden?", "Confirmar cancelación",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirmar == JOptionPane.YES_OPTION) {
                    ordenesPendientes.clear();
                    limpiarCamposParaNuevaPizza();
                    JOptionPane.showMessageDialog(this, "Pedido cancelado correctamente.", "Pedido cancelado", JOptionPane.INFORMATION_MESSAGE);
                    ordenCancelada = true; // <-- marcar que se canceló
                } else {
                    return; // si no confirma cancelar, salir sin guardar
                }
            } else {
                JOptionPane.showMessageDialog(this, "No se seleccionó ninguna opción.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

// SOLO GUARDAR SI NO SE CANCELÓ
            if (!ordenCancelada) {
                Jtext_nombre.setText("");
                jTextField_nTELEFONO.setText("");
                Jtext_direccion.setText("");

                try {
                    guardarOrden(ordenesPendientes, cliente, estado);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error al guardar el pedido:\n" + ex.getMessage(),
                            "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                }

                // LIMPIEZA FINAL Y ACTUALIZACIÓN UI
                ordenesPendientes.clear();
                limpiarCamposParaNuevaPizza();
                jButton_levantar_pedido.setEnabled(false);
                cargarRegistrosDelDia();
                configurarRendererMultilinea();

                calcularIngresosDelDia();
                obtenerPizzaMasVendida();
                obtenerCantidadPedidosDelDia();
                calcularTiempoPromedioEntrega();
                mostrarGrafica();

            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error en la base de datos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conexion != null) {
                try {
                    conexion.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }//GEN-LAST:event_jButton_levantar_pedidoActionPerformed
    // 1) Crear pedido y devolver su ID
    public int crearPedido(String cliente, String estado) throws SQLException {
        GestionPizzasConexion gp = new GestionPizzasConexion();
        Connection conn = gp.Conectar();
        if (conn == null) {
            throw new SQLException("No se pudo establecer la conexión al crear pedido");
        }

        String sql = "INSERT INTO pedido(cliente, estado) VALUES(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente);
            ps.setString(2, estado);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("No se obtuvo id_pedido");
                }
            }
        } finally {
            gp.CerrarConexion();
        }
    }

    // 2) Insertar cada pizza vinculada al pedido
    public void insertarVentaConPedido(int idPedido,
            String nombreCliente,
            String nombrePizza,
            String tamanoPizza,
            String ingredientes,
            double precioPizza,
            String complementos,
            double precioComplemento,
            String ingredientesExtras,
            int cantidad_pizzas,
            double totalVenta,
            String horaStr,
            String estado) throws SQLException {
        GestionPizzasConexion gp = new GestionPizzasConexion();
        Connection conn = gp.Conectar();
        if (conn == null) {
            throw new SQLException("No se pudo establecer la conexión al insertar venta");
        }

        String sql = ""
                + "INSERT INTO ventas ("
                + "  id_pedido, nombre_cliente, fecha_venta, "
                + "  nombre_pizza, tamanio_pizza, ingredientes, precio_pizza, "
                + "  complementos, precio_complemento, ingredientesExtras, "
                + "  cantidad_pizzas, total_venta, hora_pedido, estado"
                + ") VALUES (?, ?, CURDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            LocalTime hora = LocalTime.parse(horaStr);

            ps.setInt(1, idPedido);
            ps.setString(2, nombreCliente);
            ps.setString(3, nombrePizza);
            ps.setString(4, tamanoPizza);
            ps.setString(5, ingredientes);
            ps.setDouble(6, precioPizza);
            ps.setString(7, complementos);
            ps.setDouble(8, precioComplemento);
            ps.setString(9, ingredientesExtras);
            ps.setInt(10, cantidad_pizzas);
            ps.setDouble(11, totalVenta);
            ps.setTime(12, Time.valueOf(hora));
            ps.setString(13, estado);

            ps.executeUpdate();
        } finally {
            gp.CerrarConexion();
        }
    }

    // 3) Actualizar el total acumulado del pedido
    public void actualizarTotalPedido(int idPedido, double total) throws SQLException {
        GestionPizzasConexion gp = new GestionPizzasConexion();
        Connection conn = gp.Conectar();
        if (conn == null) {
            throw new SQLException("No se pudo establecer la conexión al actualizar total");
        }

        String sql = "UPDATE pedido SET total = ? WHERE id_pedido = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, total);
            ps.setInt(2, idPedido);
            ps.executeUpdate();
        } finally {
            gp.CerrarConexion();
        }
    }

    // 4) Orquestador: crea pedido, inserta detalles, actualiza total y muestra mensaje
    public void guardarOrden(java.util.List<PizzaData> ordenesPendientes,
            String cliente,
            String estado) {
        try {
            // 4.1 Crear la cabecera de pedido
            int idPedido = crearPedido(cliente, estado);

            // 4.2 Insertar cada pizza y acumular total
            double totalAcumulado = 0;
            for (PizzaData pd : ordenesPendientes) {
                insertarVentaConPedido(
                        idPedido,
                        pd.cliente,
                        pd.nombrePizza,
                        pd.tamanoPizza,
                        pd.ingredientes,
                        pd.precioPizza,
                        pd.complementos,
                        pd.precioComplemento,
                        pd.ingredientesExtras,
                        pd.cantidad,
                        pd.totalVenta,
                        pd.hora,
                        pd.estado
                );
                totalAcumulado += pd.totalVenta;
            }

            // 4.3 Actualizar el total en la cabecera
            actualizarTotalPedido(idPedido, totalAcumulado);

            // 4.4 Mostrar confirmación
            JOptionPane.showMessageDialog(
                    null,
                    String.format(
                            "Pedido #%d registrado correctamente.%n"
                            + "Pizzas: %d%nTotal: $%.2f",
                            idPedido,
                            ordenesPendientes.size(),
                            totalAcumulado
                    ),
                    "Pedido Registrado",
                    JOptionPane.INFORMATION_MESSAGE
            );

            String ticket = Ticket.generarTicketPedido(idPedido);
            Ticket.imprimirTicket(ticket);

            // 4.5 Limpiar lista y UI
            ordenesPendientes.clear();
            limpiarCamposParaNuevaPizza();  // llama al método de tu UI

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error al guardar la orden:\n" + ex.getMessage(),
                    "Error de Base de Datos",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void limpiarCamposParaNuevaPizza() {
        DesmarcarExtras();
        deshabilitarJCheckBoxIngredientes();
        DeshabilitarExtras();
        jCheckBox_Mitad.setSelected(false);
        jLista.setSelectedIndex(0);
        GrupoTamaño.clearSelection();
        jSpinner_cantidad.setValue(1);

        jSpinner_refrescos.setValue(1);
        jSpinner_refrescos.setEnabled(false);
        jSpinner_cantidad_ajo.setValue(1);
        jSpinner_cantidad_ajo.setEnabled(false);
        jSpinner_cantidad.setValue(1);
        jSpinner_cantidad.setEnabled(false);

        jLista.clearSelection();
        deshabilitarJCheckBoxIngredientes();

        Jtext_nombre.setText("");
        jTextField_nTELEFONO.setText("");
        Jtext_direccion.setText("");

        jRadioButton1_Chica.setEnabled(false);
        jRadioButton2_Grande.setEnabled(false);
        jRadioButton3_Familiar.setEnabled(false);

        jCheckBox1_Orilla_de_Queso.setEnabled(false);
        jCheckBox2_Orden_de_Pan_con_Ajo.setEnabled(false);
        jCheckBox3_Ingrediente_Extra.setEnabled(false);
        jCheckBox4_Refresco.setEnabled(false);
    }

    public void insertarVenta(String nombreCliente, String nombrePizza, String tamanoPizza, String ingredientes, double precioPizza, String complementos, double precioComplemento, String ingredientesExtras, int cantidad_pizzas, double totalVenta, String horaStr, String estado) {
        try {
            // Convertir la cadena de hora a LocalTime
            LocalTime hora = LocalTime.parse(horaStr);

            // Preparar la consulta SQL para la inserción
            String sql = "INSERT INTO Ventas (nombre_cliente, fecha_venta, nombre_pizza, tamanio_pizza, ingredientes, precio_pizza, complementos, precio_complemento, ingredientesExtras, cantidad_pizzas, total_venta, hora_pedido, estado) VALUES (?, CURDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // Obtener la conexión
            GestionPizzasConexion con = new GestionPizzasConexion();
            conexion = con.Conectar();

            // Preparar la declaración
            try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
                // Establecer los parámetros de la consulta
                pstmt.setString(1, nombreCliente);
                pstmt.setString(2, nombrePizza);
                pstmt.setString(3, tamanoPizza);
                pstmt.setString(4, ingredientes);
                pstmt.setDouble(5, precioPizza);
                pstmt.setString(6, complementos);
                pstmt.setDouble(7, precioComplemento);
                pstmt.setString(8, ingredientesExtras);
                pstmt.setInt(9, cantidad_pizzas);
                pstmt.setDouble(10, totalVenta);
                pstmt.setTime(11, Time.valueOf(hora)); // Aquí se pasa el objeto LocalTime correctamente
                pstmt.setString(12, estado);

                // Ejecutar la inserción
                pstmt.executeUpdate();

                // Cerrar la conexión
                conexion.close();

            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al insertar venta: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double obtenerPrecioComplemento(String nombreComplemento, String tamanoPizza) {
        double precio = 0.0;

        // Conexión a BD…
        Connection conexion = null;
        try {
            GestionPizzasConexion con = new GestionPizzasConexion();
            conexion = con.Conectar();

            String sql = "SELECT " + tamanoPizza + " FROM Complementos WHERE nombre = ?";
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, nombreComplemento);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                precio = rs.getDouble(1);
            } else {
                JOptionPane.showMessageDialog(null,
                        "Complemento no encontrado en la base de datos",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error al consultar complemento: " + e,
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conexion != null) {
                try {
                    conexion.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return precio;
    }


    private void jListaValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListaValueChanged

        esMitadyMitad = false;
        // Obtener el nombre de la pizza seleccionada en el JList
        String pizzaSeleccionada = (jLista.getSelectedValue() != null) ? jLista.getSelectedValue().toString() : "";

        // Realizar la consulta a la base de datos para obtener los ingredientesDisponibles
        // Obtener la lista de ingredientesDisponibles
        // Llamar al método para manejar la habilitación de los JCheckBox
        habilitarJCheckBoxSegunNombre(pizzaSeleccionada);


    }//GEN-LAST:event_jListaValueChanged

    private void jCheckBox3_Ingrediente_ExtraStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBox3_Ingrediente_ExtraStateChanged
        // Verificar si el jCheckBox3_Ingrediente_Extra está seleccionado
        if (jCheckBox3_Ingrediente_Extra.isSelected()) {
            jCheckBox_extra_pastor.setEnabled(true);
            jCheckBox_extra_queso.setEnabled(true);
            jCheckBox_extra_pimiento.setEnabled(true);
            jCheckBox_Extra_Pepperoni.setEnabled(true);
            jCheckBox_Extra_jamon.setEnabled(true);
            jCheckBox_Extra_Salchicha.setEnabled(true);
            jCheckBox_Extra_Salami.setEnabled(true);
            jCheckBox_Extra_Champinon.setEnabled(true);
            jCheckBox_Extra_Pina.setEnabled(true);
            jCheckBox_Extra_Tocino.setEnabled(true);
            jCheckBox_Extra_Chorizo.setEnabled(true);
        } else {
            // Deshabilitar los otros checkboxes
            DeshabilitarExtras();
            DesmarcarExtras();

        }
    }//GEN-LAST:event_jCheckBox3_Ingrediente_ExtraStateChanged

    private void jTable_pedidos_del_diaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable_pedidos_del_diaKeyPressed


    }//GEN-LAST:event_jTable_pedidos_del_diaKeyPressed

    private void jButton_MARCAR_ENTREGADOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_MARCAR_ENTREGADOActionPerformed

        // 1) Obtén la fila y el id_pedido seleccionado
        int fila = jTable_pedidos_del_dia.getSelectedRow();

        if (fila == -1) {
            return;
        } else {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "¿Deseas marcar este pedido como entregado?",
                    "Confirmación",
                    JOptionPane.YES_NO_OPTION
            );

            if (option != JOptionPane.YES_OPTION) {
                return;
            }

            if (fila < 0) {
                JOptionPane.showMessageDialog(this,
                        "Por favor, selecciona primero un pedido en la tabla.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Asumimos que la columna 0 es id_pedido
            int idPedido;
            try {
                idPedido = Integer.parseInt(jTable_pedidos_del_dia.getValueAt(fila, 0).toString());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "El ID de pedido no es válido: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2) Conexión
            Connection conexion = new GestionPizzasConexion().Conectar();
            if (conexion == null) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo conectar a la base de datos.",
                        "Error de Conexión",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // 3) Calcula el tiempo total de entrega: 
                //    obtenemos la hora de pedido más temprana (o podrías hacer por venta si lo prefieres)
                String sqlHoraPedido
                        = "SELECT MIN(hora_pedido) AS hora_inicio "
                        + "FROM ventas "
                        + "WHERE id_pedido = ?";
                LocalTime horaPedido;
                try (PreparedStatement ps1 = conexion.prepareStatement(sqlHoraPedido)) {
                    ps1.setInt(1, idPedido);
                    try (ResultSet rs = ps1.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("No existen registros de venta para el pedido " + idPedido);
                        }
                        horaPedido = rs.getTime("hora_inicio").toLocalTime();
                    }
                }

                // Hora actual como String
                String horaActualStr = new SimpleDateFormat("HH:mm:ss").format(new Date());
                LocalTime horaActual = LocalTime.parse(horaActualStr, DateTimeFormatter.ofPattern("HH:mm:ss"));

                // Diferencia en minutos
                long diffMs = Duration.between(horaPedido, horaActual).toMillis();
                int minutosEntrega = (int) TimeUnit.MILLISECONDS.toMinutes(diffMs);

                // 4) Actualiza tabla ventas: todos los registros con ese id_pedido
                String sqlUpdVentas
                        = "UPDATE ventas "
                        + "SET estado = 'Entregado', hora_entrega = ?, tiempo_entrega_minutos = ? "
                        + "WHERE id_pedido = ?";
                try (PreparedStatement ps2 = conexion.prepareStatement(sqlUpdVentas)) {
                    ps2.setString(1, horaActualStr);
                    ps2.setInt(2, minutosEntrega);
                    ps2.setInt(3, idPedido);
                    ps2.executeUpdate();
                }

                // 5) Actualiza tabla pedido
                String sqlUpdPedido
                        = "UPDATE pedido "
                        + "SET estado = 'Entregado' "
                        + "WHERE id_pedido = ?";
                try (PreparedStatement ps3 = conexion.prepareStatement(sqlUpdPedido)) {
                    ps3.setInt(1, idPedido);
                    ps3.executeUpdate();
                }

                // 6) Feedback
                JOptionPane.showMessageDialog(this,
                        "Pedido marcado como entregado!\nHora de entrega: " + horaActualStr
                        + "\nMinutos transcurridos: " + minutosEntrega,
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);

                // 7) Refresca UI
                mostrarDatosSeleccionados(idPedido);
                cargarRegistrosDelDia();
                jTextPane_pedido.setText("");
                jTextPane_Datos.setText("");
                jTextField_total.setText("");
                jTextField_efectivo.setText("");
                jTextField_cambio.setText("");
                jLabel_hora.setText("");
                calcularIngresosDelDia();
                obtenerPizzaMasVendida();
                obtenerCantidadPedidosDelDia();
                calcularTiempoPromedioEntrega();
                mostrarGrafica();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al marcar como entregado: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                try {
                    conexion.close();
                } catch (SQLException e) {
                    // Ignorar
                }
            }
        }


    }//GEN-LAST:event_jButton_MARCAR_ENTREGADOActionPerformed

    private void jTable_pedidos_del_diaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable_pedidos_del_diaMouseClicked

        // 1. Obtén la conexión desde tu clase de conexión
        GestionPizzasConexion gp = new GestionPizzasConexion();
        Connection conexion = gp.Conectar();

        // 2. Verifica que la conexión no sea null
        if (conexion == null) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar a la base de datos.",
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Averigua qué fila y qué id_pedido está seleccionado
        int fila = jTable_pedidos_del_dia.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Debes seleccionar primero un pedido en la tabla.",
                    "Sin selección",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idPedido;
        try {
            idPedido = Integer.parseInt(jTable_pedidos_del_dia.getValueAt(fila, 0).toString());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "El valor de id_pedido no es válido.",
                    "Error de Datos",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        mostrarDatosSeleccionados(idPedido);

        // 4. Consulta para obtener sólo ese pedido en concreto, incluyendo el total del pedido
        String sql = "SELECT "
                + "v.nombre_pizza, "
                + "v.tamanio_pizza, "
                + "v.cantidad_pizzas, "
                + "v.ingredientes, "
                + "v.ingredientesExtras, "
                + "v.complementos, "
                + "v.precio_complemento, "
                + "v.total_venta, "
                + "v.hora_pedido, "
                + "v.hora_entrega, "
                + "p.estado, "
                + "p.total "
                + "FROM ventas v "
                + "JOIN pedido p ON v.id_pedido = p.id_pedido "
                + "WHERE v.fecha_venta = CURDATE() "
                + "  AND v.id_pedido = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder html = new StringBuilder();
                html.append("<html><body style='font-family:Segoe UI, sans-serif;'>");

                BigDecimal totalPedido = BigDecimal.ZERO;
                boolean primeraFila = true;

                while (rs.next()) {
                    // Capturamos el total (será el mismo en cada fila, así que lo guardamos sólo la primera vez)
                    if (primeraFila) {
                        totalPedido = rs.getBigDecimal("total");
                        primeraFila = false;
                        jTextField_total.setText(totalPedido.toString());
                    }

                    html.append("<div style='margin-bottom:10px;'>")
                            .append("<b>Nombre Pizza:</b> ").append(rs.getString("nombre_pizza")).append("<br>")
                            .append("<b>Tamaño Pizza:</b> ").append(rs.getString("tamanio_pizza")).append("<br>")
                            .append("<b>Cantidad:</b> ").append(rs.getInt("cantidad_pizzas")).append("<br>")
                            .append("<b>Ingredientes:</b> ").append(rs.getString("ingredientes")).append("<br>")
                            .append("<b>Ingredientes Extras:</b> ")
                            .append(rs.getString("ingredientesExtras") == null ? "n/a" : rs.getString("ingredientesExtras"))
                            .append("<br>")
                            .append("<b>Complementos:</b> ")
                            .append(rs.getString("complementos") == null ? "n/a" : rs.getString("complementos"))
                            .append("<br>")
                            .append("<b>Precio Complemento/s:</b> ").append(rs.getBigDecimal("precio_complemento")).append("<br>")
                            .append("<b>Total Venta:</b> ").append(rs.getBigDecimal("total_venta")).append("<br>")
                            .append("<b>Hora pedido:</b> ").append(rs.getTime("hora_pedido")).append("<br>")
                            .append("<b>Hora entrega:</b> ").append(rs.getTime("hora_entrega")).append("<br>");

                    String estado = rs.getString("estado");
                    String color = estado.equalsIgnoreCase("Activo") ? "green" : "red";
                    html.append("<b>Estado:</b> ")
                            .append("<span style='color:").append(color).append(";'>")
                            .append(estado).append("</span>");

                    html.append("</div><hr>");
                }

                html.append("</body></html>");

                // 6. Muestra el HTML en el JTextPane
                jTextPane_pedido.setContentType("text/html");
                jTextPane_pedido.setText(html.toString());
                jTextPane_pedido.setCaretPosition(0);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar los detalles del pedido: " + ex.getMessage(),
                    "Error de Base de Datos",
                    JOptionPane.ERROR_MESSAGE);
        }


    }//GEN-LAST:event_jTable_pedidos_del_diaMouseClicked

    private void jButton_Cancerlar_PedidoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_Cancerlar_PedidoActionPerformed

        DefaultTableModel modelo = (DefaultTableModel) jTable_pedidos_del_dia.getModel();
        int filaSeleccionada = jTable_pedidos_del_dia.getSelectedRow();

        if (filaSeleccionada == -1) {
            return;
        } else {

        }

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona una fila para cancelar el pedido.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Obtenemos el id_pedido de la columna 0
        int idPedido = Integer.parseInt(modelo.getValueAt(filaSeleccionada, 0).toString());

        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro/a de que deseas cancelar este pedido?",
                "Confirmar cancelación",
                JOptionPane.YES_NO_OPTION);

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        Connection conexion = new GestionPizzasConexion().Conectar();
        if (conexion == null) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar a la base de datos.",
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Iniciamos transacción
            conexion.setAutoCommit(false);

            // 1) Borrar todos los registros de ventas asociados
            String sqlBorrarVentas = "DELETE FROM ventas WHERE id_pedido = ?";
            try (PreparedStatement ps1 = conexion.prepareStatement(sqlBorrarVentas)) {
                ps1.setInt(1, idPedido);
                ps1.executeUpdate();
            }

            // 2) Borrar el pedido en sí
            String sqlBorrarPedido = "DELETE FROM pedido WHERE id_pedido = ?";
            try (PreparedStatement ps2 = conexion.prepareStatement(sqlBorrarPedido)) {
                ps2.setInt(1, idPedido);
                ps2.executeUpdate();
            }

            // Commit si todo va bien
            conexion.commit();

            // Actualizar UI
            modelo.removeRow(filaSeleccionada);
            jTextPane_pedido.setText("");
            jTextPane_Datos.setText("");
            jTextField_total.setText("");
            cargarRegistrosDelDia();
            calcularIngresosDelDia();
            obtenerPizzaMasVendida();
            obtenerCantidadPedidosDelDia();
            calcularTiempoPromedioEntrega();
            mostrarGrafica();

            jLabel_hora.setText("");

            JOptionPane.showMessageDialog(this,
                    "Pedido cancelado y eliminado exitosamente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            try {
                conexion.rollback();
            } catch (SQLException ex) {
                // Ignorar
            }
            JOptionPane.showMessageDialog(this,
                    "Error al cancelar el pedido: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                conexion.setAutoCommit(true);
                conexion.close();
            } catch (SQLException ex) {
                // Ignorar
            }
        }

    }//GEN-LAST:event_jButton_Cancerlar_PedidoActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
       
        nocerrar();
        Login log = new Login();
        log.setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        AgregarCliente f = new AgregarCliente("");
        f.setVisible(true);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem_usuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_usuariosActionPerformed
        AgregarUsuario ag = new AgregarUsuario();
        ag.setVisible(true);
    }//GEN-LAST:event_jMenuItem_usuariosActionPerformed

    private void jButton_calcularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_calcularActionPerformed
// Obtener el total y el efectivo como cadenas desde los campos de texto
        String totalStr = jTextField_total.getText().trim();
        String efectivoStr = jTextField_efectivo.getText().trim();

// Verificar que los campos no estén vacíos
        if (totalStr.isEmpty() || efectivoStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor, llena ambos campos: total y efectivo.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Convertir las cadenas a números
            double total = Double.parseDouble(totalStr);
            double efectivo = Double.parseDouble(efectivoStr);

            // Validar que los valores no sean negativos
            if (total < 0 || efectivo < 0) {
                JOptionPane.showMessageDialog(null, "Los valores no pueden ser negativos.", "Valor inválido", JOptionPane.WARNING_MESSAGE);
                jTextField_efectivo.setText("");
                return;
            }

            // Validar que el efectivo no sea menor que el total
            if (efectivo < total) {
                JOptionPane.showMessageDialog(null, "El efectivo no puede ser menor que el total.", "Efectivo insuficiente", JOptionPane.WARNING_MESSAGE);
                jTextField_efectivo.setText("");
                return;
            }

            // Calcular el cambio
            double cambio = efectivo - total;

            // Mostrar el cambio en el jTextField_cambio
            jTextField_cambio.setText(String.format("%.2f", cambio));

        } catch (NumberFormatException e) {
            // Manejar errores de formato si los campos no contienen números válidos
            JOptionPane.showMessageDialog(null, "Por favor, ingresa números válidos en los campos de total y efectivo.", "Error de formato", JOptionPane.ERROR_MESSAGE);
            jTextField_efectivo.setText("");
        }

    }//GEN-LAST:event_jButton_calcularActionPerformed

    private void jTextField_efectivoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField_efectivoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            // Presionó Enter, mueve el foco al campo de texto txt_password
            jButton_calcular.doClick();
        }
    }//GEN-LAST:event_jTextField_efectivoKeyPressed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        GestionarClientes ges = new GestionarClientes();
        ges.setVisible(true);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jTextField_nTELEFONOKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField_nTELEFONOKeyPressed
        String clienteNumero = jTextField_nTELEFONO.getText().trim();
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            Connection con = null;
            try {
                con = new GestionPizzasConexion().Conectar();
                int idcliente = obtenerIdClientePorTelefono(con, clienteNumero);

                String nombreCliente = "";
                String direccionCliente = "";
                if (idcliente == -1) {
                    // 2) Cliente no existe → aviso y pide nombre
                    JOptionPane.showMessageDialog(
                            this,
                            "El cliente no está registrado.",
                            "Cliente no encontrado",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    AgregarCliente ac = new AgregarCliente(jTextField_nTELEFONO.getText());
                    ac.setVisible(true);

                    // 3) Registra usando tu método original y recupera el nuevo ID
                    //idcliente = registrarNuevoCliente(con, nombreCliente.trim(), clienteNumero);
                } else {
                    // 4) Cliente existe → asume que tienes un método para obtener su nombre
                    //    por ejemplo: obtenerNombreClientePorId(con, idcliente)
                    nombreCliente = obtenerNombreClientePorId(con, idcliente);
                    direccionCliente = obtenerDireccionClientePorId(con, idcliente);
                }

                // 5) Pone el nombre en el JTextField y mueve el foco
                Jtext_nombre.setText(nombreCliente);

                Jtext_direccion.setText(direccionCliente);

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Error de base de datos: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException ex) {
                        /* ignoro */ }

                }
            }
        }

        /**
         * ***************************
         * /*
         *
         */
        char c = evt.getKeyChar();

        // Permitir borrar con Backspace o Delete
        if (Character.isISOControl(c)) {
            return;
        }

        // Limitar a 10 caracteres
        if (clienteNumero.length() >= 10) {
            evt.consume();  // Cancela la tecla para que no se agregue
            JOptionPane.showMessageDialog(this, "Solo se permiten 10 dígitos.", "Límite alcanzado", JOptionPane.WARNING_MESSAGE);
            jTextField_nTELEFONO.requestFocus();
        }

        jButton_levantar_pedido.setEnabled(true);

    }//GEN-LAST:event_jTextField_nTELEFONOKeyPressed

    private void jCheckBox4_RefrescoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox4_RefrescoActionPerformed

        if (jCheckBox4_Refresco.isSelected()) {
            jSpinner_refrescos.setEnabled(true);
            jSpinner_refrescos.setValue(1);
        } else {
            jSpinner_refrescos.setEnabled(false);
        }
    }//GEN-LAST:event_jCheckBox4_RefrescoActionPerformed

    private void jMenuItem_reporteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_reporteActionPerformed

        reportes rep = new reportes();
        rep.setVisible(true);

    }//GEN-LAST:event_jMenuItem_reporteActionPerformed

    private void jCheckBox2_Orden_de_Pan_con_AjoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2_Orden_de_Pan_con_AjoActionPerformed
        if (jCheckBox2_Orden_de_Pan_con_Ajo.isSelected()) {
            jSpinner_cantidad_ajo.setEnabled(true);
            jSpinner_cantidad_ajo.setValue(1);
        } else {
            jSpinner_cantidad_ajo.setEnabled(false);
        }
    }//GEN-LAST:event_jCheckBox2_Orden_de_Pan_con_AjoActionPerformed

    private void jCheckBox_MitadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_MitadActionPerformed

        if (jCheckBox_Mitad.isSelected()) {
            esMitadyMitad = true;
            habilitarJCheckBoxIngredientes();
            jLista.setEnabled(false);
            maxSeleccion = 2;

        } else {
            desmarcarJCheckBoxIngredientes();
            jLista.clearSelection();
            esMitadyMitad = false;
            deshabilitarJCheckBoxIngredientes();
            jLista.setEnabled(true);
        }

    }//GEN-LAST:event_jCheckBox_MitadActionPerformed

    private void jTextField_nTELEFONOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_nTELEFONOActionPerformed

    }//GEN-LAST:event_jTextField_nTELEFONOActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        JOptionPane.showMessageDialog(null, """
                                            Sistema de pedidos de pizza
                                            Versi\u00f3n 1.2
                                            Desarrollado por Computer World
                                            \u00daltima actualizaci\u00f3n: 23 de mayo de 2025
                                            Contacto: 9811045376
                                            Página: facebook.com/cworldcamp
                                            Licencia: Uso comercial, previo autorización""",
                "Acerca de",
                JOptionPane.INFORMATION_MESSAGE
        );

    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem_preciosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_preciosActionPerformed
        GestionarPrecios ges = new GestionarPrecios();
        ges.setVisible(true);
    }//GEN-LAST:event_jMenuItem_preciosActionPerformed

    private void jCheckBox_hawaianaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_hawaianaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox_hawaianaActionPerformed

    // Método para verificar si el teléfono está en la base y obtener el ID del cliente
    private int obtenerIdClientePorTelefono(Connection con, String telefono) throws SQLException {
        String query = "SELECT idCliente FROM cliente WHERE telefono = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, telefono);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idCliente");
                }
            }
        }
        return -1; // Cliente no encontrado
    }

    // Devuelve el nombre del cliente dado su ID
    private String obtenerDireccionClientePorId(Connection con, int idcliente) throws SQLException {
        String sql = "SELECT direccion FROM cliente WHERE idCliente = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idcliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("direccion");
                } else {
                    return "";
                }
            }
        }
    }

    // Devuelve el nombre del cliente dado su ID
    private String obtenerNombreClientePorId(Connection con, int idcliente) throws SQLException {
        String sql = "SELECT nombreCliente FROM cliente WHERE idCliente = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idcliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nombreCliente");
                } else {
                    return "";
                }
            }
        }
    }

    // Método para registrar un nuevo cliente si no existe
    private int registrarNuevoCliente(Connection con, String nombre, String telefono) throws SQLException {
        String insertQuery = "INSERT INTO cliente (nombrecliente, telefono) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1; // Si falla el registro
    }

    void DeshabilitarExtras() {
        jCheckBox_Extra_Pepperoni.setEnabled(false);
        jCheckBox_Extra_jamon.setEnabled(false);
        jCheckBox_Extra_Salchicha.setEnabled(false);
        jCheckBox_Extra_Salami.setEnabled(false);
        jCheckBox_Extra_Champinon.setEnabled(false);
        jCheckBox_Extra_Pina.setEnabled(false);
        jCheckBox_Extra_Tocino.setEnabled(false);
        jCheckBox_Extra_Chorizo.setEnabled(false);
        jCheckBox_extra_pimiento.setEnabled(false);
        jCheckBox_extra_pastor.setEnabled(false);
        jCheckBox_extra_queso.setEnabled(false);
    }

    void DesmarcarExtras() {
        jCheckBox_Extra_Pepperoni.setSelected(false);
        jCheckBox_Extra_jamon.setSelected(false);
        jCheckBox_Extra_Salchicha.setSelected(false);
        jCheckBox_Extra_Salami.setSelected(false);
        jCheckBox_Extra_Champinon.setSelected(false);
        jCheckBox_Extra_Pina.setSelected(false);
        jCheckBox_Extra_Tocino.setSelected(false);
        jCheckBox_Extra_Chorizo.setSelected(false);
    }

    private void habilitarJCheckBoxSegunNombre(String nombrePizza) {
        // Desmarcar y deshabilitar todos los JCheckBox

        desmarcarJCheckBoxIngredientes();
        deshabilitarJCheckBoxIngredientes();

        switch (nombrePizza) {
            case "1 Ingrediente":
                // Habilitar sólo para 1 selección
                habilitarCheckBoxesBasicos();
                maxSeleccion = 1;
                break;

            case "2 Ingredientes":

                habilitarCheckBoxesBasicos();
                habilitarCheckBox(jCheckBox_Chorizo);
                maxSeleccion = 2;
                break;

            case "3 Ingredientes":
                habilitarCheckBoxesBasicos();
                habilitarCheckBox(jCheckBox_Chorizo);
                habilitarCheckBox(jCheckBox_Champinon);
                habilitarCheckBox(jCheckBox_Pina);
                maxSeleccion = 3;
                break;

            case "Champinon":
                jCheckBox_Champinon.setEnabled(true);
                jCheckBox_Champinon.setSelected(true);
                maxSeleccion = 1;
                break;

            case "Hawaiana":
                maxSeleccion = 1;
                jCheckBox_hawaiana.setEnabled(true);
                jCheckBox_hawaiana.setSelected(true);
                break;

            case "Carnes Frias":
                maxSeleccion = 1;
                jCheckBox_carnes_frias.setEnabled(true);
                jCheckBox_carnes_frias.setSelected(true);
                break;

            case "Campechana":
                maxSeleccion = 1;
                jCheckBox_campechana.setEnabled(true);
                jCheckBox_campechana.setSelected(true);
                break;

            case "Pastor":
                maxSeleccion = 1;
                jCheckBox_Pastor.setEnabled(true);
                jCheckBox_Pastor.setSelected(true);
                break;

            case "Pastor Especial":
                // Selección fija de ingredientes especiales
                maxSeleccion = 2; // no se permite cambiar
                jCheckBox_Pastor.setEnabled(true);
                jCheckBox_Pastor.setSelected(true);
                jCheckBox_Pina.setEnabled(true);
                jCheckBox_Pina.setSelected(true);

                break;

            case "Pizza Surtida":
                maxSeleccion = 1;
                jCheckBox_Ingredientes_Surtidos.setEnabled(true);
                jCheckBox_Ingredientes_Surtidos.setSelected(true);
                break;

            case "Carpizzio Especial":
                maxSeleccion = 1;
                jCheckBox_Ingredientes_Especiales.setEnabled(true);
                jCheckBox_Ingredientes_Especiales.setSelected(true);
                break;

            default:
                // Por si agregas más pizzas en el futuro
                break;
        }
    }

// Métodos auxiliares para mantener el código limpio:
    private void habilitarJCheckBoxIngredientes() {
        jCheckBox_jamon.setEnabled(true);
        jCheckBox_Pepperoni.setEnabled(true);
        jCheckBox_Salchicha.setEnabled(true);
        jCheckBox_Salami.setEnabled(true);
        jCheckBox_Chorizo.setEnabled(true);
        jCheckBox_Champinon.setEnabled(true);
        jCheckBox_Pina.setEnabled(true);
        jCheckBox_hawaiana.setEnabled(true);
        jCheckBox_carnes_frias.setEnabled(true);
        jCheckBox_campechana.setEnabled(true);
        jCheckBox_Pastor.setEnabled(true);
        jCheckBox_pastor_especial.setEnabled(true);
        jCheckBox_Tocino.setEnabled(true);
        jCheckBox_Pimiento.setEnabled(true);
        jCheckBox_Ingredientes_Surtidos.setEnabled(true);
        jCheckBox_Ingredientes_Especiales.setEnabled(true);

    }

    private void deshabilitarJCheckBoxIngredientes() {
        jCheckBox_jamon.setEnabled(false);
        jCheckBox_Pepperoni.setEnabled(false);
        jCheckBox_Salchicha.setEnabled(false);
        jCheckBox_Salami.setEnabled(false);
        jCheckBox_Chorizo.setEnabled(false);
        jCheckBox_Champinon.setEnabled(false);
        jCheckBox_Pina.setEnabled(false);
        jCheckBox_hawaiana.setEnabled(false);
        jCheckBox_carnes_frias.setEnabled(false);
        jCheckBox_campechana.setEnabled(false);
        jCheckBox_Pastor.setEnabled(false);
        jCheckBox_pastor_especial.setEnabled(false);
        jCheckBox_Tocino.setEnabled(false);
        jCheckBox_Pimiento.setEnabled(false);
        jCheckBox_Ingredientes_Surtidos.setEnabled(false);
        jCheckBox_Ingredientes_Especiales.setEnabled(false);
    }

    private void desmarcarJCheckBoxIngredientes() {
        permitirCambios = false; // desactiva listeners temporalmente

        jCheckBox_jamon.setSelected(false);
        jCheckBox_Pepperoni.setSelected(false);
        jCheckBox_Salchicha.setSelected(false);
        jCheckBox_Salami.setSelected(false);
        jCheckBox_Chorizo.setSelected(false);
        jCheckBox_Champinon.setSelected(false);
        jCheckBox_Pina.setSelected(false);
        jCheckBox_hawaiana.setSelected(false);
        jCheckBox_carnes_frias.setSelected(false);
        jCheckBox_campechana.setSelected(false);
        jCheckBox_Pastor.setSelected(false);
        jCheckBox_pastor_especial.setSelected(false);
        jCheckBox_Tocino.setSelected(false);
        jCheckBox_Pimiento.setSelected(false);
        jCheckBox_Ingredientes_Surtidos.setSelected(false);
        jCheckBox_Ingredientes_Especiales.setSelected(false);
        permitirCambios = true; // vuelve a activar los listeners
    }

    private void habilitarCheckBoxesBasicos() {
        habilitarCheckBox(jCheckBox_jamon);
        habilitarCheckBox(jCheckBox_Pepperoni);
        habilitarCheckBox(jCheckBox_Salchicha);
        habilitarCheckBox(jCheckBox_Salami);
    }

    private void habilitarCheckBox(JCheckBox cb) {
        cb.setEnabled(true);
    }

    private int contarSeleccionados() {
        int c = 0;
        for (JCheckBox cb : Arrays.asList(
                jCheckBox_jamon, jCheckBox_Pepperoni, jCheckBox_Salchicha,
                jCheckBox_Salami, jCheckBox_Chorizo, jCheckBox_Champinon,
                jCheckBox_Pina, jCheckBox_hawaiana, jCheckBox_carnes_frias,
                jCheckBox_campechana, jCheckBox_Pastor,
                jCheckBox_Ingredientes_Surtidos, jCheckBox_Ingredientes_Especiales, jCheckBox_Pimiento, jCheckBox_Tocino, jCheckBox_pastor_especial
        )) {
            if (cb.isSelected()) {
                c++;
            }
        }
        return c;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup GrupoTamaño;
    private javax.swing.JTextArea Jtext_direccion;
    private javax.swing.JTextField Jtext_nombre;
    private javax.swing.JButton jButton_Cancerlar_Pedido;
    private javax.swing.JButton jButton_MARCAR_ENTREGADO;
    private javax.swing.JButton jButton_calcular;
    private javax.swing.JButton jButton_levantar_pedido;
    private javax.swing.JCheckBox jCheckBox1_Orilla_de_Queso;
    private javax.swing.JCheckBox jCheckBox2_Orden_de_Pan_con_Ajo;
    private javax.swing.JCheckBox jCheckBox3_Ingrediente_Extra;
    private javax.swing.JCheckBox jCheckBox4_Refresco;
    private javax.swing.JCheckBox jCheckBox_Champinon;
    private javax.swing.JCheckBox jCheckBox_Chorizo;
    private javax.swing.JCheckBox jCheckBox_Extra_Champinon;
    private javax.swing.JCheckBox jCheckBox_Extra_Chorizo;
    private javax.swing.JCheckBox jCheckBox_Extra_Pepperoni;
    private javax.swing.JCheckBox jCheckBox_Extra_Pina;
    private javax.swing.JCheckBox jCheckBox_Extra_Salami;
    private javax.swing.JCheckBox jCheckBox_Extra_Salchicha;
    private javax.swing.JCheckBox jCheckBox_Extra_Tocino;
    private javax.swing.JCheckBox jCheckBox_Extra_jamon;
    private javax.swing.JCheckBox jCheckBox_Ingredientes_Especiales;
    private javax.swing.JCheckBox jCheckBox_Ingredientes_Surtidos;
    private javax.swing.JCheckBox jCheckBox_Mitad;
    private javax.swing.JCheckBox jCheckBox_Pastor;
    private javax.swing.JCheckBox jCheckBox_Pepperoni;
    private javax.swing.JCheckBox jCheckBox_Pimiento;
    private javax.swing.JCheckBox jCheckBox_Pina;
    private javax.swing.JCheckBox jCheckBox_Salami;
    private javax.swing.JCheckBox jCheckBox_Salchicha;
    private javax.swing.JCheckBox jCheckBox_Tocino;
    private javax.swing.JCheckBox jCheckBox_campechana;
    private javax.swing.JCheckBox jCheckBox_carnes_frias;
    private javax.swing.JCheckBox jCheckBox_extra_pastor;
    private javax.swing.JCheckBox jCheckBox_extra_pimiento;
    private javax.swing.JCheckBox jCheckBox_extra_queso;
    private javax.swing.JCheckBox jCheckBox_hawaiana;
    private javax.swing.JCheckBox jCheckBox_jamon;
    private javax.swing.JCheckBox jCheckBox_pastor_especial;
    private com.toedter.calendar.JDateChooser jDateChooser_fecha;
    private com.toedter.calendar.JDateChooser jDateChooser_fecha1;
    private com.toedter.calendar.JDateChooser jDateChooser_fecha2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel_hora;
    private javax.swing.JList<String> jLista;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem_precios;
    private javax.swing.JMenuItem jMenuItem_reporte;
    private javax.swing.JMenuItem jMenuItem_usuarios;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioButton1_Chica;
    private javax.swing.JRadioButton jRadioButton2_Grande;
    private javax.swing.JRadioButton jRadioButton3_Familiar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSpinner jSpinner_cantidad;
    private javax.swing.JSpinner jSpinner_cantidad_ajo;
    private javax.swing.JSpinner jSpinner_refrescos;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTable jTable_pedidos_del_dia;
    private javax.swing.JTextField jTextField_cambio;
    private javax.swing.JTextField jTextField_efectivo;
    private javax.swing.JTextField jTextField_nTELEFONO;
    private javax.swing.JTextField jTextField_total;
    private javax.swing.JTextPane jTextPane_Datos;
    private javax.swing.JTextPane jTextPane_pedido;
    // End of variables declaration//GEN-END:variables

    private void configurarRendererMultilinea() {
        TextAreaRenderer renderer = new TextAreaRenderer();
        TableColumnModel tcm = jTable_pedidos_del_dia.getColumnModel();
        // Ajusta el índice de columna según corresponda (2 = Items Pedido)
        tcm.getColumn(2).setCellRenderer(renderer);
    }

    public void cargarRegistrosDelDia() {
        // 1) Prepara el modelo
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID Pedido");
        model.addColumn("Cliente");
        model.addColumn("Items Pedido");
        model.addColumn("Total");
        model.addColumn("Estado");

        String sql
                = "SELECT "
                + "  v.id_pedido, "
                + "  p.cliente, "
                + "  GROUP_CONCAT( "
                + "    CONCAT( "
                + "      v.nombre_pizza, "
                + "      ' (x', v.cantidad_pizzas, "
                + "      ' ', v.tamanio_pizza, "
                + "    ')' "
                + "  ) SEPARATOR ', ' "
                + "  ) AS items_pedido, "
                + "  SUM(v.total_venta) AS total_por_pedido, "
                + "  p.estado "
                + "FROM ventas v "
                + "JOIN pedido p ON v.id_pedido = p.id_pedido "
                + "WHERE DATE(v.fecha_venta) = CURDATE() "
                + "GROUP BY v.id_pedido, p.cliente, p.estado "
                + "ORDER BY v.id_pedido DESC;";  // <-- Ordenar por ID descendente

        try (
                Connection conexion = new GestionPizzasConexion().Conectar(); Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql);) {
            while (rs.next()) {
                Object[] fila = new Object[5];
                fila[0] = rs.getInt("id_pedido");
                fila[1] = rs.getString("cliente");
                fila[2] = rs.getString("items_pedido");
                fila[3] = rs.getDouble("total_por_pedido");
                fila[4] = rs.getString("estado");
                model.addRow(fila);
            }

            // 2) Asigna el modelo y configura el sorter
            jTable_pedidos_del_dia.setModel(model);
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

            // 3) Comparator para que "Entregado" compare siempre mayor (y vaya al final)
            sorter.setComparator(4, (String s1, String s2) -> {
                boolean e1 = "Entregado".equals(s1);
                boolean e2 = "Entregado".equals(s2);
                if (e1 && !e2) {
                    return 1;
                }
                if (!e1 && e2) {
                    return -1;
                }
                return s1.compareToIgnoreCase(s2);
            });

            // 4) Ordenar por ID Pedido descendente
            List<RowSorter.SortKey> sortKeys = new ArrayList<>();
            sortKeys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));  // índice 0 = ID Pedido
            sorter.setSortKeys(sortKeys);
            sorter.sort();  // fuerza la ordenación inmediata

            jTable_pedidos_del_dia.setRowSorter(sorter);

            // 5) Renderer para colorear solo la columna Estado
            jTable_pedidos_del_dia.getColumnModel().getColumn(4)
                    .setCellRenderer(new DefaultTableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(JTable table,
                                Object value, boolean isSelected,
                                boolean hasFocus, int row, int column) {
                            Component c = super.getTableCellRendererComponent(
                                    table, value, isSelected, hasFocus, row, column);
                            if (value != null) {
                                String estado = value.toString();
                                if ("Entregado".equals(estado)) {
                                    c.setForeground(Color.RED);
                                } else if ("Activo".equals(estado)) {
                                    c.setForeground(Color.GREEN.darker());
                                } else {
                                    c.setForeground(Color.BLACK);
                                }
                            }
                            return c;
                        }
                    });

            // 6) Renderer multilinea para otras columnas si se necesita
            configurarRendererMultilinea();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error al cargar registros del día:\n" + ex.getMessage(),
                    "Error de Base de Datos",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Renderer para celdas multilínea y ajuste automático de altura
    private class TextAreaRenderer extends JTextArea implements TableCellRenderer {

        public TextAreaRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            // Ajusta tamaño para calcular altura necesaria
            setSize(table.getColumnModel().getColumn(column).getWidth(), Short.MAX_VALUE);
            int height = getPreferredSize().height;
            if (table.getRowHeight(row) != height) {
                table.setRowHeight(row, height);
            }
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            return this;
        }
    }

    private void mostrarDatosSeleccionados(int idPedido) {
        try {
            // Obtener la fila seleccionada
            int filaSeleccionada = jTable_pedidos_del_dia.getSelectedRow();

            // Obtener el ID de venta de la fila seleccionada
            int idVenta = Integer.parseInt(jTable_pedidos_del_dia.getValueAt(filaSeleccionada, 0).toString());  // Supongo que el ID de venta está en la primera columna

            // Obtener los detalles del pedido mediante una consulta a la base de datos
            String[] detallesPedido = obtenerDetallesPedido(idPedido);

            // Obtener la dirección del cliente
            String nombreCliente = jTable_pedidos_del_dia.getValueAt(filaSeleccionada, 1).toString();  // Supongo que el nombre del cliente está en la segunda columna

            // Construir el texto para mostrar en jTextPane_pedido y jTextPane_Datos
            // Obtener datos del cliente
            String[] detallesCliente = obtenerDatosCliente(nombreCliente);

            // Mostrar datos en jTextPane_Datos
            if (detallesCliente != null && detallesCliente.length == 3) {
                String textoDatosCliente = "<html>"
                        + "<b>Nombre Cliente:</b> " + detallesCliente[1] + "<br>"
                        + "<b>Teléfono:</b> " + detallesCliente[2] + "<br>"
                        + "<b>Dirección:</b> " + detallesCliente[0] + "<br>"
                        + "</html>";

                jTextPane_Datos.setContentType("text/html");
                jTextPane_Datos.setText(textoDatosCliente);
            } else {
                JOptionPane.showMessageDialog(null, "No se encontraron datos para el cliente: " + nombreCliente, "Aviso", JOptionPane.WARNING_MESSAGE);
            }

            if (detallesPedido[1].equals("Entregado")) {
                jLabel_hora.setText("");
                if (timerActivo) {
                    // Si ya hay un temporizador activo, detén el temporizador existente
                    timer.stop();
                    timerActivo = false;
                }
            } else {
                // Mostrar el temporizador
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    LocalTime horaPedido = LocalTime.parse(detallesPedido[0], formatter);

                    if (timerActivo) {
                        // Si ya hay un temporizador activo, detén el temporizador existente
                        timer.stop();
                    }

                    // Crear un nuevo temporizador
                    timer = new javax.swing.Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // Obtener la hora actual
                            LocalTime horaActual = LocalTime.now();

                            // Calcular la diferencia de tiempo en minutos y segundos
                            long minutosTranscurridos = ChronoUnit.MINUTES.between(horaPedido, horaActual);
                            long segundosTranscurridos = ChronoUnit.SECONDS.between(horaPedido, horaActual) % 60;

                            // Actualizar el texto del JLabel
                            jLabel_hora.setText("Minutos transcurridos: " + String.format("%02d:%02d", minutosTranscurridos, segundosTranscurridos));

                        }
                    });

                    // Iniciar el temporizador
                    timer.start();
                    timerActivo = true;
                } catch (DateTimeParseException ex) {
                    // Manejar la excepción de análisis de fecha y hora
                    ex.printStackTrace(); // Imprime la traza de la excepción para ayudar en la depuración
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al mostrar datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String[] obtenerDetallesPedido(int idPedido) {
        String[] detallesPedido = new String[2];
        Connection conexion = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            // 1) Establecer la conexión
            conexion = new GestionPizzasConexion().Conectar();
            if (conexion == null) {
                throw new SQLException("No se pudo establecer conexión a la base de datos.");
            }

            // 2) Consulta filtrando por ambos ID
            String consulta = "SELECT hora_pedido, estado FROM ventas WHERE id_pedido = ?";

            pst = conexion.prepareStatement(consulta);
            pst.setInt(1, idPedido);

            rs = pst.executeQuery();

            // 3) Rellenar el arreglo si hay resultado
            if (rs.next()) {

                detallesPedido[0] = rs.getString("hora_pedido");
                detallesPedido[1] = rs.getString("estado");

            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error al obtener detalles del pedido: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            // 4) Cerrar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (pst != null) {
                    pst.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (conexion != null) {
                    conexion.close();
                }
            } catch (SQLException ignored) {
            }
        }

        return detallesPedido;
    }

    private String[] obtenerDatosCliente(String nombreCliente) {
        String[] datosCliente = new String[3]; // Un arreglo para almacenar dirección, nombreCliente y telefono

        try {
            // Establecer la conexión
            Connection conexion = new GestionPizzasConexion().Conectar();

            // Consultar datos del cliente
            String consulta = "SELECT direccion, nombreCliente, telefono FROM Cliente WHERE nombreCliente = ?";
            PreparedStatement pst = conexion.prepareStatement(consulta);
            pst.setString(1, nombreCliente);
            ResultSet rs = pst.executeQuery();

            // Obtener datos y guardarlos en el arreglo
            if (rs.next()) {
                datosCliente[0] = rs.getString("direccion");
                datosCliente[1] = rs.getString("nombreCliente");
                datosCliente[2] = rs.getString("telefono");
            }

            // Cerrar recursos
            rs.close();
            pst.close();
            conexion.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al obtener datos del cliente: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        return datosCliente;
    }

    public void mostrarGrafica() {
        jPanel7.removeAll(); // Limpia cualquier componente anterior

        JFreeChart chart = crearGrafica();
        ChartPanel chartPanel = new ChartPanel(chart);

        // Establecer el color de fondo del panel de trazado (puedes ajustarlo según tus preferencias)
        chartPanel.setBackground(Color.WHITE);

        jPanel7.setLayout(new java.awt.BorderLayout());
        jPanel7.add(chartPanel, java.awt.BorderLayout.CENTER);
        jPanel7.validate();
    }

    private JFreeChart crearGrafica() {
        DefaultCategoryDataset dataset = crearDataset();

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Ventas de los últimos 7 días",
                "Días",
                "Cantidad de Ventas",
                dataset,
                PlotOrientation.VERTICAL,
                true, // incluir leyenda
                true, // mostrar tooltips
                false // URLs
        );

        // Personalizar el color de fondo del panel de trazado
        CategoryPlot plot = (CategoryPlot) lineChart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);

        // Personalizar el estilo de la línea
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesStroke(0, new BasicStroke(3.0f)); // Grosor de la línea

        return lineChart;
    }

    private DefaultCategoryDataset crearDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Connection conexion = new GestionPizzasConexion().Conectar();
        String consulta = "SELECT fecha_venta, COUNT(*) as cantidad FROM Ventas "
                + "WHERE fecha_venta BETWEEN ? AND ? AND estado = 'Entregado' "
                + "GROUP BY fecha_venta ORDER BY fecha_venta ASC LIMIT 7";

        try (PreparedStatement pst = conexion.prepareStatement(consulta)) {
            // Obtener la fecha de hoy
            Date fechaHoy = new Date();

            // Configurar el rango de fechas para los últimos 7 días
            Calendar cal = Calendar.getInstance();
            cal.setTime(fechaHoy);
            cal.add(Calendar.DATE, -6); // Retroceder 6 días
            Date fechaInicio = cal.getTime();

            pst.setDate(1, new java.sql.Date(fechaInicio.getTime()));
            pst.setDate(2, new java.sql.Date(fechaHoy.getTime()));

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                // Formatear la fecha para mostrarla en la gráfica (puedes personalizarlo según tus necesidades)
                String fecha = rs.getDate("fecha_venta").toString();

                // Agregar la cantidad de ventas al dataset
                dataset.addValue(rs.getInt("cantidad"), "Ventas", fecha);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al obtener datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        return dataset;
    }

    public void mostrarFechaEnLabel() {
        try {
            // Obtener la fecha actual
            Date fechaActual = new Date();

            // Formatear la fecha como string
            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
            String fechaFormateada = formatoFecha.format(fechaActual);

            Date getDate = new Date(); // Fecha actual
            jDateChooser_fecha.setDate(getDate);
            jDateChooser_fecha1.setDate(getDate);
            jDateChooser_fecha2.setDate(getDate);

        } catch (Exception e) {
            // Manejar cualquier error que pueda ocurrir al obtener la fecha
            e.printStackTrace();  // Puedes cambiar esto por un JOptionPane si lo prefieres
        }
    }

}
