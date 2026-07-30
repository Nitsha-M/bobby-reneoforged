package de.johni0702.minecraft.bobby.mixin;

import de.johni0702.minecraft.bobby.ext.LightEngineExt;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LightEngine.class, targets = {
        "ca.spottedleaf.starlight.common.light.StarLightInterface$1",
        "ca.spottedleaf.starlight.common.light.StarLightInterface$2"
})
public abstract class LightEngineMixin implements LightEngineExt {
    private final Long2ObjectMap<DataLayer> bobbySectionData = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
    private final Long2ObjectMap<DataLayer> bobbyOriginalSectionData = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());

    @Override
    public void bobby_addSectionData(long pos, DataLayer data) {
        this.bobbySectionData.put(pos, data);
        this.bobbyOriginalSectionData.remove(pos);
    }

    @Override
    public void bobby_removeSectionData(long pos) {
        this.bobbySectionData.remove(pos);
        this.bobbyOriginalSectionData.remove(pos);
    }

    @Override
    public void bobby_setTainted(long pos, int delta) {
        if (delta != 0) {
            DataLayer original = this.bobbyOriginalSectionData.get(pos);
            if (original == null) {
                original = this.bobbySectionData.get(pos);
                if (original == null) {
                    return;
                }
                this.bobbyOriginalSectionData.put(pos, original);
            }

            DataLayer updated = new DataLayer();

            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        updated.set(x, y, z, Math.min(Math.max(original.get(x, y, z) + delta, 0), 15));
                    }
                }
            }

            this.bobbySectionData.put(pos, updated);
        } else {
            DataLayer original = this.bobbyOriginalSectionData.remove(pos);
            if (original == null) {
                return;
            }
            bobbySectionData.put(pos, original);
        }
    }

    @Inject(method = "getDataLayerData(Lnet/minecraft/core/SectionPos;)Lnet/minecraft/world/level/chunk/DataLayer;", at = @At("HEAD"), cancellable = true)
    private void bobby_getLightSection(SectionPos pos, CallbackInfoReturnable<DataLayer> ci) {
        DataLayer data = this.bobbySectionData.get(pos.asLong());
        if (data != null) {
            ci.setReturnValue(data);
        }
    }

    @Inject(method = "getLightValue(Lnet/minecraft/core/BlockPos;)I", at = @At("HEAD"), cancellable = true)
    private void bobby_getLightSection(BlockPos blockPos, CallbackInfoReturnable<Integer> ci) {
        DataLayer data = this.bobbySectionData.get(SectionPos.of(blockPos).asLong());
        if (data != null) {
            ci.setReturnValue(data.get(
                    SectionPos.sectionRelative(blockPos.getX()),
                    SectionPos.sectionRelative(blockPos.getY()),
                    SectionPos.sectionRelative(blockPos.getZ())
            ));
        }
    }
}
