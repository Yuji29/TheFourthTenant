/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package theforthtenant;


import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 *
 * @author yuji
 */
public class Interrogate extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Interrogate.class.getName());
    
    
    // ---- Reusable overlays ----
    private PauseMenu pauseMenu;
    private OptionsMenu optionsMenu;
    private javax.swing.JComponent activeTooltip;
    private javax.sound.sampled.Clip humClip;
    private javax.sound.sampled.Clip rainClip;
    
    // ---- Interrogation Scene State ----
    private javax.swing.ImageIcon loungeBgIcon;
    private int currentSuspect = -1; // -1 = Lounge, 0 = Cardo, 1 = Cathy, 2 = Andy
    
    // Suspect images
    private javax.swing.ImageIcon cardoIdle, cardoTalk;
    private javax.swing.ImageIcon cathyIdle, cathyTalk;
    private javax.swing.ImageIcon andyIdle, andyTalk;

    /** Metadata for one suspect. */
    private static final class Suspect {
        final String id;
        final String displayName;
        final String portraitPath;
        final String hoverPath;
        final int hoverDx;
        final int hoverDy;

        Suspect(String id, String name, String normal, String hover,
                int hoverDx, int hoverDy) {
            this.id = id;
            this.displayName = name;
            this.portraitPath = normal;
            this.hoverPath = hover;
            this.hoverDx = hoverDx;
            this.hoverDy = hoverDy;
        }
    }

    /** The three tenants, in display order (left → middle → right). */
    private final Suspect[] suspects = {
        new Suspect("cardo", "CARDO",
            "/Images/gameplay/interrogate/suspects/cardo.png",
            "/Images/gameplay/interrogate/suspects/cardo_hovered.png",
            0, 0),
        new Suspect("cathy", "CATHY",
            "/Images/gameplay/interrogate/suspects/cathy.png",
            "/Images/gameplay/interrogate/suspects/cathy_hovered.png",
            0, 0),
        new Suspect("andy", "ANDY",
            "/Images/gameplay/interrogate/suspects/andy.png",
            "/Images/gameplay/interrogate/suspects/andy_hovered.png",
            0, 0),
    };

    /**
     * Creates new form Interrogate
     */
        public Interrogate() {
        initComponents();

        // ---- Window icon ----
        try {
            java.awt.Image icon = javax.imageio.ImageIO.read(
                getClass().getResourceAsStream("/Images/common/logo.png"));
            setIconImage(icon);
        } catch (Exception e) {
            System.out.println("Logo not found: " + e.getMessage());
        }

        setTitle("The Fourth Tenant");
        setResizable(false);
        setLocationRelativeTo(null);

        // ---- Load Backgrounds ----
        loungeBgIcon = loadIcon("/Images/gameplay/interrogate/lounge_bg.png");
        
        // Set initial background
        backgroundLabel.setIcon(loungeBgIcon);

        // ---- Load Suspects (Idle & Talk) ----
        cardoIdle = loadIcon("/Images/gameplay/interrogate/interrogation/cardo_idle.png");
        cardoTalk = loadIcon("/Images/gameplay/interrogate/interrogation/cardo_talk.png");
        
        cathyIdle = loadIcon("/Images/gameplay/interrogate/interrogation/cathy_idle.png");
        cathyTalk = loadIcon("/Images/gameplay/interrogate/interrogation/cathy_talk.png");
        
        andyIdle  = loadIcon("/Images/gameplay/interrogate/interrogation/andy_idle.png");
        andyTalk  = loadIcon("/Images/gameplay/interrogate/interrogation/andy_talk.png");
        
        // ---- Ambience: indoor hum + rain outside ----
        humClip  = AudioCache.loop("/audio/sfx/ambience/indoor_hum.wav");
        rainClip = AudioCache.loop("/audio/sfx/ambience/rain_loop.wav");

                // ---- Wire up the three suspect portraits ----
        JLabel[] portraitLabels = { cardoButton, cathyButton, andyButton };
        for (int i = 0; i < suspects.length; i++) {
            final Suspect s = suspects[i];
            final JLabel lbl = portraitLabels[i];
            final int suspectIndex = i; // Need this to pass into the listener

            final ImageIcon normal = loadIcon(s.portraitPath);
            final ImageIcon hover  = (s.hoverPath != null)
                    ? loadIcon(s.hoverPath) : normal;

            if (normal != null) lbl.setIcon(normal);
            lbl.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            lbl.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (hover != null) lbl.setIcon(hover);
                    lbl.setLocation(lbl.getX() + s.hoverDx,
                                    lbl.getY() + s.hoverDy);
                    showTooltip(lbl, "/Images/gameplay/interrogate/tooltip_investigate.png");
                    AudioCache.play("/audio/sfx/ui/hover.wav");
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    if (normal != null) lbl.setIcon(normal);
                    lbl.setLocation(lbl.getX() - s.hoverDx,
                                    lbl.getY() - s.hoverDy);
                    hideTooltip();
                }
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (TransitionOverlay.isPlaying()) return;
                    if (pauseMenu != null && pauseMenu.isPaused()) return;

                    AudioCache.play("/audio/sfx/gameplay/door_close.wav");
                    
                    // CALL THE TRANSITION METHOD HERE
                    startInterrogation(suspectIndex); 
                }
            });
        }

        // ---- Back button ----
        final ImageIcon backNormal = new ImageIcon(
                getClass().getResource("/Images/ui/back_button.png"));
        final ImageIcon backHover = new ImageIcon(
                darken(backNormal.getImage()));

        backButton.setIcon(backNormal);
        backButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                backButton.setIcon(backHover);
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                backButton.setIcon(backNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (currentSuspect >= 0) {
                    // We are in a conversation, just go back to the lounge
                    AudioCache.play("/audio/sfx/ui/select.wav");
                    returnToLounge();
                } else {
                    // We are in the lounge, exit completely
                    closeInterrogate();
                }
            }
        });

        // ---- ESC also backs out ----
         javax.swing.JRootPane root = getRootPane();
        
        // Create the action once
        javax.swing.AbstractAction backAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (currentSuspect >= 0) {
                    returnToLounge();
                } else {
                    closeInterrogate();
                }
            }
        };

        // Bind ESC
        root.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "backAction");
        root.getActionMap().put("backAction", backAction);

        // Bind 4
        root.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_4, 0), "backAction");
        root.getActionMap().put("backAction", backAction);

        // ---- Options popup ----
        optionsMenu = OptionsMenu.attachTo(this, new OptionsMenu.Callbacks() {
            @Override public void onSoundChanged(int percent) {
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void onClose() {
                pauseMenu.showPopupOnly();
            }
        });

        // ---- Pause menu ----
        pauseMenu = PauseMenu.attachTo(this, PauseMenu.Corner.TOP_LEFT,
                new PauseMenu.Callbacks() {
            @Override public void onPause() {
                if (humClip  != null) humClip.stop();
                if (rainClip != null) rainClip.stop();
            }
            @Override public void onResume() {
                if (humClip  != null) humClip.start();
                if (rainClip != null) rainClip.start();
            }
            @Override public void onOptions() {
                pauseMenu.hidePopupOnly();
                optionsMenu.show();
            }
            @Override public void onMainMenu() {
                hideTooltip();
                if (TransitionOverlay.isPlaying()) return;
                pauseMenu.hideOverlay();

                TransitionOverlay.play(Interrogate.this, () -> {
                    MainMenu menu = new MainMenu();
                    menu.setLocation(getLocation());
                    menu.setVisible(true);
                    Interrogate.this.dispose();
                });
            }
        });
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
        backButton = new javax.swing.JLabel();
        andyButton = new javax.swing.JLabel();
        cathyButton = new javax.swing.JLabel();
        cardoButton = new javax.swing.JLabel();
        backgroundLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        contentPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ui/back_button.png"))); // NOI18N
        contentPanel.add(backButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 630, 60, 50));

        andyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/interrogate/suspects/andy.png"))); // NOI18N
        contentPanel.add(andyButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(686, 266, -1, 410));

        cathyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/interrogate/suspects/cathy.png"))); // NOI18N
        contentPanel.add(cathyButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 278, -1, 340));

        cardoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/interrogate/suspects/cardo.png"))); // NOI18N
        cardoButton.setPreferredSize(new java.awt.Dimension(237, 239));
        contentPanel.add(cardoButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 250, -1, 430));

        backgroundLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/interrogate/lounge_bg.png"))); // NOI18N
        contentPanel.add(backgroundLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1150, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    
    
    /** Opens the dialogue window for the chosen suspect. */
    private void openDialogue(Suspect s) {
        // TODO: build InterrogateDialogue and swap this stub out.
        System.out.println("Open dialogue for: " + s.id + " (" + s.displayName + ")");
    }

    /** Returns to the CrimeScene. */
    private void closeInterrogate() {
        hideTooltip();
        if (TransitionOverlay.isPlaying()) return;

        AudioCache.play("/audio/sfx/gameplay/door_close.wav");

        CrimeScene cs = new CrimeScene();
        cs.setLocation(getLocation());
        cs.setVisible(true);
        Interrogate.this.dispose();
    }
    
    @Override
    public void dispose() {
        if (humClip  != null) { humClip.stop();  humClip  = null; }
        if (rainClip != null) { rainClip.stop(); rainClip = null; }
        super.dispose();
    }

    /** Loads an icon resource, or returns null if missing. */
    private ImageIcon loadIcon(String path) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            System.out.println("Missing icon: " + path);
            return null;
        }
        return new ImageIcon(url);
    }

    /** Returns a darkened copy of the given image (for the back-arrow hover state). */
    private static java.awt.Image darken(java.awt.Image src) {
        java.awt.image.ImageFilter filter = new java.awt.image.RGBImageFilter() {
            @Override public int filterRGB(int x, int y, int rgb) {
                int a = (rgb >> 24) & 0xff;
                int r = (int)(((rgb >> 16) & 0xff) * 0.35);
                int g = (int)(((rgb >> 8)  & 0xff) * 0.35);
                int b = (int)((rgb & 0xff) * 0.35);
                return (a << 24) | (r << 16) | (g << 8) | b;
            }
        };
        return java.awt.Toolkit.getDefaultToolkit().createImage(
                new java.awt.image.FilteredImageSource(src.getSource(), filter));
    }
    
    /**
    * Shows a PNG-based tooltip above the given anchor label. The tooltip is
    * centred horizontally on the anchor and sits just above its top edge.
    *
    * @param anchor the label the tooltip points at
    * @param pngPath classpath path of the tooltip PNG
    */
   private void showTooltip(JLabel anchor, String pngPath) {
       hideTooltip();  // never stack two

       java.net.URL url = getClass().getResource(pngPath);
       if (url == null) {
           System.out.println("Tooltip PNG missing: " + pngPath);
           return;
       }
       final javax.swing.ImageIcon icon = new ImageIcon(url);

       final int TIP_OFFSET_Y = 6;   // gap between the triangle tip and the anchor

       int w = icon.getIconWidth();
       int h = icon.getIconHeight();

       int anchorCenterX = anchor.getX() + anchor.getWidth() / 2;
       int anchorTopY    = anchor.getY();

       int x = anchorCenterX - w / 2;
       int y = anchorTopY - h - TIP_OFFSET_Y;

       // Clamp inside the frame so the tooltip never gets clipped.
       if (x < 4) x = 4;
       if (x + w > contentPanel.getWidth() - 4) {
           x = contentPanel.getWidth() - w - 4;
       }
       if (y < 4) y = 4;

       final JLabel tip = new JLabel(icon);
       tip.setOpaque(false);
       tip.setBounds(x, y, w, h);

       getRootPane().getLayeredPane().add(
           tip, javax.swing.JLayeredPane.POPUP_LAYER);
       getRootPane().getLayeredPane().repaint();

       activeTooltip = tip;
   }

   /** Removes the tooltip if one is showing. */
   private void hideTooltip() {
       if (activeTooltip != null) {
           getRootPane().getLayeredPane().remove(activeTooltip);
           getRootPane().getLayeredPane().repaint();
           activeTooltip = null;
       }
   }
   
    /** Transitions from the lounge into the interrogation room. */
    private void startInterrogation(int suspectIndex) {
        hideTooltip();
        currentSuspect = suspectIndex;
        
        // 1. Hide the lounge buttons
        cardoButton.setVisible(false);
        cathyButton.setVisible(false);
        andyButton.setVisible(false);
        
        // 2. Get the correct icon
        javax.swing.ImageIcon idleIcon = null;
        if (suspectIndex == 0) idleIcon = cardoIdle;
        else if (suspectIndex == 1) idleIcon = cathyIdle;
        else if (suspectIndex == 2) idleIcon = andyIdle;

        // 3. Always play the fade transition
        if (idleIcon != null) {
            fadeInBackground(idleIcon);
        }
        
        // 4. Fix layering
        contentPanel.setComponentZOrder(backgroundLabel, contentPanel.getComponentCount() - 1);
        contentPanel.setComponentZOrder(backButton, 0);
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /** Creates a quick fade-to-black effect, swaps the background image, then fades back in. */
    private void fadeInBackground(javax.swing.ImageIcon newBg) {
        // Create our custom fading overlay
        FadeOverlay fadeOverlay = new FadeOverlay();
        
        // Add it to the screen using AbsoluteConstraints
        contentPanel.add(fadeOverlay, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1150, 680));
        contentPanel.setComponentZOrder(fadeOverlay, 0); // Bring to front
        contentPanel.revalidate();
        contentPanel.repaint();

        final int duration = 400; // Total time in ms
        final long start = System.currentTimeMillis();
        final boolean[] imageSwapped = { false }; // To ensure we only swap once

        javax.swing.Timer timer = new javax.swing.Timer(16, null);
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - start;
            float progress = Math.min(1f, elapsed / (float) duration);

            if (progress < 0.5f) {
                // First half: Fade TO black (0.0 -> 1.0)
                fadeOverlay.setAlpha(progress * 2f);
            } else {
                // Second half: Swap image and Fade FROM black (1.0 -> 0.0)
                if (!imageSwapped[0]) {
                    backgroundLabel.setIcon(newBg);
                    imageSwapped[0] = true;
                }
                fadeOverlay.setAlpha((1f - progress) * 2f);
            }

            if (progress >= 1f) {
                // Done! Remove the overlay.
                contentPanel.remove(fadeOverlay);
                contentPanel.revalidate();
                contentPanel.repaint();
                ((javax.swing.Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }
    
    /** Returns the screen to the lounge state. */
    private void returnToLounge() {
        currentSuspect = -1;
        backgroundLabel.setIcon(loungeBgIcon);
        
        // Restore the lounge suspects
        cardoButton.setVisible(true);
        cathyButton.setVisible(true);
        andyButton.setVisible(true);
        
        // Reset their images back to the lounge portraits
        cardoButton.setIcon(loadIcon("/Images/gameplay/interrogate/suspects/cardo.png"));
        cathyButton.setIcon(loadIcon("/Images/gameplay/interrogate/suspects/cathy.png"));
        andyButton.setIcon(loadIcon("/Images/gameplay/interrogate/suspects/andy.png"));
        
        // Reset their original positions
        cardoButton.setLocation(260, 258);
        cathyButton.setLocation(480, 282);
        andyButton.setLocation(686, 266);
        
        // --- THE FIX: Bring the suspects back to the front ---
        contentPanel.setComponentZOrder(cardoButton, 0);
        contentPanel.setComponentZOrder(cathyButton, 0);
        contentPanel.setComponentZOrder(andyButton, 0);
        contentPanel.setComponentZOrder(backButton, 0);
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /** Simple black overlay that fades out. */
    private static class FadeOverlay extends javax.swing.JComponent {
        private float alpha = 1f;

        FadeOverlay() {
            setOpaque(false);
        }

        void setAlpha(float a) {
            this.alpha = Math.max(0f, Math.min(1f, a));
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            if (alpha <= 0f) return;
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setColor(new java.awt.Color(0, 0, 0, (int)(alpha * 255)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
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
        java.awt.EventQueue.invokeLater(() -> new Interrogate().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel andyButton;
    private javax.swing.JLabel backButton;
    private javax.swing.JLabel backgroundLabel;
    private javax.swing.JLabel cardoButton;
    private javax.swing.JLabel cathyButton;
    private javax.swing.JPanel contentPanel;
    // End of variables declaration//GEN-END:variables
}
