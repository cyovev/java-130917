import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import user.User;
import user.UserJDBMCDAO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
 
public class Main extends Application {
	
    private UserJDBMCDAO controller;
    private Scene        addScene, listScene;
	private TableView<User>      table = new TableView<User>();
    private ObservableList<User> users = FXCollections.observableArrayList();
    private Button clearFilterButton = new Button("Clear");
 
    public static void main(String[] args) {
        launch(args);
    }
 
    @Override
    public void start(Stage stage) {
        try {
            // set up the controller which will operate with the DB
            this.controller = new UserJDBMCDAO();
            
            // set the window title
            stage.setTitle("Christo Yovev @ TVZ 2020");
            
            // add both scenes
            addScene  = _addAddScene(stage, "Add a student");
            listScene = _addListScene(stage, "Students");
            
            // don't allow the window to be resized
            stage.setResizable(false);
            
            // set the list scene to be the default one
            stage.setScene(listScene);
            stage.show();
        }
        catch (Exception e) {
            showErrorDialog(e.getMessage());
        }

    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected Scene _addAddScene(final Stage stage, String titleLabel) {
        VBox addForm = _addNewRecordForm(stage, titleLabel);
        VBox vbox    = new VBox(addForm);
        
        // place the vbox in the center
        vbox.setAlignment(Pos.CENTER);
                
        Scene scene  = new Scene(vbox, 220, 230);
        
        return scene;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected Scene _addListScene(final Stage stage, String titleLabel) {
        // load all existing users and pass them on to the table
        this.users      = controller.getAllUsers();
        this.table      = _setUpTable(this.users);

        Label label     = _addLabelTitle(titleLabel, 20);
        HBox filterForm = _addFilterForm();
        VBox body       = _addListBody();
        
        // put the label, the filter form and the table in the body vbox
        body.getChildren().addAll(label, filterForm, this.table);
        
        // create a new vbox and place the menubar and the body vbox inside it 
        VBox window     = new VBox();
        VBox menu       = _addMenuBarVBox(stage);
        window.getChildren().addAll(menu, body);
        
        Scene scene     = new Scene(window, 830, 570);
        scene.getClass().getResourceAsStream("resources/style.css");
        
        return scene;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected VBox _addListBody() {
        VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        
        return vbox;
    }
    
    
    ///////////////////////////////////////////////////////////////////////////
    // set focus to the label, otherwise the first text field will be focused
    protected Label _addLabelTitle(String title, int fontSize) {
        Label label = new Label(title);
        label.setFont(new Font("Arial", fontSize));
        
        label.setFocusTraversable(true);
        
        return label;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // for each column that reads data from the object,
    // there should be a method get<field> in the class (e.g. getFirstName);
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected TableView<User> _setUpTable(ObservableList<User> data) {
        TableView<User> table    = new TableView<User>();
        TableColumn firstNameCol = _addFirstNameColumn(100);
        TableColumn lastNameCol  = _addLastNameColumn(100);
        TableColumn emailCol     = _addEmailColumn(200);
        TableColumn createdCol   = _addDateColumn(160, "created", "Created");
        TableColumn modifiedCol  = _addDateColumn(160, "modified", "Modified");
        TableColumn actionCol    = _addActionColumn(50);
        
        table.setEditable(true);     
        table.getColumns().addAll(firstNameCol, lastNameCol, emailCol, createdCol, modifiedCol, actionCol);
        table.setItems(data);
        
        return table;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected VBox _addMenuBarVBox(final Stage stage) {
        Menu menu = new Menu("File");
        
        // the «Add» menu item should switch to the add scene
        MenuItem add = new MenuItem("Add");
        add.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                stage.setScene(addScene);
            }
        });
        
        // the exit menu item should exit the application after the user's confirmation
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                exitApplication();
            }
        });
        
        menu.getItems().add(add);
        menu.getItems().add(exit);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(menu);
        
        VBox menuVBox   = new VBox(menuBar);
        
        return menuVBox;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected TableColumn _addFirstNameColumn(int width) {
        TableColumn column = new TableColumn("First Name");
        column.setMinWidth(width);
        column.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setOnEditCommit(
            new EventHandler<CellEditEvent<User, String>>() {
                @Override
                public void handle(CellEditEvent<User, String> cell) {
                    User user = (User) cell.getTableView().getItems().get(cell.getTablePosition().getRow());
                    // if the update was successful (both SQL and object),
                    // the modified column needs to be updated
                    if (controller.updateFirstName(user, cell.getNewValue())) {
                        refreshModifiedColumn();
                    }
                    
                    // otherwise show the error and don't accept the edit
                    else {
                        showErrorDialog(controller.getErrorMessage());
                        resetOldCellValue(cell, user);                        
                    }
                }
            }
        );
        
        return column;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected TableColumn _addLastNameColumn(int width) {
        TableColumn column = new TableColumn("Last Name");
        column.setMinWidth(width);
        column.setCellValueFactory(new PropertyValueFactory<User, String>("lastName"));
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setOnEditCommit(
            new EventHandler<CellEditEvent<User, String>>() {
                @Override
                public void handle(CellEditEvent<User, String> cell) {
                    User user = (User) cell.getTableView().getItems().get(cell.getTablePosition().getRow());
                    // if the update was successful (both SQL and object),
                    // the modified column needs to be updated
                    if (controller.updateLastName(user, cell.getNewValue())) {
                        refreshModifiedColumn();
                    }
                    
                    // otherwise show the error and don't accept the edit
                    else {
                        showErrorDialog(controller.getErrorMessage());
                        resetOldCellValue(cell, user);
                    }
                }
            }
        );
        
        return column;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected TableColumn _addEmailColumn(int width) {
        TableColumn column = new TableColumn("Email");
        column.setMinWidth(200);
        column.setCellValueFactory(new PropertyValueFactory<User, String>("email"));
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setOnEditCommit(
            new EventHandler<CellEditEvent<User, String>>() {
                @Override
                public void handle(CellEditEvent<User, String> cell) {
                    User user = (User) cell.getTableView().getItems().get(cell.getTablePosition().getRow());
                    // if the update was successful (both SQL and object),
                    // the modified column needs to be updated
                    if (controller.updateEmail(user, cell.getNewValue())) {
                        refreshModifiedColumn();
                    }
                    
                    // otherwise show the error and don't accept the edit
                    else {
                        showErrorDialog(controller.getErrorMessage());
                        resetOldCellValue(cell, user);
                    }
                }
            }
        );
        
        return column;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected TableColumn _addDateColumn(int width, String field, String label) {
        TableColumn column = new TableColumn(label);
        column.setMinWidth(width);
        column.setCellValueFactory(new PropertyValueFactory<User, String>(field));
        
        // declare a cellFactory which will disable the field from being edited
        Callback<TableColumn<User, String>, TableCell<User, String>> cellFactory = new Callback<TableColumn<User, String>, TableCell<User, String>>() {
            @Override
            public TableCell call(final TableColumn<User, String> param) {
                final TableCell<User, String> cell = new TableCell<User, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            setEditable(false);
                            setText(item);
                        }
                    }
                };
                return cell;
            }
        };
        
        column.setCellFactory(cellFactory);
        
        return column;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // TableColumn needs cellValueFactory to render the column
    // but there is no Action column in the data set, so a work-around is
    // to add some string (e. g. "empty") to the cellValueFactory and redeclare the cellFactory
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected TableColumn _addActionColumn(int width) {
        TableColumn column = new TableColumn("Action");
        column.setMinWidth(width);
        column.setCellValueFactory(new PropertyValueFactory<>("empty"));
        
        // adding delete button
        Callback<TableColumn<User, String>, TableCell<User, String>> cellFactory = new Callback<TableColumn<User, String>, TableCell<User, String>>() {
            @Override
            public TableCell call(final TableColumn<User, String> param) {
                final TableCell<User, String> cell = new TableCell<User, String>() {
                    final Button btn = new Button("Delete");
        
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        // if the item is empty, keep the cell empty, too
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        }
                        
                        // otherwise add the button and hook an event to it
                        else {
                            setGraphic(btn);
                            setText(null);
                            
                            btn.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent e) {
                                    // get the User which corresponds to this row
                                    User user   = getTableView().getItems().get(getIndex());
                                    String name = user.getFullName();
                                    int id      = user.getId();
                                    
                                    // if the user has confirmed and the deletion was successful,
                                    // remove the user from the list, refresh the table and notify the end-user
                                    if (userHasConfirmedDeletion(name)) {
                                        if (controller.deleteUser(id)) { 
                                            users.remove(user);
                                            table.refresh();
                                            showInformationDialog("User " + name + " was successfully deleted!");
                                        }
                                        // if the deletion was not successful, show the error message
                                        else {
                                            showErrorDialog(controller.getErrorMessage());   
                                        }
                                    }
                                }
                            });
                        }
                    }
                };
                return cell;
            }
        };
        
        column.setCellFactory(cellFactory);
        
        return column;
    }

    ///////////////////////////////////////////////////////////////////////////
    protected VBox _addNewRecordForm(final Stage stage, String titleLabel) {
        VBox vbox = new VBox();
        
        final Label label = _addLabelTitle(titleLabel, 20);
        
        final TextField addFirstName = new TextField();
        addFirstName.setPromptText("First Name");
        addFirstName.setMaxWidth(200);
        
        final TextField addLastName = new TextField();
        addLastName.setMaxWidth(200);
        addLastName.setPromptText("Last Name");
        
        final TextField addEmail = new TextField();
        addEmail.setMaxWidth(200);
        addEmail.setPromptText("Email");
 
        final Button addButton = new Button("Add");
        addButton.setMaxWidth(200);
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                // store all fields in params and try to save the data
                // (after proper validation)
                Map<String, String> params = new HashMap<String, String>();
                params.put("first_name", addFirstName.getText());
                params.put("last_name",  addLastName.getText());
                params.put("email",      addEmail.getText());
                
                // if adding the new user was not successful, print the error message
                User new_user = controller.addUser(params);
                if (new_user == null) {
                    showErrorDialog(controller.getErrorMessage());
                }
                
                // otherwise add the user to the users list, refresh the table,
                // clear all input fields, show confirmation to the user and bring back the main scene
                else {
                    users.add(new_user);
                    table.refresh();
                    clearInputFields(new TextField[]{addFirstName, addLastName, addEmail});
                    showInformationDialog("User " + new_user.getFullName() + " was successfully created!");
                    stage.setScene(listScene);
                }
                
            }
        });
        
        // the go back button should display the main scene and clean the input fields
        final Button backButton = new Button("« Go back");
        backButton.setMaxWidth(200);
        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                clearInputFields(new TextField[]{addFirstName, addLastName, addEmail});
                stage.setScene(listScene);
            }
        });
 
        vbox.getChildren().addAll(label, addFirstName, addLastName, addEmail, addButton, backButton);
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        
        return vbox;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected HBox _addFilterForm() {
        final TextField filterFirstName = new TextField();
        filterFirstName.setPrefWidth(100);
        filterFirstName.setPromptText("First Name");
        
        final TextField filterLastName = new TextField();
        filterLastName.setPrefWidth(100);
        filterLastName.setPromptText("Last Name");
        
        final TextField filterEmail = new TextField();
        filterEmail.setPrefWidth(150);
        filterEmail.setPromptText("Email");
 
        final Button filterButton = new Button("Filter");
        filterButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {                
                Map<String, String> params = new HashMap<String, String>();
                params.put("first_name", filterFirstName.getText());
                params.put("last_name",  filterLastName.getText());
                params.put("email",      filterEmail.getText());
                
                filterData(params);
            }
        });
        
        // when the filter gets cleared, all users get re-fetched, and sent to the table,
        // the table itself gets refreshed and the button gets hidden
        this.clearFilterButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                clearInputFields(new TextField[]{filterFirstName, filterLastName, filterEmail});
                users = controller.getAllUsers();
                table.setItems(users);
                table.refresh();
                Object node = e.getSource();
                hideButton(((Button) node));
            }
        });
        
        // clear filter button is hidden by default, it gets shown only when the filter is active
        hideButton(this.clearFilterButton);
        
        HBox hb_filter = new HBox();
        hb_filter.getChildren().addAll(filterFirstName, filterLastName, filterEmail, filterButton, this.clearFilterButton);
        hb_filter.setSpacing(3);
        hb_filter.setPadding(new Insets(15, 0, 10, 0));
        
        return hb_filter;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected void filterData(Map<String, String> params) {
        // if none of the fields were filled in, ask the user to do so
        if (controller.isEmpty(params.get("first_name")) && controller.isEmpty(params.get("last_name")) && controller.isEmpty(params.get("email"))) {
            showErrorDialog("Please enter some data in the fields.");
        }
        
        // if at least one field was indeed filled in,
        // filter the data, refresh the table, and show the clear filter button 
        else {
            this.users = controller.getAllUsers(params);
            table.setItems(this.users);
            table.refresh();
            showButton(this.clearFilterButton);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected void clearInputFields(TextField[] fields) {
        for (TextField field: fields){
            field.clear();
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected void hideButton(Button btn) {
        btn.setVisible(false);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected void showButton(Button btn) {
        btn.setVisible(true);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected boolean userHasConfirmedDeletion(String name) {
        Alert showDialog = new Alert(AlertType.CONFIRMATION);
        showDialog.setTitle("Deleting a user");
        showDialog.setHeaderText("You are about to delete user " + name + ". \nThis action cannot be undone.");
        showDialog.setContentText("Are you sure you want to do this?");

        Optional<ButtonType> result = showDialog.showAndWait();
        return (result.get() == ButtonType.OK);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected void exitApplication() {
        Alert showDialog = new Alert(AlertType.CONFIRMATION);
        showDialog.setTitle("Exit");
        showDialog.setHeaderText("Are you sure you want to exit?");

        Optional<ButtonType> result = showDialog.showAndWait();
        if (result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected void showDialog(Alert dialog, String message, String dialogTitle, String type) {
        dialog.setTitle(dialogTitle);
        dialog.setHeaderText(message);

        dialog.showAndWait();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected void showInformationDialog(String message) {
        Alert dialog = new Alert(AlertType.INFORMATION);
        this.showDialog(dialog, message, "Success!", "information");
    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected void showErrorDialog(String message) {
        Alert dialog = new Alert(AlertType.ERROR);
        this.showDialog(dialog, message, "Error!", "error");
    }
    
    ///////////////////////////////////////////////////////////////////////////
    private void resetOldCellValue(CellEditEvent<User, String> t, User user) {
        t.getTableView().getItems().set(t.getTablePosition().getRow(), user);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    private void refreshModifiedColumn() {
        table.refresh();
    }
}
