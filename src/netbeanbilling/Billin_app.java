/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netbeanbilling;

import java.util.Date;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author vasan
 */
public class Billin_app extends javax.swing.JFrame {

    /**
     * Creates new form Billin_app
     */
    
    public Billin_app() {
        initComponents();
        setExtendedState(MAXIMIZED_BOTH);
        setUndecorated(true);
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        StyledDocument doc = jTextPane1.getStyledDocument();
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        jTextPane1.setText("Store Name\n"+new Date()+"\nProduct Id  Product Name  Price  Cost\n\n");
        connectToDatabase();
//        Bill.setVisible(false);
//        Customer.setVisible(false);
//        History.setVisible(false);
        
    }
    private float total = 0;
    private float wallet = 0;
    private Connection connection;

    // Connect to the MySQL database
    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/billing_system", "root", "0221" // update with your username and password
            );
            JOptionPane.showMessageDialog(null, "Connected to database.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Get product details based on product ID and quantity
    private String getProduct(int id, int cnt) {
        String sql = "SELECT product_name, selling_price FROM products WHERE product_id = ?";
        float price = 0, tprice = 0;
        String name = "";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                price = rs.getFloat("selling_price");
                name = rs.getString("product_name");
                tprice = price * cnt;
                total += tprice;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error in getting product data: " + e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return id + "   " + name + "   " + price + "   " + tprice + "\n";
    }

    // Get customer data and insert new customer if not found
    private String getData(int id) {
        String sql = "SELECT customer_name, wallet_amount FROM customers WHERE customer_id = ?";
        String name = "";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                wallet = rs.getFloat("wallet_amount");
                name = rs.getString("customer_name");
            } else {
                // Insert new customer if not found
                sql = "INSERT INTO customers (customer_id, customer_name, wallet_amount) VALUES (?, ?, 0)";
                try (PreparedStatement insertStmt = connection.prepareStatement(sql)) {
                    insertStmt.setInt(1, id);
                    insertStmt.setString(2, "CustomerNameHere"); // Replace with input text for name
                    insertStmt.execute();
                    JOptionPane.showMessageDialog(null, "New customer inserted.");
                    name = "CustomerNameHere"; // Replace with the input text
                    wallet = 0;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error in getting or inserting customer data: " + e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return id + "\t" + name + "\nWallet Amount: " + wallet+"\n";
    }

    // Method to update product details
    private void updateProduct(int productId, String productName, double costPrice, double sellingPrice) {
        String sql = "UPDATE products SET product_name = ?, cost_price = ?, selling_price = ? WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, productName);
            stmt.setDouble(2, costPrice);
            stmt.setDouble(3, sellingPrice);
            stmt.setInt(4, productId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "Product updated successfully.");
            } else {
                JOptionPane.showMessageDialog(null, "Product ID not found.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error updating product: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to update customer billing details
    private void updateCustomer(int customerId, float billAmount, String paymentStatus, float walletAmount) {
        String sql = "UPDATE customers SET bill_amount = ?, payment_status = ?, wallet_amount = ? WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setFloat(1, billAmount);
            stmt.setString(2, paymentStatus);
            stmt.setFloat(3, walletAmount);
            stmt.setInt(4, customerId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "Customer updated successfully.");
            } else {
                JOptionPane.showMessageDialog(null, "Customer ID not found.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error updating customer: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to apply updates after printing
    private void update() {
        int customerId = Integer.parseInt("CustomerIDHere"); // Replace with actual input for customer ID
        updateCustomer(customerId, total, "Paid", wallet + (total / 100));
    }

    // Print bill to JTextArea and update customer if successful
    private void printBill(String details) {
        try { // Initialize JTextArea for example
            StyledDocument doc = jTextPane1.getStyledDocument();

        // Center alignment attribute set
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        try {
            doc.insertString(doc.getLength(), details, center);
            doc.insertString(doc.getLength(), "=================\nTotal\t\t"+total, center);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
            boolean complete = jTextPane1.print();  // Triggers print dialog
            if (complete) {
                update(); // Update customer wallet and billing if print is successful
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Printing failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        Bill1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        Bill6 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        pid5 = new javax.swing.JTextField();
        Pcnt5 = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        dct5 = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        Pcnt6 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        dct6 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();

        jLabel5.setText("jLabel2");

        jTextField4.setText("jTextField1");

        jTextField5.setText("jTextField1");

        jLabel6.setText("jLabel2");

        jTextField6.setText("jTextField1");

        jLabel7.setText("jLabel2");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(jTable1);

        javax.swing.GroupLayout Bill1Layout = new javax.swing.GroupLayout(Bill1);
        Bill1.setLayout(Bill1Layout);
        Bill1Layout.setHorizontalGroup(
            Bill1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Bill1Layout.createSequentialGroup()
                .addGap(301, 301, 301)
                .addComponent(jLabel5)
                .addGap(60, 60, 60)
                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(350, 350, 350)
                .addComponent(jLabel7)
                .addGap(60, 60, 60)
                .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 545, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addGap(60, 60, 60)
                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(229, 229, 229))
            .addGroup(Bill1Layout.createSequentialGroup()
                .addComponent(jScrollPane2)
                .addContainerGap())
        );
        Bill1Layout.setVerticalGroup(
            Bill1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Bill1Layout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addGroup(Bill1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(142, 142, 142)
                .addComponent(jScrollPane2))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(1366, 768));
        setMinimumSize(new java.awt.Dimension(1366, 768));
        setUndecorated(true);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("BILLING APP");
        jLabel1.setPreferredSize(new java.awt.Dimension(1900, 16));

        jLabel25.setText("Product Id");

        pid5.setPreferredSize(new java.awt.Dimension(100, 22));

        Pcnt5.setPreferredSize(new java.awt.Dimension(100, 22));
        Pcnt5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PcntActionPerformed(evt);
            }
        });

        jLabel26.setText("Product count");

        dct5.setPreferredSize(new java.awt.Dimension(100, 22));

        jLabel27.setText("Discount");

        jLabel28.setFont(new java.awt.Font("Tahoma", 0, 48)); // NOI18N
        jLabel28.setText("INVOICE");

        addButton.setText("PRINT");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        jButton2.setText("Exit");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("ADD");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        Pcnt6.setPreferredSize(new java.awt.Dimension(100, 22));
        Pcnt6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Pcnt6PcntActionPerformed(evt);
            }
        });

        jLabel2.setText("Customer Id");

        jLabel3.setText("Customer Name");

        dct6.setPreferredSize(new java.awt.Dimension(100, 22));

        jScrollPane1.setViewportView(jTextPane1);

        javax.swing.GroupLayout Bill6Layout = new javax.swing.GroupLayout(Bill6);
        Bill6.setLayout(Bill6Layout);
        Bill6Layout.setHorizontalGroup(
            Bill6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Bill6Layout.createSequentialGroup()
                .addGroup(Bill6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Bill6Layout.createSequentialGroup()
                        .addGap(566, 566, 566)
                        .addComponent(jLabel28))
                    .addGroup(Bill6Layout.createSequentialGroup()
                        .addGap(169, 169, 169)
                        .addGroup(Bill6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(Bill6Layout.createSequentialGroup()
                                .addComponent(addButton)
                                .addGap(423, 423, 423)
                                .addComponent(jButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton3))
                            .addGroup(Bill6Layout.createSequentialGroup()
                                .addComponent(jLabel25)
                                .addGap(50, 50, 50)
                                .addComponent(pid5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(174, 174, 174)
                                .addComponent(jLabel26)
                                .addGap(50, 50, 50)
                                .addComponent(Pcnt5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(174, 174, 174)
                                .addComponent(jLabel27)
                                .addGap(50, 50, 50)
                                .addComponent(dct5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(Bill6Layout.createSequentialGroup()
                                .addGap(91, 91, 91)
                                .addComponent(jLabel2)
                                .addGap(60, 60, 60)
                                .addComponent(Pcnt6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel3)
                                .addGap(33, 33, 33)
                                .addComponent(dct6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(170, 170, 170))
                            .addComponent(jScrollPane1))))
                .addContainerGap(170, Short.MAX_VALUE))
        );
        Bill6Layout.setVerticalGroup(
            Bill6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Bill6Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(Bill6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Pcnt6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(dct6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(Bill6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Bill6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel27)
                        .addComponent(dct5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(Bill6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel26)
                        .addComponent(Pcnt5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(Bill6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel25)
                        .addComponent(pid5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel28)
                .addGap(9, 9, 9)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 587, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(Bill6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addGap(11, 11, 11))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(Bill6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Bill6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void PcntActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PcntActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_PcntActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        StyledDocument doc = jTextPane1.getStyledDocument();

        // Center alignment attribute set
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        try {
            doc.insertString(doc.getLength(), getProduct(Integer.parseInt(pid5.getText()), Integer.parseInt(Pcnt5.getText()))+"\n", center);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // TODO add your handling code here:
        printBill(getData(Integer.parseInt(Pcnt6.getText())));
    }//GEN-LAST:event_addButtonActionPerformed

    private void Pcnt6PcntActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Pcnt6PcntActionPerformed
        // TODO add your handling code here:m
        
        
    }//GEN-LAST:event_Pcnt6PcntActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Billin_app.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Billin_app.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Billin_app.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Billin_app.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Billin_app().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Bill1;
    private javax.swing.JPanel Bill6;
    private javax.swing.JTextField Pcnt5;
    private javax.swing.JTextField Pcnt6;
    private javax.swing.JButton addButton;
    private javax.swing.JTextField dct5;
    private javax.swing.JTextField dct6;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextField pid5;
    // End of variables declaration//GEN-END:variables
}
