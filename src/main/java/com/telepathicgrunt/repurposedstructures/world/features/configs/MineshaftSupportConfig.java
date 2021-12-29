package com.telepathicgrunt.repurposedstructures.world.features.configs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class MineshaftSupportConfig implements FeatureConfiguration {

    public static final Codec<MineshaftSupportConfig> CODEC = RecordCodecBuilder.create((configInstance) -> configInstance.group(
            Registry.BLOCK.byNameCodec().fieldOf("arch_block").forGetter(mineshaftSupportConfig -> mineshaftSupportConfig.archBlock),
            BlockState.CODEC.fieldOf("pillar_state").forGetter(mineshaftSupportConfig -> mineshaftSupportConfig.pillarState),
            BlockState.CODEC.fieldOf("fence_state").forGetter(mineshaftSupportConfig -> mineshaftSupportConfig.fenceState),
            Registry.BLOCK.byNameCodec().fieldOf("target_floor_block").forGetter(mineshaftSupportConfig -> mineshaftSupportConfig.targetFloorState),
            Codec.BOOL.fieldOf("is_water_based").orElse(false).forGetter(mineshaftSupportConfig -> mineshaftSupportConfig.waterBased)
            ).apply(configInstance, MineshaftSupportConfig::new));

    public final Block archBlock;
    public final BlockState pillarState;
    public final BlockState fenceState;
    public final Block targetFloorState;
    public final boolean waterBased;

    public MineshaftSupportConfig(Block archBlock, BlockState pillarState, BlockState fenceState, Block targetFloorState, boolean waterBased) {
        this.archBlock = archBlock;
        this.pillarState = pillarState;
        this.fenceState = fenceState;
        this.targetFloorState = targetFloorState;
        this.waterBased = waterBased;
    }
}
