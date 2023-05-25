package com.gluonapplication;

import com.gluonapplication.Service.dbConnection;
import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.attach.util.Platform;
import com.gluonhq.attach.util.Services;
import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.control.Avatar;
import com.gluonhq.charm.glisten.control.NavigationDrawer;
import com.gluonhq.charm.glisten.control.NavigationDrawer.Item;
import com.gluonhq.charm.glisten.control.NavigationDrawer.ViewItem;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Objects;

import static com.gluonapplication.MainApplication.*;

public class DrawerManager {
    private static NavigationDrawer.Header header;


    public static void setReadWriter() {
        header.setSubtitle("You Are a ReadWriter");
    }
    public static void setAdmin() {
        header.setSubtitle("You Are an Admin");
    }
    public static void setReader() {
        header.setSubtitle("You Are a Reader");
    }
    public static void setDEFAULT() {
        header.setSubtitle("You're not connected");
    }
    public static void buildDrawer(AppManager app) {
        NavigationDrawer drawer = app.getDrawer();
        header = new NavigationDrawer.Header("DB2 Projekt",
              "",
                new Avatar(21, new Image(Objects.requireNonNull(DrawerManager.class.getResourceAsStream("/icon.png")))));
        setDEFAULT();
        drawer.setHeader(header);
        final Item KundeItem = new ViewItem("Kunde", MaterialDesignIcon.PEOPLE.graphic(), KUNDE_VIEW, ViewStackPolicy.SKIP);
        final Item BestellungItem = new ViewItem("Bestellung", MaterialDesignIcon.SHOPPING_BASKET.graphic(), BESTELLUNG_VIEW);
        final Item LoginItem = new ViewItem("Login", MaterialDesignIcon.GRID_ON.graphic(), LOGIN_VIEW);
        drawer.getItems().addAll(KundeItem, BestellungItem,LoginItem);
                if (Platform.isDesktop()) {
            final Item logOutItem = new Item("Log Out", MaterialDesignIcon.GRID_OFF.graphic());
            final Item quitItem = new Item("Quit", MaterialDesignIcon.EXIT_TO_APP.graphic());
            quitItem.selectedProperty().addListener((obs, ov, nv) -> {
                if (nv) {
                    Services.get(LifecycleService.class).ifPresent(LifecycleService::shutdown);
                }
            });
            logOutItem.selectedProperty().addListener((obs, ov, nv) -> {
                try {
                    MainApplication.getConnection().closeConnection();
                    MainApplication.getConnection().setStatus(false);
                    setDEFAULT();
                    AppManager.getInstance().switchView(AppManager.HOME_VIEW);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            drawer.getItems().add(logOutItem);
            drawer.getItems().add(quitItem);
        }
    }


}