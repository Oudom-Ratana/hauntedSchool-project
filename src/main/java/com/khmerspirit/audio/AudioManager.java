package com.khmerspirit.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * AudioManager handles all synthesized audio playback in the game.
 * 
 * This singleton manages:
 * - Looping background sounds (rain, wind, menu music, ambience)
 * - One-shot sound effects (footsteps, door, ghost, heartbeat, puzzle complete)
 * - Independent volume control for master, ambience, SFX, and music
 * 
 * All audio is synthesized in real-time using PCM waveform generation,
 * requiring no external audio files.
 * 
 * @author Khmer Spirit Dev Team
 * @version 1.0
 */
public class AudioManager {

    private static final AudioFormat FORMAT = new AudioFormat(44100f, 16, 1, true, false);
    private static final AudioManager INSTANCE = new AudioManager();

    private final Map<String, byte[]> samples = new HashMap<>();
    private final Map<String, Clip> loopingClips = new HashMap<>();

    private double masterVolume = 0.35;
    private double ambienceVolume = 0.35;
    private double sfxVolume = 0.55;
    private double musicVolume = 0.35;

    private AudioManager() {
        preload();
    }

    /**
     * Returns the singleton AudioManager instance.
     * @return AudioManager singleton
     */
    public static AudioManager getInstance() {
        return INSTANCE;
    }

    /**
     * Pre-generates all audio samples on initialization.
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

    /**
     * Registers a sound sample by generating audio and caching it.
     * @param key Unique identifier for the sound
     * @param durationSeconds How long the sound lasts
     */
    private void register(String key, float durationSeconds) {
        samples.put(key, generateSample(key, durationSeconds));
    }

    /**
     * Starts looping a registered sound indefinitely.
     * @param key Sound identifier to loop
     */
    public void playLoop(String key) {
        byte[] data = samples.get(key);
        if (data == null) return;

        Clip clip = loopingClips.get(key);
        if (clip == null) {
            clip = createClip(data);
            if (clip == null) return;
            loopingClips.put(key, clip);
        }

        applyVolume(clip, key);
        clip.stop();
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /**
     * Stops a currently looping sound.
     * @param key Sound identifier to stop
     */
    public void stopLoop(String key) {
        Clip clip = loopingClips.remove(key);
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }

    /**
     * Plays a sound effect once.
     * The clip auto-disposes after completion.
     * @param key Sound identifier to play
     */
    public void playOneShot(String key) {
        byte[] data = samples.get(key);
        if (data == null) return;

        Clip clip = createClip(data);
        if (clip == null) return;

        applyVolume(clip, key);
        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                clip.close();
            }
        });
        clip.setFramePosition(0);
        clip.start();
    }

    /**
     * Sets the master volume level (affects all sounds).
     * @param value Volume level 0.0-1.0
     */
    public void setMasterVolume(double value) {
        masterVolume = clamp(value);
        applyAllLoopVolumes();
    }

    /**
     * Sets ambience volume (rain, wind, background sounds).
     * @param value Volume level 0.0-1.0
     */
    public void setAmbienceVolume(double value) {
        ambienceVolume = clamp(value);
        applyAllLoopVolumes();
    }

    /**
     * Sets SFX volume (effects like footsteps, door, ghost).
     * @param value Volume level 0.0-1.0
     */
    public void setSfxVolume(double value) {
        sfxVolume = clamp(value);
        applyAllLoopVolumes();
    }

    /**
     * Sets music volume (menu and game music).
     * @param value Volume level 0.0-1.0
     */
    public void setMusicVolume(double value) {
        musicVolume = clamp(value);
        applyAllLoopVolumes();
    }

    public double getMasterVolume() { return masterVolume; }
    public double getAmbienceVolume() { return ambienceVolume; }
    public double getSfxVolume() { return sfxVolume; }
    public double getMusicVolume() { return musicVolume; }

    /**
     * Stops all currently playing sounds.
     */
    public void stopAll() {
        for (String key : new HashMap<>(loopingClips).keySet()) {
            stopLoop(key);
        }
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
            case "rain", "wind", "ambience" -> ambienceVolume;
            case "menu_music" -> musicVolume;
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

    /**
     * Generates PCM audio data for a given sound type.
     * Uses simple waveform synthesis to create atmospheric and gameplay sounds.
     * @param key Sound identifier
     * @param durationSeconds Length of generated audio
     * @return Raw PCM byte array ready for playback
     */
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
