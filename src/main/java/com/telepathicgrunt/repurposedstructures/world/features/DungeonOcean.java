package com.telepathicgrunt.repurposedstructures.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.repurposedstructures.RepurposedStructures;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;


public class DungeonOcean extends Feature<DefaultFeatureConfig> {
    public DungeonOcean(Codec<DefaultFeatureConfig> configFactory) {
        super(configFactory);
    }

    private static final Logger LOGGER = LogManager.getLogger();
    private static final BlockState WATER = Blocks.WATER.getDefaultState();
    private static final BlockState PRISMARINE = Blocks.PRISMARINE.getDefaultState();
    private static final BlockState PRISMARINE_BRICKS = Blocks.PRISMARINE_BRICKS.getDefaultState();
    private static final BlockState PRISMARINE_WALL = Blocks.PRISMARINE_WALL.getDefaultState();
    private static final BlockState DARK_PRISMARINE_STAIRS = Blocks.DARK_PRISMARINE_STAIRS.getDefaultState();
    private static final BlockState DARK_PRISMARINE = Blocks.DARK_PRISMARINE.getDefaultState();
    private static final BlockState MAGMA_BLOCK = Blocks.MAGMA_BLOCK.getDefaultState();
    private static final Identifier CHEST_LOOT = new Identifier(RepurposedStructures.MODID + ":chests/dungeon_ocean");
    private static final Identifier FROZEN_SPAWNER_ID = new Identifier(RepurposedStructures.MODID + ":dungeon_ocean_frozen");
    private static final Identifier COLD_SPAWNER_ID = new Identifier(RepurposedStructures.MODID + ":dungeon_ocean_cold");
    private static final Identifier NEUTRAL_SPAWNER_ID = new Identifier(RepurposedStructures.MODID + ":dungeon_ocean_neutral");
    private static final Identifier LUKEWARM_SPAWNER_ID = new Identifier(RepurposedStructures.MODID + ":dungeon_ocean_lukewarm");
    private static final Identifier WARM_SPAWNER_ID = new Identifier(RepurposedStructures.MODID + ":dungeon_ocean_warm");
    private static final Identifier MISC_SPAWNER_ID = new Identifier(RepurposedStructures.MODID + ":dungeon_ocean_misc");


    // only the mob spawner chance and what blocks the wall cannot replace was changed. Everything else is just the normal dungeon code.

    @Override
    public boolean generate(ServerWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockPos position, DefaultFeatureConfig config) {
        int randXRange = random.nextInt(2) + 2;
        int xMin = -randXRange - 1;
        int xMax = randXRange + 1;
        int randZRange = random.nextInt(2) + 2;
        int zMin = -randZRange - 1;
        int zMax = randZRange + 1;
        int validOpenings = 0;
        int ceilingOpenings = 0;
        boolean validSpot = false;
        boolean oceanFloor = false;
        BlockPos.Mutable blockpos$Mutable = new BlockPos.Mutable().set(position);
        int terrainHeight = world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, blockpos$Mutable.getX(), blockpos$Mutable.getZ());

        // ocean floor
        if (terrainHeight - blockpos$Mutable.getY() > 1 && terrainHeight - blockpos$Mutable.getY() < 5) {
            oceanFloor = true;
        }

        // regular dungeon spacing check
        for (int x = xMin; x <= xMax; ++x) {
            for (int y = -1; y <= 4; ++y) {
                for (int z = zMin; z <= zMax; ++z) {

                    blockpos$Mutable.set(position).move(x, y, z);
                    Material material = world.getBlockState(blockpos$Mutable).getMaterial();
                    boolean flag = material.isSolid() || material == Material.WATER;

                    if (y == -1 && !flag) {
                        return false;
                    }

                    if (y == 4 && !flag) {
                        ceilingOpenings++;
                    }

                    if ((x == xMin || x == xMax || z == zMin || z == zMax) &&
                            y == 0 &&
                            (world.getBlockState(blockpos$Mutable).getMaterial() == Material.WATER &&
                                    world.getBlockState(blockpos$Mutable.up()).getMaterial() == Material.WATER)) {
                        ++validOpenings;
                    }
                }
            }
        }

        validSpot = oceanFloor ? random.nextFloat() < 0.1f : validOpenings >= 2 && validOpenings <= 6 && ceilingOpenings < 2;


        if (validSpot) {
            BlockState currentBlock;

            for (int x = xMin; x <= xMax; ++x) {
                for (int y = 4; y >= -1; --y) {
                    for (int z = zMin; z <= zMax; ++z) {

                        blockpos$Mutable.set(position).move(x, y, z);
                        currentBlock = world.getBlockState(blockpos$Mutable);

                        if (x != xMin && y != -1 && z != zMin && x != xMax && y != 5 && z != zMax) {
                            if (y == 4) {
                                if (currentBlock.isOpaque()) {
                                    // ceiling
                                    if (random.nextInt(3) < 2) {
                                        world.setBlockState(blockpos$Mutable, PRISMARINE, 2);
                                    } else {
                                        world.setBlockState(blockpos$Mutable, PRISMARINE_BRICKS, 2);
                                    }
                                }
                            } else {
                                if (currentBlock.getBlock() != Blocks.CHEST &&
                                        currentBlock.getBlock() != Blocks.SPAWNER &&
                                        currentBlock.getBlock() != Blocks.DARK_PRISMARINE &&
                                        currentBlock.getBlock() != Blocks.DARK_PRISMARINE_STAIRS) {
                                    if (blockpos$Mutable.getY() < world.getSeaLevel()) {
                                        world.setBlockState(blockpos$Mutable, WATER, 2);
                                    } else {
                                        world.setBlockState(blockpos$Mutable, Blocks.AIR.getDefaultState(), 2);
                                    }
                                }

                                currentBlock = world.getBlockState(blockpos$Mutable.up());
                                if (currentBlock.getMaterial() == Material.UNDERWATER_PLANT) {
                                    if (blockpos$Mutable.getY() + 1 < world.getSeaLevel()) {
                                        world.setBlockState(blockpos$Mutable, WATER, 2);
                                    } else {
                                        world.setBlockState(blockpos$Mutable, Blocks.AIR.getDefaultState(), 2);
                                    }
                                }
                            }
                        } else if (blockpos$Mutable.getY() >= 0 &&
                                !world.getBlockState(blockpos$Mutable.down()).getMaterial().isSolid()) {
                            if (currentBlock.getBlock() != Blocks.CHEST &&
                                    currentBlock.getBlock() != Blocks.SPAWNER &&
                                    currentBlock.getBlock() != Blocks.DARK_PRISMARINE &&
                                    currentBlock.getBlock() != Blocks.DARK_PRISMARINE_STAIRS) {
                                if (blockpos$Mutable.getY() < world.getSeaLevel()) {
                                    world.setBlockState(blockpos$Mutable, WATER, 2);
                                } else {
                                    world.setBlockState(blockpos$Mutable, Blocks.AIR.getDefaultState(), 2);
                                }
                            }
                        }

                        // made sure the dungeon wall cannot replace other dungeon's mob spawner now.
                        else if (currentBlock.getMaterial().isSolid() &&
                                currentBlock.getBlock() != Blocks.CHEST &&
                                currentBlock.getBlock() != Blocks.SPAWNER) {
                            // floor
                            if (y == -1) {
                                if (random.nextInt(10) == 0) {
                                    world.setBlockState(blockpos$Mutable, MAGMA_BLOCK, 2);
                                } else if (random.nextInt(2) == 0) {
                                    world.setBlockState(blockpos$Mutable, PRISMARINE, 2);
                                } else {
                                    world.setBlockState(blockpos$Mutable, PRISMARINE_BRICKS, 2);
                                }
                            }

                            // wall
                            else {
                                if (random.nextInt(3) <= 1) {
                                    world.setBlockState(blockpos$Mutable, PRISMARINE, 2);
                                } else {
                                    currentBlock = world.getBlockState(blockpos$Mutable);
                                    world.setBlockState(blockpos$Mutable, PRISMARINE_WALL.with(Properties.WATERLOGGED, blockpos$Mutable.getY() < world.getSeaLevel()), 3);
                                }
                            }
                        }
                    }
                }
            }

            for (int l3 = 0; l3 < 2; ++l3) {
                for (int j4 = 0; j4 < 3; ++j4) {

                    int x = position.getX() + random.nextInt(randXRange * 2 + 1) - randXRange;
                    int y = position.getY();
                    int z = position.getZ() + random.nextInt(randZRange * 2 + 1) - randZRange;
                    blockpos$Mutable.set(x, y, z);
                    currentBlock = world.getBlockState(blockpos$Mutable);

                    if (currentBlock.getMaterial() == Material.WATER || currentBlock.getMaterial() == Material.AIR) {
                        int j3 = 0;

                        for (Direction direction : Direction.Type.HORIZONTAL) {
                            Material neighboringMaterial = world.getBlockState(blockpos$Mutable.offset(direction)).getMaterial();
                            if (neighboringMaterial.isSolid()) {
                                ++j3;
                            }
                        }

                        if (j3 == 1) {
                            world.setBlockState(blockpos$Mutable,
                                    StructurePiece.method_14916(world, blockpos$Mutable,
                                            Blocks.CHEST.getDefaultState()
                                                    .with(Properties.WATERLOGGED,
                                                            blockpos$Mutable.getY() < world.getSeaLevel())), 3);

                            LootableContainerBlockEntity.setLootTable(world, random, blockpos$Mutable, CHEST_LOOT);
                            break;
                        }
                    }
                }
            }

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    blockpos$Mutable.set(position).move(x, -1, z);
                    currentBlock = world.getBlockState(blockpos$Mutable);

                    if (currentBlock.getBlock() != Blocks.CHEST && currentBlock.getBlock() != Blocks.SPAWNER) {
                        world.setBlockState(blockpos$Mutable, DARK_PRISMARINE, 2);
                    }

                    blockpos$Mutable.move(Direction.DOWN);
                    currentBlock = world.getBlockState(blockpos$Mutable);
                    if (currentBlock.getBlock() != Blocks.CHEST && currentBlock.getBlock() != Blocks.SPAWNER) {
                        world.setBlockState(blockpos$Mutable, DARK_PRISMARINE, 2);
                    }
                }
            }

            blockpos$Mutable.set(position).move(Direction.DOWN);

            if (blockpos$Mutable.getY() < world.getSeaLevel()) world.setBlockState(blockpos$Mutable, WATER, 2);

            world.setBlockState(blockpos$Mutable, Blocks.AIR.getDefaultState(), 2);
            world.setBlockState(blockpos$Mutable, Blocks.SPAWNER.getDefaultState(), 2);
            BlockEntity tileentity = world.getBlockEntity(blockpos$Mutable);

            if (tileentity instanceof MobSpawnerBlockEntity) {
                ((MobSpawnerBlockEntity) tileentity).getLogic().setEntityId(pickMobSpawner(world, random, blockpos$Mutable));
            } else {
                LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", new Object[]{Integer.valueOf(blockpos$Mutable.getX()), Integer.valueOf(blockpos$Mutable.getY()), Integer.valueOf(blockpos$Mutable.getZ())});
            }


            currentBlock = world.getBlockState(blockpos$Mutable.move(Direction.UP));
            if (currentBlock.getBlock() != Blocks.CHEST && currentBlock.getBlock() != Blocks.SPAWNER) {
                world.setBlockState(blockpos$Mutable, PRISMARINE_WALL
                        .with(Properties.WATERLOGGED, blockpos$Mutable.getY() < world.getSeaLevel()), 2);
            }

            currentBlock = world.getBlockState(blockpos$Mutable.move(Direction.UP));
            if (currentBlock.getBlock() != Blocks.CHEST && currentBlock.getBlock() != Blocks.SPAWNER) {
                world.setBlockState(blockpos$Mutable, PRISMARINE_WALL
                        .with(Properties.WATERLOGGED, blockpos$Mutable.getY() < world.getSeaLevel()), 2);
            }

            for (Direction direction : Direction.Type.HORIZONTAL) {
                blockpos$Mutable.set(position).move(direction);
                currentBlock = world.getBlockState(blockpos$Mutable);
                if (currentBlock.getBlock() != Blocks.CHEST && currentBlock.getBlock() != Blocks.SPAWNER) {
                    world.setBlockState(blockpos$Mutable, DARK_PRISMARINE_STAIRS
                            .with(Properties.WATERLOGGED, blockpos$Mutable.getY() < world.getSeaLevel())
                            .with(StairsBlock.FACING, direction.getOpposite()), 2);
                }
            }

            return true;
        } else {
            return false;
        }
    }


    /**
     * Randomly decides which spawner to use in a dungeon
     */
    private static EntityType<?> pickMobSpawner(ServerWorldAccess world, Random random, BlockPos position) {
        Biome biome = world.getBiome(position);

        // spot must be an ocean so we don't return wrong mob when a hot land biome borders a frozen ocean
        if (biome.getCategory() == Category.OCEAN) {
            String biomeName = Registry.BIOME.getId(biome).getPath();
            float biomeTemp = biome.getTemperature();

            if (biomeTemp < 0.0 || biomeName.contains("frozen") || biomeName.contains("snow") || biomeName.contains("ice")) {
                return RepurposedStructures.mobSpawnerManager.getSpawnerMob(FROZEN_SPAWNER_ID, random);
            }
            // deliberately skip 0.5 temp as all vanilla oceans are 0.5 and we are checking for vanilla oceans temp by name
            else if (biomeTemp < 0.5 || biomeName.contains("cold")) {
                return RepurposedStructures.mobSpawnerManager.getSpawnerMob(COLD_SPAWNER_ID, random);
            }
            else if (biomeTemp > 0.5 || biomeName.equals("ocean") || biomeName.equals("deep_ocean")) {
                return RepurposedStructures.mobSpawnerManager.getSpawnerMob(NEUTRAL_SPAWNER_ID, random);
            }
            else if (biomeTemp >= 0.9 || biomeName.contains("lukewarm")) {
                return RepurposedStructures.mobSpawnerManager.getSpawnerMob(LUKEWARM_SPAWNER_ID, random);
            }
            else if (biomeTemp >= 1.5 || biomeName.contains("warm") || biomeName.contains("hot") || biomeName.contains("tropic")) {
                return RepurposedStructures.mobSpawnerManager.getSpawnerMob(WARM_SPAWNER_ID, random);
            }
        }

        return RepurposedStructures.mobSpawnerManager.getSpawnerMob(MISC_SPAWNER_ID, random);
    }
}
