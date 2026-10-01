package de.siphalor.amecs.test;

import de.siphalor.amecs.gui.SearchFieldControlsListWidget;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import de.siphalor.amecs.api.KeyModifier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import java.util.Arrays;

public class ControlsTest implements ClientModInitializer {
    private int ticks;
    private boolean started;
    private KeybindsScreen screen;
    @Override public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (++ticks < 100) return;
            try {
                if (!started) {
                    started = true;
                    for (KeyBinding key : client.options.allKeys) {
                        key.setBoundKey(key.getDefaultKey());
                        ((IKeyBinding) key).amecs$getKeyModifiers().unset();
                    }
                    screen = new KeybindsScreen(client.currentScreen, client.options);
                    client.setScreen(screen);
                    return;
                }
                if (ticks == 105 && !FabricLoader.getInstance().isModLoaded("controlling")) testSearch(client);
                if (ticks == 135) net.minecraft.client.util.ScreenshotRecorder.saveScreenshot(client.runDirectory, "amecs-search.png", client.getFramebuffer(), 1, message -> {});
                if (ticks == 110 && FabricLoader.getInstance().isModLoaded("controlling")) ControllingTest.run(client);
                if (ticks == 180) {
                    System.out.println("AMECS_CONTROLS_TEST_PASS controlling=" + FabricLoader.getInstance().isModLoaded("controlling"));
                    client.scheduleStop();
                }
            } catch (Throwable error) {
                error.printStackTrace();
                System.out.println("AMECS_CONTROLS_TEST_FAIL");
                client.scheduleStop();
                throw new AssertionError(error);
            }
        });
    }
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private void testSearch(MinecraftClient client) {
        ControlsListWidget list = (ControlsListWidget) screen.children().stream().filter(c -> c instanceof ControlsListWidget).findFirst().orElseThrow();
        check(list.children().getFirst() instanceof SearchFieldControlsListWidget, "search entry present");
        TextFieldWidget field = (TextFieldWidget) list.children().getFirst().children().getFirst();
        int count = list.children().size();
        KeyBinding[] before = client.options.allKeys.clone();
        field.setText("zzzz-no-such-binding-zzzz");
        check(list.children().stream().noneMatch(e -> e instanceof ControlsListWidget.KeyBindingEntry), "no matches");
        field.setText("jump");
        check(list.children().size() > 1 && list.children().size() < count, "name search");
        field.setText("");
        check(list.children().size() == count, "clear restores entries");
        check(Arrays.equals(before, client.options.allKeys), "search preserves key array");
        KeyBinding jump = client.options.jumpKey;
        InputUtil.Key previous = ((IKeyBinding) jump).amecs$getBoundKey();
        jump.setBoundKey(InputUtil.UNKNOWN_KEY);
        field.setText("=");
        check(list.children().stream().filter(e -> e instanceof ControlsListWidget.KeyBindingEntry).count() > 0, "unbound search");
        jump.setBoundKey(previous);
        field.setText("=space");
        check(list.children().size() > 1, "key search");
        field.setText("");
        check(list.children().size() == count, "repeat clear");
        list.setFocused(list.children().getFirst());
        screen.setFocused(list);
        field.setFocused(true);
        screen.charTyped(new net.minecraft.client.input.CharInput('j', 0));
        check(field.getText().equals("j"), "character input routed");
        screen.keyPressed(new net.minecraft.client.input.KeyInput(259, 0, 0));
        check(field.getText().isEmpty(), "backspace routed");
        check(field.isFocused(), "search focus");
        ((IKeyBinding) jump).amecs$getKeyModifiers().set(KeyModifier.CONTROL, true);
        check(!jump.isDefault(), "modifier makes binding nondefault");
        ((IKeyBinding) jump).amecs$getKeyModifiers().unset();
        KeyBinding sprint = client.options.sprintKey;
        InputUtil.Key sprintKey = ((IKeyBinding) sprint).amecs$getBoundKey();
        sprint.setBoundKey(previous);
        field.setText("=%%");
        check(list.children().stream().filter(e -> e instanceof ControlsListWidget.KeyBindingEntry).count() >= 2, "conflict filter");
        ((IKeyBinding) sprint).amecs$getKeyModifiers().set(KeyModifier.CONTROL, true);
        field.setText("jump=%%");
        check(list.children().stream().noneMatch(e -> e instanceof ControlsListWidget.KeyBindingEntry && ((de.siphalor.amecs.impl.duck.IKeyBindingEntry) e).amecs$getKeyBinding() == jump), "different modifiers avoid conflicts");
        sprint.setBoundKey(sprintKey);
        ((IKeyBinding) sprint).amecs$getKeyModifiers().unset();
        field.setText("");
        screen.setFocused(null);
        field.setFocused(false);
        jump.setBoundKey(InputUtil.UNKNOWN_KEY);
        screen.selectedKeyBinding = jump;
        screen.keyPressed(new net.minecraft.client.input.KeyInput(341, 0, 2));
        screen.keyPressed(new net.minecraft.client.input.KeyInput(74, 0, 2));
        check(((IKeyBinding) jump).amecs$getKeyModifiers().get(KeyModifier.CONTROL), "vanilla Control+J capture");
        client.options.write();
        ((IKeyBinding) jump).amecs$getKeyModifiers().unset();
        jump.setBoundKey(jump.getDefaultKey());
        client.options.load();
        check(((IKeyBinding) jump).amecs$getKeyModifiers().get(KeyModifier.CONTROL), "vanilla modifier persistence");
        jump.setBoundKey(jump.getDefaultKey());
        ((IKeyBinding) jump).amecs$getKeyModifiers().unset();
        System.out.println("AMECS_SEARCH_TEST_PASS");
    }
}
