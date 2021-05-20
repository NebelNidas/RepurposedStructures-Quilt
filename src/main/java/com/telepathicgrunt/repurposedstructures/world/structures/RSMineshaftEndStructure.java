package com.telepathicgrunt.repurposedstructures.world.structures;

import com.google.common.collect.Lists;
import com.telepathicgrunt.repurposedstructures.RepurposedStructures;
import com.telepathicgrunt.repurposedstructures.world.structures.pieces.StructurePiecesBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

import java.util.List;
import java.util.Map;


public class RSMineshaftEndStructure extends RSMineshaftStructure {
    public RSMineshaftEndStructure(Identifier poolID, int structureSize, Map<Identifier, StructurePiecesBehavior.RequiredPieceNeeds> requiredPieces, int maxY, int minY, float probability, ENVIRONMENT_CHECK environmentCheck) {
        super(poolID, structureSize, requiredPieces, maxY, minY, probability, environmentCheck);
    }

    private static final List<SpawnSettings.SpawnEntry> MONSTER_SPAWNS = Lists.newArrayList(
        new SpawnSettings.SpawnEntry(EntityType.ENDERMITE, 10, 2, 5),
        new SpawnSettings.SpawnEntry(EntityType.ENDERMAN, 5, 1, 3)
    );

    @Override
    public List<SpawnSettings.SpawnEntry> getMonsterSpawns() {
        return MONSTER_SPAWNS;
    }

    @Override
    protected boolean shouldStartAt(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long seed, ChunkRandom chunkRandom, int x, int z, Biome biome, ChunkPos chunkPos, DefaultFeatureConfig featureConfig) {
        StructureConfig structureConfig = chunkGenerator.getStructuresConfig().getForType(this);
        if(structureConfig != null) {
            chunkRandom.setCarverSeed(seed + structureConfig.getSalt(), x, z);
            double d = (probability / 10000D);
            if (chunkRandom.nextDouble() < d) {
                if(RepurposedStructures.RSAllConfig.RSMineshaftsConfig.misc.barrensIslandsEndMineshafts)
                    return true;

                int minLandHeight = Math.min(chunkGenerator.getWorldHeight(), 45);
                int xPos = x << 4;
                int zPos = z << 4;
                int landHeight = chunkGenerator.getHeightInGround(xPos, zPos, Heightmap.Type.WORLD_SURFACE_WG);

                landHeight = Math.min(landHeight, chunkGenerator.getHeightInGround(xPos + 70, zPos, Heightmap.Type.WORLD_SURFACE_WG));
                if(landHeight < minLandHeight) return false;

                landHeight = Math.min(landHeight, chunkGenerator.getHeightInGround(xPos, zPos + 70, Heightmap.Type.WORLD_SURFACE_WG));
                if(landHeight < minLandHeight) return false;

                landHeight = Math.min(landHeight, chunkGenerator.getHeightInGround(xPos - 70, zPos, Heightmap.Type.WORLD_SURFACE_WG));
                if(landHeight < minLandHeight) return false;

                landHeight = Math.min(landHeight, chunkGenerator.getHeightInGround(xPos, zPos - 70, Heightmap.Type.WORLD_SURFACE_WG));
                return landHeight >= minLandHeight;
            }
        }
        return false;
    }
}
