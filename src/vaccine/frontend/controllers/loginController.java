package vaccine.frontend.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import vaccine.backend.classes.Account;

import java.io.IOException;
import java.util.Optional;

import vaccine.backend.classes.Schedule;
import vaccine.backend.dao.*;

public class loginController {

    @FXML
    private BorderPane main;
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField passwordTextField;

    private Parent root;
    private Stage stage;
    private Scene scene;

    public void loginValidate(ActionEvent event) throws IOException {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        Account details = new Account(username, password, "");
        details = accountDAO.validate(details);
        String usertype = details.getUsertype();

        if (!details.getUsertype().equals("")) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../gui/dashboard.fxml"));
            root = loader.load();
            stage = new Stage();
            scene = new Scene(root);

            String name = null;
            ObservableList<Schedule> patient = FXCollections.observableArrayList();
            int id = details.getUserID();
            patient = scheduleDAO.getAllPatients();
            if (usertype.equals("doctor")){
                name = doctorDAO.getDoctorNameByUserID(id);
                patient = scheduleDAO.getPatientByDoctor(id);
            }
            else if (usertype.equals("medstaff"))
                name = staffDAO.getStaffNameByID(id);
            else
                name = "Admin";

            patientTable patientTable = loader.getController();
            patientTable.displayName(id, name, usertype, patient);

            draw.screen(root, stage, scene);
            Stage login = (Stage) main.getScene().getWindow();
            login.close();
        } else {
            System.out.println("Wrong Pass");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Incorrect Username or Password.");
            alert.show();
        }
    }

    public void exit(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Are you sure you want to exit?");
        alert.setTitle("Confirm");
        Optional<ButtonType> action = alert.showAndWait();

        if (action.get() == ButtonType.OK) {
            Stage login = (Stage) main.getScene().getWindow();
            login.close();
        }
    }


}
