package com.gluonapplication.views;

import com.gluonhq.charm.glisten.mvc.View;
import java.io.IOException;
import java.util.Objects;

import javafx.fxml.FXMLLoader;

public class KundeView {

    public View getView() {
        try {
            return FXMLLoader.load(Objects.requireNonNull(KundeView.class.getResource("kunde.fxml")));
        } catch (IOException e) {
            System.out.println("IOException: " + e);
            return new View();
        }
    }
}
