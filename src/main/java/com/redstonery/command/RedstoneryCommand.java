package com.redstonery.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.redstonery.Circuit;

import static net.minecraft.server.command.CommandManager.*;

import java.util.Set;

import net.minecraft.command.CommandException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class RedstoneryCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, Set<Circuit> circuits) {

        dispatcher.register(literal("redstonery")
                .requires(source -> source.hasPermissionLevel(1))
                .then(literal("circuit")
                        .then(literal("add")
                                .then(argument("name", StringArgumentType.word())
                                        .executes(ctx -> addCircuit(ctx, circuits))))
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

    private static int addCircuit(CommandContext<ServerCommandSource> ctx, Set<Circuit> circuits) {
        String circuitName = StringArgumentType.getString(ctx, "name");

        Circuit circuit = new Circuit(circuitName);

        if (containsCircuitWithName(circuits, circuitName)) {
            throw new CommandException(Text.translatable("commands.redstonery.error.circuit_exists", circuitName));
        }

        circuits.add(circuit);
            ctx.getSource().sendFeedback(() -> Text.translatable("commands.redstonery.addedCircuit", circuitName),
                    true);

            return Command.SINGLE_SUCCESS;
    }

    private static boolean containsCircuitWithName(Set<Circuit> circuits, String targetName) {
        for (Circuit circuit : circuits) {
            if (circuit.getName().equals(targetName)) {
                return true;
            }
        }
        return false;
    }
}
