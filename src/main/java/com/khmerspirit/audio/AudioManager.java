package com.khmerspirit.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * AudioManager handles all sound effects, voice lines, and background music in the game.
 *
 * Capabilities:
 * - Low-latency WAV clip playback via javax.sound.sampled for SFX and voice lines.
 * - Hardware-accelerated background MP3 playback via JavaFX MediaPlayer (background streaming, zero lag).
 * - Automatic switching between ambient exploration music ("Tili Tili Bom") and room-specific music ("music&art").
 * - Fallback real-time PCM waveform synthesis if audio files are missing.
 * - Master, Ambience, SFX, and Music volume controls.
 *
 * @author Khmer Spirit Dev Team
 * @version 3.0
 */
public class AudioManager {

    private static final AudioFormat FORMAT = new AudioFormat(44100f, 16, 1, true, false);
    private static final AudioManager INSTANCE = new AudioManager();

    private final Map<String, byte[]> samples = new HashMap<>();
    private final Map<String, Clip> loopingClips = new HashMap<>();

    // Dedicated JavaFX MediaPlayers for streaming MP3 tracks
    private MediaPlayer homeMusicPlayer = null;
    private MediaPlayer gameMusicPlayer = null;
    private MediaPlayer artMusicPlayer = null;
    private String currentGameMusicTrack = null;

    private double masterVolume = 0.35;
    private double ambienceVolume = 0.35;
    private double sfxVolume = 0.55;
    private double musicVolume = 0.35;

    private AudioManager() {
        preload();
    }

    public static AudioManager getInstance() {
        return INSTANCE;
    }

    /**
     * Pre-generates synthesized fallback audio samples on initialization.
     */
    public void preload() {
        register("rain", 1.2f);
        register("wind", 1.4f);
        register("footsteps", 0.24f);
        register("ghost", 0.75f);
        register("heartbeat", 0.7f);
        register("door", 0.6f);
        register("puzzle_complete", 1.0f);
        register("menu_music", 1.6f);
        register("ambience", 1.8f);
    }

    private void register(String key, float durationSeconds) {
        samples.put(key, generateSample(key, durationSeconds));
    }

    // ==========================================
    // MP3 MUSIC PLAYBACK (JAVAFX MEDIAPLAYER)
    // ==========================================

    /**
     * Resolves the file URI string for a music file in /audio/music/ or filesystem.
     */
    private String resolveMusicUri(String fileName) {
        try {
            // 1. Direct file on disk in resources
            File directFile = new File("src/main/resources/audio/music/" + fileName);
            if (directFile.exists()) {
                return directFile.toURI().toString();
            }

            File runtimeFile = new File("audio/music/" + fileName);
            if (runtimeFile.exists()) {
                return runtimeFile.toURI().toString();
            }

            // 2. Classpath resource
            URL res = getClass().getResource("/audio/music/" + fileName);
            if (res != null) {
                return res.toExternalForm();
            }
        } catch (Exception e) {
            System.err.println("[AudioManager] Error finding music URI for " + fileName + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Plays the Home Screen music ("at the home game.mp3").
     * Replaces the video background sound with this soundtrack.
     */
    public void playHomeMusic() {
        stopGameMusic();
        stopArtMusic();

        if (homeMusicPlayer != null) {
            homeMusicPlayer.play();
            updateHomeMusicVolume();
            return;
        }

        String uri = resolveMusicUri("at the home game.mp3");
        if (uri == null) {
            System.err.println("[AudioManager] Could not find 'at the home game.mp3'");
            return;
        }

        try {
            Media media = new Media(uri);
            homeMusicPlayer = new MediaPlayer(media);
            homeMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            updateHomeMusicVolume();
            homeMusicPlayer.play();
        } catch (Exception e) {
            System.err.println("[AudioManager] Failed to play home music: " + e.getMessage());
        }
    }

    public void stopHomeMusic() {
        if (homeMusicPlayer != null) {
            try {
                homeMusicPlayer.stop();
                homeMusicPlayer.dispose();
            } catch (Exception ignored) {}
            homeMusicPlayer = null;
        }
    }

    /**
     * Plays the main in-game background music ("Tili Tili Bom").
     * Played at a subtle, non-intrusive volume (scaled by 0.50) as requested.
     */
    public void playGameMusic() {
        stopHomeMusic();
        stopArtMusic();

        if (gameMusicPlayer != null && "tili".equals(currentGameMusicTrack)) {
            gameMusicPlayer.play();
            updateGameMusicVolume();
            return;
        }

        stopGameMusic();

        // Exact name from music folder
        String fileName = "Tili Tili Bom (Spooky Russian Lullaby) English Version [LYRICS].mp3";
        String uri = resolveMusicUri(fileName);
        if (uri == null) {
            System.err.println("[AudioManager] Could not find '" + fileName + "'");
            return;
        }

        try {
            Media media = new Media(uri);
            gameMusicPlayer = new MediaPlayer(media);
            gameMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            currentGameMusicTrack = "tili";
            updateGameMusicVolume();
            gameMusicPlayer.play();
        } catch (Exception e) {
            System.err.println("[AudioManager] Failed to play game music: " + e.getMessage());
        }
    }

    public void pauseGameMusic() {
        if (gameMusicPlayer != null) {
            try {
                gameMusicPlayer.pause();
            } catch (Exception ignored) {}
        }
    }

    public void resumeGameMusic() {
        if (gameMusicPlayer != null) {
            try {
                gameMusicPlayer.play();
                updateGameMusicVolume();
            } catch (Exception ignored) {}
        } else {
            playGameMusic();
        }
    }

    public void stopGameMusic() {
        if (gameMusicPlayer != null) {
            try {
                gameMusicPlayer.stop();
                gameMusicPlayer.dispose();
            } catch (Exception ignored) {}
            gameMusicPlayer = null;
            currentGameMusicTrack = null;
        }
    }

    /**
     * Plays the dedicated Music & Art Room track ("music&art.mp3").
     * Automatically pauses Tili Tili Bom while playing.
     */
    public void playArtRoomMusic() {
        pauseGameMusic();

        if (artMusicPlayer != null) {
            artMusicPlayer.play();
            updateArtMusicVolume();
            return;
        }

        String uri = resolveMusicUri("music&art.mp3");
        if (uri == null) {
            System.err.println("[AudioManager] Could not find 'music&art.mp3'");
            return;
        }

        try {
            Media media = new Media(uri);
            artMusicPlayer = new MediaPlayer(media);
            artMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            updateArtMusicVolume();
            artMusicPlayer.play();
        } catch (Exception e) {
            System.err.println("[AudioManager] Failed to play art room music: " + e.getMessage());
        }
    }

    /**
     * Stops the Music & Art Room track and resumes the main ambient game music.
     */
    public void stopArtRoomMusic() {
        stopArtMusic();
        resumeGameMusic();
    }

    private void stopArtMusic() {
        if (artMusicPlayer != null) {
            try {
                artMusicPlayer.stop();
                artMusicPlayer.dispose();
            } catch (Exception ignored) {}
            artMusicPlayer = null;
        }
    }

    /**
     * Called by Game whenever the player changes rooms or enters a new area.
     */
    public void updateRoomMusic(String roomId) {
        if (roomId == null) return;
        String rid = roomId.toLowerCase().trim();
        boolean isArtRoom = rid.contains("music") || rid.contains("art") || rid.equals("computer");
        if (isArtRoom) {
            playArtRoomMusic();
        } else {
            if (artMusicPlayer != null) {
                stopArtRoomMusic();
            } else if (gameMusicPlayer == null || !gameMusicPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)) {
                resumeGameMusic();
            }
        }
    }

    // ==========================================
    // WAV CLIP LOADING & PLAYBACK (SFX / VOICE)
    // ==========================================

    public Clip loadClipFromResource(String relativePath) {
        String cleanPath = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
        try (InputStream in = getClass().getResourceAsStream(cleanPath)) {
            if (in != null) {
                try (BufferedInputStream buf = new BufferedInputStream(in);
                     AudioInputStream ais = AudioSystem.getAudioInputStream(buf)) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    return clip;
                }
            }
        } catch (Exception ignored) {}

        try {
            File file = new File("src/main/resources" + cleanPath);
            if (!file.exists()) {
                file = new File(cleanPath.substring(1));
            }
            if (file.exists()) {
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(file)) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    return clip;
                }
            }
        } catch (Exception ignored) {}

        return null;
    }

    private Clip createClipForKey(String key) {
        // First check directly mapped mixkit filenames
        String mappedFile = mapKeyToFilename(key);
        if (mappedFile != null) {
            Clip c = loadClipFromResource("/audio/sfx/" + mappedFile);
            if (c != null) return c;
        }

        // Check standard subdirectories: sfx, voice, music
        String[] subdirs = { "/audio/sfx/", "/audio/voice/", "/audio/music/" };
        for (String subdir : subdirs) {
            Clip c = loadClipFromResource(subdir + key + ".wav");
            if (c != null) return c;
        }
        return null;
    }

    /**
     * Maps logical sound keys to the renamed mixkit sound files in /audio/sfx/.
     */
    private String mapKeyToFilename(String key) {
        return switch (key) {
            case "start_game", "startgame" -> "mixkit-startGame.wav";
            case "key", "getKey", "get_key", "claim_key" -> "mixkit-getKey.wav";
            case "bell", "bell_ring", "church_bell" -> "mixkit-church-bell-loop-621.wav";
            case "heartbeat", "heart" -> "mixkit-human-single-heart-beat-490.wav";
            case "footsteps", "walk" -> "mixkit-footsteps-in-a-tunnel-loop-543.wav";
            case "sprint", "run", "running" -> "mixkit-running-through-the-forest-1232.wav";
            case "rain", "light_rain" -> "mixkit-light-rain-loop-2393.wav";
            default -> null;
        };
    }

    /**
     * Plays a sound effect once.
     * The clip auto-disposes after completion.
     */
    public void playOneShot(String key) {
        Clip clip = createClipForKey(key);
        if (clip == null) {
            byte[] data = samples.get(key);
            if (data != null) {
                clip = createClip(data);
            }
        }
        if (clip == null) return;

        final Clip finalClip = clip;
        applyVolume(finalClip, key);
        finalClip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                finalClip.close();
            }
        });
        finalClip.setFramePosition(0);
        finalClip.start();
    }

    /**
     * Starts looping a registered sound indefinitely.
     */
    public void playLoop(String key) {
        Clip clip = loopingClips.get(key);
        if (clip == null) {
            clip = createClipForKey(key);
            if (clip == null) {
                byte[] data = samples.get(key);
                if (data != null) {
                    clip = createClip(data);
                }
            }
            if (clip == null) return;
            loopingClips.put(key, clip);
        }

        applyVolume(clip, key);
        clip.stop();
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stopLoop(String key) {
        Clip clip = loopingClips.remove(key);
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }

    // ==========================================
    // CONVENIENCE EVENT METHODS
    // ==========================================

    public void playSfx(String name) { playOneShot(name); }
    public void playVoice(String name) { playOneShot(name); }

    public void playStartGame() {
        playOneShot("start_game");
    }

    public void playGetKey() {
        playOneShot("key");
    }

    public void playHeartbeat() {
        playOneShot("heartbeat");
    }

    public void playFootstep() {
        playOneShot("footsteps");
    }

    public void playSprintFootstep() {
        playOneShot("sprint");
    }

    public void playBellRing() {
        playOneShot("bell_ring");
    }

    public void playCorrectAnswer() {
        playOneShot("correct_answer");
    }

    public void playWrongAnswer() {
        playOneShot("wrong_answer");
        playOneShot("wrong_answer_laugh");
    }

    public void playDoorUnlock() {
        playOneShot("key");
    }

    public void playItemPickup() {
        playOneShot("item_pickup");
    }

    public void playJumpscare() {
        playOneShot("jumpscare");
        playOneShot("ghost_whisper");
    }

    public void playFlashlight() {
        playOneShot("flashlight_click");
    }

    public void playStoryIntro() {
        playOneShot("sothea_story_intro");
    }

    // ==========================================
    // VOLUME CONTROL
    // ==========================================

    public void setMasterVolume(double value) {
        masterVolume = clamp(value);
        applyAllLoopVolumes();
        updateHomeMusicVolume();
        updateGameMusicVolume();
        updateArtMusicVolume();
    }

    public void setAmbienceVolume(double value) {
        ambienceVolume = clamp(value);
        applyAllLoopVolumes();
    }

    public void setSfxVolume(double value) {
        sfxVolume = clamp(value);
        applyAllLoopVolumes();
    }

    public void setMusicVolume(double value) {
        musicVolume = clamp(value);
        applyAllLoopVolumes();
        updateHomeMusicVolume();
        updateGameMusicVolume();
        updateArtMusicVolume();
    }

    private void updateHomeMusicVolume() {
        if (homeMusicPlayer != null) {
            homeMusicPlayer.setVolume(masterVolume * musicVolume);
        }
    }

    private void updateGameMusicVolume() {
        if (gameMusicPlayer != null) {
            // Kept comfortably subtle (scaled by 0.50) so it works as background music
            gameMusicPlayer.setVolume(masterVolume * musicVolume * 0.50);
        }
    }

    private void updateArtMusicVolume() {
        if (artMusicPlayer != null) {
            artMusicPlayer.setVolume(masterVolume * musicVolume * 0.75);
        }
    }

    public double getMasterVolume() { return masterVolume; }
    public double getAmbienceVolume() { return ambienceVolume; }
    public double getSfxVolume() { return sfxVolume; }
    public double getMusicVolume() { return musicVolume; }

    public void stopAll() {
        for (String key : new HashMap<>(loopingClips).keySet()) {
            stopLoop(key);
        }
        stopHomeMusic();
        stopGameMusic();
        stopArtMusic();
    }

    private void applyAllLoopVolumes() {
        for (Map.Entry<String, Clip> entry : loopingClips.entrySet()) {
            applyVolume(entry.getValue(), entry.getKey());
        }
    }

    private void applyVolume(Clip clip, String key) {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float volume = (float) getVolumeFor(key);
        float gainValue = (float) (Math.log10(Math.max(0.0001, volume)) * 20.0);
        gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), gainValue)));
    }

    private double getVolumeFor(String key) {
        double base = switch (key) {
            case "rain", "wind", "ambience", "light_rain" -> ambienceVolume;
            case "menu_music", "menu_theme" -> musicVolume;
            default -> sfxVolume;
        };
        return clamp(base * masterVolume);
    }

    private Clip createClip(byte[] data) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(FORMAT, data, 0, data.length);
            return clip;
        } catch (Exception ignored) {
            return null;
        }
    }

    private byte[] generateSample(String key, float durationSeconds) {
        int frameCount = (int) (FORMAT.getSampleRate() * durationSeconds);
        byte[] data = new byte[frameCount * 2];
        Random random = new Random(key.hashCode());
        for (int i = 0; i < frameCount; i++) {
            double t = i / FORMAT.getSampleRate();
            double sample = switch (key) {
                case "rain" -> generateRainSample(random, t);
                case "wind" -> generateWindSample(random, t);
                case "footsteps" -> generateFootstepSample(random, t);
                case "ghost" -> generateGhostSample(random, t);
                case "heartbeat" -> generateHeartbeatSample(t);
                case "door" -> generateDoorSample(t);
                case "puzzle_complete" -> generatePuzzleSample(t);
                case "menu_music" -> generateMenuMusicSample(t);
                default -> generateAmbienceSample(t);
            };
            writeSample(data, i * 2, sample);
        }
        return data;
    }

    private double generateRainSample(Random random, double t) {
        double noise = (random.nextDouble() * 2.0 - 1.0) * 0.14;
        double tone = Math.sin(2.0 * Math.PI * 1800.0 * t) * 0.02;
        return noise + tone;
    }

    private double generateWindSample(Random random, double t) {
        double lowTone = Math.sin(2.0 * Math.PI * 110.0 * t) * 0.08;
        double shimmer = Math.sin(2.0 * Math.PI * 0.8 * t) * 0.02;
        return lowTone + shimmer + (random.nextDouble() - 0.5) * 0.015;
    }

    private double generateFootstepSample(Random random, double t) {
        if (t < 0.02) {
            return 0.3 * Math.sin(2.0 * Math.PI * 600.0 * t);
        }
        if (t < 0.08) {
            return (random.nextDouble() - 0.5) * 0.08;
        }
        return 0.0;
    }

    private double generateGhostSample(Random random, double t) {
        double modulation = Math.sin(2.0 * Math.PI * 120.0 * t) * 0.18;
        double noise = (random.nextDouble() - 0.5) * 0.08;
        return modulation + noise;
    }

    private double generateHeartbeatSample(double t) {
        double pulse = Math.sin(2.0 * Math.PI * 1.6 * t) > 0 ? 1.0 : -1.0;
        return pulse * 0.35 * Math.exp(-t * 3.0);
    }

    private double generateDoorSample(double t) {
        double envelope = Math.exp(-t * 6.0);
        return Math.sin(2.0 * Math.PI * 440.0 * t) * envelope * 0.35;
    }

    private double generatePuzzleSample(double t) {
        double tone = 0.0;
        if (t < 0.28) tone = Math.sin(2.0 * Math.PI * 660.0 * t);
        else if (t < 0.56) tone = Math.sin(2.0 * Math.PI * 880.0 * t);
        else tone = Math.sin(2.0 * Math.PI * 1100.0 * t);
        return tone * 0.25 * Math.exp(-t * 2.2);
    }

    private double generateMenuMusicSample(double t) {
        double note = switch ((int) (t * 4.0)) {
            case 0 -> Math.sin(2.0 * Math.PI * 440.0 * t);
            case 1 -> Math.sin(2.0 * Math.PI * 523.0 * t);
            case 2 -> Math.sin(2.0 * Math.PI * 659.0 * t);
            default -> Math.sin(2.0 * Math.PI * 784.0 * t);
        };
        return note * 0.16;
    }

    private double generateAmbienceSample(double t) {
        return Math.sin(2.0 * Math.PI * 80.0 * t) * 0.03 + Math.sin(2.0 * Math.PI * 0.4 * t) * 0.01;
    }

    private void writeSample(byte[] data, int offset, double sample) {
        double clamped = Math.max(-1.0, Math.min(1.0, sample));
        short pcm = (short) (clamped * Short.MAX_VALUE * 0.6);
        data[offset] = (byte) (pcm & 0xff);
        data[offset + 1] = (byte) ((pcm >>> 8) & 0xff);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
