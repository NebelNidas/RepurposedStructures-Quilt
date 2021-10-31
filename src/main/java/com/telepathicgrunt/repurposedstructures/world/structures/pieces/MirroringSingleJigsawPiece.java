package com.telepathicgrunt.repurposedstructures.world.structures.pieces;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.telepathicgrunt.repurposedstructures.mixin.structures.SinglePoolElementAccessor;
import com.telepathicgrunt.repurposedstructures.modinit.RSStructurePieces;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.structures.SinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElementType;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

public class MirroringSingleJigsawPiece extends SinglePoolElement {
    private static final Codec<Either<ResourceLocation, StructureTemplate>> TEMPLATE_CODEC = Codec.of(MirroringSingleJigsawPiece::encodeTemplate, ResourceLocation.CODEC.map(Either::left));
    public static final Codec<MirroringSingleJigsawPiece> CODEC = RecordCodecBuilder.create((jigsawPieceInstance) ->
            jigsawPieceInstance.group(
                    templateCodec(),
                    processorsCodec(),
                    projectionCodec(),
                    mirrorCodec())
            .apply(jigsawPieceInstance, MirroringSingleJigsawPiece::new));

    private static <T> DataResult<T> encodeTemplate(Either<ResourceLocation, StructureTemplate> templateEither, DynamicOps<T> dynamicOps, T t) {
        Optional<ResourceLocation> optional = templateEither.left();
        return optional.isEmpty() ? DataResult.error("Can not serialize a runtime pool element") : ResourceLocation.CODEC.encode(optional.get(), dynamicOps, t);
    }

    protected static <E extends MirroringSingleJigsawPiece> RecordCodecBuilder<E, Mirror> mirrorCodec() {
        return Codec.STRING.fieldOf("mirror")
                .xmap(Mirror::valueOf, Mirror::toString)
                .forGetter((jigsawPieceInstance) -> jigsawPieceInstance.mirror);
    }

    protected final Mirror mirror;

    public MirroringSingleJigsawPiece(SinglePoolElement singleJigsawPiece, Mirror mirror) {
        this(((SinglePoolElementAccessor)singleJigsawPiece).repurposedstructures_getTemplate(), ((SinglePoolElementAccessor)singleJigsawPiece).repurposedstructures_getProcessors(), singleJigsawPiece.getProjection(), mirror);
    }

    protected MirroringSingleJigsawPiece(Either<ResourceLocation, StructureTemplate> locationTemplateEither, Supplier<StructureProcessorList> processorListSupplier, StructureTemplatePool.Projection placementBehaviour, Mirror mirror) {
        super(locationTemplateEither, processorListSupplier, placementBehaviour);
        this.mirror = mirror;
    }

    public MirroringSingleJigsawPiece(StructureTemplate template) {
        this(Either.right(template), () -> ProcessorLists.EMPTY, StructureTemplatePool.Projection.RIGID, Mirror.NONE);
    }

    private StructureTemplate getTemplate(StructureManager templateManager) {
        return this.template.map(templateManager::getOrCreate, Function.identity());
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager templateManager, BlockPos blockPos, Rotation rotation, Random random) {
        StructureTemplate template = this.getTemplate(templateManager);
        List<StructureTemplate.StructureBlockInfo> list = template.filterBlocks(blockPos, (new StructurePlaceSettings()).setRotation(rotation), Blocks.JIGSAW, true);
        Collections.shuffle(list, random);
        return list;
    }

    @Override
    public BoundingBox getBoundingBox(StructureManager templateManager, BlockPos blockPos, Rotation rotation) {
        StructureTemplate template = this.getTemplate(templateManager);
        return template.getBoundingBox((new StructurePlaceSettings()).setRotation(rotation).setMirror(this.mirror), blockPos);
    }

    @Override
    public boolean place(StructureManager templateManager, WorldGenLevel worldGenLevel, StructureFeatureManager structureManager, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos1, Rotation rotation, BoundingBox mutableBoundingBox, Random random, boolean doNotReplaceJigsaw) {
        StructureTemplate template = this.getTemplate(templateManager);
        StructurePlaceSettings placementsettings = this.getSettings(rotation, mutableBoundingBox, doNotReplaceJigsaw);
        if (!template.placeInWorld(worldGenLevel, blockPos, blockPos1, placementsettings, random, 18)) {
            return false;
        } else {
            for(StructureTemplate.StructureBlockInfo template$blockinfo : StructureTemplate.processBlockInfos(worldGenLevel, blockPos, blockPos1, placementsettings, this.getDataMarkers(templateManager, blockPos, rotation, false))) {
                this.handleDataMarker(worldGenLevel, template$blockinfo, blockPos, rotation, random, mutableBoundingBox);
            }

            return true;
        }
    }

    @Override
    protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox mutableBoundingBox, boolean doNotReplaceJigsaw) {
        StructurePlaceSettings placementsettings = new StructurePlaceSettings();
        placementsettings.setBoundingBox(mutableBoundingBox);
        placementsettings.setRotation(rotation);
        placementsettings.setMirror(mirror);
        placementsettings.setIgnoreEntities(false);
        placementsettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        placementsettings.setFinalizeEntities(true);
        if (!doNotReplaceJigsaw) {
            placementsettings.addProcessor(JigsawReplacementProcessor.INSTANCE);
        }

        this.processors.get().list().forEach(placementsettings::addProcessor);
        this.getProjection().getProcessors().forEach(placementsettings::addProcessor);
        return placementsettings;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return RSStructurePieces.MIRROR_SINGLE;
    }

    @Override
    public String toString() {
        return "Mirror_Single[" + this.template + "]";
    }
}
