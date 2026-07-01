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

package de.siphalor.amecs.api;

import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link net.minecraft.client.option.KeyBinding} base class to be used when you want to define modifiers keys as default
 * @author Siphalor
 */
@Environment(EnvType.CLIENT)
public class AmecsKeyBinding extends KeyBinding {
	private static final Map<String, KeyBinding.Category> CATEGORY_CACHE = new HashMap<>();
	private final KeyModifiers defaultModifiers;

	/**
	 * Constructs a new amecs keybinding. And because how the vanilla key binding works. It is automatically registered.
	 * <br>
	 * See {@link KeyBindingUtils#unregisterKeyBinding(KeyBinding)} for how to unregister it
	 * If you want to set the key's translationKey directly use {@link #AmecsKeyBinding(String, net.minecraft.client.util.InputUtil.Type, int, String, KeyModifiers)} instead
	 *
	 * @param id the id to use
	 * @param type the input type which triggers this keybinding
	 * @param code the default key code
	 * @param category the id of the category which should include this keybinding
	 * @param defaultModifiers the default modifiers
	 */
	public AmecsKeyBinding(Identifier id, InputUtil.Type type, int code, String category, KeyModifiers defaultModifiers) {
		this("key." + id.getNamespace() + "." + id.getPath(), type, code, category, defaultModifiers);
	}

	/**
	 * Constructs a new amecs keybinding. And because how the vanilla key binding works. It is automatically registered.
	 * <br>
	 * See {@link KeyBindingUtils#unregisterKeyBinding(KeyBinding)} for how to unregister it
	 *
	 * @param id the id to use
	 * @param type the input type which triggers this keybinding
	 * @param code the default key code
	 * @param category the id of the category which should include this keybinding
	 * @param defaultModifiers the default modifiers
	 */
	public AmecsKeyBinding(String id, InputUtil.Type type, int code, String category, KeyModifiers defaultModifiers) {
		super(id, type, code, categoryFromString(category));
		if (defaultModifiers == null || defaultModifiers == KeyModifiers.NO_MODIFIERS) {
			defaultModifiers = new KeyModifiers(); // the modifiable version of: KeyModifiers.NO_MODIFIERS
		}
		this.defaultModifiers = defaultModifiers;
		((IKeyBinding) this).amecs$getKeyModifiers().copyModifiers(this.defaultModifiers);
	}

	private static KeyBinding.Category categoryFromString(String category) {
		return switch (category) {
			case "key.categories.movement" -> KeyBinding.Category.MOVEMENT;
			case "key.categories.misc" -> KeyBinding.Category.MISC;
			case "key.categories.multiplayer" -> KeyBinding.Category.MULTIPLAYER;
			case "key.categories.gameplay" -> KeyBinding.Category.GAMEPLAY;
			case "key.categories.inventory" -> KeyBinding.Category.INVENTORY;
			case "key.categories.creative" -> KeyBinding.Category.CREATIVE;
			case "key.categories.spectator" -> KeyBinding.Category.SPECTATOR;
			case "key.categories.debug" -> KeyBinding.Category.DEBUG;
			case "key.categories.ui" -> KeyBinding.Category.MISC;
			case "amecs.key.categories.skin_layers" -> CATEGORY_CACHE.computeIfAbsent(category, ignored -> KeyBinding.Category.create(Identifier.of("amecs", "skin_layers")));
			default -> CATEGORY_CACHE.computeIfAbsent(category, AmecsKeyBinding::createCategory);
		};
	}

	private static KeyBinding.Category createCategory(String category) {
		Identifier id;
		if (category.contains(":")) {
			id = Identifier.of(category);
		} else if (category.startsWith("key.categories.")) {
			id = Identifier.ofVanilla(category.substring("key.categories.".length()));
		} else {
			id = Identifier.of("amecs", category.replace('.', '_').replace(' ', '_').toLowerCase());
		}
		return KeyBinding.Category.create(id);
	}

	@Override
	public void setPressed(boolean pressed) {
		super.setPressed(pressed);
		if (pressed) {
			onPressed();
		} else {
			onReleased();
		}
	}

	/**
	 * A convenience method which gets fired when the keybinding is used
	 */
	public void onPressed() {
	}

	/**
	 * A convenience method which gets fired when the keybinding is stopped being used
	 */
	public void onReleased() {
	}

	/**
	 * Resets this keybinding (triggered when the user clicks on the "Reset" button).
	 */
	public void resetKeyBinding() {
		((IKeyBinding) this).amecs$getKeyModifiers().copyModifiers(defaultModifiers);
	}

	@Override
	public boolean isDefault() {
		return defaultModifiers.equals(((IKeyBinding) this).amecs$getKeyModifiers()) && super.isDefault();
	}

	public KeyModifiers getDefaultModifiers() {
		return defaultModifiers;
	}
}
