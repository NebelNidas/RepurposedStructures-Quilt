package com.telepathicgrunt.repurposedstructures.misc;

import com.telepathicgrunt.repurposedstructures.RepurposedStructures;
import com.telepathicgrunt.repurposedstructures.mixin.resources.BuilderAccessor;
import com.telepathicgrunt.repurposedstructures.mixin.resources.LootContextAccessor;
import com.telepathicgrunt.repurposedstructures.mixin.resources.LootManagerAccessor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StructureModdedLootImporter {

    // Cache the reverse lookup for what loottable goes with what identifier
    private static final Map<LootTable, Identifier> REVERSED_TABLES = new HashMap<>();

    private static Set<Identifier> BLACKLISTED_LOOTTABLES;

    // Need to map loottables by hand to the vanilla structure that our structure is based on. (usually...)
    private static final Map<Identifier, Identifier> TABLE_IMPORTS = createMap();
    private static Map<Identifier, Identifier> createMap() {
        Map<Identifier, Identifier> tableMap = new HashMap<>();
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mineshafts/birch"), new Identifier("minecraft:chests/abandoned_mineshaft"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mineshafts/crimson"), new Identifier("minecraft:chests/abandoned_mineshaft"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mineshafts/desert"), new Identifier("minecraft:chests/abandoned_mineshaft"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mineshafts/end"), new Identifier("minecraft:chests/abandoned_mineshaft"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mineshafts/icy"), new Identifier("minecraft:chests/abandoned_mineshaft"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mineshafts/jungle"), new Identifier("minecraft:chests/abandoned_mineshaft"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mineshafts/nether"), new Identifier("minecraft:chests/abandoned_mineshaft"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mineshafts/ocean"), new Identifier("minecraft:chests/abandoned_mineshaft"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mineshafts/savanna"), new Identifier("minecraft:chests/abandoned_mineshaft"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mineshafts/stone"), new Identifier("minecraft:chests/abandoned_mineshaft"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mineshafts/dark_forest"), new Identifier("minecraft:chests/abandoned_mineshaft"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mineshafts/swamp"), new Identifier("minecraft:chests/abandoned_mineshaft"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mineshafts/taiga"), new Identifier("minecraft:chests/abandoned_mineshaft"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mineshafts/warped"), new Identifier("minecraft:chests/abandoned_mineshaft"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/bastions/underground/bridge"), new Identifier("minecraft:chests/stronghold_corridor")); // new Identifier("minecraft:chests/bastion_bridge")
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/bastions/underground/other"), new Identifier("minecraft:chests/stronghold_corridor")); // new Identifier("minecraft:chests/bastion_other")
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/bastions/underground/skeleton_horse_stable"), new Identifier("minecraft:chests/stronghold_corridor")); // new Identifier("minecraft:chests/bastion_hoglin_stable")
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/cities/nether"), new Identifier("minecraft:chests/bastion_treasure")); // new Identifier("minecraft:chests/end_city_treasure"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/dungeons/badlands"), new Identifier("minecraft:chests/simple_dungeon"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/dungeons/dark_forest"), new Identifier("minecraft:chests/simple_dungeon"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/dungeons/desert"), new Identifier("minecraft:chests/simple_dungeon"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/dungeons/end"), new Identifier("minecraft:chests/simple_dungeon"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/dungeons/jungle"), new Identifier("minecraft:chests/simple_dungeon"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/dungeons/mushroom"), new Identifier("minecraft:chests/simple_dungeon"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/dungeons/nether"), new Identifier("minecraft:chests/simple_dungeon"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/dungeons/ocean"), new Identifier("minecraft:chests/simple_dungeon"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/dungeons/snow"), new Identifier("minecraft:chests/simple_dungeon"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/dungeons/swamp"), new Identifier("minecraft:chests/simple_dungeon"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/fortresses/jungle_center_chest"), new Identifier("minecraft:chests/stronghold_crossing")); // new Identifier("minecraft:chests/nether_bridge"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/fortresses/jungle_hallway_chest"), new Identifier("minecraft:chests/stronghold_corridor")); // new Identifier("minecraft:chests/nether_bridge"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/fortresses/jungle_shrine_chest"), new Identifier("minecraft:chests/stronghold_crossing")); // new Identifier("minecraft:chests/nether_bridge"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/igloos/grassy_chest"), new Identifier("minecraft:chests/igloo_chest"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/igloos/stone_chest"), new Identifier("minecraft:chests/igloo_chest"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mansions/birch"), new Identifier("minecraft:chests/woodland_mansion"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mansions/desert"), new Identifier("minecraft:chests/woodland_mansion"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mansions/jungle"), new Identifier("minecraft:chests/woodland_mansion"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mansions/oak"), new Identifier("minecraft:chests/woodland_mansion"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mansions/savanna"), new Identifier("minecraft:chests/woodland_mansion"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mansions/snowy"), new Identifier("minecraft:chests/woodland_mansion"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/mansions/taiga"), new Identifier("minecraft:chests/woodland_mansion"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/outposts/badlands_chest"), new Identifier("minecraft:chests/pillager_outpost"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/outposts/birch_chest"), new Identifier("minecraft:chests/pillager_outpost"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/outposts/crimson_chest"), new Identifier("minecraft:chests/pillager_outpost"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/outposts/desert_chest"), new Identifier("minecraft:chests/pillager_outpost"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/outposts/giant_tree_taiga_chest"), new Identifier("minecraft:chests/pillager_outpost"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/outposts/icy_chest"), new Identifier("minecraft:chests/pillager_outpost"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/outposts/jungle_chest"), new Identifier("minecraft:chests/pillager_outpost"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/outposts/nether_brick_chest"), new Identifier("minecraft:chests/pillager_outpost"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/outposts/oak_chest"), new Identifier("minecraft:chests/pillager_outpost"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/outposts/snowy_chest"), new Identifier("minecraft:chests/pillager_outpost"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/outposts/taiga_chest"), new Identifier("minecraft:chests/pillager_outpost"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/outposts/warped_chest"), new Identifier("minecraft:chests/pillager_outpost"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/outposts/end_shulker_box"), new Identifier("minecraft:chests/end_city_treasure")); // "minecraft:chests/pillager_outpost"
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/pyramids/badlands_chest"), new Identifier("minecraft:chests/desert_pyramid"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/pyramids/nether_chest"), new Identifier("minecraft:chests/desert_pyramid"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/pyramids/snowy_chest"), new Identifier("minecraft:chests/desert_pyramid"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/pyramids/icy_chest"), new Identifier("minecraft:chests/desert_pyramid"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/pyramids/jungle_chest"), new Identifier("minecraft:chests/desert_pyramid"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/pyramids/mushroom_chest"), new Identifier("minecraft:chests/desert_pyramid"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/pyramids/ocean_chest"), new Identifier("minecraft:chests/desert_pyramid"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/pyramids/giant_tree_taiga_chest"), new Identifier("minecraft:chests/desert_pyramid"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/pyramids/flower_forest_chest"), new Identifier("minecraft:chests/desert_pyramid"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/pyramids/end_chest"), new Identifier("minecraft:chests/end_city_treasure")); // "minecraft:chests/desert_pyramid"
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/ruins/nether"), new Identifier("minecraft:chests/bastion_other")); // new Identifier("minecraft:chests/underwater_ruin_big"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/ruins/land_hot/large"), new Identifier("minecraft:chests/village/village_desert_house"));  // new Identifier("minecraft:chests/underwater_ruin_big")
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/ruins/land_hot/small"), new Identifier("minecraft:chests/village/village_desert_house"));  // new Identifier("minecraft:chests/underwater_ruin_small")
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/ruins/land_warm/large"), new Identifier("minecraft:chests/village/village_plains_house"));  // new Identifier("minecraft:chests/underwater_ruin_big")
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/ruins/land_warm/small"), new Identifier("minecraft:chests/village/village_plains_house"));  // new Identifier("minecraft:chests/underwater_ruin_small")
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/ruined_portals/large_portal_chest"), new Identifier("minecraft:chests/ruined_portal"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/ruined_portals/small_portal_chest"), new Identifier("minecraft:chests/ruined_portal"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/shipwrecks/crimson/map_chest"), new Identifier("minecraft:chests/shipwreck_map"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/shipwrecks/crimson/supply_chest"), new Identifier("minecraft:chests/shipwreck_supply"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/shipwrecks/crimson/treasure_chest"), new Identifier("minecraft:chests/shipwreck_treasure"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/shipwrecks/end/map_chest"), new Identifier("minecraft:chests/shipwreck_map"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/shipwrecks/end/supply_chest"), new Identifier("minecraft:chests/shipwreck_supply"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/shipwrecks/end/treasure_chest"), new Identifier("minecraft:chests/end_city_treasure")); //  new Identifier("minecraft:chests/shipwreck_treasure"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/shipwrecks/warped/map_chest"), new Identifier("minecraft:chests/shipwreck_map"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/shipwrecks/warped/supply_chest"), new Identifier("minecraft:chests/shipwreck_supply"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/shipwrecks/warped/treasure_chest"), new Identifier("minecraft:chests/shipwreck_treasure"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/shipwrecks/nether_bricks/treasure_chest"), new Identifier("minecraft:chests/shipwreck_treasure"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/strongholds/nether_storage_room"), new Identifier("minecraft:chests/stronghold_crossing"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/strongholds/nether_hallway"), new Identifier("minecraft:chests/stronghold_corridor"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/strongholds/nether_library"), new Identifier("minecraft:chests/stronghold_library"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/temples/nether_basalt_chest"), new Identifier("minecraft:chests/nether_bridge")); // new Identifier("minecraft:chests/jungle_temple"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/temples/nether_crimson_chest"), new Identifier("minecraft:chests/nether_bridge")); // new Identifier("minecraft:chests/jungle_temple"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/temples/nether_soul_chest"), new Identifier("minecraft:chests/nether_bridge")); // new Identifier("minecraft:chests/jungle_temple"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/temples/nether_warped_chest"), new Identifier("minecraft:chests/nether_bridge")); //  new Identifier("minecraft:chests/jungle_temple"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/temples/nether_warped_trapped_chest"), new Identifier("minecraft:chests/nether_bridge")); // new Identifier("minecraft:chests/jungle_temple"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/temples/nether_wasteland_chest"), new Identifier("minecraft:chests/nether_bridge")); // new Identifier("minecraft:chests/jungle_temple"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "dispensers/temples/nether_basalt"), new Identifier("minecraft:chests/jungle_temple_dispenser"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "dispensers/temples/nether_crimson"), new Identifier("minecraft:chests/jungle_temple_dispenser"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "dispensers/temples/nether_soul"), new Identifier("minecraft:chests/jungle_temple_dispenser"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "dispensers/temples/nether_warped"), new Identifier("minecraft:chests/jungle_temple_dispenser"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "dispensers/temples/nether_wasteland"), new Identifier("minecraft:chests/jungle_temple_dispenser"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/badlands_house"), new Identifier("minecraft:chests/village/village_desert_house"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/birch_house"), new Identifier("minecraft:chests/village/village_plains_house"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/dark_forest_house"), new Identifier("minecraft:chests/village/village_plains_house"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/giant_taiga_house"), new Identifier("minecraft:chests/village/village_taiga_house"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/jungle_house"), new Identifier("minecraft:chests/village/village_savanna_house"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/mountains_house"), new Identifier("minecraft:chests/village/village_snowy_house"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/oak_house"), new Identifier("minecraft:chests/village/village_plains_house"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/swamp_house"), new Identifier("minecraft:chests/village/village_plains_house"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/crimson_cartographer"), new Identifier("minecraft:chests/village/village_cartographer"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/crimson_fisher"), new Identifier("minecraft:chests/village/village_fisher"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/crimson_tanner"), new Identifier("minecraft:chests/village/village_tannery"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/crimson_weaponsmith"), new Identifier("minecraft:chests/village/village_weaponsmith"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/crimson_house"), new Identifier("minecraft:chests/village/village_desert_house"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/warped_cartographer"), new Identifier("minecraft:chests/village/village_cartographer"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/warped_fisher"), new Identifier("minecraft:chests/village/village_fisher"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/warped_tanner"), new Identifier("minecraft:chests/village/village_tannery"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/warped_weaponsmith"), new Identifier("minecraft:chests/village/village_weaponsmith"));
        tableMap.put(new Identifier(RepurposedStructures.MODID, "chests/villages/warped_house"), new Identifier("minecraft:chests/village/village_desert_house"));
        return tableMap;
    }



    public static List<ItemStack> checkAndGetModifiedLoot(LootContext context, LootTable currentLootTable, List<ItemStack> originalLoot){
        if(RepurposedStructures.RSAllConfig.RSMainConfig.importModdedItems)
        {
            // Cache the result of the loottable to the id into our own map.
            Identifier lootTableID = REVERSED_TABLES.computeIfAbsent(
                    currentLootTable,
                    // Will iterate lazily through loottable map for which identifier gives this loottable and return the result
                    (lootTable) -> ((LootManagerAccessor)context.getWorld().getServer().getLootManager()).repurposedstructures_getTables()
                            .entrySet()
                            .stream()
                            .filter(entry -> lootTable.equals(entry.getValue()))
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElse(null) // null should be ever returned as otherwise, that would be concerning...
            );

            if(lootTableID != null &&
                lootTableID.getNamespace().equals(RepurposedStructures.MODID) &&
                !isInBlacklist(lootTableID))
            {
                return StructureModdedLootImporter.modifyLootTables(context, lootTableID, originalLoot);
            }
        }

        return new ArrayList<>();
    }

    public static List<ItemStack> modifyLootTables(LootContext context, Identifier lootTableID, List<ItemStack> originalLoot)
    {
        Identifier tableToImportLoot = TABLE_IMPORTS.get(lootTableID);
        if(tableToImportLoot == null) return originalLoot; // Safety net

        // Generate random loot that would've been in vanilla chests. (Need to make new context or else we recursively call ourselves infinitely)
        LootContext newContext = copyLootContextWithNewQueryID(context);
        List<ItemStack> newlyGeneratedLoot = newContext.getTable(tableToImportLoot).generateLoot(newContext);

        // Remove all vanilla loot so we only have modded loot
        newlyGeneratedLoot.removeIf(itemStack -> {
            RegistryKey<Item> itemKey = Registry.ITEM.getKey(itemStack.getItem()).orElse(null);
            return itemKey != null && itemKey.getValue().getNamespace().equals("minecraft");
        });

        // Add modded loot to my structure's chests
        originalLoot.addAll(newlyGeneratedLoot);
        return originalLoot;
    }

    private static LootContext copyLootContextWithNewQueryID(LootContext oldLootContext){
        LootContext.Builder newContextBuilder = new LootContext.Builder(oldLootContext.getWorld())
                .random(oldLootContext.getRandom())
                .luck(oldLootContext.getLuck());

        ((BuilderAccessor)newContextBuilder).repurposedstructures_setDrops(((LootContextAccessor)oldLootContext).repurposedstructures_getDrops());
        ((BuilderAccessor)newContextBuilder).repurposedstructures_setParameters(((LootContextAccessor)oldLootContext).repurposedstructures_getParameters());
        return newContextBuilder.build(LootContextTypes.CHEST);
    }

    private static boolean isInBlacklist(Identifier lootTableID){
        if(BLACKLISTED_LOOTTABLES == null){
            String cleanedBlacklist = RepurposedStructures.RSAllConfig.RSMainConfig.blacklistedRSLoottablesFromImportingModdedItems.replace(" ", "");

            if(cleanedBlacklist.equals("")){
                BLACKLISTED_LOOTTABLES = new HashSet<>(); // make empty set instead of ["minecraft:"].
            }
            else {
                BLACKLISTED_LOOTTABLES =
                        Arrays.stream(cleanedBlacklist.split(","))
                            .map(Identifier::new)
                            .collect(Collectors.toSet());
            }
        }

        return BLACKLISTED_LOOTTABLES.contains(lootTableID);
    }
}
