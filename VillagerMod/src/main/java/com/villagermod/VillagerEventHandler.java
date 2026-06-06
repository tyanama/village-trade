package com.villagermod;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class VillagerEventHandler {

    // ---- 取引上限をリセット ----
    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Post event) {
        if (!VillagerConfig.unlimitedTrades) return;
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (villager.level().isClientSide()) return;

        MerchantOffers offers = villager.getOffers();
        if (offers == null) return;

        for (MerchantOffer offer : offers) {
            if (offer.isOutOfStock()) {
                offer.resetUses();
            }
        }
    }

    // ---- しゃがみ+右クリックで一括交換 ----
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (!VillagerConfig.bulkTrade) return;
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!player.isShiftKeyDown()) return;
        if (!(event.getTarget() instanceof Villager villager)) return;

        MerchantOffers offers = villager.getOffers();
        if (offers == null || offers.isEmpty()) return;

        for (MerchantOffer offer : offers) {
            if (offer.isOutOfStock()) continue;

            ItemStack costA = offer.getCostA();
            ItemStack costB = offer.getCostB();
            ItemStack result = offer.assemble();

            int times = countPossibleTrades(player, costA, costB, offer.getMaxUses() - offer.getUses());
            if (times <= 0) continue;

            for (int i = 0; i < times; i++) {
                if (!consumeItems(player, costA, costB)) break;
                ItemStack resultCopy = result.copy();
                if (!player.getInventory().add(resultCopy)) {
                    player.drop(resultCopy, false);
                }
                offer.increaseUses();
            }

            villager.notifyTrade(offer);
            VillagerMod.LOGGER.info("{} が {}回 一括交換しました", player.getName().getString(), times);
            break;
        }

        event.setCanceled(true);
    }

    private int countPossibleTrades(Player player, ItemStack costA, ItemStack costB, int maxTrades) {
        int timesA = countItemsInInventory(player, costA) / Math.max(1, costA.getCount());
        int timesB = costB.isEmpty() ? Integer.MAX_VALUE
                : countItemsInInventory(player, costB) / Math.max(1, costB.getCount());
        return Math.min(Math.min(timesA, timesB), maxTrades);
    }

    private int countItemsInInventory(Player player, ItemStack target) {
        if (target.isEmpty()) return Integer.MAX_VALUE;
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (ItemStack.isSameItemSameComponents(stack, target)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private boolean consumeItems(Player player, ItemStack costA, ItemStack costB) {
        if (!costA.isEmpty() && countItemsInInventory(player, costA) < costA.getCount()) return false;
        if (!costB.isEmpty() && countItemsInInventory(player, costB) < costB.getCount()) return false;
        if (!costA.isEmpty()) removeItems(player, costA);
        if (!costB.isEmpty()) removeItems(player, costB);
        return true;
    }

    private void removeItems(Player player, ItemStack target) {
        int remaining = target.getCount();
        for (ItemStack stack : player.getInventory().items) {
            if (remaining <= 0) break;
            if (ItemStack.isSameItemSameComponents(stack, target)) {
                int remove = Math.min(stack.getCount(), remaining);
                stack.shrink(remove);
                remaining -= remove;
            }
        }
    }
}
