package de.johni0702.minecraft.bobby;

import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;
import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.caffeinemc.mods.sodium.api.config.option.Range;

@ConfigEntryPointForge("bobby")
public class SodiumConfigEntrypoint implements ConfigEntryPoint {
    private static final ResourceLocation SODIUM_RENDER_DISTANCE = ResourceLocation.parse("sodium:general.render_distance");

    @Override
    public void registerConfigLate(ConfigBuilder configBuilder) {

        configBuilder.registerOwnModOptions()
                .registerOptionOverlay(SODIUM_RENDER_DISTANCE, configBuilder
                        .createIntegerOption(SODIUM_RENDER_DISTANCE)
                        .setRangeProvider(state -> {
                            int overwrite = Bobby.getInstance().getConfig().getMaxRenderDistance();
                            return new Range(2, Math.max(3, overwrite), 1);
                        }, ConfigState.UPDATE_ON_REBUILD)
                );
    }
}