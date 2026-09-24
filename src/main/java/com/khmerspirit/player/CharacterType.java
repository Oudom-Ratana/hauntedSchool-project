package com.khmerspirit.player;

/** Playable identities exposed by the character-selection menu. */
public enum CharacterType {
    VICHEKA("vicheka", "Vicheka", "VICHEKA", "Student",
            "A curious student constantly investigating strange rumors.", "Male Student",
            "/images/Charactors/card_vicheka.png", "/images/Charactors/sprite_vicheka.png"),
    KAKADA("kakada", "Kakada", "KAKADA", "Student",
            "A brave student wearing a scarf and backpack, ready to face danger.", "Male Kakada",
            "/images/Charactors/card_kakada.png", "/images/Charactors/sprite_kakada.png"),
    CHANDA("chanda", "Chanda", "CHANDA", "Student",
            "An observant and intelligent student with sharp instincts.", "Female Student",
            "/images/Charactors/card_chanda.png", "/images/Charactors/sprite_chanda.png"),
    PREAH_SONGK("preah_songk", "Preah Songk", "PREAH SONGK", "Monk",
            "A devoted Buddhist monk reciting sacred prayers for protection.", "Male Preah Songk",
            "/images/Charactors/card_preah_songk.png", "/images/Charactors/sprite_preah_songk.png"),
    SOPHEA("sophea", "Sophea", "Sophea", "Traditional Dancer",
            "Graceful, observant, and fearless in the dark.", "Female Sophea",
            "/images/Charactors/card_sophea.png", "/images/player/sophea/player.png"),
    DARA("dara", "Dara", "Dara", "Tech Student",
            "Sharp, analytical, and ready with technical acumen.", "Male Dara",
            "/images/Charactors/card_dara.png", "/images/player/dara/player.png"),
    BORA("bora", "Bora", "Bora", "School Athlete",
            "Energetic, fast, and driven by courage.", "Male Bora",
            "/images/Charactors/card_bora.png", "/images/player/bora/player.png"),
    KESOR("kesor", "Kesor", "Kesor", "Scholar Pupil",
            "Intelligent, resourceful, and focused on truth.", "Female Kesor",
            "/images/Charactors/card_kesor.png", "/images/player/kesor/player.png"),
    KRU_KHMER("kru_khmer", "Kru Khmer", "Kru Khmer", "Acolyte Healer",
            "Wields sacred protections and protective mantras.", "Male Kru Khmer",
            "/images/Charactors/card_kru_khmer.png", "/images/player/kru_khmer/player.png"),
    RITHY("rithy", "Rithy", "Rithy", "Bokator Fighter",
            "Master of ancient martial arts, fearless and swift.", "Male Rithy",
            "/images/player V2/card_rithy.png", "/images/player V2/rithy/player.png"),
    KALYAN("kalyan", "Kalyan", "Kalyan", "School Reporter",
            "Tenacious student journalist capturing the unseen truth.", "Female Kalyan",
            "/images/player V2/card_kalyan.png", "/images/player V2/kalyan/player.png"),
    VISAL("visal", "Visal", "Visal", "Ancient Archivist",
            "Scholarly investigator deciphering ancient curses and texts.", "Male Visal",
            "/images/player V2/card_visal.png", "/images/player V2/visal/player.png"),
    NEARY("neary", "Neary", "Neary", "First-Aid Medic",
            "Compassionate cadet nurse equipped to heal and protect.", "Female Neary",
            "/images/player V2/card_neary.png", "/images/player V2/neary/player.png"),
    SAMNANG("samnang", "Samnang", "Samnang", "Lucky Rebel",
            "Streetwise survivor blessed with supernatural luck.", "Male Samnang",
            "/images/player V2/card_samnang.png", "/images/player V2/samnang/player.png"),
    TEVY("tevy", "Tevy", "Tevy", "Spirit Flutist",
            "Traditional musician whose melodic flute calms restless souls.", "Female Tevy",
            "/images/player V2/card_tevy.png", "/images/player V2/tevy/player.png"),
    SOVANN("sovann", "Sovann", "Sovann", "Relic Explorer",
            "Daring archaeology seeker carrying an ancient spirit lantern.", "Male Sovann",
            "/images/player V2/card_sovann.png", "/images/player V2/sovann/player.png"),
    GHOST("ghost", "Ghost", "Ghost", "Haunted Spirit",
            "A terrifying, vengeful spirit that haunts the cursed school corridors.", "Ghost Spirit",
            "/images/ghost/card_prey_ghost.png", "/images/ghost/ghost_prey_sprite.png");

    private final String id;
    private final String khmerName;
    private final String displayName;
    private final String role;
    private final String description;
    private final String gameCharacterName;
    private final String cardImagePath;
    private final String spriteImagePath;

    CharacterType(String id, String khmerName, String displayName, String role,
                  String description, String gameCharacterName,
                  String cardImagePath, String spriteImagePath) {
        this.id = id;
        this.khmerName = khmerName;
        this.displayName = displayName;
        this.role = role;
        this.description = description;
        this.gameCharacterName = gameCharacterName;
        this.cardImagePath = cardImagePath;
        this.spriteImagePath = spriteImagePath;
    }

    public static CharacterType[] getSelectableCharacters() {
        return new CharacterType[] { VICHEKA, CHANDA, PREAH_SONGK };
    }

    public String getId() { return id; }
    public String getKhmerName() { return khmerName; }
    public String getDisplayName() { return displayName; }
    public String getRole() { return role; }
    public String getDescription() { return description; }
    public String getGameCharacterName() { return gameCharacterName; }
    public String getCardImagePath() { return cardImagePath; }
    public String getSpriteImagePath() { return spriteImagePath; }
}
