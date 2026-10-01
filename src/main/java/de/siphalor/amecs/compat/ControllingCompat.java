package de.siphalor.amecs.compat;

import com.blamejared.controlling.api.event.ControllingEvents;
import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.minecraft.client.util.InputUtil;

/** Loaded only when Controlling is installed; it is not a required or bundled dependency. */
public final class ControllingCompat {
    private ControllingCompat() {}

    public static void register() {
        ControllingEvents.KEY_ENTRY_RENDER_EVENT.register(event -> {
            if (event.isHovered() && event.getX() < event.getEntry().getBtnChangeKeyBinding().getX()) {
                String id = event.getEntry().getKey().getId() + ".amecs.description";
                if (net.minecraft.client.resource.language.I18n.hasTranslation(id)) {
                    var lines = java.util.Arrays.stream(net.minecraft.client.resource.language.I18n.translate(id).split("\n"))
                        .map(net.minecraft.text.Text::literal).map(text -> (net.minecraft.text.Text) text).toList();
                    event.getGuiGraphics().drawTooltip(net.minecraft.client.MinecraftClient.getInstance().textRenderer, lines, event.getX(), event.getY());
                }
            }
            return net.minecraft.util.Unit.INSTANCE;
        });
        ControllingEvents.IS_KEY_CODE_MODIFIER_EVENT.register(event -> KeyModifier.fromKey(event.key()) != KeyModifier.NONE);
        ControllingEvents.SET_KEY_EVENT.register(event -> {
            IKeyBinding binding = (IKeyBinding) event.mapping();
            KeyModifiers modifiers = binding.amecs$getKeyModifiers();
            InputUtil.Key previous = binding.amecs$getBoundKey();
            if (event.key().equals(InputUtil.UNKNOWN_KEY)) {
                modifiers.unset();
            } else {
                modifiers.copyModifiers(KeyModifiers.getCurrentlyPressed());
                modifiers.set(KeyModifier.fromKey(previous), true);
            }
            event.mapping().setBoundKey(event.key());
            modifiers.cleanup(event.mapping());
            return true;
        });
        ControllingEvents.SET_TO_DEFAULT_EVENT.register(event -> {
            if (event.mapping() instanceof AmecsKeyBinding binding) {
                binding.resetKeyBinding();
            } else {
                event.mapping().setBoundKey(event.mapping().getDefaultKey());
                ((IKeyBinding) event.mapping()).amecs$getKeyModifiers().unset();
            }
            return true;
        });
        ControllingEvents.KEY_ENTRY_MOUSE_CLICKED_EVENT.register(event -> {
            var button = event.getEntry().getBtnChangeKeyBinding();
            if (event.event().button() == 0 && button.active && button.isMouseOver(event.event().x(), event.event().y())) {
                var key = event.getEntry().getKey();
                ((IKeyBinding) key).amecs$getKeyModifiers().unset();
                key.setBoundKey(InputUtil.UNKNOWN_KEY);
            }
            return false;
        });
    }
}
