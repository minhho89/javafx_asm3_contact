package com.example.asm3.controller;

import com.example.asm3.Main;
import com.example.asm3.dao.ContactDAO;
import com.example.asm3.entity.Contact;
import com.example.asm3.entity.Group;
import com.example.asm3.util.Util;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import static com.example.asm3.util.Util.CONFIRM;
import static com.example.asm3.util.Util.displayAlert;

public class ContactController {
    public static ObservableList<Group> groups;
    public static ObservableList<Contact> searchContactList;
    private static ObservableList<Group> searchGroupsDisplayList;
    private static ObservableList<Contact> contacts;

    // Load data to groups and contacts from DAOs
    static {
        try {
            groups = GroupController.groups;
            contacts = ContactDAO.loadContacts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private BorderPane mainPanel;
    @FXML
    private TableView<Contact> contactsTable;
    @FXML
    private ComboBox<Group> cbGroup;
    @FXML
    private TextField searchField;

    public static ObservableList<Contact> getContacts() {
        return contacts;
    }

    @FXML
    void initialize() throws IOException {
        contactsTable.setItems(contacts);

        populateSearchGroupComboBox();
        cbGroup.setItems(searchGroupsDisplayList);
        if (searchGroupsDisplayList.size() > 0) {
            cbGroup.getSelectionModel().selectFirst();
        }
    }

    /**
     * Populate group items to the search ComboBox
     * Add "All" item above
     *
     * @throws IOException
     */
    public void populateSearchGroupComboBox() throws IOException {
        searchGroupsDisplayList = FXCollections.observableArrayList();
        searchGroupsDisplayList.addAll(groups);
        // Add "All" to search List
        searchGroupsDisplayList.add(0, new Group("All"));
    }

    /**
     * Show Add new contact dialog
     *
     * @throws IOException
     */
    @FXML
    public void showAddNewContactDialog() throws IOException {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainPanel.getScene().getWindow());
        dialog.setTitle("Add new contact");
        dialog.setHeaderText("Add a new contact");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(Main.class.getResource("addUpdateContact.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(saveButtonType);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        final Button btSave = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        AddUpdateContactController addController = fxmlLoader.getController();
        // Handle input validation
        btSave.addEventFilter(ActionEvent.ACTION, event -> {
            if (addController.areAllFieldsBlank()) {
                // If all fields are left blank
                event.consume();
                addController.blankInvalidHandle();
            } else {
                // Blank valid handle
                addController.blankValidHandle();
                addController.blankFieldsResolveHandle(); //Remove blank field message
                contactsTable.setItems(contacts);
            }
            // Check if PhoneField is valid
            if (!addController.checkPhoneFieldValidation(addController.getPhoneField())) {
                event.consume();
                addController.fieldInvalidHandle(addController.getPhoneField());
                addController.phoneFieldInvalidationHandle();
            } else {
                // valid handle
                addController.phoneFieldValidationHandle();
                addController.fieldValidHandle(addController.getPhoneField());
            }
            // Check if Email field is valid
            if (!addController.checkEmailFieldValidation(addController.getEmailField())) {
                event.consume();
                addController.fieldInvalidHandle(addController.getEmailField());
                addController.emailFieldInvalidationHandle();
            } else {
                // valid handle
                addController.emailFieldValidationHandle();
                addController.fieldValidHandle(addController.getEmailField());
            }
            addController.getDialogPane().getScene().getWindow().sizeToScene(); // resize the dialog when children added
        });
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            // Check if any fields empty
            if (!addController.areAllFieldsBlank()) {
                // If not empty then add new contact then save to file
                Contact newContact = addController.getInputContact();
                contacts.add(newContact);
                // Save to file
                ContactDAO.saveContactsToFile();
            }
        }
    }

    //update a contact
    @FXML
    public void showUpdateContactDialog() throws IOException {
        // get selected contact
        Contact selectedContact = contactsTable.getSelectionModel().getSelectedItem();

        // If no contact selected then inform user
        if (selectedContact == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No contact selected!");
            alert.setHeaderText(null);
            alert.setContentText("Please select the contact you want to update");
            alert.showAndWait();
            return;
        }

        // If contact selected, open dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainPanel.getScene().getWindow());
        dialog.setTitle("Update a contact");
        dialog.setHeaderText("Update a contact");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(Main.class.getResource("addUpdateContact.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(saveButtonType);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        final Button btSave = (Button) dialog.getDialogPane().lookupButton(saveButtonType);

        AddUpdateContactController updateController = fxmlLoader.getController();
        updateController.updateContact(selectedContact);

        // Handle input validation
        btSave.addEventFilter(ActionEvent.ACTION, event -> {
            // Check if all fields are blank
            if (updateController.areAllFieldsBlank()) {
                event.consume();
                updateController.blankInvalidHandle();
            } else {
                // valid handle
                for (Control control : updateController.getControls()) {
                    if (control instanceof TextField) {
                        if (!((TextField) control).getText().isBlank()) {
                            updateController.fieldValidHandle(control);
                        }
                    }
                    if (control instanceof DatePicker || control instanceof ComboBox) {
                        if (((DatePicker) control).getValue() != null) {
                            updateController.fieldValidHandle(control);
                        }
                    }
                }
                updateController.blankFieldsResolveHandle();
                contactsTable.setItems(contacts);
            }

            // Check if PhoneField is valid
            if (!updateController.checkPhoneFieldValidation(updateController.getPhoneField())) {
                event.consume();
                updateController.fieldInvalidHandle(updateController.getPhoneField());
                updateController.phoneFieldInvalidationHandle();
            } else {
                // valid handle
                updateController.phoneFieldValidationHandle();
                updateController.fieldValidHandle(updateController.getPhoneField());
            }

            // Check if Email field is valid
            if (!updateController.checkEmailFieldValidation(updateController.getEmailField())) {
                event.consume();
                updateController.fieldInvalidHandle(updateController.getEmailField());
                updateController.emailFieldInvalidationHandle();
            } else {
                // valid handle
                updateController.emailFieldValidationHandle();
                updateController.fieldValidHandle(updateController.getEmailField());
            }

            updateController.getDialogPane().getScene().getWindow().sizeToScene(); // resize the dialog when children added
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            //Check if any fields empty
            if (!updateController.areAllFieldsBlank()) {
                // Get selected contact
                int index = contacts.indexOf(selectedContact);
                Contact updatedContact = updateController.getInputContact();
                contacts.set(index, updatedContact);
                // Save to file
                ContactDAO.saveContactsToFile();
            }
        }
    }


    //delete a selected contact
    @FXML
    public void deleteContact() throws IOException {
        Contact selectedContact = contactsTable.getSelectionModel().getSelectedItem();
        if (selectedContact == null) {
            Alert a = displayAlert(Alert.AlertType.INFORMATION, Util.CONFIRM,
                    "No contact selected", "Please select the contact you want to delete");
            a.showAndWait();
            return;
        }
        Alert alert = displayAlert(Alert.AlertType.CONFIRMATION, CONFIRM,
                CONFIRM, "Do you want to delete selected contact?");
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete contact
            contacts.remove(selectedContact);
            ContactDAO.saveContactsToFile();
            return;
        } else if (result.isPresent() && result.get() == ButtonType.CANCEL) {
            return;
        }
    }

    /**
     * Open Group Management stage to add, update, delete or search for groups
     *
     * @throws IOException
     */
    @FXML
    public void openGroupManagement() throws IOException {
        Parent root;
        try {
            root = FXMLLoader.load(Main.class.getResource("group.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Groups Manager");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // Reload table
        contactsTable.refresh();
    }

    /**
     * Handles search action.
     * When user filter a group or/and enter information to search.
     * The result will be stored as a list of groups called searchGroup.
     * The table will show the searchGroup if there is any result.
     */
    @FXML
    public void searchAction() {
        Group searchGroup;

        // Initialize
        searchContactList = FXCollections.observableArrayList();
        searchContactList.removeAll();

        searchGroup = cbGroup.getSelectionModel().getSelectedItem();

        if (searchGroup.getName().equalsIgnoreCase("All")) {
            // If group is selected as "All"
            if (searchField.getText().isBlank()) {
                // If contact info field is blank
                searchContactList = contacts;
            } else {
                // If contact info field is not blank
                for (Contact contact : contacts) {
                    if (contact.toString().toLowerCase().contains(searchField.getText().toLowerCase(Locale.ROOT))) {
                        // If contact found
                        searchContactList.add(contact);
                    }
                }
            }
        } else {
            // If selected group is not "All"
            if (searchField.getText().isBlank()) {
                // If contact info field is blank
                for (Contact contact : contacts) {
                    if (contact.getGroup().equals(searchGroup)) {
                        searchContactList.add(contact);
                    }
                }
            } else {
                // If contact info field is not blank
                for (Contact contact : contacts) {
                    if (contact.getGroup().equals(searchGroup) && contact.toString().contains(searchField.getText())) {
                        // If contact found
                        searchContactList.add(contact);
                    }
                }
            }
        }
        if (searchContactList.size() > 0) {
            // If found contacts: Table loads item from search result list
            contactsTable.setItems(searchContactList);
        } else {
            // If no contact found: show alert
            Alert alert = displayAlert(Alert.AlertType.INFORMATION, "No contact found",
                    null, "No such contact found. Please try again with different information.");
            alert.showAndWait();
        }
    }
}
