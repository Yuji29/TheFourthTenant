/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package theforthtenant;

/**
 *
 * @author yuji
 */
public class MainMenu extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainMenu.class.getName());

    /**
     * Creates new form MainMenu
     */
    public MainMenu() {
        initComponents();
        
        // Play the menu music on loop
        playSound("/audio/music.wav", true);
        
        // 1. Load the custom font
        java.awt.Font customFont = getCustomFont(25f);
        
        // 2. Create an array of ALL FOUR buttons
        javax.swing.JButton[] menuButtons = { jButton1, jButton2, jButton3, jButton4 };
        
        // 3. Loop through them and apply the styling
        for (javax.swing.JButton btn : menuButtons) {
            btn.setFont(customFont);
            
            // Default (unselected) state: Gray text, transparent background
            btn.setForeground(java.awt.Color.GRAY);
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false); 
            btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            
            // Highlight when focused (keyboard navigation)
            btn.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent evt) {
                    highlightButton(btn, true);
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent evt) {
                    highlightButton(btn, false);
                }
            });
            
            // Play hover sound + highlight when mouse enters
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    playSound("/audio/hover.wav", false);
                    highlightButton(btn, true);
                    btn.requestFocusInWindow(); // so arrow keys work from here
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    // Only un-highlight if this button doesn't have focus
                    if (!btn.hasFocus()) {
                        highlightButton(btn, false);
                    }
                }
            });
        }
        
        // ===== STYLE THE CREDITS BUTTON (jButton5) =====
        jButton5.setFont(getCustomFont(12f)); // Smaller font
        jButton5.setForeground(java.awt.Color.GRAY); // Gray text
        jButton5.setOpaque(false);
        jButton5.setContentAreaFilled(false);
        jButton5.setBorderPainted(false);
        jButton5.setFocusPainted(false);
        jButton5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        // Make the credits button highlight gray when hovered (optional, but nice)
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton5.setForeground(java.awt.Color.WHITE);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButton5.setForeground(java.awt.Color.GRAY);
            }
        });
        
        // 5. Set the first button (START) to be highlighted when the game launches
        jButton1.requestFocusInWindow();
        
        // Manually highlight START as the default selection
        highlightButton(jButton1, true);
        
        // 6. Make arrow keys navigate the menu
        // Bind DOWN arrow
        jButton1.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(
            javax.swing.KeyStroke.getKeyStroke("DOWN"), "down");
        jButton2.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(
            javax.swing.KeyStroke.getKeyStroke("DOWN"), "down");
        jButton3.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(
            javax.swing.KeyStroke.getKeyStroke("DOWN"), "down");
        jButton4.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(
            javax.swing.KeyStroke.getKeyStroke("DOWN"), "down");

        // Bind UP arrow
        jButton1.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(
            javax.swing.KeyStroke.getKeyStroke("UP"), "up");
        jButton2.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(
            javax.swing.KeyStroke.getKeyStroke("UP"), "up");
        jButton3.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(
            javax.swing.KeyStroke.getKeyStroke("UP"), "up");
        jButton4.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(
            javax.swing.KeyStroke.getKeyStroke("UP"), "up");

        // DOWN actions
        jButton1.getActionMap().put("down", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { 
                playSound("/audio/hover.wav", false);
                jButton2.requestFocusInWindow(); 
            }
        });
        jButton2.getActionMap().put("down", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { 
                playSound("/audio/hover.wav", false);
                jButton3.requestFocusInWindow(); 
            }
        });
        jButton3.getActionMap().put("down", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { 
                playSound("/audio/hover.wav", false);
                jButton4.requestFocusInWindow(); 
            }
        });
        jButton4.getActionMap().put("down", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { 
                playSound("/audio/hover.wav", false);
                jButton1.requestFocusInWindow(); 
            }
        });

        // UP actions
        jButton1.getActionMap().put("up", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { 
                playSound("/audio/hover.wav", false);
                jButton4.requestFocusInWindow(); 
            }
        });
        jButton2.getActionMap().put("up", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { 
                playSound("/audio/hover.wav", false);
                jButton1.requestFocusInWindow(); 
            }
        });
        jButton3.getActionMap().put("up", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { 
                playSound("/audio/hover.wav", false);
                jButton2.requestFocusInWindow(); 
            }
        });
        jButton4.getActionMap().put("up", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { 
                playSound("/audio/hover.wav", false);
                jButton3.requestFocusInWindow(); 
            }
        });
        
        // 7. Make the ENTER key trigger the focused button
        javax.swing.AbstractAction enterAction = new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // Find which button currently has focus and click it
                java.awt.Component focused = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (focused instanceof javax.swing.JButton) {
                    ((javax.swing.JButton) focused).doClick();
                }
            }
        };

        // Bind the ENTER key to every button
        for (javax.swing.JButton btn : menuButtons) {
            btn.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(
                javax.swing.KeyStroke.getKeyStroke("ENTER"), "enter");
            btn.getActionMap().put("enter", enterAction);
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
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setPreferredSize(new java.awt.Dimension(1150, 680));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("START");
        jButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 360, 360, 50));

        jButton2.setText("HOW TO PLAY");
        jButton2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 420, 360, 50));

        jButton3.setText("OPTIONS");
        jButton3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton3.addActionListener(this::jButton3ActionPerformed);
        jPanel1.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 480, 360, 50));

        jButton4.setText("EXIT");
        jButton4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton4.addActionListener(this::jButton4ActionPerformed);
        jPanel1.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 540, 360, 50));

        jButton5.setText("CREDITS");
        jButton5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jButton5.addActionListener(this::jButton5ActionPerformed);
        jPanel1.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 640, 240, 40));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/background.gif"))); // NOI18N
        jLabel1.setText("jLabel1");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        playSound("/audio/select.wav", false);
        System.out.println("START button clicked!");
        // Later: Code to open the game screen goes here
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        playSound("/audio/select.wav", false);
        System.out.println("HOW TO PLAY button clicked!");
        // Later: Code to open a how to play window goes here
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        playSound("/audio/select.wav", false);
        System.out.println("OPTIONS button clicked!");
        // Later: Code to open a options window goes here
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        playSound("/audio/select.wav", false);
        System.exit(0); // Closes the game
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        playSound("/audio/select.wav", false);
        System.out.println("CREDITS button clicked!");
        // Later: Code to open a credits window goes here
    }//GEN-LAST:event_jButton5ActionPerformed

    /**
     * @param args the command line arguments
     */
    
    // This method loads your custom font from inside the project
    private java.awt.Font getCustomFont(float size) {
        try {
            // Load the font file (Make sure PressStart2P-Regular.ttf is in your 'theforthtenant' package)
            java.io.InputStream is = getClass().getResourceAsStream("/fonts/press_start_2p.ttf");
            java.awt.Font baseFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
            // Return the font with the size you want
            return baseFont.deriveFont(size);
        } catch (Exception e) {
            System.out.println("Font not found! Using default.");
            return new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, (int)size);
        }
    }
    
    // This method loads and plays a sound file
    private void playSound(String filepath, boolean loop) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream(filepath);
            javax.sound.sampled.AudioInputStream audioStream = 
                javax.sound.sampled.AudioSystem.getAudioInputStream(is);
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            clip.open(audioStream);
            if (loop) {
                clip.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.start();
            }
        } catch (Exception e) {
            System.out.println("Audio not found: " + filepath);
        }
    }
    
    // Helper method to highlight or un-highlight a button  <-- NEW METHOD STARTS HERE
    private void highlightButton(javax.swing.JButton btn, boolean highlight) {
        if (highlight) {
            btn.setOpaque(true);
            btn.setContentAreaFilled(true);
            btn.setBackground(new java.awt.Color(80, 80, 60, 200));
            btn.setForeground(java.awt.Color.WHITE);
        } else {
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setForeground(java.awt.Color.GRAY);
        }
    }
    
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
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new MainMenu().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
