package kth.library.model;

import java.sql.Date;

/**
 * Represents a review (rating + optional text) by a user for a book.
 */
public class Review {
    private final Book book;
    private final User user;
    private final int rating;
    private final String reviewText;
    private final Date date;

    public Review(Book book, User user, int rating, String reviewText, Date date) {
        this.book = book;
        this.user = user;
        this.rating = rating;
        this.reviewText = reviewText;
        this.date = date;
    }

    public Book getBook() {
        return book;
    }

    public User getUser() {
        return user;
    }

    public int getRating() {
        return rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return rating + "/5 by " + user.getUsername() + " (" + date + ")";
    }
}

