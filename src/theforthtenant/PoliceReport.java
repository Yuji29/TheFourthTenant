/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package theforthtenant;

/**
 *
 * @author yuji
 */
public class PoliceReport extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PoliceReport.class.getName());
    
    private javax.swing.Timer typewriterTimer;
    private String typingFull = "";
    private javax.swing.JTextArea typingArea;
    private FadeOverlay fadeOverlay;    
    private javax.sound.sampled.Clip voiceClip;
    private javax.sound.sampled.Clip ambienceClip;
    private String currentAmbiencePath = "";
    private PauseMenu pauseMenu;
    private OptionsMenu optionsMenu;
    
    // Add near your other fields at the top of the class
    private boolean transitioning = false;
    
    // 1. The slideshow array
    private final String[] slides = {
        "/Images/policeReport/slide0.png",
        "/Images/policeReport/slide1.png",
        "/Images/policeReport/slide2.png",
        "/Images/policeReport/slide3.png",
        "/Images/policeReport/slide4.png",
        "/Images/policeReport/slide5.png"
    };
    
    private int currentSlide = 0;
    
    /**
     * Creates new form PoliceReport
     */
    public PoliceReport() {
        initComponents();
        
        // ---- Window icon ----
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
        
        // ---- Options Menu ----
        optionsMenu = OptionsMenu.attachTo(this, new OptionsMenu.Callbacks() {
            @Override public void onSoundChanged(int percent) {
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void onClose() {
                pauseMenu.showPopupOnly();
            }
        });

        // ---- Pause Menu ----
        pauseMenu = PauseMenu.attachTo(this, PauseMenu.Corner.TOP_LEFT, new PauseMenu.Callbacks() {
            @Override public void onPause() {
                // Stop the background ambience and voice-over when paused
                if (ambienceClip != null) ambienceClip.stop();
                if (voiceClip != null) voiceClip.stop();
                if (typewriterTimer != null) typewriterTimer.stop();
            }
            @Override public void onResume() {
                // Resume the ambience
                if (ambienceClip != null) ambienceClip.start();
                
                // Resume the voice and typewriter if they were playing
                if (voiceClip != null) voiceClip.start();
                if (typewriterTimer != null) typewriterTimer.start();
            }
            @Override public void onOptions() {
                pauseMenu.hidePopupOnly();
                optionsMenu.show();
            }
            @Override public void onMainMenu() {
                if (TransitionOverlay.isPlaying()) return;
                pauseMenu.hideOverlay();

                TransitionOverlay.play(PoliceReport.this, () -> {
                    MainMenu menu = new MainMenu();
                    menu.setLocation(getLocation());
                    menu.setVisible(true);
                    PoliceReport.this.dispose();
                });
            }
        });            

        // Load the first slide
        showSlide(0);

        // ---- Fade In on Start ----
        fadeOverlay = new FadeOverlay();
        contentPanel.add(fadeOverlay,
                new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1150, 680));
        contentPanel.setComponentZOrder(fadeOverlay, 0);

        javax.swing.Timer starter = new javax.swing.Timer(150, e -> fadeIn(fadeOverlay));
        starter.setRepeats(false);
        starter.start();

        // ---- Style both text areas ----
        styleTextBox(dialogue0TextArea,          jScrollPane1);
        styleTextBox(dialogue1TextArea,          jScrollPane2);
        styleTextBox(dialogue2TextArea,          jScrollPane3);
        styleTextBox(dialogue3detectiveTextArea, jScrollPane4);
        styleTextBox(dialogue3policeTextArea,    jScrollPane5);
        styleTextBox(dialogue4TextArea,          jScrollPane6);
        styleTextBox(dialogue5TextArea,          jScrollPane7);

        // ---- Keyboard navigation: A / ← previous, D / → next ----
        javax.swing.JRootPane root = getRootPane();
        javax.swing.InputMap im = root.getInputMap(
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap am = root.getActionMap();

        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT,  0), "prevSlide");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), "nextSlide");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A,     0), "prevSlide");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D,     0), "nextSlide");

        am.put("prevSlide", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                previous();
            }
        });
        am.put("nextSlide", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                advance();
            }
        });     
        
        // ---- Previous / Next Button Click & Hover Effects ----
        final javax.swing.ImageIcon prevNormal = new javax.swing.ImageIcon(
                getClass().getResource("/Images/common/previous_button.png"));
        final javax.swing.ImageIcon prevHover = new javax.swing.ImageIcon(
                getClass().getResource("/Images/common/previous_button_hovered.png"));
                
        final javax.swing.ImageIcon nextNormal = new javax.swing.ImageIcon(
                getClass().getResource("/Images/common/next_button.png"));
        final javax.swing.ImageIcon nextHover = new javax.swing.ImageIcon(
                getClass().getResource("/Images/common/next_button_hovered.png"));

        // --- Previous Button ---
        prevButton.setIcon(prevNormal);
        prevButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        prevButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!transitioning) {
                    prevButton.setIcon(prevHover);
                    AudioCache.play("/audio/sfx/ui/hover.wav"); // Hover sound
                }
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                prevButton.setIcon(prevNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                // previous() handles its own select.wav sound!
                AudioCache.play("/audio/sfx/gameplay/thud.wav"); // Optional: adds the standard navigation thud
                previous();
            }
        });

        // --- Next Button ---
        nextButton.setIcon(nextNormal);
        nextButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        nextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!transitioning) {
                    nextButton.setIcon(nextHover);
                    AudioCache.play("/audio/sfx/ui/hover.wav"); // Hover sound
                }
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                nextButton.setIcon(nextNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                // advance() handles its own select.wav / zipper.wav sound!
                AudioCache.play("/audio/sfx/gameplay/thud.wav"); // Optional: adds the standard navigation thud
                advance();
            }
        });     
    }

    private void showSlide(int index) {

        // ---- Ambience Track Swapping ----
        String newAmbience;
        switch (index) {
            case 0:  newAmbience = "/audio/sfx/ambience/rain_loop.wav"; break;
            case 1:  newAmbience = "/audio/sfx/ambience/rain_loop.wav"; break;
            case 2:  newAmbience = "/audio/sfx/ambience/indoor_hum.wav"; break;
            case 3:  newAmbience = "/audio/sfx/ambience/rain_loop.wav"; break;
            case 4:  newAmbience = "/audio/sfx/ambience/indoor_hum.wav"; break;
            case 5:  newAmbience = "/audio/sfx/ambience/indoor_hum.wav"; break;
            default: newAmbience = "/audio/sfx/ambience/indoor_hum.wav"; break;
        }

        // Only restart the audio if the track is actually changing
        if (!newAmbience.equals(currentAmbiencePath)) {
            if (ambienceClip != null) {
                ambienceClip.stop();
                ambienceClip = null;
            }
            ambienceClip = AudioCache.loop(newAmbience);
            currentAmbiencePath = newAmbience;
        }
        
        if (index < 0) index = 0;
        if (index >= slides.length) index = slides.length - 1;
        currentSlide = index;

        java.net.URL url = getClass().getResource(slides[index]);
        if (url != null) {
            backgroundLabel.setIcon(new javax.swing.ImageIcon(url));
        }

        // 1. Hide EVERYTHING first
        jScrollPane1.setVisible(false);
        jScrollPane2.setVisible(false);
        jScrollPane3.setVisible(false);
        jScrollPane4.setVisible(false); 
        jScrollPane5.setVisible(false); 
        jScrollPane6.setVisible(false); 
        jScrollPane7.setVisible(false); 
        
        dialogue1.setVisible(false);    
        dialogue2.setVisible(false);    
        dialogue3detective.setVisible(false); 
        dialogue3police.setVisible(false);    
        dialogue4.setVisible(false);    
        dialogue5.setVisible(false);    // <--- Slide 5 Nametag

        if (typewriterTimer != null) typewriterTimer.stop();

        // 2. Show specific text for each slide
        if (index == 0) {
            jScrollPane1.setVisible(true);
            
            // Play the police siren once
            AudioCache.play("/audio/backstory_sfx/siren_distant.wav");
            
            startTypewriter(dialogue0TextArea, "[Police siren wailing]", null);
            
        } else if (index == 1) {
            jScrollPane2.setVisible(true);
            dialogue1.setVisible(true); 
            startTypewriter(dialogue1TextArea, "Status report. What did the field team and the lab pull from the perimeter?", 
                "/audio/voice/policereport/detective_s01.wav");
            
        } else if (index == 2) {
            jScrollPane3.setVisible(true);
            dialogue2.setVisible(true); 
            startTypewriter(dialogue2TextArea, "Everything we needed, Detective. The lab rushed the organics from that rag under Cathy's bed—DNA match comes back positive for Abby's blood mixed with Cathy's skin cells. Her 'someone planted it' excuse just evaporated.", 
                "/audio/voice/policereport/police_s02.wav");
            
        } else if (index == 3) {
            jScrollPane5.setVisible(true);
            dialogue3police.setVisible(true);

            jScrollPane4.setVisible(false);     
            dialogue3detective.setVisible(false);

            startTypewriterWithCallback(dialogue3policeTextArea, 
                "Neighbors on the south side were up late feeding strays. One of them stepped out at 2:35 AM and saw someone matching Cathy's exact height and build walking straight toward the utility shed.", 
                "/audio/voice/policereport/police_s03.wav",
                () -> {
                    jScrollPane4.setVisible(true);
                    dialogue3detective.setVisible(true);
                    startTypewriter(dialogue3detectiveTextArea, "And the timeline? Any movement outside the house during those missing hours?", 
                        "/audio/voice/policereport/detective_s03.wav");
                }
            );
        } else if (index == 4) {
            jScrollPane6.setVisible(true);
            dialogue4.setVisible(true);       
            startTypewriter(dialogue4TextArea, "That lines up down to the minute with the acoustic log from the hallway mic and Abby's smartwatch flatlining right after.", 
                "/audio/voice/policereport/police_s04.wav");

        } else if (index == 5) {
            jScrollPane7.setVisible(true);
            dialogue5.setVisible(true);       
            startTypewriter(dialogue5TextArea, "Good. The alibis of the other housemates checked out, leaving her room as the only intersection for these pieces. Let's bring her back in. Time to close the loop.", 
                "/audio/voice/policereport/detective_s05.wav");
        }
        
        // Hide Previous on the first slide, hide Next on the last slide
        prevButton.setVisible(currentSlide > 0);
        nextButton.setVisible(currentSlide < slides.length - 1);
    }
    
    /**
    * Advances to the next slide, or finishes the typewriter if it is still
    * running. Identical to the "click anywhere" behavior.
    */
    private void advance() {
        if (transitioning) return;
        if (pauseMenu != null && pauseMenu.isPaused()) return;
        // 1. If text is still typing, finish it instantly.
        if (typewriterTimer != null && typewriterTimer.isRunning()) {
            typewriterTimer.stop();
            typingArea.setText(typingFull);
            
            // Stop the voice clip immediately
            if (voiceClip != null) { voiceClip.stop(); voiceClip = null; }

            // Slide 3 special case...
            if (currentSlide == 3 && typingArea == dialogue3policeTextArea) {
                jScrollPane4.setVisible(true);
                dialogue3detective.setVisible(true);
                startTypewriter(dialogue3detectiveTextArea,
                   "And the timeline? Any movement outside the house during those missing hours?", 
                   "/audio/voice/policereport/detective_s03.wav"); // <-- Add the detective's audio path here
            }
            return;
        }

       // 2. Otherwise advance.
       if (currentSlide < slides.length - 1) {
           AudioCache.play("/audio/sfx/ui/select.wav");
           showSlide(currentSlide + 1);
       } else {
           AudioCache.play("/audio/sfx/ui/zipper.wav");
           returnToCrimeScene();
       }
   }

   /**
    * Goes back to the previous slide. Stops any typewriter in progress and
    * re-renders the previous slide from the beginning.
    */
   private void previous() {
       if (transitioning) return;
       if (pauseMenu != null && pauseMenu.isPaused()) return;
       if (currentSlide <= 0) return;
       AudioCache.play("/audio/sfx/ui/select.wav");
       showSlide(currentSlide - 1);
   }

    /** Exits the report, fades out, and goes back to the Crime Scene. */
    private void returnToCrimeScene() {
        if (transitioning) return;
        transitioning = true;
        
        GameState.setPoliceReportDone(true);
        
        if (pauseMenu != null) pauseMenu.hideOverlay();
        
        // Stop the ambience immediately
        if (ambienceClip != null) { ambienceClip.stop(); ambienceClip = null; }
        if (typewriterTimer != null) typewriterTimer.stop();         
        
        if (voiceClip != null) { voiceClip.stop(); voiceClip = null; }
        if (typewriterTimer != null) typewriterTimer.stop();
        
        FadeOverlay fade = new FadeOverlay();
        contentPanel.add(fade, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1150, 680));
        contentPanel.setComponentZOrder(fade, 0);
        
        final int DURATION_MS = 600;
        final long start = System.currentTimeMillis();
        
        javax.swing.Timer fadeTimer = new javax.swing.Timer(16, null);
        fadeTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - start;
            float p = Math.min(1f, elapsed / (float) DURATION_MS);
            
            fade.setAlpha(p); // Fade to black
            
            if (p >= 1f) {
                fadeTimer.stop();
                CrimeScene cs = new CrimeScene();
                cs.setLocation(getLocation());
                cs.setVisible(true);
                PoliceReport.this.dispose();
            }
        });
        fadeTimer.start();
    }
    
    /** Loads the custom pixel font at the given size, or falls back to Segoe UI. */
    private java.awt.Font getCustomFont(float size) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/fonts/press_start_2p.ttf");
            java.awt.Font base = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
            return base.deriveFont(size);
        } catch (Exception e) {
            System.out.println("Font not found! Using default.");
            return new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, (int) size);
        }
    }
    
    @Override
    public void dispose() {
        if (ambienceClip != null) { ambienceClip.stop(); ambienceClip = null; }
        if (typewriterTimer != null) typewriterTimer.stop();
        if (voiceClip != null) { voiceClip.stop(); voiceClip = null; }
        super.dispose();
    }
    
    /** Animates typing text letter by letter, perfectly synced to the voice-over. */
    private void startTypewriter(javax.swing.JTextArea targetArea, String fullText, String audioPath) {
        if (typewriterTimer != null) typewriterTimer.stop();
        if (voiceClip != null) { voiceClip.stop(); voiceClip = null; }
        
        typingFull = fullText;
        typingArea = targetArea;
        targetArea.setText("");

        long audioDurationMs = 0;

        // 1. Start the voice clip and get its length
        if (audioPath != null) {
            voiceClip = AudioCache.get(audioPath);
            if (voiceClip != null) {
                voiceClip.stop();
                voiceClip.setFramePosition(0);
                audioDurationMs = voiceClip.getMicrosecondLength() / 1000; // Convert to ms
                voiceClip.start();
            }
        }

        // 2. Calculate typing speed
        final int tickMs;
        final double charsPerSecond;

        if (voiceClip != null && audioDurationMs > 0 && fullText.length() > 0) {
            tickMs = 25; // Update every 25ms
            // Sync perfectly with audio, but type 10% faster so it finishes right before the voice
            charsPerSecond = (fullText.length() / (audioDurationMs / 1000.0)) * 1.1; 
        } else {
            // Fallback speed if there is no audio (e.g. "[Police siren wailing]")
            tickMs = 30;
            charsPerSecond = 50.0; 
        }

        final double charsPerTick = charsPerSecond * (tickMs / 1000.0);
        final double[] accumulated = { 0.0 };

        typewriterTimer = new javax.swing.Timer(tickMs, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                accumulated[0] += charsPerTick;
                int n = Math.min(fullText.length(), (int) accumulated[0]);
                targetArea.setText(fullText.substring(0, n));

                if (n >= fullText.length()) {
                    typewriterTimer.stop();
                }
            }
        });
        typewriterTimer.start();
    }    

    /** Animates typing perfectly synced to voice-over, then runs the onDone callback. */
    private void startTypewriterWithCallback(javax.swing.JTextArea targetArea, String fullText, String audioPath, Runnable onDone) {
        if (typewriterTimer != null) typewriterTimer.stop();
        if (voiceClip != null) { voiceClip.stop(); voiceClip = null; }
        
        typingFull = fullText;
        typingArea = targetArea;
        targetArea.setText("");

        long audioDurationMs = 0;

        // 1. Start the voice clip and get its length
        if (audioPath != null) {
            voiceClip = AudioCache.get(audioPath);
            if (voiceClip != null) {
                voiceClip.stop();
                voiceClip.setFramePosition(0);
                audioDurationMs = voiceClip.getMicrosecondLength() / 1000; 
                voiceClip.start();
            }
        }

        // 2. Calculate typing speed
        final int tickMs;
        final double charsPerSecond;

        if (voiceClip != null && audioDurationMs > 0 && fullText.length() > 0) {
            tickMs = 25;
            charsPerSecond = (fullText.length() / (audioDurationMs / 1000.0)) * 1.1; 
        } else {
            tickMs = 25;
            charsPerSecond = 50.0;
        }

        final double charsPerTick = charsPerSecond * (tickMs / 1000.0);
        final double[] accumulated = { 0.0 };

        typewriterTimer = new javax.swing.Timer(tickMs, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                accumulated[0] += charsPerTick;
                int n = Math.min(fullText.length(), (int) accumulated[0]);
                targetArea.setText(fullText.substring(0, n));

                if (n >= fullText.length()) {
                    typewriterTimer.stop();
                    if (onDone != null) onDone.run();
                }
            }
        });
        typewriterTimer.start();
    }      
    
    /** Applies the transparent interrogation style to a text area. */
    private void styleTextBox(javax.swing.JTextArea area, javax.swing.JScrollPane pane) {
        java.awt.Color clear = new java.awt.Color(0, 0, 0, 0);
        
        // 1. Text Area styling
        area.setOpaque(false);
        area.setBackground(clear);
        area.setEditable(false);
        area.setForeground(java.awt.Color.WHITE);
        area.setFont(getCustomFont(12f));
        area.setMargin(new java.awt.Insets(5, 5, 5, 5));
        
        // 2. The magic Nimbus fix
        area.setUI(new javax.swing.plaf.basic.BasicTextAreaUI());
        
        // 3. ScrollPane styling
        pane.setOpaque(false);
        pane.setBackground(clear);
        pane.setBorder(null);
        pane.setViewportBorder(null);
        pane.getViewport().setOpaque(false);
        pane.getViewport().setBackground(clear);
        pane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        pane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }   
    
    /** Fades the overlay from black to transparent. */
    private void fadeIn(FadeOverlay overlay) {
        if (overlay == null) return;

        final long startTime = System.currentTimeMillis();
        final int DURATION_MS = 800;

        javax.swing.Timer t = new javax.swing.Timer(16, null);
        t.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1f, elapsed / (float) DURATION_MS);

            overlay.setAlpha(1f - progress);   // 1 → 0

            if (progress >= 1f) {
                ((javax.swing.Timer) e.getSource()).stop();
                contentPanel.remove(overlay);
                fadeOverlay = null;
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        });
        t.start();
    }

    /** Simple black overlay that fades. */
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
            g2.setColor(new java.awt.Color(0, 0, 0, (int)(alpha * 255)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
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
        jScrollPane7 = new javax.swing.JScrollPane();
        dialogue5TextArea = new javax.swing.JTextArea();
        dialogue5 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        dialogue4TextArea = new javax.swing.JTextArea();
        dialogue4 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        dialogue3policeTextArea = new javax.swing.JTextArea();
        dialogue3police = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        dialogue3detectiveTextArea = new javax.swing.JTextArea();
        dialogue3detective = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        dialogue2TextArea = new javax.swing.JTextArea();
        dialogue2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        dialogue1TextArea = new javax.swing.JTextArea();
        dialogue1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        dialogue0TextArea = new javax.swing.JTextArea();
        nextButton = new javax.swing.JLabel();
        prevButton = new javax.swing.JLabel();
        backgroundLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        contentPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane7.setBorder(null);
        jScrollPane7.setOpaque(false);

        dialogue5TextArea.setEditable(false);
        dialogue5TextArea.setColumns(20);
        dialogue5TextArea.setLineWrap(true);
        dialogue5TextArea.setRows(5);
        dialogue5TextArea.setWrapStyleWord(true);
        dialogue5TextArea.setOpaque(false);
        jScrollPane7.setViewportView(dialogue5TextArea);

        contentPanel.add(jScrollPane7, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 530, 880, -1));

        dialogue5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/policeReport/dialogue/dialogue5.png"))); // NOI18N
        contentPanel.add(dialogue5, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 470, -1, -1));

        jScrollPane6.setBorder(null);
        jScrollPane6.setOpaque(false);

        dialogue4TextArea.setEditable(false);
        dialogue4TextArea.setColumns(20);
        dialogue4TextArea.setLineWrap(true);
        dialogue4TextArea.setRows(5);
        dialogue4TextArea.setWrapStyleWord(true);
        dialogue4TextArea.setOpaque(false);
        jScrollPane6.setViewportView(dialogue4TextArea);

        contentPanel.add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 530, 880, -1));

        dialogue4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/policeReport/dialogue/dialogue4.png"))); // NOI18N
        contentPanel.add(dialogue4, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 470, -1, -1));

        jScrollPane5.setBorder(null);
        jScrollPane5.setOpaque(false);

        dialogue3policeTextArea.setEditable(false);
        dialogue3policeTextArea.setColumns(20);
        dialogue3policeTextArea.setLineWrap(true);
        dialogue3policeTextArea.setRows(5);
        dialogue3policeTextArea.setWrapStyleWord(true);
        dialogue3policeTextArea.setOpaque(false);
        jScrollPane5.setViewportView(dialogue3policeTextArea);

        contentPanel.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 530, 570, 60));

        dialogue3police.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/policeReport/dialogue/dialogue3.1.png"))); // NOI18N
        contentPanel.add(dialogue3police, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 470, -1, -1));

        jScrollPane4.setBorder(null);
        jScrollPane4.setOpaque(false);

        dialogue3detectiveTextArea.setEditable(false);
        dialogue3detectiveTextArea.setColumns(20);
        dialogue3detectiveTextArea.setLineWrap(true);
        dialogue3detectiveTextArea.setRows(5);
        dialogue3detectiveTextArea.setWrapStyleWord(true);
        dialogue3detectiveTextArea.setOpaque(false);
        jScrollPane4.setViewportView(dialogue3detectiveTextArea);

        contentPanel.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 110, 490, 40));

        dialogue3detective.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/policeReport/dialogue/dialogue3.2.png"))); // NOI18N
        contentPanel.add(dialogue3detective, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 50, -1, -1));

        jScrollPane3.setBorder(null);
        jScrollPane3.setOpaque(false);

        dialogue2TextArea.setEditable(false);
        dialogue2TextArea.setColumns(20);
        dialogue2TextArea.setLineWrap(true);
        dialogue2TextArea.setRows(5);
        dialogue2TextArea.setWrapStyleWord(true);
        dialogue2TextArea.setOpaque(false);
        jScrollPane3.setViewportView(dialogue2TextArea);

        contentPanel.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 530, 880, -1));

        dialogue2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/policeReport/dialogue/dialogue2.png"))); // NOI18N
        contentPanel.add(dialogue2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 470, -1, -1));

        jScrollPane2.setBorder(null);
        jScrollPane2.setOpaque(false);

        dialogue1TextArea.setEditable(false);
        dialogue1TextArea.setColumns(20);
        dialogue1TextArea.setLineWrap(true);
        dialogue1TextArea.setRows(5);
        dialogue1TextArea.setWrapStyleWord(true);
        dialogue1TextArea.setOpaque(false);
        jScrollPane2.setViewportView(dialogue1TextArea);

        contentPanel.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 530, 880, -1));

        dialogue1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/policeReport/dialogue/dialogue1.png"))); // NOI18N
        contentPanel.add(dialogue1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 480, -1, -1));

        jScrollPane1.setBorder(null);
        jScrollPane1.setOpaque(false);

        dialogue0TextArea.setEditable(false);
        dialogue0TextArea.setColumns(20);
        dialogue0TextArea.setLineWrap(true);
        dialogue0TextArea.setRows(5);
        dialogue0TextArea.setWrapStyleWord(true);
        dialogue0TextArea.setOpaque(false);
        jScrollPane1.setViewportView(dialogue0TextArea);

        contentPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 570, 380, 40));

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/common/next_button.png"))); // NOI18N
        contentPanel.add(nextButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1110, 320, -1, -1));

        prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/common/previous_button.png"))); // NOI18N
        prevButton.setText("\n");
        prevButton.setPreferredSize(new java.awt.Dimension(40, 40));
        contentPanel.add(prevButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 320, -1, -1));

        backgroundLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/policeReport/slide0.png"))); // NOI18N
        contentPanel.add(backgroundLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 680));

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
        java.awt.EventQueue.invokeLater(() -> new PoliceReport().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel backgroundLabel;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JTextArea dialogue0TextArea;
    private javax.swing.JLabel dialogue1;
    private javax.swing.JTextArea dialogue1TextArea;
    private javax.swing.JLabel dialogue2;
    private javax.swing.JTextArea dialogue2TextArea;
    private javax.swing.JLabel dialogue3detective;
    private javax.swing.JTextArea dialogue3detectiveTextArea;
    private javax.swing.JLabel dialogue3police;
    private javax.swing.JTextArea dialogue3policeTextArea;
    private javax.swing.JLabel dialogue4;
    private javax.swing.JTextArea dialogue4TextArea;
    private javax.swing.JLabel dialogue5;
    private javax.swing.JTextArea dialogue5TextArea;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JLabel nextButton;
    private javax.swing.JLabel prevButton;
    // End of variables declaration//GEN-END:variables
}

