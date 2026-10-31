/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package theforthtenant;

/**
 * The Backstory cut scene frame: displays a sequence of story slides with
 * subtitles, voice-over narration, sound effects, and a typewriter effect.
 * Handles navigation, pause/resume, options, and transitions to the main menu
 * or the crime scene.
 *
 */
public class Backstory extends GameScreen {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(Backstory.class.getName());

    // ---- Slide state ----
    private int currentSlide = 0;

    // ---- UI components ----
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
        setupScreen();
    }
    
    @Override
    protected void setupScreen() {
        
        // ---- Style all text areas transparently ----
        styleTextBox(dialogue1TextArea, jScrollPane1);
        styleTextBox(dialogue3TextArea, jScrollPane2);
        styleTextBox(dialogue4TextArea, jScrollPane3);
        styleTextBox(dialogue5TextArea, jScrollPane4);
        styleTextBox(dialogue6TextArea, jScrollPane5);
        styleTextBox(dialogue7detectiveTextArea, jScrollPane6);
        styleTextBox(dialogue7policeTextArea, jScrollPane7);
        styleTextBox(dialogue8TextArea, jScrollPane8);
        styleTextBox(dialogue9TextArea, jScrollPane9);
        styleTextBox(dialogue10TextArea, jScrollPane10);
        styleTextBox(dialogue11TextArea, jScrollPane11);
       styleTextBox(dialogue12detectiveTextArea, jScrollPane12);
        styleTextBox(dialogue12policeTextArea, jScrollPane13);

        // ---- Exit label (blood-stained door): hover + click ----
        final javax.swing.ImageIcon bloodNormal = new javax.swing.ImageIcon(
            getClass().getResource("/Images/backstory/door_blood.png"));
        final javax.swing.ImageIcon bloodHover = new javax.swing.ImageIcon(
            getClass().getResource("/Images/backstory/door_blood_hovered.png"));

        exitDoor.setIcon(bloodNormal); // start with the normal icon

        exitDoor.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                exitDoor.setIcon(bloodHover);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                exitDoor.setIcon(bloodNormal);
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

        exitDoor.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // ---- Arrow icons: normal + darkened versions ----
        final javax.swing.ImageIcon prevNormal = new javax.swing.ImageIcon(
                getClass().getResource("/Images/common/previous_button.png"));
        final javax.swing.ImageIcon nextNormal = new javax.swing.ImageIcon(
                getClass().getResource("/Images/common/next_button.png"));
        final javax.swing.ImageIcon prevDark = new javax.swing.ImageIcon(
                darken(prevNormal.getImage()));
        final javax.swing.ImageIcon nextDark = new javax.swing.ImageIcon(
                darken(nextNormal.getImage()));

        prevButton.setIcon(prevNormal);
        nextButton.setIcon(nextNormal);

        prevButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        nextButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // ---- Previous arrow ----
        prevButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                prevButton.setIcon(prevDark);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                prevButton.setIcon(prevNormal);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                navigate(-1);
            }
        });

        // ---- Next arrow ----
        nextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                nextButton.setIcon(nextDark);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                nextButton.setIcon(nextNormal);
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

        // ---- Fade-in overlay (covers the whole panel) ----
        fadeOverlay = new FadeOverlay();
        contentPanel.add(fadeOverlay,
            new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1150, 680));
        contentPanel.setComponentZOrder(fadeOverlay, 0);

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
        exitDoor = new FadeLabel();
        jScrollPane13 = new javax.swing.JScrollPane();
        dialogue12policeTextArea = new javax.swing.JTextArea();
        dialogue12police = new javax.swing.JLabel();
        jScrollPane12 = new javax.swing.JScrollPane();
        dialogue12detectiveTextArea = new javax.swing.JTextArea();
        dialogue12detective = new javax.swing.JLabel();
        jScrollPane11 = new javax.swing.JScrollPane();
        dialogue11TextArea = new javax.swing.JTextArea();
        dialogue11 = new javax.swing.JLabel();
        jScrollPane10 = new javax.swing.JScrollPane();
        dialogue10TextArea = new javax.swing.JTextArea();
        dialogue10 = new javax.swing.JLabel();
        jScrollPane9 = new javax.swing.JScrollPane();
        dialogue9TextArea = new javax.swing.JTextArea();
        dialogue9 = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        dialogue8TextArea = new javax.swing.JTextArea();
        dialogue8 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        dialogue7policeTextArea = new javax.swing.JTextArea();
        dialogue7police = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        dialogue7detectiveTextArea = new javax.swing.JTextArea();
        dialogue7detective = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        dialogue6TextArea = new javax.swing.JTextArea();
        dialogue6 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        dialogue5TextArea = new javax.swing.JTextArea();
        dialogue5 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        dialogue4TextArea = new javax.swing.JTextArea();
        dialogue4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        dialogue3TextArea = new javax.swing.JTextArea();
        dialogue3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        dialogue1TextArea = new javax.swing.JTextArea();
        nextButton = new javax.swing.JLabel();
        prevButton = new javax.swing.JLabel();
        slideImage = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("The Fourth Tenant");
        setResizable(false);

        contentPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        exitDoor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/door_blood.png"))); // NOI18N
        contentPanel.add(exitDoor, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 250, -1, -1));

        jScrollPane13.setBorder(null);
        jScrollPane13.setOpaque(false);

        dialogue12policeTextArea.setEditable(false);
        dialogue12policeTextArea.setColumns(20);
        dialogue12policeTextArea.setLineWrap(true);
        dialogue12policeTextArea.setRows(5);
        dialogue12policeTextArea.setWrapStyleWord(true);
        dialogue12policeTextArea.setOpaque(false);
        jScrollPane13.setViewportView(dialogue12policeTextArea);

        contentPanel.add(jScrollPane13, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 70, 610, 150));

        dialogue12police.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/dialogue/dialogue10.2.png"))); // NOI18N
        contentPanel.add(dialogue12police, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 20, -1, -1));

        jScrollPane12.setBorder(null);
        jScrollPane12.setOpaque(false);

        dialogue12detectiveTextArea.setEditable(false);
        dialogue12detectiveTextArea.setColumns(20);
        dialogue12detectiveTextArea.setLineWrap(true);
        dialogue12detectiveTextArea.setRows(5);
        dialogue12detectiveTextArea.setWrapStyleWord(true);
        dialogue12detectiveTextArea.setOpaque(false);
        jScrollPane12.setViewportView(dialogue12detectiveTextArea);

        contentPanel.add(jScrollPane12, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 560, 400, 60));

        dialogue12detective.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/dialogue/dialogue10.1.png"))); // NOI18N
        contentPanel.add(dialogue12detective, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 510, -1, -1));

        jScrollPane11.setBorder(null);
        jScrollPane11.setOpaque(false);

        dialogue11TextArea.setEditable(false);
        dialogue11TextArea.setColumns(20);
        dialogue11TextArea.setLineWrap(true);
        dialogue11TextArea.setRows(5);
        dialogue11TextArea.setWrapStyleWord(true);
        dialogue11TextArea.setOpaque(false);
        jScrollPane11.setViewportView(dialogue11TextArea);

        contentPanel.add(jScrollPane11, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 540, 910, 80));

        dialogue11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/dialogue/dialogue9.png"))); // NOI18N
        contentPanel.add(dialogue11, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 490, -1, -1));

        jScrollPane10.setBorder(null);
        jScrollPane10.setOpaque(false);

        dialogue10TextArea.setEditable(false);
        dialogue10TextArea.setColumns(20);
        dialogue10TextArea.setLineWrap(true);
        dialogue10TextArea.setRows(5);
        dialogue10TextArea.setWrapStyleWord(true);
        dialogue10TextArea.setOpaque(false);
        jScrollPane10.setViewportView(dialogue10TextArea);

        contentPanel.add(jScrollPane10, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 560, 910, 60));

        dialogue10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/dialogue/dialogue8.png"))); // NOI18N
        contentPanel.add(dialogue10, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 510, -1, -1));

        jScrollPane9.setBorder(null);
        jScrollPane9.setOpaque(false);

        dialogue9TextArea.setEditable(false);
        dialogue9TextArea.setColumns(20);
        dialogue9TextArea.setLineWrap(true);
        dialogue9TextArea.setRows(5);
        dialogue9TextArea.setWrapStyleWord(true);
        dialogue9TextArea.setOpaque(false);
        jScrollPane9.setViewportView(dialogue9TextArea);

        contentPanel.add(jScrollPane9, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 586, 910, 40));

        dialogue9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/dialogue/dialogue7.png"))); // NOI18N
        contentPanel.add(dialogue9, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 540, -1, -1));

        jScrollPane8.setBorder(null);
        jScrollPane8.setOpaque(false);

        dialogue8TextArea.setEditable(false);
        dialogue8TextArea.setColumns(20);
        dialogue8TextArea.setLineWrap(true);
        dialogue8TextArea.setRows(5);
        dialogue8TextArea.setWrapStyleWord(true);
        dialogue8TextArea.setOpaque(false);
        jScrollPane8.setViewportView(dialogue8TextArea);

        contentPanel.add(jScrollPane8, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 520, 910, -1));

        dialogue8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/dialogue/dialogue6.png"))); // NOI18N
        contentPanel.add(dialogue8, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 470, -1, -1));

        jScrollPane7.setBorder(null);
        jScrollPane7.setOpaque(false);

        dialogue7policeTextArea.setEditable(false);
        dialogue7policeTextArea.setColumns(20);
        dialogue7policeTextArea.setLineWrap(true);
        dialogue7policeTextArea.setRows(5);
        dialogue7policeTextArea.setWrapStyleWord(true);
        dialogue7policeTextArea.setOpaque(false);
        jScrollPane7.setViewportView(dialogue7policeTextArea);

        contentPanel.add(jScrollPane7, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 70, 610, 50));

        dialogue7police.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/dialogue/dialogue5.2.png"))); // NOI18N
        contentPanel.add(dialogue7police, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 20, -1, -1));

        jScrollPane6.setBorder(null);
        jScrollPane6.setOpaque(false);

        dialogue7detectiveTextArea.setEditable(false);
        dialogue7detectiveTextArea.setColumns(20);
        dialogue7detectiveTextArea.setLineWrap(true);
        dialogue7detectiveTextArea.setRows(5);
        dialogue7detectiveTextArea.setWrapStyleWord(true);
        dialogue7detectiveTextArea.setOpaque(false);
        jScrollPane6.setViewportView(dialogue7detectiveTextArea);

        contentPanel.add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 560, 400, 50));

        dialogue7detective.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/dialogue/dialogue5.1.png"))); // NOI18N
        contentPanel.add(dialogue7detective, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 510, -1, -1));

        jScrollPane5.setBorder(null);
        jScrollPane5.setOpaque(false);

        dialogue6TextArea.setEditable(false);
        dialogue6TextArea.setColumns(20);
        dialogue6TextArea.setLineWrap(true);
        dialogue6TextArea.setRows(5);
        dialogue6TextArea.setWrapStyleWord(true);
        dialogue6TextArea.setOpaque(false);
        jScrollPane5.setViewportView(dialogue6TextArea);

        contentPanel.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 535, 910, -1));

        dialogue6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/dialogue/dialogue4.png"))); // NOI18N
        contentPanel.add(dialogue6, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 490, -1, -1));

        jScrollPane4.setBorder(null);
        jScrollPane4.setOpaque(false);

        dialogue5TextArea.setEditable(false);
        dialogue5TextArea.setColumns(20);
        dialogue5TextArea.setLineWrap(true);
        dialogue5TextArea.setRows(5);
        dialogue5TextArea.setWrapStyleWord(true);
        dialogue5TextArea.setOpaque(false);
        jScrollPane4.setViewportView(dialogue5TextArea);

        contentPanel.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 570, 910, 50));

        dialogue5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/dialogue/dialogue3.png"))); // NOI18N
        contentPanel.add(dialogue5, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 530, -1, -1));

        jScrollPane3.setBorder(null);
        jScrollPane3.setOpaque(false);

        dialogue4TextArea.setEditable(false);
        dialogue4TextArea.setColumns(20);
        dialogue4TextArea.setLineWrap(true);
        dialogue4TextArea.setRows(5);
        dialogue4TextArea.setWrapStyleWord(true);
        dialogue4TextArea.setOpaque(false);
        jScrollPane3.setViewportView(dialogue4TextArea);

        contentPanel.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 536, 910, 80));

        dialogue4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/dialogue/dialogue4.png"))); // NOI18N
        contentPanel.add(dialogue4, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 490, -1, -1));

        jScrollPane2.setBorder(null);
        jScrollPane2.setOpaque(false);

        dialogue3TextArea.setEditable(false);
        dialogue3TextArea.setColumns(20);
        dialogue3TextArea.setLineWrap(true);
        dialogue3TextArea.setRows(5);
        dialogue3TextArea.setWrapStyleWord(true);
        dialogue3TextArea.setOpaque(false);
        jScrollPane2.setViewportView(dialogue3TextArea);

        contentPanel.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 596, 910, 30));

        dialogue3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/dialogue/dialogue1.png"))); // NOI18N
        contentPanel.add(dialogue3, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 550, -1, -1));

        jScrollPane1.setBorder(null);
        jScrollPane1.setOpaque(false);

        dialogue1TextArea.setEditable(false);
        dialogue1TextArea.setColumns(20);
        dialogue1TextArea.setFont(new java.awt.Font("Press Start 2P", 0, 12)); // NOI18N
        dialogue1TextArea.setForeground(new java.awt.Color(255, 255, 255));
        dialogue1TextArea.setLineWrap(true);
        dialogue1TextArea.setRows(5);
        dialogue1TextArea.setWrapStyleWord(true);
        dialogue1TextArea.setOpaque(false);
        jScrollPane1.setViewportView(dialogue1TextArea);

        contentPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 590, 380, 40));

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/common/next_button.png"))); // NOI18N
        contentPanel.add(nextButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1090, 310, 50, 60));

        prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/common/previous_button.png"))); // NOI18N
        contentPanel.add(prevButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 310, -1, 50));

        slideImage.setBackground(new java.awt.Color(20, 20, 25));
        slideImage.setForeground(new java.awt.Color(255, 255, 255));
        slideImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/backstory/slide1.png"))); // NOI18N
        contentPanel.add(slideImage, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

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

            exitDoor.setVisible(false);
            ((FadeLabel) exitDoor).setAlpha(0f);

            if (cutsceneDelayTimer != null) cutsceneDelayTimer.stop();
            cutsceneDelayTimer = new javax.swing.Timer(3000, e -> fadeInCutscene());
            cutsceneDelayTimer.setRepeats(false);
            cutsceneDelayTimer.start();
        } else {
            exitDoor.setVisible(false);

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
            slideImage.setIcon(new javax.swing.ImageIcon(imgUrl));
        } else {
            System.out.println("Slide missing: " + s.imagePath);
        }
        
        // ---- Text Areas ----
        // 1. Hide both text areas and clear them
        dialogue1TextArea.setVisible(false);
        jScrollPane1.setVisible(false);
        dialogue1TextArea.setText(""); 

        dialogue3TextArea.setVisible(false);
        jScrollPane2.setVisible(false);
        dialogue3TextArea.setText("");       
        dialogue3.setVisible(false);
        
        dialogue4TextArea.setVisible(false);
        jScrollPane3.setVisible(false);
        dialogue4TextArea.setText("");
        dialogue4.setVisible(false);
        
        dialogue5TextArea.setVisible(false);
        jScrollPane4.setVisible(false);
        dialogue5TextArea.setText("");
        dialogue5.setVisible(false);
                
        dialogue6TextArea.setVisible(false);
        jScrollPane5.setVisible(false);
        dialogue6TextArea.setText("");
        dialogue6.setVisible(false);
        
        dialogue7detectiveTextArea.setVisible(false);
        jScrollPane6.setVisible(false);
        dialogue7detectiveTextArea.setText("");
        dialogue7detective.setVisible(false);

        dialogue7policeTextArea.setVisible(false);
        jScrollPane7.setVisible(false);
        dialogue7policeTextArea.setText("");
        dialogue7police.setVisible(false);        
        
        dialogue8TextArea.setVisible(false);
        jScrollPane8.setVisible(false);
        dialogue8TextArea.setText("");
        dialogue8.setVisible(false);  
        
        dialogue9TextArea.setVisible(false);
        jScrollPane9.setVisible(false);
        dialogue9TextArea.setText("");
        dialogue9.setVisible(false);  
        
        dialogue10TextArea.setVisible(false);
        jScrollPane10.setVisible(false);
        dialogue10TextArea.setText("");
        dialogue10.setVisible(false);  
        
        dialogue11TextArea.setVisible(false);
        jScrollPane11.setVisible(false);
        dialogue11TextArea.setText("");
        dialogue11.setVisible(false);  
        
        dialogue12detectiveTextArea.setVisible(false);
        jScrollPane12.setVisible(false);
        dialogue12detectiveTextArea.setText("");
        dialogue12detective.setVisible(false);

        dialogue12policeTextArea.setVisible(false);
        jScrollPane13.setVisible(false);
        dialogue12policeTextArea.setText("");
        dialogue12police.setVisible(false);   

        // 2. Show the correct text area based on the slide
        if (currentSlide == 0) {
            // Slide 1: [Phone ringing] (Centered with spaces)
            jScrollPane1.setVisible(true);
            dialogue1TextArea.setVisible(true);
            String text = s.leftLines.isEmpty() ? "" : s.leftLines.get(0).text;
            String audio = s.leftLines.isEmpty() ? null : s.leftLines.get(0).audioPath;
            
            runTypewriterForArea(dialogue1TextArea, text, audio, null);
            
        } else if (currentSlide == 1) {
            // Slide 2: [Receiver clicks]
            jScrollPane1.setVisible(true);
            dialogue1TextArea.setVisible(true);
            String text = s.leftLines.isEmpty() ? "" : s.leftLines.get(0).text;
            String audio = s.leftLines.isEmpty() ? null : s.leftLines.get(0).audioPath;
            
            runTypewriterForArea(dialogue1TextArea, text, audio, null);
            
        } else if (currentSlide == 2) {
            // Slide 3: Detective speaking.
            dialogue3.setVisible(true);
            jScrollPane2.setVisible(true);
            dialogue3TextArea.setVisible(true);
            String text = s.leftLines.isEmpty() ? "" : s.leftLines.get(0).text;
            String audio = s.leftLines.isEmpty() ? null : s.leftLines.get(0).audioPath;
            runTypewriterForArea(dialogue3TextArea, text, audio, null);
            
        } else if (currentSlide == 3) {
            // Slide 4: Officer Morales
            dialogue4.setVisible(true);
            jScrollPane3.setVisible(true);
            dialogue4TextArea.setVisible(true);
            String text = s.leftLines.isEmpty() ? "" : s.leftLines.get(0).text;
            String audio = s.leftLines.isEmpty() ? null : s.leftLines.get(0).audioPath;
            runTypewriterForArea(dialogue4TextArea, text, audio, null);
            
        } else if (currentSlide == 4) {
            // Slide 5: Detective asks for basics
            dialogue3.setVisible(true);
            jScrollPane2.setVisible(true);
            dialogue3TextArea.setVisible(true);
            String text = s.leftLines.isEmpty() ? "" : s.leftLines.get(0).text;
            String audio = s.leftLines.isEmpty() ? null : s.leftLines.get(0).audioPath;
            runTypewriterForArea(dialogue3TextArea, text, audio, null);
            
        } else if (currentSlide == 5) {
            // Slide 6: Morales describes the victim and scene
            dialogue4.setVisible(true);
            jScrollPane3.setVisible(true);
            dialogue4TextArea.setVisible(true);
            String text = s.leftLines.isEmpty() ? "" : s.leftLines.get(0).text;
            String audio = s.leftLines.isEmpty() ? null : s.leftLines.get(0).audioPath;
            runTypewriterForArea(dialogue4TextArea, text, audio, null);
            
        } else if (currentSlide == 6) {
            // Slide 7: Two lines! (Detective then Morales)
            dialogue7detective.setVisible(true);
            jScrollPane6.setVisible(true);
            dialogue7detectiveTextArea.setVisible(true);
            
            String text1 = s.leftLines.get(0).text;
            String audio1 = s.leftLines.get(0).audioPath;
            
            // 2. Type the Detective's line. When it's done, SHOW Morales's box (but keep the Detective's on screen!)
            runTypewriterForArea(dialogue7detectiveTextArea, text1, audio1, () -> {
                
                // Show Morales
                dialogue7police.setVisible(true);
                jScrollPane7.setVisible(true);
                dialogue7policeTextArea.setVisible(true);
                
                String text2 = s.rightLines.get(0).text;
                String audio2 = s.rightLines.get(0).audioPath;
                
                runTypewriterForArea(dialogue7policeTextArea, text2, audio2, null);
            });
            
        } else if (currentSlide == 7) {
            // Slide 8: Morales gives coroner's preliminary findings
            dialogue4.setVisible(true);
            jScrollPane3.setVisible(true);
            dialogue4TextArea.setVisible(true);
            String text = s.leftLines.isEmpty() ? "" : s.leftLines.get(0).text;
            String audio = s.leftLines.isEmpty() ? null : s.leftLines.get(0).audioPath;
            runTypewriterForArea(dialogue4TextArea, text, audio, null);

        } else if (currentSlide == 8) {
            // Slide 9: Detective asks about scene security
            dialogue3.setVisible(true);
            jScrollPane2.setVisible(true);
            dialogue3TextArea.setVisible(true);
            String text = s.leftLines.isEmpty() ? "" : s.leftLines.get(0).text;
            String audio = s.leftLines.isEmpty() ? null : s.leftLines.get(0).audioPath;
            runTypewriterForArea(dialogue3TextArea, text, audio, null);
            
        } else if (currentSlide == 9) {
            // Slide 10: Morales confirms perimeter is sealed
            dialogue4.setVisible(true);
            jScrollPane3.setVisible(true);
            dialogue4TextArea.setVisible(true);
            String text = s.leftLines.isEmpty() ? "" : s.leftLines.get(0).text;
            String audio = s.leftLines.isEmpty() ? null : s.leftLines.get(0).audioPath;
            runTypewriterForArea(dialogue4TextArea, text, audio, null);
                        
        } else if (currentSlide == 10) {
            // Slide 11: Morales lists tenants and containment
            dialogue4.setVisible(true);
            jScrollPane3.setVisible(true);
            dialogue4TextArea.setVisible(true);
            String text = s.leftLines.isEmpty() ? "" : s.leftLines.get(0).text;
            String audio = s.leftLines.isEmpty() ? null : s.leftLines.get(0).audioPath;
            runTypewriterForArea(dialogue4TextArea, text, audio, null);
            
        } else if (currentSlide == 11) {
            // Slide 12: Two lines (Detective then Morales)
            
            // 1. Show the Detective's NEW box (dialogue12detective)
            dialogue12detective.setVisible(true);
            jScrollPane12.setVisible(true);
            dialogue12detectiveTextArea.setVisible(true);
            
            String text1 = s.leftLines.get(0).text;
            String audio1 = s.leftLines.get(0).audioPath;
            
            // 2. Type Detective's line, then show Morales's NEW box (dialogue12police)
            runTypewriterForArea(dialogue12detectiveTextArea, text1, audio1, () -> {
                
                dialogue12police.setVisible(true);
                jScrollPane13.setVisible(true);
                dialogue12policeTextArea.setVisible(true);
                
                String text2 = s.rightLines.get(0).text;
                String audio2 = s.rightLines.get(0).audioPath;
                
                runTypewriterForArea(dialogue12policeTextArea, text2, audio2, null);
            });
        }
        
        // ---- Arrows: hide at boundaries ----
        prevButton.setVisible(currentSlide > 0);
        nextButton.setVisible(currentSlide < slides.length - 1);

        contentPanel.revalidate();
        contentPanel.repaint();      
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
                contentPanel.remove(fadeOverlay);
                fadeOverlay = null;
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        });
        t.start();
    }

    /** Fades the blood-stained exit label in on the final slide. */
    private void fadeInCutscene() {
        FadeLabel bloodLabel = (FadeLabel) exitDoor;

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
    
    /** Animates typing text into a standard JTextArea, synced to audio. */
    private void runTypewriterForArea(final javax.swing.JTextArea area, final String text, final String audioPath, final Runnable onDone) {
        if (typewriterTimer != null) typewriterTimer.stop();
        if (currentClip != null) { currentClip.stop(); currentClip = null; }

        area.setText("");
        long audioDurationMs = 0;
        javax.sound.sampled.Clip clip = null;

        if (audioPath != null) {
            clip = AudioCache.get(audioPath);
            if (clip != null) {
                clip.stop();
                clip.setFramePosition(0);
                audioDurationMs = clip.getMicrosecondLength() / 1000;
                currentClip = clip;
                clip.start();
            }
        }

        // If the text contains HTML, display it instantly (typewriter breaks with HTML tags)
        if (text.startsWith("<html>")) {
            area.setText(text);
            if (onDone != null) onDone.run();
            return;
        }

        final int tickMs = 25;
        final double charsPerSecond = (clip != null && audioDurationMs > 0) 
                ? (text.length() / (audioDurationMs / 1000.0)) * 1.2 
                : 50.0;
                
        final double charsPerTick = charsPerSecond * (tickMs / 1000.0);
        final double[] accumulated = { 0.0 };

        typewriterTimer = new javax.swing.Timer(tickMs, e -> {
            accumulated[0] += charsPerTick;
            int n = Math.min(text.length(), (int) accumulated[0]);
            area.setText(text.substring(0, n));
            
            if (n >= text.length()) {
                typewriterTimer.stop();
                if (onDone != null) onDone.run();
            }
        });
        typewriterTimer.start();
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
    private javax.swing.JPanel contentPanel;
    private javax.swing.JLabel dialogue10;
    private javax.swing.JTextArea dialogue10TextArea;
    private javax.swing.JLabel dialogue11;
    private javax.swing.JTextArea dialogue11TextArea;
    private javax.swing.JLabel dialogue12detective;
    private javax.swing.JTextArea dialogue12detectiveTextArea;
    private javax.swing.JLabel dialogue12police;
    private javax.swing.JTextArea dialogue12policeTextArea;
    private javax.swing.JTextArea dialogue1TextArea;
    private javax.swing.JLabel dialogue3;
    private javax.swing.JTextArea dialogue3TextArea;
    private javax.swing.JLabel dialogue4;
    private javax.swing.JTextArea dialogue4TextArea;
    private javax.swing.JLabel dialogue5;
    private javax.swing.JTextArea dialogue5TextArea;
    private javax.swing.JLabel dialogue6;
    private javax.swing.JTextArea dialogue6TextArea;
    private javax.swing.JLabel dialogue7detective;
    private javax.swing.JTextArea dialogue7detectiveTextArea;
    private javax.swing.JLabel dialogue7police;
    private javax.swing.JTextArea dialogue7policeTextArea;
    private javax.swing.JLabel dialogue8;
    private javax.swing.JTextArea dialogue8TextArea;
    private javax.swing.JLabel dialogue9;
    private javax.swing.JTextArea dialogue9TextArea;
    private javax.swing.JLabel exitDoor;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JLabel nextButton;
    private javax.swing.JLabel prevButton;
    private javax.swing.JLabel slideImage;
    // End of variables declaration//GEN-END:variables
}
