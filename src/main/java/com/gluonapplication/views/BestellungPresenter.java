package com.gluonapplication.views;

import com.gluonapplication.MainApplication;
import com.gluonapplication.Model.DatabaseDTO;
import com.gluonapplication.Service.dbConnection;
import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.control.*;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.util.List;

public class BestellungPresenter  {
    private final Connection conn = MainApplication.getConnection().GetConn();
    private final dbConnection connection = MainApplication.getConnection();
    private ResultSet resultSet;
    @FXML
    private TableView<DatabaseDTO> PostenArtikelTable;
    @FXML
    private TextField BestellNr,Bestelldatum,Lieferdatum,Rechnungsbetrag;

    @FXML
    private View secondary;

    public BestellungPresenter() throws SQLException {
    }


    public void initialize() {
        secondary.setShowTransitionFactory(BounceInRightTransition::new);
        
        FloatingActionButton fab = new FloatingActionButton(MaterialDesignIcon.INFO.text,
                e -> System.out.println("Info"));
        fab.showOn(secondary);
        
        secondary.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = AppManager.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e ->
                        AppManager.getInstance().getDrawer().open()));
                appBar.setTitleText("Bestellung");
                appBar.getActionItems().add(MaterialDesignIcon.FAVORITE.button(e ->
                        System.out.println("Favorite")));
            }
        });
        if (connection.getStatus()) {
            try {
                String query = "SELECT * FROM BESTELLUNG";
                Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                resultSet = statement.executeQuery(query);
                if (resultSet.next()) {
                    displayRowData();
                    displayPostenArtikelTable();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayPostenArtikelTable() throws SQLException {
        try {
            String bestellNrStr = BestellNr.getText();
            if (bestellNrStr.isEmpty() || !bestellNrStr.matches("\\d+")) {
                Alert alert = new Alert(javafx.scene.control.Alert.AlertType.ERROR, "Invalid BestellNr value!");
                alert.showAndWait();
                return;
            }
            String query = "SELECT B.BestellNr, B.BestellDatum, P.BestellMenge, A.ArtikelNr, A.Bezeichnung AS PostenArtikel FROM BESTELLUNG B, POSTEN P, ARTIKEL A where (B.BestellNr = P.BestellNr) AND (P.ArtikelNr = A.ArtikelNr) AND (B.BestellNr=" + bestellNrStr + ")";
            connection.prepareTable(query,PostenArtikelTable);
            List<DatabaseDTO> resultList = connection.executeQuery(query);
            ObservableList<DatabaseDTO> data = FXCollections.observableArrayList(resultList);
            PostenArtikelTable.setItems(data);
        } catch (SQLException ex) {
            ex.printStackTrace();
            // handle the exception
        }

    }


    private void displayRowData() throws SQLException {
        BestellNr.setText(resultSet.getString("BESTELLNR"));
        Bestelldatum.setText(resultSet.getString("BESTELLDATUM"));
        Lieferdatum.setText(resultSet.getString("LIEFERDATUM"));
        Rechnungsbetrag.setText(resultSet.getString("RECHNUNGSBETRAG"));
    }
    @FXML
    public void handlePrevious() {
        try {
            if (resultSet.previous()) {
                displayRowData();
                displayPostenArtikelTable();
            } else {
                resultSet.next(); // move back to current row
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNext() {
        try {
            if (resultSet.next()) {
                // display the current row data in the text fields
                displayRowData();
                displayPostenArtikelTable();
            } else {
                // move the cursor back to the last row and display the data
                resultSet.previous();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd(){
        if (connection.getUserRole() == dbConnection.UserRole.ADMIN || connection.getUserRole() == dbConnection.UserRole.READER_WRITE) {
            String BestellNrStr = BestellNr.getText().trim();
            String BestelldatumStr = Bestelldatum.getText().trim();
            String LieferdatumStr = Lieferdatum.getText().trim();
            String RechnungsbetragStr = Rechnungsbetrag.getText().trim();
            try {
                String sql = "INSERT INTO BESTELLUNG (BESTELLNR, BESTELLDATUM, LIEFERDATUM, RECHNUNGSBETRAG) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, BestellNrStr);
                stmt.setString(2, BestelldatumStr);
                stmt.setString(3, LieferdatumStr);
                stmt.setString(4, RechnungsbetragStr);
                stmt.executeUpdate();
                Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Bestellung added successfully.");
                alert.showAndWait();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            Alert alert = new Alert(javafx.scene.control.Alert.AlertType.WARNING, "You don't have permission to add, edit or delete!");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleEdit(){
        if (connection.getUserRole() == dbConnection.UserRole.ADMIN || connection.getUserRole() == dbConnection.UserRole.READER_WRITE) {
            String BestellNrStr = BestellNr.getText().trim();
            String BestelldatumStr = Bestelldatum.getText().trim();
            String LieferdatumStr = Lieferdatum.getText().trim();
            String RechnungsbetragStr = Rechnungsbetrag.getText().trim();
            String sql = "UPDATE BESTELLUNG SET BESTELLDATUM=?, LIEFERDATUM=?, RECHNUNGSBETRAG=? WHERE KUNDENNR=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // set parameter values
                stmt.setString(1, BestelldatumStr);
                stmt.setString(2, LieferdatumStr);
                stmt.setString(3, RechnungsbetragStr);
                stmt.setString(4, BestellNrStr);

                // execute update
                stmt.executeUpdate();

                // show success message and clear text fields
                Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Kunde updated successfully");
                alert.showAndWait();
            } catch (SQLException e) {
                // show error message and log exception
                System.err.println("Error updating customer: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(javafx.scene.control.Alert.AlertType.WARNING, "You don't have permission to add, edit or delete!");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDelete(){
        if (connection.getUserRole() == dbConnection.UserRole.ADMIN) {
            try {
                // Prepare the delete statement
                String sql = "DELETE FROM BESTELLUNG WHERE BESTELLNR = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, BestellNr.getText());

                // Execute the delete statement
                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0) {
                    BestellNr.setText("");
                    Bestelldatum.setText("");
                    Lieferdatum.setText("");
                    Rechnungsbetrag.setText("");
                    Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Bestellung deleted successfully.");
                    alert.showAndWait();
                }
            } catch (SQLException e) {
                Alert alert = new Alert(javafx.scene.control.Alert.AlertType.WARNING, "Error deleting Bestellung: " + e.getMessage());
                alert.showAndWait();
            }
        }
        else {
            Alert alert = new Alert(javafx.scene.control.Alert.AlertType.WARNING, "You don't have permission to add, edit or delete!");
            alert.showAndWait();
        }
    }

}

