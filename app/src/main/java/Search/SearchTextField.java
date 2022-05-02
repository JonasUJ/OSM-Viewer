package Search;

import view.Model;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.TextField;

public class SearchTextField extends TextField {
    AutofillContextMenu popupEntries;
    Address currentSearch;
    AddressDatabase addressDatabase;

    public AddressDatabase getAddressDatabase() { // TODO: FJERN
        return addressDatabase;
    }

    public void init(Model model) {
        var addressDatabase = model.getAddresses();
        this.addressDatabase = addressDatabase;
        popupEntries = new AutofillContextMenu(this, addressDatabase);
    }

    public List<Address> handleSearch() {
        popupEntries.hide();
        var parsedAddress = parseAddress();
        if (parsedAddress == null) return null;
        return addressDatabase.search(parsedAddress);
    }

    public void showHistory() {
        popupEntries.hide();
        popupEntries.getItems().clear();
        addressDatabase.getHistory().forEach(e -> popupEntries.getItems().add(new AddressMenuItem(e)));
        if (!popupEntries.isShowing()) {
            popupEntries.show(this, Side.BOTTOM, 0, 0);
        }
    }

    public void showMenuItems(ObservableList<Address> itemsToShow) {
        if (getText().length() == 0) {
            showHistory();
        } else {
            popupEntries.hide();

            boolean showStreet = (currentSearch.street() != null);
            boolean showHouse = (currentSearch.houseNumber() != null);
            boolean showCity = (currentSearch.city() != null);
            boolean showPostcode = (currentSearch.postcode() != null);
            if (currentSearch.street() != null) showCity = true;

            popupEntries.getItems().clear();

            for (Address a : itemsToShow) {
                if (a == null) continue;
                var item = new AddressMenuItem(a, showStreet, showHouse, showCity, showPostcode);
                item.setOnAction(popupEntries::onMenuClick);
                popupEntries.getItems().add(item);
            }
            if (!popupEntries.isShowing()) {
                popupEntries.show(this, Side.BOTTOM, 0, 0);
            }
        }
    }

    public void setCurrentSearch(Address currentSearch) {
        this.currentSearch = currentSearch;
    }

    private String reformat(String string) {
        if (string == null) return null;

        string = string.toLowerCase();

        var stringBuilder = new StringBuilder();
        var split = string.split(" ");
        for (String s : split) {
            if (s.length() == 0) continue;
            char[] charArray = s.toCharArray();
            charArray[0] = Character.toUpperCase(charArray[0]);
            var result = new String(charArray);
            stringBuilder.append(result).append(" ");
        }

        return stringBuilder.toString().trim();
    }

    public Address parseAddress() {
        var searchedAddressBuilder = AddressDatabase.parse(getText());

        if (searchedAddressBuilder == null) {
            return null;
        }

        searchedAddressBuilder.street(reformat(searchedAddressBuilder.getStreet()));
        searchedAddressBuilder.city(reformat(searchedAddressBuilder.getCity()));

        return searchedAddressBuilder.build();
    }
}
