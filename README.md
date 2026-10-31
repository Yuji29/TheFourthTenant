# Project Title: The Fourth Tenant
**Course:** [COCS103]
**Student Name / Group Members:** [Yuji Pelegrino, Donita MAriz Aquino, Angelo Bendicio, Janella Claire Reosa, Azelea Jean Flores]

## Project Summary
"The Fourth Tenant" is a 2D point-and-click detective mystery game built in Java Swing. The player must investigate a crime scene, collect evidence, combine items to form deductions, interrogate suspects, and ultimately arrest the killer. 

## Features Implemented
- **9 Unique Screens:** Built using NetBeans GUI Builder (`.form` files).
- **Dynamic Inventory:** 20-slot inventory with click-to-combine mechanics.
- **Interrogation System:** Branching dialogue trees with voice acting and typewriter text effects.
- **Crime Scene:** Pixel-perfect hit-testing for collecting evidence.
- **Global Systems:** Audio caching with SFX/Music volume control, and persistent game state.

## Implementation of the 4 OOP Pillars

**1. Encapsulation**
- All main frames encapsulate their UI components and state as `private` fields. 
- The `GameState` and `ItemDatabase` classes protect game data (health, collected items, discovered clues) by making the lists/maps private and only allowing access through public getter and setter methods (e.g., `setHealth()`, `markCollected()`).
- The `AudioCache` class encapsulates the `Clip` objects inside a private `Entry` class, only exposing playback methods.

**2. Inheritance**
- We created an abstract base class `GameScreen` that extends `JFrame`. All 9 screens (e.g., `MainMenu`, `CrimeScene`, `Inventory`, `Interrogate`) extend `GameScreen`, inheriting shared window setup logic (like centering the window and loading the icon).
- Custom visual components inherit from Java Swing classes. `HealthBar`, `SubtitleBox`, and `FadeOverlay` all extend `JComponent`.

**3. Polymorphism**
- **Method Overriding:** All 9 screens override the abstract `setupScreen()` method defined in `GameScreen`. The `paintComponent(Graphics g)` method is overridden in `HealthBar`, `SubtitleBox`, and `FadeOverlay` to create custom graphics. The `dispose()` method is overridden in `CrimeScene`, `Interrogate`, `Backstory`, and `PoliceReport` to clean up audio clips before closing.
- **Method Overloading:** The `AudioCache` class overloads `get()`, `play()`, and `loop()` to accept either just a path or a path and a specific audio channel. The `Node` class inside `Interrogate` overloads the `on()` method to handle different dialogue configurations.

**4. Abstraction**
- `GameScreen` is an abstract class that hides the repetitive window initialization from its child classes, forcing them to implement their own `setupScreen()` logic.
- We use interfaces to decouple UI logic from menu interactions. The `PauseMenu.Callbacks` and `OptionsMenu.Callbacks` interfaces are implemented via anonymous classes in the screens to handle menu actions dynamically without the menus needing to know which specific screen they are attached to.