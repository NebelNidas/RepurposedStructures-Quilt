package com.telepathicgrunt.repurposedstructures.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.repurposedstructures.world.features.configs.StructureTargetAndLengthConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;

import java.util.Random;


public class StructureCrimsonPlants extends Feature<StructureTargetAndLengthConfig> {

    public StructureCrimsonPlants(Codec<StructureTargetAndLengthConfig> config) {
        super(config);
    }


    @Override
    public boolean generate(StructureWorldAccess world, ChunkGenerator chunkGenerator, Random random, BlockPos position, StructureTargetAndLengthConfig config) {

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        BlockState crimsonFungus = Blocks.CRIMSON_FUNGUS.getDefaultState();
        BlockState crimsonRoots = Blocks.CRIMSON_ROOTS.getDefaultState();
        BlockState weepingVines = Blocks.WEEPING_VINES.getDefaultState();
        BlockState weepingVinesPlant = Blocks.WEEPING_VINES_PLANT.getDefaultState();

        for(int i = 0; i < config.attempts; i++){
            mutable.set(position).move(
                    random.nextInt(7) - 3,
                    random.nextInt(4) - 1,
                    random.nextInt(7) - 3
            );

            if(world.getBlockState(mutable).isAir()){
                if(random.nextFloat() < 0.8f && crimsonRoots.canPlaceAt(world, mutable)){
                    // expensive. Do this check very last
                    if(!world.toServerWorld().getStructureAccessor().getStructureAt(mutable, true, config.targetStructure).hasChildren()){
                        continue;
                    }

                    world.setBlockState(mutable, crimsonRoots, 3);
                }
                else if(crimsonFungus.canPlaceAt(world, mutable)){
                    // expensive. Do this check very last
                    if(!world.toServerWorld().getStructureAccessor().getStructureAt(mutable, true, config.targetStructure).hasChildren()){
                        continue;
                    }

                    world.setBlockState(mutable, crimsonFungus, 3);
                }
                else if(weepingVines.canPlaceAt(world, mutable)){
                    // expensive. Do this check very last
                    if(!world.toServerWorld().getStructureAccessor().getStructureAt(mutable, true, config.targetStructure).hasChildren()){
                        continue;
                    }

                    // Biased towards max length if greater than 3
                    int length = config.length > 3 ? config.length - random.nextInt(random.nextInt(config.length) + 1) : random.nextInt(config.length);
                    for(int currentLength = 0; currentLength <= length; currentLength++){
                        if(currentLength == length || !world.getBlockState(mutable.down()).isAir()){
                            world.setBlockState(mutable, weepingVines, 3);
                            break;
                        }
                        world.setBlockState(mutable, weepingVinesPlant, 3);
                        mutable.move(Direction.DOWN);
                    }
                }
            }
        }

        return true;
    }
}