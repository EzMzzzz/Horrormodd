package com.horrormod;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public final class HorrorCommands {
    private HorrorCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<ServerCommandSource> scare = CommandManager.literal("scare");
            for (ScareManager.Type t : ScareManager.Type.values()) {
                scare.then(CommandManager.literal(t.name().toLowerCase()).executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    ScareManager.trigger(p.getServerWorld(), p, t);
                    return 1;
                }));
            }
            dispatcher.register(CommandManager.literal("horror")
                    .requires(s -> s.hasPermissionLevel(2))
                    .then(scare));
        });
    }
}
