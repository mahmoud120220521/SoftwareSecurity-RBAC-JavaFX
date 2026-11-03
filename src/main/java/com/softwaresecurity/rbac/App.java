
package com.softwaresecurity.rbac;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class App extends Application {

    private final AccessControl accessControl = new AccessControl();
    private final List<User> users = new ArrayList<>();

    private final ComboBox<String> userList = new ComboBox<>();
    private final ComboBox<String> checkUser = new ComboBox<>();

    private final TextArea userInfo = new TextArea();
    private final TextArea permissionInfo = new TextArea();

    private final Label resultLabel = new Label("Ready to check access");

    private final String[] roles = {
        "Admin", "Manager", "Customer"
    };

    private final String[] actions = {
        "Create", "View", "Update", "Delete"
    };

    @Override
    public void start(Stage stage) {

        // Main title
        Label title = new Label("RBAC - Account Access Control");
        title.setStyle(
                "-fx-font-size: 24px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #1e3a5f;"
        );

        Label subtitle = new Label(
                "Manage users, roles, and account permissions"
        );
        subtitle.setStyle("-fx-text-fill: #64748b;");

        VBox header = new VBox(5, title, subtitle);

        // Section 1: User Management
        Label userTitle = sectionTitle("1. User Management");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setPrefWidth(180);

        ComboBox<String> newUserRole = new ComboBox<>();
        newUserRole.getItems().addAll(roles);
        newUserRole.setValue("Customer");

        Button addUserButton = new Button("Add User");

        HBox addUserRow = new HBox(
                10, usernameField, newUserRole, addUserButton
        );
        addUserRow.setAlignment(Pos.CENTER_LEFT);

        userList.setPromptText("Select user");
        userList.setPrefWidth(180);

        ComboBox<String> assignRole = new ComboBox<>();
        assignRole.getItems().addAll(roles);
        assignRole.setValue("Customer");

        Button assignButton = new Button("Assign Role");

        HBox assignRow = new HBox(
                10, userList, assignRole, assignButton
        );
        assignRow.setAlignment(Pos.CENTER_LEFT);

        userInfo.setEditable(false);
        userInfo.setPrefRowCount(4);
        userInfo.setWrapText(true);

        VBox userSection = new VBox(
                10, userTitle, addUserRow, assignRow, userInfo
        );

        // Section 2: Role Permissions
        Label permissionTitle = sectionTitle("2. Role Permissions");

        ComboBox<String> permissionRole = new ComboBox<>();
        permissionRole.getItems().addAll(roles);
        permissionRole.setValue("Admin");

        ComboBox<String> permissionAction = new ComboBox<>();
        permissionAction.getItems().addAll(actions);
        permissionAction.setValue("View");

        Button addPermissionButton = new Button("Add Permission");
        Button removePermissionButton = new Button("Remove Permission");
        Button resetButton = new Button("Reset Permissions");

        HBox permissionRow = new HBox(
                10,
                permissionRole,
                permissionAction,
                addPermissionButton,
                removePermissionButton,
                resetButton
        );
        permissionRow.setAlignment(Pos.CENTER_LEFT);

        permissionInfo.setEditable(false);
        permissionInfo.setPrefRowCount(4);
        permissionInfo.setWrapText(true);

        VBox permissionSection = new VBox(
                10, permissionTitle, permissionRow, permissionInfo
        );

        // Section 3: Check Account Access
        Label checkTitle = sectionTitle("3. Check Account Access");

        checkUser.setPromptText("Select user");
        checkUser.setPrefWidth(180);

        ComboBox<String> actionList = new ComboBox<>();
        actionList.getItems().addAll(actions);
        actionList.setValue("View");

        Button checkButton = new Button("Check Access");

        HBox checkRow = new HBox(
                10, checkUser, actionList, checkButton
        );
        checkRow.setAlignment(Pos.CENTER_LEFT);

        resultLabel.setStyle(
                "-fx-font-size: 15px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #475569;"
        );

        VBox checkSection = new VBox(
                10, checkTitle, checkRow, resultLabel
        );

        // Add a new user
        addUserButton.setOnAction(e -> {

            String name = usernameField.getText().trim();
            String role = newUserRole.getValue();

            if (name.isEmpty()) {
                showMessage("Please enter a username.");
                return;
            }

            for (User user : users) {
                if (user.getUsername().equalsIgnoreCase(name)) {
                    showMessage("This username already exists.");
                    return;
                }
            }

            users.add(new User(name, role));

            usernameField.clear();
            refreshUsers();

            userList.setValue(name);
            checkUser.setValue(name);
            assignRole.setValue(role);

            showMessage("User added successfully.");
        });

        // Assign a role to a user
        assignButton.setOnAction(e -> {

            String name = userList.getValue();
            String role = assignRole.getValue();

            if (name == null) {
                showMessage("Please select a user.");
                return;
            }

            for (User user : users) {
                if (user.getUsername().equals(name)) {
                    user.setRole(role);
                    break;
                }
            }

            refreshUsers();
            userList.setValue(name);
            checkUser.setValue(name);

            resetResult();
            showMessage("Role assigned successfully.");
        });

        // Show selected user's role
        userList.setOnAction(e -> {

            String name = userList.getValue();

            if (name != null) {
                for (User user : users) {
                    if (user.getUsername().equals(name)) {
                        assignRole.setValue(user.getRole());
                        break;
                    }
                }
            }
        });

        // Add permission to a role
        addPermissionButton.setOnAction(e -> {

            String role = permissionRole.getValue();
            String action = permissionAction.getValue();

            accessControl.addPermission(role, action);

            refreshPermissions();
            resetResult();

            showMessage("Permission added successfully.");
        });

        // Remove permission from a role
        removePermissionButton.setOnAction(e -> {

            String role = permissionRole.getValue();
            String action = permissionAction.getValue();

            accessControl.removePermission(role, action);

            refreshPermissions();
            resetResult();

            showMessage("Permission removed successfully.");
        });

        // Restore the original permissions
        resetButton.setOnAction(e -> {

            for (String role : roles) {
                for (String action : actions) {
                    accessControl.removePermission(role, action);
                }
            }

            accessControl.addPermission("Admin", "Create");
            accessControl.addPermission("Admin", "View");
            accessControl.addPermission("Admin", "Update");
            accessControl.addPermission("Admin", "Delete");

            accessControl.addPermission("Manager", "View");
            accessControl.addPermission("Manager", "Update");

            accessControl.addPermission("Customer", "View");

            refreshPermissions();
            resetResult();

            showMessage("Default permissions restored.");
        });

        // Check access for the selected user
        checkButton.setOnAction(e -> {

            String name = checkUser.getValue();
            String action = actionList.getValue();

            if (name == null) {
                showMessage("Please select a user.");
                return;
            }

            for (User user : users) {

                if (user.getUsername().equals(name)) {

                    String role = user.getRole();

                    boolean allowed =
                            accessControl.canAccess(role, action);

                    if (allowed) {

                        resultLabel.setText(
                                "ACCESS GRANTED: " + name
                                + " (" + role + ") can "
                                + action.toLowerCase()
                                + " accounts."
                        );

                        resultLabel.setStyle(
                                "-fx-text-fill: #15803d;"
                                + "-fx-font-size: 15px;"
                                + "-fx-font-weight: bold;"
                        );

                    } else {

                        resultLabel.setText(
                                "ACCESS DENIED: " + name
                                + " (" + role + ") cannot "
                                + action.toLowerCase()
                                + " accounts."
                        );

                        resultLabel.setStyle(
                                "-fx-text-fill: #dc2626;"
                                + "-fx-font-size: 15px;"
                                + "-fx-font-weight: bold;"
                        );
                    }

                    return;
                }
            }

            showMessage("User not found.");
        });

        // Main window layout
        VBox content = new VBox(
                18,
                header,
                userSection,
                new Separator(),
                permissionSection,
                new Separator(),
                checkSection
        );

        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: #f8fafc;");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 950, 750);

        stage.setTitle("RBAC Access Control System");
        stage.setScene(scene);
        stage.setMinWidth(780);
        stage.setMinHeight(600);
        stage.show();

        refreshUsers();
        refreshPermissions();
    }

    // Create a section heading
    private Label sectionTitle(String text) {

        Label label = new Label(text);

        label.setStyle(
                "-fx-font-size: 17px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #1e40af;"
        );

        return label;
    }

    // Update the displayed users
    private void refreshUsers() {

        String selectedUser = userList.getValue();
        String selectedCheckUser = checkUser.getValue();

        userList.getItems().clear();
        checkUser.getItems().clear();

        StringBuilder text = new StringBuilder();

        for (User user : users) {

            userList.getItems().add(user.getUsername());
            checkUser.getItems().add(user.getUsername());

            text.append(user.getUsername())
                    .append(" - ")
                    .append(user.getRole())
                    .append("\n");
        }

        if (selectedUser != null
                && userList.getItems().contains(selectedUser)) {
            userList.setValue(selectedUser);
        }

        if (selectedCheckUser != null
                && checkUser.getItems().contains(selectedCheckUser)) {
            checkUser.setValue(selectedCheckUser);
        }

        userInfo.setText(text.toString());
    }

    // Display the permissions of each role
    private void refreshPermissions() {

        StringBuilder text = new StringBuilder();

        for (String role : roles) {

            text.append(role).append(": ");

            boolean hasPermission = false;

            for (String action : actions) {

                if (accessControl.canAccess(role, action)) {
                    text.append(action).append("  ");
                    hasPermission = true;
                }
            }

            if (!hasPermission) {
                text.append("No permissions");
            }

            text.append("\n");
        }

        permissionInfo.setText(text.toString());
    }

    // Clear the previous access result
    private void resetResult() {

        resultLabel.setText("Ready to check access");

        resultLabel.setStyle(
                "-fx-font-size: 15px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #475569;"
        );
    }

    // Display an information message
    private void showMessage(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("RBAC System");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
