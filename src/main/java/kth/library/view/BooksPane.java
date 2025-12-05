package kth.library.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import kth.library.model.Book;
import kth.library.model.IBooksDb;
import kth.library.model.SearchMode;

import java.sql.Date;
import java.util.List;
import java.util.Optional;


/**
 * The main pane for the view, extending VBox and including the menus. An
 * internal BorderPane holds the TableView for books and a search utility.
 *
 * @author anderslm@kth.se
 */
public class BooksPane extends VBox {

    private TableView<Book> booksTable;
    private ObservableList<Book> booksInTable; // the data backing the table view

    private ComboBox<SearchMode> searchModeBox;
    private TextField searchField;
    private Button searchButton;

    private MenuBar menuBar;
    
    private MenuItem updateItem; // Need access to disable/enable or handle

    public BooksPane(IBooksDb booksDb) {
        final Controller controller = new Controller(booksDb, this);
        this.init(controller);
    }

    /**
     * Display a new set of books, e.g. from a database select, in the
     * booksTable table view.
     *
     * @param books the books to display
     */
    public void displayBooks(List<Book> books) {
        booksInTable.clear();
        booksInTable.addAll(books);
    }

    /**
     * Notify user on input error or exceptions.
     *
     * @param msg  the message
     * @param type types: INFORMATION, WARNING et c.
     */
    protected void showAlertAndWait(String msg, Alert.AlertType type) {
        // types: INFORMATION, WARNING et c.
        Alert alert = new Alert(type, msg);
        alert.showAndWait();
    }

    void init(Controller controller) {

        booksInTable = FXCollections.observableArrayList();

        // init views and event handlers
        initBooksTable();
        initSearchView(controller);
        initMenus(controller);

        FlowPane bottomPane = new FlowPane();
        bottomPane.setHgap(10);
        bottomPane.setPadding(new Insets(10, 10, 10, 10));
        bottomPane.getChildren().addAll(searchModeBox, searchField, searchButton);

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(booksTable);
        mainPane.setBottom(bottomPane);
        mainPane.setPadding(new Insets(10, 10, 10, 10));

        this.getChildren().addAll(menuBar, mainPane);
        VBox.setVgrow(mainPane, Priority.ALWAYS);
    }

    private void initBooksTable() {
        booksTable = new TableView<>();
        booksTable.setEditable(false); // don't allow user updates (yet)
        booksTable.setPlaceholder(new Label("No rows to display"));

        // define columns
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        TableColumn<Book, String> publisherCol = new TableColumn<>("Publisher");
        TableColumn<Book, Integer> ratingCol = new TableColumn<>("Rating"); // Added Rating column
        
        booksTable.getColumns().addAll(titleCol, isbnCol, publisherCol, ratingCol);
        // give title column some extra space
        titleCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.5));

        // define how to fill data for each cell, 
        // get values from Book properties
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        publisherCol.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));

        // associate the table view with the data
        booksTable.setItems(booksInTable);
    }

    private void initSearchView(Controller controller) {
        searchField = new TextField();
        searchField.setPromptText("Search for...");
        searchModeBox = new ComboBox<>();
        searchModeBox.getItems().addAll(SearchMode.values());
        searchModeBox.setValue(SearchMode.Title);
        searchButton = new Button("Search");

        // event handling (dispatch to controller)
        searchButton.setOnAction(event -> {
            String searchFor = searchField.getText();
            SearchMode mode = searchModeBox.getValue();
            controller.onSearchSelected(searchFor, mode);
        });
    }

    private void initMenus(Controller controller) {

        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        MenuItem connectItem = new MenuItem("Connect to Db");
        MenuItem disconnectItem = new MenuItem("Disconnect");
        fileMenu.getItems().addAll(exitItem, connectItem, disconnectItem);
        
        exitItem.setOnAction(e -> System.exit(0));

        Menu searchMenu = new Menu("Search");
        MenuItem titleItem = new MenuItem("Title");
        MenuItem isbnItem = new MenuItem("ISBN");
        MenuItem authorItem = new MenuItem("Author");
        MenuItem genreItem = new MenuItem("Genre");
        MenuItem ratingItem = new MenuItem("Rating");
        searchMenu.getItems().addAll(titleItem, isbnItem, authorItem, genreItem, ratingItem);
        
        // Bind menu items to change the combo box selection
        titleItem.setOnAction(e -> searchModeBox.setValue(SearchMode.Title));
        isbnItem.setOnAction(e -> searchModeBox.setValue(SearchMode.ISBN));
        authorItem.setOnAction(e -> searchModeBox.setValue(SearchMode.Author));
        genreItem.setOnAction(e -> searchModeBox.setValue(SearchMode.Genre));
        ratingItem.setOnAction(e -> searchModeBox.setValue(SearchMode.Rating));

        Menu manageMenu = new Menu("Manage");
        MenuItem addItem = new MenuItem("Add");
        MenuItem removeItem = new MenuItem("Remove");
        MenuItem updateItem = new MenuItem("Update Rating");
        MenuItem detailsItem = new MenuItem("Show Details"); // Requirement D
        manageMenu.getItems().addAll(addItem, removeItem, updateItem, new SeparatorMenuItem(), detailsItem);
        this.updateItem = updateItem;

        menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, searchMenu, manageMenu);

        // Event handlers
        updateItem.setOnAction(e -> {
            Book selected = booksTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                controller.onRateBookSelected(selected);
            } else {
                showAlertAndWait("No book selected", Alert.AlertType.WARNING);
            }
        });
        
        detailsItem.setOnAction(e -> {
            Book selected = booksTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Show details about authors and genres
                StringBuilder sb = new StringBuilder();
                sb.append("Title: ").append(selected.getTitle()).append("\n");
                sb.append("ISBN: ").append(selected.getIsbn()).append("\n");
                sb.append("Publisher: ").append(selected.getPublisher()).append("\n\n");
                
                sb.append("Authors:\n");
                if (selected.getAuthors().isEmpty()) {
                    sb.append(" - (None listed)\n");
                } else {
                    for (kth.library.model.Author a : selected.getAuthors()) {
                        sb.append(" - ").append(a.getName()).append("\n");
                    }
                }
                
                sb.append("\nGenres:\n");
                if (selected.getGenres().isEmpty()) {
                    sb.append(" - (None listed)\n");
                } else {
                    for (kth.library.model.Genre g : selected.getGenres()) {
                        sb.append(" - ").append(g.getName()).append("\n");
                    }
                }
                
                showAlertAndWait(sb.toString(), Alert.AlertType.INFORMATION);
            } else {
                showAlertAndWait("No book selected", Alert.AlertType.WARNING);
            }
        });
        
        addItem.setOnAction(e -> {
            controller.onAddBookSelected();
        });
    }
    
    public Book getSelectedBook() {
        return booksTable.getSelectionModel().getSelectedItem();
    }
}
