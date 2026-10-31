/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package theforthtenant;

/**
 *
 * @author yuji
 */
public class Notes extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Notes.class.getName());

    private PauseMenu pauseMenu;
    private int currentBook = 0;
    
    /**
     * Creates new form Notes
     */
    public Notes() {
        initComponents();
        
        // ---- Window icon ----
        try {
            java.awt.Image icon = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream("/Images/common/logo.png"));
            setIconImage(icon);
        } catch (Exception e) {
            System.out.println("Logo not found: " + e.getMessage());
        }
        
        setLocationRelativeTo(null);
        
        // ---- Options Menu ----
        // Closing the options menu returns the player to the pause menu popup.
        final OptionsMenu optionsMenu = OptionsMenu.attachTo(this, new OptionsMenu.Callbacks() {
            @Override public void onSoundChanged(int percent) {
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void onClose() {
                pauseMenu.showPopupOnly();
            }
        });

        // ---- Pause Menu ----
        pauseMenu = PauseMenu.attachTo(this, PauseMenu.Corner.TOP_LEFT, new PauseMenu.Callbacks() {
            @Override public void onPause() {}
            @Override public void onResume() {}
            @Override public void onOptions() {
                // Hide pause popup while the options menu is open.
                pauseMenu.hidePopupOnly();
                optionsMenu.show();
            }
            @Override public void onMainMenu() {
                if (TransitionOverlay.isPlaying()) return;
                pauseMenu.hideOverlay();

                TransitionOverlay.play(Notes.this, () -> {
                    MainMenu menu = new MainMenu();
                    menu.setLocation(getLocation());
                    menu.setVisible(true);
                    Notes.this.dispose();
                });
            }
        });           
        
        // Force both text areas and their scroll panes to be fully invisible backgrounds
        detectiveNotesLeft.setOpaque(false);
        detectiveNotesLeft.setBackground(new java.awt.Color(0, 0, 0, 0));
        jScrollPane1.setOpaque(false);
        jScrollPane1.getViewport().setOpaque(false);
        jScrollPane1.setBorder(null);
        jScrollPane1.setViewportBorder(null);

        detectiveNotesRight.setOpaque(false);
        detectiveNotesRight.setBackground(new java.awt.Color(0, 0, 0, 0));
        jScrollPane2.setOpaque(false);
        jScrollPane2.getViewport().setOpaque(false);
        jScrollPane2.setBorder(null);
        jScrollPane2.setViewportBorder(null);        
        
        // Load saved Detective's Notes
        detectiveNotesLeft.setText(GameState.getDetectiveNotesLeft());
        detectiveNotesRight.setText(GameState.getDetectiveNotesRight());

        // Save to GameState whenever the player types
        javax.swing.event.DocumentListener notesListener = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { save(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { save(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { save(); }
            private void save() {
                GameState.setDetectiveNotesLeft(detectiveNotesLeft.getText());
                GameState.setDetectiveNotesRight(detectiveNotesRight.getText());
            }
        };
        
        detectiveNotesLeft.getDocument().addDocumentListener(notesListener);
        detectiveNotesRight.getDocument().addDocumentListener(notesListener);    
        
        // Allow the player to click into the page to start typing
        java.awt.event.MouseAdapter focusClicker = new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                ((javax.swing.JComponent)e.getSource()).requestFocusInWindow();
            }
        };
        detectiveNotesLeft.addMouseListener(focusClicker);
        detectiveNotesRight.addMouseListener(focusClicker);
        jScrollPane1.addMouseListener(focusClicker);
        jScrollPane2.addMouseListener(focusClicker);

        // ---- Set up Arrows & Buttons ----
        final javax.swing.ImageIcon nextNormal = new javax.swing.ImageIcon(getClass().getResource("/Images/common/next_button.png"));
        final javax.swing.ImageIcon nextHover  = new javax.swing.ImageIcon(getClass().getResource("/Images/common/next_button_hovered.png"));
        final javax.swing.ImageIcon prevNormal = new javax.swing.ImageIcon(getClass().getResource("/Images/common/previous_button.png"));
        final javax.swing.ImageIcon prevHover  = new javax.swing.ImageIcon(getClass().getResource("/Images/common/previous_button_hovered.png"));
        final javax.swing.ImageIcon backNormal = new javax.swing.ImageIcon(getClass().getResource("/Images/ui/back_button.png"));
        final javax.swing.ImageIcon backHover  = new javax.swing.ImageIcon(getClass().getResource("/Images/ui/back_button_hovered.png"));

        // Next Button (Flip to right page)
        nextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                nextButton.setIcon(nextHover);
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                AudioCache.play("/audio/sfx/ui/select.wav");
                if (currentBook < 1) { 
                    currentBook = 1;
                    updateBookUI();
                }
            }
        });

        // Prev Button (Flip to left page)
        prevButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                prevButton.setIcon(prevHover);
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                AudioCache.play("/audio/sfx/ui/select.wav");
                // CHANGE THIS PART:
                if (currentBook > 0) { 
                    currentBook = 0;
                    updateBookUI();
                }
            }
        });

        // Back Button (Exit to CrimeScene)
        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                backButton.setIcon(backHover);
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                AudioCache.play("/audio/sfx/ui/select.wav");
                CrimeScene cs = new CrimeScene();
                cs.setLocation(getLocation());
                cs.setVisible(true);
                dispose();
            }
        });        
        
        // ---- Keyboard Shortcuts ----
        javax.swing.JRootPane root = getRootPane();
        javax.swing.InputMap im = root.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap am = root.getActionMap();

        // "3" returns to the Crime Scene
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, 0), "backAction");
        am.put("backAction", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                AudioCache.play("/audio/sfx/ui/select.wav");
                CrimeScene cs = new CrimeScene();
                cs.setLocation(getLocation());
                cs.setVisible(true);
                dispose();
            }
        });

        // "A" or Left Arrow -> Unlocked Clues (Book 0)
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, 0), "prevPage");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0), "prevPage");
        am.put("prevPage", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                // If the player is actively typing inside the text box, don't flip the page
                java.awt.Component focusOwner = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (focusOwner == detectiveNotesLeft || focusOwner == detectiveNotesRight) return;
                
                if (currentBook > 0) {
                    AudioCache.play("/audio/sfx/ui/select.wav");
                    currentBook = 0;
                    updateBookUI();
                }
            }
        });

        // "D" or Right Arrow -> Detective's Notes (Book 1)
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, 0), "nextPage");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), "nextPage");
        am.put("nextPage", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                java.awt.Component focusOwner = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (focusOwner == detectiveNotesLeft || focusOwner == detectiveNotesRight) return;
                
                if (currentBook < 1) {
                    AudioCache.play("/audio/sfx/ui/select.wav");
                    currentBook = 1;
                    updateBookUI();
                }
            }
        });

        // Call loadNotes() at the very end of the constructor
        loadNotes();
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
        nextButton = new javax.swing.JLabel();
        prevButton = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        detectiveNotesRight = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        detectiveNotesLeft = new javax.swing.JTextArea();
        rightPage = new javax.swing.JLabel();
        leftPage = new javax.swing.JLabel();
        backgroundLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("The Fourth Tenant");
        setResizable(false);

        contentPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ui/back_button.png"))); // NOI18N
        contentPanel.add(backButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 635, -1, -1));

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/common/next_button.png"))); // NOI18N
        contentPanel.add(nextButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1103, 320, -1, -1));

        prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/common/previous_button.png"))); // NOI18N
        contentPanel.add(prevButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 320, -1, -1));

        jScrollPane2.setFont(new java.awt.Font("Coming Soon", 0, 14)); // NOI18N
        jScrollPane2.setOpaque(false);

        detectiveNotesRight.setBackground(new java.awt.Color(0, 0, 0));
        detectiveNotesRight.setColumns(20);
        detectiveNotesRight.setFont(new java.awt.Font("Coming Soon", 0, 18)); // NOI18N
        detectiveNotesRight.setLineWrap(true);
        detectiveNotesRight.setRows(5);
        detectiveNotesRight.setWrapStyleWord(true);
        detectiveNotesRight.setOpaque(false);
        jScrollPane2.setViewportView(detectiveNotesRight);

        contentPanel.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 160, 310, 450));

        jScrollPane1.setFont(new java.awt.Font("Coming Soon", 0, 14)); // NOI18N
        jScrollPane1.setOpaque(false);

        detectiveNotesLeft.setBackground(new java.awt.Color(0, 0, 0));
        detectiveNotesLeft.setColumns(20);
        detectiveNotesLeft.setFont(new java.awt.Font("Coming Soon", 0, 18)); // NOI18N
        detectiveNotesLeft.setLineWrap(true);
        detectiveNotesLeft.setRows(5);
        detectiveNotesLeft.setWrapStyleWord(true);
        detectiveNotesLeft.setOpaque(false);
        jScrollPane1.setViewportView(detectiveNotesLeft);

        contentPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 160, 340, 450));

        rightPage.setFont(new java.awt.Font("Press Start 2P", 0, 12)); // NOI18N
        rightPage.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        contentPanel.add(rightPage, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 150, 300, 460));

        leftPage.setFont(new java.awt.Font("Press Start 2P", 0, 12)); // NOI18N
        leftPage.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        contentPanel.add(leftPage, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 170, 330, 430));

        backgroundLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/notes/unlocked_clues.png"))); // NOI18N
        contentPanel.add(backgroundLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

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
    
    /** Swaps the background and text based on which book we are on. */
    private void updateBookUI() {
        if (currentBook == 0) {
            // Unlocked Clues Book
            backgroundLabel.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/Images/gameplay/notes/unlocked_clues.png")));
            
            leftPage.setVisible(true);
            rightPage.setVisible(true);
            
            jScrollPane1.setVisible(false); // Hide the left text area
            jScrollPane2.setVisible(false); // Hide the right text area
            
            prevButton.setVisible(false);
            nextButton.setVisible(true);
            
            contentPanel.requestFocusInWindow();
            
        } else {
            // Detective's Notes Book
            backgroundLabel.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/Images/gameplay/notes/detective_notes.png")));
            
            leftPage.setVisible(false);
            rightPage.setVisible(false);
            
            jScrollPane1.setVisible(true); 
            jScrollPane2.setVisible(true);
            
            // --- THE MAGIC FIX: Force transparency ---
            jScrollPane1.setOpaque(false);
            jScrollPane1.getViewport().setOpaque(false);
            jScrollPane1.setBackground(new java.awt.Color(0, 0, 0, 0));
            jScrollPane1.getViewport().setBackground(new java.awt.Color(0, 0, 0, 0));
            
            jScrollPane2.setOpaque(false);
            jScrollPane2.getViewport().setOpaque(false);
            jScrollPane2.setBackground(new java.awt.Color(0, 0, 0, 0));
            jScrollPane2.getViewport().setBackground(new java.awt.Color(0, 0, 0, 0));
            // ----------------------------------------
            
            contentPanel.setComponentZOrder(jScrollPane1, 0);
            contentPanel.setComponentZOrder(jScrollPane2, 0);
            
            prevButton.setVisible(true);
            nextButton.setVisible(false);
            
        }
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /** Populates the left and right pages with everything the player has discovered. */
    private void loadNotes() {
        StringBuilder leftHtml = new StringBuilder();
        StringBuilder rightHtml = new StringBuilder();
        
        String style = "<html><body style='font-family: ComingSoon-Regular; color: #222; font-size: 14px; line-height: 1.4;'>";
        leftHtml.append(style);
        rightHtml.append(style);
        
        int leftClueCount = 0;

        // List of all possible clues
        String[] clueIds = {
            "damaged_lock", "drugged_coffee", "shoeprints", "blood_cleanup",
            "audio_log", "gym_bag_trace", "jealousy", "opened_ledger"
        };
        String[] clueTexts = {
            "The rooftop door was forced open with Cardo's tools.",
            "Abby was knocked out by coffee laced with Andy's pills.",
            "The killer wore Cardo's boots to track mud across the scene.",
            "The crime scene was wiped down, and the bloody rag was hidden in Cathy's room.",
            "CCTV recorded Abby arguing with a woman right before the murder.",
            "Drag marks confirm Abby's body was hauled away in Cathy's gym bag.",
            "Cardo previously threatened to kill Abby if she ever left him.",
            "Andy was being heavily pressured by Abby over a ₱67,000 debt."
        };

        // Loop through all clues
        for (int i = 0; i < clueIds.length; i++) {
            if (GameState.isDiscovered(clueIds[i])) {
                String bullet = "<span style='color: #000000; font-size: 20px; font-weight: bold;'>&#8226;</span> ";
                String formatted = "<div style='margin-bottom: 15px;'>" + bullet + clueTexts[i] + "</div>";
                
                // If left page has less than 4 clues, put it on the left.
                if (leftClueCount < 4) {
                    leftHtml.append(formatted);
                    leftClueCount++;
                } else {
                    rightHtml.append(formatted);
                }
            }
        }

        // Fallbacks (only if BOTH pages are completely empty)
        if (leftClueCount == 0) {
            leftHtml.append("<i style='color: #666;'>No clues discovered yet...</i>");
        }

        leftHtml.append("</body></html>");
        rightHtml.append("</body></html>");

        leftPage.setText(leftHtml.toString());
        rightPage.setText(rightHtml.toString());
        
        // Start on the Unlocked Clues book
        currentBook = 0;
        updateBookUI(); 
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
        java.awt.EventQueue.invokeLater(() -> new Notes().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel backButton;
    private javax.swing.JLabel backgroundLabel;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JTextArea detectiveNotesLeft;
    private javax.swing.JTextArea detectiveNotesRight;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel leftPage;
    private javax.swing.JLabel nextButton;
    private javax.swing.JLabel prevButton;
    private javax.swing.JLabel rightPage;
    // End of variables declaration//GEN-END:variables
}
