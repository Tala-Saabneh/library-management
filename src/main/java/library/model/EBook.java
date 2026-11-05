package library.model;

/** A digital book. Same as Book but it also has a file size. */
public class EBook extends Book {

    private final double fileSizeMb;

    public EBook(int id, String title, String author, double fileSizeMb) {
        super(id, title, author);
        this.fileSizeMb = fileSizeMb;
    }

    public double getFileSizeMb() {
        return fileSizeMb;
    }

    @Override
    public String describe() {
        return "#" + getId() + " \"" + getTitle() + "\" by " + getAuthor()
                + " (E-Book, " + fileSizeMb + " MB)" + status();
    }
}
