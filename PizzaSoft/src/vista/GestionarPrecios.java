package vista;

import Conexion.GestionPizzasConexion;
import java.sql.*;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 *
 * @author ascen
 */
public class GestionarPrecios extends javax.swing.JFrame {

    private Connection conexion;

    private DefaultTableModel dtm;
    private Object[] obj = new Object[4];
    private int filaSeleccionada;

    /**
     * Creates new form GestionarClientes
     */
    public GestionarPrecios() {
        initComponents();
        cargarTablaCompletaPizzas();
        cargarTablaCompletaComplementos();
        setLocationRelativeTo(null);
        cargarNombresPizzas();
        cargarNombresComplementos();
        ImageIcon iconoApp = new ImageIcon(getClass().getResource("/img/footer.png"));
        setIconImage(iconoApp.getImage());
        this.setLocationRelativeTo(null);
    }

    // Método para cargar la tabla de clientes
    public void cargarTablaCompletaPizzas() {
        GestionPizzasConexion con = new GestionPizzasConexion();
        conexion = con.Conectar();
        DefaultTableModel modeloTabla = new DefaultTableModel();

        // Configurar las columnas de la tabla
        modeloTabla.addColumn("id");
        modeloTabla.addColumn("nombre");
        modeloTabla.addColumn("Precio chica");
        modeloTabla.addColumn("Precio grande");
        modeloTabla.addColumn("Precio familiar");

        // Consulta SQL para obtener los datos de los clientes
        String sql = "SELECT id, nombre,  precio_chica, precio_grande, precio_familiar FROM pizzas";

        try {
            Statement statement = conexion.createStatement();
            ResultSet resultado = statement.executeQuery(sql);

            // Llenar el modelo de la tabla con los datos de la consulta
            while (resultado.next()) {
                Object[] fila = new Object[5];
                fila[0] = resultado.getInt("id");
                fila[1] = resultado.getString("nombre");
                fila[2] = resultado.getInt("precio_chica");
                fila[3] = resultado.getInt("precio_grande");
                fila[4] = resultado.getInt("precio_familiar");

                modeloTabla.addRow(fila);
            }

            // Asignar el modelo de la tabla
            jTable_clientes.setModel(modeloTabla);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar la tabla de pizzas: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void cargarTablaCompletaComplementos() {
        GestionPizzasConexion con = new GestionPizzasConexion();
        conexion = con.Conectar();
        DefaultTableModel modeloTabla = new DefaultTableModel();

        // Configurar las columnas de la tabla
        modeloTabla.addColumn("id");
        modeloTabla.addColumn("nombre");
        modeloTabla.addColumn("Precio chica");
        modeloTabla.addColumn("Precio grande");
        modeloTabla.addColumn("Precio familiar");

        // Consulta SQL para obtener los datos de los clientes
        String sql = "SELECT id, nombre,  precio_chica, precio_grande, precio_familiar FROM complementos";

        try {
            Statement statement = conexion.createStatement();
            ResultSet resultado = statement.executeQuery(sql);

            // Llenar el modelo de la tabla con los datos de la consulta
            while (resultado.next()) {
                Object[] fila = new Object[5];
                fila[0] = resultado.getInt("id");
                fila[1] = resultado.getString("nombre");
                fila[2] = resultado.getInt("precio_chica");
                fila[3] = resultado.getInt("precio_grande");
                fila[4] = resultado.getInt("precio_familiar");

                modeloTabla.addRow(fila);
            }

            // Asignar el modelo de la tabla
            jTable_complementos.setModel(modeloTabla);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar la tabla de complementos: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Nuevo método para obtener los nombres de los clientes
   
    
        
    public void cargarNombresComplementos() {
        try {
            GestionPizzasConexion con = new GestionPizzasConexion();
            conexion = con.Conectar();
            String consulta = "SELECT nombre FROM complementos";
            Statement statement = conexion.createStatement();
            ResultSet resultSet = statement.executeQuery(consulta);

            // Limpiar el JComboBox
            jComboBox_complementos.removeAllItems();

            // Agregar la opción por defecto
            jComboBox_complementos.addItem("Seleccione complementos");

            // Agregar los nombres de los clientes al JComboBox
            while (resultSet.next()) {
                String nombreCliente = resultSet.getString("nombre");
                jComboBox_complementos.addItem(nombreCliente);
            }

            // Cerrar recursos
            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar nombres de complementos: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        
    }
    }


    
    public void cargarNombresPizzas() {
        try {
            GestionPizzasConexion con = new GestionPizzasConexion();
            conexion = con.Conectar();
            String consulta = "SELECT nombre FROM pizzas";
            Statement statement = conexion.createStatement();
            ResultSet resultSet = statement.executeQuery(consulta);

            // Limpiar el JComboBox
            jComboBox_cliente.removeAllItems();

            // Agregar la opción por defecto
            jComboBox_cliente.addItem("Seleccione pizza");

            // Agregar los nombres de los clientes al JComboBox
            while (resultSet.next()) {
                String nombreCliente = resultSet.getString("nombre");
                jComboBox_cliente.addItem(nombreCliente);
            }

            // Cerrar recursos
            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar nombres de pizzas: " + e, "Error", JOptionPane.ERROR_MESSAGE);
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable_clientes = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jComboBox_cliente = new javax.swing.JComboBox<>();
        jButton_eliminar = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable_complementos = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jComboBox_complementos = new javax.swing.JComboBox<>();
        jButton_eliminar1 = new javax.swing.JButton();

        setTitle("Actualizar precios");
        setFocusable(false);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTabbedPane1.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N

        jPanel1.setBackground(new java.awt.Color(229, 242, 211));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable_clientes.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jTable_clientes.setModel(new javax.swing.table.DefaultTableModel(
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
        jTable_clientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable_clientesMouseClicked(evt);
            }
        });
        jTable_clientes.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTable_clientesPropertyChange(evt);
            }
        });
        jScrollPane1.setViewportView(jTable_clientes);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 720, 460));

        jLabel2.setFont(new java.awt.Font("Poppins", 1, 24)); // NOI18N
        jLabel2.setText("Actualizar precios pizzas");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filtrar", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Poppins", 0, 14))); // NOI18N
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jButton1.setText("Buscar");
        jButton1.setBorderPainted(false);
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 60, -1, -1));

        jComboBox_cliente.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jComboBox_cliente.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_cliente.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox_clienteItemStateChanged(evt);
            }
        });
        jComboBox_cliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_clienteActionPerformed(evt);
            }
        });
        jPanel2.add(jComboBox_cliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 24, 190, 30));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 60, 230, 110));

        jButton_eliminar.setBackground(new java.awt.Color(255, 153, 153));
        jButton_eliminar.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jButton_eliminar.setText("Eliminar");
        jButton_eliminar.setBorderPainted(false);
        jButton_eliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_eliminarActionPerformed(evt);
            }
        });
        jPanel1.add(jButton_eliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 180, 90, 30));

        jTabbedPane1.addTab("Pizzas", jPanel1);

        jPanel3.setBackground(new java.awt.Color(229, 242, 211));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable_complementos.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jTable_complementos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTable_complementos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable_complementosMouseClicked(evt);
            }
        });
        jTable_complementos.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTable_complementosPropertyChange(evt);
            }
        });
        jScrollPane2.setViewportView(jTable_complementos);

        jPanel3.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 720, 460));

        jLabel3.setFont(new java.awt.Font("Poppins", 1, 24)); // NOI18N
        jLabel3.setText("Actualizar precios complementos");
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filtrar", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Poppins", 0, 14))); // NOI18N
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton2.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jButton2.setText("Buscar");
        jButton2.setBorderPainted(false);
        jButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 60, -1, -1));

        jComboBox_complementos.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jComboBox_complementos.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_complementos.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox_complementosItemStateChanged(evt);
            }
        });
        jComboBox_complementos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_complementosActionPerformed(evt);
            }
        });
        jPanel4.add(jComboBox_complementos, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 24, 240, 30));

        jPanel3.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 60, 270, 110));

        jButton_eliminar1.setBackground(new java.awt.Color(255, 153, 153));
        jButton_eliminar1.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        jButton_eliminar1.setText("Eliminar");
        jButton_eliminar1.setBorderPainted(false);
        jButton_eliminar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_eliminar1ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton_eliminar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 180, 90, 30));

        jTabbedPane1.addTab("Complementos", jPanel3);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1040, 580));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (jComboBox_cliente.getSelectedIndex() == 0) {
            return;
        } else {
            cargarTablaPizzaFiltrada();
        }


    }//GEN-LAST:event_jButton1ActionPerformed

    private void jComboBox_clienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_clienteActionPerformed

    }//GEN-LAST:event_jComboBox_clienteActionPerformed

    private void jComboBox_clienteItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox_clienteItemStateChanged

    }//GEN-LAST:event_jComboBox_clienteItemStateChanged

    private void jTable_clientesPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTable_clientesPropertyChange

        // Sólo nos interesa cuando se termina la edición de una celda
        if ("tableCellEditor".equals(evt.getPropertyName())) {
            if (!jTable_clientes.isEditing()) {
                // Obtenemos fila y columna editadas
                int fila = jTable_clientes.getSelectedRow();
                int columna = jTable_clientes.getSelectedColumn();

                // Obtenemos el modelo y el valor nuevo
                TableModel model = jTable_clientes.getModel();
                Object nuevoValor = model.getValueAt(fila, columna);

                // Obtenemos el idCliente de la primera columna (asumiendo que está en la columna 0)
                Object idClienteObj = model.getValueAt(fila, 0);
                if (idClienteObj == null) {
                    JOptionPane.showMessageDialog(this, "No se encontró el ID de la pizza.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int idCliente = Integer.parseInt(idClienteObj.toString());

                // Determinamos el nombre de columna en la base de datos según el índice de la tabla
                String columnaBD;
                switch (columna) {
                    case 1:
                        columnaBD = "nombre";
                        break;
                    case 2:
                        columnaBD = "precio_chica";
                        break;
                    case 3:
                        columnaBD = "precio_grande";
                        break;
                    case 4:
                        columnaBD = "precio_familiar";
                        break;
                    default:
                        // Si es la columna 0 (id) o fuera de rango, no hacemos nada
                        return;
                }

                // Armamos y ejecutamos el UPDATE
                String sql = "UPDATE pizzas SET " + columnaBD + " = ? WHERE id = ?";
                try (PreparedStatement pst = conexion.prepareStatement(sql)) {
                    pst.setObject(1, nuevoValor);
                    pst.setInt(2, idCliente);
                    int afectados = pst.executeUpdate();

                    if (afectados > 0) {
                        System.out.println("Pizza #" + idCliente + " actualizado: "
                                + columnaBD + " = " + nuevoValor);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "No se pudo actualizar la pizza en la base de datos.",
                                "Error de actualización", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error al actualizar la base de datos:\n" + ex.getMessage(),
                            "SQL Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }

    }//GEN-LAST:event_jTable_clientesPropertyChange

    private void jTable_clientesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable_clientesMouseClicked


    }//GEN-LAST:event_jTable_clientesMouseClicked

    private void jButton_eliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_eliminarActionPerformed
        GestionPizzasConexion con = new GestionPizzasConexion();
        conexion = con.Conectar();
        // 1) Obtenemos la fila seleccionada
        int fila = jTable_clientes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Por favor selecciona una pizza para eliminar.",
                    "Sin selección",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2) Recuperamos el idCliente de la columna 0
        TableModel model = jTable_clientes.getModel();
        Object idObj = model.getValueAt(fila, 0);
        int idCliente = Integer.parseInt(idObj.toString());

        // 3) (Opcional) Confirmación
        int confirma = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de eliminar la pizza?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirma != JOptionPane.YES_OPTION) {
            return;
        }

        // 4) Aseguramos que la conexión esté abierta
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = con.Conectar();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo reabrir la conexión:\n" + ex.getMessage(),
                    "Error de conexión",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 5) Ejecutamos el DELETE
        String sql = "DELETE FROM pizzas WHERE id = ?";
        try (PreparedStatement pst = conexion.prepareStatement(sql)) {
            pst.setInt(1, idCliente);
            int afectados = pst.executeUpdate();
            if (afectados > 0) {
                // 6) Eliminamos la fila del modelo de la tabla
                DefaultTableModel dtm = (DefaultTableModel) jTable_clientes.getModel();
                dtm.removeRow(fila);
                JOptionPane.showMessageDialog(this,
                        "Pizza eliminada correctamente.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se encontró la pizza en la base de datos.",
                        "Error al eliminar",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al eliminar en la base de datos:\n" + ex.getMessage(),
                    "SQL Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }


    }//GEN-LAST:event_jButton_eliminarActionPerformed

    private void jTable_complementosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable_complementosMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jTable_complementosMouseClicked

    private void jTable_complementosPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTable_complementosPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jTable_complementosPropertyChange

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
           if (jComboBox_complementos.getSelectedIndex() == 0) {
            return;
        } else {
            cargarTablaComplementosFiltrada();
        }

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jComboBox_complementosItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox_complementosItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox_complementosItemStateChanged

    private void jComboBox_complementosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_complementosActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox_complementosActionPerformed

    private void jButton_eliminar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_eliminar1ActionPerformed

        GestionPizzasConexion con = new GestionPizzasConexion();
        conexion = con.Conectar();
        // 1) Obtenemos la fila seleccionada
        int fila = jTable_complementos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Por favor selecciona un complemento para eliminar.",
                    "Sin selección",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2) Recuperamos el idCliente de la columna 0
        TableModel model = jTable_complementos.getModel();
        Object idObj = model.getValueAt(fila, 0);
        int idCliente = Integer.parseInt(idObj.toString());

        // 3) (Opcional) Confirmación
        int confirma = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de eliminar complemento?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirma != JOptionPane.YES_OPTION) {
            return;
        }

        // 4) Aseguramos que la conexión esté abierta
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = con.Conectar();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo reabrir la conexión:\n" + ex.getMessage(),
                    "Error de conexión",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 5) Ejecutamos el DELETE
        String sql = "DELETE FROM complementos WHERE id = ?";
        try (PreparedStatement pst = conexion.prepareStatement(sql)) {
            pst.setInt(1, idCliente);
            int afectados = pst.executeUpdate();
            if (afectados > 0) {
                // 6) Eliminamos la fila del modelo de la tabla
                DefaultTableModel dtm = (DefaultTableModel) jTable_complementos.getModel();
                dtm.removeRow(fila);
                JOptionPane.showMessageDialog(this,
                        "Complemento eliminado correctamente.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se encontró el complemento en la base de datos.",
                        "Error al eliminar",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al eliminar en la base de datos:\n" + ex.getMessage(),
                    "SQL Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }


    }//GEN-LAST:event_jButton_eliminar1ActionPerformed

    // Método para cargar la tabla de clientes filtrada por nombre
    public void cargarTablaPizzaFiltrada() {
        GestionPizzasConexion con = new GestionPizzasConexion();
        conexion = con.Conectar();
        DefaultTableModel modeloTabla = new DefaultTableModel();

        // Configurar las columnas de la tabla
        modeloTabla.addColumn("id");
        modeloTabla.addColumn("nombre");
        modeloTabla.addColumn("precio_chica");
        modeloTabla.addColumn("precio_grande");
        modeloTabla.addColumn("precio_familiar");

        // Consulta SQL para obtener los datos de los clientes filtrados por nombre
        String nombreSeleccionado = jComboBox_cliente.getSelectedItem().toString();
        String sql = "SELECT id, nombre, precio_chica, precio_grande, precio_familiar FROM pizzas WHERE nombre = ?";

        try {
            PreparedStatement preparedStatement = conexion.prepareStatement(sql);
            preparedStatement.setString(1, nombreSeleccionado);
            ResultSet resultado = preparedStatement.executeQuery();

            // Llenar el modelo de la tabla con los datos de la consulta
            while (resultado.next()) {
                Object[] fila = new Object[5];
                fila[0] = resultado.getInt("ID");
                fila[1] = resultado.getString("nombre");
                fila[2] = resultado.getInt("precio_chica");
                fila[3] = resultado.getInt("precio_grande");
                fila[4] = resultado.getInt("precio_familiar");

                modeloTabla.addRow(fila);
            }

            jTable_clientes.setModel(modeloTabla);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar la tabla pizza filtrada: " + e, "Error", JOptionPane.ERROR_MESSAGE);

        }
    }
    public void cargarTablaComplementosFiltrada() {
        GestionPizzasConexion con = new GestionPizzasConexion();
        conexion = con.Conectar();
        DefaultTableModel modeloTabla = new DefaultTableModel();

        // Configurar las columnas de la tabla
        modeloTabla.addColumn("id");
        modeloTabla.addColumn("nombre");
        modeloTabla.addColumn("precio_chica");
        modeloTabla.addColumn("precio_grande");
        modeloTabla.addColumn("precio_familiar");

        // Consulta SQL para obtener los datos de los clientes filtrados por nombre
        String nombreSeleccionado = jComboBox_cliente.getSelectedItem().toString();
        String sql = "SELECT id, nombre, precio_chica, precio_grande, precio_familiar FROM Complementos WHERE nombre = ?";

        try {
            PreparedStatement preparedStatement = conexion.prepareStatement(sql);
            preparedStatement.setString(1, nombreSeleccionado);
            ResultSet resultado = preparedStatement.executeQuery();

            // Llenar el modelo de la tabla con los datos de la consulta
            while (resultado.next()) {
                Object[] fila = new Object[5];
                fila[0] = resultado.getInt("ID");
                fila[1] = resultado.getString("nombre");
                fila[2] = resultado.getInt("precio_chica");
                fila[3] = resultado.getInt("precio_grande");
                fila[4] = resultado.getInt("precio_familiar");

                modeloTabla.addRow(fila);
            }

            jTable_clientes.setModel(modeloTabla);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar la tabla complementos filtrada: " + e, "Error", JOptionPane.ERROR_MESSAGE);

        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton_eliminar;
    private javax.swing.JButton jButton_eliminar1;
    private javax.swing.JComboBox<String> jComboBox_cliente;
    private javax.swing.JComboBox<String> jComboBox_complementos;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable_clientes;
    private javax.swing.JTable jTable_complementos;
    // End of variables declaration//GEN-END:variables
}
