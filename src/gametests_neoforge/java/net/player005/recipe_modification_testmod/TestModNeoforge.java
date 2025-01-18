package net.player005.recipe_modification_testmod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod("recipe_modification_testmod")
public class TestModNeoforge {

    public TestModNeoforge(IEventBus eventBus) {
        TestMod.init();
    }

}
