package kth.library;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kth.library.model.IBooksDb;
import kth.library.model.IBooksDbMockImpl;
import kth.library.view.BooksPane;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        IBooksDb booksDb = new IBooksDbMockImpl(); // model
        BooksPane booksPane = new BooksPane(booksDb); // also creates a controller
        // Don't forget to connect to the db, somewhere...

        Scene scene = new Scene(booksPane, 800, 600);
        primaryStage.setTitle("Books Database Client");
        // add an exit handler to the stage (X)
        primaryStage.setOnCloseRequest(event -> {
            try {
                booksDb.disconnect();
            } catch (Exception e) {
            }
        });
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}