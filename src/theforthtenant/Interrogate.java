/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package theforthtenant;


import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 *
 * @author yuji
 */
public class Interrogate extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Interrogate.class.getName());
    
    
    // ---- Reusable overlays ----
    private PauseMenu pauseMenu;
    private OptionsMenu optionsMenu;
    private javax.swing.JComponent activeTooltip;
    private javax.sound.sampled.Clip humClip;
    private javax.sound.sampled.Clip rainClip;
    private javax.sound.sampled.Clip thrillerClip;
    private javax.swing.Timer typewriterTimer;
    private javax.sound.sampled.Clip voiceClip;
    private javax.swing.Timer hintPulseTimer;
    private float hintAlpha = 0f;
    private javax.swing.JComponent hintLabel; 
    
    // ---- Interrogation Scene State ----
    private javax.swing.ImageIcon loungeBgIcon;
    private int currentSuspect = -1; // -1 = Lounge, 0 = Cardo, 1 = Cathy, 2 = Andy
    
    // ---- Dialogue state ----
    private Node next;                  // node to show on the next click (null = conversation over)
    private Node activeChoice;          // the choice node currently on screen
    private boolean awaitingChoice;     // true while option boxes are showing
    private boolean inDialogue;         // true once the first node has appeared
    private String typingFull = "";     // full text of the line being typed
    private javax.swing.JTextArea typingArea;

    // Option widgets grouped by option count: [0] = 1 option, [1] = 2, [2] = 3
    private javax.swing.JScrollPane[][] optPanes;
    private javax.swing.JTextArea[][]   optAreas;
    private javax.swing.JLabel[][]      optLabels;
    
    // Suspect images
    private javax.swing.ImageIcon cardoIdle, cardoTalk;
    private javax.swing.ImageIcon cathyIdle, cathyTalk;
    private javax.swing.ImageIcon andyIdle, andyTalk;

    /** Metadata for one suspect. */
    private static final class Suspect {
        final String id;
        final String displayName;
        final String portraitPath;
        final String hoverPath;
        final int hoverDx;
        final int hoverDy;

        Suspect(String id, String name, String normal, String hover,
                int hoverDx, int hoverDy) {
            this.id = id;
            this.displayName = name;
            this.portraitPath = normal;
            this.hoverPath = hover;
            this.hoverDx = hoverDx;
            this.hoverDy = hoverDy;
        }
    }

    /** The three tenants, in display order (left → middle → right). */
    private final Suspect[] suspects = {
        new Suspect("cardo", "CARDO",
            "/Images/gameplay/interrogate/suspects/cardo.png",
            "/Images/gameplay/interrogate/suspects/cardo_hovered.png",
            0, 0),
        new Suspect("cathy", "CATHY",
            "/Images/gameplay/interrogate/suspects/cathy.png",
            "/Images/gameplay/interrogate/suspects/cathy_hovered.png",
            0, 0),
        new Suspect("andy", "ANDY",
            "/Images/gameplay/interrogate/suspects/andy.png",
            "/Images/gameplay/interrogate/suspects/andy_hovered.png",
            0, 0),
    };
    
    /** One step of the conversation: either a spoken line or a set of choices. */
    private static final class Node {
        final boolean isDetective;
        final String text;          // Text shown in the box
        final String[] options;     // The choices shown
        final String[] spokenText;  // What the detective actually says when clicked
        final String[] spokenAudio;      // NEW: audio for the detective's spoken sentence
        final Node[] branches;
        final String audioPath;          // NEW: audio for this node's line
        Node next;

    private Node(boolean isDetective, String text, String[] options, String audioPath) {
        this.isDetective = isDetective;
        this.text = text;
        this.options = options;
        this.audioPath = audioPath;
        this.spokenText = (options == null) ? null : new String[options.length];
        this.spokenAudio = (options == null) ? null : new String[options.length];
        this.branches = (options == null) ? null : new Node[options.length];
    }

    // Old factories (silent) — unchanged.
    static Node detective(String text)    { return new Node(true,  text, null, null); }
    static Node suspect(String text)      { return new Node(false, text, null, null); }
    static Node choice(String... options) { return new Node(true, null, options, null); }

    // New voiced factories.
    static Node detective(String text, String audioPath) {
        return new Node(true, text, null, audioPath);
    }
    static Node suspect(String text, String audioPath) {
        return new Node(false, text, null, audioPath);
    }

    Node then(Node n) { next = n; return n; }

    Node on(int i, Node n) {
        branches[i] = n;
        spokenText[i] = options[i];
        return n;
    }

    Node on(int i, String spoken, Node n) {
        branches[i] = n;
        spokenText[i] = spoken;
        return n;
    }

    // NEW: choice-with-audio
    Node on(int i, String spoken, String audioPath, Node n) {
        branches[i] = n;
        spokenText[i] = spoken;
        spokenAudio[i] = audioPath;
        return n;
    }
}

    /** The whole Cardo interrogation, mapped from the new flowchart. */
    private Node buildCardoTree() {
        final String gloves = "The lock on the rooptop was pried open with a lockpick and pliers. Coincedentally, we found your empty toolbox in the living room without the said tools in place. Admit you broke open the roof lock!";
        final String boots  = "Size 9 heavy boots match the mud prints on the roof stairwell. Yours were hidden, soaked wet, in the laundry room.";
        
        final String toolsReply = "I use those tools for every repair in this house! They sit out in the open—anyone could have grabbed them to frame me!";
        final String bootsMissing = "Those boots went missing from outside my room last night! Someone stole them, wore them out, and dumped them in the laundry!";
        final String bootsQ = "If your boots were missing, why didn't you mention that to us when we first arrived?";
        final String bootsFinal = "I thought someone kicked them aside by mistake! I only realized they were soaked when I found them this morning.";

        // 0. START NODE
        Node startNode = Node.choice("Start the interrogation");
        // 1. INTRO
        Node intro1 = Node.detective("Let the record show that today, we are conducting this formal interview at the station regarding the ongoing homicide investigation into the incident at the San Lorenzo Street boarding house.\n\nBefore we begin asking any questions, it is my duty to inform you of your constitutional rights under the law.",    
                "/audio/voice/interrogate/cardo/cardo_s01_detective.wav");
        Node intro2 = Node.detective("You have the right to remain silent. Any statement you make can and will be used against you in a court of law.\n\nYou have the right to have competent and independent counsel of your own choosing. If you cannot afford the services of counsel, you will be provided with one free of charge.\n\nYou have the right to be assisted by counsel during this investigation.",    
                "/audio/voice/interrogate/cardo/cardo_s02_detective.wav");
        Node intro3 = Node.detective("Do you understand each of these rights as I have explained them to you? Knowing and understanding your rights, are you speaking to me voluntarily, of your own free will, without any coercion, threats, or promises of leniency?",    
                "/audio/voice/interrogate/cardo/cardo_s03_detective.wav");
        Node introReply = Node.suspect("Yeah, I get it. Let's just get this over with so you can realize you're wasting your time.",    
                "/audio/voice/interrogate/cardo/cardo_s04_suspect.wav");
        
        startNode.on(0, "Start the interrogation", intro1);
        
        intro1.then(intro2).then(intro3).then(introReply);

        // 2. MAIN CHOICES
        Node mainChoices = Node.choice(
            "Ask about his whereabouts at the time of the incident",
            "Ask about his work boots",
            "Ask about his toolbox"
        );
        introReply.then(mainChoices);

        // When the player clicks option 0, the detective says the long question
        mainChoices.on(0, 
            "Where were you between 1:30 AM and 2:30 AM? Were you the one who went up to the rooftop?",
            "/audio/voice/interrogate/cardo/cardo_s05_detective.wav",
            Node.suspect("I was already in bed around that time.",
                         "/audio/voice/interrogate/cardo/cardo_s06_suspect.wav")
        );
        
        // Option 1 (Boots) -> The detective says the long boots statement
        mainChoices.on(1, 
            "Size 9 heavy boots match the mud prints on the roof stairwell. Yours were hidden, soaked wet, in the laundry room.",
            "/audio/voice/interrogate/cardo/cardo_s15_detective.wav",
            Node.suspect(bootsMissing,
                         "/audio/voice/interrogate/cardo/cardo_s16_suspect.wav")
        );
        
        // Option 2 (Gloves) -> The detective says the long gloves statement
        mainChoices.on(2, 
            "The lock on the rooptop was pried open with a lockpick and pliers. Coincedentally, we found your empty toolbox in the living room without the said tools in place. Admit you broke open the roof lock!",
            "/audio/voice/interrogate/cardo/cardo_s19_detective.wav",
            Node.suspect(toolsReply,
                         "/audio/voice/interrogate/cardo/cardo_s20_suspect.wav")
        );
        
        // ============================================================
        // BRANCH 1: EMPATHETIC (Left)
        // ============================================================
        // (Note: We already linked option 0 to the first suspect reply in Step 3)
        Node branch1First = mainChoices.branches[0];
        Node b1_empathy = Node.detective("I know you cared about Abby. Helping us establish a tight timeline is the best way to clear your name.",    
                "/audio/voice/interrogate/cardo/cardo_s07_detective.wav");
        Node b1_reply = Node.suspect("Look... I really was in bed. But if it helps, someone definitely walked past my door around 1:20 AM heading toward Cathy's room.",    
                "/audio/voice/interrogate/cardo/cardo_s08_suspect.wav");
        Node b1_breakupQ = Node.detective("We heard your breakup with Abby didn't end well. Did you two have an argument recently?",    
                "/audio/voice/interrogate/cardo/cardo_s09_detective.wav");
        Node b1_breakupReply = Node.suspect("Well, that woman dated a new guy after just 2 weeks! What did you expect?!",
                "/audio/voice/interrogate/cardo/cardo_s10_suspect.wav");
        
        branch1First.then(b1_empathy).then(b1_reply).then(b1_breakupQ).then(b1_breakupReply);
        
        Node b1_breakupChoices = Node.choice(
            "Neighbors heard you screaming at her two days ago. Was that argument the trigger?",
            "Breakups are painful, Cardo. Did things escalate between you two recently?"
        );
        b1_breakupReply.then(b1_breakupChoices);

        Node b1_opt1Reply = Node.suspect("Breakups are messy! Raised voices don't mean I wanted to end her life!",
            "/audio/voice/interrogate/cardo/cardo_s12_suspect.wav");
        b1_opt1Reply.then(mainChoices); 
        b1_breakupChoices.on(0,
            "Neighbors heard you screaming at her two days ago. Was that argument the trigger?",
            "/audio/voice/interrogate/cardo/cardo_s11_detective.wav",
            b1_opt1Reply);

        Node b1_opt2Reply = Node.suspect("We had unresolved issues, sure, but we tried to talk them through peacefully. I cared about her.",
            "/audio/voice/interrogate/cardo/cardo_s14_suspect.wav");
        b1_opt2Reply.then(mainChoices); 
        b1_breakupChoices.on(1,
            "Breakups are painful, Cardo. Did things escalate between you two recently?",
            "/audio/voice/interrogate/cardo/cardo_s13_detective.wav",
            b1_opt2Reply);

        // ============================================================
        // BRANCH 2: BOOTS (Middle)
        // ============================================================
        // (We linked option 1 to bootsMissing in Step 3)
        Node branch2First = mainChoices.branches[1];
        Node b2_Q = Node.detective(bootsQ,
            "/audio/voice/interrogate/cardo/cardo_s17_detective.wav");
        Node b2_final = Node.suspect(bootsFinal,
            "/audio/voice/interrogate/cardo/cardo_s18_suspect.wav");
        
        branch2First.then(b2_Q).then(b2_final).then(mainChoices); 

        // ============================================================
        // BRANCH 3: GLOVES (Right)
        // ============================================================
        // (We linked option 2 to toolsReply in Step 3)
        Node branch3First = mainChoices.branches[2];
        branch3First.then(mainChoices); 

        return startNode;
    }
    
    /** The whole Andy interrogation, mapped from the flowchart. */
    private Node buildAndyTree() {
        // 0. START NODE
        Node startNode = Node.choice("Start the interrogation");

        // 1. INTRO
        Node intro1 = Node.detective("Let the record show that today, We are conducting this formal interview at the station regarding the ongoing homicide investigation into the incident at the San Lorenzo Street boarding house.\n\nBefore we begin asking any questions, it is my duty to inform you of your constitutional rights under the law:",
                "/audio/voice/interrogate/andy/andy_s01_detective.wav");
        
        Node intro2 = Node.detective("You have the right to remain silent. Any statement you make can and will be used against you in a court of law.\n\nYou have the right to have competent and independent counsel of your own choosing. If you cannot afford the services of counsel, you will be provided with one free of charge.\n\nYou have the right to be assisted by counsel during this investigation.",
                "/audio/voice/interrogate/andy/andy_s02_detective.wav");
        
        Node intro3 = Node.detective("Do you understand each of these rights as I have explained them to you? Knowing and understanding your rights, are you speaking to me voluntarily, of your own free will, without any coercion, threats, or promises of leniency?",
                "/audio/voice/interrogate/andy/andy_s03_detective.wav");
        
        Node introReply = Node.suspect("Look, I didn't do anything, and I want to clear this up right now. I'll answer whatever you need.",
                "/audio/voice/interrogate/andy/andy_s04_suspect.wav");

        startNode.on(0, "Start the interrogation", intro1);
        intro1.then(intro2).then(intro3).then(introReply);

        // 2. MAIN CHOICES
        Node mainChoices = Node.choice(
            "Ask about his whereabout during the time of the crime.",
            "Ask about his debt",
            "Ask the sleeping pills"
        );
        introReply.then(mainChoices);

        // ============================================================
        // BRANCH 1: WHEREABOUTS (Left)
        // ============================================================
        Node b1_reply = Node.suspect("I was out at a study hub with my friends.",
                "/audio/voice/interrogate/andy/andy_s06_suspect.wav");
        
        Node b1_choices = Node.choice(
            "What exact time were you out, Andy? Did you remain at the study hub continuously from 1:30 to 2:30 AM?",
            "A mere word doesn't give you an ironclad alibi for an hour-long window. Can you provide any hard proof?"
        );
        
        Node b1_opt1Reply = Node.suspect("Look, I have a selfie photo timestamped at 1:45 AM. I only stepped out briefly for coffee, but my med school peers can confirm I was back shortly after.",
                "/audio/voice/interrogate/andy/andy_s08_suspect.wav");
        Node b1_opt2Reply = Node.suspect("I-I think we have a selfie that time, let me see if I can pull it up",
                "/audio/voice/interrogate/andy/andy_s10_suspect.wav");

        // Link Branch 1 
        mainChoices.on(0, "Where were you between 1:30 AM and 2:30 AM? Were you the one who went up to the rooftop?", 
                "/audio/voice/interrogate/andy/andy_s05_detective.wav", b1_reply);
        b1_reply.then(b1_choices);
        
        b1_choices.on(0, "What exact time were you out, Andy? Did you remain at the study hub continuously from 1:30 to 2:30 AM?", 
                "/audio/voice/interrogate/andy/andy_s07_detective.wav", b1_opt1Reply);
        b1_choices.on(1, "A mere word doesn't give you an ironclad alibi for an hour-long window. Can you provide any hard proof?", 
                "/audio/voice/interrogate/andy/andy_s09_detective.wav", b1_opt2Reply);
        
        b1_opt1Reply.then(mainChoices);
        b1_opt2Reply.then(mainChoices);

        // ============================================================
        // BRANCH 2: DEBT (Middle)
        // ============================================================
        Node b2_reply = Node.suspect("Um... She was... not so kind, but I guess that’s just fair. It’s my fault, afterall.",
                "/audio/voice/interrogate/andy/andy_s12_suspect.wav");
        
        Node b2_choices = Node.choice(
            "Med school debt creates extreme pressure. Did the debt dispute get out of hand? Maybe causing more than just an argument...",
            "We found a ledger notebook. You owe Abby 67,000 pesos and she was pressing you hard for payment."
        );
        
        Node b2_opt1Reply = Node.suspect("I'm drowning in tuition fees! I admit we had a talk about the debt, but it never escalated more than that! That’s not reason enough to be violent!",
                "/audio/voice/interrogate/andy/andy_s14_suspect.wav");
        Node b2_opt2Reply = Node.suspect("She was pressing me hard, yes... but I was actively securing a loan. I would never resort to violence.",
                "/audio/voice/interrogate/andy/andy_s16_suspect.wav");

        // Link Branch 2
        mainChoices.on(1, "I heard Abby hasn’t been very kind to you, because of 67000 you owed her? It Is that true?", 
                "/audio/voice/interrogate/andy/andy_s11_detective.wav", b2_reply);
        b2_reply.then(b2_choices);
        
        b2_choices.on(0, "Med school debt creates extreme pressure. Did the debt dispute get out of hand? Maybe causing more than just an argument...", 
                "/audio/voice/interrogate/andy/andy_s13_detective.wav", b2_opt1Reply);
        b2_choices.on(1, "We found a ledger notebook. You owe Abby 67,000 and she was pressing you hard for payment.", 
                "/audio/voice/interrogate/andy/andy_s15_detective.wav", b2_opt2Reply);
        
        b2_opt1Reply.then(mainChoices);
        b2_opt2Reply.then(mainChoices);

        // ============================================================
        // BRANCH 3: SLEEPING PILLS (Right)
        // ============================================================
        Node b3_reply1 = Node.suspect("Yes, detective. How did you know about that?",
                "/audio/voice/interrogate/andy/andy_s18_suspect.wav");
        
        Node b3_mid = Node.detective("Traces of sleeping pills were found in Abby's coffee. Were those from your prescription?",
                "/audio/voice/interrogate/andy/andy_s19_detective.wav");
                
        Node b3_reply2 = Node.suspect("I keep it inside my medical bag and it isn't locked... I just leave it in the kitchen or in the living room most times. Anyone in the house could have unzipped it.",
                "/audio/voice/interrogate/andy/andy_s20_suspect.wav");
        
        Node b3_end = Node.detective("One last question, did you notice anything unusual or suspicious in the house before you went out?",
                "/audio/voice/interrogate/andy/andy_s21_detective.wav");
                
        Node b3_reply3 = Node.suspect("I saw Cardo walking around in slippers when he got home around 9PM last night—which is weird because he always wears heavy boots. And around 11:15 PM before I went out, Cathy's light was on and I heard whispering near her room.",
                "/audio/voice/interrogate/andy/andy_s22_suspect.wav");
        
        Node b3_final = Node.detective("Thank you for your cooperation, Andy.",
                "/audio/voice/interrogate/andy/andy_s23_detective.wav");

        // Link Branch 3
        mainChoices.on(2, "Were you having a hard time to sleep these past days, Andy?", 
                "/audio/voice/interrogate/andy/andy_s17_detective.wav", b3_reply1);
        
        b3_reply1.then(b3_mid).then(b3_reply2).then(b3_end).then(b3_reply3).then(b3_final);
        
        // Loop back to the choices
        b3_final.then(mainChoices); 

        return startNode;
    }
    
    /** The whole Cathy interrogation, mapped from the flowchart. */
    private Node buildCathyTree() {
        // 0. START NODE
        Node startNode = Node.choice("Start the interrogation");

        // 1. INTRO
        Node intro1 = Node.detective("Let the record show that today, We are conducting this formal interview at the station regarding the ongoing homicide investigation into the incident at the San Lorenzo Street boarding house.\n\nBefore we begin asking any questions, it is my duty to inform you of your constitutional rights under the law:",
                "/audio/voice/interrogate/cathy/cathy_s01_detective.wav");
        
        Node intro2 = Node.detective("You have the right to remain silent. Any statement you make can and will be used against you in a court of law.\n\nYou have the right to have competent and independent counsel of your own choosing. If you cannot afford the services of counsel, you will be provided with one free of charge.\n\nYou have the right to be assisted by counsel during this investigation.",
                "/audio/voice/interrogate/cathy/cathy_s02_detective.wav");
        
        Node intro3 = Node.detective("Do you understand each of these rights as I have explained them to you? Knowing and understanding your rights, are you speaking to me voluntarily, of your own free will, without any coercion, threats, or promises of leniency?",
                "/audio/voice/interrogate/cathy/cathy_s03_detective.wav");
        
        Node introReply = Node.suspect("I... yes, I understand my rights. I'll cooperate, I just want tthis to be over...",
                "/audio/voice/interrogate/cathy/cathy_s04_suspect.wav");

        startNode.on(0, "Start the interrogation", intro1);
        intro1.then(intro2).then(intro3).then(introReply);

        // 2. MAIN CHOICES
        Node mainChoices = Node.choice(
            "Ask about her whereabouts during the time of the incident",
            "Ask about the bleach soaked rag",
            "Ask about her fight with Abby"
        );
        introReply.then(mainChoices);

        // ============================================================
        // BRANCH 1: WHEREABOUTS (Left)
        // ============================================================
        Node b1_reply = Node.suspect("I... I’m asleep. I remember going to bed early, detective.",
                "/audio/voice/interrogate/cathy/cathy_s06_suspect.wav");
        
        Node b1_choices = Node.choice(
            "Hmm... You don’t seem to certain yourself, Cathy. Are there any other things you remember doing before going to bed?",
            "You are the only other female tenant here in this boarding house, do you often have female vistors here?"
        );
        
        // Link the choice directly to Cathy's reply
        mainChoices.on(0, "Cathy, what were you doing between 1:00 AM and 6:00 AM?", 
                "/audio/voice/interrogate/cathy/cathy_s05_detective.wav", b1_reply);
        b1_reply.then(b1_choices);

        // Sub-choice 1 (The memory gaps)
        Node b1_opt1_reply = Node.suspect("I really don't remember... I went to sleep and woke up at 6:00 AM completely exhausted, like five hours vanished.",
                "/audio/voice/interrogate/cathy/cathy_s08_suspect.wav");
        
        Node b1_opt1_followup1 = Node.detective("Do these memory gaps occur frequently when you are under severe psychological stress?",
                "/audio/voice/interrogate/cathy/cathy_s09_detective.wav");
        
        Node b1_opt1_followup2 = Node.suspect("S-sometimes... when stress gets too high, I black out and wake up without knowing what happened.",
                "/audio/voice/interrogate/cathy/cathy_s10_suspect.wav");
        
        Node b1_opt1_followup3 = Node.detective("I see. Interesting.",
                "/audio/voice/interrogate/cathy/cathy_s11_detective.wav");
        
        b1_choices.on(0, "Hmm... You don’t seem to certain yourself, Cathy. Are there any other things you remember doing before going to bed?", 
                "/audio/voice/interrogate/cathy/cathy_s07_detective.wav", b1_opt1_reply);
        b1_opt1_reply.then(b1_opt1_followup1).then(b1_opt1_followup2).then(b1_opt1_followup3).then(mainChoices);
        
        // Sub-choice 2 (Female visitors)
        Node b1_opt2_reply = Node.suspect("Almost never, detective. I don’t even remember if there was ever one in the past.",
                "/audio/voice/interrogate/cathy/cathy_s13_suspect.wav");
        
        Node b1_opt2_followup = Node.detective("Hmm. That’s something.",
                "/audio/voice/interrogate/cathy/cathy_s14_detective.wav");
        
        b1_choices.on(1, "You are the only other female tenant here in this boarding house, do you often have female vistors here?", 
                "/audio/voice/interrogate/cathy/cathy_s12_detective.wav", b1_opt2_reply);
        b1_opt2_reply.then(b1_opt2_followup).then(mainChoices);

        // ============================================================
        // BRANCH 2: BLEACH RAG (Middle)
        // ============================================================
        Node b2_reply = Node.suspect("I-I don't know anything about that! I don't even lock my door at night—someone must have planted it while I was asleep!",
                "/audio/voice/interrogate/cathy/cathy_s16_suspect.wav");

        mainChoices.on(1, "A rag soaked in bleach was found under your bed. Why was it with your things?", 
                "/audio/voice/interrogate/cathy/cathy_s15_detective.wav", b2_reply);
        b2_reply.then(mainChoices);

        // ============================================================
        // BRANCH 3: FIGHT WITH ABBY (Right)
        // ============================================================
        Node b3_reply1 = Node.suspect("She was... hard on me. She always has been anyway. We were never really close, but I care for her.",
                "/audio/voice/interrogate/cathy/cathy_s18_suspect.wav");
        
        Node b3_followup1 = Node.detective("Has your fight ever escalated? I bet it wasn't easy for you dealing with her everyday, especially with your situation, you're living in the same house.",
                "/audio/voice/interrogate/cathy/cathy_s19_detective.wav");
        
        Node b3_reply2 = Node.suspect("I always try to de-escalate it detective, I just want to live in peace...",
                "/audio/voice/interrogate/cathy/cathy_s20_suspect.wav");
        
        Node b3_followup2 = Node.detective("A voice on the CCTV shouting 'Leave me alone!' near your room, did you and Abby have an encounter again right before the murder?",
                "/audio/voice/interrogate/cathy/cathy_s21_detective.wav");
        
        Node b3_reply3 = Node.suspect("She was hostile toward me lately... But I never hurt her! I swear! I even tried to apologize to her before I slept.",
                "/audio/voice/interrogate/cathy/cathy_s22_suspect.wav");

        // Link Branch 3
        mainChoices.on(2, "How was your relationship with Abby recently?", 
                "/audio/voice/interrogate/cathy/cathy_s17_detective.wav", b3_reply1);
        b3_reply1.then(b3_followup1).then(b3_reply2).then(b3_followup2).then(b3_reply3).then(mainChoices);

        return startNode;
    }

    /**
     * Creates new form Interrogate
     */
        public Interrogate() {
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

        // ---- Load Backgrounds ----
        loungeBgIcon = loadIcon("/Images/gameplay/interrogate/lounge_bg.png");
        
        // Set initial background
        backgroundLabel.setIcon(loungeBgIcon);

        // ---- Load Suspects (Idle & Talk) ----
        cardoIdle = loadIcon("/Images/gameplay/interrogate/interrogation/cardo_idle.png");
        cardoTalk = loadIcon("/Images/gameplay/interrogate/interrogation/cardo_talk.png");
        
        cathyIdle = loadIcon("/Images/gameplay/interrogate/interrogation/cathy_idle.png");
        cathyTalk = loadIcon("/Images/gameplay/interrogate/interrogation/cathy_talk.png");
        
        andyIdle  = loadIcon("/Images/gameplay/interrogate/interrogation/andy_idle.png");
        andyTalk  = loadIcon("/Images/gameplay/interrogate/interrogation/andy_talk.png");
        
        // ---- Ambience: indoor hum only (no rain in the lounge) ----
        humClip = AudioCache.loop("/audio/sfx/ambience/indoor_hum.wav");
        
        new javax.swing.SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                AudioCache.prepare(
                    "/audio/sfx/ambience/thriller_ambient.wav",
                    "/audio/voice/interrogate/cardo/cardo_s01_detective.wav",
                    "/audio/voice/interrogate/cardo/cardo_s02_detective.wav",
                    "/audio/voice/interrogate/cardo/cardo_s03_detective.wav",
                    "/audio/voice/interrogate/cardo/cardo_s04_suspect.wav",
                    "/audio/voice/interrogate/cardo/cardo_s05_detective.wav",
                    "/audio/voice/interrogate/cardo/cardo_s06_suspect.wav",
                    "/audio/voice/interrogate/cardo/cardo_s07_detective.wav",
                    "/audio/voice/interrogate/cardo/cardo_s08_suspect.wav",
                    "/audio/voice/interrogate/cardo/cardo_s09_detective.wav",
                    "/audio/voice/interrogate/cardo/cardo_s10_suspect.wav",
                    "/audio/voice/interrogate/cardo/cardo_s11_detective.wav",
                    "/audio/voice/interrogate/cardo/cardo_s12_suspect.wav",
                    "/audio/voice/interrogate/cardo/cardo_s13_detective.wav",
                    "/audio/voice/interrogate/cardo/cardo_s14_suspect.wav",
                    "/audio/voice/interrogate/cardo/cardo_s15_detective.wav",
                    "/audio/voice/interrogate/cardo/cardo_s16_suspect.wav",
                    "/audio/voice/interrogate/cardo/cardo_s17_detective.wav",
                    "/audio/voice/interrogate/cardo/cardo_s18_suspect.wav",
                    "/audio/voice/interrogate/cardo/cardo_s19_detective.wav",
                    "/audio/voice/interrogate/cardo/cardo_s20_suspect.wav",
                    
                    "/audio/voice/interrogate/andy/andy_s01_detective.wav",
                    "/audio/voice/interrogate/andy/andy_s02_detective.wav",
                    "/audio/voice/interrogate/andy/andy_s03_detective.wav",
                    "/audio/voice/interrogate/andy/andy_s04_suspect.wav",
                    "/audio/voice/interrogate/andy/andy_s05_detective.wav",
                    "/audio/voice/interrogate/andy/andy_s06_suspect.wav",
                    "/audio/voice/interrogate/andy/andy_s07_detective.wav",
                    "/audio/voice/interrogate/andy/andy_s08_suspect.wav",
                    "/audio/voice/interrogate/andy/andy_s09_detective.wav",
                    "/audio/voice/interrogate/andy/andy_s10_suspect.wav",
                    "/audio/voice/interrogate/andy/andy_s11_detective.wav",
                    "/audio/voice/interrogate/andy/andy_s12_suspect.wav",
                    "/audio/voice/interrogate/andy/andy_s13_detective.wav",
                    "/audio/voice/interrogate/andy/andy_s14_suspect.wav",
                    "/audio/voice/interrogate/andy/andy_s15_detective.wav",
                    "/audio/voice/interrogate/andy/andy_s16_suspect.wav",
                    "/audio/voice/interrogate/andy/andy_s17_detective.wav",
                    "/audio/voice/interrogate/andy/andy_s18_suspect.wav",
                    "/audio/voice/interrogate/andy/andy_s19_detective.wav",
                    "/audio/voice/interrogate/andy/andy_s20_suspect.wav",
                    "/audio/voice/interrogate/andy/andy_s21_detective.wav",
                    "/audio/voice/interrogate/andy/andy_s22_suspect.wav",
                    "/audio/voice/interrogate/andy/andy_s23_detective.wav",
                    
                    "/audio/voice/interrogate/cathy/cathy_s01_detective.wav",
                    "/audio/voice/interrogate/cathy/cathy_s02_detective.wav",
                    "/audio/voice/interrogate/cathy/cathy_s03_detective.wav",
                    "/audio/voice/interrogate/cathy/cathy_s04_suspect.wav",
                    "/audio/voice/interrogate/cathy/cathy_s05_detective.wav",
                    "/audio/voice/interrogate/cathy/cathy_s06_suspect.wav",
                    "/audio/voice/interrogate/cathy/cathy_s07_detective.wav",
                    "/audio/voice/interrogate/cathy/cathy_s08_suspect.wav",
                    "/audio/voice/interrogate/cathy/cathy_s09_detective.wav",
                    "/audio/voice/interrogate/cathy/cathy_s10_suspect.wav",
                    "/audio/voice/interrogate/cathy/cathy_s11_detective.wav",
                    "/audio/voice/interrogate/cathy/cathy_s12_detective.wav",
                    "/audio/voice/interrogate/cathy/cathy_s13_suspect.wav",
                    "/audio/voice/interrogate/cathy/cathy_s14_detective.wav",
                    "/audio/voice/interrogate/cathy/cathy_s15_detective.wav",
                    "/audio/voice/interrogate/cathy/cathy_s16_suspect.wav",
                    "/audio/voice/interrogate/cathy/cathy_s17_detective.wav",
                    "/audio/voice/interrogate/cathy/cathy_s18_suspect.wav",
                    "/audio/voice/interrogate/cathy/cathy_s19_detective.wav",
                    "/audio/voice/interrogate/cathy/cathy_s20_suspect.wav",
                    "/audio/voice/interrogate/cathy/cathy_s21_detective.wav",
                    "/audio/voice/interrogate/cathy/cathy_s22_suspect.wav"
                );
                return null;
            }
        }.execute();
        
        optPanes  = new javax.swing.JScrollPane[][] { {jScrollPane1}, {jScrollPane7, jScrollPane8}, {jScrollPane4, jScrollPane5, jScrollPane6} };
        optAreas  = new javax.swing.JTextArea[][]   { {option1TextArea}, {option2leftTextArea, option2rightTextArea}, {option3leftTextArea, option3middleTextArea, option3rightTextArea} };
        optLabels = new javax.swing.JLabel[][]      { {option1}, {option2left, option2right}, {option3left, option3middle, option3right} };
        
        option1.setVisible(false);
        jScrollPane1.setVisible(false); 
        option1TextArea.setVisible(false);
        detectiveChosen.setVisible(false);        
        jScrollPane2.setVisible(false);   
        suspectAnswer.setVisible(false);
        jScrollPane3.setVisible(false);
        option3left.setVisible(false);
        option3middle.setVisible(false);
        option3right.setVisible(false);
        jScrollPane4.setVisible(false);
        jScrollPane5.setVisible(false);
        jScrollPane6.setVisible(false);
        option2left.setVisible(false);
        option2right.setVisible(false);
        jScrollPane7.setVisible(false);
        jScrollPane8.setVisible(false);
        option2leftTextArea.setVisible(false);
        option2rightTextArea.setVisible(false);
        option3leftTextArea.setVisible(false);
        option3middleTextArea.setVisible(false);
        option3rightTextArea.setVisible(false);
        suspectAnswerTextArea.setVisible(false);        
        detectiveChosenTextArea.setVisible(false);
        contentPanel.revalidate();
        contentPanel.repaint();
        
        // ---- Style the suspect name plate (JLabel) ----
        suspectNamePlate.setOpaque(false);            // fully transparent
        suspectNamePlate.setBorder(null);
        suspectNamePlate.setFont(getCustomFont(16f).deriveFont(java.awt.Font.BOLD)); 
        suspectNamePlate.setForeground(java.awt.Color.BLACK);
        suspectNamePlate.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        suspectNamePlate.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        suspectNamePlate.setText("");
        suspectNamePlate.setVisible(false);           // hidden in the lounge
        
        // ---- Custom pulsing Hint Text ----
        javax.swing.JComponent hintOverlay = new javax.swing.JComponent() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                if (hintAlpha <= 0f) return;
                
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                                    java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                
                g2.setFont(getCustomFont(10f)); // Pixel font
                g2.setColor(new java.awt.Color(255, 255, 255, (int)(hintAlpha * 255))); // White text with pulse alpha
                
                String text = "Click anywhere to continue";
                java.awt.FontMetrics fm = g2.getFontMetrics();
                
                // Center it horizontally, and put it near the bottom
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = getHeight() - 15; 
                
                g2.drawString(text, x, y);
                g2.dispose();
            }
        };
        hintOverlay.setOpaque(false);
        hintOverlay.setBounds(0, 0, 1150, 680);
        hintOverlay.setVisible(false); // Hidden by default
        
        // Add it to the panel
        contentPanel.add(hintOverlay, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1150, 680));
        
        // Save it as a field so we can show/hide it later
        this.hintLabel = hintOverlay;

        // ---- Wire up the three suspect portraits ----
        JLabel[] portraitLabels = { cardoButton, cathyButton, andyButton };
        for (int i = 0; i < suspects.length; i++) {
            final Suspect s = suspects[i];
            final JLabel lbl = portraitLabels[i];
            final int suspectIndex = i; // Need this to pass into the listener

            final ImageIcon normal = loadIcon(s.portraitPath);
            final ImageIcon hover  = (s.hoverPath != null)
                    ? loadIcon(s.hoverPath) : normal;

            if (normal != null) lbl.setIcon(normal);
            lbl.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            lbl.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (hover != null) lbl.setIcon(hover);
                    lbl.setLocation(lbl.getX() + s.hoverDx,
                                    lbl.getY() + s.hoverDy);
                    showTooltip(lbl, "/Images/gameplay/interrogate/tooltip_investigate.png");
                    AudioCache.play("/audio/sfx/ui/hover.wav");
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    if (normal != null) lbl.setIcon(normal);
                    lbl.setLocation(lbl.getX() - s.hoverDx,
                                    lbl.getY() - s.hoverDy);
                    hideTooltip();
                }
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (TransitionOverlay.isPlaying()) return;
                    if (pauseMenu != null && pauseMenu.isPaused()) return;

                    AudioCache.play("/audio/sfx/gameplay/door_close.wav");
                    
                    // CALL THE TRANSITION METHOD HERE
                    startInterrogation(suspectIndex); 
                }
            });
        }

        // Hover effect for the option box
        final java.awt.Color hoverColor  = new java.awt.Color(150, 150, 150);
        final java.awt.Color normalColor = java.awt.Color.WHITE;
        final java.awt.Cursor handCursor = new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR);

        // The text area and viewport have their own cursors, so set them all
        jScrollPane1.setCursor(handCursor);
        jScrollPane1.getViewport().setCursor(handCursor);
        option1TextArea.setCursor(handCursor);
        
        wireChoices();

        // Click anywhere (and on the dialogue text itself) to advance
        java.awt.event.MouseAdapter advance = new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { advanceDialogue(); }
        };
        contentPanel.addMouseListener(advance);
        // Text areas/scroll panes consume clicks themselves, so give them the same listener
        for (javax.swing.JScrollPane p : new javax.swing.JScrollPane[] { jScrollPane2, jScrollPane3 }) {
            p.addMouseListener(advance);
            p.getViewport().addMouseListener(advance);
        }
        detectiveChosenTextArea.addMouseListener(advance);
        suspectAnswerTextArea.addMouseListener(advance);        

        // ---- Back button ----
        final ImageIcon backNormal = new ImageIcon(
                getClass().getResource("/Images/ui/back_button.png"));
        final ImageIcon backHover = new ImageIcon(
                darken(backNormal.getImage()));

        backButton.setIcon(backNormal);
        backButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                backButton.setIcon(backHover);
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                backButton.setIcon(backNormal);
            }        
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (currentSuspect >= 0) {
                    // We are in a conversation, just go back to the lounge
                    AudioCache.play("/audio/sfx/ui/select.wav");
                    returnToLounge();
                } else {
                    // We are in the lounge, exit completely
                    closeInterrogate();
                }
            }
        });

        // ---- ESC also backs out ----
         javax.swing.JRootPane root = getRootPane();
        
        // Create the action once
        javax.swing.AbstractAction backAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (currentSuspect >= 0) {
                    returnToLounge();
                } else {
                    closeInterrogate();
                }
            }
        };

        // Bind ESC
        root.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "backAction");
        root.getActionMap().put("backAction", backAction);

        // Bind 4
        root.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_4, 0), "backAction");
        root.getActionMap().put("backAction", backAction);

        // ---- Options popup ----
        optionsMenu = OptionsMenu.attachTo(this, new OptionsMenu.Callbacks() {
            @Override public void onSoundChanged(int percent) {
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }
            @Override public void onClose() {
                pauseMenu.showPopupOnly();
            }
        });

        // ---- Pause menu ----
        pauseMenu = PauseMenu.attachTo(this, PauseMenu.Corner.TOP_LEFT,
                new PauseMenu.Callbacks() {
            @Override public void onPause() {
                if (humClip      != null) humClip.stop();
                if (thrillerClip != null) thrillerClip.stop();
            }
            @Override public void onResume() {
                if (humClip      != null) humClip.start();
                if (thrillerClip != null) thrillerClip.start();
            }
            @Override public void onOptions() {
                pauseMenu.hidePopupOnly();
                optionsMenu.show();
            }
            @Override public void onMainMenu() {
                hideTooltip();
                if (TransitionOverlay.isPlaying()) return;
                pauseMenu.hideOverlay();

                TransitionOverlay.play(Interrogate.this, () -> {
                    MainMenu menu = new MainMenu();
                    menu.setLocation(getLocation());
                    menu.setVisible(true);
                    Interrogate.this.dispose();
                });
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

        contentPanel = new javax.swing.JPanel();
        suspectNamePlate = new javax.swing.JLabel();
        suspectAnswer = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        option2rightTextArea = new javax.swing.JTextArea();
        jScrollPane7 = new javax.swing.JScrollPane();
        option2leftTextArea = new javax.swing.JTextArea();
        option2right = new javax.swing.JLabel();
        option2left = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        option3rightTextArea = new javax.swing.JTextArea();
        jScrollPane5 = new javax.swing.JScrollPane();
        option3middleTextArea = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        option3leftTextArea = new javax.swing.JTextArea();
        option3right = new javax.swing.JLabel();
        option3middle = new javax.swing.JLabel();
        option3left = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        suspectAnswerTextArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        detectiveChosenTextArea = new javax.swing.JTextArea();
        detectiveChosen = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        option1TextArea = new javax.swing.JTextArea();
        option1 = new javax.swing.JLabel();
        backButton = new javax.swing.JLabel();
        andyButton = new javax.swing.JLabel();
        cathyButton = new javax.swing.JLabel();
        cardoButton = new javax.swing.JLabel();
        backgroundLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        contentPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        suspectNamePlate.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        contentPanel.add(suspectNamePlate, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 492, 140, 40));

        suspectAnswer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/interrogate/dialogue/suspect_text.png"))); // NOI18N
        contentPanel.add(suspectAnswer, new org.netbeans.lib.awtextra.AbsoluteConstraints(101, 295, -1, -1));

        jScrollPane8.setBorder(null);
        jScrollPane8.setOpaque(false);

        option2rightTextArea.setEditable(false);
        option2rightTextArea.setColumns(20);
        option2rightTextArea.setLineWrap(true);
        option2rightTextArea.setRows(5);
        option2rightTextArea.setWrapStyleWord(true);
        option2rightTextArea.setOpaque(false);
        jScrollPane8.setViewportView(option2rightTextArea);

        contentPanel.add(jScrollPane8, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 536, 420, 70));

        jScrollPane7.setBorder(null);
        jScrollPane7.setOpaque(false);

        option2leftTextArea.setEditable(false);
        option2leftTextArea.setColumns(20);
        option2leftTextArea.setLineWrap(true);
        option2leftTextArea.setRows(5);
        option2leftTextArea.setWrapStyleWord(true);
        option2leftTextArea.setOpaque(false);
        jScrollPane7.setViewportView(option2leftTextArea);

        contentPanel.add(jScrollPane7, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 536, 420, 70));

        option2right.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/interrogate/dialogue/detective_option2.png"))); // NOI18N
        contentPanel.add(option2right, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 520, -1, -1));

        option2left.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/interrogate/dialogue/detective_option2.png"))); // NOI18N
        contentPanel.add(option2left, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 520, -1, -1));

        jScrollPane6.setBorder(null);
        jScrollPane6.setOpaque(false);

        option3rightTextArea.setEditable(false);
        option3rightTextArea.setColumns(20);
        option3rightTextArea.setLineWrap(true);
        option3rightTextArea.setRows(5);
        option3rightTextArea.setWrapStyleWord(true);
        option3rightTextArea.setOpaque(false);
        jScrollPane6.setViewportView(option3rightTextArea);

        contentPanel.add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 500, 280, 100));

        jScrollPane5.setBorder(null);
        jScrollPane5.setOpaque(false);

        option3middleTextArea.setEditable(false);
        option3middleTextArea.setColumns(20);
        option3middleTextArea.setLineWrap(true);
        option3middleTextArea.setRows(5);
        option3middleTextArea.setWrapStyleWord(true);
        option3middleTextArea.setOpaque(false);
        jScrollPane5.setViewportView(option3middleTextArea);

        contentPanel.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 500, 270, 100));

        jScrollPane4.setBorder(null);
        jScrollPane4.setOpaque(false);

        option3leftTextArea.setEditable(false);
        option3leftTextArea.setColumns(20);
        option3leftTextArea.setLineWrap(true);
        option3leftTextArea.setRows(5);
        option3leftTextArea.setWrapStyleWord(true);
        option3leftTextArea.setOpaque(false);
        jScrollPane4.setViewportView(option3leftTextArea);

        contentPanel.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 500, 260, 100));

        option3right.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/interrogate/dialogue/detective_option3.png"))); // NOI18N
        contentPanel.add(option3right, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 475, -1, -1));
        option3right.getAccessibleContext().setAccessibleName("");

        option3middle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/interrogate/dialogue/detective_option3.png"))); // NOI18N
        contentPanel.add(option3middle, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 475, -1, -1));

        option3left.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/interrogate/dialogue/detective_option3.png"))); // NOI18N
        contentPanel.add(option3left, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 475, -1, -1));

        jScrollPane3.setBorder(null);
        jScrollPane3.setOpaque(false);

        suspectAnswerTextArea.setEditable(false);
        suspectAnswerTextArea.setColumns(20);
        suspectAnswerTextArea.setLineWrap(true);
        suspectAnswerTextArea.setRows(5);
        suspectAnswerTextArea.setWrapStyleWord(true);
        suspectAnswerTextArea.setOpaque(false);
        jScrollPane3.setViewportView(suspectAnswerTextArea);

        contentPanel.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 540, 910, -1));

        jScrollPane2.setBorder(null);
        jScrollPane2.setOpaque(false);

        detectiveChosenTextArea.setEditable(false);
        detectiveChosenTextArea.setColumns(20);
        detectiveChosenTextArea.setLineWrap(true);
        detectiveChosenTextArea.setRows(5);
        detectiveChosenTextArea.setWrapStyleWord(true);
        detectiveChosenTextArea.setOpaque(false);
        jScrollPane2.setViewportView(detectiveChosenTextArea);

        contentPanel.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 524, 870, 100));

        detectiveChosen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/interrogate/dialogue/detective_chosen.png"))); // NOI18N
        contentPanel.add(detectiveChosen, new org.netbeans.lib.awtextra.AbsoluteConstraints(101, 295, -1, -1));

        jScrollPane1.setBorder(null);
        jScrollPane1.setOpaque(false);

        option1TextArea.setEditable(false);
        option1TextArea.setColumns(20);
        option1TextArea.setLineWrap(true);
        option1TextArea.setRows(5);
        option1TextArea.setWrapStyleWord(true);
        option1TextArea.setOpaque(false);
        jScrollPane1.setViewportView(option1TextArea);

        contentPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 534, 510, 70));

        option1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/interrogate/dialogue/detective_option1.png"))); // NOI18N
        contentPanel.add(option1, new org.netbeans.lib.awtextra.AbsoluteConstraints(275, 520, -1, -1));

        backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ui/back_button.png"))); // NOI18N
        contentPanel.add(backButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 630, 60, 50));

        andyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/interrogate/suspects/andy.png"))); // NOI18N
        contentPanel.add(andyButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(686, 266, -1, 410));

        cathyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/interrogate/suspects/cathy.png"))); // NOI18N
        contentPanel.add(cathyButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 278, -1, 340));

        cardoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/interrogate/suspects/cardo.png"))); // NOI18N
        cardoButton.setPreferredSize(new java.awt.Dimension(237, 239));
        contentPanel.add(cardoButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 250, -1, 430));

        backgroundLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/gameplay/interrogate/interrogation/cardo_idle.png"))); // NOI18N
        contentPanel.add(backgroundLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1150, -1));

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
    
    
    /** A JLabel that supports a fading alpha value for animations. */
    private static class FadeLabel extends javax.swing.JLabel {
        private float alpha = 1f;

        public FadeLabel() { 
            setOpaque(false); 
        }

        public void setAlpha(float a) {
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
    
    /** Opens the dialogue window for the chosen suspect. */
    private void openDialogue(Suspect s) {
        // TODO: build InterrogateDialogue and swap this stub out.
        System.out.println("Open dialogue for: " + s.id + " (" + s.displayName + ")");
    }

    /** Returns to the CrimeScene. */
    private void closeInterrogate() {
        hideTooltip();
        if (TransitionOverlay.isPlaying()) return;

        AudioCache.play("/audio/sfx/gameplay/door_close.wav");

        CrimeScene cs = new CrimeScene();
        cs.setLocation(getLocation());
        cs.setVisible(true);
        Interrogate.this.dispose();
    }
    
    @Override
    public void dispose() {
        if (humClip      != null) { humClip.stop();      humClip      = null; }
        if (thrillerClip != null) { thrillerClip.stop(); thrillerClip = null; }
        if (voiceClip    != null) { voiceClip.stop();    voiceClip    = null; }
        super.dispose();
    }

    /** Loads an icon resource, or returns null if missing. */
    private ImageIcon loadIcon(String path) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            System.out.println("Missing icon: " + path);
            return null;
        }
        return new ImageIcon(url);
    }

    /** Returns a darkened copy of the given image (for the back-arrow hover state). */
    private static java.awt.Image darken(java.awt.Image src) {
        java.awt.image.ImageFilter filter = new java.awt.image.RGBImageFilter() {
            @Override public int filterRGB(int x, int y, int rgb) {
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
    * Shows a PNG-based tooltip above the given anchor label. The tooltip is
    * centred horizontally on the anchor and sits just above its top edge.
    *
    * @param anchor the label the tooltip points at
    * @param pngPath classpath path of the tooltip PNG
    */
   private void showTooltip(JLabel anchor, String pngPath) {
       hideTooltip();  // never stack two

       java.net.URL url = getClass().getResource(pngPath);
       if (url == null) {
           System.out.println("Tooltip PNG missing: " + pngPath);
           return;
       }
       final javax.swing.ImageIcon icon = new ImageIcon(url);

       final int TIP_OFFSET_Y = 6;   // gap between the triangle tip and the anchor

       int w = icon.getIconWidth();
       int h = icon.getIconHeight();

       int anchorCenterX = anchor.getX() + anchor.getWidth() / 2;
       int anchorTopY    = anchor.getY();

       int x = anchorCenterX - w / 2;
       int y = anchorTopY - h - TIP_OFFSET_Y;

       // Clamp inside the frame so the tooltip never gets clipped.
       if (x < 4) x = 4;
       if (x + w > contentPanel.getWidth() - 4) {
           x = contentPanel.getWidth() - w - 4;
       }
       if (y < 4) y = 4;

       final JLabel tip = new JLabel(icon);
       tip.setOpaque(false);
       tip.setBounds(x, y, w, h);

       getRootPane().getLayeredPane().add(
           tip, javax.swing.JLayeredPane.POPUP_LAYER);
       getRootPane().getLayeredPane().repaint();

       activeTooltip = tip;
   }

   /** Removes the tooltip if one is showing. */
   private void hideTooltip() {
       if (activeTooltip != null) {
           getRootPane().getLayeredPane().remove(activeTooltip);
           getRootPane().getLayeredPane().repaint();
           activeTooltip = null;
       }
   }
   
    /** The only place that decides what appears on screen. */
    private void showNode(Node n) {
        if (n == null) { returnToLounge(); return; }
        inDialogue = true;

        if (n.options != null) {                 // choice node
            hideBubbles();
            suspectNamePlate.setVisible(false);
            activeChoice = n;
            awaitingChoice = true;
            next = null;                         // the pick decides what's next
            showChoices(n);
        } else {                                 // spoken line
            showLine(n.isDetective, n.text, n.audioPath);
            next = n.next;
        }
    }

    /** Click handler for "click anywhere". */
    private void advanceDialogue() {
        if (currentSuspect == -1 || !inDialogue || awaitingChoice) return;

        // Still typing: finish the line instantly instead of advancing
        if (typewriterTimer != null && typewriterTimer.isRunning()) {
            typewriterTimer.stop();
            typingArea.setText(typingFull);
            if (voiceClip != null) { voiceClip.stop(); voiceClip = null; }
            
            // NEW: Show the hint if they skip the typing
            startHintPulse(); 
            
            return;
        }
        
        AudioCache.play("/audio/sfx/ui/select.wav");
        showNode(next);
    }

    /** Player clicked option i of the choices on screen. */
    private void pickChoice(int i) {
        if (!awaitingChoice || activeChoice == null) return;
        AudioCache.play("/audio/sfx/ui/select.wav");

        Node choice = activeChoice;
        awaitingChoice = false;
        activeChoice = null;

        hideChoices();
        
        // Set the next node first
        next = choice.branches[i];           
        
        // Check if this is the "Start the interrogation" menu button
        boolean isMenuButton = choice.options[i].equals("Start the interrogation");

        // 3. Skip the bubble ONLY for the Start button
        if (isMenuButton) {
            showNode(next);
        } else {
            // For EVERYTHING else (including identical labels), type out the line!
            showLine(true, choice.spokenText[i], choice.spokenAudio[i]);
        }
    }
    
    private void startHintPulse() {
        hintLabel.setVisible(true);
        contentPanel.setComponentZOrder(hintLabel, 0); // Bring to front

        if (hintPulseTimer != null && hintPulseTimer.isRunning()) return; // already running

        final long start = System.currentTimeMillis();
        
        hintPulseTimer = new javax.swing.Timer(30, e -> {
            double time = (System.currentTimeMillis() - start) / 500.0;
            // Pulse between 20% opacity (0.2) and 100% opacity (1.0)
            hintAlpha = (float) (0.6 + 0.4 * Math.sin(time)); 
            hintLabel.repaint();
        });
        hintPulseTimer.start();
    }

    private void stopHintPulse() {
        if (hintPulseTimer != null) {
            hintPulseTimer.stop();
            hintPulseTimer = null;
        }
        hintAlpha = 0f;
        if (hintLabel != null) {
            hintLabel.setVisible(false);
        }
    }

    /** Shows 1, 2 or 3 option boxes depending on how many options the node has. */
    private void showChoices(Node n) {
        int g = n.options.length - 1;
        boolean center = true;       // Center everything, including the 1-option box
        int m = (g == 0) ? 10 : 5;

        for (int i = 0; i < n.options.length; i++) {
            styleBox(optAreas[g][i], optPanes[g][i], new java.awt.Insets(m, m, m, m), center);
            optAreas[g][i].setText(n.options[i]);
            centerVertically(optAreas[g][i], optPanes[g][i]);   // no-op for the 1-option box

            optLabels[g][i].setVisible(true);
            optPanes[g][i].setVisible(true);
            optAreas[g][i].setVisible(true);
            contentPanel.setComponentZOrder(optLabels[g][i], 0);
            contentPanel.setComponentZOrder(optPanes[g][i], 0);
        }
        if (g == 0) {
            final int PAD_X = 20;
            final int PAD_Y = 14;

            // 1. Graphic dead centre on the screen
            java.awt.Dimension gfx = option1.getPreferredSize();
            int pw = contentPanel.getWidth()  > 0 ? contentPanel.getWidth()  : 1150;
            int ph = contentPanel.getHeight() > 0 ? contentPanel.getHeight() : 680;
            int gx = (pw - gfx.width)  / 2;
            int gy = (ph - gfx.height) / 2;
            option1.setBounds(gx, gy, gfx.width, gfx.height);

            // 2. Scroll pane inset equally inside the graphic, then lay it out so the viewport has its size
            jScrollPane1.setBounds(gx + PAD_X, gy + PAD_Y, gfx.width - 2 * PAD_X, gfx.height - 2 * PAD_Y);
            jScrollPane1.doLayout();

            // 3. Horizontal centering: a JTextArea can't align text, so move the LEFT MARGIN instead.
            //    Measure the node's own string (n.options[0]), not option1TextArea.getText().
            java.awt.FontMetrics fm = option1TextArea.getFontMetrics(option1TextArea.getFont());
            int textW = fm.stringWidth(n.options[0]);
            int vpW   = jScrollPane1.getViewport().getWidth();

            java.awt.Insets all = option1TextArea.getInsets();
            java.awt.Insets cur = option1TextArea.getMargin();
            int borderL = all.left - cur.left;          // the border's share of the inset (Nimbus adds some)

            int left = Math.max(m, (vpW - textW) / 2 - borderL);   // never smaller than the normal margin

            // 4. Store it as the base margin. centerVertically() builds on BASE_MARGIN,
            //    so the vertical padding gets added without losing the new left margin.
            option1TextArea.putClientProperty(BASE_MARGIN, new java.awt.Insets(m, left, m, m));
            centerVertically(option1TextArea, jScrollPane1);
        }
    }

        private void hideBubbles() {
        detectiveChosen.setVisible(false);
        jScrollPane2.setVisible(false);
        suspectAnswer.setVisible(false);
        jScrollPane3.setVisible(false);
        suspectNamePlate.setVisible(false);
        suspectNamePlate.setText("");  
        stopHintPulse();
    }

    // Name plate position relative to the bubble's top-left corner.
    private static final int NAME_DX = 59;    // same x offset as your old plate (160 - 101)
    private static final int NAME_DY = -20;   // negative = sticks out above the bubble

    /** Shows one speech bubble (detective or suspect) and types the text. */
    private void showLine(boolean isDetective, String text, String audioPath) {
        javax.swing.JLabel box          = isDetective ? detectiveChosen : suspectAnswer;
        javax.swing.JScrollPane pane    = isDetective ? jScrollPane2 : jScrollPane3;
        javax.swing.JTextArea area      = isDetective ? detectiveChosenTextArea : suspectAnswerTextArea;

        hideBubbles();   // hides the plate: detective lines never show it

        styleBox(area, pane, new java.awt.Insets(10, 10, 10, 10));

        box.setLocation(101, 295);
        pane.setLocation(isDetective ? 136 : 120, 305);

        box.setVisible(true);
        pane.setVisible(true);
        area.setVisible(true);

        // Layering, back to front: bubble image -> text -> name plate.
        // The plate goes LAST because setComponentZOrder(x, 0) puts x in front.
        contentPanel.setComponentZOrder(box, 0);
        contentPanel.setComponentZOrder(pane, 0);

        if (!isDetective && currentSuspect >= 0) {
            suspectNamePlate.setText(suspects[currentSuspect].displayName);
            suspectNamePlate.setLocation(box.getX() + NAME_DX, box.getY() + NAME_DY);
            suspectNamePlate.setVisible(true);
            contentPanel.setComponentZOrder(suspectNamePlate, 0);
        }

        typeText(text, area, audioPath);
    }

    private void hideChoices() {
        for (int g = 0; g < optLabels.length; g++) {
            for (int i = 0; i < optLabels[g].length; i++) {
                optLabels[g][i].setVisible(false);
                optPanes[g][i].setVisible(false);
            }
        }
    }

    /** Hides every dialogue element and resets the dialogue state. */
    private void hideDialogueUI() {
        if (typewriterTimer != null) typewriterTimer.stop();
        if (voiceClip != null) { voiceClip.stop(); voiceClip = null; }
        next = null;
        activeChoice = null;
        awaitingChoice = false;
        inDialogue = false;
        hideBubbles();
        hideChoices();
    }

    private static final String BASE_MARGIN = "baseMargin";

    /** Existing callers (dialogue bubbles) keep working and stay top-aligned. */
    private void styleBox(javax.swing.JTextArea area, javax.swing.JScrollPane pane, java.awt.Insets margin) {
        styleBox(area, pane, margin, false);
    }

    private void styleBox(final javax.swing.JTextArea area, final javax.swing.JScrollPane pane,
                          java.awt.Insets margin, boolean centerVertically) {
        java.awt.Color clear = new java.awt.Color(0, 0, 0, 0);

        area.setFont(getCustomFont(12f));
        area.setForeground(java.awt.Color.WHITE);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setBackground(clear);
        area.setMargin(margin);

        pane.setOpaque(false);
        pane.setBackground(clear);
        pane.setBorder(null);
        pane.setViewportBorder(null);
        pane.getViewport().setOpaque(false);
        pane.getViewport().setBackground(clear);
        pane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        pane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        area.putClientProperty(BASE_MARGIN, centerVertically ? margin : null);

        if (centerVertically) {
            area.setRows(0);
            area.setColumns(0);

            if (pane.getClientProperty("centerListener") == null) {
                pane.putClientProperty("centerListener", Boolean.TRUE);
                pane.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
                    @Override public void componentResized(java.awt.event.ComponentEvent e) {
                        centerVertically(area, pane);
                    }
                });
            }
        }
    }

    /** Pads the top and bottom margins equally so the wrapped text sits in the middle of the box. */
    private void centerVertically(javax.swing.JTextArea area, javax.swing.JScrollPane pane) {
        java.awt.Insets base = (java.awt.Insets) area.getClientProperty(BASE_MARGIN);
        if (base == null) return;

        int w = pane.getViewport().getWidth();
        int h = pane.getViewport().getHeight();
        if (w <= 0 || h <= 0) return;

        area.setMargin(base);
        area.setSize(w, h);
        int free = h - area.getPreferredSize().height;
        int pad = Math.max(0, free / 2);

        area.setMargin(new java.awt.Insets(base.top + pad, base.left, base.bottom + pad, base.right));
    }

    /** One hover + click listener per option box, generated from the grouped arrays. */
    private void wireChoices() {
        final java.awt.Cursor hand = new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR);

        for (int g = 0; g < optPanes.length; g++) {
            for (int i = 0; i < optPanes[g].length; i++) {
                final javax.swing.JScrollPane pane = optPanes[g][i];
                final javax.swing.JTextArea area   = optAreas[g][i];
                final int choiceIndex = i;
                if (g == 0) {
                    centerVertically(area, pane);
                }

                java.awt.event.MouseAdapter l = new java.awt.event.MouseAdapter() {
                    private boolean hovering = false;

                    @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                        if (hovering) return;
                        hovering = true;
                        AudioCache.play("/audio/sfx/ui/hover.wav");
                        area.setForeground(new java.awt.Color(150, 150, 150));
                    }
                    @Override public void mouseExited(java.awt.event.MouseEvent e) {
                        java.awt.Point p = javax.swing.SwingUtilities.convertPoint(
                                e.getComponent(), e.getPoint(), pane);
                        if (pane.contains(p)) return;
                        hovering = false;
                        area.setForeground(java.awt.Color.WHITE);
                    }
                    @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                        hovering = false;   
                        pickChoice(choiceIndex);
                    }
                };

                pane.addMouseListener(l);
                pane.getViewport().addMouseListener(l);
                area.addMouseListener(l);
                pane.setCursor(hand);
                pane.getViewport().setCursor(hand);
                area.setCursor(hand);
            }
        }
    }
    
    /** Transitions from the lounge into the interrogation room. */
    private void startInterrogation(int suspectIndex) {
        hideTooltip();
        currentSuspect = suspectIndex;

        // 1. Hide the lounge buttons
        cardoButton.setVisible(false);
        cathyButton.setVisible(false);
        andyButton.setVisible(false);

        // 2. Get the correct icon
        javax.swing.ImageIcon idleIcon = null;
        if (suspectIndex == 0) idleIcon = cardoIdle;
        else if (suspectIndex == 1) idleIcon = cathyIdle; 
        else if (suspectIndex == 2) idleIcon = andyIdle;

        // 3. Swap ambience: lounge hum → thriller ambient.
        if (humClip != null) { humClip.stop(); humClip = null; }
        thrillerClip = AudioCache.loop("/audio/sfx/ambience/thriller_ambient.wav");

        // 4. Build the tree based on who was clicked
        Node treeToBuild = null;
        if (suspectIndex == 0) treeToBuild = buildCardoTree();
        else if (suspectIndex == 1) treeToBuild = buildCathyTree();
        else if (suspectIndex == 2) treeToBuild = buildAndyTree();

        // 5. Always play the fade transition
        if (idleIcon != null) {
            fadeInBackground(idleIcon, treeToBuild);
        }

        // 5. Fix layering
        contentPanel.setComponentZOrder(backgroundLabel, contentPanel.getComponentCount() - 1);
        contentPanel.setComponentZOrder(backButton, 0);

        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /** Loads the custom pixel font at the given size. */
    private java.awt.Font getCustomFont(float size) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/fonts/press_start_2p.ttf");
            java.awt.Font base = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
            return base.deriveFont(size);
        } catch (Exception e) {
            System.out.println("Font not found! Using default.");
            return new java.awt.Font("Arial", java.awt.Font.PLAIN, (int) size);
        }
    }
    
    /** Creates a long fade-to-black effect, swaps the background image, then fades back in. */
    private void fadeInBackground(javax.swing.ImageIcon newBg, Node startNode) {
        FadeOverlay fadeOverlay = new FadeOverlay();
        
        contentPanel.add(fadeOverlay, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1150, 680));
        contentPanel.setComponentZOrder(fadeOverlay, 0); 
        contentPanel.revalidate();
        contentPanel.repaint();

        final int duration = 1200; 
        final long start = System.currentTimeMillis();
        final boolean[] imageSwapped = { false };

        javax.swing.Timer timer = new javax.swing.Timer(16, null);
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - start;
            float progress = Math.min(1f, elapsed / (float) duration);

            if (progress < 0.5f) {
                fadeOverlay.setAlpha(progress * 2f);
            } else {
                if (!imageSwapped[0]) {
                    backgroundLabel.setIcon(newBg);
                    imageSwapped[0] = true;
                }
                fadeOverlay.setAlpha((1f - progress) * 2f);
            }

            if (progress >= 1f) {
                contentPanel.remove(fadeOverlay);
                contentPanel.revalidate();
                contentPanel.repaint();
                ((javax.swing.Timer) e.getSource()).stop();
                
                if (startNode != null) {
                    showNode(startNode);
                }
            }
        });
        timer.start();    
    }
    
    /** Returns the screen to the lounge state. */
    private void returnToLounge() {
        currentSuspect = -1;
        backgroundLabel.setIcon(loungeBgIcon);

        hideDialogueUI();

        // Swap ambience back: thriller ambient → lounge hum.
        if (thrillerClip != null) { thrillerClip.stop(); thrillerClip = null; }
        if (humClip == null) {
            humClip = AudioCache.loop("/audio/sfx/ambience/indoor_hum.wav");
        }
        
        // Restore the lounge suspects
        cardoButton.setVisible(true);
        cathyButton.setVisible(true);
        andyButton.setVisible(true);
        
        // Reset their images back to the lounge portraits
        cardoButton.setIcon(loadIcon("/Images/gameplay/interrogate/suspects/cardo.png"));
        cathyButton.setIcon(loadIcon("/Images/gameplay/interrogate/suspects/cathy.png"));
        andyButton.setIcon(loadIcon("/Images/gameplay/interrogate/suspects/andy.png"));
        
        // Reset their original positions
        cardoButton.setLocation(260, 258);
        cathyButton.setLocation(480, 282);
        andyButton.setLocation(686, 266);
        
        // --- THE FIX: Bring the suspects back to the front ---
        contentPanel.setComponentZOrder(cardoButton, 0);
        contentPanel.setComponentZOrder(cathyButton, 0);
        contentPanel.setComponentZOrder(andyButton, 0);
        contentPanel.setComponentZOrder(backButton, 0);
        
        contentPanel.revalidate();
        contentPanel.repaint();
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
    
    /** Animates typing text letter by letter into a specific text area. */
    private void typeText(String fullText, javax.swing.JTextArea targetArea, String audioPath) {
        if (typewriterTimer != null) typewriterTimer.stop();
        if (voiceClip != null)       { voiceClip.stop(); voiceClip = null; }

        typingFull = fullText;
        typingArea = targetArea;
        targetArea.setText("");

        // Load + start the voice clip if one exists.
        long audioDurationMs = 0;
        if (audioPath != null) {
            voiceClip = AudioCache.get(audioPath);
            if (voiceClip != null) {
                voiceClip.stop();
                voiceClip.setFramePosition(0);
                audioDurationMs = voiceClip.getMicrosecondLength() / 1000;
                voiceClip.start();
            }
        }

        // Tick rate: match voice length if audio exists, else 30ms/char.
        final int tickMs;
        final double charsPerSecond;
        if (voiceClip != null && audioDurationMs > 0 && fullText.length() > 0) {
            tickMs = 25;
            charsPerSecond = (fullText.length() / (audioDurationMs / 1000.0)) * 1.15;
        } else {
            tickMs = 10;
            charsPerSecond = 1000.0 / 10.0;
        }
        final double charsPerTick = charsPerSecond * (tickMs / 1000.0);
        final double[] accumulated = { 0.0 };

        typewriterTimer = new javax.swing.Timer(tickMs, e -> {
            accumulated[0] += charsPerTick;
            int n = Math.min(fullText.length(), (int) accumulated[0]);
            targetArea.setText(fullText.substring(0, n));

            if (voiceClip == null && n > 0 && n % 3 == 0) {
                AudioCache.play("/audio/sfx/ui/hover.wav");
            }

            if (n >= fullText.length()) {
                typewriterTimer.stop();
                
                // NEW: Show the hint when typing is done
                startHintPulse();
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
        java.awt.EventQueue.invokeLater(() -> new Interrogate().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel andyButton;
    private javax.swing.JLabel backButton;
    private javax.swing.JLabel backgroundLabel;
    private javax.swing.JLabel cardoButton;
    private javax.swing.JLabel cathyButton;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JLabel detectiveChosen;
    private javax.swing.JTextArea detectiveChosenTextArea;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JLabel option1;
    private javax.swing.JTextArea option1TextArea;
    private javax.swing.JLabel option2left;
    private javax.swing.JTextArea option2leftTextArea;
    private javax.swing.JLabel option2right;
    private javax.swing.JTextArea option2rightTextArea;
    private javax.swing.JLabel option3left;
    private javax.swing.JTextArea option3leftTextArea;
    private javax.swing.JLabel option3middle;
    private javax.swing.JTextArea option3middleTextArea;
    private javax.swing.JLabel option3right;
    private javax.swing.JTextArea option3rightTextArea;
    private javax.swing.JLabel suspectAnswer;
    private javax.swing.JTextArea suspectAnswerTextArea;
    private javax.swing.JLabel suspectNamePlate;
    // End of variables declaration//GEN-END:variables
}
