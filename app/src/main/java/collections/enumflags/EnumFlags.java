package collections.enumflags;

import java.io.Serializable;

public class EnumFlags<T extends Enum<T>> implements Serializable {
    private int flags;

    public EnumFlags() {
        this(true);
    }

    public EnumFlags(boolean allEnabled) {
        // TODO: Find way to check whether T has more than 32 variants
        if (allEnabled) flags = 0xffffffff;
    }

    public boolean toggle(T e) {
        flags ^= 1 << e.ordinal();
        return isSet(e);
    }

    public void set(T e) {
        flags |= 1 << e.ordinal();
    }

    public void unset(T e) {
        flags &= ~(1 << e.ordinal());
    }

    public boolean isSet(T e) {
        return (flags & 1 << e.ordinal()) > 0;
    }

    public int getFlags() {
        return flags;
    }
}
