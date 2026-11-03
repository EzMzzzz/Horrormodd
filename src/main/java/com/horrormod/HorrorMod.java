package com.horrormod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class HorrorMod implements ModInitializer {
    public static final String MOD_ID = "horrormod";

    public static final EntityType<StalkerEntity> STALKER = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(MOD_ID, "stalker"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, StalkerEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 2.4f))
                    .trackRangeBlocks(64)
                    .build());

    public static final Item STALKER_SPAWN_EGG = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "stalker_spawn_egg"),
            new SpawnEggItem(STALKER, 0x050505, 0xE8E8E8, new FabricItemSettings()));

    @Override
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(STALKER, StalkerEntity.createStalkerAttributes());
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(e -> e.add(STALKER_SPAWN_EGG));
        ScareManager.init();
        HorrorCommands.register();
    }
}
