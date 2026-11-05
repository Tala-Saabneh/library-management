package library.service;
import library.model.Book;
import library.model.EBook;
import library.model.User;
import library.util.UserFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileStorage {
    private static final String SEPARATOR = "|";
    private FileStorage() {
    }
////////to save book in file
 public static void saveBooks(List<Book> books, Path file) throws IOException {
        List<String> lines =new ArrayList<>();
    for (Book book : books) {
        String borrowedBy =book.getBorrowedBy() == null ? "" : book.getBorrowedBy();
            String line = (book instanceof EBook ? "EBOOK" : "BOOK") + SEPARATOR + book.getId() + SEPARATOR + clean(book.getTitle()) + SEPARATOR + clean(book.getAuthor())+ SEPARATOR + borrowedBy;
              if (book instanceof EBook) {
            line += SEPARATOR + ((EBook) book).getFileSizeMb();
            }
                   lines.add(line);
        }
 Files.write(file, lines, StandardCharsets.UTF_8);
    }
/////
    public static List<Book> loadBooks(Path file) throws IOException {
 List<Book> books = new ArrayList<>();
              for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
               if (line.trim().isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\|", -1); 
            int id = Integer.parseInt(parts[1]);
            Book book;
            if (parts[0].equals("EBOOK")) {
             book = new EBook(id, parts[2], parts[3], Double.parseDouble(parts[5]));
            } else {
             book = new Book(id, parts[2], parts[3]);
            }
             if (!parts[4].isEmpty()) {
            book.setBorrowedBy(parts[4]);
            }
            books.add(book);
        }
     return books;
    }
////////////
    public static void saveUsers(List<User> users, Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(user.getRole() + SEPARATOR + clean(user.getId()) + SEPARATOR + clean(user.getName()));
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
/////////
    public static List<User> loadUsers(Path file) throws IOException {
        List<User> users = new ArrayList<>();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\|", -1);
            // the role class is created by reflection
            users.add(UserFactory.create(parts[0], parts[1], parts[2]));
        }
        return users;
    }
//////////
    private static String clean(String text) {
        return text.replace("|", "/");
    }
}
