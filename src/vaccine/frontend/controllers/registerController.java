package vaccine.frontend.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import vaccine.backend.classes.Account;
import vaccine.backend.classes.Doctor;
import vaccine.backend.classes.Staff;
import vaccine.backend.dao.accountDAO;
import vaccine.backend.dao.doctorDAO;
import vaccine.backend.dao.scheduleDAO;
import vaccine.backend.dao.staffDAO;
import vaccine.backend.util.AES256;

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

    String decryptedPass;

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

    public boolean errorChecker(){
        int days = 0;
        if (chkMon.isSelected()) days++;
        if (chkTues.isSelected()) days++;
        if (chkWed.isSelected()) days++;
        if (chkThurs.isSelected()) days++;
        if (chkFri.isSelected()) days++;
        if (chkSat.isSelected()) days++;

        if(doctor.isSelected()){
            if(fname.getText()==""||lname.getText()==""||username.getText()==""||(username.getText().length()<=5&&username.getText().length()>=10)||
                    password.getText().length()<5||fname.getText().matches(".*[^a-z A-Z].*")||lname.getText().matches(".*[^a-z A-Z].*")||days<=2){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please Fill in all the details and correct format.\n Select at least three (3) days for the schedule");
                alert.show();
            }
            else{
                return true;
            }
        }
        else if(medstaff.isSelected()){
            if(fname.getText()==""||lname.getText()==""||username.getText()==""||(username.getText().length()<=5&&username.getText().length()>=10)||
                    password.getText().length()<5||fname.getText().matches(".*[^a-z A-Z].*")||lname.getText().matches(".*[^a-z A-Z].*")){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please Fill in all the details and correct format");
                alert.show();
            }
            else{
                return true;
            }
        }
        else return true;
        return false;
    }


    public void register(ActionEvent event) throws IOException {
        try {
            if(errorChecker()){
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

                // Generates a random salt
                String salt = AES256.generateSalt();

                // Checks if the Confirm Password field matches the Password Field.
                if (!pass.equals(cpass)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Confirm Password must match Password.");
                    alert.show();
                } else {
                    if (accountDAO.getUserIDByUsername(user) == 0) {
                        pass = AES256.securePassword(pass, salt, true);
                        // Creates an Account object, which is sent as a parameter in the addAccount method
                        Account account = new Account(user, pass, usertype, salt);
                        int id = accountDAO.addAccount(account);
                        accountDAO.setLoginStatus(id, "out");

                        // Checks if the record is inserted
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
                        // Closes the current window after processing.
                        Stage stage = (Stage) main.getScene().getWindow();
                        stage.hide();
                        // Reloads the table to see the inserted record.
                        registerButton.setOnAction(event1 -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_HIDDEN)));
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Username already exists.");
                        alert.show();
                    }
                }

            }

        } catch (IOException | NoSuchAlgorithmException exception) {
            exception.printStackTrace();
        }
    }

    public void updateAccount(ActionEvent event) throws NoSuchAlgorithmException {
        // Get values of input fields.
        // TODO: Input Validation for all fields. (Empty or Incorrect data type)

        if(errorChecker()){
            String user = username.getText();
            String pass = password.getText();
            String fName = fname.getText();
            String lName = lname.getText();
            String sched = getScheduleBoxes();

            // Check for the selected usertype in the radio buttons.
            String usertype = null;
            if(doctor.isSelected())
                usertype = doctor.getText().toLowerCase(Locale.ROOT);
            else if (medstaff.isSelected())
                usertype = medstaff.getText().toLowerCase(Locale.ROOT);
            else
                usertype = "admin";
            // Creates an Account object, which is sent as a parameter in the updateAccount method
            String salt;
            if (!(pass.equals(decryptedPass)) || account.getSalt() == null || usertype.equals("admin")){
                salt = AES256.generateSalt();
            }else{
                salt = account.getSalt();
            }

            pass = AES256.securePassword(pass,salt,true);
            Account a = new Account(account.getUserID(), user, pass, usertype, salt);
            int id = accountDAO.updateAccount(a);

            // Use the returned ID as a foreign key
            // for updating the corresponding doctor_info or staff_info row
            String fullname = lName + ", " + fName;
            if (medstaff.isSelected()) {
                staff = new Staff(id, staff.getStaffID(), fullname);
                staffDAO.updateStaff(staff);
            } else if(doctor.isSelected()) {
                doctors = new Doctor(id, doctors.getDoctorNum(), fullname, sched);
                doctorDAO.updateDoctor(doctors);
            }


            // Closes the current window after processing.
            Stage stage = (Stage) main.getScene().getWindow();
            stage.hide();
            // Reloads the table to see the inserted record.
            updateButton.setOnAction(event1 -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_HIDDEN)));
        }
    }

    public void deleteAccount(ActionEvent event) throws Exception {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Are you sure you want to delete this record?");
            alert.setTitle("Confirm");
            Optional<ButtonType> action = alert.showAndWait();

            if (action.get() == ButtonType.OK) {
                if (account.getUsertype().equals("vaccinator")){
                    if (scheduleDAO.getPatientByDoctor(account.getUserID()).isEmpty()) {
                        accountDAO.deleteAccount(account);
                        doctorDAO.deleteDoctor(doctors);
                    } else {
                        alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Error deleting this account. The selected vaccinator has pending patients.");
                        alert.setTitle("Error");
                        alert.show();
                    }
                } else if (account.getUsertype().equals("medical staff")){
                    accountDAO.deleteAccount(account);
                    staffDAO.deleteStaff(staff);
                } else {
                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Error deleting this account. You cannot delete the Administrator Account.");
                    alert.setTitle("Error");
                    alert.show();
                }


                Stage stage = (Stage) main.getScene().getWindow();
                stage.hide();

                deleteButton.setOnAction(event1 -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_HIDDEN)));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
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

        String rawPass = account.getPassword();
        decryptedPass = AES256.securePassword(rawPass, account.getSalt(), false);
        password.setText(decryptedPass);


        if(account.getUsertype().equals("medical staff")){
            staff = staffDAO.getStaffByUserID(staffID);
            medstaff.setSelected(true);
            disableSchedule(true);
            String[] fullname = staff.getStaffName().split(",", -2);
            lname.setText(fullname[0].strip());
            fname.setText(fullname[1].strip());
        }else if(account.getUsertype().equals("vaccinator")){
            doctors = doctorDAO.getDoctorByUserID(staffID);
            doctor.setSelected(true);
            disableSchedule(false);
            String sched = doctors.getSchedule();
            if (sched.contains("Monday")) chkMon.setSelected(true);
            if (sched.contains("Tuesday")) chkTues.setSelected(true);
            if (sched.contains("Wednesday")) chkWed.setSelected(true);
            if (sched.contains("Thursday")) chkThurs.setSelected(true);
            if (sched.contains("Friday")) chkFri.setSelected(true);
            if (sched.contains("Saturday")) chkSat.setSelected(true);

            String[] fullname = doctors.getDoctorName().split(",", -2);
            lname.setText(fullname[0].strip());
            fname.setText(fullname[1].strip());
        } else {
            disableSchedule(true);
            fname.setDisable(true);
            lname.setDisable(true);
            doctor.setDisable(true);
            medstaff.setDisable(true);
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
