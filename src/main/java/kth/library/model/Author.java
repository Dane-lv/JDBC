package kth.library.model;

import java.sql.Date;

/**
 * Representation of an author.
 */
public class Author {
    private final int authorId;
    private final String name;
    private final Date birthdate;

    public Author(int authorId, String name, Date birthdate) {
        this.authorId = authorId;
        this.name = name;
        this.birthdate = birthdate;
    }

    public Author(String name, Date birthdate) {
        this(-1, name, birthdate);
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getName() {
        return name;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    @Override
    public String toString() {
        return name;
    }
}

