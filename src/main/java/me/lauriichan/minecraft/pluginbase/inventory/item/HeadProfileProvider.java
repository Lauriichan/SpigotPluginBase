package me.lauriichan.minecraft.pluginbase.inventory.item;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

final class HeadProfileProvider {

    public static final UUID HEAD_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final String URL_BASE = "http://textures.minecraft.net/texture/%s";

    static final HeadProfileProvider PROVIDER = new HeadProfileProvider();

    public static void dispose() {
        PROVIDER.profiles.clear();
    }

    private final Object2ObjectOpenHashMap<String, PlayerProfile> profiles = new Object2ObjectOpenHashMap<>();

    private HeadProfileProvider() {}

    private PlayerProfile buildProfile(final String texture) {
        final PlayerProfile profile = Bukkit.createPlayerProfile(HEAD_ID, "Head");
        profile.getTextures().setSkin(buildUrl(texture));
        return profile;
    }

    private URL buildUrl(final String texture) {
        try {
            return new URL(String.format(URL_BASE, texture));
        } catch (final MalformedURLException e) {
            return null;
        }
    }

    public void setTexture(final SkullMeta meta, final String texture) {
        meta.setOwnerProfile(profiles.computeIfAbsent(texture, this::buildProfile));
    }
    
    public void setTexture(final SkullMeta meta, final OfflinePlayer player) {
        meta.setOwnerProfile(player.getPlayerProfile());
    }

    public String getTexture(final SkullMeta meta) {
        final PlayerProfile profile = meta.getOwnerProfile();
        if (profile == null) {
            return null;
        }
        final URL url = profile.getTextures().getSkin();
        if (url == null) {
            return null;
        }
        final String[] parts = url.toString().split("/");
        return parts[parts.length - 1];
    }

}