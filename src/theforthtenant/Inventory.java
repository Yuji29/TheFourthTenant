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
            boolean selected = false;

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
            
            /** Paints the appropriate icon given the slot's current state. */
            void refresh() {               // ← add this method
                if (selected) showHover();
                else          showNormal();
            }
        }

        // ---- Fields ----
        private Slot[] slots;

        // Shared icons for the empty state — every slot uses the same pair.
        private javax.swing.ImageIcon emptyIcon;
        private javax.swing.ImageIcon emptyHoverIcon;
        private Slot selectedSlot;
        private String combineItemA;
        private String combineItemB;  
        private javax.swing.ImageIcon emptyCombineIcon;
        private javax.swing.ImageIcon combineAHover;   
        private javax.swing.ImageIcon combineBHover;   
        private javax.swing.JComponent activeToast;
        private javax.swing.ImageIcon phoneLockscreenIcon;
        private javax.swing.ImageIcon phoneUnlockedIcon;

        // ---- PIN state ----
        private final StringBuilder pinBuffer = new StringBuilder();
        private javax.swing.ImageIcon pinEmptyIcon;
        private javax.swing.ImageIcon pinFilledIcon;
        private javax.swing.JLabel[] pinDots;

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
        
        emptyCombineIcon = emptyIcon;
        
        // Empty-slot hover
        java.net.URL emptyHoverUrl = getClass().getResource(
                "/Images/gameplay/inventory/item_slots/highlighted_slot_empty.png");
        emptyHoverIcon = (emptyHoverUrl != null)
                ? new javax.swing.ImageIcon(emptyHoverUrl)
                : emptyIcon;
        
        // ---- PIN dot icons ----
        pinEmptyIcon = new javax.swing.ImageIcon(
            getClass().getResource("/Images/gameplay/inventory/phone_pin_empty.png"));
        pinFilledIcon = new javax.swing.ImageIcon(
            getClass().getResource("/Images/gameplay/inventory/phone_pin_filled.png"));
        
        // ---- Phone screen states ----
        phoneLockscreenIcon = new javax.swing.ImageIcon(
            getClass().getResource("/Images/gameplay/inventory/phone_lockscreen.png"));
        phoneUnlockedIcon = new javax.swing.ImageIcon(
            getClass().getResource("/Images/gameplay/inventory/phone_unlocked.png"));
        phoneScreen.setIcon(phoneLockscreenIcon);

        pinDots = new javax.swing.JLabel[] { pin1, pin2, pin3, pin4 };
        for (javax.swing.JLabel dot : pinDots) {
            dot.setIcon(pinEmptyIcon);
        }
        
        // ---- Inspect button: hidden by default, only shows for the phone ----
        inspectButton.setVisible(false);
        inspectButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        inspectButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                AudioCache.play("/audio/sfx/ui/select.wav");
                openPhoneInspect();
            }
        });

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
        
        // ---- Combine slot A: hover preview + click to remove ----
        combineSlotA.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        combineSlotA.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (combineItemA != null && combineAHover != null) {
                    combineSlotA.setIcon(combineAHover);
                }
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (combineItemA != null) {
                    setCombineSlotIcon(combineSlotA, combineItemA);   // restore normal
                }
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (combineItemA != null) {
                    AudioCache.play("/audio/sfx/ui/select.wav");   // ← new removal sound
                    clearCombineSlot(true);
                }
            }
        });

        // ---- Combine slot B: hover preview + click to remove ----
        combineSlotB.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        combineSlotB.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (combineItemB != null && combineBHover != null) {
                    combineSlotB.setIcon(combineBHover);
                }
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (combineItemB != null) {
                    setCombineSlotIcon(combineSlotB, combineItemB);   // restore normal
                }
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (combineItemB != null) {
                    AudioCache.play("/audio/sfx/ui/select.wav");   // ← new removal sound
                    clearCombineSlot(false);
                }
            }
        });
        
        // Start with empty combine slots.
        clearAllCombineSlots();
        
        // ---- Style the COMBINE button (label on the red ribbon) ----
        combineLabel.setText("COMBINE");
        combineLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combineLabel.setFont(getCustomFont(12f));
        combineLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        final java.awt.Color COMBINE_READY   = new java.awt.Color(255, 230, 160);   // warm gold
        final java.awt.Color COMBINE_DIM     = new java.awt.Color(120, 120, 120);   // muted gray
        final java.awt.Color COMBINE_HOVER   = new java.awt.Color(255, 255, 255);   // white on hover

        combineLabel.setForeground(COMBINE_DIM);   // starts dim

        combineLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (combineItemA != null && combineItemB != null) {
                    combineLabel.setForeground(COMBINE_HOVER);
                }
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                refreshCombineLabel();   // restores dim or gold depending on state
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (combineItemA == null || combineItemB == null) {
                    // Nothing staged — soft error.
                    AudioCache.play("/audio/sfx/ui/click.wav");
                    return;
                }
                AudioCache.play("/audio/sfx/ui/select.wav");
                tryCombine();
            }
        });   
        
        // ---- Right-side description panel ----
        descriptionPanel = new DescriptionPanel();
        contentPanel.add(descriptionPanel,
            new org.netbeans.lib.awtextra.AbsoluteConstraints(815, 95, 310, 500));
        
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
            javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, 0);
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
        
        // ---- Phone lockscreen: hidden by default, only shows for the phone ----
        phoneScrim.setVisible(false);
        phoneScreen.setVisible(false);
        phoneHeading.setVisible(false);
        phoneSubheading.setVisible(false);
        num1.setVisible(false);
        num2.setVisible(false);
        num3.setVisible(false);
        num4.setVisible(false);
        num5.setVisible(false);
        num6.setVisible(false);
        num7.setVisible(false);
        num8.setVisible(false);
        num9.setVisible(false);
        num0.setVisible(false);
        backspace.setVisible(false);
        pin1.setVisible(false);
        pin2.setVisible(false);
        pin3.setVisible(false);
        pin4.setVisible(false);
        
        pin1.addMouseListener(new java.awt.event.MouseAdapter() { });
        pin2.addMouseListener(new java.awt.event.MouseAdapter() { });
        pin3.addMouseListener(new java.awt.event.MouseAdapter() { });
        pin4.addMouseListener(new java.awt.event.MouseAdapter() { });

        phoneScrim.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        phoneScrim.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                closePhoneScreen();
            }
        });
        
        // ---- Phone keypad: swallow clicks so they don't fall through to the scrim ----
        javax.swing.JLabel[] keypad = {
            num1, num2, num3, num4, num5,
            num6, num7, num8, num9, num0
        };
        String[] keypadDigits = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0" };

        for (int i = 0; i < keypad.length; i++) {
            final String digit = keypadDigits[i];
            final javax.swing.JLabel key = keypad[i];

            final javax.swing.ImageIcon normal = new javax.swing.ImageIcon(
                getClass().getResource("/Images/gameplay/inventory/phone_btn" + digit + ".png"));
            java.net.URL hoverUrl = getClass().getResource(
                "/Images/gameplay/inventory/phone_hover_btn" + digit + ".png");
            final javax.swing.ImageIcon hover = (hoverUrl != null)
                ? new javax.swing.ImageIcon(hoverUrl)
                : normal;

            key.setIcon(normal);
            key.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            key.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    key.setIcon(hover);
                    AudioCache.play("/audio/sfx/ui/hover.wav");
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    key.setIcon(normal);
                }
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    AudioCache.play("/audio/sfx/ui/select.wav");
                    onPinDigit(digit);
                }
            });
        }

        // Backspace: swallow the click (real action wired later).
        backspace.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        final javax.swing.ImageIcon bsNormal = new javax.swing.ImageIcon(
            getClass().getResource("/Images/gameplay/inventory/phone_btnbackspace.png"));
        final javax.swing.ImageIcon bsHover = new javax.swing.ImageIcon(
            getClass().getResource("/Images/gameplay/inventory/phone_hover_btnbackspace.png"));

        backspace.setIcon(bsNormal);
        backspace.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                backspace.setIcon(bsHover);
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                backspace.setIcon(bsNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                AudioCache.play("/audio/sfx/ui/select.wav");
                onPinBackspace();
            }
        });

        // Headings and phone image: swallow clicks without doing anything.
        phoneHeading.addMouseListener(new java.awt.event.MouseAdapter() { });
        phoneSubheading.addMouseListener(new java.awt.event.MouseAdapter() { });
        phoneScreen.addMouseListener(new java.awt.event.MouseAdapter() { });

        contentPanel.setComponentZOrder(descriptionPanel, 0);
        contentPanel.setComponentZOrder(inspectButton,    0);
        contentPanel.setComponentZOrder(phoneScrim,       0);
        contentPanel.setComponentZOrder(phoneScreen,      0);
        contentPanel.setComponentZOrder(num1,             0);
        contentPanel.setComponentZOrder(num2,             0);
        contentPanel.setComponentZOrder(num3,             0);
        contentPanel.setComponentZOrder(num4,             0);
        contentPanel.setComponentZOrder(num5,             0);
        contentPanel.setComponentZOrder(num6,             0);
        contentPanel.setComponentZOrder(num7,             0);
        contentPanel.setComponentZOrder(num8,             0);
        contentPanel.setComponentZOrder(num9,             0);
        contentPanel.setComponentZOrder(num0,             0);
        contentPanel.setComponentZOrder(backspace,        0);
        contentPanel.setComponentZOrder(pin1,             0);
        contentPanel.setComponentZOrder(pin2,             0);
        contentPanel.setComponentZOrder(pin3,             0);
        contentPanel.setComponentZOrder(pin4,             0);
        contentPanel.setComponentZOrder(phoneSubheading,  0);
        contentPanel.setComponentZOrder(phoneHeading,     0);
        contentPanel.repaint();
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
        backspace = new javax.swing.JLabel();
        pin4 = new javax.swing.JLabel();
        pin3 = new javax.swing.JLabel();
        pin2 = new javax.swing.JLabel();
        pin1 = new javax.swing.JLabel();
        num0 = new javax.swing.JLabel();
        num9 = new javax.swing.JLabel();
        num8 = new javax.swing.JLabel();
        num7 = new javax.swing.JLabel();
        num6 = new javax.swing.JLabel();
        num5 = new javax.swing.JLabel();
        num4 = new javax.swing.JLabel();
        num3 = new javax.swing.JLabel();
        num2 = new javax.swing.JLabel();
        num1 = new javax.swing.JLabel();
        phoneHeading = new javax.swing.JLabel();
        phoneSubheading = new javax.swing.JLabel();
        phoneScreen = new javax.swing.JLabel();
        phoneScrim = new javax.swing.JLabel();
        combineLabel = new javax.swing.JLabel();
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
        inspectButton = new javax.swing.JLabel();
        backgroundLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        contentPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ui/back_button.png"))); // NOI18N
        contentPanel.add(backButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 630, -1, 50));

        backspace.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btnbackspace.png"))); // NOI18N
        contentPanel.add(backspace, new org.netbeans.lib.awtextra.AbsoluteConstraints(623, 460, -1, -1));

        pin4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_pin_empty.png"))); // NOI18N
        contentPanel.add(pin4, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 220, -1, -1));

        pin3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_pin_empty.png"))); // NOI18N
        contentPanel.add(pin3, new org.netbeans.lib.awtextra.AbsoluteConstraints(562, 220, -1, -1));

        pin2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_pin_empty.png"))); // NOI18N
        contentPanel.add(pin2, new org.netbeans.lib.awtextra.AbsoluteConstraints(545, 220, -1, -1));

        pin1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_pin_empty.png"))); // NOI18N
        contentPanel.add(pin1, new org.netbeans.lib.awtextra.AbsoluteConstraints(527, 220, -1, -1));

        num0.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn0.png"))); // NOI18N
        contentPanel.add(num0, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 460, -1, -1));

        num9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn9.png"))); // NOI18N
        contentPanel.add(num9, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 400, -1, -1));

        num8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn8.png"))); // NOI18N
        contentPanel.add(num8, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 400, -1, -1));

        num7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn7.png"))); // NOI18N
        contentPanel.add(num7, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 400, -1, -1));

        num6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn6.png"))); // NOI18N
        contentPanel.add(num6, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 340, -1, -1));

        num5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn5.png"))); // NOI18N
        contentPanel.add(num5, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 340, -1, -1));

        num4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn4.png"))); // NOI18N
        contentPanel.add(num4, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 340, -1, -1));

        num3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn3.png"))); // NOI18N
        contentPanel.add(num3, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 280, -1, -1));

        num2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn2.png"))); // NOI18N
        contentPanel.add(num2, new org.netbeans.lib.awtextra.AbsoluteConstraints(551, 280, -1, -1));

        num1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn1.png"))); // NOI18N
        contentPanel.add(num1, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 280, -1, -1));

        phoneHeading.setBackground(new java.awt.Color(255, 255, 255));
        phoneHeading.setFont(new java.awt.Font("Arial Narrow", 1, 18)); // NOI18N
        phoneHeading.setForeground(new java.awt.Color(255, 255, 255));
        phoneHeading.setText("Enter Passcode");
        contentPanel.add(phoneHeading, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 210, -1, -1));

        phoneSubheading.setBackground(new java.awt.Color(255, 255, 255));
        phoneSubheading.setFont(new java.awt.Font("Arial Narrow", 0, 14)); // NOI18N
        phoneSubheading.setForeground(new java.awt.Color(255, 255, 255));
        phoneSubheading.setText("Forgot Pin?");
        contentPanel.add(phoneSubheading, new org.netbeans.lib.awtextra.AbsoluteConstraints(547, 250, -1, -1));

        phoneScreen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_lockscreen.png"))); // NOI18N
        contentPanel.add(phoneScreen, new org.netbeans.lib.awtextra.AbsoluteConstraints(416, 73, -1, -1));

        phoneScrim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_scrim.png"))); // NOI18N
        contentPanel.add(phoneScrim, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        combineLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combineLabel.setText("Combine");
        contentPanel.add(combineLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(824, 555, 260, 50));

        combineSlotB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(combineSlotB, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 510, -1, -1));

        combineSlotA.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        contentPanel.add(combineSlotA, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 510, -1, -1));

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

        inspectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_description/inspect_button.png"))); // NOI18N
        contentPanel.add(inspectButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1065, 110, -1, -1));

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
    
    /** Shows the phone lockscreen overlay. */
    private void openPhoneInspect() {
        phoneScreen.setIcon(phoneLockscreenIcon);
        
        phoneScrim.setVisible(true);
        phoneScreen.setVisible(true);
        phoneHeading.setVisible(true);
        phoneSubheading.setVisible(true);
        num1.setVisible(true);
        num2.setVisible(true);
        num3.setVisible(true);
        num4.setVisible(true);
        num5.setVisible(true);
        num6.setVisible(true);
        num7.setVisible(true);
        num8.setVisible(true);
        num9.setVisible(true);
        num0.setVisible(true);
        backspace.setVisible(true);
        pin1.setVisible(true);
        pin2.setVisible(true);
        pin3.setVisible(true);
        pin4.setVisible(true);
        
        pinBuffer.setLength(0);
        refreshPinDots();
    }
    
    private void closePhoneScreen() {
        phoneScrim.setVisible(false);
        phoneScreen.setVisible(false);
        phoneHeading.setVisible(false);
        phoneSubheading.setVisible(false);
        num1.setVisible(false);
        num2.setVisible(false);
        num3.setVisible(false);
        num4.setVisible(false);
        num5.setVisible(false);
        num6.setVisible(false);
        num7.setVisible(false);
        num8.setVisible(false);
        num9.setVisible(false);
        num0.setVisible(false);
        backspace.setVisible(false);
        pin1.setVisible(false);
        pin2.setVisible(false);
        pin3.setVisible(false);
        pin4.setVisible(false);
        AudioCache.play("/audio/sfx/ui/click.wav");
    }
    
    /** Appends a digit to the PIN buffer (max 4) and refreshes the dots. */
    private void onPinDigit(String digit) {
        if (pinBuffer.length() >= 4) return;
        pinBuffer.append(digit);
        refreshPinDots();
        System.out.println("PIN buffer: " + pinBuffer);

        if (pinBuffer.length() == 4) {
            checkPin();
        }
    }

    /** Verifies the entered PIN and swaps the phone screen if correct. */
    private void checkPin() {
        final String CORRECT_PIN = "0818";

        if (pinBuffer.toString().equals(CORRECT_PIN)) {
            // Unlocked — swap the phone image and hide the lock UI.
            AudioCache.play("/audio/sfx/ui/notification.wav");   // or a dedicated unlock sfx
            phoneScreen.setIcon(phoneUnlockedIcon);

            phoneHeading.setVisible(false);
            phoneSubheading.setVisible(false);
            num1.setVisible(false);
            num2.setVisible(false);
            num3.setVisible(false);
            num4.setVisible(false);
            num5.setVisible(false);
            num6.setVisible(false);
            num7.setVisible(false);
            num8.setVisible(false);
            num9.setVisible(false);
            num0.setVisible(false);
            backspace.setVisible(false);
            pin1.setVisible(false);
            pin2.setVisible(false);
            pin3.setVisible(false);
            pin4.setVisible(false);

            pinBuffer.setLength(0);
        } else {
            // Wrong PIN — clear the buffer for another attempt.
            AudioCache.play("/audio/sfx/ui/click.wav");   // or a dedicated error sfx
            pinBuffer.setLength(0);
            refreshPinDots();
        }
    }

    /** Removes the last digit from the PIN buffer and refreshes the dots. */
    private void onPinBackspace() {
        if (pinBuffer.length() == 0) return;
        pinBuffer.setLength(pinBuffer.length() - 1);
        refreshPinDots();
        System.out.println("PIN buffer: " + pinBuffer);
    }

    /** Repaints the four PIN dots to match the current buffer length. */
    private void refreshPinDots() {
        for (int i = 0; i < pinDots.length; i++) {
            pinDots[i].setIcon(i < pinBuffer.length() ? pinFilledIcon : pinEmptyIcon);
        }
    }
    
        // =========================================================
        // Slot behavior
        // =========================================================

        /** Wires hover + click listeners to a single slot. */
        private void wireSlot(final Slot slot) {
            final javax.swing.JLabel s = slot.label;

            s.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    // Show hover only if this slot isn't already the selected one.
                    if (!slot.selected) slot.showHover();
                    s.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                    AudioCache.play("/audio/sfx/ui/hover.wav");
                }

                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    // Restore the slot's persistent state — hover if selected, normal otherwise.
                    slot.refresh();
                    s.setCursor(java.awt.Cursor.getDefaultCursor());
                }

                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        // Double-click → stage into a combine slot.
                        if (!slot.isEmpty()) {
                            stageForCombine(slot.itemId);
                            AudioCache.play("/audio/sfx/ui/select.wav");
                        }
                        return;   // don't run the single-click logic on the second click
                    }

                    // Single-click → original behavior (highlight + description).
                    AudioCache.play("/audio/sfx/ui/select.wav");

                    if (slot.isEmpty()) {
                        descriptionPanel.showEmpty();
                        inspectButton.setVisible(false);
                        return;
                    }

                    if (selectedSlot != null && selectedSlot != slot) {
                        selectedSlot.selected = false;
                        selectedSlot.refresh();
                    }
                    slot.selected = true;
                    slot.refresh();
                    selectedSlot = slot;
                    descriptionPanel.setItem(ItemDatabase.get(slot.itemId));

                    // Show the magnifier only when the phone is selected.
                    inspectButton.setVisible("phone".equals(slot.itemId));
                }
            });
        }
        
        /** Places an item into combine slot A or B, whichever is free. */
        private void stageForCombine(String itemId) {
            if (itemId == null) return;

            // Already staged? Ignore.
            if (itemId.equals(combineItemA) || itemId.equals(combineItemB)) return;

            if (combineItemA == null) {
                combineItemA = itemId;
                setCombineSlotIcon(combineSlotA, itemId);
            } else if (combineItemB == null) {
                combineItemB = itemId;
                setCombineSlotIcon(combineSlotB, itemId);
            } else {
                AudioCache.play("/audio/sfx/ui/click.wav");
                System.out.println("Combine slots full");
                return;
            }

            // Staging changed — no result until the player clicks COMBINE.
            descriptionPanel.showEmpty();
            refreshCombineLabel();
        }

        /**
        * Sets the icon on a combine slot to the item's slot_<id>.png, and remembers
        * the matching highlighted_slot_<id>.png so hover can swap to it.
        */
       private void setCombineSlotIcon(javax.swing.JLabel slot, String itemId) {
        java.net.URL normalUrl = getClass().getResource(
            "/Images/gameplay/inventory/item_slots/slot_" + itemId + ".png");
        if (normalUrl == null) {
            System.out.println("Missing combine icon for: " + itemId);
            return;
        }
        slot.setIcon(new javax.swing.ImageIcon(normalUrl));

        java.net.URL hoverUrl = getClass().getResource(
            "/Images/gameplay/inventory/item_slots/highlighted_slot_" + itemId + ".png");

        javax.swing.ImageIcon hover = (hoverUrl != null)
            ? new javax.swing.ImageIcon(hoverUrl)
            : new javax.swing.ImageIcon(normalUrl);

        if (slot == combineSlotA) combineAHover = hover;
        else if (slot == combineSlotB) combineBHover = hover;
    }

        /** Removes whatever item is staged in the given combine slot. */
        private void clearCombineSlot(boolean isA) {
            if (isA) {
                if (combineItemA == null) return;
                combineItemA = null;
                combineAHover = null;
                combineSlotA.setIcon(emptyCombineIcon);
            } else {
                if (combineItemB == null) return;
                combineItemB = null;
                combineBHover = null;
                combineSlotB.setIcon(emptyCombineIcon);
            }

            // Staging changed — reset the description panel and the COMBINE glow.
            descriptionPanel.showEmpty();
            refreshCombineLabel();
        }
        
        /**
        * If both combine slots are filled and the pair matches a recipe,
        * shows the resulting deduction in the description panel.
        * Otherwise resets the description panel to its empty state.
        */
        private void tryCombine() {
            if (combineItemA == null || combineItemB == null) {
                descriptionPanel.showEmpty();
                return;
            }

            String resultId = ItemDatabase.getRecipe(combineItemA, combineItemB);

            if (resultId == null) {
                descriptionPanel.showEmpty();
                return;
            }

            ItemDatabase.Item result = ItemDatabase.get(resultId);
            if (result != null) {
                boolean firstTime = !GameState.isDiscovered(resultId);

                descriptionPanel.setItem(result);

                if (firstTime) {
                    GameState.markDiscovered(resultId);
                    AudioCache.play("/audio/sfx/ui/notification.wav");
                    showToast("/Images/ui/notification_new_discovery.png");
                } else {
                    // Already discovered — play a softer reveal sound, no toast.
                    AudioCache.play("/audio/sfx/ui/select.wav");
                }

                System.out.println("Combined: " + combineItemA + " + " + combineItemB
                    + " → " + resultId + (firstTime ? " (first time)" : " (already known)"));
            }
        }
        
        /** Updates the COMBINE label's color based on whether both slots are filled. */
        private void refreshCombineLabel() {
            boolean ready = (combineItemA != null && combineItemB != null);
            combineLabel.setForeground(ready
                ? new java.awt.Color(255, 230, 160)    
                : new java.awt.Color(120, 120, 120));  
        }

        /** Clears both A and B and the result. */
        private void clearAllCombineSlots() {
            combineItemA = null;
            combineItemB = null;
            combineAHover = null;       
            combineBHover = null;
            combineSlotA.setIcon(emptyCombineIcon);
            combineSlotB.setIcon(emptyCombineIcon);
            refreshCombineLabel();
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
    
    private java.awt.Font getCustomFont(float size) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/fonts/press_start_2p.ttf");
            java.awt.Font base = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
            return base.deriveFont(size);
        } catch (Exception e) {
            return new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, (int) size);
        }
    }
    
    /**
    * Slides a PNG toast in from the left edge of the frame, holds it, then
    * slides it back out.
    *
    * @param pngPath classpath path of the toast PNG (text + icon baked in)
    */
   private void showToast(String pngPath) {
       hideToast();   // never stack

       java.net.URL url = getClass().getResource(pngPath);
       if (url == null) {
           System.out.println("Toast PNG missing: " + pngPath);
           return;
       }
       final javax.swing.ImageIcon icon = new javax.swing.ImageIcon(url);

       final int w = icon.getIconWidth();
       final int h = icon.getIconHeight();

       // Vertical position — about 18% down the frame.
       final int y = (int)(getContentPane().getHeight() * 0.18);

       // Slide boundaries.
       final int START_X = -w - 20;   // fully off-screen
       final int REST_X  = 40;        // resting position

       // The component just draws the PNG at its own origin.
       javax.swing.JComponent toast = new javax.swing.JComponent() {
           @Override protected void paintComponent(java.awt.Graphics g) {
               super.paintComponent(g);
               g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), null);
           }
       };
       toast.setOpaque(false);
       toast.setBounds(START_X, y, w, h);

       getRootPane().getLayeredPane().add(
           toast, javax.swing.JLayeredPane.POPUP_LAYER);
       getRootPane().getLayeredPane().repaint();

       activeToast = toast;

       // ---- Animate: slide in → hold → slide out ----
       final int SLIDE_IN  = 320;
       final int HOLD      = 2400;
       final int SLIDE_OUT = 320;
       final int TICK      = 16;

       final long start = System.currentTimeMillis();
       final int totalMs = SLIDE_IN + HOLD + SLIDE_OUT;

       javax.swing.Timer timer = new javax.swing.Timer(TICK, null);
       timer.addActionListener(e -> {
           long elapsed = System.currentTimeMillis() - start;
           if (elapsed >= totalMs) {
               timer.stop();
               hideToast();
               return;
           }

           int x;
           if (elapsed < SLIDE_IN) {
               double t = elapsed / (double) SLIDE_IN;
               double eased = 1 - Math.pow(1 - t, 3);   // ease out
               x = (int)(START_X + (REST_X - START_X) * eased);
           } else if (elapsed < SLIDE_IN + HOLD) {
               x = REST_X;
           } else {
               long t = elapsed - SLIDE_IN - HOLD;
               double p = t / (double) SLIDE_OUT;
               double eased = p * p * p;                // ease in
               x = (int)(REST_X + (START_X - REST_X) * eased);
           }

           toast.setLocation(x, y);
           getRootPane().getLayeredPane().repaint();
       });
       timer.start();
   }

   /** Removes the toast if one is showing. */
   private void hideToast() {
       if (activeToast != null) {
           getRootPane().getLayeredPane().remove(activeToast);
           getRootPane().getLayeredPane().repaint();
           activeToast = null;
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
           java.awt.EventQueue.invokeLater(() -> new Inventory().setVisible(true));
       }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel backButton;
    private javax.swing.JLabel backgroundLabel;
    private javax.swing.JLabel backspace;
    private javax.swing.JLabel combineLabel;
    private javax.swing.JLabel combineSlotA;
    private javax.swing.JLabel combineSlotB;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JLabel inspectButton;
    private javax.swing.JLabel num0;
    private javax.swing.JLabel num1;
    private javax.swing.JLabel num2;
    private javax.swing.JLabel num3;
    private javax.swing.JLabel num4;
    private javax.swing.JLabel num5;
    private javax.swing.JLabel num6;
    private javax.swing.JLabel num7;
    private javax.swing.JLabel num8;
    private javax.swing.JLabel num9;
    private javax.swing.JLabel phoneHeading;
    private javax.swing.JLabel phoneScreen;
    private javax.swing.JLabel phoneScrim;
    private javax.swing.JLabel phoneSubheading;
    private javax.swing.JLabel pin1;
    private javax.swing.JLabel pin2;
    private javax.swing.JLabel pin3;
    private javax.swing.JLabel pin4;
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
