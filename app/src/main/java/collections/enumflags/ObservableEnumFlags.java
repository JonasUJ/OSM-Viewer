package collections.enumflags;

import collections.observable.Observer;
import collections.observable.ObserverManager;

public class ObservableEnumFlags<T extends Enum<T>> extends EnumFlags<T> {
    private final ObserverManager<EnumFlagsEvent<T>> observers = new ObserverManager<>();

    public void addObserver(Observer<EnumFlagsEvent<T>> observer) {
        observers.add(observer);
    }

    @Override
    public boolean toggle(T e) {
        var enabled = super.toggle(e);
        observers.send(new EnumFlagsEvent<>(e, enabled));
        return enabled;
    }

    @Override
    public void set(T e) {
        super.set(e);
        observers.send(new EnumFlagsEvent<>(e, isSet(e)));
    }

    @Override
    public void unset(T e) {
        super.unset(e);
        observers.send(new EnumFlagsEvent<>(e, isSet(e)));
    }
}
