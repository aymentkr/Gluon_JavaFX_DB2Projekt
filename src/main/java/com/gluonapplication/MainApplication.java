package com.gluonapplication;

import com.gluonapplication.Service.dbConnection;
import com.gluonapplication.views.BestellungView;
import com.gluonapplication.views.KundeView;
import com.gluonapplication.views.LoginView;
import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.visual.Swatch;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Objects;

import static com.gluonhq.charm.glisten.application.AppManager.HOME_VIEW;

public class MainApplication extends Application {
    public static final String LOGIN_VIEW = HOME_VIEW;
    public static final String KUNDE_VIEW = "Kunde View";
    public static final String BESTELLUNG_VIEW = "Secondary View";
    private final AppManager appManager = AppManager.initialize(this::postInit);
    private static final dbConnection dbc = new dbConnection();

    @Override
    public void init(){
        appManager.addViewFactory(LOGIN_VIEW, () -> new LoginView().getView());
        appManager.addViewFactory(KUNDE_VIEW, () -> new KundeView().getView());
        appManager.addViewFactory(BESTELLUNG_VIEW, () -> new BestellungView().getView());
        DrawerManager.buildDrawer(appManager);
    }

    @Override
    public void start(Stage primaryStage) {
        appManager.start(primaryStage);
    }


    private void postInit(Scene scene) {
        Swatch.BLUE.assignTo(scene);
        scene.getStylesheets().add(MainApplication.class.getResource("style.css").toExternalForm());
        ((Stage) scene.getWindow()).getIcons().add(new Image(Objects.requireNonNull(MainApplication.class.getResourceAsStream("/icon.png"))));
    }
    public static dbConnection getConnection() throws SQLException {
        return dbc;
    }

    public static void main(String args[]) {
        launch(args);
    }

}
