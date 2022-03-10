package osm.elements;

public record OSMMemberWay(OSMWay way, Role role) {
    public enum Role {
        Inner,
        Outer;

        public static Role from(String role) {
            return switch (role) {
                case "inner" -> Inner;
                case "outer" -> Outer;
                default -> null;
            };
        }
    }

    public static OSMMemberWay from(OSMWay way, String role) {
        var r = Role.from(role);
        if (r == null || way == null) return null;

        return new OSMMemberWay(way, r);
    }
}
