package Search;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextField;

public class AutofillContextMenu extends ContextMenu {
    TextField text;
    AddressDatabase addresses;

    public AutofillContextMenu(TextField text, AddressDatabase addresses) {
        this.text = text;
        this.addresses = addresses;
    }

    public void onMenuClick(ActionEvent actionEvent) {
        String inputText = text.getText();

        AddressMenuItem menuItem = (AddressMenuItem) actionEvent.getSource();
        Address menuAddress = menuItem.getAddress();
        boolean pointOfIntest = (menuAddress.street().toLowerCase().contains(("(point of interest)")));

        AddressBuilder parsed = AddressDatabase.parse(inputText);
        assert parsed != null;
        Address address = parsed.build();

        StringBuilder stringBuilder = new StringBuilder();
        boolean appendCity = false;

        if (address.street() != null) {
            stringBuilder.append(menuAddress.street()).append(" ");
            appendCity = true;
        }
        if (address.houseNumber() != null) {
            stringBuilder.append(menuAddress.houseNumber()).append(" ");
        } else if (appendCity && !pointOfIntest) {
            stringBuilder.append("House No. ");
        }
        if (address.postcode() != null) stringBuilder.append(menuAddress.postcode()).append(" ");
        if (address.city() != null || appendCity) stringBuilder.append(menuAddress.city()).append(" ");

        text.setText(stringBuilder.toString());
        if (appendCity && address.houseNumber() == null && !pointOfIntest) {
            for (int i = 0; i < menuAddress.street().split(" ").length; i++) {
                text.nextWord();
            }
            text.selectNextWord();
            text.selectNextWord();
            text.selectForward();
        } else {
            text.end();
        }
    }
}
