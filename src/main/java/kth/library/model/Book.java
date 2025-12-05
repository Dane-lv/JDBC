package kth.library.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a book.
 */
public class Book {

    private final int bookId;
    private final String isbn;
    private final String title;
    private final String publisher;
    private final Integer rating; // Nullable in DB
    
    private final List<Author> authors;
    private final List<Genre> genres;

    public Book(int bookId, String isbn, String title, String publisher, Integer rating) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.publisher = publisher;
        this.rating = rating;
        this.authors = new ArrayList<>();
        this.genres = new ArrayList<>();
    }

    public Book(String isbn, String title, String publisher, Integer rating) {
        this(-1, isbn, title, publisher, rating);
    }

    public int getBookId() {
        return bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getPublisher() {
        return publisher;
    }
    
    public Integer getRating() {
        return rating;
    }

    public List<Author> getAuthors() {
        return authors;
    }
    
    public void addAuthor(Author author) {
        authors.add(author);
    }
    
    public void setAuthors(List<Author> authors) {
        this.authors.clear();
        if (authors != null) {
            this.authors.addAll(authors);
        }
    }

    public List<Genre> getGenres() {
        return genres;
    }
    
    public void addGenre(Genre genre) {
        genres.add(genre);
    }
    
    public void setGenres(List<Genre> genres) {
        this.genres.clear();
        if (genres != null) {
            this.genres.addAll(genres);
        }
    }

    @Override
    public String toString() {
        return title + ", " + isbn + ", " + publisher;
    }
}
