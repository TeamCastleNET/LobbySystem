package de.zcreeper.lobbysystem.data;

import de.zcreeper.lobbysystem.cosmetics.Cosmetic;
import de.zcreeper.lobbysystem.cosmetics.CosmeticCategory;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private final boolean newPlayer;

    private String name;
    private long cookies;
    private final Set<String> ownedCosmetics = new LinkedHashSet<>();
    private final Map<CosmeticCategory, String> selectedCosmetics = new EnumMap<>(CosmeticCategory.class);

    private volatile boolean dirty;

    public PlayerData(UUID uuid, String name, boolean newPlayer) {
        this.uuid = uuid;
        this.name = name;
        this.newPlayer = newPlayer;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // true, wenn es für den Spieler noch keine Zeile in der Tabelle gab
    public boolean isNewPlayer() {
        return newPlayer;
    }

    public long getCookies() {
        return cookies;
    }

    public void setCookies(long cookies) {
        this.cookies = cookies;
        dirty = true;
    }

    public boolean ownsCosmetic(Cosmetic cosmetic) {
        return ownedCosmetics.contains(toId(cosmetic));
    }

    public void addOwnedCosmetic(Cosmetic cosmetic) {
        if (ownedCosmetics.add(toId(cosmetic))) dirty = true;
    }

    public String getSelectedCosmetic(CosmeticCategory category) {
        return selectedCosmetics.get(category);
    }

    public void setSelectedCosmetic(CosmeticCategory category, String id) {
        if (id == null) selectedCosmetics.remove(category);
        else selectedCosmetics.put(category, id);

        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /*
    In der Datenbank als Text gespeichert:
    cosmetics_owned    -> "trail:herzen,hat:krone"
    cosmetics_selected -> "trail:herzen,hat:krone"
     */

    public String serializeOwned() {
        return String.join(",", ownedCosmetics);
    }

    public void deserializeOwned(String value) {
        ownedCosmetics.clear();

        if (value == null || value.isBlank()) return;

        for (String entry : value.split(",")) {
            if (!entry.isBlank()) ownedCosmetics.add(entry.trim());
        }
    }

    public String serializeSelected() {
        StringBuilder builder = new StringBuilder();

        selectedCosmetics.forEach((category, id) -> {
            if (!builder.isEmpty()) builder.append(',');
            builder.append(category.getId()).append(':').append(id);
        });

        return builder.toString();
    }

    public void deserializeSelected(String value) {
        selectedCosmetics.clear();

        if (value == null || value.isBlank()) return;

        for (String entry : value.split(",")) {
            String[] parts = entry.trim().split(":", 2);

            if (parts.length != 2) continue;

            for (CosmeticCategory category : CosmeticCategory.values()) {
                if (category.getId().equals(parts[0])) selectedCosmetics.put(category, parts[1]);
            }
        }
    }

    public PlayerData copy() {
        PlayerData copy = new PlayerData(uuid, name, newPlayer);

        copy.cookies = cookies;
        copy.ownedCosmetics.addAll(ownedCosmetics);
        copy.selectedCosmetics.putAll(selectedCosmetics);

        return copy;
    }

    private static String toId(Cosmetic cosmetic) {
        return cosmetic.getCategory().getId() + ":" + cosmetic.getId();
    }

}
