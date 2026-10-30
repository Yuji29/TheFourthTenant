/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package theforthtenant;

/**
 * The crime scene frame: lets the player navigate between rooms of the
 * boarding house, collect evidence items, and open the inventory, suspects,
 * notes, interrogate, and case-file panels from the bottom menu bar.
 *
 * @author yuji
 */
public class CrimeScene extends javax.swing.JFrame {

    // ---- Background scene rotation ----
    private final String[] sceneImages = {
        "/Images/gameplay/locations/FrontPorch.png",
        "/Images/gameplay/locations/LaundryRoom.png",
        "/Images/gameplay/locations/Kitchen.png",
        "/Images/gameplay/locations/LivingRoom.png",
        "/Images/gameplay/locations/DiningArea.png",
        "/Images/gameplay/locations/BedRoom.png",
        "/Images/gameplay/locations/CrimeScene.png",
        "/Images/gameplay/locations/CardosRoom.png"
    };

    /** Index of the currently displayed scene (matches sceneImages). */
    private int currentScene = 0;

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(CrimeScene.class.getName());

    // =========================================================
    // Collectable items
    // =========================================================

    /**
     * One collectable item: owns its sprite label, the scene it belongs to,
     * its stable id, its alpha mask (for pixel-accurate hit testing), and
     * whether it has been picked up. All per-item state lives here, so the
     * frame never has to consult parallel maps or client properties.
     */
    private static final class Collectable {
        final javax.swing.JLabel sprite;
        final int sceneIndex;
        final String id;
        final java.awt.image.BufferedImage mask;
        boolean pickedUp;

        Collectable(javax.swing.JLabel sprite, int sceneIndex, String id,
                    java.awt.image.BufferedImage mask) {
            this.sprite = sprite;
            this.sceneIndex = sceneIndex;
            this.id = id;
            this.mask = mask;
        }
    }

    // ---- Items and menus ----
    private final java.util.List<Collectable> collectables = new java.util.ArrayList<>();
    private PauseMenu pauseMenu;
    private OptionsMenu optionsMenu;
    private boolean isGameOver = false;

    // Currently playing ambient loop (rain or indoor hum)
    private javax.sound.sampled.Clip ambienceClip;
    private String ambiencePath;

    // ---- Fade-in overlay shown when the scene first opens ----
    private FadeOverlay fadeOverlay;

    // ---- Interrogation unlock state ----
    private boolean interrogateUnlocked = false;
    private boolean interrogateListenerAttached = false;

    // Only play the fade-in the first time the CrimeScene opens (from Backstory).
    private static boolean introFadePlayed = false;

    // ---- Scene index constants ----
    private static final int SCENE_PORCH   = 0;
    private static final int SCENE_LAUNDRY = 1;
    private static final int SCENE_KITCHEN = 2;
    private static final int SCENE_LIVING  = 3;
    private static final int SCENE_DINING  = 4;
    private static final int SCENE_BED     = 5;
    private static final int SCENE_CRIME   = 6;
    private static final int SCENE_CARDO   = 7;

    /**
     * Creates new form CrimeScene.
     */
    public CrimeScene() {
        initComponents();

        // ---- Window icon ----
        try {
            java.awt.Image icon = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream("/Images/common/logo.png"));
            setIconImage(icon);
        } catch (Exception e) {
            System.out.println("Logo not found: " + e.getMessage());
        }

        // Warm the SFX cache on a background thread.
        new javax.swing.SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                AudioCache.prepare(
                    "/audio/sfx/gameplay/pickup.wav",
                    "/audio/sfx/ambience/indoor_hum.wav",
                    "/audio/sfx/gameplay/tape_ripping.wav",
                    "/audio/sfx/gameplay/thud.wav",
                    "/audio/sfx/ambience/rain_loop.wav",
                    "/audio/sfx/ui/hover.wav",
                    "/audio/sfx/ui/select.wav",
                    "/audio/sfx/ui/pause_open.wav",
                    "/audio/sfx/ui/click.wav",
                    "/audio/sfx/ui/zipper.wav",
                    "/audio/sfx/gameplay/door_close.wav"
                );
                return null;
            }
        }.execute();

        // ---- Single hit-test for the whole scene: pick on press ----
        contentPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                if (pauseMenu != null && pauseMenu.isPaused()) return;
                if (TransitionOverlay.isPlaying()) return;
                if (GameState.getHealth() <= 0) return;

                Collectable hit = pickItem(e.getPoint());
                if (hit != null) {
                    collect(hit);
                } else {
                    // Missed an item -> take damage.
                    AudioCache.play("/audio/sfx/ui/click.wav");
                    GameState.setHealth(GameState.getHealth() - 1);

                    // Update the health bar.
                    for (java.awt.Component c : contentPanel.getComponents()) {
                        if (c instanceof HealthBar) {
                            ((HealthBar) c).setCurrent(GameState.getHealth());
                        }
                    }

                    if (GameState.getHealth() <= 0) {
                        showGameOver();
                    }
                }
            }
        });

        contentPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                if (pauseMenu != null && pauseMenu.isPaused()) {
                    contentPanel.setCursor(java.awt.Cursor.getDefaultCursor());
                    return;
                }
                contentPanel.setCursor(pickItem(e.getPoint()) != null
                        ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
                        : java.awt.Cursor.getDefaultCursor());
            }
        });

        setLocationRelativeTo(null);

        // ---- Bottom menu-bar labels ----
        final javax.swing.JLabel[] menuLabels = {
            inventoryLabel, suspectsLabel, notesLabel, interrogateLabel, caseFileLabel
        };
        final String[] menuText = {
            "INVENTORY", "SUSPECTS", "NOTES", "INTERROGATE", "CASE FILE"
        };

        for (int i = 0; i < menuLabels.length; i++) {
            final javax.swing.JLabel lbl = menuLabels[i];
            final int index = i;

            // INTERROGATE (index 3) starts locked — no hover, no hand cursor,
            // no click listener. attachInterrogateListener() wires it up later.
            final boolean locked = (index == 3) && !interrogateUnlocked;
            if (locked) continue;

            lbl.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (isGameOver) return;
                    AudioCache.play("/audio/sfx/ui/hover.wav");
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                }
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (isGameOver) return;
                    System.out.println(menuText[index] + " clicked");

                    switch (index) {
                        case 0: openInventory();   break;
                        case 1: openSuspects();    break;
                        case 2: openNotes();       break;
                        case 3: openInterrogate(); break;
                        case 4: openCaseFile();    break;
                        default:
                            // NOTES (2) is not yet wired up — sound only.
                            AudioCache.play("/audio/sfx/ui/select.wav");
                    }
                }
            });
        }

        // ---- Options popup ----
        optionsMenu = OptionsMenu.attachTo(this, new OptionsMenu.Callbacks() {
            @Override public void onMusicChanged(int percent) {
                // CrimeScene has no music — nothing to do here.
            }
            @Override public void onSoundChanged(int percent) {
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void onClose() {
                pauseMenu.showPopupOnly();
            }
        });

        // ---- Pause menu ----
        pauseMenu = PauseMenu.attachTo(this, PauseMenu.Corner.TOP_LEFT, new PauseMenu.Callbacks() {
            @Override public void onPause() {
                if (isGameOver) return;
                if (ambienceClip != null) ambienceClip.stop();
            }
            @Override public void onResume() {
                if (ambienceClip != null) ambienceClip.start();
            }
            @Override public void onOptions() {
                pauseMenu.hidePopupOnly();
                optionsMenu.show();
            }
            @Override public void onMainMenu() {
                // If a transition is already in flight, don't hide the pause
                // overlay — otherwise the player lands on an unpaused scene
                // with no pause menu visible.
                if (TransitionOverlay.isPlaying()) return;

                if (ambienceClip != null) { ambienceClip.stop(); ambienceClip = null; }
                pauseMenu.hideOverlay();

                TransitionOverlay.play(CrimeScene.this, () -> {
                    MainMenu menu = new MainMenu();
                    menu.setLocation(getLocation());
                    menu.setVisible(true);
                    CrimeScene.this.dispose();
                });
            }
        });

        // ---- Scene navigation + menu shortcuts (keyboard) ----
        javax.swing.JRootPane root = getRootPane();
        javax.swing.InputMap im = root.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap am = root.getActionMap();

        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0),  "scenePrev");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), "sceneNext");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, 0),     "scenePrev");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, 0),     "sceneNext");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, 0),     "openInventory");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, 0),     "openSuspects");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, 0),     "openNotes");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_4, 0),     "openInterrogate");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_5, 0),     "openCaseFile");
        
        // ---- DEBUG CHEAT: Press '0' to unlock everything ----
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0, 0), "debugUnlock");
        am.put("debugUnlock", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (isMenuBlocked()) return;

                // 1. Collect all evidence
                String[] items = {"padlock", "belt", "dragpath", "phone", "footmarks", "drum", "usb", "boots", "mug", "toolbox", "prescription", "ledger", "rag", "love_letters"};
                for (String item : items) {
                    GameState.markCollected(item);
                }

                // 2. Unlock phone and discover all combines
                GameState.setPhoneUnlocked(true);
                String[] discoveries = {"damaged_lock", "drugged_coffee", "shoeprints", "blood_cleanup", "audio_log", "gym_bag_trace", "jealousy", "opened_ledger"};
                for (String disc : discoveries) {
                    GameState.markDiscovered(disc);
                }

                // 3. Mark all interrogations as complete
                String[] topics = {"cardo_time", "cardo_boots", "cardo_damage_lock", "cardo_jealousy", "cathy_time", "cathy_audio", "cathy_rag", "cathy_gym_bag", "andy_time", "andy_debt", "andy_pills"};
                for (String topic : topics) {
                    GameState.markInterrogated(topic);
                }

                // 4. Skip straight to the arrest phase
                GameState.setPoliceReportDone(true);
                GameState.setCathyConfronted(true);

                AudioCache.play("/audio/sfx/ui/notification.wav");
                System.out.println("DEBUG: Game Unlocked! Reloading Scene...");
                
                // Reload the crime scene so the UI updates
                CrimeScene cs = new CrimeScene();
                cs.setLocation(getLocation());
                cs.setVisible(true);
                CrimeScene.this.dispose();
            }
        });        

        am.put("scenePrev", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (isGameOver) return;
                navigateScene(-1);
            }
        });
        am.put("sceneNext", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (isGameOver) return;
                navigateScene(+1);
            }
        });

        am.put("openInventory", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (isMenuBlocked()) return;
                openInventory();
            }
        });
        am.put("openSuspects", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (isMenuBlocked()) return;
                openSuspects();
            }
        });
        am.put("openNotes", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (isMenuBlocked()) return;
                openNotes();
            }
        });        
        am.put("openInterrogate", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (isMenuBlocked()) return;
                if (!interrogateUnlocked) return;
                openInterrogate();
            }
        });
        am.put("openCaseFile", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (isMenuBlocked()) return;
                openCaseFile();
            }
        });

        // ---- Prev / Next scene arrows ----
        final javax.swing.ImageIcon prevNormal = new javax.swing.ImageIcon(
                getClass().getResource("/Images/common/previous_button.png"));
        final javax.swing.ImageIcon nextNormal = new javax.swing.ImageIcon(
                getClass().getResource("/Images/common/next_button.png"));
        final javax.swing.ImageIcon prevDark = new javax.swing.ImageIcon(
                getClass().getResource("/Images/common/previous_button_hovered.png"));
        final javax.swing.ImageIcon nextDark = new javax.swing.ImageIcon(
                getClass().getResource("/Images/common/next_button_hovered.png"));

        prevButton.setIcon(prevNormal);
        prevButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                prevButton.setIcon(prevDark);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                prevButton.setIcon(prevNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                navigateScene(-1);
            }
        });

        nextButton.setIcon(nextNormal);
        nextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                nextButton.setIcon(nextDark);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                nextButton.setIcon(nextNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                navigateScene(+1);
            }
        });
        
        // ---- Fade-in overlay — only on the very first CrimeScene (from Backstory) ----
        if (!introFadePlayed) {
            introFadePlayed = true;

            fadeOverlay = new FadeOverlay();
            contentPanel.add(fadeOverlay,
                    new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1150, 680));
            contentPanel.setComponentZOrder(fadeOverlay, 0);

            javax.swing.Timer starter = new javax.swing.Timer(150, e -> fadeIn());
            starter.setRepeats(false);
            starter.start();
        }

        // ---- Collectable crime scene items ----
        makeCollectable(padlock,      SCENE_CRIME,   "padlock");
        makeCollectable(belt,         SCENE_CRIME,   "belt");
        makeCollectable(dragpath,     SCENE_CRIME,   "dragpath");
        makeCollectable(phone,        SCENE_CRIME,   "phone");
        makeCollectable(footmarks,    SCENE_CRIME,   "footmarks");
        makeCollectable(drum,         SCENE_CRIME,   "drum");
        makeCollectable(usb,          SCENE_PORCH,   "usb");
        makeCollectable(boots,        SCENE_LAUNDRY, "boots");
        makeCollectable(mug,          SCENE_KITCHEN, "mug");
        makeCollectable(toolbox,      SCENE_LIVING,  "toolbox");
        makeCollectable(prescription, SCENE_DINING,  "prescription");
        makeCollectable(ledger,       SCENE_DINING,  "ledger");
        makeCollectable(rag,          SCENE_BED,     "rag");
        makeCollectable(loveLetters,  SCENE_CARDO,   "love_letters");

        // ---- Restore INTERROGATE unlock state (from a previous visit) ----
        if (GameState.isFullyComplete()) {
            interrogateUnlocked = true;
            interrogateListenerAttached = true;
            interrogateLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            attachInterrogateListener();

            if (!GameState.hasTapeAnimationPlayed()) {
                // First time — play the tape fall animation.
                GameState.setTapeAnimationPlayed(true);
                AudioCache.play("/audio/sfx/gameplay/tape_ripping.wav");

                javax.swing.Timer tapeDelay = new javax.swing.Timer(150, e -> {
                    if (policeTape != null && policeTape.getParent() != null) {
                        playTapeFallAnimation();
                    }
                });
                tapeDelay.setRepeats(false);
                tapeDelay.start();
            } else {
                // Already played — just remove the tape quietly.
                if (policeTape != null && policeTape.getParent() != null) {
                    policeTape.getParent().remove(policeTape);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                }
            }
        }

        // ---- Health bar ----
        HealthBar healthBar = new HealthBar(100);
        // Width 280 = 70px icon + 210px bar; height 70.
        contentPanel.add(healthBar,
                new org.netbeans.lib.awtextra.AbsoluteConstraints(830, 10, 280, 70));
        contentPanel.setComponentZOrder(healthBar, 0);

        // ---- Show the starting scene ----
        showScene(GameState.getLastSceneIndex());

        // ---- Hide Game Over UI by default ----
        gameoverLabel.setVisible(false);
        restartButton.setVisible(false);
        menuButton.setVisible(false);

        // ---- Game-over menu buttons (hover sound + click, no selection marker) ----
        javax.swing.JLabel[] goButtons = { restartButton, menuButton };

        for (javax.swing.JLabel lbl : goButtons) {
            lbl.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    AudioCache.play("/audio/sfx/ui/hover.wav");
                }
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    AudioCache.play("/audio/sfx/ui/select.wav");

                    if (lbl == restartButton) {
                        GameState.reset();
                        TransitionOverlay.play(CrimeScene.this, () -> {
                            Backstory bs = new Backstory();
                            bs.setLocation(getLocation());
                            bs.setVisible(true);
                            CrimeScene.this.dispose();
                        });
                    } else {
                        GameState.reset();
                        TransitionOverlay.play(CrimeScene.this, () -> {
                            MainMenu menu = new MainMenu();
                            menu.setLocation(getLocation());
                            menu.setVisible(true);
                            CrimeScene.this.dispose();
                        });
                    }
                }
            });
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
        restartButton = new javax.swing.JLabel();
        menuButton = new javax.swing.JLabel();
        gameoverLabel = new javax.swing.JLabel();
        ledger = new javax.swing.JLabel();
        loveLetters = new javax.swing.JLabel();
        rag = new javax.swing.JLabel();
        prescription = new javax.swing.JLabel();
        usb = new javax.swing.JLabel();
        mug = new javax.swing.JLabel();
        boots = new javax.swing.JLabel();
        toolbox = new javax.swing.JLabel();
        padlock = new javax.swing.JLabel();
        belt = new javax.swing.JLabel();
        phone = new javax.swing.JLabel();
        footmarks = new javax.swing.JLabel();
        drum = new javax.swing.JLabel();
        dragpath = new javax.swing.JLabel();
        policeTape = new javax.swing.JLabel();
        nextButton = new javax.swing.JLabel();
        prevButton = new javax.swing.JLabel();
        caseFileLabel = new javax.swing.JLabel();
        interrogateLabel = new javax.swing.JLabel();
        notesLabel = new javax.swing.JLabel();
        suspectsLabel = new javax.swing.JLabel();
        inventoryLabel = new javax.swing.JLabel();
        backgroundLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("The Fourth Tenant\n");
        setResizable(false);

        contentPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

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

        gameoverLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ui/gameover.png"))); // NOI18N
        contentPanel.add(gameoverLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        ledger.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/items/ledger.png"))); // NOI18N
        contentPanel.add(ledger, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 320, -1, -1));

        loveLetters.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/items/love_letters.png"))); // NOI18N
        contentPanel.add(loveLetters, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 340, -1, -1));

        rag.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/items/rag.png"))); // NOI18N
        contentPanel.add(rag, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 510, -1, -1));

        prescription.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/items/prescription.png"))); // NOI18N
        contentPanel.add(prescription, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 400, -1, -1));

        usb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/items/usb.png"))); // NOI18N
        contentPanel.add(usb, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 550, -1, -1));

        mug.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/items/mug.png"))); // NOI18N
        contentPanel.add(mug, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 380, -1, -1));

        boots.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/items/boots.png"))); // NOI18N
        contentPanel.add(boots, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 390, -1, -1));

        toolbox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/items/toolbox.png"))); // NOI18N
        contentPanel.add(toolbox, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 370, -1, -1));

        padlock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/items/padlock.png"))); // NOI18N
        contentPanel.add(padlock, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 80, -1, -1));

        belt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/items/belt.png"))); // NOI18N
        contentPanel.add(belt, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 420, -1, -1));

        phone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/items/phone.png"))); // NOI18N
        contentPanel.add(phone, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 420, -1, -1));

        footmarks.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/items/footmarks.png"))); // NOI18N
        contentPanel.add(footmarks, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 280, -1, -1));

        drum.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/items/drum.png"))); // NOI18N
        contentPanel.add(drum, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 80, -1, -1));

        dragpath.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/items/dragpath.png"))); // NOI18N
        contentPanel.add(dragpath, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 310, -1, -1));

        policeTape.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/policetape.png"))); // NOI18N
        contentPanel.add(policeTape, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 610, 250, 70));

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/common/next_button.png"))); // NOI18N
        nextButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(nextButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1110, 240, 40, 60));

        prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/common/previous_button.png"))); // NOI18N
        prevButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(prevButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 240, 60, 80));

        caseFileLabel.setBackground(new java.awt.Color(0, 0, 0));
        caseFileLabel.setFont(new java.awt.Font("Press Start 2P", 0, 15)); // NOI18N
        caseFileLabel.setForeground(new java.awt.Color(255, 255, 255));
        caseFileLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        caseFileLabel.setText("CASE FILE");
        caseFileLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(caseFileLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 610, 180, 50));

        interrogateLabel.setBackground(new java.awt.Color(0, 0, 0));
        interrogateLabel.setFont(new java.awt.Font("Press Start 2P", 0, 15)); // NOI18N
        interrogateLabel.setForeground(new java.awt.Color(255, 255, 255));
        interrogateLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        interrogateLabel.setText("INTERROGATE");
        interrogateLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(interrogateLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 610, 220, 70));

        notesLabel.setBackground(new java.awt.Color(0, 0, 0));
        notesLabel.setFont(new java.awt.Font("Press Start 2P", 0, 15)); // NOI18N
        notesLabel.setForeground(new java.awt.Color(255, 255, 255));
        notesLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        notesLabel.setText("NOTES");
        notesLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(notesLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 610, 220, 70));

        suspectsLabel.setBackground(new java.awt.Color(0, 0, 0));
        suspectsLabel.setFont(new java.awt.Font("Press Start 2P", 0, 15)); // NOI18N
        suspectsLabel.setForeground(new java.awt.Color(255, 255, 255));
        suspectsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        suspectsLabel.setText("SUSPECTS");
        suspectsLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(suspectsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 610, 230, 70));

        inventoryLabel.setBackground(new java.awt.Color(0, 0, 0));
        inventoryLabel.setFont(new java.awt.Font("Press Start 2P", 0, 15)); // NOI18N
        inventoryLabel.setForeground(new java.awt.Color(255, 255, 255));
        inventoryLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        inventoryLabel.setText("INVENTORY");
        inventoryLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(inventoryLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 610, 230, 70));

        backgroundLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/locations/FrontPorch.png"))); // NOI18N
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

    // ------------------------------------------------------------------
    // Navigation helpers
    // Shared by the bottom menu labels AND the number-key shortcuts so
    // that both paths behave identically.
    // ------------------------------------------------------------------

    /** True when a menu shortcut should be ignored (game over / paused / transitioning). */
    private boolean isMenuBlocked() {
        if (isGameOver) return true;
        if (pauseMenu != null && pauseMenu.isPaused()) return true;
        if (TransitionOverlay.isPlaying()) return true;
        return false;
    }

    /** Opens the Inventory screen, preserving window location. */
    private void openInventory() {
        AudioCache.play("/audio/sfx/ui/zipper.wav");
        Inventory inv = new Inventory();
        inv.setLocation(getLocation());
        inv.setVisible(true);
        dispose();
    }

    /** Opens the Suspects screen, preserving window location. */
    private void openSuspects() {
        AudioCache.play("/audio/sfx/ui/select.wav");
        Suspects sus = new Suspects();
        sus.setLocation(getLocation());
        sus.setVisible(true);
        dispose();
    }
    
    /** Opens the Notes screen, preserving window location. */
    private void openNotes() {
        AudioCache.play("/audio/sfx/ui/select.wav");
        Notes notes = new Notes();
        notes.setLocation(getLocation());
        notes.setVisible(true);
        dispose();
    }    

    /** Opens the Interrogate screen, preserving window location. */
    private void openInterrogate() {
        AudioCache.play("/audio/sfx/gameplay/door_close.wav");
        Interrogate ir = new Interrogate();
        ir.setLocation(getLocation());
        ir.setVisible(true);
        dispose();
    }

    /** Opens the Case File screen, preserving window location. */
    private void openCaseFile() {
        AudioCache.play("/audio/sfx/ui/select.wav");
        CaseFile cf = new CaseFile();
        cf.setLocation(getLocation());
        cf.setVisible(true);
        dispose();
    }    
    
    /**
     * Shows the scene at the given index, clamping to valid bounds.
     * Also updates item visibility so only items belonging to this scene
     * (and not yet picked up) are shown.
     */
    private void showScene(int index) {
        if (sceneImages.length == 0) return;
        if (index < 0) index = 0;
        if (index >= sceneImages.length) index = sceneImages.length - 1;
        currentScene = index;

        GameState.setLastSceneIndex(index);

        java.net.URL url = getClass().getResource(sceneImages[index]);
        if (url != null) {
            backgroundLabel.setIcon(new javax.swing.ImageIcon(url));
        } else {
            System.out.println("Scene image missing: " + sceneImages[index]);
        }

        for (Collectable c : collectables) {
            c.sprite.setVisible(c.sceneIndex == index && !c.pickedUp);
        }

        // ---- Ambience: rain outside (porch + rooftop), indoor hum inside ----
        boolean isOutside = (index == SCENE_PORCH) || (index == SCENE_CRIME);
        String newAmbience = isOutside
                ? "/audio/sfx/ambience/rain_loop.wav"
                : "/audio/sfx/ambience/indoor_hum.wav";

        // Only restart if we're switching to a *different* track.
        if (!newAmbience.equals(ambiencePath)) {
            if (ambienceClip != null) {
                ambienceClip.stop();
                ambienceClip = null;
            }
            ambienceClip = AudioCache.loop(newAmbience);
            ambiencePath = newAmbience;
        }
    }

    /**
     * Moves to the previous ({@code -1}) or next ({@code +1}) scene,
     * wrapping around at the ends. Blocked while a transition is in flight.
     */
    private void navigateScene(int direction) {
        if (isGameOver) return;
        if (pauseMenu != null && pauseMenu.isPaused()) return;
        if (TransitionOverlay.isPlaying()) return;
        if (sceneImages.length == 0) return;
        int next = (currentScene + direction + sceneImages.length) % sceneImages.length;
        AudioCache.play("/audio/sfx/gameplay/thud.wav");
        showScene(next);
    }

    /** Fades the black overlay out from full opacity to transparent. */
    private void fadeIn() {
        if (fadeOverlay == null) return;

        final long startTime = System.currentTimeMillis();
        final int DURATION_MS = 1200;

        javax.swing.Timer t = new javax.swing.Timer(16, null);
        t.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1f, elapsed / (float) DURATION_MS);

            fadeOverlay.setAlpha(1f - progress);

            if (progress >= 1f) {
                ((javax.swing.Timer) e.getSource()).stop();
                contentPanel.remove(fadeOverlay);
                fadeOverlay = null;
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        });
        t.start();
    }

    /**
     * Unlocks or re-locks the INTERROGATE menu label. When unlocking for the
     * first time, plays the tape-fall animation and attaches the click/hover
     * listener.
     */
    private void setInterrogateUnlocked(boolean unlocked) {
        interrogateUnlocked = unlocked;

        if (unlocked) {
            GameState.setTapeAnimationPlayed(true);
            AudioCache.play("/audio/sfx/gameplay/tape_ripping.wav");

            if (policeTape != null && policeTape.getParent() != null) {
                playTapeFallAnimation();
            }

            interrogateLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            if (!interrogateListenerAttached) {
                attachInterrogateListener();
                interrogateListenerAttached = true;
            }
        } else {
            if (policeTape != null) policeTape.setVisible(true);
        }
    }

    /** Attaches hover and click handlers to the INTERROGATE menu label. */
    private void attachInterrogateListener() {
        final javax.swing.JLabel lbl = interrogateLabel;

        lbl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                AudioCache.play("/audio/sfx/ui/hover.wav"); // sound only
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (TransitionOverlay.isPlaying()) return;
                openInterrogate();
            }
        });
    }

    /**
     * Plays the tape-fall animation on {@code policeTape}: removes the real
     * label from the layout, spawns a "ghost" label on the layered pane, and
     * animates it falling with gravity and a slight sine-wave sway before
     * removing it.
     */
    private void playTapeFallAnimation() {
        if (policeTape == null) return;

        // Capture geometry in contentPanel's coordinate space (which the
        // layered pane uses), then convert to the frame's content-pane coords.
        final java.awt.Point originInPanel =
                javax.swing.SwingUtilities.convertPoint(policeTape.getParent(), 0, 0, contentPanel);
        final int baseX = policeTape.getX() + originInPanel.x;
        final int baseY = policeTape.getY() + originInPanel.y;
        final int w = policeTape.getWidth();
        final int h = policeTape.getHeight();

        final javax.swing.Icon originalIcon = policeTape.getIcon();

        // Remove the real label so it doesn't stay behind.
        java.awt.Container parent = policeTape.getParent();
        if (parent != null) parent.remove(policeTape);
        contentPanel.revalidate();
        contentPanel.repaint();

        // Ghost label on the layered pane — free positioning, no layout manager.
        final javax.swing.JLabel ghost = new javax.swing.JLabel(originalIcon);
        ghost.setBounds(baseX, baseY, w, h);
        ghost.setOpaque(false);

        javax.swing.JLayeredPane layered = getRootPane().getLayeredPane();
        layered.add(ghost, javax.swing.JLayeredPane.POPUP_LAYER);
        layered.repaint();

        final long start    = System.currentTimeMillis();
        final int  DURATION = 900;

        javax.swing.Timer timer = new javax.swing.Timer(16, null);
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - start;
            float p = Math.min(1f, elapsed / (float) DURATION);

            float fall = p * p; // gravity ease-in
            int dy = (int) (fall * (getContentPane().getHeight() - baseY + h));
            int dx = (int) (Math.sin(p * Math.PI * 2.5) * 18 * (1 - p * 0.5));
            double angle = Math.toRadians(-8 * p);

            // Rotate the icon on a buffer, then set it as the ghost's icon.
            java.awt.image.BufferedImage buf = new java.awt.image.BufferedImage(
                    w + 80, h + 80, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2 = buf.createGraphics();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.rotate(angle, buf.getWidth() / 2.0, buf.getHeight() / 2.0);
            originalIcon.paintIcon(ghost, g2, 40, 40);
            g2.dispose();

            ghost.setIcon(new javax.swing.ImageIcon(buf));
            ghost.setBounds(baseX + dx - 40,
                    baseY + dy - 40,
                    buf.getWidth(), buf.getHeight());

            layered.repaint();

            if (p >= 1f) {
                timer.stop();
                layered.remove(ghost);
                layered.repaint();
            }
        });
        timer.start();
    }

    /**
     * Registers a collectable item: builds its mask, stores it in the
     * {@link #collectables} list, restores its picked-up state if it was
     * already collected in a previous visit, and applies initial visibility.
     */
    private void makeCollectable(javax.swing.JLabel sprite, int sceneIndex, String id) {
        Collectable c = new Collectable(sprite, sceneIndex, id, iconToMask(sprite.getIcon()));
        c.pickedUp = GameState.isCollected(id);
        collectables.add(c);

        sprite.setVisible(sceneIndex == currentScene && !c.pickedUp);
    }

    /** Returns the {@link Collectable} wrapping the given sprite, or {@code null}. */
    private Collectable findCollectable(javax.swing.JLabel sprite) {
        for (Collectable c : collectables) {
            if (c.sprite == sprite) return c;
        }
        return null;
    }

    /**
     * Returns the topmost visible collectable whose icon has an opaque pixel
     * within {@code radius} pixels of {@code p} (panel coordinates), or
     * {@code null} if none matches.
     */
    private Collectable pickItem(java.awt.Point p) {
        // Iterate component list in index order (0 = topmost). For each
        // label that is a collectable sprite, hit-test against its mask.
        for (int i = 0; i < contentPanel.getComponentCount(); i++) {
            java.awt.Component comp = contentPanel.getComponent(i);
            if (!(comp instanceof javax.swing.JLabel)) continue;
            if (!comp.isVisible()) continue;

            Collectable c = findCollectable((javax.swing.JLabel) comp);
            if (c == null) continue;
            if (c.mask == null) continue;

            int lx = p.x - comp.getX();
            int ly = p.y - comp.getY();
            if (opaqueNear(c.mask, lx, ly, 8)) return c;
        }
        return null;
    }

    /** Returns true if any pixel within {@code r} of (cx,cy) has alpha > 10. */
    private static boolean opaqueNear(java.awt.image.BufferedImage m,
                                      int cx, int cy, int r) {
        int x0 = Math.max(0, cx - r);
        int y0 = Math.max(0, cy - r);
        int x1 = Math.min(m.getWidth()  - 1, cx + r);
        int y1 = Math.min(m.getHeight() - 1, cy + r);
        for (int y = y0; y <= y1; y++)
            for (int x = x0; x <= x1; x++)
                if (((m.getRGB(x, y) >>> 24) & 0xff) > 10) return true;
        return false;
    }

    /**
     * Runs the pickup: sound, state, animation, hide. Called from the panel's
     * mousePressed handler when pickItem() returns a hit.
     */
    private void collect(Collectable c) {
        System.out.println("CLICK on " + c.id
                + " at " + c.sprite.getX() + "," + c.sprite.getY());

        AudioCache.play("/audio/sfx/gameplay/pickup.wav");

        c.pickedUp = true;
        GameState.markCollected(c.id);

        if (!interrogateUnlocked && GameState.isFullyComplete()) {
            setInterrogateUnlocked(true);
        }

        java.awt.Point itemP = javax.swing.SwingUtilities.convertPoint(
                c.sprite, c.sprite.getWidth() / 2, c.sprite.getHeight() / 2,
                CrimeScene.this.getContentPane());

        java.awt.Point invP = javax.swing.SwingUtilities.convertPoint(
                inventoryLabel, inventoryLabel.getWidth() / 2,
                inventoryLabel.getHeight() / 2,
                CrimeScene.this.getContentPane());

        PickupAnimation.play(CrimeScene.this,
                c.sprite.getIcon(),
                itemP.x, itemP.y,
                invP.x,  invP.y);

        c.sprite.setVisible(false);
    }

    /**
     * Returns the icon rendered as an ARGB {@link java.awt.image.BufferedImage}
     * for per-pixel hit-testing. Reuses the underlying image when possible.
     */
    private java.awt.image.BufferedImage iconToMask(javax.swing.Icon icon) {
        if (icon == null) return null;
        int w = icon.getIconWidth(), h = icon.getIconHeight();
        if (w <= 0 || h <= 0) return null;

        if (icon instanceof javax.swing.ImageIcon
                && ((javax.swing.ImageIcon) icon).getImage() instanceof java.awt.image.BufferedImage) {
            return (java.awt.image.BufferedImage) ((javax.swing.ImageIcon) icon).getImage();
        }

        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();
        return img;
    }

    /** Returns {@code true} if every collectable item has been picked up. */
    private boolean allItemsCollected() {
        return GameState.allCollected();
    }

    @Override
    public void dispose() {
        if (ambienceClip != null) {
            ambienceClip.stop();
            ambienceClip = null;
        }
        super.dispose();
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

    private void showGameOver() {
        isGameOver = true;

        // 1. Reveal the game-over UI.
        gameoverLabel.setVisible(true);
        restartButton.setVisible(true);
        menuButton.setVisible(true);

        // 2. Fade the black overlay out (starts fully opaque).
        FadeOverlay fade = new FadeOverlay();
        fade.setAlpha(1f);
        contentPanel.add(fade,
                new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1150, 680));
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

        // 3. Lock the game: hide pause overlay + drop all keyboard shortcuts.
        pauseMenu.hideOverlay();
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).clear();
        getRootPane().getActionMap().clear();

        AudioCache.play("/audio/sfx/ui/splatter.wav");
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
        java.awt.EventQueue.invokeLater(() -> new CrimeScene().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel backgroundLabel;
    private javax.swing.JLabel belt;
    private javax.swing.JLabel boots;
    private javax.swing.JLabel caseFileLabel;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JLabel dragpath;
    private javax.swing.JLabel drum;
    private javax.swing.JLabel footmarks;
    private javax.swing.JLabel gameoverLabel;
    private javax.swing.JLabel interrogateLabel;
    private javax.swing.JLabel inventoryLabel;
    private javax.swing.JLabel ledger;
    private javax.swing.JLabel loveLetters;
    private javax.swing.JLabel menuButton;
    private javax.swing.JLabel mug;
    private javax.swing.JLabel nextButton;
    private javax.swing.JLabel notesLabel;
    private javax.swing.JLabel padlock;
    private javax.swing.JLabel phone;
    private javax.swing.JLabel policeTape;
    private javax.swing.JLabel prescription;
    private javax.swing.JLabel prevButton;
    private javax.swing.JLabel rag;
    private javax.swing.JLabel restartButton;
    private javax.swing.JLabel suspectsLabel;
    private javax.swing.JLabel toolbox;
    private javax.swing.JLabel usb;
    // End of variables declaration//GEN-END:variables
}
