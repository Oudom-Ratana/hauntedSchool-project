# Khmer Spirit: The Haunted School

A JavaFX-based educational adventure game where players explore a haunted school, solve room-based puzzles, and collect keys to progress. Features intelligent ghost AI, synthesized audio, and a teacher admin panel for question management.

## 🎮 Features

### Core Gameplay
- **Room-Based Exploration**: Navigate through 5 interconnected school rooms with locked doors
- **Educational Puzzles**: Answer 5 randomized multiple-choice questions to unlock each room
- **Hearts System**: Start with 5 hearts; lose hearts when caught by ghosts; game over at 0 hearts
- **Item Inventory**: Collect and use 10+ unique items
- **Auto-Save**: Game saves every 10 seconds; continue feature available

### Ghost AI System
- **State Machine**: 6 intelligent states (Idle, Patrol, Search, Chase, Attack, Return)
- **Dynamic Spawning**: Ghosts appear when players answer incorrectly
- **Smooth Animations**: Float, attack, and disappear effects
- **Attack Mechanics**: Chase and damage player with cooldown invulnerability

### Audio & Animation
- **Synthesized Audio**: 9 sound effects (footsteps, ghost cries, door unlock, etc.)
- **Volume Control**: Separate sliders for master, ambience, SFX, and music
- **Player Animations**: Idle, Walk, Run states with directional sprites
- **Object Animations**: Door bobbing, lamp flickering, rain scrolling, torch flaming
- **Environmental Effects**: Particles, fog, rain, lightning, flashlight lighting

### Teacher Admin Panel
- **Question Editor**: Add/edit/delete questions without coding
- **File Management**: Save/load questions from JSON/text files
- **Answer Selection**: Choose correct answer per question
- **Room Assignment**: Assign questions to specific rooms

## 📋 System Requirements

- **Java**: JDK 21+
- **JavaFX**: 21.0.4+
- **Maven**: 3.6.0+
- **OS**: Windows, macOS, Linux

## 🚀 Building & Running

### Maven (Recommended)
```bash
mvn clean compile
mvn javafx:run
```

### IntelliJ IDEA
1. Open `pom.xml` as project
2. Run `Main.java` directly

## 🎮 Controls

| Key | Action |
|-----|--------|
| **WASD / Arrows** | Move |
| **E** | Interact / Pickup |
| **1-4** | Answer questions |
| **1-0** | Use inventory |

## 📁 Project Structure

```
src/main/java/com/khmerspirit/
├── animation/          # GhostAnimation, ObjectAnimation
├── audio/              # AudioManager (synthesized sounds)
├── config/             # Constants
├── core/               # Game, GameLoop, SceneManager
├── education/          # Question, RoomTask, EducationManager
├── entities/           # Ghost AI
├── inventory/          # Inventory system
├── items/              # Item models & registry
├── map/                # TileMap, Room, Door, Collision
├── player/             # Player, PlayerAnimation, Camera
├── save/               # Save/Load system
└── scene/              # UI scenes
```

## ✨ Architecture Highlights

- **State-Driven Design**: Ghost AI uses state machine pattern
- **Single Canvas Rendering**: Optimized draw order (background → tiles → entities → HUD)
- **Delta-Time Updates**: Frame-independent animation and movement
- **Lazy Loading**: Questions loaded on-demand per room
- **Audio Generation**: No external audio files; synthesized PCM

## 🐛 Known Issues & Future Work

- Pause menu not implemented
- Difficulty levels not yet available
- Mobile port requires different framework

## 📄 Documentation

- `README.md` - This file
- `ARCHITECTURE.md` - System design overview
- `UML.md` - Class diagrams
- Code comments provide detailed implementation notes

## 📝 License

Educational project - free to modify and distribute for learning purposes.

---

**Version**: 1.0.0 | **Status**: Production Ready | **Last Updated**: 2026-08-08
