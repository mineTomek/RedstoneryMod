package com.redstonery.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import static net.minecraft.server.command.CommandManager.*;

import net.minecraft.server.command.ServerCommandSource;

public final class RedstoneryCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("redstonery")
                .requires(source -> source.hasPermissionLevel(1))
                .then(literal("circuit")
                        .then(literal("add")
                                .then(argument("name", StringArgumentType.word())))
                        .then(literal("list"))
                        .then(literal("modify")
                                .then(argument("name", StringArgumentType.word())
                                        .then(literal("descriptions")
                                                .then(literal("list"))
                                                .then(literal("add")
                                                        .then(argument("name", StringArgumentType.word())))
                                                .then(literal("clear")))
                                        .then(literal("selection")
                                                .then(literal("give"))
                                                .then(literal("replace")))))));
    }
}
