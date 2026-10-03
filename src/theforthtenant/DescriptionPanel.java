package theforthtenant;

public class DescriptionPanel extends javax.swing.JComponent {

    private static final int PAD = 20;
    private static final int IMG_MAX_W = 260;
    private static final int IMG_MAX_H = 220;
    private static final int CONTENT_OFFSET_X = -12;
    private static final int NAME_TO_BODY_GAP = 60;  
    private static final int EMPTY_IMAGE_OFFSET_Y = 60;

    private javax.swing.ImageIcon image;
    private String name;
    private String body;

    private java.awt.Font nameFont;
    private java.awt.Font bodyFont;

    public DescriptionPanel() {
        setOpaque(false);
        setBackground(new java.awt.Color(30, 30, 30));
        try {
            java.io.InputStream is = DescriptionPanel.class.getResourceAsStream(
                    "/fonts/press_start_2p.ttf");
            java.awt.Font base = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
            nameFont = base.deriveFont(11f);
            bodyFont = base.deriveFont(10f);
        } catch (Exception e) {
            nameFont = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);
            bodyFont = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12);
        }
    }

    public void setItem(ItemDatabase.Item item) {
        if (item == null) {
            image = null; name = null; body = null;
        } else {
            java.net.URL u = DescriptionPanel.class.getResource(
                    ItemDatabase.descriptionImagePath(item.id));
            image = (u != null) ? new javax.swing.ImageIcon(u) : null;
            name  = item.displayName;
            body  = item.description;
        }
        repaint();
    }

    /** Empty-slot state: shows description_slot_empty.png + the hint. */
    public void showEmpty() {
        java.net.URL u = DescriptionPanel.class.getResource(
                "/Images/gameplay/inventory/item_description/description_slot_empty.png");
        image = (u != null) ? new javax.swing.ImageIcon(u) : null;
        name  = null;
        body  = null;
        repaint();
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                            java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        int w = getWidth();
        int cx = w / 2 + CONTENT_OFFSET_X;

        // --- Large description image ---
        int imgBottomY = PAD + 20;
        if (image != null) {
            int iw = image.getIconWidth();
            int ih = image.getIconHeight();
            double scale = Math.min(IMG_MAX_W / (double) iw, IMG_MAX_H / (double) ih);
            int dw = (int) (iw * scale);
            int dh = (int) (ih * scale);
            int dx = cx - dw / 2;

            // Empty-state art sits lower than real item art.
            int dy = PAD + 10 + (name == null ? EMPTY_IMAGE_OFFSET_Y : 0);

            g2.drawImage(image.getImage(), dx, dy, dw, dh, null);
            imgBottomY = dy + dh;
        }

        // --- Red display name ---
        g2.setFont(nameFont);
        g2.setColor(new java.awt.Color(200, 50, 45));
        java.awt.FontMetrics nfm = g2.getFontMetrics();
        String displayName = (name != null) ? name : "";
        int nameW = nfm.stringWidth(displayName);
        int nameY = imgBottomY + nfm.getAscent() + 16;
        g2.drawString(displayName, cx - nameW / 2, nameY);

        // --- Body text ---
        g2.setFont(bodyFont);
        g2.setColor(new java.awt.Color(230, 230, 225));
        java.awt.FontMetrics bfm = g2.getFontMetrics();
        int textY = nameY + NAME_TO_BODY_GAP;
        int textMaxW = w - PAD * 2;
        int textX = PAD + CONTENT_OFFSET_X;
        if (body != null) {
            textY = drawWrapped(g2, body, textX, textY, textMaxW, bfm);
        }

        // --- Empty-state hint ---
        if (name == null) {
            g2.setFont(bodyFont);
            g2.setColor(new java.awt.Color(120, 120, 120));
            String hint = "Select an item to inspect";
            int hw = bfm.stringWidth(hint);
            g2.drawString(hint, cx - hw / 2, getHeight() - 150);
        }

        g2.dispose();
    }

    private int drawWrapped(java.awt.Graphics2D g2, String text, int x, int y,
                            int maxWidth, java.awt.FontMetrics fm) {
        int lineHeight = fm.getHeight() + 2;
        for (String paragraph : text.split("\n")) {
            String[] words = paragraph.split(" ");
            StringBuilder buf = new StringBuilder();
            for (String word : words) {
                String test = buf.length() == 0 ? word : buf + " " + word;
                if (fm.stringWidth(test) > maxWidth && buf.length() > 0) {
                    g2.drawString(buf.toString(), x, y);
                    y += lineHeight;
                    buf = new StringBuilder(word);
                } else {
                    if (buf.length() > 0) buf.append(' ');
                    buf.append(word);
                }
            }
            if (buf.length() > 0) {
                g2.drawString(buf.toString(), x, y);
                y += lineHeight;
            }
        }
        return y;
    }
}