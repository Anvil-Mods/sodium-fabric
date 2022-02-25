package me.jellysquid.mods.sodium.config.user.options;

import me.jellysquid.mods.sodium.gui.config.Control;
import net.minecraft.network.chat.Component;
import me.jellysquid.mods.sodium.config.user.options.storage.OptionStorage;
import java.util.Collection;

public interface Option<T> {
    Component getName();

    Component getTooltip();

    OptionImpact getImpact();

    Control<T> getControl();

    T getValue();

    void setValue(T value);

    void reset();

    OptionStorage<?> getStorage();

    boolean isAvailable();

    boolean hasChanged();

    void applyChanges();

    Collection<OptionFlag> getFlags();
}
