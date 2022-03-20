package collections;

public abstract class Entity implements Comparable<Entity> {
    private static class IdEntity extends Entity {
        private final long id;

        private IdEntity(long id) {
            this.id = id;
        }

        @Override
        public long id() {
            return id;
        }
    }

    public abstract long id();

    public static Entity withId(long id) {
        return new IdEntity(id);
    }

    @Override
    public int compareTo(Entity other) {
        return Long.compare(id(), other.id());
    }
}
