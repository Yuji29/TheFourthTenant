/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package theforthtenant;

/**
 *
 * @author yuji
 */
public class Inventory extends GameScreen {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Inventory.class.getName());
    
    // =========================================================
    // Slot model
    // =========================================================

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
        void refresh() {
            if (selected) showHover();
            else          showNormal();
        }
    }

    // ---- Slot state ----
    private Slot[] slots;

    // Shared icons for the empty state — every slot uses the same pair.
    private javax.swing.ImageIcon emptyIcon;
    private javax.swing.ImageIcon emptyHoverIcon;
    private Slot selectedSlot;

    // ---- Combine slots ----
    private String combineItemA;
    private String combineItemB;
    private javax.swing.ImageIcon emptyCombineIcon;
    private javax.swing.ImageIcon combineAHover;
    private javax.swing.ImageIcon combineBHover;

    // ---- Toast ----
    private javax.swing.JComponent activeToast;

    // ---- Phone screen state ----
    private javax.swing.ImageIcon phoneLockscreenIcon;
    private javax.swing.ImageIcon phoneUnlockedIcon;
    private javax.swing.ImageIcon phoneMessageScreenIcon;
    private javax.swing.ImageIcon convoCathyIcon, convoAndayaIcon, convoAndyIcon, convoCardoIcon;
    private int currentConvo = 0;

    // ---- Game state ----
    private boolean isGameOver = false;
    private boolean closing = false;

    // Description images pre-loaded so clicking an item doesn't stutter.
    private java.util.Map<String, javax.swing.ImageIcon> descriptionCache = new java.util.HashMap<>();

    // ---- PIN entry ----
    private final StringBuilder pinBuffer = new StringBuilder();
    private javax.swing.ImageIcon pinEmptyIcon;
    private javax.swing.ImageIcon pinFilledIcon;
    private javax.swing.JLabel[] pinDots;

    // ---- Message-box icons ----
    private javax.swing.ImageIcon msgCathyNorm,  msgCathyHover;
    private javax.swing.ImageIcon msgCardoNorm,  msgCardoHover;
    private javax.swing.ImageIcon msgAndyNorm,   msgAndyHover;
    private javax.swing.ImageIcon msgAndayaNorm, msgAndayaHover;

    // ---- Menus ----
    private PauseMenu pauseMenu;
    private OptionsMenu optionsMenu;
    
    /**
     * Creates new form Inventory
     */
    public Inventory() {
        initComponents();
        setupScreen(); 
    }    

    @Override
    protected void setupScreen() {    

        // ---- Empty slot icons (shared across all 20 slots) ----
        emptyIcon = new javax.swing.ImageIcon(
                getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"));
        emptyCombineIcon = emptyIcon;

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
        phoneMessageScreenIcon = new javax.swing.ImageIcon(
                getClass().getResource("/Images/gameplay/inventory/phone_message_screen.png"));

        // ---- Message inbox icons ----
        msgCathyNorm  = new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/message_cathy.png"));
        msgCathyHover = new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/message_cathy_hover.png"));

        msgAndayaNorm  = new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/message_andaya.png"));
        msgAndayaHover = new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/message_andaya_hover.png"));

        msgCardoNorm  = new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/message_cardo.png"));
        msgCardoHover = new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/message_cardo_hover.png"));

        msgAndyNorm  = new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/message_andy.png"));
        msgAndyHover = new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/message_andy_hover.png"));

        // ---- Message-conversation screens ----
        convoCathyIcon  = new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_convo_cathy.png"));
        convoAndayaIcon = new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_convo_andaya.png"));
        convoAndyIcon   = new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_convo_andy.png"));
        convoCardoIcon  = new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_convo_cardo.png"));

        pinDots = new javax.swing.JLabel[] { pin1, pin2, pin3, pin4 };
        for (javax.swing.JLabel dot : pinDots) {
            dot.setIcon(pinEmptyIcon);
        }

        // ---- Inspect button: hidden by default, only shows for the phone ----
        inspectButton.setVisible(false);
        inspectButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                AudioCache.play("/audio/sfx/phone/phone_tap.wav");
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
        combineSlotA.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                if (combineItemA != null && combineAHover != null) {
                    combineSlotA.setIcon(combineAHover);
                }
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (combineItemA != null) {
                    setCombineSlotIcon(combineSlotA, combineItemA); // restore normal
                }
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                if (combineItemA != null) {
                    AudioCache.play("/audio/sfx/ui/select.wav");
                    clearCombineSlot(true);
                }
            }
        });

        // ---- Combine slot B: hover preview + click to remove ----
        combineSlotB.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                if (combineItemB != null && combineBHover != null) {
                    combineSlotB.setIcon(combineBHover);
                }
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (combineItemB != null) {
                    setCombineSlotIcon(combineSlotB, combineItemB); // restore normal
                }
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                if (combineItemB != null) {
                    AudioCache.play("/audio/sfx/ui/select.wav");
                    clearCombineSlot(false);
                }
            }
        });

        // Start with empty combine slots.
        clearAllCombineSlots();

        // ---- COMBINE button (label on the red ribbon) ----
        combineLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                // no-op
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                if (combineItemA == null || combineItemB == null) {
                    // Nothing staged — soft error.
                    AudioCache.play("/audio/sfx/ui/click.wav");
                    return;
                }
                AudioCache.play("/audio/sfx/ui/select.wav");
                tryCombine();
            }
        });

        // ---- Back button ----
        final javax.swing.ImageIcon backNormal = new javax.swing.ImageIcon(
                getClass().getResource("/Images/ui/back_button.png"));
        final javax.swing.ImageIcon backHover = new javax.swing.ImageIcon(
                getClass().getResource("/Images/ui/back_button_hovered.png"));

        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                backButton.setIcon(backHover);
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
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
                pauseMenu.showPopupOnly();
            }
        });

        // ---- Pause menu ----
        pauseMenu = PauseMenu.attachTo(this, PauseMenu.Corner.TOP_LEFT, new PauseMenu.Callbacks() {
            @Override public void onPause() {
                // Nothing to pause here — hover sounds are fire-and-forget.
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

        // ---- Keyboard shortcut: 1 returns to CrimeScene ----
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

        // ---- Fill slots from GameState, prep description cache ----
        refreshSlots(GameState.getCollected());
        setDescription(null);

        for (String itemId : GameState.getCollected()) {
            String path = ItemDatabase.descriptionImagePath(itemId);
            java.net.URL u = getClass().getResource(path);
            if (u != null) {
                descriptionCache.put(itemId, new javax.swing.ImageIcon(u));
            }
        }
        
        // ---- Phone overlay: everything hidden by default ----
        hidePhoneOverlay();

        // Swallow clicks on the PIN dots so they don't fall through to the scrim.
        pin1.addMouseListener(new java.awt.event.MouseAdapter() { });
        pin2.addMouseListener(new java.awt.event.MouseAdapter() { });
        pin3.addMouseListener(new java.awt.event.MouseAdapter() { });
        pin4.addMouseListener(new java.awt.event.MouseAdapter() { });

        // Clicking outside the phone screen closes it.
        phoneScrim.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                closePhoneScreen();
            }
        });

        // ---- Phone keypad (0–9): hover swap + click → onPinDigit() ----
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
            key.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (isGameOver) return;
                    key.setIcon(hover);
                    AudioCache.play("/audio/sfx/ui/hover.wav");
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    key.setIcon(normal);
                }
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (isGameOver) return;
                    AudioCache.play("/audio/sfx/phone/phone_tap.wav");
                    onPinDigit(digit);
                }
            });
        }

        // ---- Backspace key ----
        final javax.swing.ImageIcon bsNormal = new javax.swing.ImageIcon(
                getClass().getResource("/Images/gameplay/inventory/phone_btnbackspace.png"));
        final javax.swing.ImageIcon bsHover = new javax.swing.ImageIcon(
                getClass().getResource("/Images/gameplay/inventory/phone_hover_btnbackspace.png"));

        backspace.setIcon(bsNormal);
        backspace.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                backspace.setIcon(bsHover);
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                backspace.setIcon(bsNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                AudioCache.play("/audio/sfx/phone/phone_tap.wav");
                onPinBackspace();
            }
        });

        // Swallow clicks on non-interactive phone widgets.
        phoneHeading.addMouseListener(new java.awt.event.MouseAdapter() { });
        phoneScreen.addMouseListener(new java.awt.event.MouseAdapter() { });
        phoneClueText.addMouseListener(new java.awt.event.MouseAdapter() { });
        
        // ---- "Forgot Pin?" link ----
        phoneSubheading.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                // no-op
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                AudioCache.play("/audio/sfx/phone/phone_swipe.wav");
                showClueScreen();
            }
        });

        // ---- "Back" link inside the clue screen ----
        phoneBackText.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                // no-op
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                AudioCache.play("/audio/sfx/phone/phone_swipe.wav");
                returnToLockScreen();
            }
        });

        // ---- Message-app icon: opens the inbox ----
        phoneAppMsg.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                AudioCache.play("/audio/sfx/phone/phone_swipe.wav");

                currentConvo = 0;
                phoneScreen.setIcon(phoneMessageScreenIcon);
                phoneAppMsg.setVisible(false);
                phoneMsgBack.setVisible(true);

                hideInboxMessages();
                showMessageInbox();
            }
        });

        // ---- Message-app back button ----
        phoneMsgBack.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                AudioCache.play("/audio/sfx/phone/phone_swipe.wav");

                if (currentConvo > 0) {
                    // Inside a conversation → back to the inbox.
                    currentConvo = 0;
                    phoneScreen.setIcon(phoneMessageScreenIcon);
                    showMessageInbox();
                } else {
                    // In the inbox → back to the home screen.
                    phoneScreen.setIcon(phoneUnlockedIcon);
                    phoneMsgBack.setVisible(false);
                    phoneAppMsg.setVisible(true);
                    hideInboxMessages();
                }
            }
        });

        // ---- Inbox entries: hover swap + click → open conversation ----
        javax.swing.JLabel[] inboxLabels = { msg1, msg2, msg3, msg4 };
        javax.swing.ImageIcon[] normIcons  = { msgCathyNorm,  msgAndayaNorm, msgAndyNorm, msgCardoNorm };
        javax.swing.ImageIcon[] hoverIcons = { msgCathyHover, msgAndayaHover, msgAndyHover, msgCardoHover };
        javax.swing.ImageIcon[] convoIcons = { convoCathyIcon, convoAndayaIcon, convoAndyIcon, convoCardoIcon };

        for (int i = 0; i < inboxLabels.length; i++) {
            final javax.swing.JLabel msg = inboxLabels[i];
            final javax.swing.ImageIcon normal = normIcons[i];
            final javax.swing.ImageIcon hover  = hoverIcons[i];
            final javax.swing.ImageIcon convo  = convoIcons[i];
            final int convoIndex = i + 1; // 1 = Cathy, 2 = Andaya, 3 = Andy, 4 = Cardo

            msg.setIcon(normal);
            msg.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (isGameOver) return;
                    msg.setIcon(hover);
                    AudioCache.play("/audio/sfx/ui/hover.wav");
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    msg.setIcon(normal);
                }
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (isGameOver) return;
                    AudioCache.play("/audio/sfx/phone/phone_swipe.wav");

                    currentConvo = convoIndex;
                    phoneScreen.setIcon(convo);
                    hideInboxMessages();
                }
            });
        }

        // ---- Health bar ----
        HealthBar healthBar = new HealthBar(100);
        // 280 wide = 70px icon + 210px bar; 70 tall.
        contentPanel.add(healthBar,
                new org.netbeans.lib.awtextra.AbsoluteConstraints(830, 10, 280, 70));
        contentPanel.setComponentZOrder(healthBar, 0);

        // ---- Hide game-over UI by default ----
        gameoverLabel.setVisible(false);
        restartButton.setVisible(false);
        menuButton.setVisible(false);

        // ---- Game-over buttons (hover sound + click) ----
        restartButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!isGameOver) return;
                AudioCache.play("/audio/sfx/ui/select.wav");
                GameState.reset();
                TransitionOverlay.play(Inventory.this, () -> {
                    Backstory bs = new Backstory();
                    bs.setLocation(getLocation());
                    bs.setVisible(true);
                    Inventory.this.dispose();
                });
            }
        });

        menuButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!isGameOver) return;
                AudioCache.play("/audio/sfx/ui/select.wav");
                GameState.reset();
                TransitionOverlay.play(Inventory.this, () -> {
                    MainMenu menu = new MainMenu();
                    menu.setLocation(getLocation());
                    menu.setVisible(true);
                    Inventory.this.dispose();
                });
            }
        });

        // ---- Z-order pass: phone overlay widgets on top, back button very top ----
        // (Kept explicit so future additions don't accidentally hide the phone.)
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
        contentPanel.setComponentZOrder(phoneClueText,    0);
        contentPanel.setComponentZOrder(phoneBackText,    0);
        contentPanel.setComponentZOrder(phoneAppMsg,      0);
        contentPanel.setComponentZOrder(phoneMsgBack,     0);
        contentPanel.setComponentZOrder(msg1,             0);
        contentPanel.setComponentZOrder(msg2,             0);
        contentPanel.setComponentZOrder(msg3,             0);
        contentPanel.setComponentZOrder(msg4,             0);

        // Back button must sit above the scrim so the player can always exit.
        contentPanel.setComponentZOrder(backButton, 0);

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
        restartButton = new javax.swing.JLabel();
        menuButton = new javax.swing.JLabel();
        gameoverLabel = new javax.swing.JLabel();
        backButton = new javax.swing.JLabel();
        msg4 = new javax.swing.JLabel();
        msg3 = new javax.swing.JLabel();
        msg2 = new javax.swing.JLabel();
        msg1 = new javax.swing.JLabel();
        phoneMsgBack = new javax.swing.JLabel();
        phoneAppMsg = new javax.swing.JLabel();
        phoneBackText = new javax.swing.JLabel();
        phoneClueText = new javax.swing.JLabel();
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
        phoneHeading = new javax.swing.JLabel("Enter Passcode", javax.swing.SwingConstants.CENTER);
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
        descTitle = new javax.swing.JLabel();
        descImage = new javax.swing.JLabel();
        descText = new javax.swing.JLabel();
        inspectButton = new javax.swing.JLabel();
        backgroundLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("The Fourth Tenant");
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

        backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ui/back_button.png"))); // NOI18N
        backButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(backButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 630, -1, 50));

        msg4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/message_cardo.png"))); // NOI18N
        contentPanel.add(msg4, new org.netbeans.lib.awtextra.AbsoluteConstraints(439, 413, 260, -1));

        msg3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/message_andy.png"))); // NOI18N
        contentPanel.add(msg3, new org.netbeans.lib.awtextra.AbsoluteConstraints(439, 341, 260, -1));

        msg2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/message_andaya.png"))); // NOI18N
        contentPanel.add(msg2, new org.netbeans.lib.awtextra.AbsoluteConstraints(439, 268, 260, -1));

        msg1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/message_cathy.png"))); // NOI18N
        contentPanel.add(msg1, new org.netbeans.lib.awtextra.AbsoluteConstraints(439, 200, 260, -1));

        phoneMsgBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/message_back_button.png"))); // NOI18N
        phoneMsgBack.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(phoneMsgBack, new org.netbeans.lib.awtextra.AbsoluteConstraints(465, 176, -1, -1));

        phoneAppMsg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_app_message.png"))); // NOI18N
        phoneAppMsg.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(phoneAppMsg, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 485, -1, -1));

        phoneBackText.setFont(new java.awt.Font("Arial Narrow", 0, 14)); // NOI18N
        phoneBackText.setForeground(new java.awt.Color(255, 255, 255));
        phoneBackText.setText("Back");
        phoneBackText.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(phoneBackText, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 500, -1, -1));

        phoneClueText.setFont(new java.awt.Font("Arial Narrow", 0, 14)); // NOI18N
        phoneClueText.setForeground(new java.awt.Color(255, 255, 255));
        phoneClueText.setText("<html><div style='text-align: center;'>An important date I wish to forget but can't.</div></html>");
        phoneClueText.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(phoneClueText, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 280, 190, 40));

        backspace.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btnbackspace.png"))); // NOI18N
        backspace.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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
        num0.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(num0, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 460, -1, -1));

        num9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn9.png"))); // NOI18N
        num9.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(num9, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 400, -1, -1));

        num8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn8.png"))); // NOI18N
        num8.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(num8, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 400, -1, -1));

        num7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn7.png"))); // NOI18N
        num7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(num7, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 400, -1, -1));

        num6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn6.png"))); // NOI18N
        num6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(num6, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 340, -1, -1));

        num5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn5.png"))); // NOI18N
        num5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(num5, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 340, -1, -1));

        num4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn4.png"))); // NOI18N
        num4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(num4, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 340, -1, -1));

        num3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn3.png"))); // NOI18N
        num3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(num3, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 280, -1, -1));

        num2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn2.png"))); // NOI18N
        num2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(num2, new org.netbeans.lib.awtextra.AbsoluteConstraints(551, 280, -1, -1));

        num1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_btn1.png"))); // NOI18N
        num1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(num1, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 280, -1, -1));

        phoneHeading.setBackground(new java.awt.Color(255, 255, 255));
        phoneHeading.setFont(new java.awt.Font("Arial Narrow", 1, 18)); // NOI18N
        phoneHeading.setForeground(new java.awt.Color(255, 255, 255));
        phoneHeading.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        phoneHeading.setText("<html><div style='text-align: center;'>Enter Passcode</div></html>");
        contentPanel.add(phoneHeading, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 210, 290, -1));

        phoneSubheading.setBackground(new java.awt.Color(255, 255, 255));
        phoneSubheading.setFont(new java.awt.Font("Arial Narrow", 0, 14)); // NOI18N
        phoneSubheading.setForeground(new java.awt.Color(255, 255, 255));
        phoneSubheading.setText("<html><u>Forgot Pin?</u></html>");
        phoneSubheading.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(phoneSubheading, new org.netbeans.lib.awtextra.AbsoluteConstraints(547, 250, -1, -1));

        phoneScreen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_message_screen.png"))); // NOI18N
        contentPanel.add(phoneScreen, new org.netbeans.lib.awtextra.AbsoluteConstraints(416, 73, -1, -1));

        phoneScrim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/phone_scrim.png"))); // NOI18N
        phoneScrim.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(phoneScrim, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        combineLabel.setFont(new java.awt.Font("Press Start 2P", 0, 12)); // NOI18N
        combineLabel.setForeground(new java.awt.Color(255, 255, 255));
        combineLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combineLabel.setText("Combine");
        combineLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(combineLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(824, 555, 260, 50));

        combineSlotB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        combineSlotB.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(combineSlotB, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 510, -1, -1));

        combineSlotA.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_slots/slot_empty.png"))); // NOI18N
        combineSlotA.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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

        descTitle.setFont(new java.awt.Font("Press Start 2P", 1, 12)); // NOI18N
        descTitle.setForeground(new java.awt.Color(202, 50, 50));
        descTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        contentPanel.add(descTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 350, 310, 40));

        descImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        descImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_description/description_slot_drum.png"))); // NOI18N
        descImage.setPreferredSize(new java.awt.Dimension(260, 220));
        contentPanel.add(descImage, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 130, 290, 240));

        descText.setBackground(new java.awt.Color(28, 28, 28));
        descText.setFont(new java.awt.Font("Press Start 2P", 1, 10)); // NOI18N
        descText.setForeground(new java.awt.Color(255, 255, 255));
        descText.setText("click an item to show description");
        descText.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        descText.setPreferredSize(new java.awt.Dimension(250, 260));
        contentPanel.add(descText, new org.netbeans.lib.awtextra.AbsoluteConstraints(830, 420, 260, 90));

        inspectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/inventory/item_description/inspect_button.png"))); // NOI18N
        inspectButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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
    
    // ------------------------------------------------------------------
    // Phone overlay visibility helpers
    // Each of these collapses what used to be ~20 lines of setVisible()
    // calls per place. Change the widget list here once and every screen
    // picks it up.
    // ------------------------------------------------------------------

    /** Hides every phone overlay widget (scrim, screen, keypad, message UI, ...). */
    private void hidePhoneOverlay() {
        phoneScrim.setVisible(false);
        phoneScreen.setVisible(false);
        phoneHeading.setVisible(false);
        phoneSubheading.setVisible(false);
        phoneClueText.setVisible(false);
        phoneBackText.setVisible(false);
        phoneAppMsg.setVisible(false);
        phoneMsgBack.setVisible(false);
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
        hideInboxMessages();
    }

    /** Hides the four inbox message rows. */
    private void hideInboxMessages() {
        msg1.setVisible(false);
        msg2.setVisible(false);
        msg3.setVisible(false);
        msg4.setVisible(false);
    }

    /** Hides the lock-screen keypad (digits + backspace + pin dots). */
    private void hideLockScreenKeypad() {
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
    }

    /** Shows the lock-screen keypad (digits + backspace + pin dots). */
    private void showLockScreenKeypad() {
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
    }

    /**
     * Applies damage to the player, refreshes the health bar, and shows the
     * game-over screen if this hit killed them.
     */
    private void damage(int amount) {
        GameState.setHealth(GameState.getHealth() - amount);
        for (java.awt.Component c : contentPanel.getComponents()) {
            if (c instanceof HealthBar) {
                ((HealthBar) c).setCurrent(GameState.getHealth());
            }
        }
        if (GameState.getHealth() <= 0) showGameOver();
    }    
    
    /** Closes this Inventory and returns to the CrimeScene. */
    private void closeInventory() {
        if (closing) return;
        closing = true;

        CrimeScene cs = new CrimeScene();
        cs.setLocation(getLocation());
        cs.setVisible(true);
        dispose();
    }

    /** Shows the phone overlay (lockscreen or home screen depending on state). */
    private void openPhoneInspect() {
        phoneScrim.setVisible(true);
        phoneScreen.setVisible(true);

        if (GameState.isPhoneUnlocked()) {
            // Already unlocked → straight to the home screen.
            phoneScreen.setIcon(phoneUnlockedIcon);
            phoneAppMsg.setVisible(true);
            hideLockScreenKeypad();
            phoneMsgBack.setVisible(false);
            hideInboxMessages();

            pinBuffer.setLength(0);
            refreshPinDots();
        } else {
            // Locked → show the PIN entry screen.
            phoneScreen.setIcon(phoneLockscreenIcon);
            returnToLockScreen();

            pinBuffer.setLength(0);
            refreshPinDots();
        }
    }

    /** Hides every phone widget and resets the current conversation. */
    private void closePhoneScreen() {
        hidePhoneOverlay();
        currentConvo = 0;
        AudioCache.play("/audio/sfx/ui/click.wav");
    }

    /** Shows all four inbox rows in normal (non-hover) state. */
    private void showMessageInbox() {
        msg1.setIcon(msgCathyNorm);
        msg2.setIcon(msgAndayaNorm);
        msg3.setIcon(msgAndyNorm);
        msg4.setIcon(msgCardoNorm);

        msg1.setVisible(true);
        msg2.setVisible(true);
        msg3.setVisible(true);
        msg4.setVisible(true);
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
        if (isGameOver) return;
        final String CORRECT_PIN = "0818";

        if (pinBuffer.toString().equals(CORRECT_PIN)) {
            // Correct — swap the phone image and hide the lock UI.
            AudioCache.play("/audio/sfx/phone/phone_unlock.wav");
            GameState.setPhoneUnlocked(true);
            phoneScreen.setIcon(phoneUnlockedIcon);
            phoneAppMsg.setVisible(true);
            hideLockScreenKeypad();
            pinBuffer.setLength(0);
        } else {
            // Wrong — clear, shake, and take 2 damage.
            pinBuffer.setLength(0);
            refreshPinDots();
            shakePhone();
            damage(2);
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
    
    /** Shakes the entire phone (screen, buttons, text) horizontally to indicate an error. */
    private void shakePhone() {
        AudioCache.play("/audio/sfx/phone/phone_error.wav");

        // Capture original X positions.
        final int origScreenX = phoneScreen.getX();
        final int origHeadingX = phoneHeading.getX();
        final int origSubheadingX = phoneSubheading.getX();

        final int origNum0X = num0.getX(); final int origNum1X = num1.getX();
        final int origNum2X = num2.getX(); final int origNum3X = num3.getX();
        final int origNum4X = num4.getX(); final int origNum5X = num5.getX();
        final int origNum6X = num6.getX(); final int origNum7X = num7.getX();
        final int origNum8X = num8.getX(); final int origNum9X = num9.getX();

        final int origPin1X = pin1.getX(); final int origPin2X = pin2.getX();
        final int origPin3X = pin3.getX(); final int origPin4X = pin4.getX();

        final int origBackspaceX = backspace.getX();

        final int duration = 300;
        final int shakeDistance = 8;
        final long startTime = System.currentTimeMillis();

        javax.swing.Timer timer = new javax.swing.Timer(16, null);
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;

            if (elapsed >= duration) {
                // Snap back exactly.
                phoneScreen.setLocation(origScreenX, phoneScreen.getY());
                phoneHeading.setLocation(origHeadingX, phoneHeading.getY());
                phoneSubheading.setLocation(origSubheadingX, phoneSubheading.getY());

                num0.setLocation(origNum0X, num0.getY()); num1.setLocation(origNum1X, num1.getY());
                num2.setLocation(origNum2X, num2.getY()); num3.setLocation(origNum3X, num3.getY());
                num4.setLocation(origNum4X, num4.getY()); num5.setLocation(origNum5X, num5.getY());
                num6.setLocation(origNum6X, num6.getY()); num7.setLocation(origNum7X, num7.getY());
                num8.setLocation(origNum8X, num8.getY()); num9.setLocation(origNum9X, num9.getY());

                pin1.setLocation(origPin1X, pin1.getY()); pin2.setLocation(origPin2X, pin2.getY());
                pin3.setLocation(origPin3X, pin3.getY()); pin4.setLocation(origPin4X, pin4.getY());

                backspace.setLocation(origBackspaceX, backspace.getY());

                ((javax.swing.Timer) e.getSource()).stop();
                return;
            }

            double progress = (double) elapsed / duration;
            double amplitude = shakeDistance * (1.0 - progress);
            double offset = Math.sin(progress * Math.PI * 10) * amplitude;
            int dx = (int) offset;

            phoneScreen.setLocation(origScreenX + dx, phoneScreen.getY());
            phoneHeading.setLocation(origHeadingX + dx, phoneHeading.getY());
            phoneSubheading.setLocation(origSubheadingX + dx, phoneSubheading.getY());

            num0.setLocation(origNum0X + dx, num0.getY()); num1.setLocation(origNum1X + dx, num1.getY());
            num2.setLocation(origNum2X + dx, num2.getY()); num3.setLocation(origNum3X + dx, num3.getY());
            num4.setLocation(origNum4X + dx, num4.getY()); num5.setLocation(origNum5X + dx, num5.getY());
            num6.setLocation(origNum6X + dx, num6.getY()); num7.setLocation(origNum7X + dx, num7.getY());
            num8.setLocation(origNum8X + dx, num8.getY()); num9.setLocation(origNum9X + dx, num9.getY());

            pin1.setLocation(origPin1X + dx, pin1.getY()); pin2.setLocation(origPin2X + dx, pin2.getY());
            pin3.setLocation(origPin3X + dx, pin3.getY()); pin4.setLocation(origPin4X + dx, pin4.getY());

            backspace.setLocation(origBackspaceX + dx, backspace.getY());
        });
        timer.start();
    }
    
    /** Shows the Clue screen when "Forgot Pin?" is clicked. */
    private void showClueScreen() {
        hideLockScreenKeypad();

        phoneClueText.setVisible(true);
        phoneBackText.setVisible(true);

        phoneHeading.setText("Clue");
        phoneHeading.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        phoneHeading.setVisible(true);

        pinBuffer.setLength(0);
    }

    /** Returns to the normal lock screen from the Clue screen. */
    private void returnToLockScreen() {
        phoneClueText.setVisible(false);
        phoneBackText.setVisible(false);

        phoneHeading.setText("Enter Passcode");
        showLockScreenKeypad();

        refreshPinDots();
    }

     // =========================================================
    // Slot behavior
    // =========================================================

    /** Wires hover + click listeners to a single slot. */
    private void wireSlot(final Slot slot) {
        final javax.swing.JLabel s = slot.label;

        s.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isGameOver) return;
                if (!slot.selected) slot.showHover();
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }

            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                slot.refresh();
            }

            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isGameOver) return;

                if (e.getClickCount() == 2) {
                    // Double-click → stage into a combine slot.
                    if (!slot.isEmpty()) {
                        stageForCombine(slot.itemId);
                        AudioCache.play("/audio/sfx/ui/select.wav");
                    }
                    return;
                }

                // Single-click → highlight + description.
                AudioCache.play("/audio/sfx/ui/select.wav");

                if (slot.isEmpty()) {
                    setDescription(null);
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
                setDescription(ItemDatabase.get(slot.itemId));

                // Magnifier only for the phone.
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
        setDescription(null);
        inspectButton.setVisible(false);
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

        setDescription(null);
        inspectButton.setVisible(false);
    }

    /** Clears both A and B. */
    private void clearAllCombineSlots() {
        combineItemA = null;
        combineItemB = null;
        combineAHover = null;
        combineBHover = null;
        combineSlotA.setIcon(emptyCombineIcon);
        combineSlotB.setIcon(emptyCombineIcon);
    }
        
    /**
     * If both combine slots are filled and the pair matches a recipe,
     * shows the resulting deduction. Otherwise resets the description panel.
     */
    private void tryCombine() {
        if (isGameOver) return;
        if (combineItemA == null || combineItemB == null) {
            setDescription(null);
            return;
        }

        String resultId = ItemDatabase.getRecipe(combineItemA, combineItemB);

        // 1. No such combination exists.
        if (resultId == null) {
            AudioCache.play("/audio/sfx/phone/phone_error.wav");
            setDescription(null);
            shakeDescription();
            damage(2);
            return;
        }

        // 2. Phone-only recipes require an unlocked phone.
        boolean usesPhone = "phone".equals(combineItemA) || "phone".equals(combineItemB);
        boolean isPhoneRecipe = "opened_ledger".equals(resultId)
                || "jealousy".equals(resultId)
                || "audio_log".equals(resultId);

        if (usesPhone && isPhoneRecipe && !GameState.isPhoneUnlocked()) {
            AudioCache.play("/audio/sfx/phone/phone_error.wav");
            setDescription(null);
            System.out.println("You need to unlock the phone first.");
            return;
        }

        // 3. Valid recipe.
        ItemDatabase.Item result = ItemDatabase.get(resultId);
        if (result == null) return;

        boolean firstTime = !GameState.isDiscovered(resultId);
        setDescription(result);

        if (firstTime) {
            GameState.markDiscovered(resultId);
            AudioCache.play("/audio/sfx/ui/notification.wav");
            showToast("/Images/ui/notification_new_discovery.png");

            if (GameState.isFullyComplete()) {
                javax.swing.Timer delay = new javax.swing.Timer(3200, evt -> {
                    AudioCache.play("/audio/sfx/ui/notification.wav");
                    showToast("/Images/ui/notification_phase1.png");
                });
                delay.setRepeats(false);
                delay.start();
            }
        } else {
            AudioCache.play("/audio/sfx/ui/select.wav");
        }
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
                : slot.normal;
        slot.showNormal();
    }
    
    /**
     * Slides a PNG toast in from the left edge of the frame, holds it, then
     * slides it back out.
     */
    private void showToast(String pngPath) {
        hideToast(); // never stack

        java.net.URL url = getClass().getResource(pngPath);
        if (url == null) {
            System.out.println("Toast PNG missing: " + pngPath);
            return;
        }
        final javax.swing.ImageIcon icon = new javax.swing.ImageIcon(url);

        final int w = icon.getIconWidth();
        final int h = icon.getIconHeight();
        final int y = (int) (getContentPane().getHeight() * 0.18);

        final int START_X = -w - 20;
        final int REST_X  = -25;

        javax.swing.JComponent toast = new javax.swing.JComponent() {
            @Override protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), null);
            }
        };
        toast.setOpaque(false);
        toast.setBounds(START_X, y, w, h);

        getRootPane().getLayeredPane().add(toast, javax.swing.JLayeredPane.POPUP_LAYER);
        getRootPane().getLayeredPane().repaint();

        activeToast = toast;

        final int SLIDE_IN  = 320;
        final int HOLD      = 2400;
        final int SLIDE_OUT = 320;
        final int TICK      = 16;

        final long start   = System.currentTimeMillis();
        final int totalMs  = SLIDE_IN + HOLD + SLIDE_OUT;

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
                double eased = 1 - Math.pow(1 - t, 3);
                x = (int) (START_X + (REST_X - START_X) * eased);
            } else if (elapsed < SLIDE_IN + HOLD) {
                x = REST_X;
            } else {
                long t = elapsed - SLIDE_IN - HOLD;
                double p = t / (double) SLIDE_OUT;
                double eased = p * p * p;
                x = (int) (REST_X + (START_X - REST_X) * eased);
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

    /** Shakes the description labels horizontally to indicate an error. */
    private void shakeDescription() {
        final int origImgX   = descImage.getX();
        final int origTitleX = descTitle.getX();
        final int origTextX  = descText.getX();
        final int duration      = 300;
        final int shakeDistance = 8;
        final long start = System.currentTimeMillis();

        javax.swing.Timer shakeTimer = new javax.swing.Timer(16, null);
        shakeTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed >= duration) {
                descImage.setLocation(origImgX, descImage.getY());
                descTitle.setLocation(origTitleX, descTitle.getY());
                descText.setLocation(origTextX, descText.getY());
                ((javax.swing.Timer) e.getSource()).stop();
                return;
            }
            double progress = (double) elapsed / duration;
            double amplitude = shakeDistance * (1.0 - progress);
            double offset = Math.sin(progress * Math.PI * 10) * amplitude;
            int dx = (int) offset;

            descImage.setLocation(origImgX + dx, descImage.getY());
            descTitle.setLocation(origTitleX + dx, descTitle.getY());
            descText.setLocation(origTextX + dx, descText.getY());
        });
        shakeTimer.start();
    }

    /** Updates the description panel with the item's data (or the empty hint). */
    private void setDescription(ItemDatabase.Item item) {
        if (item == null) {
            descTitle.setText("");
            descImage.setIcon(null);

            String hintHtml = "<html><div style='width: 200px; text-align: center;'>"
                    + "Select an item to inspect"
                    + "</div></html>";
            descText.setText(hintHtml);
            return;
        }

        // Load image (cached or on-demand) and scale it.
        javax.swing.ImageIcon cachedIcon = descriptionCache.get(item.id);
        if (cachedIcon == null) {
            java.net.URL u = getClass().getResource(ItemDatabase.descriptionImagePath(item.id));
            if (u != null) {
                cachedIcon = new javax.swing.ImageIcon(u);
                descriptionCache.put(item.id, cachedIcon);
            }
        }

        if (cachedIcon != null) {
            java.awt.Image img = cachedIcon.getImage();
            java.awt.Image scaled = img.getScaledInstance(
                    199, 199, java.awt.Image.SCALE_REPLICATE);
            descImage.setIcon(new javax.swing.ImageIcon(scaled));
        } else {
            descImage.setIcon(null);
        }

        String htmlText = "<html><div style='width: 200px; text-align: justify;'>"
                + item.description + "</div></html>";
        descText.setText(htmlText);
    }
    
    private void showGameOver() {
        isGameOver = true;

        // Reveal the game-over UI.
        gameoverLabel.setVisible(true);
        restartButton.setVisible(true);
        menuButton.setVisible(true);

        gameoverLabel.setOpaque(false);
        restartButton.setOpaque(false);
        menuButton.setOpaque(false);

        contentPanel.setComponentZOrder(gameoverLabel, 0);
        contentPanel.setComponentZOrder(restartButton, 0);
        contentPanel.setComponentZOrder(menuButton, 0);

        // Fade the black overlay out.
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

        // Lock the game.
        pauseMenu.hideOverlay();
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).clear();
        getRootPane().getActionMap().clear();

        AudioCache.play("/audio/sfx/ui/splatter.wav");
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
            g2.setColor(new java.awt.Color(0, 0, 0, (int) (alpha * 255)));
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
    private javax.swing.JLabel descImage;
    private javax.swing.JLabel descText;
    private javax.swing.JLabel descTitle;
    private javax.swing.JLabel gameoverLabel;
    private javax.swing.JLabel inspectButton;
    private javax.swing.JLabel menuButton;
    private javax.swing.JLabel msg1;
    private javax.swing.JLabel msg2;
    private javax.swing.JLabel msg3;
    private javax.swing.JLabel msg4;
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
    private javax.swing.JLabel phoneAppMsg;
    private javax.swing.JLabel phoneBackText;
    private javax.swing.JLabel phoneClueText;
    private javax.swing.JLabel phoneHeading;
    private javax.swing.JLabel phoneMsgBack;
    private javax.swing.JLabel phoneScreen;
    private javax.swing.JLabel phoneScrim;
    private javax.swing.JLabel phoneSubheading;
    private javax.swing.JLabel pin1;
    private javax.swing.JLabel pin2;
    private javax.swing.JLabel pin3;
    private javax.swing.JLabel pin4;
    private javax.swing.JLabel restartButton;
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
