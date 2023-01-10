package net.fill1890.fabsit.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fill1890.fabsit.FabSit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.world.World;

public class ChairEntity extends Entity {
    public static final String ENTITY_ID = "entity_chair";
    public static final EntityDimensions DIMENSIONS = EntityDimensions.fixed(0.01F, 1.00F);

    public ChairEntity(EntityType<ChairEntity> type, World world) {
        super(type, world);
    }

    public static EntityType<ChairEntity> register() {
        return Registry.register(
                Registries.ENTITY_TYPE,
                new Identifier(FabSit.MOD_ID, ENTITY_ID),
                FabricEntityTypeBuilder.create(SpawnGroup.MISC, ChairEntity::new).dimensions(DIMENSIONS).build()
        );
    }

    @Override
    protected void initDataTracker() {}

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {}

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {}

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
