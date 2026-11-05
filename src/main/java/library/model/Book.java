package library.model;
public class Book {
    private final int id;
    private final String title;
    private final String author;
    private String borrowedBy; // user id, or null when the book is available
    public Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
    }
@Override
    public String toString() {
        return describe();
    }
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getBorrowedBy() {
        return borrowedBy;
    }

    public void setBorrowedBy(String userId) {
        this.borrowedBy = userId;
    }

    public boolean isAvailable() {
        return borrowedBy == null;
    }

    public String describe() {
        return "#" + id + " \"" + title + "\" by " + author + status();
    }

    protected String status() {
        return isAvailable() ? " [available]" : " [borrowed by " + borrowedBy + "]";
    }

    
}
