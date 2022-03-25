package Search;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextField;

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
        String replace = addressBuilder.getStringToReplace(menuAddress);
        text.setText(""); //TODO ugly way to move cursor to the end by appending... how to move the cursor?????
        text.appendText(replace);
    }
}