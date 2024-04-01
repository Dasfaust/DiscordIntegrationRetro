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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import com.mojang.authlib.GameProfile;

import dcshadow.net.kyori.adventure.text.Component;
import dcshadow.net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import de.erdbeerbaerlp.dcintegration.common.util.ComponentUtils;
import de.erdbeerbaerlp.dcintegration.common.util.McServerInterface;
import de.erdbeerbaerlp.dcintegration.common.util.MinecraftPermission;

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
        for (final EntityPlayer player : MinecraftServer.getServer()
            .getConfigurationManager().playerEntityList) {
            UUID playerUuid = player.getGameProfile()
                .getId();
            String playerName = player.getGameProfile()
                .getName();

            final Map.Entry<Boolean, Component> ping = ComponentUtils.parsePing(msg, playerUuid, playerName);
            final String jsonComp = GsonComponentSerializer.gson()
                .serialize(ping.getValue())
                .replace("\\\\n", "\n");
            final IChatComponent comp = IChatComponent.Serializer.func_150699_a(jsonComp);

            EntityPlayerMP playerMp = (EntityPlayerMP) player;
            playerMp.addChatMessage(comp);
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
        for (final EntityPlayer player : MinecraftServer.getServer()
            .getConfigurationManager().playerEntityList) {
            if (player.getGameProfile()
                .getId() == playerUuid) {

                EntityPlayerMP playerMp = (EntityPlayerMP) player;
                playerMp.addChatMessage(new ChatComponentText(msg));
                break;
            }
        }
    }

    @Override
    public void sendIngameReaction(Member member, RestAction<Message> retrieveMessage, UUID targetUUID,
        EmojiUnion reactionEmote) {
        // TODO
    }

    @Override
    public void runMcCommand(String cmd, CompletableFuture<InteractionHook> cmdMsg, User user) {
        // TODO
    }

    @Override
    public String runMCCommand(String cmdString) {
        // TODO
        return "";
    }

    @Override
    public HashMap<UUID, String> getPlayers() {
        // TODO: This should probably be cached somewhere
        final HashMap<UUID, String> players = new HashMap<>();

        for (final EntityPlayer player : MinecraftServer.getServer()
            .getConfigurationManager().playerEntityList) {
            UUID playerUuid = player.getGameProfile()
                .getId();
            String playerName = player.getGameProfile()
                .getName();
            players.put(playerUuid, playerName);
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
        GameProfile profile = MinecraftServer.getServer()
            .func_152358_ax()
            .func_152652_a(uuid);
        if (profile != null) {
            return profile.getName();
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

    @Override
    public boolean playerHasPermissions(UUID player, MinecraftPermission... permissions) {
        return McServerInterface.super.playerHasPermissions(player, permissions);
    }
}
