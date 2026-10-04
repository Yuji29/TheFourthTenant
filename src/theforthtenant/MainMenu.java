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
        javax.swing.JButton[] menuButtons = { startButton, howToPlayButton, optionsButton, exitButton };

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
        creditsButton.setFont(getCustomFont(12f));       // smaller font
        creditsButton.setForeground(java.awt.Color.GRAY);
        creditsButton.setOpaque(false);
        creditsButton.setContentAreaFilled(false);
        creditsButton.setBorderPainted(false);
        creditsButton.setFocusPainted(false);
        creditsButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Brighten to white on hover.
        creditsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                creditsButton.setForeground(java.awt.Color.WHITE);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                creditsButton.setForeground(java.awt.Color.GRAY);
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
                startButton.requestFocusInWindow();
            }
        });

        // ---- Default selection: START button ----
        startButton.requestFocusInWindow();
        highlightButton(startButton, true);

        // ---- Arrow-key navigation ----
        // Bind DOWN and UP arrows on each button.
        for (javax.swing.JButton btn : menuButtons) {
            btn.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(
                javax.swing.KeyStroke.getKeyStroke("DOWN"), "down");
            btn.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(
                javax.swing.KeyStroke.getKeyStroke("UP"), "up");
        }

        // DOWN cycles forward through the buttons (wraps around).
        startButton.getActionMap().put("down", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                playSound("/audio/sfx/ui/hover.wav", false);
                howToPlayButton.requestFocusInWindow();
            }
        });
        howToPlayButton.getActionMap().put("down", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                playSound("/audio/sfx/ui/hover.wav", false);
                optionsButton.requestFocusInWindow();
            }
        });
        optionsButton.getActionMap().put("down", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                playSound("/audio/sfx/ui/hover.wav", false);
                exitButton.requestFocusInWindow();
            }
        });
        exitButton.getActionMap().put("down", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                playSound("/audio/sfx/ui/hover.wav", false);
                startButton.requestFocusInWindow();
            }
        });

        // UP cycles backward through the buttons (wraps around).
        startButton.getActionMap().put("up", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                playSound("/audio/sfx/ui/hover.wav", false);
                exitButton.requestFocusInWindow();
            }
        });
        howToPlayButton.getActionMap().put("up", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                playSound("/audio/sfx/ui/hover.wav", false);
                startButton.requestFocusInWindow();
            }
        });
        optionsButton.getActionMap().put("up", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                playSound("/audio/sfx/ui/hover.wav", false);
                howToPlayButton.requestFocusInWindow();
            }
        });
        exitButton.getActionMap().put("up", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                playSound("/audio/sfx/ui/hover.wav", false);
                optionsButton.requestFocusInWindow();
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

        contentPanel = new javax.swing.JPanel();
        startButton = new javax.swing.JButton();
        howToPlayButton = new javax.swing.JButton();
        optionsButton = new javax.swing.JButton();
        exitButton = new javax.swing.JButton();
        creditsButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("The Fourth Tenant");
        setResizable(false);

        contentPanel.setPreferredSize(new java.awt.Dimension(1150, 680));
        contentPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        startButton.setText("START");
        startButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        startButton.addActionListener(this::startButtonActionPerformed);
        contentPanel.add(startButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 360, 360, 50));

        howToPlayButton.setText("HOW TO PLAY");
        howToPlayButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        howToPlayButton.addActionListener(this::howToPlayButtonActionPerformed);
        contentPanel.add(howToPlayButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 420, 360, 50));

        optionsButton.setText("OPTIONS");
        optionsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        optionsButton.addActionListener(this::optionsButtonActionPerformed);
        contentPanel.add(optionsButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 480, 360, 50));

        exitButton.setText("EXIT");
        exitButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        exitButton.addActionListener(this::exitButtonActionPerformed);
        contentPanel.add(exitButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 540, 360, 50));

        creditsButton.setText("CREDITS");
        creditsButton.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        creditsButton.addActionListener(this::creditsButtonActionPerformed);
        contentPanel.add(creditsButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 0, 240, 40));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/common/background.gif"))); // NOI18N
        jLabel1.setText("jLabel1");
        contentPanel.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(contentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
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
    }//GEN-LAST:event_startButtonActionPerformed

    private void howToPlayButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_howToPlayButtonActionPerformed
        playSound("/audio/sfx/ui/select.wav", false);
        System.out.println("HOW TO PLAY button clicked!");
        // Later: Code to open a how to play window goes here
    }//GEN-LAST:event_howToPlayButtonActionPerformed

    private void optionsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsButtonActionPerformed
        playSound("/audio/sfx/ui/select.wav", false);
        System.out.println("OPTIONS button clicked!");
        optionsMenu.show();
    }//GEN-LAST:event_optionsButtonActionPerformed

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
        playSound("/audio/sfx/ui/select.wav", false);
        AudioCache.closeAll();
        System.exit(0); 
    }//GEN-LAST:event_exitButtonActionPerformed

    private void creditsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_creditsButtonActionPerformed
        playSound("/audio/sfx/ui/select.wav", false);
        System.out.println("CREDITS button clicked!");
        // Later: Code to open a credits window goes here
    }//GEN-LAST:event_creditsButtonActionPerformed

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
    private javax.swing.JPanel contentPanel;
    private javax.swing.JButton creditsButton;
    private javax.swing.JButton exitButton;
    private javax.swing.JButton howToPlayButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton optionsButton;
    private javax.swing.JButton startButton;
    // End of variables declaration//GEN-END:variables
}
