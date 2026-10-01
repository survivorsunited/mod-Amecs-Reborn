/* Copyright 2020-2023 Siphalor. Licensed under Apache-2.0. */
package de.siphalor.amecs.gui;

import de.siphalor.amecs.Amecs;
import de.siphalor.amecs.impl.duck.IKeyBindingEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Search the existing controls entries without reconstructing or sorting game bindings. */
public class SearchFieldControlsListWidget extends ControlsListWidget.Entry {
    private final TextFieldWidget field;
    private final ControlsListWidget list;
    private final List<ControlsListWidget.Entry> original;

    public SearchFieldControlsListWidget(MinecraftClient client, ControlsListWidget list) {
        this.list = list;
        this.original = new ArrayList<>(list.children());
        field = new TextFieldWidget(client.textRenderer, 0, 0, 250, 20, Text.translatable("amecs.meta.options.searchBar"));
        field.setSuggestion(Text.translatable("amecs.search.placeholder").getString());
        field.setChangedListener(this::filter);
    }

    private void filter(String query) {
        field.setSuggestion(query.isEmpty() ? Text.translatable("amecs.search.placeholder").getString() : "");
        query = query.trim();
        int delimiter = query.indexOf('=');
        String name = (delimiter < 0 ? query : query.substring(0, delimiter)).toLowerCase(Locale.ROOT);
        String key = delimiter < 0 ? null : query.substring(delimiter + 1).trim();
        List<ControlsListWidget.Entry> visible = new ArrayList<>();
        visible.add(this);
        ControlsListWidget.Entry categoryEntry = null;
        KeyBinding.Category lastCategory = null;
        for (ControlsListWidget.Entry entry : original) {
            if (entry instanceof ControlsListWidget.CategoryEntry) {
                categoryEntry = entry;
            } else if (entry instanceof ControlsListWidget.KeyBindingEntry bindingEntry) {
                KeyBinding binding = ((IKeyBindingEntry) bindingEntry).amecs$getKeyBinding();
                boolean named = Text.translatable(binding.getId()).getString().toLowerCase(Locale.ROOT).contains(name)
                    || binding.getCategory().getLabel().getString().toLowerCase(Locale.ROOT).contains(name);
                if (named && Amecs.entryKeyMatches(bindingEntry, key)) {
                    if (!binding.getCategory().equals(lastCategory)) {
                        if (categoryEntry != null) visible.add(categoryEntry);
                        lastCategory = binding.getCategory();
                    }
                    visible.add(entry);
                }
            }
        }
        if (visible.size() == 1) visible.add(new EmptyResultsEntry());
        boolean focused = field.isFocused();
        list.replaceEntries(visible);
        if (focused) {
            list.setFocused(this);
            setFocused(field);
            field.setFocused(true);
        }
        list.setScrollY(0);
    }

    @Override public List<? extends Element> children() { return List.of(field); }
    @Override public List<? extends Selectable> selectableChildren() { return List.of(field); }
    @Override public boolean mouseClicked(Click click, boolean doubleClick) { boolean handled = field.mouseClicked(click, doubleClick); if (handled) setFocused(field); return handled; }
    @Override public boolean mouseReleased(Click click) { return field.mouseReleased(click); }
    @Override public boolean keyPressed(KeyInput input) { return field.keyPressed(input); }
    @Override public boolean charTyped(CharInput input) { return field.charTyped(input); }
    @Override public void setFocused(boolean focused) { super.setFocused(focused); field.setFocused(focused); }
    @Override public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float delta) {
        field.setPosition(getContentMiddleX() - 125, getContentY());
        field.render(context, mouseX, mouseY, delta);
    }
    @Override protected void update() {}

    private static final class EmptyResultsEntry extends ControlsListWidget.Entry {
        @Override public List<? extends Element> children() { return List.of(); }
        @Override public List<? extends Selectable> selectableChildren() { return List.of(); }
        @Override public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float delta) {
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
                Text.translatable("amecs.search.no_results"), getContentMiddleX(), getContentY(), 0xffaaaaaa);
        }
        @Override protected void update() {}
    }
}
