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
    
    /** Pause menu attached to the top-left corner of the screen. */
    private PauseMenu pauseMenu;

    /* Arrest Button */
    private javax.swing.ImageIcon arrestNormal;
    private javax.swing.ImageIcon arrestHover; 
    private boolean isGameOver = false;
    private boolean isCathyArrestedEnding = false; 

    // NOTE: this field is currently unused. Keep if you plan to use it,
    // otherwise delete it to avoid warnings.
    // private javax.swing.JLabel[] alibiLabels = new javax.swing.JLabel[4];

    /** Background file image for each suspect, in carousel order. */
    private final String[] suspectFiles = {
        "/Images/gameplay/suspects/cardo_file.png",
        "/Images/gameplay/suspects/cathy_file.png",
        "/Images/gameplay/suspects/andy_file.png"
    };

    /** Index of the suspect currently shown (0 = Cardo, 1 = Cathy, 2 = Andy). */
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

        setLocationRelativeTo(null);

        // ---- Options Menu ----
        // Closing options returns the player to the pause menu popup.
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
        final javax.swing.ImageIcon backNormal =
                new javax.swing.ImageIcon(getClass().getResource("/Images/ui/back_button.png"));
        final javax.swing.ImageIcon backHover =
                new javax.swing.ImageIcon(getClass().getResource("/Images/ui/back_button_hovered.png"));

        backButton.setIcon(backNormal);
        backButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) return; 
                backButton.setIcon(backHover);
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                backButton.setIcon(backNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isGameOver) return; 
                AudioCache.play("/audio/sfx/ui/select.wav");
                goToCrimeScene();
            }
        });

        // ---- Arrow icons for swapping suspect pages ----
        final javax.swing.ImageIcon nextNormal =
                new javax.swing.ImageIcon(getClass().getResource("/Images/common/next_button.png"));
        final javax.swing.ImageIcon nextHover =
                new javax.swing.ImageIcon(getClass().getResource("/Images/common/next_button_hovered.png"));

        final javax.swing.ImageIcon prevNormal =
                new javax.swing.ImageIcon(getClass().getResource("/Images/common/previous_button.png"));
        final javax.swing.ImageIcon prevHover =
                new javax.swing.ImageIcon(getClass().getResource("/Images/common/previous_button_hovered.png"));

        // --- Prev Button ---
        prevButton.setIcon(prevNormal);
        prevButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        prevButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                prevButton.setIcon(prevHover);
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                prevButton.setIcon(prevNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                AudioCache.play("/audio/sfx/ui/select.wav");
                prevSuspect();
            }
        });

        // --- Next Button ---
        nextButton.setIcon(nextNormal);
        nextButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        nextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                nextButton.setIcon(nextHover);
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                nextButton.setIcon(nextNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                AudioCache.play("/audio/sfx/ui/select.wav");
                nextSuspect();
            }
        });

        // ---- Keyboard Bindings ----
        javax.swing.JRootPane root = getRootPane();
        javax.swing.InputMap im = root.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap am = root.getActionMap();

        // "2" returns to the Crime Scene
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, 0), "backAction");
        am.put("backAction", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                AudioCache.play("/audio/sfx/ui/select.wav");
                goToCrimeScene();
            }
        });

        // "A" or Left Arrow -> previous suspect
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, 0), "prevAction");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0), "prevAction");
        am.put("prevAction", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                AudioCache.play("/audio/sfx/ui/select.wav");
                prevSuspect();
            }
        });

        // "D" or Right Arrow -> next suspect
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, 0), "nextAction");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), "nextAction");
        am.put("nextAction", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                AudioCache.play("/audio/sfx/ui/select.wav");
                nextSuspect();
            }
        });
        
        // ---- Arrest Button Logic ----
        arrestNormal = new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/suspects/arrest_btn.png"));
        arrestHover = new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/suspects/arrest_btn_hovered.png"));

        arrestButton.setIcon(arrestNormal);
        arrestButton.setText(""); // Make sure no text overrides the image
        arrestButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        arrestButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                arrestButton.setIcon(arrestHover);
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                arrestButton.setIcon(arrestNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                AudioCache.play("/audio/sfx/ui/select.wav");
                handleArrest();
            }
        });
        
        // ---- Back to Main Menu Button (Only for True Ending) ----
        backToMainMenu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        backToMainMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!isGameOver || !isCathyArrestedEnding) return;
                AudioCache.play("/audio/sfx/ui/hover.wav"); // <--- HOVER SOUND
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                // No visual change needed, but you can add one here if you want
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!isGameOver || !isCathyArrestedEnding) return;
                AudioCache.play("/audio/sfx/ui/select.wav");
                GameState.reset();
                TransitionOverlay.play(Suspects.this, () -> {
                    MainMenu menu = new MainMenu();
                    menu.setLocation(getLocation());
                    menu.setVisible(true);
                    Suspects.this.dispose();
                });
            }
        });        

        // Hide the optional 4th alibi slot + all its icons by default.
        // updateBackground() will reveal only the slots that are unlocked.
        note4Alibi1.setVisible(false);
        note4Alibi2.setVisible(false);
        note4Alibi3.setVisible(false);
        note4Alibi4.setVisible(false);
        note4Alibi1Icon.setVisible(false);
        note4Alibi2Icon.setVisible(false);
        note4Alibi3Icon.setVisible(false);
        note4Alibi4Icon.setVisible(false);

        // ---- Hide Game Over UI by default ----
        gameoverLabel.setVisible(false);
        restartButton.setVisible(false);
        menuButton.setVisible(false);
        backToMainMenu.setVisible(false);
        
        // ---- Game Over menu buttons ----
        javax.swing.JLabel[] goButtons = { restartButton, menuButton };
        for (javax.swing.JLabel lbl : goButtons) {
            lbl.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!isGameOver) return;
                    AudioCache.play("/audio/sfx/ui/hover.wav");
                }
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (!isGameOver) return;
                    AudioCache.play("/audio/sfx/ui/select.wav");

                    if (lbl == restartButton) {
                        GameState.reset();
                        TransitionOverlay.play(Suspects.this, () -> {
                            Backstory bs = new Backstory();
                            bs.setLocation(getLocation());
                            bs.setVisible(true);
                            Suspects.this.dispose();
                        });
                    } else {
                        GameState.reset();
                        TransitionOverlay.play(Suspects.this, () -> {
                            MainMenu menu = new MainMenu();
                            menu.setLocation(getLocation());
                            menu.setVisible(true);
                            Suspects.this.dispose();
                        });
                    }
                }
            });
        }

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
        backToMainMenu = new javax.swing.JLabel();
        restartButton = new javax.swing.JLabel();
        menuButton = new javax.swing.JLabel();
        gameoverLabel = new javax.swing.JLabel();
        arrestButton = new javax.swing.JLabel();
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
        setTitle("The Fourth Tenant\n");
        setResizable(false);

        contentPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        backToMainMenu.setFont(new java.awt.Font("Press Start 2P", 0, 20)); // NOI18N
        backToMainMenu.setForeground(new java.awt.Color(255, 255, 255));
        backToMainMenu.setText("Back to Main Menu");
        contentPanel.add(backToMainMenu, new org.netbeans.lib.awtextra.AbsoluteConstraints(405, 460, -1, -1));

        restartButton.setFont(new java.awt.Font("Press Start 2P", 0, 20)); // NOI18N
        restartButton.setForeground(new java.awt.Color(255, 255, 255));
        restartButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        restartButton.setText("RESTART");
        restartButton.setPreferredSize(new java.awt.Dimension(180, 32));
        contentPanel.add(restartButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(485, 360, -1, -1));

        menuButton.setFont(new java.awt.Font("Press Start 2P", 0, 20)); // NOI18N
        menuButton.setForeground(new java.awt.Color(255, 255, 255));
        menuButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        menuButton.setText("MAIN MENU");
        menuButton.setPreferredSize(new java.awt.Dimension(180, 32));
        contentPanel.add(menuButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(485, 410, -1, -1));

        gameoverLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ui/3stars_cathy.png"))); // NOI18N
        contentPanel.add(gameoverLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        arrestButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/suspects/arrest_btn.png"))); // NOI18N
        contentPanel.add(arrestButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 250, -1, -1));

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
    
    // ------------------------------------------------------------------
    // Suspect carousel helpers
    // ------------------------------------------------------------------

    /** Moves to the previous suspect, wrapping around. */
    private void prevSuspect() {
        currentSuspectIndex =
                (currentSuspectIndex - 1 + suspectFiles.length) % suspectFiles.length;
        updateBackground();
    }

    /** Moves to the next suspect, wrapping around. */
    private void nextSuspect() {
        currentSuspectIndex = (currentSuspectIndex + 1) % suspectFiles.length;
        updateBackground();
    }

    /** Opens the Crime Scene screen and closes this one. */
    private void goToCrimeScene() {
        CrimeScene cs = new CrimeScene();
        cs.setLocation(getLocation());
        cs.setVisible(true);
        dispose();
    }

    // ------------------------------------------------------------------
    // updateBackground — swaps the big file image and repaints the alibi
    // notes for the currently selected suspect.
    // ------------------------------------------------------------------

    /** Swaps the full-screen background image and updates the alibi notes. */
    private void updateBackground() {
        // 1. Swap the file image.
        String path = suspectFiles[currentSuspectIndex];
        java.net.URL url = getClass().getResource(path);
        if (url != null) {
            suspect_bg.setIcon(new javax.swing.ImageIcon(url));
        } else {
            System.out.println("Missing file image: " + path);
        }

        // 2. Hide every note + icon before repainting.
        hideAllNotes();

        // 3. Collect the unlocked notes for whichever suspect is shown.
        java.util.List<String> notes = new java.util.ArrayList<>();
        java.util.List<String> icons = new java.util.ArrayList<>();
        int[] slots;

        switch (currentSuspectIndex) {
            case 0:
                collectCardoNotes(notes, icons);
                slots = new int[]{0, 1, 2, 3};
                break;
            case 1:
                collectCathyNotes(notes, icons);
                slots = new int[]{0, 1, 2, 3};
                break;
            case 2:
                collectAndyNotes(notes, icons);
                // Andy's 3rd note lives in the bottom-left slot (index 4).
                slots = new int[]{0, 1, 4};
                break;
            default:
                return;
        }

        // 4. Paint the collected notes into their assigned slots.
        applyNotes(notes, icons, slots);
        
        // Only show the arrest button if it's time to arrest the killer
        boolean isEndgame = GameState.isCathyConfronted();
        arrestButton.setVisible(isEndgame);
        
        // Bring it to the front so it's clickable over the background
        if (isEndgame) {
            contentPanel.setComponentZOrder(arrestButton, 0);
        }        

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /** Hides every alibi note and its matching icon. */
    private void hideAllNotes() {
        javax.swing.JLabel[] noteLabels = {
            note4Alibi1, note4Alibi2, note4Alibi3, note4Alibi4, note3Alibi3
        };
        javax.swing.JLabel[] iconLabels = {
            note4Alibi1Icon, note4Alibi2Icon, note4Alibi3Icon, note4Alibi4Icon,
            note3Alibi3Icon
        };
        for (int i = 0; i < noteLabels.length; i++) {
            noteLabels[i].setVisible(false);
            iconLabels[i].setVisible(false);
        }
    }

    /**
     * Paints each collected note + icon into the corresponding entry in
     * {@code slots}. The note is layered above the icon (z-order 1 vs 0).
     */
    private void applyNotes(java.util.List<String> notes,
                            java.util.List<String> icons,
                            int[] slots) {
        javax.swing.JLabel[] noteLabels = {
            note4Alibi1, note4Alibi2, note4Alibi3, note4Alibi4, note3Alibi3
        };
        javax.swing.JLabel[] iconLabels = {
            note4Alibi1Icon, note4Alibi2Icon, note4Alibi3Icon, note4Alibi4Icon,
            note3Alibi3Icon
        };

        for (int i = 0; i < notes.size(); i++) {
            java.net.URL noteUrl = getClass().getResource(notes.get(i));
            java.net.URL iconUrl = getClass().getResource(icons.get(i));
            if (noteUrl == null || iconUrl == null) continue;

            int slot = slots[i];
            noteLabels[slot].setIcon(new javax.swing.ImageIcon(noteUrl));
            iconLabels[slot].setIcon(new javax.swing.ImageIcon(iconUrl));
            noteLabels[slot].setVisible(true);
            iconLabels[slot].setVisible(true);
            contentPanel.setComponentZOrder(noteLabels[slot], 1);
            contentPanel.setComponentZOrder(iconLabels[slot], 0);
        }
    }

    // ------------------------------------------------------------------
    // Per-suspect note collection
    // Each method walks the interrogation order and, for every topic the
    // player has unlocked (plus its evidence prerequisite), adds the note
    // path and icon path to the lists.
    // ------------------------------------------------------------------

    private void collectCardoNotes(java.util.List<String> notes,
                                   java.util.List<String> icons) {
        for (String topic : GameState.getInterrogationOrder()) {
            if (topic.equals("cardo_time")) {
                notes.add("/Images/gameplay/suspects/notes/cardo/time_alibi.png");
                icons.add("/Images/gameplay/suspects/notes/icons/time.png");
            }
            else if (topic.equals("cardo_boots") && GameState.isDiscovered("shoeprints")) {
                notes.add("/Images/gameplay/suspects/notes/cardo/boots_alibi.png");
                icons.add("/Images/gameplay/suspects/notes/icons/boots.png");
            }
            else if (topic.equals("cardo_damage_lock") && GameState.isDiscovered("damaged_lock")) {
                notes.add("/Images/gameplay/suspects/notes/cardo/damage_locks_alibi.png");
                icons.add("/Images/gameplay/suspects/notes/icons/damage_locks.png");
            }
            else if (topic.equals("cardo_jealousy") && GameState.isDiscovered("jealousy")) {
                notes.add("/Images/gameplay/suspects/notes/cardo/jealousy_alibi.png");
                icons.add("/Images/gameplay/suspects/notes/icons/jealousy.png");
            }
        }
    }

    private void collectCathyNotes(java.util.List<String> notes,
                                   java.util.List<String> icons) {
        for (String topic : GameState.getInterrogationOrder()) {
            if (topic.equals("cathy_time")) {
                notes.add("/Images/gameplay/suspects/notes/cathy/time_alibi.png");
                icons.add("/Images/gameplay/suspects/notes/icons/time.png");
            }
            else if (topic.equals("cathy_audio") && GameState.isDiscovered("audio_log")) {
                notes.add("/Images/gameplay/suspects/notes/cathy/audio_alibi.png");
                icons.add("/Images/gameplay/suspects/notes/icons/audio.png");
            }
            else if (topic.equals("cathy_rag") && GameState.isDiscovered("blood_cleanup")) {
                notes.add("/Images/gameplay/suspects/notes/cathy/rag_alibi.png");
                icons.add("/Images/gameplay/suspects/notes/icons/rag.png");
            }
            else if (topic.equals("cathy_gym_bag") && GameState.isDiscovered("gym_bag_trace")) {
                notes.add("/Images/gameplay/suspects/notes/cathy/gym_bag_alibi.png");
                icons.add("/Images/gameplay/suspects/notes/icons/gym_bag.png");
            }
        }
    }

    private void collectAndyNotes(java.util.List<String> notes,
                                  java.util.List<String> icons) {
        for (String topic : GameState.getInterrogationOrder()) {
            if (topic.equals("andy_time")) {
                notes.add("/Images/gameplay/suspects/notes/andy/time_alibi.png");
                icons.add("/Images/gameplay/suspects/notes/icons/time.png");
            }
            else if (topic.equals("andy_debt") && GameState.isDiscovered("opened_ledger")) {
                notes.add("/Images/gameplay/suspects/notes/andy/ledger_alibi.png");
                icons.add("/Images/gameplay/suspects/notes/icons/ledger.png");
            }
            else if (topic.equals("andy_pills") && GameState.isDiscovered("drugged_coffee")) {
                notes.add("/Images/gameplay/suspects/notes/andy/drugged_coffee_alibi.png");
                // NOTE: "cofee" is misspelled in the actual filename —
                // keep it matching the on-disk file.
                icons.add("/Images/gameplay/suspects/notes/icons/drugged_cofee.png");
            }
        }
    }
    
    private void handleArrest() {
        if (currentSuspectIndex == 1) {
            // Correct Suspect (Cathy) -> True Ending
            GameState.setCathyArrested(true);
            showEndingScreen(true, 1); 
        } else {
            // Wrong Suspect (Cardo or Andy) -> Game Over
            showEndingScreen(false, currentSuspectIndex);
        }
    }
    
    private void showEndingScreen(boolean isTrueEnding, int arrestedSuspectIndex) {
        isGameOver = true;
        isCathyArrestedEnding = isTrueEnding;

        // 1. Reveal the game-over UI
        gameoverLabel.setVisible(true);
        arrestButton.setVisible(false);
        
        if (isTrueEnding) {
            // Cathy is arrested: Hide Restart/Menu, show Back to Main Menu
            restartButton.setVisible(false);
            menuButton.setVisible(false);
            backToMainMenu.setVisible(true);
            
            // Pick star rating based on health
            int hp = GameState.getHealth();
            if (hp >= 100) {
                gameoverLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ui/3stars_cathy.png")));
            } else if (hp >= 85) {
                gameoverLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ui/2stars_cathy.png")));
            } else {
                gameoverLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ui/1star_cathy.png")));
            }
            
            // Put the image in the back, and the button in the front
            contentPanel.setComponentZOrder(gameoverLabel, 1); 
            contentPanel.setComponentZOrder(backToMainMenu, 0);
            
        } else {
            // Wrong suspect: Show Restart/Menu, hide Back to Main Menu
            restartButton.setVisible(true);
            menuButton.setVisible(true);
            backToMainMenu.setVisible(false);
            
            // Pick the image based on who was arrested
            if (arrestedSuspectIndex == 0) { // Cardo
                gameoverLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ui/gameover_cardo.png")));
            } else { // Andy
                gameoverLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ui/gameover_andy.png")));
            }
            
            // Put the image in the back, and the buttons in the front
            contentPanel.setComponentZOrder(gameoverLabel, 2); 
            contentPanel.setComponentZOrder(restartButton, 1);
            contentPanel.setComponentZOrder(menuButton, 0);
        }

        // 2. Fade in
        FadeOverlay fade = new FadeOverlay();
        fade.setAlpha(1f);
        contentPanel.add(fade, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1150, 680));
        contentPanel.setComponentZOrder(fade, 0);

        final int DURATION_MS = 800;
        final long start = System.currentTimeMillis();

        javax.swing.Timer fadeTimer = new javax.swing.Timer(16, null);
        fadeTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - start;
            float p = Math.min(1f, elapsed / (float) DURATION_MS);

            fade.setAlpha(1f - p);

            if (p >= 1f) {
                fadeTimer.stop();
                contentPanel.remove(fade);
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        });
        fadeTimer.start();

        // 3. Lock the keyboard and pause menu
        pauseMenu.hideOverlay();
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).clear();
        getRootPane().getActionMap().clear();

        AudioCache.play("/audio/sfx/ui/splatter.wav");
    }
    
    /** Simple black overlay that fades. */
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
        java.awt.EventQueue.invokeLater(() -> new Suspects().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel arrestButton;
    private javax.swing.JLabel backButton;
    private javax.swing.JLabel backToMainMenu;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JLabel gameoverLabel;
    private javax.swing.JLabel menuButton;
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
    private javax.swing.JLabel restartButton;
    private javax.swing.JLabel suspect_bg;
    // End of variables declaration//GEN-END:variables
}
