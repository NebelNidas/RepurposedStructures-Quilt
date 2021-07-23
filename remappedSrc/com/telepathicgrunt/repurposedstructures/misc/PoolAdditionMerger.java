package com.telepathicgrunt.repurposedstructures.misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.telepathicgrunt.repurposedstructures.RepurposedStructures;
import com.telepathicgrunt.repurposedstructures.mixin.resources.NamespaceResourceManagerAccessor;
import com.telepathicgrunt.repurposedstructures.mixin.resources.ReloadableResourceManagerImplAccessor;
import com.telepathicgrunt.repurposedstructures.mixin.structures.ListPoolElementAccessor;
import com.telepathicgrunt.repurposedstructures.mixin.structures.SinglePoolElementAccessor;
import com.telepathicgrunt.repurposedstructures.mixin.structures.StructureManagerAccessor;
import com.telepathicgrunt.repurposedstructures.mixin.structures.StructurePoolAccessor;
import com.telepathicgrunt.repurposedstructures.utils.SafeDecodingRegistryOps;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PoolAdditionMerger {

    // Needed for detecting the correct files, ignoring file extension, and what JSON parser to use for parsing the files
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().setLenient().disableHtmlEscaping().create();
    private static final String DATA_TYPE = "pool_additions";
    private static final int FILE_SUFFIX_LENGTH = ".json".length();

    /**
     * Call this at mod init so we can subscribe our pool merging to run at server startup as that's when the dynamic registry exists.
     */
    public static void mergeAdditionPools() {
        ServerLifecycleEvents.SERVER_STARTING.register((MinecraftServer minecraftServer) -> {
            ResourceManager resourceManager = ((StructureManagerAccessor) minecraftServer.getStructureManager()).repurposedstructures_getResourceManager();
            Map<ResourceLocation, List<JsonElement>> poolAdditionJSON = getPoolAdditionJSON(resourceManager);
            parsePoolsAndBeginMerger(poolAdditionJSON, minecraftServer.registryAccess(), minecraftServer.getStructureManager());
        });
    }

    /**
     * Will grab all JSON objects from all datapacks's pool_additions folder.
     *
     * @return - A map of paths (identifiers) to a list of all JSON elements found under it from all datapacks.
     */
    private static Map<ResourceLocation, List<JsonElement>> getPoolAdditionJSON(ResourceManager resourceManager) {
        Map<ResourceLocation, List<JsonElement>> map = new HashMap<>();
        int dataTypeLength = DATA_TYPE.length() + 1;

        // Finds all JSON files paths within the pool_additions folder. NOTE: this is just the path rn. Not the actual files yet.
        for (ResourceLocation fileIDWithExtension : resourceManager.listResources(DATA_TYPE, (fileString) -> fileString.endsWith(".json"))) {
            String identifierPath = fileIDWithExtension.getPath();
            ResourceLocation fileID = new ResourceLocation(
                    fileIDWithExtension.getNamespace(),
                    identifierPath.substring(dataTypeLength, identifierPath.length() - FILE_SUFFIX_LENGTH));

            try {
                // getAllFileStreams will find files with the given ID. This part is what will loop over all matching files from all datapacks.
                for (InputStream fileStream : getAllFileStreams(resourceManager, fileIDWithExtension)) {
                    try (Reader bufferedReader = new BufferedReader(new InputStreamReader(fileStream, StandardCharsets.UTF_8))) {

                        // Get the JSON from the file
                        JsonElement poolJSONElement = GsonHelper.fromJson(GSON, bufferedReader, (Class<? extends JsonElement>) JsonElement.class);
                        if (poolJSONElement != null) {

                            // Create list in map for the ID if non exists yet for that ID
                            if (!map.containsKey(fileID)) {
                                map.put(fileID, new ArrayList<>());
                            }
                            // Add the pool to the list we will merge later on
                            map.get(fileID).add(poolJSONElement);
                        }
                        else {
                            RepurposedStructures.LOGGER.error(
                                    "(Repurposed Structures POOL MERGER) Couldn't load data file {} from {} as it's null or empty",
                                    fileID,
                                    fileIDWithExtension);
                        }
                    }
                }
            }
            catch (IllegalArgumentException | IOException | JsonParseException exception) {
                RepurposedStructures.LOGGER.error(
                        "(Repurposed Structures POOL MERGER) Couldn't parse data file {} from {}",
                        fileID,
                        fileIDWithExtension,
                        exception);
            }
        }

        return map;
    }


    /**
     * Obtains all of the file streams for all files found in all datapacks with the given id.
     *
     * @return - Filestream list of all files found with id
     */
    private static List<InputStream> getAllFileStreams(ResourceManager resourceManager, ResourceLocation fileID) throws IOException {
        List<InputStream> fileStreams = new ArrayList<>();

        FallbackResourceManager namespaceResourceManager = ((ReloadableResourceManagerImplAccessor) resourceManager).repurposedstructures_getNamespaceManagers().get(fileID.getNamespace());
        List<PackResources> allResourcePacks = ((NamespaceResourceManagerAccessor) namespaceResourceManager).repurposedstructures_getPackList();

        // Find the file with the given id and add its filestream to the list
        for (PackResources resourcePack : allResourcePacks) {
            if (resourcePack.hasResource(PackType.SERVER_DATA, fileID)) {
                InputStream inputStream = ((NamespaceResourceManagerAccessor) namespaceResourceManager).repurposedstructures_callOpen(fileID, resourcePack);
                if (inputStream != null) fileStreams.add(inputStream);
            }
        }

        // Return filestream of all files matching id path
        return fileStreams;
    }

    /**
     * Using the given dynamic registry, will now parse the JSON objects of pools and resolve their processors with the dynamic registry.
     * Afterwards, it will merge the parsed pool into the targeted pool found in the dynamic registry.
     */
    private static void parsePoolsAndBeginMerger(Map<ResourceLocation, List<JsonElement>> poolAdditionJSON, RegistryAccess dynamicRegistryManager, StructureManager structureManager) {
        WritableRegistry<StructureTemplatePool> poolRegistry = dynamicRegistryManager.ownedRegistryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        // A RegistryOps that doesn't break everything under the sun and can take a DynamicRegistryManager instead of DynamicRegistryManager.Impl.
        SafeDecodingRegistryOps<JsonElement> customRegistryOps = new SafeDecodingRegistryOps<>(JsonOps.INSTANCE, dynamicRegistryManager);

        // Will iterate over all of our found pool additions and make sure the target pool exists before we parse our JSON objects
        for (Map.Entry<ResourceLocation, List<JsonElement>> entry : poolAdditionJSON.entrySet()) {
            if (poolRegistry.get(entry.getKey()) == null) continue;

            // Parse the given pool addition JSON objects and add their pool to the dynamic registry pool
            for (JsonElement jsonElement : entry.getValue()) {
                StructureTemplatePool.DIRECT_CODEC.parse(customRegistryOps, jsonElement)
                        .resultOrPartial(messageString -> logBadData(entry.getKey(), messageString))
                        .ifPresent(validPool -> mergeIntoExistingPool(validPool, poolRegistry.get(entry.getKey()), structureManager));
            }
        }
    }

    /**
     * Merges the incoming pool with the given target pool in an additive manner that does not affect any other pools and can be stacked safely.
     */
    private static void mergeIntoExistingPool(StructureTemplatePool feedingPool, StructureTemplatePool gluttonyPool, StructureManager structureManager) {
        // Make new copies of lists as the originals are immutable lists and we want to make sure our changes only stays with this pool element
        List<StructurePoolElement> elements = new ArrayList<>(((StructurePoolAccessor) gluttonyPool).repurposedstructures_getElements());
        List<Pair<StructurePoolElement, Integer>> elementCounts = new ArrayList<>(((StructurePoolAccessor) gluttonyPool).repurposedstructures_getElementCounts());

        elements.addAll(((StructurePoolAccessor) feedingPool).repurposedstructures_getElements());
        elementCounts.addAll(((StructurePoolAccessor) feedingPool).repurposedstructures_getElementCounts());

        // Helps people know if they typoed their merger pool's nbt file paths
        for(StructurePoolElement element : elements){
            if(element instanceof SinglePoolElement singlePoolElement){
                Optional<Identifier> nbtID = ((SinglePoolElementAccessor)singlePoolElement).repurposedstructures_getLocation().left();
                if(nbtID.isEmpty()) continue;
                Optional<Structure> structureTemplate = structureManager.getStructure(nbtID.get());
                if(structureTemplate.isEmpty()){
                    RepurposedStructures.LOGGER.error("(Repurposed Structures POOL MERGER) Found an entry in {} that points to the non-existent nbt file called {}", feedingPool.getId(), nbtID.get());
                }
            }
            else if(element instanceof ListPoolElement listPoolElement){
                for(StructurePoolElement listElement : ((ListPoolElementAccessor)listPoolElement).repurposedstructures_getElements()){
                    if(listElement instanceof SinglePoolElement singlePoolElement) {
                        Optional<Identifier> nbtID = ((SinglePoolElementAccessor) singlePoolElement).repurposedstructures_getLocation().left();
                        if (nbtID.isEmpty()) continue;
                        Optional<Structure> structureTemplate = structureManager.getStructure(nbtID.get());
                        if (structureTemplate.isEmpty()) {
                            RepurposedStructures.LOGGER.error("(Repurposed Structures POOL MERGER) Found an entry in {} that points to the non-existent nbt file called {}", feedingPool.getId(), nbtID.get());
                        }
                    }
                }
            }
        }

        ((StructurePoolAccessor) gluttonyPool).repurposedstructures_setElements(elements);
        ((StructurePoolAccessor) gluttonyPool).repurposedstructures_setElementCounts(elementCounts);
    }

    /**
     * Log out the pool that failed to be parsed and what the error is.
     */
    private static void logBadData(ResourceLocation poolPath, String messageString) {
        RepurposedStructures.LOGGER.error("(Repurposed Structures POOL MERGER) Failed to parse {} additions file. Error is: {}", poolPath, messageString);
    }
}
