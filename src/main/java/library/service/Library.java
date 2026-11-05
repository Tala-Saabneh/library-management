package library.service;
import library.model.Book;
import library.model.User;
import library.util.LibraryException;
import library.util.Log;
import library.util.ReportGenerator;
import library.util.Repository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Library {

    private static final String BOOKS_FILE = "books.txt";
    private static final String USERS_FILE = "users.txt";
    private final Repository<Book> books = new Repository<>();
    private final Repository<User> users = new Repository<>();

////////////users 
    public synchronized void registerUser(User user) {
        if (findUser(user.getId()).isPresent()) {
        throw new LibraryException("user id already exists: " + user.getId());}
               users.add(user);
    }
/////////
    public synchronized void addUser(User actor, User newUser) {
        requireManager(actor, "add users");
             registerUser(newUser);
        Log.debug(actor.getName() + " added user " + newUser);
    }
///////////////
    public synchronized Optional<User> findUser(String id) {
          return users.findFirst(u -> u.getId().equals(id));
    }
///////////////
    public synchronized List<User> getAllUsers() {
       return users.getAll();
    }
////////////////////////
    ///////////////////books

  public synchronized void addBook(User actor, Book book) {
             requireManager(actor, "add books");
        if (findBook(book.getId()).isPresent()) {
         throw new LibraryException("Book id already exists: " + book.getId());
        }
           books.add(book);
    Log.debug(actor.getName() + " added book " + book);
    }
////////////////
 public synchronized Optional<Book> findBook(int id) {
        return books.findFirst(b -> b.getId() == id);
    }
/////////////////
    public synchronized List<Book> getAllBooks() {
             return books.getAll();
    }
///////////////////
    
        public synchronized int nextBookId() {
    return books.getAll().stream().mapToInt(Book::getId).max().orElse(0) + 1;
    }
///////////////////
   public synchronized List<Book> listAvailableBooks() {
                    return books.filter(Book::isAvailable);
    }
////////////////////
    public synchronized List<Book> searchByTitle(String text) {
       String low = text.toLowerCase();
    return books.filter(b -> b.getTitle().toLowerCase().contains(low));
    }
///////////////////
    public synchronized List<Book> searchByAuthor(String text) {
     String lowname = text.toLowerCase();
             return books.filter(b -> b.getAuthor().toLowerCase().contains(lowname));
    }
////////////////////////////////////////
    public synchronized List<Book> getBooksBorrowedBy(String userId) {
     return books.getAll().stream().filter(b -> userId.equals(b.getBorrowedBy())).collect(Collectors.toList());
    }
///////////////////////////////////
    //borrow and return 

    public synchronized void borrowBook(String userId, int bookId) {
    User user =findUser(userId).orElseThrow(() -> new LibraryException("User not found: " + userId));
        Book book =findBook(bookId).orElseThrow( () -> new LibraryException(" book not found: " + bookId));
           if (!book.isAvailable()) {
          throw new LibraryException("book #" + bookId + " is already borrowed");
        }

           int alreadyBorrowed =getBooksBorrowedBy(userId).size();
           
        Log.debug(user.getName() + " has " + alreadyBorrowed + " books, limit is " + user.getMaxBorrowLimit());
        if (alreadyBorrowed >= user.getMaxBorrowLimit()) {
            throw new LibraryException(user.getName() + " reached the  limit ("+ user.getMaxBorrowLimit() + ")");
        }
             book.setBorrowedBy(userId);
        Log.debug(user.getName() + " borrowed book #" + bookId);
    }
////////////////////////////////////
    public synchronized void returnBook(String userId, int bookId) {
        Book book= findBook(bookId).orElseThrow(() -> new LibraryException("Book not found: " + bookId));
        if (book.isAvailable()) {
     throw new LibraryException("Book #" + bookId + " is not borrowed");
        }
            if (!userId.equals(book.getBorrowedBy())) {
            throw new LibraryException("Book #" + bookId + " was borrowed by someone else");}
          book.setBorrowedBy(null);
        Log.debug(userId + " returned book #" + bookId);
    }
//////////////////////////
//////////report

    public synchronized String adminReport(User actor) {
    requireManager(actor, "see the admin report");
        return ReportGenerator.generate(books.getAll(), users.getAll());
    }
/////////////////////////////
////////save load

    public synchronized void saveTo(Path folder) throws IOException {
           Files.createDirectories(folder);
        FileStorage.saveBooks(books.getAll(),folder.resolve(BOOKS_FILE));
         FileStorage.saveUsers(users.getAll(),folder.resolve(USERS_FILE));
    }
////////////////////////////////////////////////////////
   
    public synchronized boolean loadFrom(Path folder) throws IOException {
             Path booksF=folder.resolve(BOOKS_FILE);
        Path usersF=folder.resolve(USERS_FILE);
        if (!Files.exists(booksF) || !Files.exists(usersF)) {
            return false; }
         books.clear();
        users.clear();
        FileStorage.loadUsers(usersF).forEach(users::add);
            FileStorage.loadBooks(booksF).forEach(books::add);
        return true;
    }
//////////////////////////////////
    private void requireManager(User actor, String action) {
        if (actor ==null || !actor.canManageBooks()) {
        throw new LibraryException("0nly a librarian can " + action);}
    }
}
