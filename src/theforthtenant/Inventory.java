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
    
        // ---- Slot model ----

        /**
         * One inventory slot: owns its label, its current item id (null = empty),
         * and the two icons it can show (normal / hover). All per-slot state lives
         * here, so the frame never has to consult a parallel map or client property.
         */
        private static final class Slot {
            final javax.swing.JLabel label;
            final int index;              // 0-based position, row-major
            String itemId;                // null when empty
            javax.swing.ImageIcon normal; // shown when not hovered
            javax.swing.ImageIcon hover;  // shown while hovered

            Slot(javax.swing.JLabel label, int index) {
                this.label = label;
                this.index = index;
            }

            boolean isEmpty() { return itemId == null; }

            /** Paints the non-hover icon. Safe to call at any time. */
            void showNormal() {
                if (normal != null) label.setIcon(normal);
            }

            /** Paints the hover icon. Safe to call at any time. */
            void showHover() {
                if (hover != null) label.setIcon(hover);
            }
        }

        // ---- Fields ----
        private Slot[] slots;

        // Shared icons for the empty state — every slot uses the same pair.
        private javax.swing.ImageIcon emptyIcon;
        private javax.swing.ImageIcon emptyHoverIcon;

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
        javax.swing.JLabel[] slotLabels = {
            slot1,  slot2,  slot3,  slot4,  slot5,
            slot6,  slot7,  slot8,  slot9,  slot10,
            slot11, slot12, slot13, slot14, slot15,
            slot16, slot17, slot18, slot19, slot20
        };
        slots = new Slot[slotLabels.length];
        for (int i = 0; i < slotLabels.length; i++) {
            slots[i] = new Slot(slotLabels[i], i);
            slots[i].normal = emptyIcon;
            slots[i].hover  = emptyHoverIcon;
            wireSlot(slots[i]);
        }
        
        // ---- Right-side description panel ----
        descriptionPanel = new DescriptionPanel();
        contentPanel.add(descriptionPanel,
            new org.netbeans.lib.awtextra.AbsoluteConstraints(815, 95, 310, 500));
        contentPanel.setComponentZOrder(descriptionPanel, 1); 
        
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
                AudioCache.play("/audio/sfx/ui/zipper.wav");
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
                AudioCache.play("/audio/sfx/ui/zipper.wav");
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

        contentPanel = new javax.swing.JPanel();
        combineResultSlot = new javax.swing.JLabel();
        combineSlotB = new javax.swing.JLabel();
        combineSlotA = new javax.swing.JLabel();
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
        backgroundLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        contentPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        combineResultSlot.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(combineResultSlot, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 510, -1, -1));

        combineSlotB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(combineSlotB, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 510, -1, -1));

        combineSlotA.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(combineSlotA, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 510, -1, -1));

        slot20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot20, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 380, -1, -1));

        slot19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot19, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 380, -1, -1));

        slot18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot18, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 380, -1, -1));

        slot17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot17, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 380, -1, -1));

        slot16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot16, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 380, -1, -1));

        slot15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot15, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 270, -1, -1));

        slot14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot14, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 270, -1, -1));

        slot13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot13, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 270, -1, -1));

        slot12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot12, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 270, -1, -1));

        slot11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot11, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 270, -1, -1));

        slot10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot10, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 160, -1, -1));

        slot9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot9, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 160, -1, -1));

        slot8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot8, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 160, -1, -1));

        slot7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot7, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 160, -1, -1));

        slot6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot6, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 160, -1, -1));

        slot5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot5, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 50, -1, -1));

        slot4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot4, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 50, -1, -1));

        slot3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot3, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 50, -1, -1));

        slot2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot2, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 50, -1, -1));

        slot1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(slot1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 50, -1, -1));

        backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ui/back_button.png"))); // NOI18N
        contentPanel.add(backButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 620, -1, 50));

        backgroundLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/Inventory.png"))); // NOI18N
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
    
    /** Closes this Inventory and returns to the CrimeScene. */
    private void closeInventory() {
        if (closing) return;
        closing = true;

        CrimeScene cs = new CrimeScene();
        cs.setLocation(getLocation());
        cs.setVisible(true);
        Inventory.this.dispose();
    }
    
        // =========================================================
        // Slot behavior
        // =========================================================

        /** Wires hover + click listeners to a single slot. */
        private void wireSlot(final Slot slot) {
            final javax.swing.JLabel s = slot.label;

            s.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    slot.showHover();
                    s.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                }

                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    slot.showNormal();
                    s.setCursor(java.awt.Cursor.getDefaultCursor());
                }

                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    AudioCache.play("/audio/sfx/ui/select.wav");
                    if (slot.isEmpty()) {
                        descriptionPanel.showEmpty();
                    } else {
                        descriptionPanel.setItem(ItemDatabase.get(slot.itemId));
                    }
                }
            });
        }

        /** Clears all slots, then fills them in order with the given ids. */
        private void refreshSlots(java.util.List<String> collectedIds) {
            if (slots == null) return;
            for (Slot slot : slots) setSlotItem(slot, null);
            if (collectedIds == null) return;

            int n = Math.min(collectedIds.size(), slots.length);
            for (int i = 0; i < n; i++) {
                setSlotItem(slots[i], collectedIds.get(i));
            }
        }

        /**
         * Puts an item into the given slot (or empties it if {@code id} is null).
         * Loads slot_<id>.png and highlighted_slot_<id>.png, falling back to the
         * shared empty icons when the id is null or the art is missing.
         */
        private void setSlotItem(Slot slot, String id) {
            if (slot == null) return;

            if (id == null) {
                slot.itemId = null;
                slot.normal = emptyIcon;
                slot.hover  = emptyHoverIcon;
                slot.showNormal();
                return;
            }

            java.net.URL normalUrl = getClass().getResource(
                    "/Images/gameplay/inventory/item_slots/slot_" + id + ".png");
            if (normalUrl == null) {
                System.out.println("Missing item icon for: " + id);
                return;
            }

            java.net.URL hoverUrl = getClass().getResource(
                    "/Images/gameplay/inventory/item_slots/highlighted_slot_" + id + ".png");

            slot.itemId = id;
            slot.normal = new javax.swing.ImageIcon(normalUrl);
            slot.hover  = (hoverUrl != null)
                    ? new javax.swing.ImageIcon(hoverUrl)
                    : slot.normal;    // no hover art — fall back to the normal icon
            slot.showNormal();
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
    private javax.swing.JLabel backButton;
    private javax.swing.JLabel backgroundLabel;
    private javax.swing.JLabel combineResultSlot;
    private javax.swing.JLabel combineSlotA;
    private javax.swing.JLabel combineSlotB;
    private javax.swing.JPanel contentPanel;
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
