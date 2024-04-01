package me.dasfaust.discordintegration;

import java.util.Date;
import java.util.UUID;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.minecraftforge.event.ServerChatEvent;

import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.storage.Localization;
import de.erdbeerbaerlp.dcintegration.common.storage.linking.LinkManager;
import de.erdbeerbaerlp.dcintegration.common.util.DiscordMessage;
import de.erdbeerbaerlp.dcintegration.common.util.MessageUtils;
import de.erdbeerbaerlp.dcintegration.common.util.TextColors;

public class EventListener {

    public void onServerStarted(FMLServerStartedEvent event) {
        DiscordIntegration.started = new Date().getTime();
        if (DiscordIntegration.INSTANCE != null) {
            if (DiscordIntegration.startingMsg != null) {
                if (Configuration.instance().embedMode.enabled
                    && Configuration.instance().embedMode.startMessages.asEmbed) {
                    if (!Configuration.instance().embedMode.startMessages.customJSON.isEmpty()) {
                        final EmbedBuilder b = Configuration.instance().embedMode.startMessages
                            .toEmbedJson(Configuration.instance().embedMode.startMessages.customJSON);
                        DiscordIntegration.startingMsg.thenAccept(
                            (a) -> a.editMessageEmbeds(b.build())
                                .queue());
                    } else {
                        DiscordIntegration.startingMsg.thenAccept(
                            (a) -> a.editMessageEmbeds(
                                Configuration.instance().embedMode.startMessages.toEmbed()
                                    .setDescription(Localization.instance().serverStarted)
                                    .build())
                                .queue());
                    }
                } else {
                    DiscordIntegration.startingMsg.thenAccept(
                        (a) -> a.editMessage(Localization.instance().serverStarted)
                            .queue());
                }
            } else {
                if (Configuration.instance().embedMode.enabled
                    && Configuration.instance().embedMode.startMessages.asEmbed) {
                    if (!Configuration.instance().embedMode.startMessages.customJSON.isEmpty()) {
                        final EmbedBuilder b = Configuration.instance().embedMode.startMessages
                            .toEmbedJson(Configuration.instance().embedMode.startMessages.customJSON);
                        DiscordIntegration.INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    } else {
                        DiscordIntegration.INSTANCE.sendMessage(
                            new DiscordMessage(
                                Configuration.instance().embedMode.startMessages.toEmbed()
                                    .setDescription(Localization.instance().serverStarted)
                                    .build()));
                    }
                } else {
                    DiscordIntegration.INSTANCE.sendMessage(Localization.instance().serverStarted);
                }
            }
            DiscordIntegration.INSTANCE.startThreads();
        }
    }

    public void onServerStopping(FMLServerStoppingEvent event) {
        if (DiscordIntegration.INSTANCE != null) {
            DiscordIntegrationMod.LOG.info("Stopping Discord bot...");
            if (!Localization.instance().serverStopped.isEmpty()) {
                if (Configuration.instance().embedMode.enabled
                    && Configuration.instance().embedMode.stopMessages.asEmbed) {
                    if (!Configuration.instance().embedMode.stopMessages.customJSON.isEmpty()) {
                        final EmbedBuilder b = Configuration.instance().embedMode.stopMessages
                            .toEmbedJson(Configuration.instance().embedMode.stopMessages.customJSON);
                        DiscordIntegration.INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    } else {
                        DiscordIntegration.INSTANCE.sendMessage(
                            new DiscordMessage(
                                Configuration.instance().embedMode.stopMessages.toEmbed()
                                    .setDescription(Localization.instance().serverStopped)
                                    .build()));
                    }
                } else {
                    DiscordIntegration.INSTANCE.sendMessage(Localization.instance().serverStopped);
                }
            }
            DiscordIntegration.INSTANCE.stopThreads();
            DiscordIntegration.INSTANCE.kill(false);
            DiscordIntegration.INSTANCE = null;
            DiscordIntegrationMod.LOG.info("Bot stopped!");
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // TODO
        DiscordIntegrationMod.LOG.info("PlayerLoggedInEvent: " + event.player.getDisplayName());
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        if (Localization.instance().discordChatMessage.isEmpty()) {
            return;
        }

        // TODO: permissions
        /*
         * if (!DiscordIntegration.INSTANCE.getServerInterface().playerHasPermissions(event.player.getUniqueID(),
         * MinecraftPermission.SEND_MESSAGES, MinecraftPermission.USER)) {
         * return;
         * }
         */

        if (LinkManager.isPlayerLinked(event.player.getUniqueID())
            && LinkManager.getLink(null, event.player.getUniqueID()).settings.hideFromDiscord) {
            return;
        }

        // TODO: ItemStacks? Can you even link items in chat in 1.7.10?
        String text = MessageUtils.escapeMarkdown(
            event.message.replace("@everyone", "[at]everyone")
                .replace("@here", "[at]here"));

        if (DiscordIntegration.INSTANCE != null) {
            GuildMessageChannel channel = DiscordIntegration.INSTANCE
                .getChannel(Configuration.instance().advanced.chatOutputChannelID);
            if (channel == null) {
                return;
            }

            if (!Localization.instance().discordChatMessage.isEmpty()) {
                if (Configuration.instance().embedMode.enabled
                    && Configuration.instance().embedMode.chatMessages.asEmbed) {
                    final String avatarURL = Configuration.instance().webhook.playerAvatarURL
                        .replace(
                            "%uuid%",
                            event.player.getUniqueID()
                                .toString())
                        .replace(
                            "%uuid_dashless%",
                            event.player.getUniqueID()
                                .toString()
                                .replace("-", ""))
                        .replace("%name%", event.player.getDisplayName())
                        .replace(
                            "%randomUUID%",
                            UUID.randomUUID()
                                .toString());
                    if (!Configuration.instance().embedMode.chatMessages.customJSON.isEmpty()) {
                        DiscordIntegrationMod.LOG.info("Sending custom embed");
                        final EmbedBuilder b = Configuration.instance().embedMode.chatMessages.toEmbedJson(
                            Configuration.instance().embedMode.chatMessages.customJSON
                                .replace(
                                    "%uuid%",
                                    event.player.getUniqueID()
                                        .toString())
                                .replace(
                                    "%uuid_dashless%",
                                    event.player.getUniqueID()
                                        .toString()
                                        .replace("-", ""))
                                .replace("%name%", event.player.getDisplayName())
                                .replace(
                                    "%randomUUID%",
                                    UUID.randomUUID()
                                        .toString())
                                .replace("%avatarURL%", avatarURL)
                                .replace("%msg%", text)
                                .replace(
                                    "%playerColor%",
                                    "" + TextColors.generateFromUUID(event.player.getUniqueID())
                                        .getRGB()));
                        DiscordIntegration.INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    } else {
                        DiscordIntegrationMod.LOG.info("Sending non-custom embed");
                        EmbedBuilder b = Configuration.instance().embedMode.chatMessages.toEmbed();
                        if (Configuration.instance().embedMode.chatMessages.generateUniqueColors) {
                            b = b.setColor(TextColors.generateFromUUID(event.player.getUniqueID()));
                        }
                        b = b.setAuthor(event.player.getDisplayName(), null, avatarURL)
                            .setDescription(text);
                        DiscordIntegration.INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    }
                } else {
                    DiscordIntegrationMod.LOG.info("Embeds not enabled, sending message");
                    DiscordIntegration.INSTANCE.sendMessage(
                        event.player.getDisplayName(),
                        event.player.getUniqueID()
                            .toString(),
                        new DiscordMessage(null, text, true),
                        channel);
                }
            }
        }
    }
}
