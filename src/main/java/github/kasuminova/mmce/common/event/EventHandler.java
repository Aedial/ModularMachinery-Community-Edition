package github.kasuminova.mmce.common.event;

/**
 * Script callback for Modular Machinery events.
 *
 * <p>
 * Script integrations adapt their own function types to this interface on
 * their side, so machine and recipe state never depends on a particular
 * scripting mod.
 */
@FunctionalInterface
public interface EventHandler<E> {

    void handle(E event);
}
