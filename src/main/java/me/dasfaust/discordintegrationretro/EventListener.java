package me.dasfaust.discordintegrationretro;

import java.util.Date;
import java.util.UUID;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AchievementEvent;

import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.WorkThread;
import de.erdbeerbaerlp.dcintegration.common.storage.CommandRegistry;
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.storage.Localization;
import de.erdbeerbaerlp.dcintegration.common.storage.linking.LinkManager;
import de.erdbeerbaerlp.dcintegration.common.util.DiscordMessage;
import de.erdbeerbaerlp.dcintegration.common.util.MessageUtils;
import de.erdbeerbaerlp.dcintegration.common.util.TextColors;

public class EventListener {

    public void onServerStarted(FMLServerStartedEvent event) {
        CommandRegistry.registerDefaultCommands();
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
            DiscordIntegrationRetro.LOG.info("Stopping Discord bot...");
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
        }
    }

    public void onServerStopped(FMLServerStoppedEvent event) {
        if (DiscordIntegration.INSTANCE != null) {
            DiscordIntegration.INSTANCE.stopThreads();
            DiscordIntegration.INSTANCE.kill(false);
            DiscordIntegration.INSTANCE = null;
            DiscordIntegrationRetro.LOG.info("Bot stopped!");
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.isCanceled() || event.player == null) {
            return;
        }

        if (DiscordIntegration.INSTANCE != null) {
            UUID playerUuid = event.player.getGameProfile()
                .getId();
            String playerName = event.player.getGameProfile()
                .getName();

            if (LinkManager.isPlayerLinked(playerUuid)
                && LinkManager.getLink(null, playerUuid).settings.hideFromDiscord) {
                return;
            }

            LinkManager.checkGlobalAPI(playerUuid);
            if (!Localization.instance().playerJoin.isEmpty()) {
                if (Configuration.instance().embedMode.enabled
                    && Configuration.instance().embedMode.playerJoinMessage.asEmbed) {
                    final String avatarURL = Configuration.instance().webhook.playerAvatarURL
                        .replace("%uuid%", playerUuid.toString())
                        .replace(
                            "%uuid_dashless%",
                            playerUuid.toString()
                                .replace("-", ""))
                        .replace("%name%", playerName)
                        .replace(
                            "%randomUUID%",
                            UUID.randomUUID()
                                .toString());
                    if (!Configuration.instance().embedMode.playerJoinMessage.customJSON.isEmpty()) {
                        final EmbedBuilder b = Configuration.instance().embedMode.playerJoinMessage.toEmbedJson(
                            Configuration.instance().embedMode.playerJoinMessage.customJSON
                                .replace("%uuid%", playerUuid.toString())
                                .replace(
                                    "%uuid_dashless%",
                                    playerUuid.toString()
                                        .replace("-", ""))
                                .replace("%name%", playerName)
                                .replace(
                                    "%randomUUID%",
                                    UUID.randomUUID()
                                        .toString())
                                .replace("%avatarURL%", avatarURL)
                                .replace(
                                    "%playerColor%",
                                    "" + TextColors.generateFromUUID(playerUuid)
                                        .getRGB()));
                        DiscordIntegration.INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    } else {
                        final EmbedBuilder b = Configuration.instance().embedMode.playerJoinMessage.toEmbed();
                        b.setAuthor(playerName, null, avatarURL)
                            .setDescription(Localization.instance().playerJoin.replace("%player%", playerName));
                        DiscordIntegration.INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    }
                } else {
                    DiscordIntegration.INSTANCE
                        .sendMessage(Localization.instance().playerJoin.replace("%player%", playerName));
                }
            }

            WorkThread.executeJob(() -> {
                if (Configuration.instance().linking.linkedRoleID.equals("0")) return;
                if (!LinkManager.isPlayerLinked(playerUuid)) return;
                final Guild guild = DiscordIntegration.INSTANCE.getChannel()
                    .getGuild();
                final Role linkedRole = guild.getRoleById(Configuration.instance().linking.linkedRoleID);
                if (LinkManager.isPlayerLinked(playerUuid)) {
                    final Member member = DiscordIntegration.INSTANCE
                        .getMemberById(LinkManager.getLink(null, playerUuid).discordID);
                    if (!member.getRoles()
                        .contains(linkedRole))
                        guild.addRoleToMember(member, linkedRole)
                            .queue();
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.isCanceled() || event.player == null) {
            return;
        }

        if (DiscordIntegration.INSTANCE != null) {
            if (Localization.instance().playerLeave.isEmpty()) {
                return;
            }

            UUID playerUuid = event.player.getGameProfile()
                .getId();
            String playerName = event.player.getGameProfile()
                .getName();

            final String avatarURL = Configuration.instance().webhook.playerAvatarURL
                .replace("%uuid%", playerUuid.toString())
                .replace(
                    "%uuid_dashless%",
                    playerUuid.toString()
                        .replace("-", ""))
                .replace("%name%", playerName)
                .replace(
                    "%randomUUID%",
                    UUID.randomUUID()
                        .toString());

            if (Configuration.instance().embedMode.enabled
                && Configuration.instance().embedMode.playerLeaveMessages.asEmbed) {
                if (!Configuration.instance().embedMode.playerLeaveMessages.customJSON.isEmpty()) {
                    final EmbedBuilder b = Configuration.instance().embedMode.playerLeaveMessages.toEmbedJson(
                        Configuration.instance().embedMode.playerLeaveMessages.customJSON
                            .replace("%uuid%", playerUuid.toString())
                            .replace(
                                "%uuid_dashless%",
                                playerUuid.toString()
                                    .replace("-", ""))
                            .replace("%name%", playerName)
                            .replace(
                                "%randomUUID%",
                                UUID.randomUUID()
                                    .toString())
                            .replace("%avatarURL%", avatarURL)
                            .replace(
                                "%playerColor%",
                                "" + TextColors.generateFromUUID(playerUuid)
                                    .getRGB()));
                    DiscordIntegration.INSTANCE.sendMessage(new DiscordMessage(b.build()));
                } else {
                    EmbedBuilder b = Configuration.instance().embedMode.playerLeaveMessages.toEmbed();
                    b = b.setAuthor(playerName, null, avatarURL)
                        .setDescription(Localization.instance().playerLeave.replace("%player%", playerName));
                    DiscordIntegration.INSTANCE.sendMessage(new DiscordMessage(b.build()));
                }
            } else {
                DiscordIntegration.INSTANCE
                    .sendMessage(Localization.instance().playerLeave.replace("%player%", playerName));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChat(ServerChatEvent event) {
        if (event.isCanceled() || event.player == null) {
            return;
        }

        if (DiscordIntegration.INSTANCE != null) {
            if (Localization.instance().discordChatMessage.isEmpty()) {
                return;
            }

            // NoSuchMethodError if getGameProfile() is used on EntityPlayerMP...
            EntityPlayer player = event.player;
            UUID playerUuid = player.getGameProfile()
                .getId();
            String playerName = player.getGameProfile()
                .getName();

            // TODO: permissions
            /*
             * if (!DiscordIntegration.INSTANCE.getServerInterface().playerHasPermissions(event.player.getUniqueID(),
             * MinecraftPermission.SEND_MESSAGES, MinecraftPermission.USER)) {
             * return;
             * }
             */

            if (LinkManager.isPlayerLinked(playerUuid)
                && LinkManager.getLink(null, playerUuid).settings.hideFromDiscord) {
                return;
            }

            // TODO: ItemStacks? Can you even link items in chat in 1.7.10?
            String text = MessageUtils.escapeMarkdown(
                event.message.replace("@everyone", "[at]everyone")
                    .replace("@here", "[at]here"));

            GuildMessageChannel channel = DiscordIntegration.INSTANCE
                .getChannel(Configuration.instance().advanced.chatOutputChannelID);
            if (channel == null) {
                return;
            }

            if (!Localization.instance().discordChatMessage.isEmpty()) {
                if (Configuration.instance().embedMode.enabled
                    && Configuration.instance().embedMode.chatMessages.asEmbed) {
                    final String avatarURL = Configuration.instance().webhook.playerAvatarURL
                        .replace("%uuid%", playerUuid.toString())
                        .replace(
                            "%uuid_dashless%",
                            playerUuid.toString()
                                .replace("-", ""))
                        .replace("%name%", playerName)
                        .replace(
                            "%randomUUID%",
                            UUID.randomUUID()
                                .toString());
                    if (!Configuration.instance().embedMode.chatMessages.customJSON.isEmpty()) {
                        DiscordIntegrationRetro.LOG.info("Sending custom embed");
                        final EmbedBuilder b = Configuration.instance().embedMode.chatMessages.toEmbedJson(
                            Configuration.instance().embedMode.chatMessages.customJSON
                                .replace("%uuid%", playerUuid.toString())
                                .replace(
                                    "%uuid_dashless%",
                                    playerUuid.toString()
                                        .replace("-", ""))
                                .replace("%name%", playerName)
                                .replace("%randomUUID%", playerUuid.toString())
                                .replace("%avatarURL%", avatarURL)
                                .replace("%msg%", text)
                                .replace(
                                    "%playerColor%",
                                    "" + TextColors.generateFromUUID(playerUuid)
                                        .getRGB()));
                        DiscordIntegration.INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    } else {
                        EmbedBuilder b = Configuration.instance().embedMode.chatMessages.toEmbed();
                        if (Configuration.instance().embedMode.chatMessages.generateUniqueColors) {
                            b = b.setColor(TextColors.generateFromUUID(playerUuid));
                        }
                        b = b.setAuthor(playerName, null, avatarURL)
                            .setDescription(text);
                        DiscordIntegration.INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    }
                } else {
                    DiscordIntegration.INSTANCE
                        .sendMessage(playerName, playerUuid.toString(), new DiscordMessage(null, text, true), channel);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDeath(LivingDeathEvent event) {
        if (event.isCanceled() || event.entity == null) {
            return;
        }

        if (event.entity instanceof EntityPlayer player) {
            if (DiscordIntegration.INSTANCE != null) {
                if (Localization.instance().playerDeath.isEmpty()) {
                    return;
                }

                UUID playerUuid = player.getGameProfile()
                    .getId();
                String playerName = player.getGameProfile()
                    .getName();

                if (LinkManager.isPlayerLinked(playerUuid)
                    && LinkManager.getLink(null, playerUuid).settings.hideFromDiscord) {
                    return;
                }

                IChatComponent deathMessage = player.func_110142_aN()
                    .func_151521_b();
                if (Configuration.instance().embedMode.enabled
                    && Configuration.instance().embedMode.deathMessage.asEmbed) {
                    final String avatarURL = Configuration.instance().webhook.playerAvatarURL
                        .replace("%uuid%", playerUuid.toString())
                        .replace(
                            "%uuid_dashless%",
                            playerUuid.toString()
                                .replace("-", ""))
                        .replace("%name%", playerName)
                        .replace(
                            "%randomUUID%",
                            UUID.randomUUID()
                                .toString());
                    if (!Configuration.instance().embedMode.deathMessage.customJSON.isEmpty()) {
                        final EmbedBuilder b = Configuration.instance().embedMode.deathMessage.toEmbedJson(
                            Configuration.instance().embedMode.deathMessage.customJSON
                                .replace("%uuid%", playerUuid.toString())
                                .replace(
                                    "%uuid_dashless%",
                                    playerUuid.toString()
                                        .replace("-", ""))
                                .replace("%name%", playerName)
                                .replace(
                                    "%randomUUID%",
                                    UUID.randomUUID()
                                        .toString())
                                .replace("%avatarURL%", avatarURL)
                                .replace(
                                    "%deathMessage%",
                                    deathMessage.getUnformattedText()
                                        .replace(playerName + " ", ""))
                                .replace(
                                    "%playerColor%",
                                    "" + TextColors.generateFromUUID(playerUuid)
                                        .getRGB()));
                        DiscordIntegration.INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    } else {
                        final EmbedBuilder b = Configuration.instance().embedMode.deathMessage.toEmbed();
                        b.setDescription(
                            ":skull: " + Localization.instance().playerDeath.replace("%player%", playerName)
                                .replace(
                                    "%msg%",
                                    deathMessage.getUnformattedText()
                                        .replace(playerName + " ", "")));
                        DiscordIntegration.INSTANCE.sendMessage(
                            new DiscordMessage(b.build()),
                            DiscordIntegration.INSTANCE.getChannel(Configuration.instance().advanced.deathsChannelID));
                    }
                } else {
                    DiscordIntegration.INSTANCE.sendMessage(
                        new DiscordMessage(
                            null,
                            Localization.instance().playerDeath.replace("%player%", playerName)
                                .replace(
                                    "%msg%",
                                    deathMessage.getUnformattedText()
                                        .replace(playerName + " ", ""))),
                        DiscordIntegration.INSTANCE.getChannel(Configuration.instance().advanced.deathsChannelID));
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerAchievement(AchievementEvent event) {
        if (event.isCanceled()) {
            return;
        }

        // So grabbing the stats file directly from the player and calling canUnlockAchievement or
        // hasAchievementUnlocked results in a NoSuchMethodError
        // Or maybe it was using StatisticsFile instead of StatFileWriter, but anyway, this works
        // Crazy stuff
        StatFileWriter file = ((EntityPlayerMP) event.entityPlayer).mcServer.getConfigurationManager()
            .func_152602_a(event.entityPlayer);

        if (!file.canUnlockAchievement(event.achievement) || file.hasAchievementUnlocked(event.achievement)) {
            return;
        }

        if (DiscordIntegration.INSTANCE != null) {
            if (Localization.instance().advancementMessage.isEmpty()) {
                return;
            }

            UUID playerUuid = event.entityPlayer.getGameProfile()
                .getId();
            String playerName = event.entityPlayer.getGameProfile()
                .getName();

            if (LinkManager.isPlayerLinked(playerUuid)
                && LinkManager.getLink(null, playerUuid).settings.hideFromDiscord) {
                return;
            }

            Achievement achievement = event.achievement;
            IChatComponent achievementMessage = achievement.func_150951_e();
            String achievementDescription = StatCollector
                .translateToLocalFormatted(achievement.achievementDescription, "KEY");

            if (Configuration.instance().embedMode.enabled
                && Configuration.instance().embedMode.advancementMessage.asEmbed) {
                final String avatarURL = Configuration.instance().webhook.playerAvatarURL
                    .replace("%uuid%", playerUuid.toString())
                    .replace(
                        "%uuid_dashless%",
                        playerUuid.toString()
                            .replace("-", ""))
                    .replace("%name%", playerName)
                    .replace(
                        "%randomUUID%",
                        UUID.randomUUID()
                            .toString());
                if (!Configuration.instance().embedMode.advancementMessage.customJSON.isEmpty()) {
                    final EmbedBuilder b = Configuration.instance().embedMode.advancementMessage.toEmbedJson(
                        Configuration.instance().embedMode.advancementMessage.customJSON
                            .replace("%uuid%", playerUuid.toString())
                            .replace(
                                "%uuid_dashless%",
                                playerUuid.toString()
                                    .replace("-", ""))
                            .replace("%name%", playerName)
                            .replace(
                                "%randomUUID%",
                                UUID.randomUUID()
                                    .toString())
                            .replace("%avatarURL%", avatarURL)
                            .replace("%advName%", achievementMessage.getUnformattedText())
                            .replace("%advDesc%", achievementDescription)
                            .replace("%avatarURL%", avatarURL)
                            .replace(
                                "%playerColor%",
                                "" + TextColors.generateFromUUID(playerUuid)
                                    .getRGB()));
                    DiscordIntegration.INSTANCE.sendMessage(new DiscordMessage(b.build()));
                } else {
                    EmbedBuilder b = Configuration.instance().embedMode.advancementMessage.toEmbed();
                    b = b.setAuthor(playerName, null, avatarURL)
                        .setDescription(
                            Localization.instance().advancementMessage.replace("%player%", playerName)
                                .replace("%advName%", achievementMessage.getUnformattedText())
                                .replace("%advDesc%", achievementDescription)
                                .replace("\\n", "\n"));
                    DiscordIntegration.INSTANCE.sendMessage(new DiscordMessage(b.build()));
                }
            } else DiscordIntegration.INSTANCE.sendMessage(
                Localization.instance().advancementMessage.replace("%player%", playerName)
                    .replace("%advName%", achievementMessage.getUnformattedText())
                    .replace("%advDesc%", achievementDescription)
                    .replace("\\n", "\n"));
        }
    }
}
