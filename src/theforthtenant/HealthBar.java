package theforthtenant;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import javax.imageio.ImageIO;
import javax.swing.JComponent;

public class HealthBar extends JComponent {

    // ---- Colors ----
    private static final Color BAR_BG     = new Color(180, 180, 180);
    private static final Color BAR_FILL   = new Color(85, 155, 70);
    private static final Color OUTLINE    = new Color(20, 20, 20);
    private static final Color TEXT_COLOR = Color.WHITE;

    // ---- State ----
    private int current;
    private final int max;
    private Font textFont;
    private Image focusIcon;

    public HealthBar(int max) {
        this.max = max;
        this.current = GameState.getHealth();
        setOpaque(false);

        // Load the pixel font
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/fonts/press_start_2p.ttf");
            Font base = Font.createFont(Font.TRUETYPE_FONT, is);
            this.textFont = base.deriveFont(10f);
        } catch (Exception e) {
            this.textFont = new Font("Arial", Font.BOLD, 12);
        }

        // Load the focus icon
        try {
            java.net.URL url = getClass().getResource("/Images/ui/focus.png");
            if (url != null) {
                focusIcon = ImageIO.read(url);
            }
        } catch (Exception e) {
            System.out.println("Focus icon missing: " + e.getMessage());
        }
    }
    
    /** Deducts health and updates the visual bar. */
    public void damage(int amount) {
        GameState.setHealth(GameState.getHealth() - amount);
        this.current = GameState.getHealth();
        repaint(); // Redraw the bar immediately
    }

    public void setCurrent(int value) {
        this.current = Math.max(0, Math.min(max, value));
        repaint();
    }

    public int getCurrent() { return current; }
    public int getMax()     { return max; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        int w = getWidth();
        int h = getHeight();

        int barLeft = 35;
        int barWidth = w - barLeft - 4;

        // 1) Draw the "71/100" text
        g2.setFont(textFont);
        g2.setColor(TEXT_COLOR);
        String label = current + "/" + max;
        java.awt.FontMetrics fm = g2.getFontMetrics();
        
        int textX = barLeft + (barWidth - fm.stringWidth(label)) / 2;
        int textY = (h / 2) - 10; 
        g2.drawString(label, textX, textY);

        // 2) Draw the bar
        int barTop    = textY + 4;
        int barHeight = 14;
        
        // Dark outline
        g2.setColor(OUTLINE);
        g2.fillRoundRect(barLeft, barTop, barWidth, barHeight, barHeight, barHeight);

        // Gray background
        int innerInset = 2;
        int innerW = barWidth - innerInset * 2;
        int innerH = barHeight - innerInset * 2;
        g2.setColor(BAR_BG);
        g2.fillRoundRect(barLeft + innerInset, barTop + innerInset, innerW, innerH, innerH, innerH);

        // Green fill
        int fillW = (int) ((innerW) * ((double) current / max));
        if (fillW > 0) {
            g2.setColor(BAR_FILL);
            g2.fillRoundRect(barLeft + innerInset, barTop + innerInset, fillW, innerH, innerH, innerH);
        }

        // 3) Draw the focus icon LAST so it sits on top of the bar
        int iconSize = 70;
        if (focusIcon != null) {
            // You can adjust the 0 to move the icon left/right, 
            // or the (h - iconSize) / 2 to move it up/down
            g2.drawImage(focusIcon, 0, (h - iconSize) / 2, iconSize, iconSize, null);
        }

        g2.dispose();
    }
}