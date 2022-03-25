package Search;

import javafx.geometry.Side;
import javafx.scene.control.TextField;

import java.util.List;

public class AutofillTextField extends TextField {
    AutofillContextMenu popupEntries;
    AddressDatabase addressDatabase;

    public AutofillTextField(){
    }

    public void init(AddressDatabase addressDatabase){
        this.addressDatabase = addressDatabase;
        popupEntries = new AutofillContextMenu(this, addressDatabase);
    }

    public void handleSearchChange(String text){
        int inputLength = getText().length();
        if(inputLength == 0){
            popupEntries.hide();
            showHistory(addressDatabase.getHistory());
        } else{
            List<Address> results;


            var searchedAddressBuilder = AddressDatabase.parse(getText());

            if(searchedAddressBuilder == null){
                return;
            };


            var searchedAddress = searchedAddressBuilder.build();

            boolean showStreet = (searchedAddress.street() != null);
            boolean showHouse = (searchedAddress.houseNumber() != null);
            boolean showCity = (searchedAddress.city() != null);
            boolean showPostcode = (searchedAddress.postcode() != null);
            if(searchedAddress.street() != null) showCity = true;


            results = addressDatabase.autofillSearch(searchedAddressBuilder,5);
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

    public void showHistory(List<Address> history){
        //TODO
    }

}