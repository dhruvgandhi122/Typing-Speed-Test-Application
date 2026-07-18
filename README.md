# TypeMaster

TypeMaster is a premium, dark-themed desktop typing speed test application built with **JavaFX** and **Gradle**. Inspired by modern web typing tools like Monkeytype, it provides a clean, beautiful, and fluid typing test experience.

---

## Key Features

- **Dynamic Visual Feedback**:
  - Correct characters are highlighted in **Green** (`#2ec27e`).
  - Mistakes are highlighted in **Red** (`#e01b24`) with an underline.
  - The next character to type is highlighted in **Electric Blue** (`#3584e4`) with a cursor underline.
  - Untyped characters remain in a sleek, muted gray.
- **Multiple Screen States**:
  - **Home Screen**: Select difficulty levels (Easy, Medium, Hard) and get ready.
  - **Typing Playground**: Dynamic typing area with a 60-second test timer that starts automatically on the **first keystroke**, and live WPM and Accuracy calculation. It also automatically finishes the test if the paragraph is fully typed before the timer ends.
  - **Results Screen**: Breakdown of WPM, Accuracy, and raw keystrokes (Correct vs. Errors) along with a speed evaluation rating.
- **Difficulty Modes**:
  - **Easy**: Short, common words in lowercase with no punctuation.
  - **Medium**: Standard sentences with proper capitalization and basic punctuation.
  - **Hard**: Technical terms, numbers, symbols, and programming-related snippets (curly braces, brackets, etc.).

---

## Prerequisites

To run this application, you need to have:
- **Java Development Kit (JDK) 21** or higher installed.
- Git (optional, for cloning).

---

## Quick Start

### 1. Clone the repository
```bash
git clone <your-repository-url>
cd TypeMaster
```

### 2. Build and Run the App
TypeMaster uses the Gradle wrapper, which means you don't need a local Gradle installation to run it:

#### On Windows (PowerShell or Command Prompt):
```powershell
# Build and verify compilation
.\gradlew build

# Run the desktop application
.\gradlew run
```

#### On Linux / macOS:
```bash
# Set execution permission for the Gradle wrapper (if not already set)
chmod +x gradlew

# Build and verify compilation
./gradlew build

# Run the desktop application
./gradlew run
```

---

## Project Structure

```text
TypeMaster/
├── build.gradle                # Gradle configuration (plugins, dependencies)
├── settings.gradle             # Project settings name definition
├── gradlew & gradlew.bat       # Gradle wrapper execution scripts
├── gradle/                     # Gradle wrapper folder
└── src/
    └── main/
        ├── java/
        │    └── com/typemaster/
        │         └── Main.java # Main application code & controller logic
        └── resources/
             ├── css/
             │    └── style.css # Custom dark theme styles
             ├── fxml/
             │    └── main.fxml # Interactive view structures
             └── paragraphs/
                  ├── easy.txt  # Easy difficulty database
                  ├── medium.txt# Medium difficulty database
                  └── hard.txt  # Hard difficulty database
```

---

## License

This project is open-source and free to modify or distribute. Happy typing!
