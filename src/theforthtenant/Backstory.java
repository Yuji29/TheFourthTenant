/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package theforthtenant;

/**
 * The Backstory cutscene frame: displays a sequence of story slides with
 * subtitles, voice-over narration, sound effects, and a typewriter effect.
 * Handles navigation, pause/resume, options, and transitions to the main menu
 * or the crime scene.
 *
 * @author yuji
 */
public class Backstory extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(Backstory.class.getName());

    // ---- Slide state ----
    private int currentSlide = 0;

    // ---- UI components ----
    private SubtitleBox subtitleBoxLeft;
    private SubtitleBox subtitleBoxRight;
    private FadeOverlay fadeOverlay;
    private PauseMenu pauseMenu;
    private OptionsMenu optionsMenu;

    // ---- Audio ----
    private javax.sound.sampled.Clip currentClip;        // currently playing voice/narration
    private javax.sound.sampled.Clip rainClip;           // looping rain ambience
    private javax.sound.sampled.Clip phoneRingClip;      // phone ringing (slide 0)
    private javax.sound.sampled.Clip thunderClip;        // looping thunder (slide 0)
    private javax.sound.sampled.Clip intenseClip;        // looping intense music (slide 12)
    private final java.util.List<javax.sound.sampled.Clip> slideSfxClips =
            new java.util.ArrayList<>();

    // ---- Timers ----
    private javax.swing.Timer typewriterTimer;
    private javax.swing.Timer ringDelayTimer;
    private javax.swing.Timer cutsceneDelayTimer;
    private javax.swing.Timer bloodFadeTimer;

    // ---- Typewriter state ----
    private long typewriterStartTime;

    // ---- Typewriter resume state ----
    private double savedCharsShown = 0;
    private long   savedStartTime  = 0;
    private long   savedTickMs     = 25;
    private double savedCharsPerTick = 1;
    private int    savedTotalChars = 0;
    private SubtitleBox savedBox;
    private Runnable savedOnDone;
    private boolean typewriterWasRunning = false;

    // ---- Voice resume state ----
    private long savedClipMicros = 0;

    /**
     * Represents a single story slide: an image, optional voice-over line(s),
     * and an optional sound effect.
     */
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

    /** Convenience factory for a list of subtitle lines. */
    private static java.util.List<SubtitleBox.Line> L(SubtitleBox.Line... lines) {
        return java.util.Arrays.asList(lines);
    }

    /**
     * All story slides, in order.
     * Each entry may contain a single line (shown on the left) or two lines
     * (left/right speakers) for dialogue exchanges.
     */
    private final Slide[] slides = {
        // 1 — Phone rings
        new Slide("/Images/backstory/slide1.png",
            "/audio/backstory_sfx/thunder.wav",
            new SubtitleBox.Line(null, "[Phone ringing]")),

        // 2 — Receiver clicks
        new Slide("/Images/backstory/slide2.png",
            "/audio/backstory_sfx/receiver_click.wav",
            new SubtitleBox.Line(null, "[Receiver clicks]")),

        // 3 — Detective answers
        new Slide("/Images/backstory/slide3.png",
            new SubtitleBox.Line("Detective", "Detective speaking.",
            "/audio/voice/backstory/s03_detective.wav")),

        // 4 — Morales briefs the detective
        new Slide("/Images/backstory/slide4.png",
            new SubtitleBox.Line("Officer Morales", "Detective, glad I caught you at your desk. We have a 10-54 code over at Barangay San Lorenzo—specifically the old three-story boarding house near the corner lot. We need you on-site immediately.",
            "/audio/voice/backstory/s04_morales.wav")),

        // 5 — Detective asks for basics
        new Slide("/Images/backstory/slide5.png",
            new SubtitleBox.Line("Detective", "Give me the basics, Morales. What are we looking at?",
            "/audio/voice/backstory/s05_detective.wav")),

        // 6 — Morales describes the victim and scene
        new Slide("/Images/backstory/slide6.png",
            "/audio/backstory_sfx/siren_distant.wav",
            new SubtitleBox.Line("Officer Morales", "Homicide. The victim is a female tenant named Abby Salle. She was stuffed inside an industrial blue water drum on the open rooftop.",
                    "/audio/voice/backstory/s06_morales.wav")),

        // 7 — Detective asks about the ME; Morales gives discovery time
        new Slide("/Images/backstory/slide7.png",
            "/audio/backstory_sfx/wind_gust.wav",
            L(new SubtitleBox.Line("Detective", "Has the medical examiner given an initial read?",
                    "/audio/voice/backstory/s07_detective.wav")),
            L(new SubtitleBox.Line("Officer Morales", "The body was discovered just twenty minutes ago, around 6:30 AM, by one of the housemates heading up to do laundry.",
                    "/audio/voice/backstory/s07_morales.wav"))),

        // 8 — Morales gives coroner's preliminary findings
        new Slide("/Images/backstory/slide8.png",
                "/audio/backstory_sfx/siren_distant.wav",
            new SubtitleBox.Line("Officer Morales", "The coroner just did a preliminary check. Rigor mortis is fairly advanced—they're placing the estimated time of death between 1:30 AM and 2:30 AM earlier today. As for the primary cause of death, preliminary findings show severe head trauma and asphyxiation before she was folded into the drum.",
                    "/audio/voice/backstory/s08_morales.wav")),

        // 9 — Detective asks about scene security
        new Slide("/Images/backstory/slide9.png",
            new SubtitleBox.Line("Detective", "Anyone secured the area? Who's at the location?",
                    "/audio/voice/backstory/s09_detective.wav")),

        // 10 — Morales confirms perimeter is sealed
        new Slide("/Images/backstory/slide10.png",
                "/audio/backstory_sfx/siren_distant.wav",
            new SubtitleBox.Line("Officer Morales", "The rooftop and the entire boarding house are sealed under standard perimeter protocol.",
                    "/audio/voice/backstory/s10_morales.wav")),

        // 11 — Morales lists tenants and containment
        new Slide("/Images/backstory/slide11.png",
            new SubtitleBox.Line("Officer Morales", "Aside from the victim, three other tenants live in the building. We have all three detained downstairs in the common area until you arrive. Nobody enters, nobody leaves.",
                    "/audio/voice/backstory/s11_morales.wav")),

        // 12 — Detective asks about statements/weapons; Morales urges haste
        new Slide("/Images/backstory/slide12.png",
            null,
            L(new SubtitleBox.Line("Detective", "Any initial statements or weapons recovered?",
                    "/audio/voice/backstory/s12_detective.wav")),
            L(new SubtitleBox.Line("Officer Morales", "Nothing yet. We didn't want to contaminate the crime scene or compromise preliminary interviews before lead gets here. The rain’s letting up, but we need you to process the rooftop and review the house before things get cold. Get down here right away, Detective.",
                    "/audio/voice/backstory/s12_morales.wav"))),

        // 13 — Door creaks; player clicks to proceed
        new Slide("/Images/backstory/slide13.png",
            "/audio/backstory_sfx/door_creak.wav")
    };

    /**
     * Creates new form Backstory.
     */
    public Backstory() {
        initComponents();

        // ---- Window icon ----
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

        // ---- Exit label (blood-stained door): hover + click ----
        final javax.swing.ImageIcon bloodNormal = new javax.swing.ImageIcon(
            getClass().getResource("/Images/backstory/door_blood.png"));
        final javax.swing.ImageIcon bloodHover = new javax.swing.ImageIcon(
            getClass().getResource("/Images/backstory/door_blood_hovered.png"));

        jLabel4.setIcon(bloodNormal); // start with the normal icon

        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                jLabel4.setIcon(bloodHover);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                jLabel4.setIcon(bloodNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (TransitionOverlay.isPlaying()) return;

                stopAllSlideSfx();

                // Stop voice, phone ring, and any pending timers before transition.
                if (currentClip != null) { currentClip.stop(); currentClip = null; }
                if (phoneRingClip != null) { phoneRingClip.stop(); phoneRingClip = null; }

                if (ringDelayTimer != null) { ringDelayTimer.stop(); ringDelayTimer = null; }
                if (bloodFadeTimer != null) { bloodFadeTimer.stop(); bloodFadeTimer = null; }
                if (cutsceneDelayTimer != null) { cutsceneDelayTimer.stop(); cutsceneDelayTimer = null; }

                // Stop looping ambience.
                if (rainClip    != null) { rainClip.stop();    rainClip = null; }
                if (thunderClip != null) { thunderClip.stop(); thunderClip = null; }
                if (intenseClip != null) { intenseClip.stop(); intenseClip = null; }

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

        // ---- Previous arrow ----
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

        // ---- Next arrow ----
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

        // ---- Keyboard arrow navigation ----
        javax.swing.JRootPane root = getRootPane();
        javax.swing.InputMap im = root.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap am = root.getActionMap();

        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0), "prev");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), "next");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, 0), "prev");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, 0), "next");

        am.put("prev", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { navigate(-1); }
        });
        am.put("next", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { navigate(+1); }
        });

        // ---- Subtitle boxes (reusable, one per side) ----
        subtitleBoxLeft = new SubtitleBox();
        subtitleBoxLeft.setVisible(false);
        jPanel1.add(subtitleBoxLeft,
            new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 900, 100));

        subtitleBoxRight = new SubtitleBox();
        subtitleBoxRight.setVisible(false);
        jPanel1.add(subtitleBoxRight,
            new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 900, 100));

        // ---- Fade-in overlay (covers the whole panel) ----
        fadeOverlay = new FadeOverlay();
        jPanel1.add(fadeOverlay,
            new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1150, 680));
        jPanel1.setComponentZOrder(fadeOverlay, 0);

        // Start fade shortly after the window is shown.
        javax.swing.Timer starter = new javax.swing.Timer(150, e -> fadeIn());
        starter.setRepeats(false);
        starter.start();

        // Warm the audio cache with every Backstory asset on a background thread.
        // The window appears immediately; clips are ready by the time the player
        // reaches the slides that use them.
        new javax.swing.SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                AudioCache.prepare(
                    "/audio/voice/backstory/s03_detective.wav",
                    "/audio/voice/backstory/s04_morales.wav",
                    "/audio/voice/backstory/s05_detective.wav",
                    "/audio/voice/backstory/s06_morales.wav",
                    "/audio/voice/backstory/s07_detective.wav",
                    "/audio/voice/backstory/s07_morales.wav",
                    "/audio/voice/backstory/s08_morales.wav",
                    "/audio/voice/backstory/s09_detective.wav",
                    "/audio/voice/backstory/s10_morales.wav",
                    "/audio/voice/backstory/s11_morales.wav",
                    "/audio/voice/backstory/s12_detective.wav",
                    "/audio/voice/backstory/s12_morales.wav",
                    "/audio/backstory_sfx/thunder.wav",
                    "/audio/backstory_sfx/receiver_click.wav",
                    "/audio/backstory_sfx/siren_distant.wav",
                    "/audio/backstory_sfx/wind_gust.wav",
                    "/audio/backstory_sfx/door_creak.wav",
                    "/audio/sfx/ambience/rain_loop.wav",
                    "/audio/backstory_sfx/intense.wav",
                    "/audio/backstory_sfx/phone_ring.wav"
                );
                return null;
            }
        }.execute();

        showSlide(0);

        // ---- Ambience ----
        rainClip = AudioCache.loop("/audio/sfx/ambience/rain_loop.wav");

        // ---- Options menu ----
        optionsMenu = OptionsMenu.attachTo(this, new OptionsMenu.Callbacks() {

            @Override public void onMusicChanged(int percent) {
                // Backstory has no dedicated music clip — ambience (rain/thunder/intense)
                // is treated as SFX and controlled by the SOUND slider.
            }

            @Override public void onSoundChanged(int percent) {
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }

            @Override public void onClose() {
                pauseMenu.showPopupOnly();
            }
        });

        // ---- Pause menu ----
        pauseMenu = PauseMenu.attachTo(this, PauseMenu.Corner.TOP_LEFT, new PauseMenu.Callbacks() {

            @Override public void onPause() {
                // 1) Voice / narration clip — remember playback position.
                if (currentClip != null) {
                    savedClipMicros = currentClip.getMicrosecondPosition();
                    currentClip.stop();
                }

                // 2) Looping ambience.
                if (rainClip      != null) rainClip.stop();
                if (thunderClip   != null) thunderClip.stop();
                if (intenseClip   != null) intenseClip.stop();

                // 3) One-shot SFX that may still be playing.
                if (phoneRingClip != null) phoneRingClip.stop();

                // NOTE: any other one-shot SFX (siren_distant, wind_gust, receiver_click,
                // door_creak) are fire-and-forget Clips — they'll naturally finish on their
                // own. If you want to pause those too, see the "hardening" section below.

                // 4) Typewriter timer.
                if (typewriterTimer != null && typewriterTimer.isRunning()) {
                    typewriterWasRunning = true;
                    typewriterTimer.stop();
                }

                // 5) Kill any pending delayed timers (ring delay, cutscene fade).
                // These are one-shot timers scheduled inside showSlide(); if we don't
                // stop them they'll fire while paused.
                // (See the hardening section below for how to track them.)
            }

            @Override public void onResume() {
                // 1) Voice / narration — resume from where it was paused.
                if (currentClip != null && savedClipMicros > 0) {
                    currentClip.setMicrosecondPosition(savedClipMicros);
                    currentClip.start();
                    savedClipMicros = 0;
                }

                // 2) Looping ambience — start() resumes from where stop() left it.
                if (rainClip != null) rainClip.start();

                // Slide 0 loops thunder; slide 12 loops intense music.
                if (thunderClip != null && currentSlide == 0) {
                    thunderClip.start();
                }
                if (intenseClip != null && currentSlide == 12) {
                    intenseClip.start();
                }

                // 3) Typewriter — resume if it was mid-typing.
                if (typewriterWasRunning && savedBox != null) {
                    resumeTypewriter();
                }

                // 4) Re-fire one-shot SFX if still relevant.
                // (For now, skip — the clip already finished or will finish naturally.
                // If you want them to resume too, use the hardening pattern below.)
            }

            @Override public void onMainMenu() {
                pauseMenu.hideOverlay();

                // Stop all audio and timers before leaving the frame.
                stopAllSlideSfx();
                if (currentClip       != null) { currentClip.stop();       currentClip = null; }
                if (rainClip          != null) { rainClip.stop();          rainClip = null; }
                if (thunderClip       != null) { thunderClip.stop();       thunderClip = null; }
                if (intenseClip       != null) { intenseClip.stop();       intenseClip = null; }
                if (phoneRingClip     != null) { phoneRingClip.stop();     phoneRingClip = null; }
                if (typewriterTimer   != null) { typewriterTimer.stop();   typewriterTimer = null; }
                if (ringDelayTimer    != null) { ringDelayTimer.stop();    ringDelayTimer = null; }
                if (cutsceneDelayTimer!= null) { cutsceneDelayTimer.stop();cutsceneDelayTimer = null; }

                TransitionOverlay.play(Backstory.this, () -> {
                    MainMenu menu = new MainMenu();
                    menu.setLocation(getLocation());
                    menu.setVisible(true);
                    Backstory.this.dispose();
                });
            }

            @Override public void onOptions() {
                pauseMenu.hidePopupOnly();
                optionsMenu.show();
                System.out.println("Options clicked");
            }
        });
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
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new FadeLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("The Fourth Tenant");
        setResizable(false);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/next_button.png"))); // NOI18N
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1090, 310, 50, 60));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/door_blood.png"))); // NOI18N
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 250, -1, -1));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/previous_button.png"))); // NOI18N
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 310, -1, 50));

        jLabel1.setBackground(new java.awt.Color(20, 20, 25));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/slide1.png"))); // NOI18N
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/previous_button.png"))); // NOI18N
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 680));

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
    
    /**
     * Displays the slide at the given index, clamping to valid bounds.
     * Stops any audio from the previous slide, sets up the image, subtitle
     * boxes, SFX, and typewriter effect for this slide.
     */
    private void showSlide(int index) {
        if (index < 0) index = 0;
        if (index >= slides.length) index = slides.length - 1;
        currentSlide = index;

        Slide s = slides[currentSlide];
        stopAllSlideSfx();

        // Stop any audio from the previous slide.
        if (currentClip   != null) { currentClip.stop();   currentClip = null; }
        if (phoneRingClip != null) { phoneRingClip.stop(); phoneRingClip = null; }
        if (thunderClip   != null) { thunderClip.stop();   thunderClip = null; }
        if (intenseClip   != null) { intenseClip.stop();   intenseClip = null; }

        // Fire this slide's one-shot SFX — but skip if this slide handles its own SFX.
        if (s.sfxPath != null && currentSlide != 0) {
            playSfx(s.sfxPath);
        }

        // Slide 1: loop thunder + delayed phone ring.
        if (currentSlide == 0) {
            thunderClip = AudioCache.loop("/audio/backstory_sfx/thunder.wav");

            if (ringDelayTimer != null) ringDelayTimer.stop();
            ringDelayTimer = new javax.swing.Timer(600, e -> {
                if (pauseMenu != null && pauseMenu.isPaused()) return; // don't ring while paused
                phoneRingClip = playSfx("/audio/backstory_sfx/phone_ring.wav");
            });
            ringDelayTimer.setRepeats(false);
            ringDelayTimer.start();
        }

        // Slide 13: loop intense music, then fade in the exit label.
        if (currentSlide == 12) {
            intenseClip = AudioCache.loop("/audio/backstory_sfx/intense.wav");

            jLabel4.setVisible(false);
            ((FadeLabel) jLabel4).setAlpha(0f);

            if (cutsceneDelayTimer != null) cutsceneDelayTimer.stop();
            cutsceneDelayTimer = new javax.swing.Timer(3000, e -> fadeInCutscene());
            cutsceneDelayTimer.setRepeats(false);
            cutsceneDelayTimer.start();
        } else {
            jLabel4.setVisible(false);

            if (cutsceneDelayTimer != null) {
                cutsceneDelayTimer.stop();
                cutsceneDelayTimer = null;
            }
        }

        if (typewriterTimer != null) {
            typewriterTimer.stop();
        }

        // ---- Image ----
        java.net.URL imgUrl = getClass().getResource(s.imagePath);
        if (imgUrl != null) {
            jLabel1.setIcon(new javax.swing.ImageIcon(imgUrl));
        } else {
            System.out.println("Slide missing: " + s.imagePath);
        }

        // ---- Left subtitle (bottom-left) ----
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

            // If this slide has a right box too, shift the left box to bottom-left.
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

        // ---- Right subtitle (top-right, only used on two-box slides) ----
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

        // Bring subtitles to front.
        jPanel1.setComponentZOrder(subtitleBoxLeft, 0);
        jPanel1.setComponentZOrder(subtitleBoxRight, 0);

        // ---- Arrows: hide at boundaries ----
        jLabel2.setVisible(currentSlide > 0);
        jLabel3.setVisible(currentSlide < slides.length - 1);

        boolean hasLeft  = !s.leftLines.isEmpty();
        boolean hasRight = !s.rightLines.isEmpty();

        // Two-box slides: type the left box first, then reveal and type the right box.
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

    /**
     * Moves to the previous ({@code -1}) or next ({@code +1}) slide.
     * Blocked while the pause menu is open or a transition is in flight.
     */
    private void navigate(int direction) {
        // Block navigation while the pause menu is open.
        if (pauseMenu != null && pauseMenu.isPaused()) return;

        // Block navigation while a scene transition is in flight.
        if (TransitionOverlay.isPlaying()) return;

        int next = currentSlide + direction;
        if (next < 0 || next >= slides.length) return;

        AudioCache.play("/audio/sfx/gameplay/thud.wav");
        showSlide(next);
    }

    /** Fades the black overlay out from full opacity to transparent. */
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

    /** Fades the blood-stained exit label in on the final slide. */
    private void fadeInCutscene() {
        FadeLabel bloodLabel = (FadeLabel) jLabel4;

        // Cancel any previous fade still running.
        if (bloodFadeTimer != null) bloodFadeTimer.stop();

        bloodLabel.setAlpha(0f);
        bloodLabel.setVisible(true);

        final long start = System.currentTimeMillis();
        final int DURATION_MS = 900;

        bloodFadeTimer = new javax.swing.Timer(16, null);
        bloodFadeTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - start;
            float p = Math.min(1f, elapsed / (float) DURATION_MS);
            bloodLabel.setAlpha(p);
            if (p >= 1f) {
                ((javax.swing.Timer) e.getSource()).stop();
                bloodFadeTimer = null;
            }
        });
        bloodFadeTimer.start();
    }

    /**
     * Resumes the typewriter timer after a pause, picking up from the last
     * visible character count without replaying key clicks.
     */
    private void resumeTypewriter() {
        if (savedBox == null) return;

        final double charsPerTick = savedCharsPerTick;
        final int    totalChars   = savedTotalChars;
        final SubtitleBox box     = savedBox;
        final Runnable onDone     = savedOnDone;
        final double[] accumulated = { savedCharsShown };

        // Don't replay key clicks on resume — reset the "typing started" marker.
        typewriterStartTime = System.currentTimeMillis();

        typewriterTimer = new javax.swing.Timer((int) savedTickMs, null);
        typewriterTimer.addActionListener(e -> {
            accumulated[0] += charsPerTick;
            int toShow = (int) accumulated[0];
            savedCharsShown = toShow;
            box.setVisibleChars(toShow);

            if (toShow >= totalChars) {
                typewriterTimer.stop();
                box.showAllChars();
                typewriterWasRunning = false;
                if (onDone != null) onDone.run();
            }
        });
        typewriterTimer.start();
    }

    /**
     * Types out one box's contents character by character, synced to the
     * box's voice-over audio (if its first line has one). Runs {@code onDone}
     * when typing finishes.
     */
    private void runTypewriterFor(SubtitleBox box, Runnable onDone) {
        if (typewriterTimer != null) typewriterTimer.stop();

        // ---- 1) Audio path ----
        String audioPath = box.getFirstAudioPath();

        long audioDurationMs = 0;
        javax.sound.sampled.Clip clip = null;

        if (audioPath != null) {
            clip = AudioCache.get(audioPath);
            if (clip != null) {
                clip.stop();
                clip.setFramePosition(0);
                audioDurationMs = clip.getMicrosecondLength() / 1000;
                currentClip = clip;
            }
        }

        int totalChars = box.getTotalChars();

        // No text — just play the audio (if any) and finish immediately.
        if (totalChars == 0) {
            box.showAllChars();
            if (clip != null) clip.start();
            if (onDone != null) onDone.run();
            return;
        }

        // ---- 2) Speed ----
        // With audio: sync typing to narration length, sped up slightly so the
        // text finishes a touch before the voice does.
        // Without audio: fall back to a fixed reading pace.
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

        // ---- 3) Play audio ----
        final javax.sound.sampled.Clip clipToStop = clip;
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }

        // ---- 4) Typewriter ----
        final double charsPerTick = charsPerSecond * (tickMs / 1000.0);
        final double[] accumulated = {0.0};

        // Save state for pause/resume.
        savedBox          = box;
        savedOnDone       = onDone;
        savedTotalChars   = totalChars;
        savedTickMs       = tickMs;
        savedCharsPerTick = charsPerTick;
        savedCharsShown   = 0;
        savedStartTime    = System.currentTimeMillis();
        typewriterWasRunning = true;

        typewriterTimer = new javax.swing.Timer(tickMs, null);
        typewriterTimer.addActionListener(e -> {

            accumulated[0] += charsPerTick;
            int toShow = (int) accumulated[0];
            savedCharsShown = toShow;   // remember for pause
            box.setVisibleChars(toShow);

            if (toShow >= totalChars) {
                typewriterTimer.stop();
                box.showAllChars();
                if (clipToStop != null) clipToStop.stop();
                if (onDone != null) onDone.run();
                typewriterWasRunning = false;
            }
        });
        typewriterStartTime = System.currentTimeMillis();
        typewriterTimer.start();
    }

    /** Returns a darkened copy of the given image (used for arrow hover states). */
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

    /**
     * Stops all audio and timers owned by this frame.
     * Clips are stopped but not closed — the {@link AudioCache} owns them.
     */
    @Override
    public void dispose() {
        // Stop anything still playing — do NOT close, the cache owns the clips.
        stopAllSlideSfx();
        if (currentClip       != null) currentClip.stop();
        if (rainClip          != null) rainClip.stop();
        if (thunderClip       != null) thunderClip.stop();
        if (intenseClip       != null) intenseClip.stop();
        if (phoneRingClip     != null) phoneRingClip.stop();

        currentClip = null;
        rainClip = null;
        thunderClip = null;
        intenseClip = null;
        phoneRingClip = null;

        if (typewriterTimer    != null) typewriterTimer.stop();
        if (ringDelayTimer     != null) ringDelayTimer.stop();
        if (cutsceneDelayTimer != null) cutsceneDelayTimer.stop();
        if (bloodFadeTimer     != null) bloodFadeTimer.stop();

        super.dispose();
    }

    /** JLabel variant that supports a fading alpha value. */
    private static class FadeLabel extends javax.swing.JLabel {
        private float alpha = 1f;

        FadeLabel() { setOpaque(false); }

        void setAlpha(float a) {
            this.alpha = Math.max(0f, Math.min(1f, a));
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setComposite(java.awt.AlphaComposite.getInstance(
                java.awt.AlphaComposite.SRC_OVER, alpha));
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    /** Stops and clears every one-shot SFX clip tracked for the current slide. */
    private void stopAllSlideSfx() {
        for (javax.sound.sampled.Clip c : new java.util.ArrayList<>(slideSfxClips)) {
            try { c.stop(); } catch (Exception ignored) {}
        }
        slideSfxClips.clear();
    }

    /**
     * Plays a one-shot SFX via the cache and tracks it so it can be stopped
     * when the slide changes or the frame is disposed.
     *
     * @return the playing clip, or {@code null} if the sound could not be loaded
     */
    private javax.sound.sampled.Clip playSfx(String path) {
        javax.sound.sampled.Clip c = AudioCache.play(path);
        if (c != null) slideSfxClips.add(c);
        return c;
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
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
