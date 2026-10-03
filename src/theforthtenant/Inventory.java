/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package theforthtenant;

/**
 *
 * @author yuji
 */
public class Inventory extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Inventory.class.getName());
    
    // ---- Slot access ----
    private javax.swing.JLabel[] slots;              
    private javax.swing.ImageIcon emptyIcon; 
    private javax.swing.ImageIcon emptyHoverIcon;      
    private final java.util.Set<javax.swing.JLabel> filledSlots =
        java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>()); 
    
    private boolean closing = false; 
    private PauseMenu pauseMenu;
    private OptionsMenu optionsMenu;
    private DescriptionPanel descriptionPanel;
    
    /**
     * Creates new form Inventory
     */
    public Inventory() {
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
        
        // ---- Cache the empty slot icon ----
        emptyIcon = new javax.swing.ImageIcon(
            getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"));
        
        // Empty-slot hover
        java.net.URL emptyHoverUrl = getClass().getResource(
                "/Images/gameplay/inventory/item_slots/highlighted_slot_empty.png");
        emptyHoverIcon = (emptyHoverUrl != null)
                ? new javax.swing.ImageIcon(emptyHoverUrl)
                : emptyIcon;

        // ---- Wire up slot labels (row-major: slot1 = top-left) ----
        slots = new javax.swing.JLabel[] {
            slot1,  slot2,  slot3,  slot4,  slot5,
            slot6,  slot7,  slot8,  slot9,  slot10,
            slot11, slot12, slot13, slot14, slot15,
            slot16, slot17, slot18, slot19, slot20
        };
        
        // ---- Right-side description panel ----
        descriptionPanel = new DescriptionPanel();
        jPanel1.add(descriptionPanel,
            new org.netbeans.lib.awtextra.AbsoluteConstraints(815, 95, 310, 500));
        jPanel1.setComponentZOrder(descriptionPanel, 1); 
        
        // ---- Hover highlight for each slot ----
        for (javax.swing.JLabel slot : slots) {
            final javax.swing.JLabel s = slot;

            s.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    String id = (String) s.getClientProperty("itemId");

                    if (id == null) {
                        s.setIcon(emptyHoverIcon);
                    } else {
                        javax.swing.ImageIcon hover = loadFilledHoverIcon(id);
                        if (hover != null) {
                            s.setIcon(hover);
                        } else {
                            // No highlighted art for this id — fall back to the base item icon
                            // so the slot doesn't look stuck or unchanged.
                            System.out.println("No hover art for id: " + id);
                        }
                    }
                    s.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                }

                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    restoreSlotIcon(s);
                    s.setCursor(java.awt.Cursor.getDefaultCursor());
                }
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    String id = (String) s.getClientProperty("itemId");
                    AudioCache.play("/audio/sfx/ui/select.wav");
                    if (id == null) {
                        descriptionPanel.showEmpty();
                    } else {
                        descriptionPanel.setItem(ItemDatabase.get(id));
                    }
                }
            });
        }
        
        // ---- Back button ----
        final javax.swing.ImageIcon backNormal = new javax.swing.ImageIcon(
                getClass().getResource("/Images/ui/back_button.png"));
        final javax.swing.ImageIcon backHover = new javax.swing.ImageIcon(
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
                AudioCache.play("/audio/sfx/ui/select.wav");
                closeInventory();
            }
        });
        
        // ---- Options popup ----
        optionsMenu = OptionsMenu.attachTo(this, new OptionsMenu.Callbacks() {
            @Override public void onMusicChanged(int percent) {
                // Inventory has no music — nothing to do.
            }
            @Override public void onSoundChanged(int percent) {
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void onClose() {
                pauseMenu.showPopupOnly();   // bring back the pause popup
            }
        });

        // ---- Pause menu ----
        pauseMenu = PauseMenu.attachTo(this, PauseMenu.Corner.TOP_LEFT, new PauseMenu.Callbacks() {
            @Override public void onPause() {
                // Nothing to pause in Inventory — hover sounds are fire-and-forget.
            }
            @Override public void onResume() {
                // Nothing to resume.
            }
            @Override public void onOptions() {
                pauseMenu.hidePopupOnly();
                optionsMenu.show();
            }
            @Override public void onMainMenu() {
                if (TransitionOverlay.isPlaying()) return;

                pauseMenu.hideOverlay();

                TransitionOverlay.play(Inventory.this, () -> {
                    MainMenu menu = new MainMenu();
                    menu.setLocation(getLocation());
                    menu.setVisible(true);
                    Inventory.this.dispose();
                });
            }
        });
        
        // ---- Keyboard shortcut: E returns to CrimeScene ----
        javax.swing.JRootPane root = getRootPane();
        javax.swing.KeyStroke backKey =
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, 0);
        root.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(backKey, "closeInventoryKey");
        root.getActionMap().put("closeInventoryKey", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                AudioCache.play("/audio/sfx/ui/select.wav");
                closeInventory();
            }
        });
        
        refreshSlots(GameState.getCollected());
        descriptionPanel.showEmpty();
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
        slot20 = new javax.swing.JLabel();
        slot19 = new javax.swing.JLabel();
        slot18 = new javax.swing.JLabel();
        slot17 = new javax.swing.JLabel();
        slot16 = new javax.swing.JLabel();
        slot15 = new javax.swing.JLabel();
        slot14 = new javax.swing.JLabel();
        slot13 = new javax.swing.JLabel();
        slot12 = new javax.swing.JLabel();
        slot11 = new javax.swing.JLabel();
        slot10 = new javax.swing.JLabel();
        slot9 = new javax.swing.JLabel();
        slot8 = new javax.swing.JLabel();
        slot7 = new javax.swing.JLabel();
        slot6 = new javax.swing.JLabel();
        slot5 = new javax.swing.JLabel();
        slot4 = new javax.swing.JLabel();
        slot3 = new javax.swing.JLabel();
        slot2 = new javax.swing.JLabel();
        slot1 = new javax.swing.JLabel();
        backButton = new javax.swing.JLabel();
        Background = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        slot20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot20, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 380, -1, -1));

        slot19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot19, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 380, -1, -1));

        slot18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot18, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 380, -1, -1));

        slot17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot17, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 380, -1, -1));

        slot16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot16, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 380, -1, -1));

        slot15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot15, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 270, -1, -1));

        slot14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot14, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 270, -1, -1));

        slot13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot13, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 270, -1, -1));

        slot12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot12, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 270, -1, -1));

        slot11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot11, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 270, -1, -1));

        slot10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot10, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 160, -1, -1));

        slot9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot9, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 160, -1, -1));

        slot8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot8, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 160, -1, -1));

        slot7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot7, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 160, -1, -1));

        slot6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot6, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 160, -1, -1));

        slot5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot5, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 50, -1, -1));

        slot4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot4, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 50, -1, -1));

        slot3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot3, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 50, -1, -1));

        slot2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot2, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 50, -1, -1));

        slot1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        jPanel1.add(slot1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 50, -1, -1));

        backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ui/back_button.png"))); // NOI18N
        jPanel1.add(backButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 620, -1, 50));

        Background.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/Inventory.png"))); // NOI18N
        jPanel1.add(Background, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

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
    
    /**
    * Puts an item into the given slot (0..19). Pass null to empty it.
    * Loads /Images/gameplay/inventory/item_slots/slot_<id>.png.
    */
   private void setSlotItem(int slotIndex, String id) {
        if (slots == null) return;
        if (slotIndex < 0 || slotIndex >= slots.length) return;

        javax.swing.JLabel lbl = slots[slotIndex];
        if (id == null) {
            lbl.setIcon(emptyIcon);
            lbl.putClientProperty("itemId", null);
            filledSlots.remove(lbl);           
            return;
        }

        java.net.URL url = getClass().getResource(
                "/Images/gameplay/inventory/item_slots/slot_" + id + ".png");
        if (url == null) {
            System.out.println("Missing item icon for: " + id);
            return;
        }
        lbl.setIcon(new javax.swing.ImageIcon(url));
        lbl.putClientProperty("itemId", id);
        filledSlots.add(lbl);                  
    }

   /** Clears all slots, then fills them in order with the given ids. */
    private void refreshSlots(java.util.List<String> collectedIds) {
        if (slots == null) return;
        filledSlots.clear();                    
        for (javax.swing.JLabel lbl : slots) {
            lbl.setIcon(emptyIcon);
            lbl.putClientProperty("itemId", null);
        }
        if (collectedIds == null) return;
        int n = Math.min(collectedIds.size(), slots.length);
        for (int i = 0; i < n; i++) setSlotItem(i, collectedIds.get(i));
    }
    
    /** Loads highlighted_slot_<id>.png for a filled slot's hover state. */
    private javax.swing.ImageIcon loadFilledHoverIcon(String id) {
        if (id == null) return null;
        java.net.URL url = getClass().getResource(
                "/Images/gameplay/inventory/item_slots/highlighted_slot_" + id + ".png");
        if (url == null) {
            System.out.println("Missing hover icon for: " + id);
            return null;
        }
        return new javax.swing.ImageIcon(url);
    }
    
    /** Closes this Inventory and returns to the CrimeScene. */
    private void closeInventory() {
        if (closing) return;
        closing = true;

        CrimeScene cs = new CrimeScene();
        cs.setLocation(getLocation());
        cs.setVisible(true);
        Inventory.this.dispose();
    }
    
    /** Restores the non-highlighted icon for the given slot, based on its current itemId. */
    private void restoreSlotIcon(javax.swing.JLabel s) {
        String id = (String) s.getClientProperty("itemId");
        if (id == null) {
            s.setIcon(emptyIcon);
            return;
        }
        java.net.URL url = getClass().getResource(
                "/Images/gameplay/inventory/item_slots/slot_" + id + ".png");
        if (url != null) {
            s.setIcon(new javax.swing.ImageIcon(url));
        } else {
            System.out.println("Cannot restore icon for: " + id);
        }
    }
    
    /** Returns a darkened copy of the given image (used for hover states). */
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
           java.awt.EventQueue.invokeLater(() -> new Inventory().setVisible(true));
       }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Background;
    private javax.swing.JLabel backButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel slot1;
    private javax.swing.JLabel slot10;
    private javax.swing.JLabel slot11;
    private javax.swing.JLabel slot12;
    private javax.swing.JLabel slot13;
    private javax.swing.JLabel slot14;
    private javax.swing.JLabel slot15;
    private javax.swing.JLabel slot16;
    private javax.swing.JLabel slot17;
    private javax.swing.JLabel slot18;
    private javax.swing.JLabel slot19;
    private javax.swing.JLabel slot2;
    private javax.swing.JLabel slot20;
    private javax.swing.JLabel slot3;
    private javax.swing.JLabel slot4;
    private javax.swing.JLabel slot5;
    private javax.swing.JLabel slot6;
    private javax.swing.JLabel slot7;
    private javax.swing.JLabel slot8;
    private javax.swing.JLabel slot9;
    // End of variables declaration//GEN-END:variables
}
