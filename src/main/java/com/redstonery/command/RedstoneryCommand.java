package com.redstonery.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.redstonery.Circuit;
import com.redstonery.StateSaverAndLoader;

import static net.minecraft.server.command.CommandManager.*;

import java.util.HashSet;

import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class RedstoneryCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
    literal("redstonery")
        .requires(source -> source.hasPermissionLevel(1))
        .then(
            literal("circuit")
                .then(
                    literal("add")
                        .then(
                            argument("name", StringArgumentType.word())
                                .executes(ctx -> addCircuit(ctx))
                        )
                )
                .then(
                    literal("list")
                        .executes(ctx -> listCircuits(ctx))
                        .then(
                            literal("clear")
                                .executes(ctx -> clearCircuits(ctx))
                        )
                )
                .then(
                    literal("modify")
                        .then(
                            argument("name", StringArgumentType.word())
                                .then(
                                    literal("descriptions")
                                        .then(
                                            literal("list")
                                        )
                                        .then(
                                            literal("add")
                                                .then(
                                                    argument("name", StringArgumentType.word())
                                                )
                                        )
                                        .then(
                                            literal("clear")
                                        )
                                )
                        )
                        .then(
                            literal("selection")
                                .then(
                                    literal("give")
                                )
                                .then(
                                    literal("replace")
                                )
                        )
                )
        )
);

    }

    private static int addCircuit(CommandContext<ServerCommandSource> ctx) {
        String circuitName = StringArgumentType.getString(ctx, "name");

        Circuit circuit = new Circuit(circuitName);

        HashSet<Circuit> circuits = getCircuits(ctx.getSource().getServer());

        if (containsCircuitWithName(circuits, circuitName)) {
            throw new CommandException(Text.translatable("commands.redstonery.error.circuit_exists", circuitName));
        }

        circuits.add(circuit);
            ctx.getSource().sendFeedback(() -> Text.translatable("commands.redstonery.addedCircuit", circuitName),
                    true);

            return Command.SINGLE_SUCCESS;
    }

    private static int listCircuits(CommandContext<ServerCommandSource> ctx) {
        HashSet<Circuit> circuits = getCircuits(ctx.getSource().getServer());

        if (circuits.isEmpty()) {
                ctx.getSource().sendFeedback(() -> Text.translatable("commands.redstonery.circuitListEmpty"), true);
        } else {
                for (Circuit circuit : circuits) {
                        ctx.getSource().sendFeedback(() -> Text.of(circuit.getName()), true);
                }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int clearCircuits(CommandContext<ServerCommandSource> ctx) {
        getCircuits(ctx.getSource().getServer()).clear();

        ctx.getSource().sendFeedback(() -> Text.translatable("commands.redstonery.clearedCircuits"), true);

        return Command.SINGLE_SUCCESS;
    }

    private static boolean containsCircuitWithName(HashSet<Circuit> circuits, String targetName) {
        for (Circuit circuit : circuits) {
            if (circuit.getName().equals(targetName)) {
                return true;
            }
        }
        return false;
    }

    private static HashSet<Circuit> getCircuits(MinecraftServer server) {
        return StateSaverAndLoader.getServerState(server).circuits;
    }
}
