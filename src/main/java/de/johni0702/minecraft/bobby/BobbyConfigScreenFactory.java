package de.johni0702.minecraft.bobby;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;

public class BobbyConfigScreenFactory {
    public static Screen createConfigScreen(Screen parent, BobbyConfig config, Consumer<BobbyConfig> update) {
        BobbyConfig defaultConfig = BobbyConfig.DEFAULT;

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.bobby.config"));


        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        BooleanListEntry enabled = entryBuilder
                .startBooleanToggle(Component.translatable("option.bobby.enabled"), config.isEnabled())
                .setDefaultValue(defaultConfig.isEnabled())
                .build();

        BooleanListEntry dynamicMultiWorld = entryBuilder
                .startBooleanToggle(Component.translatable("option.bobby.dynamic_multi_world"), config.isDynamicMultiWorld())
                .setTooltip(Component.translatable("tooltip.option.bobby.dynamic_multi_world"))
                .setDefaultValue(defaultConfig.isDynamicMultiWorld())
                .build();

        BooleanListEntry noBlockEntities = entryBuilder
                .startBooleanToggle(Component.translatable("option.bobby.no_block_entities"), config.isNoBlockEntities())
                .setTooltip(Component.translatable("tooltip.option.bobby.no_block_entities"))
                .setDefaultValue(defaultConfig.isNoBlockEntities())
                .build();

        BooleanListEntry taintFakeChunks = entryBuilder
                .startBooleanToggle(Component.translatable("option.bobby.taint_fake_chunks"), config.isTaintFakeChunks())
                .setTooltip(Component.translatable("tooltip.option.bobby.taint_fake_chunks"))
                .setDefaultValue(defaultConfig.isTaintFakeChunks())
                .build();

        IntegerListEntry unloadDelaySecs = entryBuilder
                .startIntField(Component.translatable("option.bobby.unload_delay"), config.getUnloadDelaySecs())
                .setTooltip(Component.translatable("tooltip.option.bobby.unload_delay"))
                .setDefaultValue(defaultConfig.getUnloadDelaySecs())
                .build();

        IntegerListEntry deleteUnusedRegionsAfterDays = entryBuilder
                .startIntField(Component.translatable("option.bobby.delete_unused_regions_after_days"), config.getDeleteUnusedRegionsAfterDays())
                .setTooltip(Component.translatable("tooltip.option.bobby.delete_unused_regions_after_days"))
                .setDefaultValue(defaultConfig.getDeleteUnusedRegionsAfterDays())
                .build();

        IntegerListEntry maxRenderDistance = entryBuilder
                .startIntField(Component.translatable("option.bobby.max_render_distance"), config.getMaxRenderDistance())
                .setTooltip(Component.translatable("tooltip.option.bobby.max_render_distance"))
                .setDefaultValue(defaultConfig.getMaxRenderDistance())
                .build();

        IntegerSliderEntry viewDistanceOverwrite = entryBuilder
                .startIntSlider(Component.translatable("option.bobby.view_distance_overwrite"), config.getViewDistanceOverwrite(), 0, 32)
                .setTooltip(Component.translatable("tooltip.option.bobby.view_distance_overwrite"))
                .setDefaultValue(defaultConfig.getViewDistanceOverwrite())
                .build();

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("category.bobby.general"));
        general.addEntry(enabled);
        general.addEntry(dynamicMultiWorld);
        general.addEntry(noBlockEntities);
        general.addEntry(taintFakeChunks);
        general.addEntry(unloadDelaySecs);
        general.addEntry(deleteUnusedRegionsAfterDays);
        general.addEntry(maxRenderDistance);
        general.addEntry(viewDistanceOverwrite);

        builder.setSavingRunnable(() -> update.accept(new BobbyConfig(
                enabled.getValue(),
                dynamicMultiWorld.getValue(),
                noBlockEntities.getValue(),
                taintFakeChunks.getValue(),
                unloadDelaySecs.getValue(),
                deleteUnusedRegionsAfterDays.getValue(),
                maxRenderDistance.getValue(),
                viewDistanceOverwrite.getValue()
        )));

        return builder.build();
    }
}
