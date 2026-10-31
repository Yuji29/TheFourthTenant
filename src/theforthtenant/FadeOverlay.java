package theforthtenant;

import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;

/**
 * A reusable black overlay that supports fading in and out.
 * Used across multiple screens to transition between scenes.
 */
public class FadeOverlay extends JComponent {
    private float alpha = 1f;

    public FadeOverlay() {
        setOpaque(false);
    }

    public void setAlpha(float a) {
        this.alpha = Math.max(0f, Math.min(1f, a));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (alpha <= 0f) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new java.awt.Color(0, 0, 0, (int)(alpha * 255)));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}