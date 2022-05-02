package Search;

import java.io.Serializable;
import osm.elements.SlimOSMNode;

public class AddressBuilder implements Serializable {
    private String street, house, floor, side, postcode, city;
    private SlimOSMNode node;

    public String getStreet() {
        return street;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getCity() {
        return city;
    }

    public String getHouse() {
        return house;
    }

    public String getSide() {
        return side;
    }

    public String getFloor() {
        return floor;
    }

    public void street(String street) {
        this.street = street;
    }

    public void house(String house) {
        this.house = house;
    }

    public void floor(String floor) {
        this.floor = floor;
    }

    public void side(String side) {
        this.side = side;
    }

    public void postcode(String postcode) {
        this.postcode = postcode;
    }

    public void city(String city) {
        this.city = city;
    }

    public void SlimOSMNode(SlimOSMNode node) {
        this.node = node;
    }

    public Address build() {
        return new Address(street, house, floor, side, postcode, city, node);
    }
}
