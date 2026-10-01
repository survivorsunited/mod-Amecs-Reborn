package de.siphalor.amecs.test;

import com.blamejared.controlling.client.NewKeyBindsScreen;
import com.blamejared.controlling.api.event.ControllingEvents;
import com.blamejared.controlling.api.event.SetToDefaultEvent;
import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;

final class ControllingTest {
    static void run(MinecraftClient client) {
        var screen = new NewKeyBindsScreen(client.currentScreen, client.options);
        client.setScreen(screen);
        try {
            var searchField = NewKeyBindsScreen.class.getDeclaredField("search");
            searchField.setAccessible(true);
            var search = (net.minecraft.client.gui.widget.TextFieldWidget) searchField.get(screen);
            int original = screen.getKeyBindsList().children().size();
            search.setText("zzzz-no-such-binding-zzzz");
            ControlsTest.check(screen.getKeyBindsList().children().stream().noneMatch(e -> e instanceof com.blamejared.controlling.client.NewKeyBindsList.KeyEntry), "Controlling search filters entries");
            search.setText("");
            search.setFocused(false);
            ControlsTest.check(screen.getKeyBindsList().children().size() == original, "Controlling clear restores entries");
        } catch (ReflectiveOperationException exception) { throw new AssertionError(exception); }
        var binding = client.options.jumpKey;
        binding.setBoundKey(net.minecraft.client.util.InputUtil.UNKNOWN_KEY);
        screen.selectedKeyBinding = binding;
        screen.keyPressed(new KeyInput(341, 0, 2));
        ControlsTest.check(screen.selectedKeyBinding == binding, "modifier keeps capture active");
        screen.keyPressed(new KeyInput(74, 0, 2));
        ControlsTest.check(((IKeyBinding) binding).amecs$getKeyModifiers().get(KeyModifier.CONTROL), "Control+J captured");
        ControlsTest.check(screen.selectedKeyBinding == null, "normal key completes capture");
        client.options.write();
        ((IKeyBinding) binding).amecs$getKeyModifiers().unset();
        binding.setBoundKey(binding.getDefaultKey());
        client.options.load();
        ControlsTest.check(((IKeyBinding) binding).amecs$getKeyModifiers().get(KeyModifier.CONTROL), "modifier persistence");
        ControllingEvents.SET_TO_DEFAULT_EVENT.invoker().handle(new SetToDefaultEvent(client.options, binding));
        ControlsTest.check(binding.isDefault() && ((IKeyBinding) binding).amecs$getKeyModifiers().isUnset(), "reset removes modifiers");
        screen.selectedKeyBinding = binding;
        screen.keyPressed(new KeyInput(256, 0, 0));

        ControlsTest.check(binding.isUnbound(), "escape unbinds");

        ControllingEvents.SET_TO_DEFAULT_EVENT.invoker().handle(new SetToDefaultEvent(client.options, binding));
        screen.selectedKeyBinding = binding;
        screen.keyPressed(new KeyInput(341, 0, 2));
        screen.mouseClicked(new net.minecraft.client.gui.Click(1, 1, new net.minecraft.client.input.MouseInput(1, 2)), false);
        ControlsTest.check(((IKeyBinding) binding).amecs$getBoundKey().getCategory() == net.minecraft.client.util.InputUtil.Type.MOUSE, "mouse binding capture");
        ControlsTest.check(((IKeyBinding) binding).amecs$getKeyModifiers().get(KeyModifier.CONTROL), "Control+mouse capture");
        screen.resetButton().onPress(new KeyInput(257, 0, 0));
        screen.resetButton().onPress(new KeyInput(257, 0, 0));
        ControlsTest.check(binding.isDefault() && ((IKeyBinding) binding).amecs$getKeyModifiers().isUnset(), "Reset All removes modifiers");
        System.out.println("AMECS_CONTROLLING_TEST_PASS");
    }
}
