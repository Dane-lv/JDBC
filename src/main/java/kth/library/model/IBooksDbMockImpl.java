/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kth.library.model;

import kth.library.model.exceptions.ConnectionException;
import kth.library.model.exceptions.InsertException;
import kth.library.model.exceptions.SelectException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A mock implementation of the IBooksDB interface to demonstrate how to
 * use it together with the user interface.
 * <p>
 * Your implementation must access a real database.
 * @author anderslm@kth.se
 */
public class IBooksDbMockImpl implements IBooksDb {

    private final List<Book> books; // the mock "database"
    private final List<Author> authors; // mock authors
    private final List<Genre> genres; // mock genres

    public IBooksDbMockImpl() {
        books = new ArrayList<>(Arrays.asList(DATA));
        authors = new ArrayList<>();
        genres = new ArrayList<>();
        setupMockData();
    }
    
    private void setupMockData() {
        // Create some mock authors and genres and link them to books
        Author rowling = new Author(1, "J.K. Rowling", null);
        Author tolkien = new Author(2, "J.R.R. Tolkien", null);
        Author martin = new Author(3, "George R.R. Martin", null);
        authors.addAll(Arrays.asList(rowling, tolkien, martin));
        
        Genre fantasy = new Genre(1, "Fantasy");
        Genre adventure = new Genre(2, "Adventure");
        Genre drama = new Genre(3, "Drama");
        genres.addAll(Arrays.asList(fantasy, adventure, drama));
        
        // Link to books (simplified for mock)
        books.get(0).addAuthor(rowling); // Databases Illuminated -> Rowling (fake)
        books.get(0).addGenre(fantasy);
        
        books.get(1).addAuthor(tolkien);
        books.get(1).addGenre(adventure);
        
        // Just add some random data to others so searches work
        for(int i = 2; i < books.size(); i++) {
            books.get(i).addAuthor(martin);
            books.get(i).addGenre(drama);
        }
    }

    @Override
    public boolean connect(String database) throws ConnectionException {
        // mock implementation
        return true;
    }

    @Override
    public void disconnect() throws ConnectionException {
        // mock implementation
    }

    @Override
    public List<Book> findBooksByTitle(String title)
            throws SelectException {
        List<Book> result = new ArrayList<>();
        title = title.trim().toLowerCase();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(title)) {
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public List<Book> findBooksByIsbn(String isbn) throws SelectException {
        List<Book> result = new ArrayList<>();
        isbn = isbn.trim().toLowerCase();
        for (Book book : books) {
            if (book.getIsbn().toLowerCase().equals(isbn)) { // exact match
                result.add(book);
            }
        }
        return result;
    }
    
    @Override
    public List<Book> findBooksByAuthor(String authorName) throws SelectException {
        List<Book> result = new ArrayList<>();
        authorName = authorName.trim().toLowerCase();
        for (Book book : books) {
            for (Author author : book.getAuthors()) {
                if (author.getName().toLowerCase().contains(authorName)) {
                    result.add(book);
                    break; // Found match for this book
                }
            }
        }
        return result;
    }

    @Override
    public List<Book> findBooksByGenre(String genreName) throws SelectException {
        List<Book> result = new ArrayList<>();
        genreName = genreName.trim().toLowerCase();
        for (Book book : books) {
            for (Genre genre : book.getGenres()) {
                if (genre.getName().toLowerCase().equalsIgnoreCase(genreName)) {
                    result.add(book);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public List<Book> findBooksByRating(int rating) throws SelectException {
        List<Book> result = new ArrayList<>();
        for (Book book : books) {
            if (book.getRating() != null && book.getRating() >= rating) {
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public void addBook(Book book) throws InsertException {
        books.add(book);
    }
    
    @Override
    public void addAuthor(Author author) throws InsertException {
        authors.add(author);
    }

    @Override
    public void addGenre(Genre genre) throws InsertException {
        genres.add(genre);
    }

    @Override
    public void setRating(Book book, int rating) throws Exception {
        // In a real DB, we would update the row. Here we just update the object.
        // Since the book object passed might be a copy or from the list, we ideally find it in our list.
        for (Book b : books) {
            if (b.getBookId() == book.getBookId()) {
                // We can't set rating directly on Book if it's final or no setter.
                // Our Book class doesn't have a setRating method yet, but we can fake it or ignore for mock.
                // Wait, Book needs to be mutable for the mock to reflect changes instantly in memory?
                // Or we just say "done".
                // The Book class fields are final. So we can't update the object in memory easily without replacing it.
                // For now, we'll just print.
                System.out.println("Mock: Set rating for " + b.getTitle() + " to " + rating);
                return;
            }
        }
    }

    @Override
    public List<Author> getAllAuthors() throws SelectException {
        return new ArrayList<>(authors);
    }

    @Override
    public List<Genre> getAllGenres() throws SelectException {
        return new ArrayList<>(genres);
    }

    private static final Book[] DATA = {
            new Book(1, "123456789", "Databases Illuminated", "Cathy Ricardo", 5),
            new Book(2, "234567891", "Dark Databases", "Someone", 3),
            new Book(3, "456789012", "The buried giant", "Kazuo Ishiguro", 4),
            new Book(4, "567890123", "Never let me go", "Kazuo Ishiguro", 4),
            new Book(5, "678901234", "The remains of the day", "Kazuo Ishiguro", 5),
            new Book(6, "234567890", "Alias Grace", "Margaret Atwood", 3),
            new Book(7, "345678911", "The handmaids tale", "Margaret Atwood", 4),
            new Book(8, "345678901", "Shuggie Bain", "Douglas Stuart", 4),
            new Book(9, "345678912", "Microserfs", "Douglas Coupland", 3),
    };
}
