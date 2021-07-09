package vaccine.frontend.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import vaccine.backend.classes.Account;
import vaccine.backend.classes.Doctor;
import vaccine.backend.classes.Staff;
import vaccine.backend.dao.accountDAO;
import vaccine.backend.dao.doctorDAO;
import vaccine.backend.dao.staffDAO;

public class registerController {
    // Initialize UI components
    @FXML
    private Label cpasswordLabel;
    @FXML
    private Button registerButton, deleteButton, updateButton;
    @FXML
    private TextField username, password, cpassword;
    @FXML
    private TextField fname, lname;
    @FXML
    private RadioButton doctor, medstaff;
    @FXML
    private CheckBox chkMon, chkTues, chkWed, chkThurs, chkFri, chkSat;
    @FXML
    private AnchorPane main;

    Account account = null;
    Doctor doctors = null;
    Staff staff = null;

    /**
     * Creates an ObservableList that contains the combo box input options for schedule
     */
    public String getScheduleBoxes() {
        String sched="";
        if (chkMon.isSelected()) sched += "Monday,";
        if (chkTues.isSelected()) sched += "Tuesday,";
        if (chkWed.isSelected()) sched += "Wednesday,";
        if (chkThurs.isSelected()) sched += "Thursday,";
        if (chkFri.isSelected()) sched += "Friday,";
        if (chkSat.isSelected()) sched += "Saturday";
        return sched;
    }

    public void register(ActionEvent event) throws IOException {
        try {
            // Get values of input fields.
            // TODO: Input Validation for all fields. (Empty or Incorrect data type)
            String user = username.getText();
            String pass = password.getText();
            String cpass = cpassword.getText();
            String fName = fname.getText();
            String lName = lname.getText();
            String sched = getScheduleBoxes();

            // Check for the selected usertype in the radio buttons.
            String usertype = null;
            if(doctor.isSelected())
                usertype = doctor.getText().toLowerCase(Locale.ROOT);
            else if (medstaff.isSelected())
                usertype = medstaff.getText().toLowerCase(Locale.ROOT);

            // Checks if the Confirm Password field matches the Password Field.
            if (!pass.equals(cpass)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Confirm Password must match Password.");
                alert.show();
            } else {
                // Creates an Account object, which is sent as a parameter in the addAccount method
                Account account = new Account(user, pass, usertype);
                int id = accountDAO.addAccount(account);

                // Checks if the  recordis inserted
                if (id == -1) throw new IOException("Error");

                    // Use the returned ID as a foreign key
                    // for creating a new doctor_info or staff_info
                else {
                    String fullname = lName + ", " + fName;
                    if (medstaff.isSelected()) {
                        Staff staff = new Staff(id, fullname);
                        staffDAO.addStaff(staff);
                    } else {
                        Doctor doctor = new Doctor(id, fullname, sched);
                        doctorDAO.addDoctor(doctor);
                    }
                }
            }
            // Closes the current window after processing.
            Stage stage = (Stage) main.getScene().getWindow();
            stage.hide();
            // Reloads the table to see the inserted record.
            registerButton.setOnAction(event1 -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_HIDDEN)));

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void updateRegister(ActionEvent event) throws IOException{
        String user = username.getText();
        String pass = password.getText();
        String fName = fname.getText();
        String lName = lname.getText();
        String sched = getScheduleBoxes();
    }

    public void cancel (ActionEvent event) throws IOException {
        // Closes the current window and restores focus to the main window.
        Stage reg = (Stage) main.getScene().getWindow();
        reg.close();
    }

    public void med (ActionEvent event) throws IOException {
        disableSchedule(true);
    }

    public void doc (ActionEvent event) throws IOException {
        disableSchedule(false);
    }

    public void disableSchedule(boolean set) {
        chkMon.setDisable(set);
        chkTues.setDisable(set);
        chkWed.setDisable(set);
        chkThurs.setDisable(set);
        chkFri.setDisable(set);
        chkSat.setDisable(set);
    }

    public void setFieldContent(int staffID) {
        account = accountDAO.getAccountByUserID(staffID);
        username.setText(account.getUsername());
        password.setText(account.getPassword());

        if(account.getUsertype().equals("medstaff")||account.getUsertype().equals("medical staff")){
            staff = staffDAO.getStaffByUserID(staffID);
            medstaff.setSelected(true);
            disableSchedule(true);
            String[] fullname = staff.getStaffName().split(",", -2);
            lname.setText(fullname[0]);
            fname.setText(fullname[1]);
        }else{
            doctors = doctorDAO.getDoctorByUserID(staffID);
            doctor.setSelected(true);
            disableSchedule(false);
            String sched = doctors.getSchedule();
            if (sched.contains("Monday")) chkMon.setSelected(true);
            if (sched.contains("Tuesday")) chkTues.setSelected(true);
            if (sched.contains("Wednesday")) chkWed.setSelected(true);
            if (sched.contains("Thursday")) chkThurs.setSelected(true);
            if (sched.contains("Friday")) chkFri.setSelected(true);
            if (sched.contains("Staurday")) chkSat.setSelected(true);

            String[] fullname = doctors.getDoctorName().split(",", -2);
            lname.setText(fullname[0].strip());
            fname.setText(fullname[1].strip());
        }

        cpassword.setDisable(true);
        cpassword.setVisible(false);
        cpasswordLabel.setVisible(false);
        updateButton.setDisable(false);
        updateButton.setVisible(true);
        registerButton.setDisable(true);
        registerButton.setVisible(false);
        deleteButton.setDisable(false);
        deleteButton.setVisible(true);
    }
}
