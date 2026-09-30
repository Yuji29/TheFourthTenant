package theforthtenant;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JFrame;
import javax.swing.Timer;

/**
 * Reusable "darken + swinging magnifier" transition overlay.
 *
 * Usage:
 *   TransitionOverlay.play(myFrame, TransitionOverlay.Position.BOTTOM_RIGHT, () -> {
 *       // whatever to do when the transition finishes
 *   });
 */
public class TransitionOverlay {

    public enum Position {
        BOTTOM_RIGHT, BOTTOM_LEFT, TOP_RIGHT, TOP_LEFT, CENTER
    }

    // ---- Tuning knobs ----
    private static final int DURATION_MS   = 2000;   // total animation time
    private static final double ORBIT_RADIUS = 10;   // how big the circle is (pixels)
    private static final double ORBITS       = 1.0;  // how many full revolutions in the duration
    private static final int FRAME_MS      = 16;     // ~60fps
    private static final int ICON_SIZE     = 80;    // magnifier render size
    private static final float MAX_DARKNESS = 0.9f;  // 0 = no dim, 1 = full black
    private static final int MARGIN        = 10;     // distance from the chosen corner

    /** Convenience: default position is BOTTOM_RIGHT. */
    public static void play(JFrame frame, Runnable onFinished) {
        play(frame, Position.BOTTOM_RIGHT, onFinished);
    }

    public static void play(JFrame frame, Position pos, Runnable onFinished) {
        JLayeredPane layered = frame.getRootPane().getLayeredPane();

        OverlayPanel overlay = new OverlayPanel(pos);
        overlay.setBounds(0, 0,
                frame.getContentPane().getWidth(),
                frame.getContentPane().getHeight());
        overlay.setOpaque(false);

        layered.add(overlay, JLayeredPane.POPUP_LAYER);
        layered.repaint();

        Timer timer = new Timer(FRAME_MS, null);
        final long startTime = System.currentTimeMillis();

        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1f, elapsed / (float) DURATION_MS);
            overlay.update(progress);
            overlay.repaint();

            if (progress >= 1f) {
                timer.stop();
                layered.remove(overlay);
                layered.repaint();
                if (onFinished != null) onFinished.run();
            }
        });
        timer.start();
    }

    private static class OverlayPanel extends JComponent {
        private Image icon;
        private float progress = 0f;
        private final Position position;

        OverlayPanel(Position position) {
            this.position = position;
            try {
                InputStream is = getClass().getResourceAsStream("/Images/magnifier.png");
                if (is != null) icon = ImageIO.read(is);
            } catch (Exception e) {
                System.out.println("Magnifier icon not found: " + e.getMessage());
            }
        }

        void update(float p) { this.progress = p; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            // --- 1) Darken the screen instantly ---
            g2.setColor(new Color(0, 0, 0, (int) (MAX_DARKNESS * 255)));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // --- 2) Draw the swinging magnifier ---
            if (icon != null) {
                drawSwingingIcon(g2);
            }

            g2.dispose();
        }

        private void drawSwingingIcon(Graphics2D g2) {
            int panelW = getWidth();
            int panelH = getHeight();

            // Base position: bottom-right corner
            int drawX = panelW - ICON_SIZE - MARGIN;
            int drawY = panelH - ICON_SIZE - MARGIN;

            // --- Circular orbit ---
            // The icon traces a small circle around its resting spot.
            double angle = progress * Math.PI * 2 * ORBITS;  // full revolutions
            int offsetX = (int) (Math.cos(angle) * ORBIT_RADIUS);
            int offsetY = (int) (Math.sin(angle) * ORBIT_RADIUS);

            // No rotation — the icon stays upright, just moves around.
            g2.drawImage(icon, drawX + offsetX, drawY + offsetY, ICON_SIZE, ICON_SIZE, null);
        }

        private float easeInOut(float t) {
            return t * t * (3f - 2f * t);
        }
    }
}