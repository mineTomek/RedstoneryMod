package com.redstonery.command;

import com.google.gson.Gson;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.redstonery.Circuit;
import com.redstonery.CircuitBlock;
import com.redstonery.Redstonery;
import com.redstonery.StateSaverAndLoader;

import static net.minecraft.server.command.CommandManager.*;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.List;

import java.lang.reflect.Method;

import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.DaylightDetectorBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.RedstoneBlock;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.WallRedstoneTorchBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.block.enums.ComparatorMode;
import net.minecraft.command.CommandException;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class RedstoneryCommand {
        public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
                LiteralCommandNode<ServerCommandSource> addNode = literal("add").then(
                                argument("name", StringArgumentType.word()).executes(ctx -> addCircuit(ctx)))
                                .build();

                LiteralCommandNode<ServerCommandSource> listNode = literal("list")
                                .executes(ctx -> listCircuits(ctx))
                                .then(literal("clear").executes(ctx -> clearCircuits(ctx)))
                                .build();

                LiteralCommandNode<ServerCommandSource> seeNode = literal("see")
                                .then(argument("name", StringArgumentType.word()).executes(ctx -> seeCircuit(ctx, false))
                                                .then(literal("withBlocks").executes(ctx -> seeCircuit(ctx, true))))
                                .build();

                LiteralCommandNode<ServerCommandSource> descriptionsNode = literal("descriptions")
                                .then(argument("name", StringArgumentType.word())
                                        .then(literal("list").executes(ctx -> listDescriptions(ctx)))
                                        .then(literal("add").then(argument("description", StringArgumentType.greedyString()).executes(ctx -> addDescription(ctx))))
                                        .then(literal("clear").executes(ctx -> clearDescriptions(ctx)))
                                ).build();

                LiteralCommandNode<ServerCommandSource> saveNode = literal("save")
                                .then(literal("to").then(
                                        argument("name", StringArgumentType.word()).executes(ctx -> saveSelection(ctx))))
                                .build();

                LiteralCommandNode<ServerCommandSource> exportNode = literal("export")
                                .then(literal("all").executes(ctx -> exportSelection(ctx)))
                                .build();

                dispatcher.register(literal("circuit")
                                .requires(source -> source.hasPermissionLevel(1))
                                .then(addNode)
                                .then(listNode)
                                .then(seeNode)
                                .then(saveNode)
                                .then(descriptionsNode)
                                .then(exportNode));

        }

        private static int addCircuit(CommandContext<ServerCommandSource> ctx) {
                String circuitName = StringArgumentType
                                .getString(ctx, "name");

                HashSet<Circuit> circuits = getCircuits(ctx.getSource()
                                .getServer());

                if (getCircuitByName(circuits, circuitName) != null) {
                        throw new CommandException(
                                        Text.translatable(
                                                        "commands.redstonery.error.circuit_exists",
                                                        circuitName));
                }

                Circuit circuit = new Circuit(circuitName);

                circuits.add(circuit);
                
                ctx.getSource().sendFeedback(
                                () -> Text.translatable(
                                                "commands.redstonery.addedCircuit",
                                                circuitName),
                                true);

                return Command.SINGLE_SUCCESS;
        }

        private static int listCircuits(CommandContext<ServerCommandSource> ctx) {
                HashSet<Circuit> circuits = getCircuits(ctx.getSource()
                                .getServer());

                if (circuits.isEmpty()) {
                        ctx.getSource().sendFeedback(
                                        () -> Text.translatable(
                                                        "commands.redstonery.circuitListEmpty"),
                                        true);
                } else {
                        ctx.getSource().sendFeedback(
                                        () -> Text.translatable(
                                                        "commands.redstonery.listCircuits"),
                                        true);

                        for (Circuit circuit : circuits) {
                                ctx.getSource().sendFeedback(
                                                () -> Text.of("- "
                                                                + circuit.getName()),
                                                true);
                        }
                }

                return Command.SINGLE_SUCCESS;
        }

        private static int clearCircuits(CommandContext<ServerCommandSource> ctx) {
                getCircuits(ctx.getSource()
                                .getServer()).clear();

                ctx.getSource().sendFeedback(
                                () -> Text.translatable(
                                                "commands.redstonery.clearedCircuits"),
                                true);

                return Command.SINGLE_SUCCESS;
        }

        private static int seeCircuit(CommandContext<ServerCommandSource> ctx, boolean withBlocks) {
                Circuit currentCircuit = null;
                for (Circuit circuit : getCircuits(
                                ctx.getSource().getServer())) {
                        if (circuit.getName()
                                        .equals(StringArgumentType
                                                        .getString(ctx, "name"))) {
                                currentCircuit = circuit;
                        }
                }
                if (currentCircuit == null) {
                        throw new CommandException(
                                        Text.translatable(
                                                        "commands.redstonery.error.selection.nonexistent_circuit_name",
                                                        StringArgumentType
                                                                        .getString(ctx, "name")));
                }

                String circuitName = currentCircuit.getName();
                ctx.getSource().sendFeedback(
                                () -> Text.of(circuitName + ":"),
                                true);

                ctx.getSource().sendFeedback(
                                () -> Text.translatable("commands.redstonery.see.descriptions"),
                                true);

                if (currentCircuit
                                .getDescriptions()
                                .isEmpty()) {
                        ctx.getSource().sendFeedback(
                                        () -> Text.translatable("commands.redstonery.see.no_descriptions"),
                                        true);
                } else {
                        for (String description : currentCircuit
                                        .getDescriptions()) {
                                ctx.getSource().sendFeedback(
                                                () -> Text.of("- " + description),
                                                true);
                        }
                }

                int blockCount = currentCircuit
                                .getBlocks()
                                .size();

                ctx.getSource().sendFeedback(
                                () -> Text.translatable("commands.redstonery.see.blocks_count", blockCount),
                                true);

                if (withBlocks) {
                        boolean odd = true;
                        for (CircuitBlock block : currentCircuit.getBlocks()) {
                                boolean isOdd = odd;
                                ctx.getSource().sendFeedback(
                                                () -> Text.literal(new Gson()
                                                                .toJson(block))
                                                                .formatted(isOdd ? Formatting.GRAY : Formatting.RESET),
                                                true);

                                odd = !odd;
                        }
                }

                return Command.SINGLE_SUCCESS;
        }

        private static int saveSelection(CommandContext<ServerCommandSource> ctx) {
                ItemStack stack = ctx.getSource().getPlayer()
                                .getStackInHand(Hand.MAIN_HAND);

                BlockPos pos1 = null;
                BlockPos pos2 = null;

                if (stack.getItem() == Redstonery.REDSTONE_SELECTOR) {
                        stack.getOrCreateNbt();

                        if (stack.getNbt()
                                        .contains("redstonery.pos1")) {
                                int[] nbtPos1 = stack
                                                .getNbt()
                                                .getIntArray("redstonery.pos1");

                                pos1 = new BlockPos(
                                                nbtPos1[0],
                                                nbtPos1[1],
                                                nbtPos1[2]);
                        }

                        if (stack.getNbt()
                                        .contains("redstonery.pos2")) {
                                int[] nbtPos2 = stack
                                                .getNbt()
                                                .getIntArray("redstonery.pos2");

                                pos2 = new BlockPos(
                                                nbtPos2[0],
                                                nbtPos2[1],
                                                nbtPos2[2]);
                        }

                        if (pos1 == null && pos2 == null) {
                                throw new CommandException(
                                                Text.translatable("commands.redstonery.error.selection.empty"));
                        } else if (pos1 == null
                                        || pos2 == null) {
                                throw new CommandException(
                                                Text.translatable("commands.redstonery.error.selection.not_complete"));
                        }

                        Circuit currentCircuit = null;
                        for (Circuit circuit : getCircuits(
                                        ctx.getSource().getServer())) {
                                if (circuit.getName()
                                                .equals(StringArgumentType
                                                                .getString(ctx, "name"))) {
                                        currentCircuit = circuit;
                                }
                        }
                        if (currentCircuit == null) {
                                throw new CommandException(
                                                Text.translatable(
                                                                "commands.redstonery.error.selection.nonexistent_circuit_name",
                                                                StringArgumentType
                                                                                .getString(ctx, "name")));
                        }

                        Set<CircuitBlock> blocks = new HashSet<CircuitBlock>();

                        for (int x = Math.min(pos1.getX(), pos2.getX()); x <= Math.max(pos1.getX(), pos2.getX()); x++) {
                                for (int y = Math.min(pos1.getY(), pos2.getY()); y <= Math.max(pos1.getY(),
                                                pos2.getY()); y++) {
                                        for (int z = Math.min(pos1.getZ(), pos2.getZ()); z <= Math.max(pos1.getZ(),
                                                        pos2.getZ()); z++) {
                                                BlockPos pos = new BlockPos(x, y, z);

                                                BlockState state = ctx.getSource().getWorld()
                                                                .getBlockState(pos);

                                                try {
                                                        saveBlock(ctx, blocks, pos, state);
                                                } catch (Exception e) {
                                                        Redstonery.LOGGER.info(e.getClass().getName());
                                                        Redstonery.LOGGER.info(e.getMessage());
                                                }
                                        }
                                }
                        }

                        currentCircuit.setBlocks(blocks);

                        ctx.getSource().sendFeedback(() -> Text.translatable("commands.redstonery.save.success", StringArgumentType.getString(ctx, "name")), false);
                } else {
                        throw new CommandException(
                                        Text.translatable("commands.redstonery.selection.player_not_holding_selector"));
                }

                return Command.SINGLE_SUCCESS;
        }

        private static void saveBlock(
                        CommandContext<ServerCommandSource> ctx,
                        Set<CircuitBlock> blocks,
                        BlockPos pos,
                        BlockState state) {
                Direction direction = getDirection(state);
                int powered = getPower(
                                pos,
                                state,
                                ctx.getSource().getWorld());
                boolean locked = getLocked(
                                state);
                int delay = getDelay(
                                state);
                boolean subtract = getSubtract(state);
                boolean open = getOpen(
                                state);
                int page = getPage(
                                state,
                                pos,
                                ctx.getSource().getWorld());
                boolean inverted = getInverted(state);

                CircuitBlock circuitBlock = new CircuitBlock(
                                Registries.BLOCK.getId(
                                                state.getBlock())
                                                .toString(),
                                direction,
                                powered,
                                locked,
                                delay,
                                subtract,
                                open,
                                page,
                                inverted);

                blocks.add(circuitBlock);
        }

        private static Direction getDirection(
                        BlockState state) {
                Direction direction = null;

                if (state.getBlock() instanceof HorizontalFacingBlock)
                        direction = state.get(HorizontalFacingBlock.FACING);
                if (state.getBlock() instanceof FacingBlock)
                        direction = state.get(FacingBlock.FACING);
                if (state.getBlock() instanceof DispenserBlock)
                        direction = state
                                        .get(DispenserBlock.FACING);
                if (state.getBlock() instanceof StairsBlock)
                        direction = state.get(StairsBlock.FACING);
                if (state.getBlock() instanceof DoorBlock)
                        direction = state.get(DoorBlock.FACING);
                if (state.getBlock() instanceof ChestBlock)
                        direction = state.get(ChestBlock.FACING);
                if (state.getBlock() instanceof LecternBlock)
                        direction = state.get(LecternBlock.FACING);
                if (state.getBlock() instanceof WallTorchBlock)
                        direction = state.get(WallTorchBlock.FACING);
                if (state.getBlock() instanceof WallRedstoneTorchBlock)
                        direction = state.get(WallRedstoneTorchBlock.FACING);

                if (direction == null)
                        return null;

                return direction;
        }

        private static int getPower(
                        BlockPos pos,
                        BlockState state,
                        ServerWorld world) {
                Integer power = null;

                if (state.getBlock() instanceof RedstoneBlock)
                        power = 15;
                if (state.getBlock() instanceof ButtonBlock)
                        power = state.get(ButtonBlock.POWERED) ? 15 : 0;
                if (state.getBlock() instanceof LeverBlock)
                        power = state.get(LeverBlock.POWERED) ? 15 : 0;
                if (state.getBlock() instanceof RepeaterBlock)
                        power = state.get(RepeaterBlock.POWERED) ? 15 : 0;
                if (state.getBlock() instanceof RedstoneTorchBlock)
                        power = state.get(RedstoneTorchBlock.LIT) ? 15 : 0;
                if (state.getBlock() instanceof RedstoneWireBlock)
                        power = state.get(RedstoneWireBlock.POWER);
                if (state.getBlock() instanceof ComparatorBlock) {
                        List<Method> methods = Arrays.asList(ComparatorBlock.class
                                        .getDeclaredMethods());
                        for (Method method : methods) {
                                if (method.getName() == "calculateOutputSignal") {
                                        method.setAccessible(true);
                                        try {
                                                power = (int) method.invoke(
                                                                (ComparatorBlock) state
                                                                                .getBlock(),
                                                                world,
                                                                pos,
                                                                state);
                                        } catch (Exception e) {
                                                return 0;
                                        }
                                }
                        }
                }

                if (power == null)
                        return 0;
                return power;
        }

        private static boolean getLocked(
                        BlockState state) {
                if (state.getBlock() instanceof RepeaterBlock) {
                        return state.get(RepeaterBlock.LOCKED);
                }

                return false;
        }

        private static int getDelay(
                        BlockState state) {
                if (state.getBlock() instanceof RepeaterBlock) {
                        return state.get(RepeaterBlock.DELAY);
                }

                return 0;
        }

        private static boolean getSubtract(
                        BlockState state) {
                if (state.getBlock() instanceof ComparatorBlock) {
                        return state.get(ComparatorBlock.MODE) == ComparatorMode.SUBTRACT;
                }

                return false;
        }

        private static boolean getOpen(BlockState state) {
                if (state.getBlock() instanceof DoorBlock) {
                        return state.get(DoorBlock.OPEN);
                }

                if (state.getBlock() instanceof TrapdoorBlock) {
                        return state.get(TrapdoorBlock.OPEN);
                }

                return false;
        }

        private static int getPage(
                        BlockState state,
                        BlockPos pos,
                        ServerWorld world) {
                if (state.getBlock() instanceof LecternBlock) {
                        return ((LecternBlockEntity) world
                                        .getBlockEntity(pos))
                                        .getCurrentPage();
                }

                return 0;
        }

        private static boolean getInverted(BlockState state) {
                if (state.getBlock() instanceof DaylightDetectorBlock) {
                        return state.get(DaylightDetectorBlock.INVERTED);
                }

                return false;
        }

        private static int addDescription(CommandContext<ServerCommandSource> ctx) {
                Circuit currentCircuit = null;
                for (Circuit circuit : getCircuits(
                                ctx.getSource().getServer())) {
                        if (circuit.getName()
                                        .equals(StringArgumentType
                                                        .getString(ctx, "name"))) {
                                currentCircuit = circuit;
                        }
                }
                if (currentCircuit == null) {
                        throw new CommandException(Text.translatable(
                                "commands.redstonery.error.selection.nonexistent_circuit_name",
                                StringArgumentType
                                .getString(ctx, "name")));
                }

                String newDescription = StringArgumentType.getString(ctx, "description");

                currentCircuit.addDescription(newDescription);

                ctx.getSource().sendFeedback(() -> Text.translatable("commands.redstonery.description.add.success", StringArgumentType.getString(ctx, "name")), false);

                return Command.SINGLE_SUCCESS;
        }

        private static int clearDescriptions(CommandContext<ServerCommandSource> ctx) {
                Circuit currentCircuit = null;
                for (Circuit circuit : getCircuits(
                                ctx.getSource().getServer())) {
                        if (circuit.getName()
                                        .equals(StringArgumentType
                                                        .getString(ctx, "name"))) {
                                currentCircuit = circuit;
                        }
                }
                if (currentCircuit == null) {
                        throw new CommandException(Text.translatable(
                                "commands.redstonery.error.selection.nonexistent_circuit_name",
                                StringArgumentType
                                .getString(ctx, "name")));
                }

                currentCircuit.clearDescriptions();

                ctx.getSource().sendFeedback(() -> Text.translatable("commands.redstonery.descriptions.clear.success", StringArgumentType.getString(ctx, "name")), false);

                return Command.SINGLE_SUCCESS;
        }

        private static int listDescriptions(CommandContext<ServerCommandSource> ctx) {
                Circuit currentCircuit = null;
                for (Circuit circuit : getCircuits(
                                ctx.getSource().getServer())) {
                        if (circuit.getName()
                                        .equals(StringArgumentType
                                                        .getString(ctx, "name"))) {
                                currentCircuit = circuit;
                        }
                }
                if (currentCircuit == null) {
                        throw new CommandException(Text.translatable(
                                "commands.redstonery.error.selection.nonexistent_circuit_name",
                                StringArgumentType
                                .getString(ctx, "name")));
                }

                if (currentCircuit.getDescriptions().isEmpty()) {
                        ctx.getSource().sendFeedback(() -> Text.translatable("commands.redstonery.descriptions.empty", StringArgumentType.getString(ctx, "name")), false);
                        return Command.SINGLE_SUCCESS;
                }

                boolean odd = true;
                for (String description : currentCircuit.getDescriptions()) {
                        boolean isOdd = odd;
                        ctx.getSource().sendFeedback(
                                        () -> Text.literal(description)
                                                        .formatted(isOdd ? Formatting.GRAY : Formatting.RESET),
                                        true);

                        odd = !odd;
                }

                return Command.SINGLE_SUCCESS;
        }

        private static Circuit getCircuitByName(
                        HashSet<Circuit> circuits,
                        String targetName) {
                for (Circuit circuit : circuits) {
                        if (circuit.getName().equals(targetName)) {
                                return circuit;
                        }
                }
                return null;
        }

        private static HashSet<Circuit> getCircuits(
                        MinecraftServer server) {
                return StateSaverAndLoader.getServerState(server).circuits;
        }
}
