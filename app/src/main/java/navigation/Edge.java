package navigation;

import collections.enumflags.EnumFlags;

import java.io.Serializable;

record Edge(
        long from,
        long to,
        float distance,
        float maxSpeed,
        EnumFlags<EdgeRole> roles,
        RoadRole role,
        String name)
        implements Serializable {
    public boolean hasRole(EdgeRole role) {
        return roles.isSet(role);
    }

    public void setRole(EdgeRole role) {
        roles.set(role);
    }
}
