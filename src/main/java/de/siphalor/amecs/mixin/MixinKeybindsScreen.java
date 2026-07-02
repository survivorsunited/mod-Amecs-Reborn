/*
 * Copyright 2020-2023 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.siphalor.amecs.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Siphalor
 */
@SuppressWarnings("WeakerAccess")
@Environment(EnvType.CLIENT)
@Mixin(KeybindsScreen.class)
public abstract class MixinKeybindsScreen extends GameOptionsScreen {
	@Shadow
	public KeyBinding selectedKeyBinding;

	@Shadow
	public long lastKeyCodeUpdateTime;

	@Shadow private ControlsListWidget controlsList;

	@Shadow private ButtonWidget resetAllButton;

	public MixinKeybindsScreen(Screen screen, GameOptions gameOptions, Text text) {
		super(screen, gameOptions, text);
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void amecs$ensureResetAllButton(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo callbackInfo) {
		if (this.resetAllButton == null) {
			this.resetAllButton = ButtonWidget.builder(Text.translatable("controls.resetAll"), button -> {
				for (KeyBinding keyBinding : this.gameOptions.allKeys) {
					keyBinding.setBoundKey(keyBinding.getDefaultKey());
					((IKeyBinding) keyBinding).amecs$getKeyModifiers().unset();
					if (keyBinding instanceof AmecsKeyBinding amecsKeyBinding) {
						amecsKeyBinding.resetKeyBinding();
					}
				}
				if (this.controlsList != null) {
					this.controlsList.update();
				}
			}).dimensions(0, 0, 150, 20).build();
		}
	}

	@Inject(
			method = "mouseClicked",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/option/KeyBinding;setBoundKey(Lnet/minecraft/client/util/InputUtil$Key;)V"
			)
	)
	public void onClicked(Click click, boolean doubleClick, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		InputUtil.Key key = ((IKeyBinding) selectedKeyBinding).amecs$getBoundKey();
		KeyModifiers keyModifiers = ((IKeyBinding) selectedKeyBinding).amecs$getKeyModifiers();
		if (!key.equals(InputUtil.UNKNOWN_KEY)) {
			keyModifiers.set(KeyModifier.fromKey(key), true);
		}

	}

	@Inject(
			method = "keyPressed",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/option/KeyBinding;setBoundKey(Lnet/minecraft/client/util/InputUtil$Key;)V",
					ordinal = 0
			)
	)
	public void clearKeyBinding(KeyInput input, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		((IKeyBinding) selectedKeyBinding).amecs$getKeyModifiers().unset();
	}

	@Inject(
			method = "keyPressed",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/option/KeyBinding;setBoundKey(Lnet/minecraft/client/util/InputUtil$Key;)V",
					ordinal = 1
			),
			cancellable = true
	)
	public void onKeyPressed(KeyInput input, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (selectedKeyBinding.isUnbound()) {
			selectedKeyBinding.setBoundKey(InputUtil.fromKeyCode(input));
		} else {
			InputUtil.Key mainKey = ((IKeyBinding) selectedKeyBinding).amecs$getBoundKey();
			KeyModifiers keyModifiers = ((IKeyBinding) selectedKeyBinding).amecs$getKeyModifiers();
			KeyModifier mainKeyModifier = KeyModifier.fromKey(mainKey);
			KeyModifier keyModifier = KeyModifier.fromKeyCode(input.key());
			if (mainKeyModifier != KeyModifier.NONE && keyModifier == KeyModifier.NONE) {
				keyModifiers.set(mainKeyModifier, true);
				selectedKeyBinding.setBoundKey(InputUtil.fromKeyCode(input));
				return;
			} else {
				keyModifiers.set(keyModifier, true);
				keyModifiers.cleanup(selectedKeyBinding);
			}
		}

		this.lastKeyCodeUpdateTime = Util.getMeasuringTimeMs();
		this.controlsList.update();
		callbackInfoReturnable.setReturnValue(true);
	}

	@Inject(
			method = "method_60342",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/option/KeyBinding;setBoundKey(Lnet/minecraft/client/util/InputUtil$Key;)V"
			)
	)
	public void onSetToDefault(ButtonWidget btn, CallbackInfo ci, @Local KeyBinding keyBinding) {
		if (keyBinding instanceof AmecsKeyBinding) {
			((AmecsKeyBinding) keyBinding).resetKeyBinding();
		} else {
			((IKeyBinding) keyBinding).amecs$getKeyModifiers().unset();
		}
	}
}
