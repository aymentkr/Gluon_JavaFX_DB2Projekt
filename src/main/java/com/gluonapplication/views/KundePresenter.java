package com.gluonapplication.views;

import com.gluonapplication.MainApplication;
import com.gluonapplication.Model.DatabaseDTO;
import com.gluonapplication.Service.dbConnection;
import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.control.Alert;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.TextField;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

import java.sql.*;
import java.util.List;

public class KundePresenter {
    private final Connection conn = MainApplication.getConnection().GetConn();
    private final dbConnection connection = MainApplication.getConnection();
    private ResultSet resultSet;

    @FXML
    private TableView<DatabaseDTO> BestellungenTable;

    @FXML
    private TextField KundenNr,Name,Vorname,Ort,Strasse;

    @FXML
    private View primary;

    public KundePresenter() throws SQLException {
    }


    public void initialize() {
        primary.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = AppManager.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> 
                        AppManager.getInstance().getDrawer().open()));
                appBar.setTitleText("Kunde");
                appBar.getActionItems().add(MaterialDesignIcon.SEARCH.button(e -> 
                        System.out.println("Search")));
            }
        });
        if (connection.getStatus()) {
            try {
                String query = "SELECT * FROM KUNDE";
                Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                resultSet = statement.executeQuery(query);
                if (resultSet.next()) {
                    displayRowData();
                    displayBestellungenTable();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private void displayBestellungenTable() throws SQLException {
        try {
            String KundenNrStr = KundenNr.getText();
            if (KundenNrStr.isEmpty() || !KundenNrStr.matches("\\d+")) {
                Alert alert = new Alert(javafx.scene.control.Alert.AlertType.ERROR, "Invalid KundenNrStr value!");
                alert.showAndWait();
                return;
            }
            String query = "SELECT * FROM BESTELLUNG WHERE KUNDENNR=" + KundenNrStr;
            connection.prepareTable(query,BestellungenTable);
            List<DatabaseDTO> resultList = connection.executeQuery(query);
            ObservableList<DatabaseDTO> data = FXCollections.observableArrayList(resultList);
            BestellungenTable.setItems(data);
        } catch (SQLException ex) {
            ex.printStackTrace();
            // handle the exception
        }

    }


    @FXML
    public void handlePrevious() {
        try {
            if (resultSet.previous()) {
                displayRowData();
                displayBestellungenTable();
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
                displayBestellungenTable();
            } else {
                // move the cursor back to the last row and display the data
                resultSet.previous();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayRowData() throws SQLException {
        // set the text fields with data from the current row
        KundenNr.setText(resultSet.getString("KUNDENNR"));
        Name.setText(resultSet.getString("NAME"));
        Vorname.setText(resultSet.getString("VORNAME"));
        Strasse.setText(resultSet.getString("STRASSE"));
        Ort.setText(resultSet.getString("ORT"));
    }

    @FXML
    private void handleAdd(){
        if (connection.getUserRole() == dbConnection.UserRole.ADMIN || connection.getUserRole() == dbConnection.UserRole.READER_WRITE) {
            String KundenNrStr = KundenNr.getText().trim();
            String NameStr = Name.getText().trim();
            String VornameStr = Vorname.getText().trim();
            String StrasseStr = Strasse.getText().trim();
            String OrtStr = Ort.getText().trim();
            try {
                String sql = "INSERT INTO KUNDE (KUNDENNR, NAME, VORNAME, STRASSE, ORT) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, KundenNrStr);
                stmt.setString(2, NameStr);
                stmt.setString(3, VornameStr);
                stmt.setString(4, StrasseStr);
                stmt.setString(5, OrtStr);
                stmt.executeUpdate();
                Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Kunde added successfully.");
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
            String KundenNrStr = KundenNr.getText().trim();
            String NameStr = Name.getText().trim();
            String VornameStr = Vorname.getText().trim();
            String StrasseStr = Strasse.getText().trim();
            String OrtStr = Ort.getText().trim();
            String sql = "UPDATE KUNDE SET NAME=?, VORNAME=?, STRASSE=?, ORT=? WHERE KUNDENNR=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // set parameter values
                stmt.setString(1, NameStr);
                stmt.setString(2, VornameStr);
                stmt.setString(3, StrasseStr);
                stmt.setString(4, OrtStr);
                stmt.setString(5, KundenNrStr);

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
                String sql = "DELETE FROM KUNDE WHERE KUNDENNR = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, KundenNr.getText());

                // Execute the delete statement
                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0) {
                    KundenNr.setText("");
                    Name.setText("");
                    Vorname.setText("");
                    Ort.setText("");
                    Strasse.setText("");
                    Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Kunde deleted successfully.");
                    alert.showAndWait();
                }
            } catch (SQLException e) {
                Alert alert = new Alert(javafx.scene.control.Alert.AlertType.WARNING, "Error deleting Kunde: " + e.getMessage());
                alert.showAndWait();
            }
        }
        else {
            Alert alert = new Alert(javafx.scene.control.Alert.AlertType.WARNING, "You don't have permission to add, edit or delete!");
            alert.showAndWait();
        }
    }
}
