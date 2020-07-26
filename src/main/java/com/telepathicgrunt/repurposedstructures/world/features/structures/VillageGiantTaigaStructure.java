package com.telepathicgrunt.repurposedstructures.world.features.structures;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.repurposedstructures.RSFeatures;
import com.telepathicgrunt.repurposedstructures.RepurposedStructures;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public class VillageGiantTaigaStructure extends AbstractVillageStructure {
	public VillageGiantTaigaStructure(Codec<DefaultFeatureConfig> config) {
		super(config);
	}


	@Override
	public StructureFeature<DefaultFeatureConfig> getVillageInstance() {
		return RSFeatures.GIANT_TAIGA_VILLAGE;
	}

	public StructureFeature.StructureStartFactory<DefaultFeatureConfig> getStructureStartFactory() {
		return VillageGiantTaigaStructure.Start::new;
	}

	public static class Start extends AbstractStart {
		public Start(StructureFeature<DefaultFeatureConfig> structureIn, int chunkX, int chunkZ, BlockBox mutableBoundingBox, int referenceIn, long seedIn) {
			super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
		}

		public static Identifier VILLAGE_IDENTIFIER = new Identifier(RepurposedStructures.MODID + ":village/giant_taiga/town_centers");
		@Override
		public Identifier getIdentifier() {
			return VILLAGE_IDENTIFIER;
		}

		@Override
		public int getSize() {
			return 6;
		}
	}
}