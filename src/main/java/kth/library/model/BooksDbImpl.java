package kth.library.model;

import kth.library.model.exceptions.ConnectionException;
import kth.library.model.exceptions.InsertException;
import kth.library.model.exceptions.SelectException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A MySQL implementation of the IBooksDb interface.
 */
public class BooksDbImpl implements IBooksDb {

    private Connection connection;
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String USER = "library_client";
    private static final String PASSWORD = "lib123";

    public BooksDbImpl() {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load JDBC driver: " + JDBC_DRIVER, e);
        }
    }

    @Override
    public boolean connect(String databaseUrl) throws ConnectionException {
        try {
            if (connection != null && !connection.isClosed()) {
                return true; // Already connected
            }
            connection = DriverManager.getConnection(databaseUrl, USER, PASSWORD);
            return true;
        } catch (SQLException e) {
            throw new ConnectionException("Could not connect to database: " + databaseUrl, e);
        }
    }

    @Override
    public void disconnect() throws ConnectionException {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new ConnectionException("Could not disconnect from database.", e);
        }
    }
    
    // --- Helper method to map ResultSet to Book list ---
    private List<Book> mapBooks(ResultSet rs) throws SQLException, SelectException {
        List<Book> books = new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt("book_id");
            String isbn = rs.getString("isbn");
            String title = rs.getString("title");
            String publisher = rs.getString("publisher");
            // rating can be null
            int ratingVal = rs.getInt("rating");
            Integer rating = rs.wasNull() ? null : ratingVal;
            
            Book book = new Book(id, isbn, title, publisher, rating);
            // We need to populate authors and genres for this book.
            // In a real app with many rows, we might want to do this eagerly with JOINs 
            // or lazily. For this assignment, let's fetch them separately or use a JOIN query initially.
            // The current structure of the loop implies we already executed a query.
            // If the query was simple (SELECT * FROM T_Book), we need to fetch authors/genres now.
            
            fetchAuthorsForBook(book);
            fetchGenresForBook(book);
            
            books.add(book);
        }
        return books;
    }
    
    private void fetchAuthorsForBook(Book book) throws SQLException {
        String sql = "SELECT a.author_id, a.name, a.birthdate FROM T_Author a " +
                     "JOIN T_Book_Author ba ON a.author_id = ba.author_id " +
                     "WHERE ba.book_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, book.getBookId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("author_id");
                    String name = rs.getString("name");
                    Date birthDate = rs.getDate("birthdate");
                    book.addAuthor(new Author(id, name, birthDate));
                }
            }
        }
    }
    
    private void fetchGenresForBook(Book book) throws SQLException {
        String sql = "SELECT g.genre_id, g.name FROM T_Genre g " +
                     "JOIN T_Book_Genre bg ON g.genre_id = bg.genre_id " +
                     "WHERE bg.book_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, book.getBookId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("genre_id");
                    String name = rs.getString("name");
                    book.addGenre(new Genre(id, name));
                }
            }
        }
    }

    @Override
    public List<Book> findBooksByTitle(String title) throws SelectException {
        String sql = "SELECT * FROM T_Book WHERE title LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + title + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                return mapBooks(rs);
            }
        } catch (SQLException e) {
            throw new SelectException("Error finding books by title: " + title, e);
        }
    }

    @Override
    public List<Book> findBooksByIsbn(String isbn) throws SelectException {
        String sql = "SELECT * FROM T_Book WHERE isbn = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, isbn.trim()); // Trim whitespace
            try (ResultSet rs = stmt.executeQuery()) {
                return mapBooks(rs);
            }
        } catch (SQLException e) {
            throw new SelectException("Error finding books by ISBN: " + isbn, e);
        }
    }

    @Override
    public List<Book> findBooksByAuthor(String author) throws SelectException {
        String sql = "SELECT DISTINCT b.* FROM T_Book b " +
                     "JOIN T_Book_Author ba ON b.book_id = ba.book_id " +
                     "JOIN T_Author a ON ba.author_id = a.author_id " +
                     "WHERE a.name LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + author + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                return mapBooks(rs);
            }
        } catch (SQLException e) {
            throw new SelectException("Error finding books by author: " + author, e);
        }
    }

    @Override
    public List<Book> findBooksByGenre(String genre) throws SelectException {
        String sql = "SELECT DISTINCT b.* FROM T_Book b " +
                     "JOIN T_Book_Genre bg ON b.book_id = bg.book_id " +
                     "JOIN T_Genre g ON bg.genre_id = g.genre_id " +
                     "WHERE g.name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, genre);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapBooks(rs);
            }
        } catch (SQLException e) {
            throw new SelectException("Error finding books by genre: " + genre, e);
        }
    }

    @Override
    public List<Book> findBooksByRating(int rating) throws SelectException {
        String sql = "SELECT * FROM T_Book WHERE rating >= ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, rating);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapBooks(rs);
            }
        } catch (SQLException e) {
            throw new SelectException("Error finding books by rating: " + rating, e);
        }
    }

    @Override
    public void addBook(Book book) throws InsertException {
        String insertBookSql = "INSERT INTO T_Book (isbn, title, publisher, rating) VALUES (?, ?, ?, ?)";
        String insertAuthorRelSql = "INSERT INTO T_Book_Author (book_id, author_id) VALUES (?, ?)";
        String insertGenreRelSql = "INSERT INTO T_Book_Genre (book_id, genre_id) VALUES (?, ?)";
        
        try {
            connection.setAutoCommit(false); // Start transaction
            
            // 1. Insert Book
            int bookId;
            try (PreparedStatement stmt = connection.prepareStatement(insertBookSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, book.getIsbn());
                stmt.setString(2, book.getTitle());
                stmt.setString(3, book.getPublisher());
                if (book.getRating() == null) {
                    stmt.setNull(4, Types.INTEGER);
                } else {
                    stmt.setInt(4, book.getRating());
                }
                stmt.executeUpdate();
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        bookId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating book failed, no ID obtained.");
                    }
                }
            }
            
            // 2. Insert Authors (assuming they exist, based on assignment prompt "Add books... with author specified")
            // The prompt B says "Only give possibility to add already known authors".
            // So we assume book.getAuthors() contains Authors with valid IDs.
            try (PreparedStatement stmt = connection.prepareStatement(insertAuthorRelSql)) {
                for (Author author : book.getAuthors()) {
                    stmt.setInt(1, bookId);
                    stmt.setInt(2, author.getAuthorId());
                    stmt.executeUpdate();
                }
            }
            
            // 3. Insert Genres (assuming they exist)
            try (PreparedStatement stmt = connection.prepareStatement(insertGenreRelSql)) {
                for (Genre genre : book.getGenres()) {
                    stmt.setInt(1, bookId);
                    stmt.setInt(2, genre.getGenreId());
                    stmt.executeUpdate();
                }
            }
            
            connection.commit(); // Commit transaction
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                // Log or ignore
            }
            throw new InsertException("Error adding book: " + book.getTitle(), e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    @Override
    public void addAuthor(Author author) throws InsertException {
        String sql = "INSERT INTO T_Author (name, birthdate) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, author.getName());
            stmt.setDate(2, author.getBirthdate());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new InsertException("Error adding author: " + author.getName(), e);
        }
    }

    @Override
    public void addGenre(Genre genre) throws InsertException {
        String sql = "INSERT INTO T_Genre (name) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, genre.getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new InsertException("Error adding genre: " + genre.getName(), e);
        }
    }

    @Override
    public void setRating(Book book, int rating) throws Exception {
        String sql = "UPDATE T_Book SET rating = ? WHERE book_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, rating);
            stmt.setInt(2, book.getBookId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Could wrap in specific UpdateException if defined
            throw new Exception("Error updating rating for bookId: " + book.getBookId(), e);
        }
    }

    @Override
    public List<Author> getAllAuthors() throws SelectException {
        String sql = "SELECT * FROM T_Author ORDER BY name";
        List<Author> authors = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                authors.add(new Author(rs.getInt("author_id"), rs.getString("name"), rs.getDate("birthdate")));
            }
            return authors;
        } catch (SQLException e) {
            throw new SelectException("Error fetching all authors", e);
        }
    }

    @Override
    public List<Genre> getAllGenres() throws SelectException {
        String sql = "SELECT * FROM T_Genre ORDER BY name";
        List<Genre> genres = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                genres.add(new Genre(rs.getInt("genre_id"), rs.getString("name")));
            }
            return genres;
        } catch (SQLException e) {
            throw new SelectException("Error fetching all genres", e);
        }
    }
}

