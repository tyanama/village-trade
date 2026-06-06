package com.villagermod;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod(VillagerMod.MOD_ID)
public class VillagerMod {

    public static final String MOD_ID = "villagermod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VillagerMod(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(new VillagerEventHandler());
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        LOGGER.info("VillagerMod が読み込まれました！");
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        VillagerCommand.register(event.getDispatcher());
    }
}
