/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package theforthtenant;

/**
 *
 * @author yuji
 */
public class Backstory extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Backstory.class.getName());
    
    private int currentSlide = 0;
    private SubtitleBox subtitleBoxLeft;
    private SubtitleBox subtitleBoxRight;
    private FadeOverlay fadeOverlay;
    private javax.swing.Timer typewriterTimer; 
    private javax.sound.sampled.Clip currentClip;
    private javax.sound.sampled.Clip rainClip;
    private javax.sound.sampled.Clip phoneRingClip;
    private javax.sound.sampled.Clip thunderClip;
    private javax.sound.sampled.Clip intenseClip;
    private javax.sound.sampled.Clip typewriterKeyClip;
    private long typewriterStartTime;
    private static final long TYPEWRITER_KEY_DURATION_MS = 1500;   // key clicks stop after this
    private boolean navigationLocked = false;
    private static final int NAV_LOCK_MS = 600;   // how long between allowed clicks

    private static class Slide {
        final String imagePath;
        final java.util.List<SubtitleBox.Line> leftLines;
        final java.util.List<SubtitleBox.Line> rightLines;
        final String sfxPath;

        Slide(String imagePath, SubtitleBox.Line... lines) {
            this(imagePath, null, lines);
        }

        Slide(String imagePath, String sfxPath, SubtitleBox.Line... lines) {
            this.imagePath = imagePath;
            this.sfxPath = sfxPath;
            this.leftLines = java.util.Arrays.asList(lines);
            this.rightLines = java.util.Collections.emptyList();
        }

        Slide(String imagePath, String sfxPath,
              java.util.List<SubtitleBox.Line> leftLines,
              java.util.List<SubtitleBox.Line> rightLines) {
            this.imagePath = imagePath;
            this.sfxPath = sfxPath;
            this.leftLines = leftLines;
            this.rightLines = rightLines;
        }
    }
    
    private static java.util.List<SubtitleBox.Line> L(SubtitleBox.Line... lines) {
        return java.util.Arrays.asList(lines);
    }

    private final Slide[] slides = {
        // 1 
        new Slide("/Images/backstory/slide1.png",
            "/audio/backstory/sfx/thunder.wav",
            new SubtitleBox.Line(null, "[Phone ringing]")),

        // 2 
        new Slide("/Images/backstory/slide2.png",
        "/audio/backstory/sfx/receiver_click.wav",
        new SubtitleBox.Line(null, "[Receiver clicks]")),

        // 3
        new Slide("/Images/backstory/slide3.png",
            new SubtitleBox.Line("Detective", "Detective speaking.",
            "/audio/backstory/s03_detective.wav")),

        // 4
        new Slide("/Images/backstory/slide4.png",
            new SubtitleBox.Line("Officer Morales", "Detective, glad I caught you at your desk. We have a 10-54 code over at Barangay San Lorenzo—specifically the old three-story boarding house near the corner lot. We need you on-site immediately.",
            "/audio/backstory/s04_morales.wav")),

        // 5
        new Slide("/Images/backstory/slide5.png",
            new SubtitleBox.Line("Detective", "Give me the basics, Morales. What are we looking at?",
            "/audio/backstory/s05_detective.wav")),

        // 6 
        new Slide("/Images/backstory/slide6.png",
            "/audio/backstory/sfx/siren_distant.wav",
            new SubtitleBox.Line("Officer Morales", "Homicide. The victim is a female tenant named Abby Salle. She was stuffed inside an industrial blue water drum on the open rooftop.",
                    "/audio/backstory/s06_morales.wav")),

        // 7 
        new Slide("/Images/backstory/slide7.png",
            "/audio/backstory/sfx/wind_gust.wav",
            L(new SubtitleBox.Line("Detective", "Has the medical examiner given an initial read?",
                    "/audio/backstory/s07_detective.wav")),
            L(new SubtitleBox.Line("Officer Morales", "The body was discovered just twenty minutes ago, around 6:30 AM, by one of the housemates heading up to do laundry.",
                    "/audio/backstory/s07_morales.wav"))),

        // 8
        new Slide("/Images/backstory/slide8.png",
                "/audio/backstory/sfx/siren_distant.wav",
            new SubtitleBox.Line("Officer Morales", "The coroner just did a preliminary check. Rigor mortis is fairly advanced—they're placing the estimated time of death between 1:30 AM and 2:30 AM earlier today. As for the primary cause of death, preliminary findings show severe head trauma and asphyxiation before she was folded into the drum.",
                    "/audio/backstory/s08_morales.wav")),

        // 9
        new Slide("/Images/backstory/slide9.png",
            new SubtitleBox.Line("Detective", "Anyone secured the area? Who's at the location?",
                    "/audio/backstory/s09_detective.wav")),

        // 10
        new Slide("/Images/backstory/slide10.png",
                "/audio/backstory/sfx/siren_distant.wav",
            new SubtitleBox.Line("Officer Morales", "The rooftop and the entire boarding house are sealed under standard perimeter protocol.",
                    "/audio/backstory/s10_morales.wav")),

        // 11
        new Slide("/Images/backstory/slide11.png",
            new SubtitleBox.Line("Officer Morales", "Aside from the victim, three other tenants live in the building. We have all three detained downstairs in the common area until you arrive. Nobody enters, nobody leaves.",
                    "/audio/backstory/s11_morales.wav")),

        // 12 
        new Slide("/Images/backstory/slide12.png",
            null,
            L(new SubtitleBox.Line("Detective", "Any initial statements or weapons recovered?",
                    "/audio/backstory/s12_detective.wav")),
            L(new SubtitleBox.Line("Officer Morales", "Nothing yet. We didn't want to contaminate the crime scene or compromise preliminary interviews before lead gets here. The rain’s letting up, but we need you to process the rooftop and review the house before things get cold. Get down here right away, Detective.",
                    "/audio/backstory/s12_morales.wav"))),

        // 13 
        new Slide("/Images/backstory/slide13.png",
            "/audio/backstory/sfx/door_creak.wav")
    };

    /**
     * Creates new form Backstory
     */
    public Backstory() {
        initComponents();

        try {
            java.awt.Image icon = javax.imageio.ImageIO.read(
                getClass().getResourceAsStream("/Images/logo.png"));
            setIconImage(icon);
        } catch (Exception e) {
            System.out.println("Logo not found: " + e.getMessage());
        }

        setTitle("The Fourth Tenant");
        setResizable(false);
        setLocationRelativeTo(null);
        
        final javax.swing.ImageIcon bloodNormal = new javax.swing.ImageIcon(
            getClass().getResource("/Images/backstory/door_blood.png"));
        final javax.swing.ImageIcon bloodHover = new javax.swing.ImageIcon(
            getClass().getResource("/Images/backstory/door_blood_hovered.png"));
        
        jLabel4.setIcon(bloodNormal);   // start with the normal icon

        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                jLabel4.setIcon(bloodHover);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                jLabel4.setIcon(bloodNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                // Stop all audio immediately
                if (rainClip      != null) { rainClip.stop();      rainClip.close();      rainClip = null; }
                if (thunderClip   != null) { thunderClip.stop();   thunderClip.close();   thunderClip = null; }
                if (intenseClip   != null) { intenseClip.stop();   intenseClip.close();   intenseClip = null; }

                TransitionOverlay.play(Backstory.this, () -> {
                    CrimeScene cs = new CrimeScene();
                    cs.setLocation(getLocation());
                    cs.setVisible(true);
                    Backstory.this.dispose();
                });
            }
        });

        jLabel4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // ---- Arrow icons: normal + darkened versions ----
        final javax.swing.ImageIcon prevNormal = new javax.swing.ImageIcon(
                getClass().getResource("/Images/previous_button.png"));
        final javax.swing.ImageIcon nextNormal = new javax.swing.ImageIcon(
                getClass().getResource("/Images/next_button.png"));
        final javax.swing.ImageIcon prevDark = new javax.swing.ImageIcon(
                darken(prevNormal.getImage()));
        final javax.swing.ImageIcon nextDark = new javax.swing.ImageIcon(
                darken(nextNormal.getImage()));

        jLabel2.setIcon(prevNormal);
        jLabel3.setIcon(nextNormal);

        jLabel2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                jLabel2.setIcon(prevDark);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                jLabel2.setIcon(prevNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                navigate(-1);
            }
        });

        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                jLabel3.setIcon(nextDark);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                jLabel3.setIcon(nextNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                navigate(+1);
            }
        });

        // Keyboard arrow navigation
        javax.swing.JRootPane root = getRootPane();
        javax.swing.InputMap im = root.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap am = root.getActionMap();

        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0), "prev");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), "next");

        am.put("prev", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { navigate(-1); }
        });
        am.put("next", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { navigate(+1); }
        });
        
        // --- Subtitle boxes (reusable) ---
        subtitleBoxLeft = new SubtitleBox();
        subtitleBoxLeft.setVisible(false);
        jPanel1.add(subtitleBoxLeft,
            new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 900, 100));

        subtitleBoxRight = new SubtitleBox();
        subtitleBoxRight.setVisible(false);
        jPanel1.add(subtitleBoxRight,
            new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 900, 100));
        
        // --- Fade-in overlay ---
        fadeOverlay = new FadeOverlay();
        jPanel1.add(fadeOverlay,
            new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1150, 680));
        jPanel1.setComponentZOrder(fadeOverlay, 0);

        // Start fade shortly after the window is shown
        javax.swing.Timer starter = new javax.swing.Timer(150, e -> fadeIn());
        starter.setRepeats(false);
        starter.start();
        
        showSlide(0);
        
        // --- Ambience ---
        rainClip = playLooping("/audio/backstory/sfx/rain_loop.wav");

        // --- Preload typewriter key click ---
        try {
            java.io.InputStream is = getClass().getResourceAsStream(
                "/audio/backstory/sfx/typewriter_key.wav");
            typewriterKeyClip = javax.sound.sampled.AudioSystem.getClip();
            typewriterKeyClip.open(javax.sound.sampled.AudioSystem.getAudioInputStream(is));
        } catch (Exception e) {
            System.out.println("Key click not loaded: " + e.getMessage());
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

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new FadeLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("The Fourth Tenant");
        setResizable(false);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/previous_button.png"))); // NOI18N
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 680));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/next_button.png"))); // NOI18N
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1090, 0, 60, 680));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/door_blood.png"))); // NOI18N
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 250, -1, -1));

        jLabel1.setBackground(new java.awt.Color(20, 20, 25));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/slide1.png"))); // NOI18N
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    
    private void showSlide(int index) {
        if (index < 0) index = 0;
        if (index >= slides.length) index = slides.length - 1;
        currentSlide = index;

        Slide s = slides[currentSlide];

        // Stop any audio from the previous slide
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
        if (phoneRingClip != null) {
            phoneRingClip.stop();
            phoneRingClip.close();
            phoneRingClip = null;
        }
        if (thunderClip != null) {
            thunderClip.stop();
            thunderClip.close();
            thunderClip = null;
        }
        if (intenseClip != null) {
            intenseClip.stop();
            intenseClip.close();
            intenseClip = null;
        }

        // Fire this slide's one-shot SFX
        if (s.sfxPath != null) {
            playSfx(s.sfxPath);
        }

        // Slide 1: loop thunder + delayed phone ring
        if (currentSlide == 0) {
            thunderClip = playLooping("/audio/backstory/sfx/thunder.wav");
            javax.swing.Timer ringDelay = new javax.swing.Timer(600, e -> {
                phoneRingClip = playSfx("/audio/backstory/sfx/phone_ring.wav");
            });
            ringDelay.setRepeats(false);
            ringDelay.start();
        }

        // Slide 13: thunder + intense music
        if (currentSlide == 12) {
            thunderClip = playSfx("/audio/backstory/sfx/thunder.wav");
            intenseClip = playLooping("/audio/backstory/sfx/intense.wav");

            jLabel4.setVisible(false);
            javax.swing.Timer delay = new javax.swing.Timer(3000, e -> fadeInCutscene());
            delay.setRepeats(false);
            delay.start();
        } else {
            jLabel4.setVisible(false);
        }

        if (typewriterTimer != null) {
            typewriterTimer.stop();
        }

        // --- Image ---
        java.net.URL imgUrl = getClass().getResource(s.imagePath);
        if (imgUrl != null) {
            jLabel1.setIcon(new javax.swing.ImageIcon(imgUrl));
        } else {
            System.out.println("Slide missing: " + s.imagePath);
        }

        // --- Left subtitle (bottom-left) ---
        if (s.leftLines.isEmpty()) {
            subtitleBoxLeft.clear();
            subtitleBoxLeft.setVisible(false);
        } else {
            boolean plainCaption = (currentSlide == 0 || currentSlide == 1);
            subtitleBoxLeft.setPlainMode(plainCaption);
            subtitleBoxLeft.setCenterText(plainCaption);

            subtitleBoxLeft.setLines(s.leftLines);
            subtitleBoxLeft.setVisible(true);

            int boxW = 900;
            int boxX = (1150 - boxW) / 2;   // centered horizontally
            int boxH = subtitleBoxLeft.getPreferredHeight(boxW, s.leftLines);
            int boxY = 680 - boxH - 40;     // near bottom

            // If this slide has a right box too, shift left box to bottom-left
            if (!s.rightLines.isEmpty()) {
                boxW = 620;
                boxX = 40;
                boxH = subtitleBoxLeft.getPreferredHeight(boxW, s.leftLines);
                boxY = 680 - boxH - 40;
            }

            jPanel1.remove(subtitleBoxLeft);
            jPanel1.add(subtitleBoxLeft,
                new org.netbeans.lib.awtextra.AbsoluteConstraints(boxX, boxY, boxW, boxH));
        }

        // --- Right subtitle (top-right, only used on 2-box slides) ---
        if (s.rightLines.isEmpty()) {
            subtitleBoxRight.clear();
            subtitleBoxRight.setVisible(false);
        } else {
            subtitleBoxRight.setPlainMode(false);
            subtitleBoxRight.setCenterText(false);

            subtitleBoxRight.setLines(s.rightLines);
            subtitleBoxRight.setVisible(true);

            int boxW = 500;
            int boxX = 1150 - boxW - 40;    // top-right
            int boxH = subtitleBoxRight.getPreferredHeight(boxW, s.rightLines);
            int boxY = 40;

            jPanel1.remove(subtitleBoxRight);
            jPanel1.add(subtitleBoxRight,
                new org.netbeans.lib.awtextra.AbsoluteConstraints(boxX, boxY, boxW, boxH));
        }

        // Bring subtitles to front
        jPanel1.setComponentZOrder(subtitleBoxLeft, 0);
        jPanel1.setComponentZOrder(subtitleBoxRight, 0);

        // --- Arrows: hide at boundaries ---
        jLabel2.setVisible(currentSlide > 0);
        jLabel3.setVisible(currentSlide < slides.length - 1);

        boolean hasLeft  = !s.leftLines.isEmpty();
        boolean hasRight = !s.rightLines.isEmpty();

        if (hasLeft && hasRight) {
            subtitleBoxRight.setVisible(false);
            runTypewriterFor(subtitleBoxLeft, () -> {
                subtitleBoxRight.setVisible(true);
                runTypewriterFor(subtitleBoxRight, null);
            });
        } else if (hasLeft) {
            runTypewriterFor(subtitleBoxLeft, null);
        } else if (hasRight) {
            runTypewriterFor(subtitleBoxRight, null);
        }

        jPanel1.revalidate();
        jPanel1.repaint();
    }
    
    private void navigate(int direction) {   // ← add this method
        if (navigationLocked) return;
        int next = currentSlide + direction;
        if (next < 0 || next >= slides.length) return;

        navigationLocked = true;
        showSlide(next);

        javax.swing.Timer unlock = new javax.swing.Timer(NAV_LOCK_MS, ev -> navigationLocked = false);
        unlock.setRepeats(false);
        unlock.start();
    }
    
    private void fadeIn() {
        final long startTime = System.currentTimeMillis();
        final int DURATION_MS = 1200;

        javax.swing.Timer t = new javax.swing.Timer(16, null);
        t.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1f, elapsed / (float) DURATION_MS);

            fadeOverlay.setAlpha(1f - progress);   // 1 → 0

            if (progress >= 1f) {
                ((javax.swing.Timer) e.getSource()).stop();
                jPanel1.remove(fadeOverlay);
                fadeOverlay = null;
                jPanel1.revalidate();
                jPanel1.repaint();
            }
        });
        t.start();
    }
    
    private void fadeInCutscene() {
        FadeLabel bloodLabel = (FadeLabel) jLabel4;

        bloodLabel.setAlpha(0f);
        bloodLabel.setVisible(true);

        final long start = System.currentTimeMillis();
        final int DURATION_MS = 900;

        javax.swing.Timer t = new javax.swing.Timer(16, null);
        t.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - start;
            float p = Math.min(1f, elapsed / (float) DURATION_MS);
            bloodLabel.setAlpha(p);
            if (p >= 1f) ((javax.swing.Timer) e.getSource()).stop();
        });
        t.start();
    }
    
    /**
    * Types out one box's contents character by character, synced to an
    * audio file (if the box's first line has audio).
    */
        private void runTypewriterFor(SubtitleBox box, Runnable onDone) {
         if (typewriterTimer != null) typewriterTimer.stop();

         // --- 1) Audio path ---
         String audioPath = box.getFirstAudioPath();
         if (audioPath != null && !audioPath.startsWith("/")) {
             audioPath = "/" + audioPath;
         }

         long audioDurationMs = 0;
         javax.sound.sampled.Clip clip = null;

         if (audioPath != null) {
             try {
                 java.io.InputStream is = getClass().getResourceAsStream(audioPath);
                 if (is != null) {
                     javax.sound.sampled.AudioInputStream ais =
                         javax.sound.sampled.AudioSystem.getAudioInputStream(is);
                     clip = javax.sound.sampled.AudioSystem.getClip();
                     clip.open(ais);
                     audioDurationMs = clip.getMicrosecondLength() / 1000;
                     currentClip = clip;
                 }
             } catch (Exception e) {
                 System.out.println("Audio load FAILED: " + audioPath);
                 System.out.println("Reason: " + e.getClass().getSimpleName()
                                    + " — " + e.getMessage());
             }
         }

         int totalChars = box.getTotalChars();

         if (totalChars == 0) {
             box.showAllChars();
             if (clip != null) clip.start();
             if (onDone != null) onDone.run();
             return;
         }

         // --- 2) Speed ---
         final int DEFAULT_TICK_MS = 30;
         final double DEFAULT_CHARS_PER_SEC = 50.0;
         final double AUDIO_SYNC_MULTIPLIER = 1.2;

         final int tickMs;
         final double charsPerSecond;

         if (clip != null && audioDurationMs > 0) {
             tickMs = 25;
             charsPerSecond = totalChars / (audioDurationMs / 1000.0) * AUDIO_SYNC_MULTIPLIER;
         } else {
             tickMs = DEFAULT_TICK_MS;
             charsPerSecond = DEFAULT_CHARS_PER_SEC;
         }

         box.setVisibleChars(0);

         // --- 3) Play audio ---
         final javax.sound.sampled.Clip clipToStop = clip;
         if (clip != null) {
             clip.setFramePosition(0);
             clip.start();
         }

         // --- 4) Typewriter ---
         final double charsPerTick = charsPerSecond * (tickMs / 1000.0);
         final double[] accumulated = {0.0};

        typewriterTimer = new javax.swing.Timer(tickMs, null);
        typewriterTimer.addActionListener(e -> {
            // Key click for the first 1.5s
            if (System.currentTimeMillis() - typewriterStartTime < TYPEWRITER_KEY_DURATION_MS) {
                playKeyClick();
            }

            accumulated[0] += charsPerTick;
            int toShow = (int) accumulated[0];
            box.setVisibleChars(toShow);

            if (toShow >= totalChars) {
                typewriterTimer.stop();
                box.showAllChars();
                if (clipToStop != null) clipToStop.stop();
                if (onDone != null) onDone.run();
            }
        });
        typewriterStartTime = System.currentTimeMillis();
        typewriterTimer.start();
     }
    
    private static java.awt.Image darken(java.awt.Image src) {
        java.awt.image.ImageFilter filter = new java.awt.image.RGBImageFilter() {
            @Override
            public int filterRGB(int x, int y, int rgb) {
                int a = (rgb >> 24) & 0xff;
                int r = (int)(((rgb >> 16) & 0xff) * 0.35);
                int g = (int)(((rgb >> 8)  & 0xff) * 0.35);
                int b = (int)((rgb & 0xff) * 0.35);
                return (a << 24) | (r << 16) | (g << 8) | b;
            }
        };
        return java.awt.Toolkit.getDefaultToolkit().createImage(
                new java.awt.image.FilteredImageSource(src.getSource(), filter));
    }
    
    /** Simple black overlay that fades out. */
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
    
    /** Loads and starts a looping clip (used for rain ambience). */
    private javax.sound.sampled.Clip playLooping(String path) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream(path);
            javax.sound.sampled.AudioInputStream ais =
                javax.sound.sampled.AudioSystem.getAudioInputStream(is);
            javax.sound.sampled.Clip c = javax.sound.sampled.AudioSystem.getClip();
            c.open(ais);
            c.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY);
            return c;
        } catch (Exception e) {
            System.out.println("SFX loop not found: " + path);
            return null;
        }
    }

    /** Loads and plays a one-shot clip. Returns the clip so you can stop it if needed. */
    private javax.sound.sampled.Clip playSfx(String path) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream(path);
            javax.sound.sampled.AudioInputStream ais =
                javax.sound.sampled.AudioSystem.getAudioInputStream(is);
            javax.sound.sampled.Clip c = javax.sound.sampled.AudioSystem.getClip();
            c.open(ais);
            c.start();
            return c;
        } catch (Exception e) {
            System.out.println("SFX not found: " + path);
            return null;
        }
    }

    /** Plays the typewriter key click once. Reuses the same loaded clip. */
    private void playKeyClick() {
        if (typewriterKeyClip == null) return;
        typewriterKeyClip.setFramePosition(0);
        typewriterKeyClip.start();
    }
    
    @Override
    public void dispose() {
        if (rainClip      != null) { rainClip.stop();      rainClip.close(); }
        if (phoneRingClip != null) { phoneRingClip.stop(); phoneRingClip.close(); }
        if (thunderClip   != null) { thunderClip.stop();   thunderClip.close(); }
        if (intenseClip   != null) { intenseClip.stop();   intenseClip.close(); }
        if (typewriterKeyClip != null) { typewriterKeyClip.stop(); typewriterKeyClip.close(); }
        if (currentClip   != null) { currentClip.stop();   currentClip.close(); }
        if (typewriterTimer != null) { typewriterTimer.stop(); }
        super.dispose();
    }
    
    private static class FadeLabel extends javax.swing.JLabel {
        private float alpha = 1f;
        FadeLabel() { setOpaque(false); }
        void setAlpha(float a) { this.alpha = Math.max(0f, Math.min(1f, a)); repaint(); }
        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setComposite(java.awt.AlphaComposite.getInstance(
                java.awt.AlphaComposite.SRC_OVER, alpha));
            super.paintComponent(g2);
            g2.dispose();
        }
    }
    
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
        java.awt.EventQueue.invokeLater(() -> new Backstory().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
