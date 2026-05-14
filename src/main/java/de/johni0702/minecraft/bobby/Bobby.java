package de.johni0702.minecraft.bobby;

import de.johni0702.minecraft.bobby.commands.CreateWorldCommand;
import de.johni0702.minecraft.bobby.commands.MergeWorldsCommand;
import de.johni0702.minecraft.bobby.commands.UpgradeCommand;
import de.johni0702.minecraft.bobby.commands.WorldsCommand;
import de.johni0702.minecraft.bobby.ext.ClientChunkManagerExt;
import de.johni0702.minecraft.bobby.mixin.OptionInstanceAccessor;
import de.johni0702.minecraft.bobby.mixin.OptionInstanceIntRangeAccessor;
import de.johni0702.minecraft.bobby.util.FlawlessFrames;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.configurate.reference.WatchServiceListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod("bobby")
public class Bobby {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "bobby";

    private static Bobby instance;

    public static Bobby getInstance() {
        return instance;
    }

    private static final Minecraft client = Minecraft.getInstance();
    private final ModContainer modContainer;

    private static ValueReference<BobbyConfig, CommentedConfigurationNode> configReference;

    public static void initStaticConfig() {
        if (configReference != null)
            return;
        try {
            Path configPath = FMLPaths.CONFIGDIR.get().resolve(MOD_ID + ".conf");
            @SuppressWarnings("resource")
            ConfigurationReference<CommentedConfigurationNode> rootRef = createWatchServiceListener()
                    .listenToConfiguration(path -> HoconConfigurationLoader.builder().path(path).build(), configPath);
            configReference = rootRef.referenceTo(BobbyConfig.class);
            rootRef.saveAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BobbyConfig getConfigStatic() {
        initStaticConfig();
        return configReference != null ? configReference.get() : BobbyConfig.DEFAULT;
    }

    public Bobby(IEventBus modEventBus, ModContainer modContainer) {
        instance = this;
        this.modContainer = modContainer;

        initStaticConfig();

        modEventBus.addListener(this::onClientSetup);
        NeoForge.EVENT_BUS.addListener(this::registerClientCommands);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("cloth_config")) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                    (minecraft, screen) -> createConfigScreen(screen));
        }

        FlawlessFrames.onClientInitialization();

        configReference.subscribe(new TaintChunksConfigHandler()::update);
        configReference.subscribe(new MaxRenderDistanceConfigHandler()::update);

        Util.ioPool().submit(this::cleanupOldWorlds);
    }

    private void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("bobby")
                .then(Commands.literal("worlds").executes(new WorldsCommand(false))
                        .then(Commands.literal("full").executes(new WorldsCommand(true)))
                        .then(Commands.literal("merge")
                                .then(Commands.argument("source", integer())
                                        .then(Commands.argument("target", integer())
                                                .executes(new MergeWorldsCommand()))))
                        .then(Commands.literal("create").executes(new CreateWorldCommand())))
                .then(Commands.literal("upgrade").executes(new UpgradeCommand())));
    }

    public BobbyConfig getConfig() {
        return getConfigStatic();
    }

    public boolean isEnabled() {
        BobbyConfig config = getConfigStatic();
        return config.isEnabled()
                // For singleplayer, disable ourselves unless the view-distance overwrite is
                // active.
                && (client.getSingleplayerServer() == null || config.getViewDistanceOverwrite() != 0);
    }

    public Screen createConfigScreen(Screen parent) {
        if (ModList.get().isLoaded("cloth_config")) {
            return BobbyConfigScreenFactory.createConfigScreen(parent, getConfig(), configReference::setAndSaveAsync);
        }
        return null;
    }

    private void cleanupOldWorlds() {
        int deleteUnusedRegionsAfterDays = Bobby.getInstance().getConfig().getDeleteUnusedRegionsAfterDays();
        if (deleteUnusedRegionsAfterDays < 0) {
            return;
        }

        Path basePath = client.gameDirectory.toPath().resolve(".bobby");

        List<Path> toBeDeleted;
        try (Stream<Path> stream = Files.walk(basePath, 4)) {
            toBeDeleted = stream
                    .filter(it -> basePath.relativize(it).getNameCount() == 4)
                    .flatMap(directory -> {
                        try {
                            if (Files.exists(Worlds.metaFile(directory))) {
                                List<Path> worlds;
                                try (Stream<Path> fStream = Files.list(directory)) {
                                    worlds = fStream.filter(Files::isDirectory).collect(Collectors.toList());
                                }
                                boolean stillHasWorlds = false;
                                List<Path> toDelete = new ArrayList<>();
                                for (Path world : worlds) {
                                    if (LastAccessFile.isEverythingOlderThan(world, deleteUnusedRegionsAfterDays)) {
                                        try (Stream<Path> fStream = Files.list(world)) {
                                            fStream.forEach(toDelete::add);
                                        }
                                    } else {
                                        stillHasWorlds = true;
                                    }
                                }
                                if (!stillHasWorlds && LastAccessFile.isEverythingOlderThan(directory,
                                        deleteUnusedRegionsAfterDays)) {
                                    try (Stream<Path> fStream = Files.list(directory)) {
                                        fStream.forEach(toDelete::add);
                                    }
                                }
                                return toDelete.stream();
                            } else {
                                if (LastAccessFile.isEverythingOlderThan(directory, deleteUnusedRegionsAfterDays)) {
                                    try (Stream<Path> fStream = Files.list(directory)) {
                                        return fStream.toList().stream();
                                    }
                                }
                            }
                        } catch (IOException e) {
                            LOGGER.error("Failed to read last used file in " + directory + ":", e);
                        }
                        return Stream.empty();
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Failed to index bobby cache for cleanup:", e);
            return;
        }

        for (Path path : toBeDeleted) {
            if (!Files.exists(path))
                continue;
            try {
                Files.delete(path);
                deleteParentsIfEmpty(path);
            } catch (IOException e) {
                LOGGER.error("Failed to delete " + path + ":", e);
            }
        }
    }

    private static void deleteParentsIfEmpty(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent == null) {
            return;
        }
        try (Stream<Path> stream = Files.list(parent)) {
            if (stream.findAny().isPresent()) {
                return;
            }
        }
        Files.delete(parent);
        deleteParentsIfEmpty(parent);
    }

    private static WatchServiceListener createWatchServiceListener() throws IOException {
        WatchServiceListener listener = WatchServiceListener.create();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                listener.close();
            } catch (IOException e) {
                LOGGER.catching(e);
            }
        }, "Configurate shutdown thread (Bobby)"));

        return listener;
    }

    private class TaintChunksConfigHandler {
        private boolean wasEnabled = getConfig().isTaintFakeChunks();

        public void update(BobbyConfig config) {
            client.submit(() -> setEnabled(config.isTaintFakeChunks()));
        }

        private void setEnabled(boolean enabled) {
            if (wasEnabled == enabled) {
                return;
            }
            wasEnabled = enabled;

            ClientLevel world = client.level;
            if (world == null) {
                return;
            }

            FakeChunkManager bobbyChunkManager = ((ClientChunkManagerExt) world.getChunkSource())
                    .bobby_getFakeChunkManager();
            if (bobbyChunkManager == null) {
                return;
            }

            for (LevelChunk fakeChunk : bobbyChunkManager.getFakeChunks()) {
                ((FakeChunk) fakeChunk).setTainted(enabled);
            }
        }
    }

    private class MaxRenderDistanceConfigHandler {
        private int oldMaxRenderDistance = 0;

        {
            update(getConfig(), true);
        }

        public void update(BobbyConfig config) {
            update(config, false);
        }

        public void update(BobbyConfig config, boolean increaseOnly) {
            client.submit(() -> setMaxRenderDistance(config.getMaxRenderDistance(), increaseOnly));
        }

        @SuppressWarnings({ "ConstantConditions", "unchecked" })
        private void setMaxRenderDistance(int newMaxRenderDistance, boolean increaseOnly) {
            if (oldMaxRenderDistance == newMaxRenderDistance) {
                return;
            }
            oldMaxRenderDistance = newMaxRenderDistance;

            OptionInstance<Integer> viewDistance = client.options.renderDistance();
            if (viewDistance.values() instanceof OptionInstance.IntRange callbacks) {
                int newMax = increaseOnly ? Math.max(callbacks.maxInclusive(), newMaxRenderDistance) : newMaxRenderDistance;
                OptionInstance.IntRange newRange = new OptionInstance.IntRange(callbacks.minInclusive(), newMax);
                OptionInstanceAccessor<Integer> optionAccessor = (OptionInstanceAccessor<Integer>) (Object) viewDistance;
                optionAccessor.setValues(newRange);
                optionAccessor.setCodec(newRange.codec());
            }
        }
    }
}
