package collections.enumflags;

public record EnumFlagsEvent<T>(T variant, boolean enabled) {
}
