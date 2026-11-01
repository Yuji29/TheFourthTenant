# The Fourth Tenant

**Course:** COCS103
**Group Members:** Yuji Pelegrino, Donita Mariz Aquino, Angelo Bendicio, Janella Claire Reosa, Azelea Jean Flores

---

## 1. Project Summary

*The Fourth Tenant* is a 2D point-and-click detective mystery game built with **Java Swing** and the **Apache NetBeans GUI Builder**. A woman has been found dead on the rooftop of her boarding house. The player takes the role of the detective: search the crime scene, gather evidence, combine clues into deductions, interrogate the three housemates, report to the police, and finally decide who to arrest.

## 2. How to Run

1. Open the project folder in **Apache NetBeans** (JDK 21 recommended).
2. Make sure the **Absolute Layout** library (bundled with NetBeans) is attached to the project.
3. Right-click the project → **Clean and Build**, then **Run**. The main class is `theforthtenant.MainMenu`.
4. Speakers or headphones are recommended; the game uses voice acting and sound effects.

**Controls:** mouse to interact with everything. `ESC` opens the pause menu, `Up`/`Down`/`Enter` navigate it.

## 3. Game Flow

| Step | Objective | Screen |
|---|---|---|
| 1 | Watch the backstory | `Backstory` |
| 2 | Search the rooms and collect all 14 pieces of evidence | `CrimeScene` |
| 3 | Unlock Abby's phone (4-digit PIN) | `Inventory` |
| 4 | Combine evidence into 8 deductions | `Inventory`, `Notes` |
| 5 | Interrogate Cardo, Cathy and Andy | `Interrogate` |
| 6 | Report to Officer Morales | `PoliceReport` |
| 7 | Confront the prime suspect | `Interrogate` |
| 8 | Make the arrest | `Suspects` |

The **Case File** (`CaseFile`) always shows the current objective, and its **Go** button takes the player to the right screen.

## 4. Features Implemented

- **11 screens**, all laid out in the NetBeans Form Designer: `MainMenu`, `HowToPlay`, `Credits`, `Backstory`, `CrimeScene`, `CaseFile`, `Inventory`, `Notes`, `Interrogate`, `PoliceReport`, `Suspects`.
- **Crime scene exploration:** multiple rooms, pixel-accurate hit-testing, 14 collectable items with a pickup animation and sound.
- **Inventory and deduction system:** 20-slot inventory, item inspection, click-to-combine recipes (`ItemDatabase`) producing 8 deductions, and a "New Discovery" notification.
- **Phone puzzle:** PIN keypad, lock screen, message threads that reveal extra clues.
- **Interrogation system:** branching dialogue trees (`Interrogate.Node`), voice acting, typewriter text, suspect reactions, and a health ("Focus") bar that drops on wrong choices, leading to a game-over state.
- **Detective's Notes** that persist across screens, and a **Suspects board** that fills in as topics are interrogated.
- **Multiple endings:** wrong arrests lead to different game-over screens.
- **Global systems:** audio caching with independent SFX and music volume (saved between sessions), pause menu, options menu, animated screen transitions, and a session-wide game state.

## 5. Project Structure

```
TheFourthTenant/
├── README.md
├── nbproject/                  NetBeans project files
└── src/
    ├── Images/                 UI art, backgrounds, items, portraits
    ├── audio/                  music, SFX, voice lines
    ├── fonts/                  Press Start 2P pixel font
    └── theforthtenant/
        ├── GameScreen.java     abstract base class for every screen
        ├── *.java + *.form     the 11 screens (Form Designer pairs)
        ├── GameState.java      session-wide game progress
        ├── ItemDatabase.java   item metadata and combine recipes
        ├── AudioCache.java     clip cache and volume channels
        ├── SfxManager.java     saves volume settings
        ├── PauseMenu / OptionsMenu / TransitionOverlay /
        │   PickupAnimation / HealthBar / FadeOverlay   reusable UI components
```

## 6. GUI Design and NetBeans Compliance

- All 11 screens were built visually in the NetBeans Form Designer; every screen has its `.form` file and a generated `initComponents()` block. In total the designer holds about **240 components**, all visible in the Navigator component tree.
- Only **2** components are created by hand in code (one each in `CrimeScene` and `Interrogate`).
- A small set of reusable **overlay components** are custom-painted with `paintComponent` and attached on top of the designer-built frames: `PauseMenu`, `OptionsMenu`, `TransitionOverlay`, `PickupAnimation`, `HealthBar`, and the shared `FadeOverlay`. These are visual effects, not screen layouts, and together stay well under the 25% code-based UI limit.

## 7. Implementation of the 4 OOP Pillars

### 7.1 Encapsulation
- Every screen keeps its UI components and state in `private` fields.
- `GameState` keeps all progress (collected items, discovered deductions, interrogation topics, health, notes) in `private static` collections and exposes it only through methods such as `markCollected()`, `isCollected()`, `setHealth()` (which clamps the value to 0–100) and `markInterrogated()`. Callers receive **copies** of lists (`getCollected()`), never the internal list.
- `ItemDatabase` stores items in a private map; `ItemDatabase.Item` has `final` fields and a package-private constructor.
- `AudioCache` hides its `Clip` objects inside a private `Entry` class and exposes only `play()`, `loop()`, `get()`, `prepare()`, volume setters and `closeAll()`. `SfxManager` hides how volume is persisted.

### 7.2 Inheritance
- **`GameScreen extends JFrame`**, and all 11 screens extend `GameScreen`:
  `MainMenu`, `HowToPlay`, `Credits`, `Backstory`, `CrimeScene`, `CaseFile`, `Inventory`, `Notes`, `Interrogate`, `PoliceReport`, `Suspects`.
  `GameScreen` holds the code every screen needs (window icon, title, non-resizable window), so it is written once.
- Custom components extend Swing classes to reuse painting and layout behavior: `HealthBar`, `FadeOverlay`, `PauseMenu.PauseOverlay`, `OptionsMenu.OptionsOverlay`, `TransitionOverlay.OverlayPanel` and `PickupAnimation.ParticlePanel` all extend `JComponent`; `Backstory.FadeLabel` extends `JLabel`.

### 7.3 Polymorphism
- **Method overriding:**
  - Every screen overrides the abstract `GameScreen.setupScreen()` with its own behavior; the same call produces different results on each screen.
  - `paintComponent(Graphics)` is overridden in `HealthBar`, `FadeOverlay`, and the overlay classes to draw custom graphics.
  - `dispose()` is overridden in `CrimeScene`, `Interrogate`, `Backstory` and `PoliceReport` to stop and release audio before the window closes.
  - Mouse, key and action listeners are overridden throughout (`mouseClicked`, `mouseEntered`, `actionPerformed`, ...).
- **Method overloading:**
  - `AudioCache.get()`, `play()`, `loop()` and `prepare()` each have a version that takes only a path and one that also takes an audio `Channel`.
  - `Interrogate.Node.on(...)` has three overloads that build dialogue branches with different amounts of information (choice + next node; plus spoken text; plus voice-over file).
  - `PauseMenu.attachTo(...)` and `TransitionOverlay.play(...)` are overloaded to provide default positions.

### 7.4 Abstraction
- **Abstract class:** `GameScreen` declares `protected abstract void setupScreen();`. It defines *what* every screen must do (set up its own behavior) and leaves *how* to each subclass, so the rest of the game can treat all screens uniformly.
- **Interfaces:** `PauseMenu.Callbacks` and `OptionsMenu.Callbacks` let the reusable menus notify any screen of events (`onPause`, `onResume`, `onOptions`, `onMainMenu`, `onSoundChanged`, `onClose`) without knowing which screen they belong to. Each screen supplies its own behavior through an anonymous implementation, so the menu logic is fully decoupled from the screens.
- **Utility abstraction:** `AudioCache`, `GameState` and `ItemDatabase` hide their implementation behind small public APIs so game logic never touches `javax.sound` or raw collections directly.

## 8. Technologies and Assets

- Java (JDK 21), Java Swing / AWT, Java Sound API (`javax.sound.sampled`), `java.util.prefs` for saved settings
- Apache NetBeans GUI Builder with the AbsoluteLayout library
- Font: *Press Start 2P* (bundled in `src/fonts/`)
- All art, voice lines and sound effects are included in `src/Images/` and `src/audio/`

## 9. Known Notes

- Progress is kept per session only. Closing the game resets the story (volume settings are remembered).
- Audio assets are loaded from the classpath; if a file is missing the game prints a warning and continues without that sound.