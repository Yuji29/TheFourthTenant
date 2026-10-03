package theforthtenant;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.InputStream;
import javax.swing.JComponent;

/**
 * Reusable subtitle box.
 * Renders 0, 1, or N lines of text with optional speaker names.
 * Auto-sizes its height via {@link #getPreferredHeight(int, java.util.List)}.
 */
public class SubtitleBox extends JComponent {

    /** One subtitle: optional speaker name + text + optional voice-over audio path. */
    public static class Line {
        public final String speaker;
        public final String text;
        public final String audioPath;

        public Line(String speaker, String text) {
            this(speaker, text, null);
        }

        public Line(String speaker, String text, String audioPath) {
            this.speaker = speaker;
            this.text = text;
            this.audioPath = audioPath;
        }
    }

    // ---- Layout ----
    private static final int PADDING_X       = 28;
    private static final int PADDING_TOP     = 12;    // space above the pill
    private static final int PADDING_BOTTOM  = 20;    // space below the text
    private static final int NAME_PILL_PAD_X = 20;
    private static final int NAME_PILL_PAD_Y = 6;
    private static final int GAP_NAME_BODY   = 15;    // between speaker pill and body text
    private static final int GAP_BETWEEN     = 20;    // between stacked subtitle lines
    private static final int LINE_EXTRA      = 6;     // extra space between wrapped lines

    // ---- Display modes ----
    private boolean plainMode = false;    // no background box, no border
    private boolean centerText = false;   // center each line horizontally

    // ---- Colors ----
    private static final Color BOX_BG     = new Color(15, 15, 20, 215);
    private static final Color BOX_BORDER = new Color(90, 90, 100, 170);
    private static final Color NAME_RED   = new Color(150, 30, 30);
    private static final Color BODY_WHITE = new Color(235, 235, 235);

    // ---- Typewriter state ----
    private int visibleChars = Integer.MAX_VALUE;   // 0 = nothing shown, MAX = all shown

    private final java.util.List<Line> lines = new java.util.ArrayList<>();
    private Font nameFont;
    private Font bodyFont;

    public SubtitleBox() {
        setOpaque(false);
        try {
            InputStream is = getClass().getResourceAsStream("/fonts/press_start_2p.ttf");
            Font base = Font.createFont(Font.TRUETYPE_FONT, is);
            nameFont = base.deriveFont(10f);
            bodyFont = base.deriveFont(11f);
        } catch (Exception e) {
            nameFont = new Font("Segoe UI", Font.BOLD, 12);
            bodyFont = new Font("Segoe UI", Font.PLAIN, 13);
        }
    }

    /** Replaces the current contents with a single unspeaker-ed line of text. */
    public void setText(String text) {
        lines.clear();
        if (text != null && !text.isEmpty()) {
            lines.add(new Line(null, text));
        }
        repaint();
    }

    /** Replaces the current contents with the given list of lines. */
    public void setLines(java.util.List<Line> newLines) {
        lines.clear();
        if (newLines != null) lines.addAll(newLines);
        repaint();
    }

    /** Shows only the first {@code n} characters of the currently displayed lines. */
    public void setVisibleChars(int n) {
        this.visibleChars = Math.max(0, n);
        repaint();
    }

    /** Shows all characters (used when the typewriter effect isn't needed). */
    public void showAllChars() {
        this.visibleChars = Integer.MAX_VALUE;
        repaint();
    }

    /** Returns the total character count of all lines (speaker names excluded). */
    public int getTotalChars() {
        int total = 0;
        for (Line l : lines) {
            if (l.text != null) total += l.text.length();
        }
        return total;
    }

    /** Returns the audio path for the first line, or {@code null} if there is none. */
    public String getFirstAudioPath() {
        if (lines.isEmpty()) return null;
        return lines.get(0).audioPath;
    }

    /** Enables or disables plain mode (no background box or border). */
    public void setPlainMode(boolean plain) {
        this.plainMode = plain;
        repaint();
    }

    /** Enables or disables horizontal centering of each rendered line. */
    public void setCenterText(boolean center) {
        this.centerText = center;
        repaint();
    }

    /** Clears all lines. */
    public void clear() {
        lines.clear();
        repaint();
    }

    /** Returns {@code true} if there are no lines to display. */
    public boolean isEmpty() {
        return lines.isEmpty();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (lines.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background box (skipped in plain mode).
        if (!plainMode) {
            g2.setColor(BOX_BG);
            g2.fillRoundRect(0, 0, w, h, 14, 14);
            g2.setColor(BOX_BORDER);
            g2.setStroke(new java.awt.BasicStroke(1.2f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, 14, 14);
        }

        int cursorY = PADDING_TOP;
        int visibleCharsLocal = visibleChars;
        int textMaxW = w - PADDING_X * 2;

        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);

            // Speaker name pill.
            if (line.speaker != null && !line.speaker.isEmpty()) {
                g2.setFont(nameFont);
                FontMetrics nfm = g2.getFontMetrics();
                int pillW = nfm.stringWidth(line.speaker) + NAME_PILL_PAD_X * 2;
                int pillH = nfm.getHeight() + NAME_PILL_PAD_Y * 2;

                g2.setColor(Color.WHITE);
                drawHexagonPill(g2, PADDING_X, cursorY, pillW, pillH);

                g2.setColor(NAME_RED);
                g2.drawString(line.speaker,
                        PADDING_X + NAME_PILL_PAD_X,
                        cursorY + NAME_PILL_PAD_Y + nfm.getAscent());

                cursorY += pillH + GAP_NAME_BODY;
            }

            // Body text.
            g2.setFont(bodyFont);
            g2.setColor(BODY_WHITE);

            // Decide how much of this line to show.
            String shown = line.text;
            if (visibleCharsLocal != Integer.MAX_VALUE) {
                int thisLine = Math.max(0, Math.min(line.text.length(), visibleCharsLocal));
                shown = line.text.substring(0, thisLine);
                visibleCharsLocal -= line.text.length();   // next line gets whatever's left
            }

            cursorY = drawWrappedText(g2, shown, PADDING_X, cursorY, textMaxW);

            if (i < lines.size() - 1) cursorY += GAP_BETWEEN;
        }

        g2.dispose();
    }

    /**
     * Draws an elongated hexagon (rectangle with pointed left and right ends).
     *
     * @param x left edge
     * @param y top edge
     * @param w total width
     * @param h total height
     */
    private void drawHexagonPill(Graphics2D g2, int x, int y, int w, int h) {
        int point = h / 2;   // how far the point sticks out on each side

        int[] xs = {
            x + point,          // top-left (after the point)
            x + w - point,      // top-right (before the point)
            x + w,              // right point
            x + w - point,      // bottom-right
            x + point,          // bottom-left
            x                   // left point
        };
        int[] ys = {
            y,
            y,
            y + h / 2,
            y + h,
            y + h,
            y + h / 2
        };

        g2.fillPolygon(xs, ys, 6);
    }

    /**
     * Draws word-wrapped text starting at the given top-left corner.
     *
     * @return the cursor Y position below the last rendered line
     */
    private int drawWrappedText(Graphics2D g2, String text, int x, int yTop, int maxWidth) {
        FontMetrics fm = g2.getFontMetrics();
        int lineHeight = fm.getHeight() + LINE_EXTRA;
        int y = yTop + fm.getAscent();

        String[] words = text.split(" ");
        StringBuilder buf = new StringBuilder();
        java.util.List<String> renderedLines = new java.util.ArrayList<>();

        for (String word : words) {
            String test = buf.length() == 0 ? word : buf + " " + word;
            if (fm.stringWidth(test) > maxWidth && buf.length() > 0) {
                renderedLines.add(buf.toString());
                buf = new StringBuilder(word);
            } else {
                if (buf.length() > 0) buf.append(" ");
                buf.append(word);
            }
        }
        if (buf.length() > 0) renderedLines.add(buf.toString());

        // Draw the lines — centered or left-aligned.
        for (String line : renderedLines) {
            int drawX = x;
            if (centerText) {
                drawX = x + (maxWidth - fm.stringWidth(line)) / 2;
            }
            g2.drawString(line, drawX, y);
            y += lineHeight;
        }

        return y - fm.getAscent();   // cursor below the last line
    }

    /**
     * Computes the preferred height of the box for the given width and list
     * of lines, accounting for speaker pills, wrapped text, and padding.
     */
    public int getPreferredHeight(int width, java.util.List<Line> list) {
        if (list == null || list.isEmpty()) return 0;

        java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        int maxW = width - PADDING_X * 2;
        int total = PADDING_TOP;

        for (int i = 0; i < list.size(); i++) {
            Line line = list.get(i);

            if (line.speaker != null && !line.speaker.isEmpty()) {
                g2.setFont(nameFont);
                FontMetrics nfm = g2.getFontMetrics();
                total += nfm.getHeight() + NAME_PILL_PAD_Y * 2 + GAP_NAME_BODY;
            }

            g2.setFont(bodyFont);
            FontMetrics bfm = g2.getFontMetrics();
            total += countLines(bfm, line.text, maxW) * (bfm.getHeight() + LINE_EXTRA);

            if (i < list.size() - 1) total += GAP_BETWEEN;
        }

        g2.dispose();
        return total + PADDING_BOTTOM;
    }

    /** Counts how many wrapped lines the given text will occupy at the given width. */
    private int countLines(FontMetrics fm, String text, int maxWidth) {
        String[] words = text.split(" ");
        StringBuilder buf = new StringBuilder();
        int count = 1;
        for (String word : words) {
            String test = buf.length() == 0 ? word : buf + " " + word;
            if (fm.stringWidth(test) > maxWidth && buf.length() > 0) {
                count++;
                buf = new StringBuilder(word);
            } else {
                if (buf.length() > 0) buf.append(" ");
                buf.append(word);
            }
        }
        return count;
    }
}