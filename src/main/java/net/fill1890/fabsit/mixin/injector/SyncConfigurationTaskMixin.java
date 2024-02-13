package net.fill1890.fabsit.mixin.injector;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fill1890.fabsit.FabSit;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.entity.ChairEntity;
import net.fill1890.fabsit.mixin.accessor.ServerCommonNetworkHandlerAccessor;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.util.Identifier;
import net.yukulab.fabsit.entity.FabSitEntities;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Hijack registry sync manager to remove entites
 */
@SuppressWarnings("UnstableApiUsage")
@Mixin(RegistrySyncManager.SyncConfigurationTask.class)
public abstract class SyncConfigurationTaskMixin {
    @Shadow
    @Final
    private ServerConfigurationNetworkHandler handler;
    @Shadow
    @Final
    private Map<Identifier, Object2IntMap<Identifier>> map;

    /**
     * Scrub registry if needed to remove custom fabsit entities for compatibility
     * <br>
     * Vanilla clients don't like having unknown entities, and clients with the Fabric API without fabsit will crash if
     * they receive unknown registry IDs
     * <br>
     * Injects just before sending the registry sync packet, checks if client has fabsit, and scrubs if not
     */
    @Inject(
            method = "sendPacket",
            at = @At("HEAD")
    )
    private void removeFromSync(Consumer<Packet<?>> sender, CallbackInfo ci) {
        // if client does not have fabsit
        var connection = ((ServerCommonNetworkHandlerAccessor) handler).getConnection();
        if (!ConfigManager.loadedPlayers.contains(connection.getAddress())) {

            var id = RegistryKeys.ENTITY_TYPE.getValue();
            // scrub entities from the syncing registry
            map.get(id).removeInt(new Identifier(FabSit.MOD_ID, ChairEntity.ENTITY_ID));
            map.get(id).removeInt(Registries.ENTITY_TYPE.getKey(FabSitEntities.POSE_MANAGER));
        }
    }

}
