package me.dasfaust.discordintegration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.RestAction;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import com.mojang.authlib.GameProfile;

import dcshadow.net.kyori.adventure.text.Component;
import dcshadow.net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import de.erdbeerbaerlp.dcintegration.common.util.ComponentUtils;
import de.erdbeerbaerlp.dcintegration.common.util.McServerInterface;

public class ForgeServerInterface implements McServerInterface {

    @Override
    public int getMaxPlayers() {
        return MinecraftServer.getServer()
            .getMaxPlayers();
    }

    @Override
    public int getOnlinePlayers() {
        return MinecraftServer.getServer()
            .getCurrentPlayerCount();
    }

    @Override
    public void sendIngameMessage(Component msg) {
        for (final EntityPlayerMP player : MinecraftServer.getServer()
            .getConfigurationManager().playerEntityList) {
            final Map.Entry<Boolean, Component> ping = ComponentUtils
                .parsePing(msg, player.getUniqueID(), player.getDisplayName());
            final String jsonComp = GsonComponentSerializer.gson()
                .serialize(ping.getValue())
                .replace("\\\\n", "\n");
            final IChatComponent comp = IChatComponent.Serializer.func_150699_a(jsonComp);
            player.addChatMessage(comp);
        }

        final String jsonComp = GsonComponentSerializer.gson()
            .serialize(msg)
            .replace("\\\\n", "\n");
        final IChatComponent comp = IChatComponent.Serializer.func_150699_a(jsonComp);
        MinecraftServer.getServer()
            .addChatMessage(comp);
    }

    @Override
    public void sendIngameMessage(String msg, UUID playerUuid) {
        for (final EntityPlayerMP player : MinecraftServer.getServer()
            .getConfigurationManager().playerEntityList) {
            if (player.getUniqueID() == playerUuid) {
                player.addChatMessage(new ChatComponentText(msg));
                break;
            }
        }
    }

    @Override
    public void sendIngameReaction(Member member, RestAction<Message> retrieveMessage, UUID targetUUID,
        EmojiUnion reactionEmote) {
        // Not implementing
    }

    @Override
    public void runMcCommand(String cmd, CompletableFuture<InteractionHook> cmdMsg, User user) {
        // Not implementing
    }

    @Override
    public HashMap<UUID, String> getPlayers() {
        // TODO: This should probably be cached somewhere
        final HashMap<UUID, String> players = new HashMap<>();

        for (final EntityPlayerMP player : MinecraftServer.getServer()
            .getConfigurationManager().playerEntityList) {
            players.put(player.getUniqueID(), player.getDisplayName());
        }

        return players;
    }

    @Override
    public boolean isOnlineMode() {
        return MinecraftServer.getServer()
            .isServerInOnlineMode();
    }

    @Override
    public String getNameFromUUID(UUID uuid) {
        // TODO: there has to be a better way
        // TODO: if not, we should cache this ourselves
        for (GameProfile profile : MinecraftServer.getServer()
            .getConfigurationManager()
            .func_152600_g()) {
            if (profile.getId() == uuid) {
                return profile.getName();
            }
        }

        return null;
    }

    @Override
    public String getLoaderName() {
        return "Forge";
    }

    @Override
    public boolean playerHasPermissions(UUID player, String... permissions) {
        // TODO: ServerUtilities integration
        return true;
    }
}
