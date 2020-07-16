package com.telepathicgrunt.repurposedstructures.mixin;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.telepathicgrunt.repurposedstructures.RSFeatures;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.SurfaceChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SurfaceChunkGenerator.class)
public class StructureMobSpawningMixin {

    @Inject(
            method = "getEntitySpawnList(Lnet/minecraft/world/biome/Biome;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/util/math/BlockPos;)Ljava/util/List;",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void locateRSStrongholds(Biome biome, StructureAccessor accessor, SpawnGroup group, BlockPos pos, CallbackInfoReturnable<List<Biome.SpawnEntry>> cir) {
        List<Biome.SpawnEntry> list = getStructureSpawns(biome, accessor, group, pos);
        if(list != null) cir.setReturnValue(list);
    }


    private static List<Biome.SpawnEntry>  getStructureSpawns(Biome biome, StructureAccessor accessor, SpawnGroup group, BlockPos pos){
        if (group == SpawnGroup.MONSTER) {
            if (accessor.method_28388(pos, true, RSFeatures.NETHER_BRICK_OUTPOST).hasChildren()) {
               return RSFeatures.NETHER_BRICK_OUTPOST.getMonsterSpawns();
            }

            if (accessor.method_28388(pos, true, RSFeatures.WARPED_OUTPOST).hasChildren()) {
                return RSFeatures.WARPED_OUTPOST.getMonsterSpawns();
            }

            if (accessor.method_28388(pos, true, RSFeatures.CRIMSON_OUTPOST).hasChildren()) {
                return RSFeatures.CRIMSON_OUTPOST.getMonsterSpawns();
            }


            if (accessor.method_28388(pos, true, RSFeatures.NETHER_STRONGHOLD).hasChildren()) {
                return RSFeatures.NETHER_STRONGHOLD.getMonsterSpawns();
            }

            if (accessor.method_28388(pos, true, RSFeatures.JUNGLE_FORTRESS).hasChildren()) {
                return Lists.newArrayList(Iterators.concat(biome.getEntitySpawnList(SpawnGroup.MONSTER).iterator(), RSFeatures.JUNGLE_FORTRESS.getMonsterSpawns().iterator()));
            }

            if (accessor.method_28388(pos, true, RSFeatures.END_MINESHAFT).hasChildren()) {
                return Lists.newArrayList(Iterators.concat(biome.getEntitySpawnList(SpawnGroup.MONSTER).iterator(), RSFeatures.END_MINESHAFT.getMonsterSpawns().iterator()));
            }
        }

        return null;
    }
}