package Search;

import java.io.Serializable;

public record Address(

        String street, String houseNumber, String postcode, String city, float lat, float lon)
        implements Serializable {
    @Override
    public String toString() {

        return street + " " + houseNumber + " " + postcode + " " + city;
    }
}
