package Search;

import javafx.scene.control.CustomMenuItem;
import javafx.scene.text.Text;

public class AddressMenuItem extends CustomMenuItem{
    Address address;

    public AddressMenuItem(Address address, boolean showStreet, boolean showHouse, boolean showCity, boolean showPostcode){
        this.address = address;

        this.setContent(getTextToShow(showStreet, showHouse, showCity, showPostcode));
        setHideOnClick(true);
    }

    private Text getTextToShow(boolean showStreet, boolean showHouse, boolean showCity, boolean showPostcode) {
        StringBuilder stringBuilder = new StringBuilder();

        if(showStreet) stringBuilder.append(address.street()).append(" ");
        if(showHouse) stringBuilder.append(address.houseNumber()).append(" ");
        if(showCity) stringBuilder.append(address.city()).append(" ");
        if(showPostcode) stringBuilder.append(address.postcode()).append(" ");

        return new Text(stringBuilder.toString());

    }

    public Address getAddress() {
        return address;
    }
}