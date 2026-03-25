package com.bluedev.blockduplicatortree.registry;

import com.bluedev.blockduplicatortree.BlockDuplicatorTreeMod;
import com.bluedev.blockduplicatortree.world.DuplicatorTreeFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
        DeferredRegister.create(ForgeRegistries.FEATURES, BlockDuplicatorTreeMod.MODID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DUPLICATOR_TREE =
        FEATURES.register("duplicator_tree", DuplicatorTreeFeature::new);

    private ModFeatures() {
    }

    public static void register(BusGroup modBusGroup) {
        FEATURES.register(modBusGroup);
    }
}
