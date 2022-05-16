package collections;

import java.io.Serializable;

public abstract class Entity implements Comparable<Entity>, Serializable {
    public static Entity withIdStatic(long id) {
        IdEntity.instance.id = id;
        return IdEntity.instance;
    }

    public static Entity withId(long id) {
        return new IdEntity(id);
    }

    public abstract long id();

    @Override
    public int compareTo(Entity other) {
        return Long.compare(id(), other.id());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Entity e && e.id() == id();
    }

    private static class IdEntity extends Entity {
        private static final IdEntity instance = new IdEntity(0);

        private long id;

        private IdEntity(long id) {
            this.id = id;
        }

        @Override
        public long id() {
            return id;
        }

        @Override
        public String toString() {
            return "Entity(" + id() + ")";
        }
    }
}
