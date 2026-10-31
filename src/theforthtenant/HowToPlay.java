/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package theforthtenant;

/**
 *
 * @author yuji
 */
public class HowToPlay extends GameScreen {
    
    private int currentPage = 0;
    private final String[] pages = {
        "/Images/howtoplay/page1.png",
        "/Images/howtoplay/page2.png",
        "/Images/howtoplay/page3.png"
    };    
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(HowToPlay.class.getName());

    /**
     * Creates new form HowToPlay
     */
    public HowToPlay() {
        initComponents();
        setupScreen();
    }
    
    @Override
    protected void setupScreen() {
        
        // ---- Back Button (Goes to Main Menu) ----
        final javax.swing.ImageIcon backNormal =
                new javax.swing.ImageIcon(getClass().getResource("/Images/ui/back_button.png"));
        final javax.swing.ImageIcon backHover =
                new javax.swing.ImageIcon(getClass().getResource("/Images/ui/back_button_hovered.png"));

        backButton.setIcon(backNormal);
        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                backButton.setIcon(backHover);
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                backButton.setIcon(backNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                AudioCache.play("/audio/sfx/ui/select.wav");
                MainMenu menu = new MainMenu();
                menu.setLocation(getLocation());
                menu.setVisible(true);
                dispose();
            }
        });

        // ---- Prev / Next Buttons ----
        final javax.swing.ImageIcon prevNormal = new javax.swing.ImageIcon(getClass().getResource("/Images/common/previous_button.png"));
        final javax.swing.ImageIcon prevHover  = new javax.swing.ImageIcon(getClass().getResource("/Images/common/previous_button_hovered.png"));
        final javax.swing.ImageIcon nextNormal = new javax.swing.ImageIcon(getClass().getResource("/Images/common/next_button.png"));
        final javax.swing.ImageIcon nextHover  = new javax.swing.ImageIcon(getClass().getResource("/Images/common/next_button_hovered.png"));

        prevButton.setIcon(prevNormal);
        prevButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                prevButton.setIcon(prevHover);
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                prevButton.setIcon(prevNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (currentPage > 0) {
                    AudioCache.play("/audio/sfx/ui/select.wav");
                    currentPage--;
                    updatePage();
                }
            }
        });

        nextButton.setIcon(nextNormal);
        nextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                nextButton.setIcon(nextHover);
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                nextButton.setIcon(nextNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (currentPage < pages.length - 1) {
                    AudioCache.play("/audio/sfx/ui/select.wav");
                    currentPage++;
                    updatePage();
                }
            }
        });

        // ---- Keyboard Shortcuts ----
        javax.swing.JRootPane root = getRootPane();
        javax.swing.InputMap im = root.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap am = root.getActionMap();

        // ESC to go back
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "backAction");
        am.put("backAction", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                AudioCache.play("/audio/sfx/ui/select.wav");
                MainMenu menu = new MainMenu();
                menu.setLocation(getLocation());
                menu.setVisible(true);
                dispose();
            }
        });

        // A / Left Arrow -> Prev Page
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, 0), "prevPage");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0), "prevPage");
        am.put("prevPage", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (currentPage > 0) {
                    AudioCache.play("/audio/sfx/ui/select.wav");
                    currentPage--;
                    updatePage();
                }
            }
        });

        // D / Right Arrow -> Next Page
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, 0), "nextPage");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), "nextPage");
        am.put("nextPage", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (currentPage < pages.length - 1) {
                    AudioCache.play("/audio/sfx/ui/select.wav");
                    currentPage++;
                    updatePage();
                }
            }
        });

        // Initialize the first page
        updatePage();
        
        setLocationRelativeTo(null);
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
        nextButton = new javax.swing.JLabel();
        prevButton = new javax.swing.JLabel();
        backgroundLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("The Fourth Tenant");

        contentPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ui/back_button.png"))); // NOI18N
        backButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(backButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 635, -1, -1));

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/common/next_button.png"))); // NOI18N
        nextButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(nextButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1103, 320, -1, -1));

        prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/common/previous_button.png"))); // NOI18N
        prevButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        contentPanel.add(prevButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 320, -1, -1));

        backgroundLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/howtoplay/page1.png"))); // NOI18N
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
    
    /** Updates the background image and hides/shows arrows based on the current page. */
    private void updatePage() {
        // 1. Swap the background image
        backgroundLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource(pages[currentPage])));
        
        // 2. Hide arrows if we are on the first or last page
        prevButton.setVisible(currentPage > 0);
        nextButton.setVisible(currentPage < pages.length - 1);
        
        // 3. Bring them to the front so they are clickable
        contentPanel.setComponentZOrder(prevButton, 0);
        contentPanel.setComponentZOrder(nextButton, 0);
        contentPanel.setComponentZOrder(backButton, 0);
        
        // 4. Refresh
        contentPanel.revalidate();
        contentPanel.repaint();
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
        java.awt.EventQueue.invokeLater(() -> new HowToPlay().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel backButton;
    private javax.swing.JLabel backgroundLabel;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JLabel nextButton;
    private javax.swing.JLabel prevButton;
    // End of variables declaration//GEN-END:variables
}
