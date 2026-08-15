# Khmer Spirit: UML Class Diagram

## Core Game Classes

```
┌─────────────────────────────────────────┐
│              Main                       │
├─────────────────────────────────────────┤
│ - primaryStage: Stage                   │
├─────────────────────────────────────────┤
│ + start(Stage): void                    │
│ + main(String[]): void                  │
└─────────────────────────────────────────┘
           │
           │ extends
           ▼
┌─────────────────────────────────────────┐
│          Application (JavaFX)           │
└─────────────────────────────────────────┘
```

## Scene Management

```
┌────────────────────────────────────────┐
│        SceneManager (Singleton)        │
├────────────────────────────────────────┤
│ - stage: Stage {static}                │
├────────────────────────────────────────┤
│ + initialize(Stage): void              │
│ + showMainMenu(): void                 │
│ + showCharacterSelection(): void       │
│ + showGame(String): void               │
│ + showGameWithSave(SaveData): void     │
│ + showTeacherAdmin(): void             │
│ + exitGame(): void                     │
│ - setScene(Scene): void                │
└────────────────────────────────────────┘
       ▲              ▲            ▲
       │              │            │
   creates         manages      transitions
       │              │            │
       ├──────────────┼────────────┤
       │              │            │
       ▼              ▼            ▼
┌─────────────────┐ ┌──────────────────┐ ┌──────────────────┐
│ MainMenuScene   │ │  CharacterScene  │ │   GameScene      │
├─────────────────┤ ├──────────────────┤ ├──────────────────┤
│ - canvas        │ │ - selectedChar   │ │ - game: Game     │
│ - rain: List    │ │ - button[5]      │ │ - hud: HBox      │
│ - buttons[6]    │ │                  │ │ - canvas: Canvas │
├─────────────────┤ ├──────────────────┤ ├──────────────────┤
│ + createScene() │ │ + createScene()  │ │ + createScene()  │
│ + showAudio()   │ │                  │ │ + startHudUpdate │
└─────────────────┘ └──────────────────┘ └──────────────────┘
       │                      │                    │
       │ contains             │ uses               │ contains
       │                      │                    │
       ▼                      ▼                    ▼
┌─────────────────┐ ┌──────────────────┐ ┌──────────────────┐
│ Audio Sliders   │ │  CharacterSelect │ │      Game        │
│ [Master, SFX]   │ │     Button       │ │   (See Below)    │
└─────────────────┘ └──────────────────┘ └──────────────────┘

┌──────────────────────────────────────┐
│   TeacherAdminScene                  │
├──────────────────────────────────────┤
│ - questionList: ListView             │
│ - roomSelector: ComboBox             │
│ - questionField: TextArea            │
│ - buttons: [Add, Edit, Delete, Save] │
├──────────────────────────────────────┤
│ + createScene(): Scene               │
│ + addQuestion(): void                │
│ + editQuestion(): void               │
│ + deleteQuestion(): void             │
│ + saveToFile(): void                 │
└──────────────────────────────────────┘
```

## Game Core

```
┌────────────────────────────────────────────────────┐
│                    Game                           │
├────────────────────────────────────────────────────┤
│ - canvas: Canvas                                   │
│ - graphics: GraphicsContext                        │
│ - player: Player                                   │
│ - tileMap: TileMap                                │
│ - collisionMap: CollisionMap                       │
│ - camera: Camera                                   │
│ - gameLoop: GameLoop                              │
│ - inventory: Inventory                            │
│ - educationManager: EducationManager               │
│ - itemPickups: List<ItemPickup>                    │
│ - rainAnimation: ObjectAnimation                   │
│ - torchAnimation: ObjectAnimation                  │
│ - rainDrops: List<RainDrop>                        │
│ - particles: List<Particle>                        │
│ - fogPatches: List<FogPatch>                       │
├────────────────────────────────────────────────────┤
│ + update(double): void                             │
│ + render(): void                                   │
│ + start(): void                                    │
│ + stop(): void                                     │
│ + playSound(String): void                          │
│ + showNotification(String): void                   │
│ + getPlayer(): Player                              │
│ + getInventory(): Inventory                        │
│ + getPlayerController(): PlayerController          │
│ + getCurrentRoomId(): String                       │
│ + getEducationActiveRoomId(): String               │
│ + spawnItemPickup(ItemPickup): void                │
│ - updateInventoryInput(): void                     │
│ - updateEffects(double): void                      │
│ - renderRain(): void                               │
│ - renderFog(): void                                │
│ - renderFlashlightLighting(): void                 │
│ - renderParticles(): void                          │
│ - renderNotification(): void                       │
└────────────────────────────────────────────────────┘
       ▲
       │ uses
       │
       ├────────┬────────┬────────┬──────────┬─────────────┐
       │         │        │        │          │             │
       ▼         ▼        ▼        ▼          ▼             ▼
    Player   TileMap  Camera  GameLoop  EducationMgr  AssetManager
```

## Player & Animation

```
┌──────────────────────────────┐
│       Player                 │
├──────────────────────────────┤
│ - x, y: double               │
│ - hearts: int (0-5)          │
│ - characterName: String      │
│ - spriteSheet: Image         │
│ - animation: PlayerAnimation │
│ - invulnerable: double       │
│ - footstepAccumulator: double│
├──────────────────────────────┤
│ + update(double, Controller, │
│          CollisionMap): void │
│ + render(Graphics, Camera)   │
│ + getCenterX/Y(): double     │
│ + getHearts(): int           │
│ + setHearts(int): void       │
│ + loseHeart(): void          │
│ + isDead(): boolean          │
│ - move(x,y, CollisionMap)    │
└──────────────────────────────┘
       │
       │ uses
       │
       ▼
┌──────────────────────────────┐
│   PlayerAnimation            │
├──────────────────────────────┤
│ + enum State {               │
│   IDLE, WALK, RUN           │
│ }                            │
│                              │
│ - state: State               │
│ - directionRow: int          │
│ - frameIndex: int            │
│ - elapsedSeconds: double     │
├──────────────────────────────┤
│ + update(double, vx, vy)     │
│ + getViewport(): Rectangle2D │
│ + getState(): State          │
└──────────────────────────────┘
```

## Entity System

```
┌────────────────────────────────────┐
│          Ghost                     │
├────────────────────────────────────┤
│ + enum State {                     │
│   IDLE, PATROL, SEARCH, CHASE,    │
│   ATTACK, RETURN                  │
│ }                                  │
│                                    │
│ - x, y: double                     │
│ - spawnX, spawnY: double           │
│ - state: State                     │
│ - stateTimer: double               │
│ - searchTimer: double              │
│ - attackCooldown: double           │
│ - animation: GhostAnimation        │
│ - lastKnownPlayer: (x, y)         │
│ - patrolTarget: (x, y)            │
├────────────────────────────────────┤
│ + update(double, Player, Game)    │
│ + render(Graphics, cameraX, Y)    │
│ + getState(): State                │
│ - moveTowards(tx, ty, spd, dt)    │
│ - pickNewPatrolTarget(): void      │
└────────────────────────────────────┘
       │
       │ uses
       │
       ▼
┌──────────────────────────────┐
│   GhostAnimation             │
├──────────────────────────────┤
│ + enum State {               │
│   FLOAT, ATTACK, DISAPPEAR  │
│ }                            │
│                              │
│ - state: State               │
│ - elapsedSeconds: double     │
│ - scale: double              │
│ - opacity: double            │
├──────────────────────────────┤
│ + update(double, State)      │
│ + getScale(): double         │
│ + getOpacity(): double       │
│ + isDisappeared(): boolean   │
│ + reset(): void              │
└──────────────────────────────┘

┌──────────────────────────────┐
│   ObjectAnimation            │
├──────────────────────────────┤
│ + enum Type {                │
│   DOOR, LAMP, RAIN, TORCH   │
│ }                            │
│                              │
│ - type: Type                 │
│ - elapsedSeconds: double     │
│ - rotation: double           │
│ - scale: double              │
│ - flicker: double            │
│ - offsetY: double            │
├──────────────────────────────┤
│ + update(double): void       │
│ + getRotation(): double      │
│ + getScale(): double         │
│ + getFlicker(): double       │
│ + getOffsetY(): double       │
└──────────────────────────────┘

┌──────────────────────────────┐
│     ItemPickup               │
├──────────────────────────────┤
│ - item: Item                 │
│ - tileColumn: double         │
│ - tileRow: double            │
├──────────────────────────────┤
│ + render(Graphics, Camera)   │
│ + isNear(x, y): boolean      │
│ + getItem(): Item            │
└──────────────────────────────┘
```

## Education System

```
┌──────────────────────────────────────┐
│     EducationManager                 │
├──────────────────────────────────────┤
│ - tasks: Map<String, RoomTask>       │
│ - activeTask: RoomTask               │
│ - activeRoomId: String               │
│ - ghosts: List<Ghost>                │
│ - completedRooms: List<String>       │
│ - loader: QuestionLoader             │
│ - tileMap: TileMap                   │
│ - game: Game                         │
├──────────────────────────────────────┤
│ + update(double, Player): void       │
│ + render(Graphics, Camera, w, h)     │
│ + tryInteractAt(x, y): boolean       │
│ + isActive(): boolean                │
│ + submitAnswer(int): void            │
│ + getCompletedRooms(): List          │
│ + getActiveTaskCorrectCount(): int   │
│ - spawnGhostNearPlayer(): void       │
│ - dropRoomKeyAndUnlock(roomId): void │
│ - ensureRoomTask(roomId): void       │
└──────────────────────────────────────┘
       │
       ├────────┬────────────┐
       │         │            │
       ▼         ▼            ▼
┌──────────────┐ ┌────────────────────┐ ┌──────────────┐
│  RoomTask    │ │  Question          │ │ QuestionLdr  │
├──────────────┤ ├────────────────────┤ ├──────────────┤
│ - roomId:Str │ │ - text: String     │ │              │
│ - questions: │ │ - options: List    │ │ + loadQuest  │
│   List<Quest>│ │ - correctIndex:int │ │   For Room() │
│ - shuffled   │ │                    │ │ + parseJSON()│
│ - correct:int│ │                    │ │ + parseTxt() │
├──────────────┤ ├────────────────────┤ ├──────────────┤
│ + update()   │ │ + getText(): Str   │ │              │
│ + submit()   │ │ + getOptions()     │ │              │
│ + get        │ │ + getCorrect()     │ │              │
│   Current    │ │ + isCorrect(int)   │ │              │
│   Quest()    │ │                    │ │              │
└──────────────┘ └────────────────────┘ └──────────────┘
```

## Map & Collision

```
┌──────────────────────────────────┐
│         TileMap                  │
├──────────────────────────────────┤
│ - tiles: Tile[][]                │
│ - rooms: List<Room>              │
│ - doors: List<Door>              │
│ - width, height: int             │
├──────────────────────────────────┤
│ + render(Graphics, Camera): void │
│ + getTile(col, row): Tile        │
│ + setTile(col, row, Tile): void  │
│ + getRooms(): List<Room>         │
│ + getDoors(): List<Door>         │
│ + findRoomAt(x, y): Optional     │
│ + createSchoolMap(): TileMap     │
└──────────────────────────────────┘
       │
       ├──────┬──────────┐
       │      │          │
       ▼      ▼          ▼
┌────────┐ ┌──────┐ ┌──────────┐
│ Tile   │ │ Room │ │  Door    │
├────────┤ ├──────┤ ├──────────┤
│+ enum  │ │- id  │ │- from    │
│ FLOOR, │ │- name│ │- to      │
│ WALL,  │ │- col │ │- locked  │
│ DOOR   │ │- row │ └──────────┘
└────────┘ │- w,h │
           └──────┘
┌──────────────────────────────────┐
│      CollisionMap                │
├──────────────────────────────────┤
│ - collisionGrid: boolean[][]      │
│ - tileMap: TileMap               │
├──────────────────────────────────┤
│ + isBlocked(x, y, w, h): bool    │
│ + checkTile(x, y): boolean       │
└──────────────────────────────────┘
```

## Inventory & Items

```
┌────────────────────────────────────┐
│       Inventory                    │
├────────────────────────────────────┤
│ - items: Map<String, Integer>      │
│ - itemRegistry: ItemRegistry       │
├────────────────────────────────────┤
│ + addItem(Item): void              │
│ + removeItem(id): void             │
│ + useSlot(int): String             │
│ + getItemCounts(): Map             │
│ + replaceAll(Map): void            │
└────────────────────────────────────┘
       │
       │ uses
       │
       ▼
┌────────────────────────────────────┐
│      Item (Interface)              │
├────────────────────────────────────┤
│ + getId(): String                  │
│ + getDisplayName(): String         │
│ + use(): String                    │
└────────────────────────────────────┘
       △
       │ implements
       │
       ├─────────┬──────────┬──────────┬─────────┐
       │         │          │          │         │
    Flashlight  Key    Lighter    Toolbox  FirstAidKit
    (+ more)
```

## Save System

```
┌────────────────────────────────────┐
│      SaveManager                   │
├────────────────────────────────────┤
│ - saveFile: File                   │
├────────────────────────────────────┤
│ + saveGame(SaveData): void         │
│ + load(): SaveData                 │
│ + hasSave(): boolean               │
│ + deleteSave(): void               │
└────────────────────────────────────┘
       │
       │ serializes
       │
       ▼
┌────────────────────────────────────┐
│      SaveData                      │
├────────────────────────────────────┤
│ - characterName: String            │
│ - playerX, playerY: double         │
│ - hearts: int                      │
│ - playTimeSeconds: double          │
│ - currentRoom: String              │
│ - completedRooms: List<String>     │
│ - currentTask: String              │
│ - inventoryItems: Map<String, Int> │
│ - keys: List<String>               │
├────────────────────────────────────┤
│ + getters/setters                  │
└────────────────────────────────────┘

┌────────────────────────────────────┐
│      FileManager                   │
├────────────────────────────────────┤
│                                    │
│ + readProperties(file): Properties │
│ + writeProperties(file, props)     │
│ + readJSON(file): JSONObject       │
│ + writeJSON(file, json)            │
└────────────────────────────────────┘
```

## Audio System

```
┌────────────────────────────────────┐
│       AudioManager (Singleton)     │
├────────────────────────────────────┤
│ - samples: Map<String, byte[]>     │
│ - loopingClips: Map<String, Clip>  │
│ - masterVolume: double             │
│ - ambienceVolume: double           │
│ - sfxVolume: double                │
│ - musicVolume: double              │
├────────────────────────────────────┤
│ + playLoop(key): void              │
│ + stopLoop(key): void              │
│ + playOneShot(key): void           │
│ + setMasterVolume(value): void     │
│ + setAmbienceVolume(value): void   │
│ + setSfxVolume(value): void        │
│ + setMusicVolume(value): void      │
│ + stopAll(): void                  │
│ + getInstance(): AudioManager      │
│ - preload(): void                  │
│ - register(key, duration): void    │
│ - generateSample(key): byte[]      │
└────────────────────────────────────┘
```

## Class Relationships

```
Inheritance:
  Application ← Main

Composition:
  Game
    ├─ Player
    ├─ TileMap
    ├─ Camera
    ├─ GameLoop
    ├─ Inventory
    ├─ EducationManager
    │   ├─ RoomTask
    │   │   ├─ Question
    │   │   └─ QuestionLoader
    │   └─ Ghost
    │       └─ GhostAnimation
    ├─ ObjectAnimation (rain, torch)
    └─ Visual Effects (particles, fog)

  SceneManager
    ├─ MainMenuScene
    ├─ CharacterScene
    ├─ GameScene
    └─ TeacherAdminScene

Association:
  Player → PlayerAnimation (has-a)
  Ghost → GhostAnimation (has-a)
  Game → AudioManager (uses)
  SaveManager → SaveData (manages)
```

---

**UML Version**: 1.0 | **Last Updated**: 2026-08-08
