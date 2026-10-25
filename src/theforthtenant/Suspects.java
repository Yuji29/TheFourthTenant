/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package theforthtenant;

/**
 *
 * @author yuji
 */
public class Suspects extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Suspects.class.getName()); 
    
    private PauseMenu pauseMenu;
    private javax.swing.JLabel[] alibiLabels = new javax.swing.JLabel[4];    
    
    // The images for each suspect's file page
    private final String[] suspectFiles = {
        "/Images/gameplay/suspects/cardo_file.png",
        "/Images/gameplay/suspects/cathy_file.png",
        "/Images/gameplay/suspects/andy_file.png"
    };
    private int currentSuspectIndex = 0; 
    
    /**
     * Creates new form suspects
     */
    public Suspects() {
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
        
        // ---- Options Menu ----
        // If you have an OptionsMenu class, you can attach it here
        final OptionsMenu optionsMenu = OptionsMenu.attachTo(this, new OptionsMenu.Callbacks() {
            @Override public void onSoundChanged(int percent) {
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void onClose() {
                pauseMenu.showPopupOnly(); // Bring back the pause menu when options close
            }
        });
        
        // Update the PauseMenu callback to open the OptionsMenu
        pauseMenu = PauseMenu.attachTo(this, PauseMenu.Corner.TOP_LEFT, new PauseMenu.Callbacks() {
            @Override public void onPause() {}
            @Override public void onResume() {}
            @Override public void onOptions() {
                pauseMenu.hidePopupOnly();
                optionsMenu.show();
            }
            @Override public void onMainMenu() {
                if (TransitionOverlay.isPlaying()) return;
                pauseMenu.hideOverlay();

                TransitionOverlay.play(Suspects.this, () -> {
                    MainMenu menu = new MainMenu();
                    menu.setLocation(getLocation());
                    menu.setVisible(true);
                    Suspects.this.dispose();
                });
            }
        });        
        
        // ---- Back Button ----
        final javax.swing.ImageIcon backNormal = new javax.swing.ImageIcon(getClass().getResource("/Images/ui/back_button.png"));
        final javax.swing.ImageIcon backHover = new javax.swing.ImageIcon(darken(backNormal.getImage()));
        
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
                AudioCache.play("/audio/sfx/ui/select.wav");
                CrimeScene cs = new CrimeScene();
                cs.setLocation(getLocation());
                cs.setVisible(true);
                Suspects.this.dispose();
            }
        });        
        
        // ---- Setup Arrows for Swapping Pages ----
        final javax.swing.ImageIcon nextNormal = new javax.swing.ImageIcon(getClass().getResource("/Images/common/next_button.png"));
        final javax.swing.ImageIcon nextHover = new javax.swing.ImageIcon(darken(nextNormal.getImage()));
        
        final javax.swing.ImageIcon prevNormal = new javax.swing.ImageIcon(getClass().getResource("/Images/common/previous_button.png"));
        final javax.swing.ImageIcon prevHover = new javax.swing.ImageIcon(darken(prevNormal.getImage()));
        
        // --- Prev Button ---
        prevButton.setIcon(prevNormal);
        prevButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        prevButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                prevButton.setIcon(prevHover);
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                prevButton.setIcon(prevNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                AudioCache.play("/audio/sfx/ui/select.wav");
                currentSuspectIndex = (currentSuspectIndex - 1 + suspectFiles.length) % suspectFiles.length;
                updateBackground();
            }
        });
        
        // --- Next Button ---
        nextButton.setIcon(nextNormal);
        nextButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        nextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                nextButton.setIcon(nextHover);
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                nextButton.setIcon(nextNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                AudioCache.play("/audio/sfx/ui/select.wav");
                currentSuspectIndex = (currentSuspectIndex + 1) % suspectFiles.length;
                updateBackground();
            }
        });       
        
        // ---- Keyboard Bindings ----
        javax.swing.JRootPane root = getRootPane();
        javax.swing.InputMap im = root.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap am = root.getActionMap();

        // 2 to go back to CrimeScene
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, 0), "backAction");
        am.put("backAction", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                AudioCache.play("/audio/sfx/ui/select.wav");
                CrimeScene cs = new CrimeScene();
                cs.setLocation(getLocation());
                cs.setVisible(true);
                Suspects.this.dispose();
            }
        });

        // A or Left to go Prev
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, 0), "prevAction");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0), "prevAction");
        am.put("prevAction", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                AudioCache.play("/audio/sfx/ui/select.wav");
                currentSuspectIndex = (currentSuspectIndex - 1 + suspectFiles.length) % suspectFiles.length;
                updateBackground();
            }
        });

        // D or Right to go Next
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, 0), "nextAction");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), "nextAction");
        am.put("nextAction", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                AudioCache.play("/audio/sfx/ui/select.wav");
                currentSuspectIndex = (currentSuspectIndex + 1) % suspectFiles.length;
                updateBackground();
            }
        });   
        
       note4Alibi1.setVisible(false);
       note4Alibi2.setVisible(false);
       note4Alibi3.setVisible(false);
       note4Alibi4.setVisible(false);
       note4Alibi1Icon.setVisible(false);
       note4Alibi2Icon.setVisible(false);
       note4Alibi3Icon.setVisible(false);
       note4Alibi4Icon.setVisible(false);
       updateBackground();       
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
        note3Alibi3Icon = new javax.swing.JLabel();
        note3Alibi3 = new javax.swing.JLabel();
        note4Alibi4Icon = new javax.swing.JLabel();
        note4Alibi3Icon = new javax.swing.JLabel();
        note4Alibi2Icon = new javax.swing.JLabel();
        note4Alibi1Icon = new javax.swing.JLabel();
        note4Alibi4 = new javax.swing.JLabel();
        note4Alibi3 = new javax.swing.JLabel();
        note4Alibi2 = new javax.swing.JLabel();
        note4Alibi1 = new javax.swing.JLabel();
        backButton = new javax.swing.JLabel();
        nextButton = new javax.swing.JLabel();
        prevButton = new javax.swing.JLabel();
        suspect_bg = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        contentPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        note3Alibi3Icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/suspects/notes/icons/ledger.png"))); // NOI18N
        contentPanel.add(note3Alibi3Icon, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 540, -1, -1));

        note3Alibi3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/suspects/notes/andy/ledger_alibi.png"))); // NOI18N
        contentPanel.add(note3Alibi3, new org.netbeans.lib.awtextra.AbsoluteConstraints(775, 420, -1, -1));

        note4Alibi4Icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/suspects/notes/icons/boots.png"))); // NOI18N
        contentPanel.add(note4Alibi4Icon, new org.netbeans.lib.awtextra.AbsoluteConstraints(745, 530, -1, -1));

        note4Alibi3Icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/suspects/notes/icons/jealousy.png"))); // NOI18N
        contentPanel.add(note4Alibi3Icon, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 530, -1, -1));

        note4Alibi2Icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/suspects/notes/icons/damage_locks.png"))); // NOI18N
        contentPanel.add(note4Alibi2Icon, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 265, -1, -1));

        note4Alibi1Icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/suspects/notes/icons/time.png"))); // NOI18N
        contentPanel.add(note4Alibi1Icon, new org.netbeans.lib.awtextra.AbsoluteConstraints(745, 265, -1, -1));

        note4Alibi4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/suspects/notes/cardo/boots_alibi.png"))); // NOI18N
        contentPanel.add(note4Alibi4, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 410, -1, -1));

        note4Alibi3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/suspects/notes/cardo/jealousy_alibi.png"))); // NOI18N
        contentPanel.add(note4Alibi3, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 410, -1, -1));

        note4Alibi2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/suspects/notes/cardo/damage_locks_alibi.png"))); // NOI18N
        contentPanel.add(note4Alibi2, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 140, -1, -1));

        note4Alibi1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/suspects/notes/cardo/time_alibi.png"))); // NOI18N
        contentPanel.add(note4Alibi1, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 140, -1, -1));

        backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ui/back_button.png"))); // NOI18N
        contentPanel.add(backButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 635, -1, -1));

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/common/next_button.png"))); // NOI18N
        contentPanel.add(nextButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1103, 320, -1, -1));

        prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/common/previous_button.png"))); // NOI18N
        contentPanel.add(prevButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 320, -1, -1));

        suspect_bg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/suspects/andy_file.png"))); // NOI18N
        contentPanel.add(suspect_bg, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

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
    
    /** Loads the custom pixel font at the given size, or falls back to Segoe UI. */
    private java.awt.Font getCustomFont(float size) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/fonts/press_start_2p.ttf");
            java.awt.Font base = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
            return base.deriveFont(size);
        } catch (Exception e) {
            System.out.println("Font not found! Using default.");
            return new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, (int) size);
        }
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
    
    /** Swaps the full-screen background image and updates the alibi notes. */
    private void updateBackground() {
        String path = suspectFiles[currentSuspectIndex];
        java.net.URL url = getClass().getResource(path);
        if (url != null) {
            suspect_bg.setIcon(new javax.swing.ImageIcon(url)); 
        } else {
            System.out.println("Missing file image: " + path);
        }

        // 1. Hide all notes and icons first
        javax.swing.JLabel[] noteLabels = {note4Alibi1, note4Alibi2, note4Alibi3, note4Alibi4, note3Alibi3};
        javax.swing.JLabel[] iconLabels = {note4Alibi1Icon, note4Alibi2Icon, note4Alibi3Icon, note4Alibi4Icon, note3Alibi3Icon};
        
        for (int i = 0; i < noteLabels.length; i++) {
            noteLabels[i].setVisible(false);
            iconLabels[i].setVisible(false);
        }

        // 2. CARDOS NOTES (Uses 4 slots: 0, 1, 2, 3)
        if (currentSuspectIndex == 0) {
            java.util.List<String> unlockedNotes = new java.util.ArrayList<>();
            java.util.List<String> unlockedIcons = new java.util.ArrayList<>();
            java.util.List<String> order = GameState.getInterrogationOrder();

            for (String topic : order) {
                if (topic.equals("cardo_time")) {
                    unlockedNotes.add("/Images/gameplay/suspects/notes/cardo/time_alibi.png");
                    unlockedIcons.add("/Images/gameplay/suspects/notes/icons/time.png");
                }
                else if (topic.equals("cardo_boots") && GameState.isDiscovered("shoeprints")) {
                    unlockedNotes.add("/Images/gameplay/suspects/notes/cardo/boots_alibi.png");
                    unlockedIcons.add("/Images/gameplay/suspects/notes/icons/boots.png");
                }
                else if (topic.equals("cardo_damage_lock") && GameState.isDiscovered("damaged_lock")) {
                    unlockedNotes.add("/Images/gameplay/suspects/notes/cardo/damage_locks_alibi.png");
                    unlockedIcons.add("/Images/gameplay/suspects/notes/icons/damage_locks.png");
                }
                else if (topic.equals("cardo_jealousy") && GameState.isDiscovered("jealousy")) {
                    unlockedNotes.add("/Images/gameplay/suspects/notes/cardo/jealousy_alibi.png");
                    unlockedIcons.add("/Images/gameplay/suspects/notes/icons/jealousy.png");
                }
            }

            for (int i = 0; i < unlockedNotes.size(); i++) {
                java.net.URL noteUrl = getClass().getResource(unlockedNotes.get(i));
                java.net.URL iconUrl = getClass().getResource(unlockedIcons.get(i));
                if (noteUrl != null && iconUrl != null) {
                    noteLabels[i].setIcon(new javax.swing.ImageIcon(noteUrl));
                    iconLabels[i].setIcon(new javax.swing.ImageIcon(iconUrl));
                    noteLabels[i].setVisible(true);
                    iconLabels[i].setVisible(true);
                    contentPanel.setComponentZOrder(noteLabels[i], 1); 
                    contentPanel.setComponentZOrder(iconLabels[i], 0); 
                }
            }
        }
        // 3. CATHY'S NOTES (Uses 4 slots: 0, 1, 2, 3)
        else if (currentSuspectIndex == 1) {
            java.util.List<String> unlockedNotes = new java.util.ArrayList<>();
            java.util.List<String> unlockedIcons = new java.util.ArrayList<>();
            java.util.List<String> order = GameState.getInterrogationOrder();

            for (String topic : order) {
                if (topic.equals("cathy_time")) {
                    unlockedNotes.add("/Images/gameplay/suspects/notes/cathy/time_alibi.png");
                    unlockedIcons.add("/Images/gameplay/suspects/notes/icons/time.png");
                }
                else if (topic.equals("cathy_audio") && GameState.isDiscovered("audio_log")) {
                    unlockedNotes.add("/Images/gameplay/suspects/notes/cathy/audio_alibi.png");
                    unlockedIcons.add("/Images/gameplay/suspects/notes/icons/audio.png");
                }
                else if (topic.equals("cathy_rag") && GameState.isDiscovered("blood_cleanup")) {
                    unlockedNotes.add("/Images/gameplay/suspects/notes/cathy/rag_alibi.png");
                    unlockedIcons.add("/Images/gameplay/suspects/notes/icons/rag.png");
                }
                else if (topic.equals("cathy_gym_bag") && GameState.isDiscovered("gym_bag_trace")) {
                    unlockedNotes.add("/Images/gameplay/suspects/notes/cathy/gym_bag_alibi.png");
                    unlockedIcons.add("/Images/gameplay/suspects/notes/icons/gym_bag.png");
                }
            }

            for (int i = 0; i < unlockedNotes.size(); i++) {
                java.net.URL noteUrl = getClass().getResource(unlockedNotes.get(i));
                java.net.URL iconUrl = getClass().getResource(unlockedIcons.get(i));
                if (noteUrl != null && iconUrl != null) {
                    noteLabels[i].setIcon(new javax.swing.ImageIcon(noteUrl));
                    iconLabels[i].setIcon(new javax.swing.ImageIcon(iconUrl));
                    noteLabels[i].setVisible(true);
                    iconLabels[i].setVisible(true);
                    contentPanel.setComponentZOrder(noteLabels[i], 1); 
                    contentPanel.setComponentZOrder(iconLabels[i], 0); 
                }
            }
        }
        // 4. ANDY'S NOTES (Uses 3 slots: 0, 1, and 4 which is note3Alibi3)
        else if (currentSuspectIndex == 2) {
            java.util.List<String> unlockedNotes = new java.util.ArrayList<>();
            java.util.List<String> unlockedIcons = new java.util.ArrayList<>();
            java.util.List<String> order = GameState.getInterrogationOrder();

            for (String topic : order) {
                if (topic.equals("andy_time")) {
                    unlockedNotes.add("/Images/gameplay/suspects/notes/andy/time_alibi.png");
                    unlockedIcons.add("/Images/gameplay/suspects/notes/icons/time.png");
                }
                else if (topic.equals("andy_debt") && GameState.isDiscovered("opened_ledger")) {
                    unlockedNotes.add("/Images/gameplay/suspects/notes/andy/ledger_alibi.png");
                    unlockedIcons.add("/Images/gameplay/suspects/notes/icons/ledger.png");
                }
                else if (topic.equals("andy_pills") && GameState.isDiscovered("drugged_coffee")) {
                    unlockedNotes.add("/Images/gameplay/suspects/notes/andy/drugged_coffee_alibi.png");
                    unlockedIcons.add("/Images/gameplay/suspects/notes/icons/drugged_cofee.png"); // Note: "cofee" is spelled without 'f' in your file name
                }
            }

            // We map Andy's 3 slots manually: 0 (Top-Left), 1 (Top-Right), 4 (note3Alibi3 at Bottom-Left)
            int[] slotIndices = {0, 1, 4}; 
            
            for (int i = 0; i < unlockedNotes.size(); i++) {
                java.net.URL noteUrl = getClass().getResource(unlockedNotes.get(i));
                java.net.URL iconUrl = getClass().getResource(unlockedIcons.get(i));
                
                int slot = slotIndices[i];
                if (noteUrl != null && iconUrl != null) {
                    noteLabels[slot].setIcon(new javax.swing.ImageIcon(noteUrl));
                    iconLabels[slot].setIcon(new javax.swing.ImageIcon(iconUrl));
                    noteLabels[slot].setVisible(true);
                    iconLabels[slot].setVisible(true);
                    contentPanel.setComponentZOrder(noteLabels[slot], 1); 
                    contentPanel.setComponentZOrder(iconLabels[slot], 0); 
                }
            }
        }
        
        contentPanel.revalidate();
        contentPanel.repaint();
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
        java.awt.EventQueue.invokeLater(() -> new Suspects().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel backButton;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JLabel nextButton;
    private javax.swing.JLabel note3Alibi3;
    private javax.swing.JLabel note3Alibi3Icon;
    private javax.swing.JLabel note4Alibi1;
    private javax.swing.JLabel note4Alibi1Icon;
    private javax.swing.JLabel note4Alibi2;
    private javax.swing.JLabel note4Alibi2Icon;
    private javax.swing.JLabel note4Alibi3;
    private javax.swing.JLabel note4Alibi3Icon;
    private javax.swing.JLabel note4Alibi4;
    private javax.swing.JLabel note4Alibi4Icon;
    private javax.swing.JLabel prevButton;
    private javax.swing.JLabel suspect_bg;
    // End of variables declaration//GEN-END:variables
}
