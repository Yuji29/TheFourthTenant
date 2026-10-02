/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package theforthtenant;

/**
 *
 * @author yuji
 */
public class CrimeScene extends javax.swing.JFrame {
    
    // ---- Background scene rotation ----
    private final String[] sceneImages = {
        "/Images/GameScreen/locations/CrimeScene.png",
        "/Images/GameScreen/locations/FrontPorch.png",
        "/Images/GameScreen/locations/LaundryRoom.png",
        "/Images/GameScreen/locations/Kitchen.png",
        "/Images/GameScreen/locations/LivingRoom.png",
        "/Images/GameScreen/locations/DiningArea.png",
        "/Images/GameScreen/locations/BedRoom.png",
    };

    private int currentScene = 0;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CrimeScene.class.getName());
    private final java.util.Map<javax.swing.JLabel, Integer> itemScene = new java.util.HashMap<>();
    private PauseMenu pauseMenu;
    private OptionsMenu optionsMenu;
    private boolean interrogateUnlocked = false;
    private static final int SCENE_CRIME   = 0;
    private static final int SCENE_PORCH   = 1;
    private static final int SCENE_LAUNDRY = 2;
    private static final int SCENE_KITCHEN = 3;
    private static final int SCENE_LIVING = 4;
    private static final int SCENE_DINING = 5;
    private static final int SCENE_BED = 6;

    /**
     * Creates new form CrimeScene
     */
    public CrimeScene() {
        initComponents();
        
        
        itemScene.put(padlock,      SCENE_CRIME);
        itemScene.put(belt,         SCENE_CRIME);
        itemScene.put(dragpath,     SCENE_CRIME);
        itemScene.put(phone,        SCENE_CRIME);
        itemScene.put(footmarks,    SCENE_CRIME);
        itemScene.put(drum,         SCENE_CRIME);
        itemScene.put(usb,          SCENE_PORCH);
        itemScene.put(boots,        SCENE_LAUNDRY);
        itemScene.put(mug,          SCENE_KITCHEN);
        itemScene.put(toolbox,      SCENE_LIVING);
        itemScene.put(prescription, SCENE_DINING);
        itemScene.put(rag,          SCENE_BED);
        
        showScene(0);
        
        // Icon (optional, match Backstory)
        try {
            java.awt.Image icon = javax.imageio.ImageIO.read(
                getClass().getResourceAsStream("/Images/logo.png"));
            setIconImage(icon);
        } catch (Exception e) {
            System.out.println("Logo not found: " + e.getMessage());
        }

        setTitle("The Fourth Tenant");
        setResizable(false);
        setLocationRelativeTo(null);
        
        // ---- Style the menu labels ----
        java.awt.Font menuFont = getCustomFont(15f);

        final java.awt.Color NORMAL_COLOR = java.awt.Color.WHITE;
        final java.awt.Color HOVER_COLOR = new java.awt.Color(245, 235, 190); 

        javax.swing.JLabel[] menuLabels = { jLabel2, jLabel3, jLabel4, jLabel5, jLabel6 };
        String[] menuText = { "INVENTORY", "SUSPECTS", "NOTES", "INTERROGATE", "CASE FILE" };

        for (int i = 0; i < menuLabels.length; i++) {
            final javax.swing.JLabel lbl = menuLabels[i];
            final int index = i;   // ← keep for scene index later

            // --- static styling (applies to all) ---
            lbl.setText(menuText[i]);
            lbl.setFont(menuFont);
            lbl.setForeground(NORMAL_COLOR);
            lbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lbl.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
            lbl.setOpaque(false);

            applyTextOutline(lbl);

            // INTERROGATE (index 3) starts locked — no hover, no cursor
            final boolean locked = (index == 3) && !interrogateUnlocked;

            lbl.setCursor(new java.awt.Cursor(
                locked ? java.awt.Cursor.DEFAULT_CURSOR : java.awt.Cursor.HAND_CURSOR));

            if (!locked) {
                lbl.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                        lbl.setForeground(HOVER_COLOR);
                    }
                    @Override public void mouseExited(java.awt.event.MouseEvent e) {
                        lbl.setForeground(NORMAL_COLOR);
                    }
                    @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                        System.out.println(menuText[index] + " clicked");
                        // add per-label click behavior here later
                    }
                });
            }
        }

        // CASE FILE is the highlighted action — keep it at 15f (or bump to taste)
        jLabel6.setFont(getCustomFont(15f));

        // ---- Options popup ----
        optionsMenu = OptionsMenu.attachTo(this, new OptionsMenu.Callbacks() {
            @Override public void onMusicChanged(int percent) {
                // CrimeScene has no music — nothing to do here.
            }
            @Override public void onSoundChanged(int percent) {
                // SfxManager is already updated by OptionsMenu itself.
                SfxManager.playOneShot(CrimeScene.this.getClass(), "/audio/hover.wav");
            }
            @Override public void onClose() {
                // Bring back the pause popup
                pauseMenu.showPopupOnly();
            }
        });

        // ---- Pause menu ----
        pauseMenu = PauseMenu.attachTo(this, PauseMenu.Corner.TOP_LEFT, new PauseMenu.Callbacks() {
            @Override public void onPause() {
                // CrimeScene has no audio to stop (yet). If you add ambience later,
                // stop it here.
            }
            @Override public void onResume() {
                // CrimeScene has no audio to resume (yet). If you add ambience later,
                // resume it here.
            }
            @Override public void onOptions() {
                pauseMenu.hidePopupOnly();
                optionsMenu.show();
            }
            @Override public void onMainMenu() {
                pauseMenu.hideOverlay();

                // Transition back to MainMenu
                TransitionOverlay.play(CrimeScene.this, () -> {
                    MainMenu menu = new MainMenu();
                    menu.setLocation(getLocation());
                    menu.setVisible(true);
                    CrimeScene.this.dispose();
                });
            }
        });
        
        // ---- Scene navigation (keyboard) ----
        javax.swing.JRootPane root = getRootPane();
        javax.swing.InputMap im = root.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap am = root.getActionMap();

        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0),  "scenePrev");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), "sceneNext");

        am.put("scenePrev", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                navigateScene(-1);
            }
        });
        am.put("sceneNext", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                navigateScene(+1);
            }
        });
        
        // ---- Arrow buttons: prev (jLabel7) / next (jLabel8) ----

        // Load normal + darkened icons (darken() helper shown below)
        final javax.swing.ImageIcon prevNormal = new javax.swing.ImageIcon(
                getClass().getResource("/Images/previous_button.png"));
        final javax.swing.ImageIcon nextNormal = new javax.swing.ImageIcon(
                getClass().getResource("/Images/next_button.png"));
        final javax.swing.ImageIcon prevDark = new javax.swing.ImageIcon(
                darken(prevNormal.getImage()));
        final javax.swing.ImageIcon nextDark = new javax.swing.ImageIcon(
                darken(nextNormal.getImage()));

        // jLabel7 = PREV
        jLabel7.setIcon(prevNormal);
        jLabel7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                jLabel7.setIcon(prevDark);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                jLabel7.setIcon(prevNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                navigateScene(-1);
            }
        });

        // jLabel8 = NEXT
        jLabel8.setIcon(nextNormal);  
        jLabel8.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                jLabel8.setIcon(nextDark);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                jLabel8.setIcon(nextNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                navigateScene(+1);
            }
        });
        
        // ---- Collectable crime scene items ----
        javax.swing.JLabel[] items = {
            padlock, belt, dragpath, phone, footmarks, drum,
            usb, 
            boots, 
            mug, 
            toolbox, 
            prescription, 
            rag,
        };
        for (javax.swing.JLabel item : items) {
            makeCollectable(item);
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
        rag = new javax.swing.JLabel();
        prescription = new javax.swing.JLabel();
        toolbox = new javax.swing.JLabel();
        mug = new javax.swing.JLabel();
        boots = new javax.swing.JLabel();
        usb = new javax.swing.JLabel();
        padlock = new javax.swing.JLabel();
        belt = new javax.swing.JLabel();
        dragpath = new javax.swing.JLabel();
        phone = new javax.swing.JLabel();
        footmarks = new javax.swing.JLabel();
        drum = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        rag.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/GameScreen/items/rag.png"))); // NOI18N
        jPanel1.add(rag, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 500, -1, -1));

        prescription.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/GameScreen/items/prescription.png"))); // NOI18N
        jPanel1.add(prescription, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 400, -1, -1));

        toolbox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/GameScreen/items/toolbox.png"))); // NOI18N
        jPanel1.add(toolbox, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 370, -1, -1));

        mug.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/GameScreen/items/mug.png"))); // NOI18N
        jPanel1.add(mug, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 380, -1, -1));

        boots.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/GameScreen/items/boots.png"))); // NOI18N
        jPanel1.add(boots, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 370, -1, -1));

        usb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/GameScreen/items/usb.png"))); // NOI18N
        jPanel1.add(usb, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 550, -1, -1));

        padlock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/GameScreen/items/padlock.png"))); // NOI18N
        jPanel1.add(padlock, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 80, -1, -1));

        belt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/GameScreen/items/belt.png"))); // NOI18N
        jPanel1.add(belt, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 400, -1, -1));

        dragpath.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/GameScreen/items/dragpath.png"))); // NOI18N
        jPanel1.add(dragpath, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 270, -1, -1));

        phone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/GameScreen/items/phone.png"))); // NOI18N
        jPanel1.add(phone, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 410, -1, -1));

        footmarks.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/GameScreen/items/footmarks.png"))); // NOI18N
        jPanel1.add(footmarks, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 270, -1, -1));

        drum.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/GameScreen/items/drum.png"))); // NOI18N
        jPanel1.add(drum, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 50, -1, -1));

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/PoliceTape.png"))); // NOI18N
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 610, 250, 70));

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/next_button.png"))); // NOI18N
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(1080, 0, -1, 570));

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/previous_button.png"))); // NOI18N
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 570));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("CASE FILE");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(953, 612, 180, 50));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("INTERROGATE");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(691, 612, 220, 70));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("NOTES");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(464, 612, 220, 70));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("SUSPECTS");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(228, 612, 230, 70));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("INVENTORY");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 612, 220, 70));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/GameScreen/locations/CrimeScene.png"))); // NOI18N
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 680));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    
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
    
    private void applyTextOutline(javax.swing.JLabel label) {
        final int OUTLINE = 2;                                    // thickness in px
        final java.awt.Color OUTLINE_COLOR = java.awt.Color.BLACK;

        label.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            public void paint(java.awt.Graphics g, javax.swing.JComponent c) {
                javax.swing.JLabel l = (javax.swing.JLabel) c;

                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                                    java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                g2.setFont(l.getFont());

                String text = l.getText();
                if (text == null || text.isEmpty()) { g2.dispose(); return; }

                java.awt.FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(text);
                int x = (l.getWidth()  - textW) / 2;
                int y = (l.getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                // outline pass: draw the string in black, offset in every direction
                g2.setColor(OUTLINE_COLOR);
                for (int dx = -OUTLINE; dx <= OUTLINE; dx++) {
                    for (int dy = -OUTLINE; dy <= OUTLINE; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        g2.drawString(text, x + dx, y + dy);
                    }
                }

                // fill pass: draw the actual foreground color on top
                g2.setColor(l.getForeground());
                g2.drawString(text, x, y);

                g2.dispose();
            }
        });
    }
    
    private void showScene(int index) {
        if (sceneImages.length == 0) return;
        if (index < 0) index = 0;
        if (index >= sceneImages.length) index = sceneImages.length - 1;
        currentScene = index;

        java.net.URL url = getClass().getResource(sceneImages[index]);
        if (url != null) {
            jLabel1.setIcon(new javax.swing.ImageIcon(url));
        } else {
            System.out.println("Scene image missing: " + sceneImages[index]);
        }

        for (java.util.Map.Entry<javax.swing.JLabel, Integer> e : itemScene.entrySet()) {
            javax.swing.JLabel item = e.getKey();
            int itemSceneIndex = e.getValue();
            boolean pickedUp = Boolean.TRUE.equals(item.getClientProperty("pickedUp"));
            item.setVisible(itemSceneIndex == index && !pickedUp);
        }
    }

    private void navigateScene(int direction) {
        if (TransitionOverlay.isPlaying()) return;
        if (sceneImages.length == 0) return;
        int next = (currentScene + direction + sceneImages.length) % sceneImages.length;
        showScene(next);
    }
    
    private static java.awt.Image darken(java.awt.Image src) {
        java.awt.image.ImageFilter filter = new java.awt.image.RGBImageFilter() {
            @Override
            public int filterRGB(int x, int y, int rgb) {
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
    
    private void setInterrogateUnlocked(boolean unlocked) {
        interrogateUnlocked = unlocked;

        if (unlocked) {
            if (jLabel9 != null && jLabel9.getParent() != null) {
                playTapeFallAnimation();     // replaces jLabel9.setVisible(false)
            }

            // enable INTERROGATE
            jLabel5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            final javax.swing.JLabel lbl = jLabel5;
            final java.awt.Color NORMAL = java.awt.Color.WHITE;
            final java.awt.Color HOVER  = new java.awt.Color(245, 235, 190);

            lbl.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    lbl.setForeground(HOVER);
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    lbl.setForeground(NORMAL);
                }
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    System.out.println("INTERROGATE clicked");
                    // open interrogate panel here
                }
            });
        } else {
            if (jLabel9 != null) jLabel9.setVisible(true);
        }
    }
    
    private void playTapeFallAnimation() {
        if (jLabel9 == null) return;

        // Capture geometry in jPanel1's coordinate space, then convert to
        // the frame's content-pane coordinates (which the layered pane uses).
        final java.awt.Point originInPanel = javax.swing.SwingUtilities.convertPoint(
                jLabel9.getParent(), 0, 0, jPanel1);
        final int baseX = jLabel9.getX() + originInPanel.x;
        final int baseY = jLabel9.getY() + originInPanel.y;
        final int w     = jLabel9.getWidth();
        final int h     = jLabel9.getHeight();

        final javax.swing.Icon originalIcon = jLabel9.getIcon();

        // Remove the real label from the layout so it doesn't stay behind.
        java.awt.Container parent = jLabel9.getParent();
        if (parent != null) parent.remove(jLabel9);
        jPanel1.revalidate();
        jPanel1.repaint();

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

            float fall = p * p;                                        // gravity ease-in
            int dy = (int) (fall * (getContentPane().getHeight() - baseY + h));
            int dx = (int) (Math.sin(p * Math.PI * 2.5) * 18 * (1 - p * 0.5));
            double angle = Math.toRadians(-8 * p);

            // Rotate the icon on a temporary buffer, then set as the ghost's icon
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

            // Alpha fade via a simple wrapper — use a transparent overlay with
            // setForeground-based trick isn't needed; just remove at end.
            layered.repaint();

            if (p >= 1f) {
                timer.stop();
                layered.remove(ghost);
                layered.repaint();
            }
        });
        timer.start();
    }
    
    private void makeCollectable(javax.swing.JLabel item) {
        tightenHitbox(item);
        item.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {

                // mark as collected FIRST so re-showing the scene keeps it hidden
                item.putClientProperty("pickedUp", Boolean.TRUE);
                
                // NEW: unlock interrogation once everything is collected
                if (allItemsCollected()) {
                    setInterrogateUnlocked(true);
                }

                java.awt.Point itemP = javax.swing.SwingUtilities.convertPoint(
                        item, item.getWidth() / 2, item.getHeight() / 2,
                        CrimeScene.this.getContentPane());

                java.awt.Point invP = javax.swing.SwingUtilities.convertPoint(
                        jLabel2, jLabel2.getWidth() / 2, jLabel2.getHeight() / 2,
                        CrimeScene.this.getContentPane());

                PickupAnimation.play(CrimeScene.this,
                        item.getIcon(),
                        itemP.x, itemP.y,
                        invP.x,  invP.y);

                item.setVisible(false);
            }
        });
    }
    
    /**
    * Tightens a JLabel's mouse hitbox to the bounding box of its opaque
    * (non-transparent) pixels, so clicks outside the artwork pass through.
    */
   private void tightenHitbox(final javax.swing.JLabel item) {
        final java.awt.image.BufferedImage mask = iconToMask(item.getIcon());
        if (mask == null) return;

        item.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            public boolean contains(javax.swing.JComponent c, int x, int y) {
                if (x < 0 || y < 0 || x >= mask.getWidth() || y >= mask.getHeight()) return false;
                int alpha = (mask.getRGB(x, y) >>> 24) & 0xff;
                return alpha > 10;
            }
        });
    }

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

   /**
    * Scans the icon's pixels and returns the tight bounding box of all
    * pixels whose alpha > 0. Returns null if the icon isn't a BufferedImage.
    */
   private java.awt.Rectangle opaqueBounds(javax.swing.Icon icon) {
       if (icon == null) return null;

       java.awt.image.BufferedImage img;
       if (icon instanceof javax.swing.ImageIcon
               && ((javax.swing.ImageIcon) icon).getImage() instanceof java.awt.image.BufferedImage) {
           img = (java.awt.image.BufferedImage) ((javax.swing.ImageIcon) icon).getImage();
       } else {
           // fall back: rasterize the icon
           int w = icon.getIconWidth(), h = icon.getIconHeight();
           if (w <= 0 || h <= 0) return null;
           img = new java.awt.image.BufferedImage(w, h,
                   java.awt.image.BufferedImage.TYPE_INT_ARGB);
           java.awt.Graphics2D g = img.createGraphics();
           icon.paintIcon(null, g, 0, 0);
           g.dispose();
       }

       int w = img.getWidth(), h = img.getHeight();
       int minX = w, minY = h, maxX = -1, maxY = -1;

       int[] row = new int[w];
       for (int y = 0; y < h; y++) {
           img.getRGB(0, y, w, 1, row, 0, w);
           for (int x = 0; x < w; x++) {
               int a = (row[x] >>> 24) & 0xff;
               if (a > 10) {                 // treat very faint pixels as empty
                   if (x < minX) minX = x;
                   if (x > maxX) maxX = x;
                   if (y < minY) minY = y;
                   if (y > maxY) maxY = y;
               }
           }
       }

       if (maxX < 0 || maxY < 0) return null;   // fully transparent image
       return new java.awt.Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
   }
   
   private boolean allItemsCollected() {
        for (javax.swing.JLabel item : itemScene.keySet()) {
            if (!Boolean.TRUE.equals(item.getClientProperty("pickedUp"))) {
                return false;
            }
        }
        return true;
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
    private javax.swing.JLabel belt;
    private javax.swing.JLabel boots;
    private javax.swing.JLabel dragpath;
    private javax.swing.JLabel drum;
    private javax.swing.JLabel footmarks;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel mug;
    private javax.swing.JLabel padlock;
    private javax.swing.JLabel phone;
    private javax.swing.JLabel prescription;
    private javax.swing.JLabel rag;
    private javax.swing.JLabel toolbox;
    private javax.swing.JLabel usb;
    // End of variables declaration//GEN-END:variables
}
