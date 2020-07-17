package com.telepathicgrunt.repurposedstructures.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.repurposedstructures.RepurposedStructures;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.Structure;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

import java.util.Random;


public class WellMossyStone extends WellAbstract {
    private static final float ORE_CHANCE = 0.12f;
    private static final Identifier MOSSY_WELL_ORE_RL = new Identifier("repurposed_structures:mossy_well_ores");
    private static final Identifier MOSSY_WELL_RL = new Identifier(RepurposedStructures.MODID + ":wells/mossy");

    public WellMossyStone(Codec<DefaultFeatureConfig> config) {
        super(config);
    }


    public boolean generate(ServerWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockPos position, DefaultFeatureConfig config) {
        // move to top land block below position
        BlockPos.Mutable mutable = new BlockPos.Mutable().set(position);
        for (mutable.move(Direction.UP); (world.isAir(mutable) || !world.getFluidState(mutable).isEmpty()) && mutable.getY() > 2; ) {
            mutable.move(Direction.DOWN);
        }

        // check to make sure spot is valid and not a single block ledge
        BlockState block = world.getBlockState(mutable);
        if ((BlockTags.SAND.contains(block.getBlock()) || block.isOf(Blocks.CLAY) || isDirt(block.getBlock()))
                && (!world.isAir(mutable.down()) || !world.isAir(mutable.down(2)))) {
            // Creates the well centered on our spot
            mutable.move(Direction.DOWN);
            Structure template = this.generateTemplate(MOSSY_WELL_RL, world, random, mutable);
            this.handleDataBlocks(MOSSY_WELL_ORE_RL, template, world, random, mutable, Blocks.COBBLESTONE, ORE_CHANCE);

            // turns some of the stony blocks into mossy versions and waterlogs blocks below sealevel
            BlockPos offset = new BlockPos(-template.getSize().getX() / 2, 0, -template.getSize().getZ() / 2);
            BlockPos.stream(template
                    .calculateBoundingBox(this.placementsettings, mutable.add(offset)))
                    .forEach(pos -> mossifyBlocks(world, random, pos));
            BlockPos.stream(template
                    .calculateBoundingBox(this.placementsettings, mutable.add(offset)))
                    .forEach(pos -> waterlogBlocks(world, pos));

            return true;
        }

        return false;
    }

    private static void mossifyBlocks(ServerWorldAccess world, Random random, BlockPos position) {
        BlockState block = world.getBlockState(position);
        if (block.isOf(Blocks.STONE_BRICKS) && random.nextFloat() < 0.6f) {
            world.setBlockState(position, Blocks.MOSSY_STONE_BRICKS.getDefaultState(), 2);
        } else if (block.isOf(Blocks.STONE_BRICK_WALL) && random.nextFloat() < 0.6f) {
            world.setBlockState(position, Blocks.MOSSY_STONE_BRICK_WALL.getDefaultState(), 2);
        } else if (block.isOf(Blocks.STONE_BRICK_SLAB) && random.nextFloat() < 0.6f) {
            world.setBlockState(position, Blocks.MOSSY_STONE_BRICK_SLAB.getDefaultState(), 2);
        } else if (block.isOf(Blocks.COBBLESTONE) && random.nextFloat() < 0.5f) {
            world.setBlockState(position, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 2);
        }
    }

    private static void waterlogBlocks(ServerWorldAccess world, BlockPos position) {
        BlockState blockstate = world.getBlockState(position);
        if (position.getY() < world.getSeaLevel() && blockstate.contains(Properties.WATERLOGGED)) {
            world.setBlockState(position, blockstate.with(Properties.WATERLOGGED, true), 2);
        }
    }
}