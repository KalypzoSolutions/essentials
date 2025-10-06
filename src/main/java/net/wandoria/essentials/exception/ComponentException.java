package net.wandoria.essentials.exception;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;

/**
 * A ComponentException holds an Adventure Component that can be used to display an error message to the user.
 * <p>
 * This is pretty useful in combination with the incendo command framework during argument parsing.
 */
@Getter
public class ComponentException extends RuntimeException implements ComponentLike {
    private final Component component;

    public ComponentException(Component component) {
        this.component = component;
    }

    @Override
    public @NotNull Component asComponent() {
        return component;
    }


    // factory methods

    public static ComponentException translatable(String key) {
        return new ComponentException(Component.translatable(key));
    }

    /**
     * create a ComponentException with a translatable component
     *
     * @param key       the translation key
     * @param arguments other than in components these string arguments will be converted to components
     * @return a command exception with a translatable component
     */
    public static ComponentException translatable(String key, String... arguments) {
        ComponentLike[] componentArgs = new ComponentLike[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            componentArgs[i] = Component.text(arguments[i]);
        }
        return new ComponentException(Component.translatable(key, componentArgs));
    }

    public static ComponentException translatable(String key, ComponentLike... arguments) {
        return new ComponentException(Component.translatable(key, arguments));
    }
}
