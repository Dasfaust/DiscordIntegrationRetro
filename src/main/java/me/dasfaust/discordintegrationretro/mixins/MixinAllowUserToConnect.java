package me.dasfaust.discordintegrationretro.mixins;

import java.net.SocketAddress;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.storage.Localization;
import de.erdbeerbaerlp.dcintegration.common.storage.linking.LinkManager;
import me.dasfaust.discordintegrationretro.DiscordIntegrationRetro;

@Mixin(ServerConfigurationManager.class)
public class MixinAllowUserToConnect {

    @Inject(method = "allowUserToConnect", at = @At("HEAD"), cancellable = true)
    public void allowUserToConnect(SocketAddress address, GameProfile profile, CallbackInfoReturnable<String> cir) {
        DiscordIntegrationRetro.LOG.info(profile.getName() + " is attempting to connect");

        if (Configuration.instance().linking.whitelistMode && MinecraftServer.getServer()
            .isServerInOnlineMode()) {
            LinkManager.checkGlobalAPI(profile.getId());

            try {
                if (!LinkManager.isPlayerLinked(profile.getId())) {
                    cir.setReturnValue(
                        Localization.instance().linking.notWhitelistedCode
                            .replace("%code%", "" + LinkManager.genLinkNumber(profile.getId())));
                } else if (!DiscordIntegration.INSTANCE.canPlayerJoin(profile.getId())) {
                    cir.setReturnValue(Localization.instance().linking.notWhitelistedRole);
                }
            } catch (IllegalStateException e) {
                cir.setReturnValue("An error occurred!\nPlease check the server log for more information");
            }
        }
    }
}
