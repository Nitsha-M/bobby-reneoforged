package de.johni0702.minecraft.bobby.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import de.johni0702.minecraft.bobby.FakeChunkManager;
import de.johni0702.minecraft.bobby.Worlds;
import de.johni0702.minecraft.bobby.ext.ClientChunkManagerExt;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;

public class CreateWorldCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ClientLevel world = Minecraft.getInstance().level;

        FakeChunkManager bobbyChunkManager = ((ClientChunkManagerExt) world.getChunkSource()).bobby_getFakeChunkManager();
        if (bobbyChunkManager == null) {
            source.sendFailure(Component.translatable("bobby.upgrade.not_enabled"));
            return 0;
        }

        Worlds worlds = bobbyChunkManager.getWorlds();
        if (worlds == null) {
            source.sendFailure(Component.translatable("bobby.dynamic_multi_world.not_enabled"));
            return 0;
        }

        worlds.userRequestedFork(source);

        return 0;
    }
}
