package library.model;

/** A student can borrow only a few books and cannot manage the library. */
public class Student extends User {

    public static final int MAX_BOOKS = 3;

    public Student(String id, String name) {
        super(id, name);
    }

    @Override
    public int getMaxBorrowLimit() {
        return MAX_BOOKS;
    }

    @Override
    public boolean canManageBooks() {
        return false;
    }
}
