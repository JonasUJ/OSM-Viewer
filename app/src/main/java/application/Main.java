package application;

import java.io.IOException;
import javafx.application.Application;
import javax.xml.stream.XMLStreamException;

public class Main {
    public static void main(String[] args) throws IOException, XMLStreamException {
        // var reader = new OSMReader(args[0]);
        Application.launch(App.class, args);
    }
}
