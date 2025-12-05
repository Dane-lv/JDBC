package kth.library.model;

import kth.library.model.exceptions.ConnectionException;
import kth.library.model.exceptions.InsertException;
import kth.library.model.exceptions.SelectException;

import java.util.List;

/**
 * This interface declares methods for querying a Books database.
 * Different implementations of this interface handles the connection and
 * queries to a specific DBMS and database, for example a MySQL or a MongoDB
 * database.
 *
 * NB! The methods in the implementation must catch the SQL/MongoDBExceptions thrown
 * by the underlying driver, wrap in a Connection/Insert/SelectException and re-throw the
 * latter exception. This way the interface is the same for both implementations, because the
 * exception type in the method signatures is the same. More info in the mentioned exception classes.
 * 
 * @author anderslm@kth.se
 */
public interface IBooksDb {
    
    /**
     * Connect to the database.
     * @param database url
     * @return true on successful connection
     */
    boolean connect(String database) throws ConnectionException;
    
    void disconnect() throws ConnectionException;
    
    List<Book> findBooksByTitle(String title) throws SelectException;

    List<Book> findBooksByIsbn(String isbn) throws SelectException;
    
    List<Book> findBooksByAuthor(String author) throws SelectException;
    
    List<Book> findBooksByGenre(String genre) throws SelectException;
    
    List<Book> findBooksByRating(int rating) throws SelectException;
    
    void addBook(Book book) throws InsertException;
    
    /**
     * Add an author to the database. 
     * Note: If the author already exists, this method might throw an exception or handle it gracefully
     * depending on implementation.
     */
    void addAuthor(Author author) throws InsertException;

    /**
     * Add a genre to the database.
     */
    void addGenre(Genre genre) throws InsertException;

    /**
     * Set or update the rating for a book.
     */
    void setRating(Book book, int rating) throws Exception; // Using Exception or a specific UpdateException if created
    
    // Helper methods to fetch available authors/genres for the "Add Book" dialog
    List<Author> getAllAuthors() throws SelectException;
    List<Genre> getAllGenres() throws SelectException;
}
