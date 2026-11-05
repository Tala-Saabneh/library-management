package library.model;

/**
 * Base class for everyone who uses the library.
 * Subclasses decide how many books they can borrow and what they are allowed to do.
 */
public abstract class User {

    private final String id;
    private final String name;

    protected User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** How many books this user may hold at the same time. */
    public abstract int getMaxBorrowLimit();

    /** True if the user can add books / users and see the admin report. */
    public abstract boolean canManageBooks();

    /** Role name, used when saving to disk. */
    public String getRole() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getRole() + " " + name + " (" + id + ")";
    }
}
