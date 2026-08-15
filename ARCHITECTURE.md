# Khmer Spirit: Architecture & Design

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                         Main (JavaFX)                        │
├─────────────────────────────────────────────────────────────┤
│                      SceneManager                            │
│  (MainMenu → Character → Game → Teacher)                     │
├──────────────────────┬──────────────────────────────────────┤
│   GameScene          │         Scenes                        │
│  ┌────────────────┐  │  - MainMenuScene                     │
│  │   GameLoop     │  │  - CharacterScene                    │
│  │  (60 FPS)      │  │  - GameScene                         │
│  └────┬───────────┘  │  - TeacherAdminScene                 │
│       │              │                                       │
│   ┌───▼─────────┐   │  ┌────────────────────────────────┐   │
│   │  Update()   │───┼──┤ Player Controller (Input)      │   │
│   ├─────────────┤   │  └────────────────────────────────┘   │
│   │ Render()    │   │                                       │
│   └─────────────┘   │  ┌────────────────────────────────┐   │
└──────────────────────┤ Audio Manager (Looping/OneShot)  │   │
                       │  ├─ Rain, Wind, Ambience         │   │
                       │  ├─ Menu Music                   │   │
                       │  └─ SFX (footsteps, door, etc)   │   │
                       │  └────────────────────────────────┘   │
                       └──────────────────────────────────────┘
```

## Game Loop

```
┌──────────────────────────────────────────┐
│         GameLoop (60 FPS)                │
├──────────────────────────────────────────┤
│                                          │
│  1. Handle Input                         │
│     └─ PlayerController.process()        │
│                                          │
│  2. Update Game State                    │
│     ├─ Player.update()                   │
│     ├─ EducationManager.update()         │
│     │  ├─ Ghost.update()                 │
│     │  ├─ Question Logic                 │
│     │  └─ Room Completion                │
│     ├─ CollisionDetection                │
│     └─ Audio Triggers                    │
│                                          │
│  3. Update Effects                       │
│     ├─ Rain Animation                    │
│     ├─ Particle System                   │
│     ├─ Fog Movement                      │
│     └─ Lightning Effect                  │
│                                          │
│  4. Render Frame                         │
│     ├─ Clear Canvas                      │
│     ├─ Background                        │
│     ├─ TileMap                           │
│     ├─ Objects & Particles               │
│     ├─ Entities (Player, Ghosts)         │
│     ├─ Effects (Lighting, Overlays)      │
│     └─ HUD & Notifications               │
│                                          │
│  5. Auto-Save (every 10s)                │
│                                          │
└──────────────────────────────────────────┘
```

## Entity System

### Player
- **Class**: `Player.java`
- **State**: Position, hearts (0-5), inventory
- **Animation**: `PlayerAnimation.java` (Idle, Walk, Run)
- **Update**: Input-driven movement with collision
- **Rendering**: Sprite-based with camera offset

### Ghost
- **Class**: `Ghost.java`
- **States**: Idle → Patrol → Search/Chase → Attack → Return → Disappear
- **Animation**: `GhostAnimation.java` (Float, Attack, Disappear)
- **AI Logic**:
  - Detection radius: 160px
  - Detection triggers state transitions
  - Attack cooldown: 1.2s between hits
  - Automatically returns to spawn when search fails
- **Rendering**: Circle with scale/opacity effects

### ItemPickup
- **Class**: `ItemPickup.java`
- **Properties**: Item, position, collision radius
- **Interaction**: Collected by player proximity and E key
- **Persistence**: Respawned on room completion

## Education System

```
┌────────────────────────────────────────┐
│     EducationManager                   │
├────────────────────────────────────────┤
│                                        │
│  Task State per Room:                  │
│  ┌──────────────────────────────────┐  │
│  │ RoomTask                         │  │
│  ├─────────────────────────────────┤  │
│  │ - Questions: List<Question>      │  │
│  │ - CorrectCount: 0-5              │  │
│  │ - CurrentQuestion: Question      │  │
│  │ - Completed: boolean             │  │
│  └──────────────────────────────────┘  │
│                                        │
│  Question Loading:                     │
│  └─ QuestionLoader.loadFromFile()      │
│     ├─ JSON parsing                   │
│     ├─ Text file parsing              │
│     └─ Random shuffle                 │
│                                        │
│  Event Handlers:                       │
│  ├─ onPlayerRoomChanged()             │
│  ├─ tryInteractAt()                   │
│  ├─ submitAnswer()                    │
│  │  ├─ Correct → progress             │
│  │  └─ Wrong → spawnGhost()           │
│  └─ onRoomCompleted()                 │
│     ├─ Drop key                       │
│     ├─ Unlock door                    │
│     └─ Audio feedback                 │
│                                        │
└────────────────────────────────────────┘
```

## Rendering Pipeline

```
Frame Start
    ↓
1. Clear Canvas (black background)
    ↓
2. Draw Rain (batch render 110 drops)
    ↓
3. Draw TileMap (visible tiles only)
    ├─ Floor tiles
    ├─ Wall tiles
    ├─ Door tiles (locked/unlocked)
    └─ Collision visualization (debug)
    ↓
4. Draw Fog Patches (6 patches, moving)
    ↓
5. Draw Flashlight Lighting (radial gradient)
    ↓
6. Draw ItemPickups (world items)
    ↓
7. Draw Player (sprite with animation)
    ↓
8. Draw Particles (40 active, fading)
    ↓
9. Draw Ghosts (scale/opacity animation)
    ↓
10. Draw Lightning Flash (random opacity)
    ↓
11. Draw HUD
    ├─ Hearts display
    ├─ Room name
    ├─ Inventory
    └─ Notifications
    ↓
12. Draw Question Overlay (if active)
    ├─ Question text
    ├─ 4 options
    └─ Input prompt
    ↓
Frame End
```

## Save Data Model

```json
{
  "characterName": "character_name",
  "playerX": 2560.0,
  "playerY": 2560.0,
  "hearts": 5,
  "playTimeSeconds": 324.5,
  "currentRoom": "Classroom",
  "completedRooms": ["Entrance"],
  "currentTask": "Classroom",
  "inventory": {
    "key": 2,
    "flashlight": 1,
    "first_aid_kit": 1
  },
  "keys": ["key_entrance"]
}
```

## File I/O

```
Project Root/
├── saves/
│   └── game.properties          (Player save state)
│
├── questions/
│   ├── entrance.json            (Question set per room)
│   ├── classroom.json
│   └── ...
│
└── resources/
    ├── sprites/
    │   ├── character_*.png
    │   └── npc_*.png
    └── maps/
        └── school.dat
```

## Key Design Patterns

### 1. Singleton Pattern
- **SceneManager**: Global scene controller
- **AudioManager**: Single audio instance
- **AssetManager**: Cached resource loading

### 2. State Pattern
- **Ghost.State**: Enum with behavior logic
- **PlayerAnimation.State**: Movement animation states
- **GhostAnimation.State**: Visual animation states

### 3. Manager Pattern
- **EducationManager**: Orchestrates room tasks and ghost spawning
- **SaveManager**: Handles persistence layer
- **TileMap**: Manages map grid and room lookups

### 4. Factory Pattern
- **ItemRegistry**: Creates items by ID
- **QuestionLoader**: Loads questions from different formats

### 5. Observer Pattern
- **Input Events**: PlayerController → Player movement
- **Audio Triggers**: Game events → AudioManager.playSound()

## Optimization Strategies

### Rendering Optimization
1. **Single Canvas**: All drawing to one GraphicsContext
2. **Camera Culling**: Only render visible portion of map
3. **Batch Drawing**: Group similar objects (rain, particles)
4. **Image Smoothing Disabled**: Pixel-perfect rendering
5. **Conditional Rendering**: Skip off-screen entities

### Memory Optimization
1. **Lazy Loading**: Questions loaded per room on demand
2. **Object Pooling**: Potential for ghost/particle reuse
3. **Resource Caching**: AssetManager caches sprites
4. **Iterator Cleanup**: Remove dead particles/ghosts in-place

### Logic Optimization
1. **State Machines**: Avoid redundant calculations
2. **Distance Caching**: Calculate once per update
3. **Efficient Collision**: Check only nearby tiles
4. **Delta-Time Updates**: Frame-independent logic

## Thread Safety

- **Single-Threaded**: Game runs on JavaFX Application Thread
- **No Concurrent Modifications**: Updates and renders serially
- **Audio**: AudioManager handles thread-safe media playback

## Error Handling

```
Game
├─ NullPointerException Checks
│  ├─ Optional.ifPresent() for rooms
│  ├─ Null checks for sprites
│  └─ Safe defaults for missing data
│
├─ File I/O
│  ├─ Try-catch for Properties load/save
│  ├─ Graceful fallback for missing questions
│  └─ Empty inventory on new game
│
└─ Audio
   ├─ Silent fallback if audio unavailable
   ├─ Volume bounds checking (0.0-1.0)
   └─ Clip disposal on close
```

## Performance Targets

- **Frame Rate**: 60 FPS target (16.67ms per frame)
- **Memory**: <200MB steady state
- **Load Time**: <2s from launch to menu
- **Save Time**: <100ms auto-save operation

---

**Architecture Version**: 1.0 | **Last Updated**: 2026-08-08
