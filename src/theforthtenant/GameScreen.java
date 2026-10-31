package theforthtenant;

import javax.swing.JFrame;

public abstract class GameScreen extends JFrame {

    public GameScreen() {
        // Setup Window Icon
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
    }

    protected abstract void setupScreen();
}