package library.model;

/** A librarian can add books and users, and can borrow more books than a student. */
public class Librarian extends User {

    public static final int MAX_BOOKS = 10;

    public Librarian(String id, String name) {
        super(id, name);
    }

    @Override
    public int getMaxBorrowLimit() {
        return MAX_BOOKS;
    }

    @Override
    public boolean canManageBooks() {
        return true;
    }
}
