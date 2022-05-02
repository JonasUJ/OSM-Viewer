package Search;

import java.io.Serializable;
import osm.elements.SlimOSMNode;

public record Address(
        String street,
        String houseNumber,
        String floor,
        String side,
        String postcode,
        String city,
        SlimOSMNode node)
        implements Serializable {
    @Override
    public String toString() {
        var floor = this.floor == null ? "" : this.floor;
        var side = this.side == null ? "" : this.side;

        return street + " " + houseNumber + " " + floor + side + ", " + postcode + " " + city;
    }
}
