package navigation;

public enum EdgeRole {
    CAR,
    BIKE,
    WALK;

    @Override
    public String toString() {
        var name = super.toString();
        var substring = name.substring(1).toLowerCase();
        return name.charAt(0) + substring;
    }
}
