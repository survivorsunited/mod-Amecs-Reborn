package de.siphalor.amecs.mixin;

import de.siphalor.amecs.Amecs;
import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.impl.KeyBindingManager;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Siphalor
 */
@Mixin(Keyboard.class)
public abstract class MixinKeyboard {
	@ModifyVariable(
			method = "onKey",
			argsOnly = true,
			ordinal = 0,
			at = @At(value = "FIELD", target = "Lnet/minecraft/client/Keyboard;debugCrashStartTime:J")
	)
	public int modifyPressedKey(int key, long window, int action, KeyInput input) {
		KeyInput adjustedInput = new KeyInput(key, input.scancode(), input.modifiers());
		if (Amecs.ESCAPE_KEYBINDING != null && Amecs.ESCAPE_KEYBINDING.matchesKey(adjustedInput)) {
			return GLFW.GLFW_KEY_ESCAPE;
		}
		return key;
	}

	@Inject(method = "onKey", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", ordinal = 0, shift = At.Shift.BEFORE), cancellable = true)
	private void onKeyPriority(long window, int action, KeyInput input, CallbackInfo callbackInfo) {
		InputUtil.Key inputKey = InputUtil.fromKeyCode(input);
		if (action == InputUtil.GLFW_PRESS) {
			if (KeyBindingManager.onKeyPressedPriority(inputKey)) {
				callbackInfo.cancel();
			}
		} else if (action == InputUtil.GLFW_RELEASE) {
			if (KeyBindingManager.onKeyReleasedPriority(inputKey)) {
				callbackInfo.cancel();
			}
		}
	}

	@Inject(method = "onKey", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Keyboard;debugCrashStartTime:J", ordinal = 0))
	private void onKey(long window, int action, KeyInput input, CallbackInfo callbackInfo) {
		// Key released
		if (action == InputUtil.GLFW_RELEASE && MinecraftClient.getInstance().currentScreen instanceof KeybindsScreen screen) {
            screen.selectedKeyBinding = null;
			screen.lastKeyCodeUpdateTime = Util.getMeasuringTimeMs();
		}

		Amecs.CURRENT_MODIFIERS.set(KeyModifier.fromKeyCode(InputUtil.fromKeyCode(input).getCode()), action != InputUtil.GLFW_RELEASE);
	}
}
