/* Copyright 2020-2023 Siphalor. Licensed under Apache-2.0. */
package de.siphalor.amecs.mixin;

import de.siphalor.amecs.gui.SearchFieldControlsListWidget;
import de.siphalor.amecs.impl.MetaOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ControlsListWidget.class)
public abstract class MixinEntryListWidget {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void amecs$addSearch(KeybindsScreen screen, MinecraftClient client, CallbackInfo ci) {
        ControlsListWidget list = (ControlsListWidget) (Object) this;
        if (list.getClass() == ControlsListWidget.class && MetaOptions.get().searchBar) {
            var entries = new java.util.ArrayList<>(list.children());
            entries.addFirst(new SearchFieldControlsListWidget(client, list));
            list.replaceEntries(entries);
        }
    }
}
