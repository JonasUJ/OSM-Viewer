package Search;

import javafx.geometry.Side;
import javafx.scene.control.TextField;
import java.util.List;

public class SearchTextField extends TextField {
    AutofillContextMenu popupEntries;
    AddressDatabase addressDatabase;


    public void init(AddressDatabase addressDatabase){
        this.addressDatabase = addressDatabase;
        popupEntries = new AutofillContextMenu(this, addressDatabase);
    }

    public Address handleSearch() {
        popupEntries.hide();
        var parsedAddress = parseAddress();
        if(parsedAddress == null){
            // throw new Exception("No match");
            //TODO display error "message" to user
            return null;
        }
        var result = addressDatabase.search(parsedAddress);
        if(result == null){
            //TODO display error "message" to user
            return null;
        }
        return result;
    }

    public void showHistory(){
        popupEntries.hide();
        popupEntries.getItems().clear();
        addressDatabase.getHistory().forEach(e -> popupEntries.getItems().add(new AddressMenuItem(e)));
        if (!popupEntries.isShowing()) {
            popupEntries.show(this, Side.BOTTOM, 0, 0);
        }
    }

    public void handleSearchChange(){
        if(getText().length() == 0){
            showHistory();
        } else{
            popupEntries.hide();
            List<Address> results;

            var searchedAddress = parseAddress();
            if(searchedAddress == null) return;

            results = addressDatabase.possibleAddresses(searchedAddress,5);

            boolean showStreet = (searchedAddress.street() != null);
            boolean showHouse = (searchedAddress.houseNumber() != null);
            boolean showCity = (searchedAddress.city() != null);
            boolean showPostcode = (searchedAddress.postcode() != null);
            if(searchedAddress.street() != null) showCity = true;

            popupEntries.getItems().clear();

            for(Address a : results){
                if(a == null) continue;
                var item = new AddressMenuItem(a, showStreet, showHouse, showCity, showPostcode);
                item.setOnAction(popupEntries::onMenuClick);
                popupEntries.getItems().add(item);
            }
            if (!popupEntries.isShowing()) {
                popupEntries.show(this, Side.BOTTOM, 0, 0);
            }
        }

    }
    private String reformat(String string){
        if(string == null) return null;

        string = string.toLowerCase();

        var stringBuilder = new StringBuilder();
        var split = string.split(" ");
        for(String s : split){
            if(s.length() == 0) continue;
            char[] charArray = s.toCharArray();
            charArray[0] = Character.toUpperCase(charArray[0]);
            var result = new String(charArray);
            stringBuilder.append(result).append(" ");
        }

        return stringBuilder.toString().trim();
    }

    private Address parseAddress(){
        var searchedAddressBuilder = AddressDatabase.parse(getText());

        if(searchedAddressBuilder == null){
            return null;
        };
        searchedAddressBuilder.street(reformat(searchedAddressBuilder.getStreet()));
        searchedAddressBuilder.city(reformat(searchedAddressBuilder.getCity()));

        return searchedAddressBuilder.build();
    }

}