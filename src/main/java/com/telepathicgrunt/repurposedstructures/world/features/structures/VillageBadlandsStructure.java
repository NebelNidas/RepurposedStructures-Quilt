package com.telepathicgrunt.repurposedstructures.world.features.structures;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.repurposedstructures.RSFeatures;
import com.telepathicgrunt.repurposedstructures.RepurposedStructures;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public class VillageBadlandsStructure extends AbstractVillageStructure {
    public VillageBadlandsStructure(Codec<DefaultFeatureConfig> config) {
        super(config);
    }

    @Override
    public StructureFeature<DefaultFeatureConfig> getVillageInstance() {
        return RSFeatures.BADLANDS_VILLAGE;
    }

    public StructureFeature.StructureStartFactory<DefaultFeatureConfig> getStructureStartFactory() {
        return VillageBadlandsStructure.Start::new;
    }

    public static class Start extends AbstractStart {
        public Start(StructureFeature<DefaultFeatureConfig> structureIn, int chunkX, int chunkZ, BlockBox mutableBoundingBox, int referenceIn, long seedIn) {
            super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
        }

        public static Identifier VILLAGE_IDENTIFIER = new Identifier(RepurposedStructures.MODID + ":village/badlands/town_centers");
        @Override
        public Identifier getIdentifier() {
            return VILLAGE_IDENTIFIER;
        }

        @Override
        public int getSize() {
            return 10;
        }
    }
}