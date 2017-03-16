package POP3_ClientServer.Client;

import POP3_ClientServer.common.EMail;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.Observer;

/**
 *
 * @author sachouw
 */
public class MailsOverview {

    private ClientApp mainApp;

    @FXML
    private TableView<EMail> mailsTable;
    @FXML
    private TableColumn<EMail, String> expediteurColumn;
    @FXML
    private TableColumn<EMail, String> objetColumn;

    /* Plus tard pour vue de détail ?
    @FXML
    private Label ExpediteurLabel;
    @FXML
    private Label ObjetLabel;
    @FXML
    private Label DateLabel;
    @FXML
    private Label DestinataireLabel;
    @FXML
    private Label messageLabel;
    */

    public MailsOverview() {}

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Initialize the mail table with the two columns.
        expediteurColumn.setCellValueFactory(cellData -> cellData.getValue().getFromProperty());
        objetColumn.setCellValueFactory(cellData -> cellData.getValue().getObjetProperty());
    }

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(ClientApp mainApp) {
        this.mainApp = mainApp;

        // Add observable list data to the vue
        mailsTable.setItems(mainApp.getMails());
    }
    
}
