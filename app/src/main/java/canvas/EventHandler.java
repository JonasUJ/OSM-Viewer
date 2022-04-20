package canvas;

import java.util.EventListener;

// JavaFX EventHandler without the <T extends Event> constraint
@FunctionalInterface
public interface EventHandler<T> extends EventListener {
    /**
     * Invoked when a specific event of the type for which this handler is registered happens.
     *
     * @param event the event which occurred
     */
    void handle(T event);
}
