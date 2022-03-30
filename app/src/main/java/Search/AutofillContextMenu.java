package Search;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

public class AutofillContextMenu extends ContextMenu {
    TextField text;
    AddressDatabase addresses;

    public AutofillContextMenu(TextField text, AddressDatabase addresses){
        this.text = text;
        this.addresses = addresses;
    }

    public void onMenuClick(ActionEvent actionEvent){
        String inputText = text.getText();

        AddressMenuItem menuItem = (AddressMenuItem) actionEvent.getSource();
        Address menuAddress = menuItem.getAddress();

        AddressBuilder addressBuilder = AddressDatabase.parse(inputText); //TODO: or keep the typed address as a field in the MenuItem
        assert addressBuilder != null;

        StringBuilder stringBuilder = new StringBuilder();
        boolean appendCity = false;

        if(addressBuilder.getStreet() != null) {
            stringBuilder.append(menuAddress.street()).append(" ");
            appendCity = true;
        }
        if(addressBuilder.getHouse() != null) {
            stringBuilder.append(menuAddress.houseNumber()).append(" ");
        }else if(appendCity){
            stringBuilder.append("House No. ");
        }
        if(addressBuilder.getFloor() != null)
            stringBuilder.append(addressBuilder.getFloor()).append(" ");
        if(addressBuilder.getSide() != null)
            stringBuilder.append(addressBuilder.getSide()).append(" ");
        if(addressBuilder.getCity() != null || appendCity)
            stringBuilder.append(menuAddress.city()).append(" ");
        if(addressBuilder.getPostcode() != null)
            stringBuilder.append(menuAddress.postcode()).append(" ");

        text.setText(stringBuilder.toString());
        if(appendCity && addressBuilder.getHouse() == null){
            for(int i = 0; i < menuAddress.street().split(" ").length; i++){
                text.nextWord();
            }
            text.selectNextWord();
            text.selectNextWord();
            text.selectForward();
        }else{
            text.end();
        }

        ((AutofillTextField)text).setCurrentAddress(menuAddress);
    }
}