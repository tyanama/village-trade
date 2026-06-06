package com.villagermod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class VillagerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("villagermod")
                .then(Commands.literal("unlimited")
                    .then(Commands.argument("value", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("on");
                            builder.suggest("off");
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String value = StringArgumentType.getString(ctx, "value");
                            VillagerConfig.unlimitedTrades = value.equalsIgnoreCase("on");
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "[VillagerMod] 取引上限なし: " + (VillagerConfig.unlimitedTrades ? "ON" : "OFF")
                            ), true);
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("bulk")
                    .then(Commands.argument("value", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("on");
                            builder.suggest("off");
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String value = StringArgumentType.getString(ctx, "value");
                            VillagerConfig.bulkTrade = value.equalsIgnoreCase("on");
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "[VillagerMod] 一括交換: " + (VillagerConfig.bulkTrade ? "ON" : "OFF")
                            ), true);
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("status")
                    .executes(ctx -> {
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "[VillagerMod] 取引上限なし: " + (VillagerConfig.unlimitedTrades ? "ON" : "OFF") +
                            " | 一括交換: " + (VillagerConfig.bulkTrade ? "ON" : "OFF")
                        ), false);
                        return 1;
                    })
                )
        );
    }
}
