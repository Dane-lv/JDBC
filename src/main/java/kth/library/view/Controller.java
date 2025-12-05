package kth.library.view;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import kth.library.model.Book;
import kth.library.model.IBooksDb;
import kth.library.model.SearchMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static javafx.scene.control.Alert.AlertType.*;

/**
 * The controller is responsible for handling user requests and update the view
 * (and in some cases the model).
 *
 * @author anderslm@kth.se
 */
public class Controller {

    private final BooksPane booksView; // view
    private final IBooksDb booksDb; // model

    public Controller(IBooksDb booksDb, BooksPane booksView) {
        this.booksDb = booksDb;
        this.booksView = booksView;
    }

    protected void onSearchSelected(String searchFor, SearchMode mode) {
        new Thread(() -> {
            try {
                if (searchFor != null && !searchFor.isEmpty()) {
                    List<Book> result = null;
                    switch (mode) {
                        case Title:
                            result = booksDb.findBooksByTitle(searchFor);
                            break;
                        case ISBN:
                            result = booksDb.findBooksByIsbn(searchFor);
                            break;
                        case Author:
                            result = booksDb.findBooksByAuthor(searchFor);
                            break;
                        case Genre:
                            result = booksDb.findBooksByGenre(searchFor);
                            break;
                        case Rating:
                            // For rating, we expect an integer
                            try {
                                int rating = Integer.parseInt(searchFor);
                                result = booksDb.findBooksByRating(rating);
                            } catch (NumberFormatException e) {
                                Platform.runLater(() -> booksView.showAlertAndWait("Rating must be a number", WARNING));
                                return;
                            }
                            break;
                        default:
                            result= new ArrayList<>();
                    }
                    final List<Book> finalResult = result;
                    Platform.runLater(() -> {
                        if (finalResult == null || finalResult.isEmpty()) {
                            booksView.showAlertAndWait("No results found.", INFORMATION);
                        } else {
                            booksView.displayBooks(finalResult);
                        }
                    });
                } else {
                    Platform.runLater(() -> booksView.showAlertAndWait("Enter a search string!", WARNING));
                }
            } catch (Exception e) {
                Platform.runLater(() -> booksView.showAlertAndWait("Database error: " + e.getMessage(), ERROR));
            }
        }).start();
    }
    
    protected void onRateBookSelected(Book book) {
        List<Integer> choices = Arrays.asList(1, 2, 3, 4, 5);
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(book.getRating() != null ? book.getRating() : 3, choices);
        dialog.setTitle("Rate Book");
        dialog.setHeaderText("Set rating for: " + book.getTitle());
        dialog.setContentText("Choose rating:");

        Optional<Integer> result = dialog.showAndWait();
        result.ifPresent(rating -> {
            new Thread(() -> {
                try {
                    booksDb.setRating(book, rating);
                    Platform.runLater(() -> {
                        booksView.showAlertAndWait("Rating updated!", INFORMATION);
                        // Ideally refresh the view or update the book object in the list
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> booksView.showAlertAndWait("Error updating rating: " + e.getMessage(), ERROR));
                }
            }).start();
        });
    }
    
    protected void onAddBookSelected() {
        new Thread(() -> {
            try {
                // Fetch available authors and genres first
                List<kth.library.model.Author> authors = booksDb.getAllAuthors();
                List<kth.library.model.Genre> genres = booksDb.getAllGenres();

                Platform.runLater(() -> {
                    AddBookDialog dialog = new AddBookDialog(authors, genres);
                    Optional<Book> result = dialog.showAndWait();
                    
                    result.ifPresent(newBook -> {
                        new Thread(() -> {
                            try {
                                booksDb.addBook(newBook);
                                Platform.runLater(() -> {
                                    booksView.showAlertAndWait("Book added successfully!", INFORMATION);
                                });
                            } catch (Exception e) {
                                Platform.runLater(() -> booksView.showAlertAndWait("Error adding book: " + e.getMessage(), ERROR));
                            }
                        }).start();
                    });
                });
            } catch (Exception e) {
                Platform.runLater(() -> booksView.showAlertAndWait("Could not fetch authors/genres: " + e.getMessage(), ERROR));
            }
        }).start();
    }
}
