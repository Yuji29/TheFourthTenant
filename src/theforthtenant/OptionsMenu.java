package theforthtenant;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
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
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;

/**
 * In-game Options popup with MUSIC and SOUND sliders + a close (X) button.
 * Popup, −/+ buttons and X come from PNG assets. The slider bar itself
 * is drawn in code: white base with dark-red outline, red fill for the value.
 */
public class OptionsMenu {

    public interface Callbacks {
        default void onMusicChanged(int percent) {}
        default void onSoundChanged(int percent) {}
        default void onClose() {}
    }

    private static final int ROW_COUNT = 2;
    private static final String[] ROW_LABELS = { "Music", "Sound" };

    // ---- Asset paths ----
    private static final String POPUP_PATH = "/Images/ui/options_popup.png";
    private static final String MINUS_PATH = "/Images/ui/minus_button.png";
    private static final String PLUS_PATH  = "/Images/ui/plus_button.png";
    private static final String CLOSE_PATH = "/Images/ui/close_button.png";

    // ---- Slider colors (tweak to match your art) ----
    private static final Color BAR_BASE       = new Color(245, 235, 215);  // cream / off-white
    private static final Color BAR_BASE_EDGE  = new Color(120, 30, 30);    // dark red outline
    private static final Color BAR_FILL       = new Color(160, 35, 30);    // solid red
    private static final Color BAR_FILL_EDGE  = new Color(90, 20, 20);     // darker red inner edge

    // ---- Label color on the manila folder ----
    private static final Color LABEL_COLOR = new Color(110, 30, 30);

    private final JFrame frame;
    private final Callbacks callbacks;
    private OptionsOverlay overlay;
    private boolean open = false;
    private int selectedRow = 0;

    private int musicPercent = 100;
    private int soundPercent = 100;

    private Rectangle[] minusRect = new Rectangle[ROW_COUNT];
    private Rectangle[] plusRect  = new Rectangle[ROW_COUNT];
    private Rectangle closeRect;

    private Font labelFont;

    // Loaded assets
    private Image imgPopup;
    private Image imgMinus;
    private Image imgPlus;
    private Image imgClose;

    private OptionsMenu(JFrame frame, Callbacks callbacks) {
        this.frame = frame;
        this.callbacks = callbacks;
    }

    public static OptionsMenu attachTo(JFrame frame, Callbacks callbacks) {
        OptionsMenu om = new OptionsMenu(frame, callbacks);
        om.install();
        return om;
    }

    private void install() {
        labelFont = loadFont(16f);
        
        // NEW — pull saved values from SfxManager
        musicPercent = SfxManager.getMusicVolumePercent();
        soundPercent = SfxManager.getVolumePercent();

        imgPopup = loadImage(POPUP_PATH);
        imgMinus = loadImage(MINUS_PATH);
        imgPlus  = loadImage(PLUS_PATH);
        imgClose = loadImage(CLOSE_PATH);

        overlay = new OptionsOverlay();
        overlay.setBounds(0, 0, 1150, 680);
        frame.getRootPane().getLayeredPane()
             .add(overlay, JLayeredPane.POPUP_LAYER);
        overlay.setVisible(false);

        
    }
    
    private void bindKeys(boolean on) {
        javax.swing.JRootPane root = frame.getRootPane();
        javax.swing.InputMap im = root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT); 
        javax.swing.ActionMap am = root.getActionMap();

        if (on) {
            im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "optEsc");
            im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP,     0), "optUp");
            im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN,   0), "optDown");
            im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT,   0), "optLeft");
            im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT,  0), "optRight");

            am.put("optEsc", new AbstractAction() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (open) close();
                }
            });
            am.put("optUp", new AbstractAction() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (!open) return;
                    selectedRow = (selectedRow - 1 + ROW_COUNT) % ROW_COUNT;
                    overlay.repaint();
                }
            });
            am.put("optDown", new AbstractAction() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (!open) return;
                    selectedRow = (selectedRow + 1) % ROW_COUNT;
                    overlay.repaint();
                }
            });
            am.put("optLeft", new AbstractAction() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (!open) return;
                    adjustSelected(-10);
                }
            });
            am.put("optRight", new AbstractAction() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (!open) return;
                    adjustSelected(+10);
                }
            });
        } else {
            im.remove(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0));
            im.remove(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP,     0));
            im.remove(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN,   0));
            im.remove(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT,   0));
            im.remove(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT,  0));

            am.remove("optEsc");
            am.remove("optUp");
            am.remove("optDown");
            am.remove("optLeft");
            am.remove("optRight");
        }
    }

    public boolean isOpen() { return open; }

    public void show() {
        if (open) return;
        open = true;
        selectedRow = 0;
        bindKeys(true); 
        overlay.setVisible(true);
        overlay.startPopupAnim();
    }

    public void close() {
        if (!open) return;
        open = false;
        bindKeys(false); 
        overlay.setVisible(false);
        if (callbacks != null) callbacks.onClose();
    }

    private void adjustSelected(int delta) {
        if (selectedRow == 0) {
            musicPercent = clamp(musicPercent + delta);
            SfxManager.setMusicVolumePercent(musicPercent);   // ← NEW: persist
            if (callbacks != null) callbacks.onMusicChanged(musicPercent);
        } else {
            soundPercent = clamp(soundPercent + delta);
            SfxManager.setVolumePercent(soundPercent);        // ← NEW: persist
            if (callbacks != null) callbacks.onSoundChanged(soundPercent);
        }
        overlay.repaint();
    }

    private static int clamp(int v) { return Math.max(0, Math.min(100, v)); }

    private Image loadImage(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.out.println("Options asset not found: " + path);
                return null;
            }
            return ImageIO.read(is);
        } catch (Exception e) {
            System.out.println("Failed to load " + path + ": " + e.getMessage());
            return null;
        }
    }

    private Font loadFont(float size) {
        try {
            InputStream is = getClass().getResourceAsStream("/fonts/press_start_2p.ttf");
            Font base = Font.createFont(Font.TRUETYPE_FONT, is);
            return base.deriveFont(size);
        } catch (Exception e) {
            return new Font("Monospaced", Font.BOLD, (int) size);
        }
    }

    // =========================================================
    //  Overlay
    // =========================================================

    private class OptionsOverlay extends JComponent {
        private float popupScale = 1f;
        private float popupAlpha = 1f;
        private Timer popupTimer;

        private Rectangle[] sliderRows = new Rectangle[ROW_COUNT];

        OptionsOverlay() {
            setOpaque(false);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (!open) return;
                    handleClick(e.getPoint());
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

        @Override public boolean contains(int x, int y) { return open; }

        private Rectangle computePopupRect() {
            if (imgPopup == null) {
                int w = 420, h = 360;
                return new Rectangle((getWidth() - w) / 2, (getHeight() - h) / 2, w, h);
            }
            int pw = imgPopup.getWidth(null);
            int ph = imgPopup.getHeight(null);
            int drawW = (int) (pw * popupScale);
            int drawH = (int) (ph * popupScale);
            int px = (getWidth()  - drawW) / 2;
            int py = (getHeight() - drawH) / 2;
            return new Rectangle(px, py, drawW, drawH);
        }

        private void handleClick(Point p) {
            if (closeRect != null && closeRect.contains(p)) {
                close();
                return;
            }

            for (int i = 0; i < ROW_COUNT; i++) {
                if (minusRect[i] != null && minusRect[i].contains(p)) {
                    selectedRow = i;
                    adjustSelected(-10);
                    return;
                }
                if (plusRect[i] != null && plusRect[i].contains(p)) {
                    selectedRow = i;
                    adjustSelected(+10);
                    return;
                }
                if (sliderRows[i] != null && sliderRows[i].contains(p)) {
                    selectedRow = i;
                    overlay.repaint();
                    return;
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!open) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            // Dark scrim
            g2.setColor(new Color(0, 0, 0, 170));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Popup art
            Rectangle pr = computePopupRect();
            if (imgPopup != null) {
                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, popupAlpha));
                g2.drawImage(imgPopup, pr.x, pr.y, pr.width, pr.height, null);
                g2.setComposite(AlphaComposite.SrcOver);
            }
            
            // Global vertical offset — slide the whole options block up/down
            int yOffset = (int)(pr.height * 0.10);   // 0.10 = push down 10% of popup height

            // ---- Layout metrics (fractions of the popup) ----
            g2.setFont(labelFont);
            FontMetrics fm = g2.getFontMetrics();

            int centerX = pr.x + pr.width / 2;

            int firstRowCenterY = pr.y + (int)(pr.height * 0.48) + yOffset;
            int rowSpacing      = (int)(pr.height * 0.20);

            int sliderW = (int)(pr.width  * 0.55);
            int sliderH = (int)(pr.height * 0.050);
            int sliderX = centerX - sliderW / 2;

            int btnSize = (int)(pr.height * 0.095);
            int sideGap = (int)(pr.width  * -0.050);

            int arc = sliderH;                        // fully rounded ends

            sliderRows = new Rectangle[ROW_COUNT];
            minusRect  = new Rectangle[ROW_COUNT];
            plusRect   = new Rectangle[ROW_COUNT];

            for (int i = 0; i < ROW_COUNT; i++) {
                int rowCenterY = firstRowCenterY + i * rowSpacing;

                // 1) Label above the bar
                String label = ROW_LABELS[i].toUpperCase();
                int labelW = fm.stringWidth(label);
                g2.setColor(LABEL_COLOR);
                g2.drawString(label, centerX - labelW / 2,
                              rowCenterY - sliderH / 2 - fm.getAscent() + 2);

                // 2) Slider bar
                int barX = sliderX;
                int barY = rowCenterY - sliderH / 2;
                int percent = (i == 0) ? musicPercent : soundPercent;
                int fillW = (int)(sliderW * percent / 100.0);

                // --- (a) base: cream bar with dark-red outline ---
                RoundRectangle2D base = new RoundRectangle2D.Float(
                        barX, barY, sliderW, sliderH, arc, arc);
                g2.setColor(BAR_BASE);
                g2.fill(base);
                g2.setColor(BAR_BASE_EDGE);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(base);

                // --- (b) fill: red rounded bar clipped to fillW ---
                if (fillW > 0) {
                    Shape oldClip = g2.getClip();
                    // Clip to the fill width; keep the same rounded-left shape
                    g2.clipRect(barX, barY - 2, fillW, sliderH + 4);

                    RoundRectangle2D fill = new RoundRectangle2D.Float(
                            barX, barY, sliderW, sliderH, arc, arc);
                    g2.setColor(BAR_FILL);
                    g2.fill(fill);

                    // Slightly darker inner edge on top of the fill
                    g2.setColor(BAR_FILL_EDGE);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(fill);

                    g2.setClip(oldClip);
                }

                // --- (c) selected row glow around the bar ---
                if (i == selectedRow) {
                    g2.setColor(new Color(255, 230, 190, 180));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(barX - 4, barY - 4,
                                     sliderW + 8, sliderH + 8,
                                     arc + 8, arc + 8);
                }

                // 3) − / + buttons
                int minusCx = barX - sideGap - btnSize / 2;
                int plusCx  = barX + sliderW + sideGap + btnSize / 2;

                if (imgMinus != null) {
                    g2.drawImage(imgMinus,
                            minusCx - btnSize / 2, rowCenterY - btnSize / 2,
                            btnSize, btnSize, null);
                } else {
                    g2.setColor(new Color(160, 40, 35));
                    g2.fillOval(minusCx - btnSize / 2, rowCenterY - btnSize / 2,
                                btnSize, btnSize);
                }

                if (imgPlus != null) {
                    g2.drawImage(imgPlus,
                            plusCx - btnSize / 2, rowCenterY - btnSize / 2,
                            btnSize, btnSize, null);
                } else {
                    g2.setColor(new Color(160, 40, 35));
                    g2.fillOval(plusCx - btnSize / 2, rowCenterY - btnSize / 2,
                                btnSize, btnSize);
                }

                // Hit rectangles
                minusRect[i] = new Rectangle(minusCx - btnSize/2, rowCenterY - btnSize/2,
                                             btnSize, btnSize);
                plusRect[i]  = new Rectangle(plusCx  - btnSize/2, rowCenterY - btnSize/2,
                                             btnSize, btnSize);
                sliderRows[i] = new Rectangle(barX - sideGap - btnSize,
                                              rowCenterY - Math.max(sliderH, btnSize),
                                              sliderW + sideGap*2 + btnSize*2,
                                              Math.max(sliderH, btnSize) * 2);
            }

            // 4) Close (X) button
            int closeSize = (int)(pr.height * 0.115);
            int closeCx   = centerX;
            int closeCy   = pr.y + (int)(pr.height * 0.90);
            if (imgClose != null) {
                g2.drawImage(imgClose,
                        closeCx - closeSize/2, closeCy - closeSize/2,
                        closeSize, closeSize, null);
            } else {
                g2.setColor(new Color(160, 40, 35));
                g2.fillOval(closeCx - closeSize/2, closeCy - closeSize/2,
                            closeSize, closeSize);
            }
            closeRect = new Rectangle(closeCx - closeSize/2, closeCy - closeSize/2,
                                      closeSize, closeSize);

            g2.dispose();
        }
    }
}