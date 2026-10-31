# Project Title: The Fourth Tenant
**Course:** [Your Course Code]
**Student Name / Group Members:** [Your Name(s)]

## Project Summary
"The Fourth Tenant" is a 2D point-and-click detective mystery game built in Java Swing. The player must investigate a crime scene, collect evidence, combine items to form deductions, interrogate suspects, and ultimately arrest the killer. 

## Features Implemented
- **10 Unique Screens:** Built using NetBeans GUI Builder (`.form` files).
- **Dynamic Inventory:** 20-slot inventory with drag-and-drop (click) combination mechanics.
- **Interrogation System:** Branching dialogue trees with voice acting and typewriter text effects.
- **Crime Scene:** Pixel-perfect hit-testing for collecting evidence.
- **Global Systems:** Audio caching with SFX/Music volume control, and persistent game state.

## Implementation of the 4 OOP Pillars

**1. Encapsulation**
- All main frames (`CrimeScene`, `Inventory`, etc.) encapsulate their UI components and state as `private` fields. 
- The `GameState` class protects game data (health, collected items, discovered clues) by making the lists private and only allowing access through public getter and setter methods (e.g., `setHealth()`, `markCollected()`).

**2. Inheritance**
- All 10 main screens extend `javax.swing.JFrame` and inherit standard window behaviors.
- Custom visual components inherit from Java Swing classes. For example, `HealthBar` and `SubtitleBox` extend `JComponent`, and the `FadeOverlay` class extends `JComponent` to inherit painting behaviors.
- [INSERT: "The new `GameScreen` abstract class is inherited by all main screens to share window setup logic." - *We will add this next*]

**3. Polymorphism**
- **Method Overloading:** The `AudioCache` class overloads the `get()`, `play()`, and `loop()` methods to accept either just a file path, or a file path and a specific audio channel. The `Node` class in `Interrogate` overloads the `on()` method to accept different dialogue configurations.
- **Method Overriding:** The `paintComponent(Graphics g)` method is overridden in `HealthBar`, `SubtitleBox`, `FadeOverlay`, and the custom `SpacedTextArea` (inside `Interrogate`) to create custom graphics. The `dispose()` method is overridden in `CrimeScene` and `Interrogate` to clean up audio clips before closing the window.

**4. Abstraction**
- We use interfaces to decouple logic from the UI. The `PauseMenu.Callbacks` interface and `OptionsMenu.Callbacks` interface are implemented by all frames to handle pause and volume adjustments dynamically without the menu needing to know which screen it is on.
- [INSERT: "We use the `GameScreen` abstract class to abstract away the repetitive UI setup..." - *We will add this next*]