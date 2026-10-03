/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package theforthtenant;

/**
 * The main menu frame: displays the title screen buttons, handles keyboard
 * and mouse navigation, plays menu music, and hosts the options overlay.
 *
 * @author yuji
 */
public class MainMenu extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(MainMenu.class.getName());

    // ---- Audio ----
    private javax.sound.sampled.Clip musicClip;

    // ---- UI ----
    private OptionsMenu optionsMenu;

    /**
     * Creates new form MainMenu.
     */
    public MainMenu() {
        initComponents();

        // ---- Window icon ----
        try {
            java.awt.Image icon = javax.imageio.ImageIO.read(
                getClass().getResourceAsStream("/Images/common/logo.png"));
            setIconImage(icon);
        } catch (Exception e) {
            System.out.println("Logo not found: " + e.getMessage());
        }

        // ---- Menu music (looping) ----
        musicClip = AudioCache.loop("/audio/music/menu.wav", AudioCache.Channel.MUSIC);

        // ---- Style the four main menu buttons ----
        java.awt.Font customFont = getCustomFont(25f);
        javax.swing.JButton[] menuButtons = { jButton1, jButton2, jButton3, jButton4 };

        for (javax.swing.JButton btn : menuButtons) {
            btn.setFont(customFont);

            // Default (unselected) state: gray text, transparent background.
            btn.setForeground(java.awt.Color.GRAY);
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            // Highlight when focused (keyboard navigation).
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

            // Play hover sound + highlight when mouse enters.
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    playSound("/audio/sfx/ui/hover.wav", false);
                    highlightButton(btn, true);
                    btn.requestFocusInWindow(); // so arrow keys work from here
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    // Only un-highlight if this button doesn't have focus.
                    if (!btn.hasFocus()) {
                        highlightButton(btn, false);
                    }
                }
            });
        }

        // ---- Style the credits button (jButton5) ----
        jButton5.setFont(getCustomFont(12f));       // smaller font
        jButton5.setForeground(java.awt.Color.GRAY);
        jButton5.setOpaque(false);
        jButton5.setContentAreaFilled(false);
        jButton5.setBorderPainted(false);
        jButton5.setFocusPainted(false);
        jButton5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Brighten to white on hover.
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

        // ---- Options menu ----
        optionsMenu = OptionsMenu.attachTo(this, new OptionsMenu.Callbacks() {

            @Override public void onMusicChanged(int percent) {
                applyMusicVolume(percent);
            }

            @Override public void onSoundChanged(int percent) {
                // SfxManager is already updated by OptionsMenu itself — just play a blip.
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }

            @Override public void onClose() {
                jButton1.requestFocusInWindow();
            }
        });

        // ---- Default selection: START button ----
        jButton1.requestFocusInWindow();
        highlightButton(jButton1, true);

        // ---- Arrow-key navigation ----
        // Bind DOWN and UP arrows on each button.
        for (javax.swing.JButton btn : menuButtons) {
            btn.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(
                javax.swing.KeyStroke.getKeyStroke("DOWN"), "down");
            btn.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(
                javax.swing.KeyStroke.getKeyStroke("UP"), "up");
        }

        // DOWN cycles forward through the buttons (wraps around).
        jButton1.getActionMap().put("down", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                playSound("/audio/sfx/ui/hover.wav", false);
                jButton2.requestFocusInWindow();
            }
        });
        jButton2.getActionMap().put("down", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                playSound("/audio/sfx/ui/hover.wav", false);
                jButton3.requestFocusInWindow();
            }
        });
        jButton3.getActionMap().put("down", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                playSound("/audio/sfx/ui/hover.wav", false);
                jButton4.requestFocusInWindow();
            }
        });
        jButton4.getActionMap().put("down", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                playSound("/audio/sfx/ui/hover.wav", false);
                jButton1.requestFocusInWindow();
            }
        });

        // UP cycles backward through the buttons (wraps around).
        jButton1.getActionMap().put("up", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                playSound("/audio/sfx/ui/hover.wav", false);
                jButton4.requestFocusInWindow();
            }
        });
        jButton2.getActionMap().put("up", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                playSound("/audio/sfx/ui/hover.wav", false);
                jButton1.requestFocusInWindow();
            }
        });
        jButton3.getActionMap().put("up", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                playSound("/audio/sfx/ui/hover.wav", false);
                jButton2.requestFocusInWindow();
            }
        });
        jButton4.getActionMap().put("up", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                playSound("/audio/sfx/ui/hover.wav", false);
                jButton3.requestFocusInWindow();
            }
        });

        // ---- ENTER activates the focused button ----
        javax.swing.AbstractAction enterAction = new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // Find which button currently has focus and click it.
                java.awt.Component focused = java.awt.KeyboardFocusManager
                        .getCurrentKeyboardFocusManager().getFocusOwner();
                if (focused instanceof javax.swing.JButton) {
                    ((javax.swing.JButton) focused).doClick();
                }
            }
        };

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
        setTitle("The Fourth Tenant");
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
        jPanel1.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 0, 240, 40));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/common/background.gif"))); // NOI18N
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
         // Don't play click / kill music if a transition is already playing
        if (TransitionOverlay.isPlaying()) return;

        playSound("/audio/sfx/ui/select.wav", false);
        System.out.println("START button clicked!");
        
        // Reset session state — new playthrough.
        GameState.reset();

        // Stop the menu music
        if (musicClip != null) {
            musicClip.stop();
            musicClip = null;
        }

        TransitionOverlay.play(this, () -> {
            Backstory bs = new Backstory();
            bs.show();
            bs.setLocation(getLocation());
            bs.setVisible(true);
            dispose();
        });    
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        playSound("/audio/sfx/ui/select.wav", false);
        System.out.println("HOW TO PLAY button clicked!");
        // Later: Code to open a how to play window goes here
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        playSound("/audio/sfx/ui/select.wav", false);
        System.out.println("OPTIONS button clicked!");
        optionsMenu.show();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        playSound("/audio/sfx/ui/select.wav", false);
        AudioCache.closeAll();
        System.exit(0); 
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        playSound("/audio/sfx/ui/select.wav", false);
        System.out.println("CREDITS button clicked!");
        // Later: Code to open a credits window goes here
    }//GEN-LAST:event_jButton5ActionPerformed

    /**
     * @param args the command line arguments
     */
    
    /** Loads the custom pixel font at the given size, or falls back to Segoe UI. */
    private java.awt.Font getCustomFont(float size) {
        try {
            // Load the font file from the resource folder.
            java.io.InputStream is = getClass().getResourceAsStream("/fonts/press_start_2p.ttf");
            java.awt.Font baseFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
            return baseFont.deriveFont(size);
        } catch (Exception e) {
            System.out.println("Font not found! Using default.");
            return new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, (int) size);
        }
    }

    /**
     * Plays a sound effect or music track.
     *
     * @param filepath resource path of the audio file
     * @param loop     {@code true} to loop continuously, {@code false} for one-shot
     * @return the playing clip, or {@code null} if the sound could not be loaded
     */
    private javax.sound.sampled.Clip playSound(String filepath, boolean loop) {
        return loop ? AudioCache.loop(filepath)
                    : AudioCache.play(filepath);
    }

    /** Highlights or un-highlights a menu button. */
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

    /** Applies the given music volume (0–100) to the audio cache. */
    private void applyMusicVolume(int percent) {
        AudioCache.setMusicVolume(percent);
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
        java.awt.EventQueue.invokeLater(() -> new LoadingScreen().setVisible(true));
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
