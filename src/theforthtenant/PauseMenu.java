package theforthtenant;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;

/**
 * Reusable pause menu overlay.
 *  - Shows a pause icon in a corner.
 *  - Click the icon to pause. Click a menu item to select it.
 *  - ESC key toggles pause/resume.
 *  - Popup has a scale-in + fade-in animation.
 */
public class PauseMenu {

    public interface Callbacks {
        void onPause();
        void onResume();
        /** Called when the player picks "Main Menu". */
        default void onMainMenu() {}
        /** Called when the player picks "Options". */
        default void onOptions() {}
    }

    public enum Corner { BOTTOM_RIGHT, BOTTOM_LEFT, TOP_RIGHT, TOP_LEFT }

    private static final int ICON_SIZE   = 30;
    private static final int ICON_MARGIN = 15;

    // Menu item labels (top → bottom). Index 0 is the default selection.
    private static final String[] MENU_ITEMS = { "Resume", "Options", "Main Menu" };

    private final JFrame frame;
    private final Callbacks callbacks;
    private final Corner corner;
    private PauseOverlay overlay;
    private boolean paused = false;
    private int selectedIndex = 0;

    private Font menuFont;

    private PauseMenu(JFrame frame, Corner corner, Callbacks callbacks) {
        this.frame = frame;
        this.corner = corner;
        this.callbacks = callbacks;
    }

    public static PauseMenu attachTo(JFrame frame, Callbacks callbacks) {
        return attachTo(frame, Corner.BOTTOM_RIGHT, callbacks);
    }

    public static PauseMenu attachTo(JFrame frame, Corner corner, Callbacks callbacks) {
        PauseMenu pm = new PauseMenu(frame, corner, callbacks);
        pm.install();
        return pm;
    }

    private void install() {
        // Load the pixel font once
        menuFont = loadMenuFont(22f);

        overlay = new PauseOverlay();
        JLayeredPane layered = frame.getRootPane().getLayeredPane();
        overlay.setBounds(0, 0, 1150, 680);
        layered.add(overlay, JLayeredPane.POPUP_LAYER);
        layered.repaint();

        // ESC key toggles pause
        javax.swing.JRootPane root = frame.getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "togglePause");
        root.getActionMap().put("togglePause", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (paused) resume(); else pause();
            }
        });

        // Up/Down/Enter keys work while paused
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0), "pauseUp");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0), "pauseDown");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "pauseEnter");

        root.getActionMap().put("pauseUp", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!paused) return;
                selectedIndex = (selectedIndex - 1 + MENU_ITEMS.length) % MENU_ITEMS.length;
                overlay.repaint();
            }
        });
        root.getActionMap().put("pauseDown", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!paused) return;
                selectedIndex = (selectedIndex + 1) % MENU_ITEMS.length;
                overlay.repaint();
            }
        });
        root.getActionMap().put("pauseEnter", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!paused) return;
                activate(selectedIndex);
            }
        });
    }

    public boolean isPaused() { return paused; }

    public void pause() {
        if (paused) return;
        paused = true;
        selectedIndex = 0;              // default back to "Resume"
        overlay.startPopupAnim();
        if (callbacks != null) callbacks.onPause();
    }

    public void resume() {
        if (!paused) return;
        paused = false;
        overlay.repaint();
        if (callbacks != null) callbacks.onResume();
    }
    
    public void hideOverlay() {
        paused = false;
        if (overlay != null) {
            overlay.setVisible(false);
        }
    }

    /** Run the action for the given menu item. */
    private void activate(int index) {
        switch (index) {
            case 0: // Resume
                resume();
                break;
            case 1: // Options
                if (callbacks != null) callbacks.onOptions();
                break;
            case 2: // Main Menu
                if (callbacks != null) callbacks.onMainMenu();
                break;
        }
    }

    private Font loadMenuFont(float size) {
        try {
            InputStream is = getClass().getResourceAsStream("/fonts/press_start_2p.ttf");
            Font base = Font.createFont(Font.TRUETYPE_FONT, is);
            return base.deriveFont(size);
        } catch (Exception e) {
            System.out.println("Pause font not found, using default.");
            return new Font("Monospaced", Font.BOLD, (int) size);
        }
    }

    // ---- Overlay ----

    private class PauseOverlay extends JComponent {
        private Image icon;
        private Image popup;
        private float popupScale = 1f;
        private float popupAlpha = 1f;
        private Timer popupTimer;
        private Rectangle[] itemBounds = new Rectangle[0];
        private int hoveredIndex = -1;

        PauseOverlay() {
            setOpaque(false);
            try {
                InputStream i1 = getClass().getResourceAsStream("/Images/ui/pause_icon.png");
                if (i1 != null) icon = ImageIO.read(i1);
                InputStream i2 = getClass().getResourceAsStream("/Images/ui/pause_popup.png");
                if (i2 != null) popup = ImageIO.read(i2);
            } catch (Exception e) {
                System.out.println("Pause assets missing: " + e.getMessage());
            }

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (paused) {
                        int hit = hitTest(e.getPoint());
                        if (hit >= 0) {
                            selectedIndex = hit;
                            activate(hit);
                        }
                    } else if (isPauseIconHit(e.getPoint())) {
                        pause();
                    }
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    if (!paused) return;
                    int hit = hitTest(e.getPoint());
                    if (hit != hoveredIndex) {
                        hoveredIndex = hit;
                        if (hit >= 0) selectedIndex = hit;
                        repaint();
                    }
                }
            });

            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        void startPopupAnim() {
            popupScale = 0.6f;
            popupAlpha = 0f;
            if (popupTimer != null) popupTimer.stop();

            final long start = System.currentTimeMillis();
            final int DURATION_MS = 220;

            popupTimer = new Timer(16, null);
            popupTimer.addActionListener(e -> {
                long elapsed = System.currentTimeMillis() - start;
                float p = Math.min(1f, elapsed / (float) DURATION_MS);
                float eased = 1f - (1f - p) * (1f - p);
                popupScale = 0.6f + 0.4f * eased;
                popupAlpha = eased;
                repaint();
                if (p >= 1f) popupTimer.stop();
            });
            popupTimer.start();
        }

        private Rectangle getIconRect() {
            int w = getWidth();
            int h = getHeight();
            int x, y;
            switch (corner) {
                case BOTTOM_LEFT -> { x = ICON_MARGIN;                 y = h - ICON_SIZE - ICON_MARGIN; }
                case TOP_RIGHT   -> { x = w - ICON_SIZE - ICON_MARGIN; y = ICON_MARGIN; }
                case TOP_LEFT    -> { x = ICON_MARGIN;                 y = ICON_MARGIN; }
                default          -> { x = w - ICON_SIZE - ICON_MARGIN; y = h - ICON_SIZE - ICON_MARGIN; }
            }
            return new Rectangle(x, y, ICON_SIZE, ICON_SIZE);
        }

        @Override
        public boolean contains(int x, int y) {
            if (paused) return true;
            return getIconRect().contains(x, y);
        }

        private boolean isPauseIconHit(Point p) {
            return !paused && getIconRect().contains(p);
        }

        private int hitTest(Point p) {
            for (int i = 0; i < itemBounds.length; i++) {
                if (itemBounds[i] != null && itemBounds[i].contains(p)) return i;
            }
            return -1;
        }

        /** Compute the draw rectangle for the popup image (centered, scaled). */
        private Rectangle computePopupRect() {
            if (popup == null) {
                // Fallback: a fixed-size centered rect
                int w = 420, h = 260;
                return new Rectangle((getWidth() - w) / 2, (getHeight() - h) / 2, w, h);
            }
            int pw = popup.getWidth(null);
            int ph = popup.getHeight(null);
            int drawW = (int) (pw * popupScale);
            int drawH = (int) (ph * popupScale);
            int px = (getWidth()  - drawW) / 2;
            int py = (getHeight() - drawH) / 2;
            return new Rectangle(px, py, drawW, drawH);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (!paused) {
                if (icon != null) {
                    Rectangle r = getIconRect();
                    g2.drawImage(icon, r.x, r.y, r.width, r.height, null);
                }
                g2.dispose();
                return;
            }

            // Darken screen
            g2.setColor(new Color(0, 0, 0, 170));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Draw the popup art (if any)
            Rectangle pr = computePopupRect();
            if (popup != null) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, popupAlpha));
                g2.drawImage(popup, pr.x, pr.y, pr.width, pr.height, null);
                g2.setComposite(AlphaComposite.SrcOver);
            }

            // ---- Layout the menu items ----
            g2.setFont(menuFont);
            FontMetrics fm = g2.getFontMetrics();

            int rowH      = fm.getHeight() + 22;
            int firstRowY = pr.y + (int)(pr.height * 0.50) + fm.getAscent();
            int centerX   = pr.x + pr.width / 2;

            itemBounds = new Rectangle[MENU_ITEMS.length];

            for (int i = 0; i < MENU_ITEMS.length; i++) {
                String label = MENU_ITEMS[i];
                boolean selected = (i == selectedIndex);

                // Indicator arrows ONLY on the selected row
                String display = selected
                        ? "> " + label + " <"
                        : "  " + label + "  ";

                int textW = fm.stringWidth(display);
                int tx = centerX - textW / 2;
                int ty = firstRowY + i * rowH;

                // ---- Background ONLY when selected ----
                int pad = 12;
                int barX = tx - pad;
                int barY = ty - fm.getAscent() - 6;
                int barW = textW + pad * 2;
                int barH = fm.getHeight() + 12;

                if (selected) {
                    // dark bar so white text pops
                    g2.setColor(new Color(30, 30, 30, 220));
                    g2.fillRoundRect(barX, barY, barW, barH, 8, 8);
                }

                // ---- Text color ----
                g2.setColor(selected ? Color.WHITE : Color.BLACK);
                g2.drawString(display, tx, ty);

                // Store clickable bounds (always, so hover/click still work)
                itemBounds[i] = new Rectangle(barX, barY, barW, barH);
            }

            g2.dispose();
        }
    }
}