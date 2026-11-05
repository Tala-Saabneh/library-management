package library;

import library.model.Book;
import library.model.EBook;
import library.model.Librarian;
import library.model.Student;
import library.model.User;
import library.service.BackupTask;
import library.service.Library;
import library.util.LibraryException;
import library.util.UserFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    private static final Path DATA_FOLDER = Paths.get("data");
    private static final Path BACKUP_FOLDER = Paths.get("backup");
    private static final long BACKUP_EVERY_MILLIS = 30_000;

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("--demo")) {
        ConcurrencyDemo.run();
             return;
        }

        Library library =new Library();
        if (library.loadFrom(DATA_FOLDER)) {
            System.out.println("Loaded saved data from folder '" + DATA_FOLDER + "'.");
        } 
        else 
        {
            addSampleData(library);
            System.out.println("No saved data found, created sample data.");
        }
        BackupTask backup =new BackupTask(library, BACKUP_FOLDER, BACKUP_EVERY_MILLIS);
        backup.start();
        Scanner scanner =new Scanner(System.in);
        try {
            User currentUser = login(library, scanner);
            runMenu(library, scanner, currentUser);
        } catch (NoSuchElementException e) {
            System.out.println("\nInput ended.");
        }

        backup.stop();
        library.saveTo(DATA_FOLDER);
        System.out.println("Data saved. Goodbye!");
    }

////////////////////////login

    private static User login(Library library, Scanner scanner) {
        System.out.println("\nRegistered users:");
        library.getAllUsers().forEach(u -> System.out.println("  " + u));

        while (true) {
            System.out.print("\nEnter your user id: ");
            String id =scanner.nextLine().trim();
            User user =library.findUser(id).orElse(null);
            if (user != null) {
                   System.out.println("Welcome, " + user.getName() + " (" + user.getRole() + ")");
         return user;
            }
            System.out.println("no user with this id, try again");
        }
    }

/////////////// menu  

    private static void runMenu(Library library, Scanner scanner, User user) throws Exception {
             boolean running = true;
        while (running) {
            printMenu(user);
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                            printBooks(library.listAvailableBooks());
                        break;
                    case "2":
                          printBooks(library.getAllBooks());
                        break;
                    case "3":
                              System.out.print("Title contains: ");
                          printBooks(library.searchByTitle(scanner.nextLine().trim()));
                           break;
                    case "4":
                         System.out.print("Author contains: ");
                        printBooks(library.searchByAuthor(scanner.nextLine().trim()));
                                break;
                    case "5":
                        library.borrowBook(user.getId(), readInt(scanner, "Book id to borrow: "));
                              System.out.println("Done. Enjoy your book!");
                        break;
                    case "6":
                           library.returnBook(user.getId(), readInt(scanner, "Book id to return: "));
                        System.out.println("Done. Thank you!");
                           break;
                    case "7":
                        printBooks(library.getBooksBorrowedBy(user.getId()));
                              break;
                    case "8":
                           addBook(library, scanner, user);
                        break;
                    case "9":
                               addUser(library, scanner, user);
                        break;
                    case "10":
                        System.out.println(library.adminReport(user));
                              break;
                    case "11":
                        ConcurrencyDemo.run();
                             break;
                    case "12":
                        library.saveTo(DATA_FOLDER);
                               System.out.println("Saved.");
                        break;
                    case "0":
                        running = false;
                                 break;
                    default:
                          System.out.println("Please choose a number from the menu.");
                }
            } catch (LibraryException e) 
            {
                System.out.println("error: " + e.getMessage());
            } catch (IOException e) 
            {
                System.out.println("could not save: " + e.getMessage());
            }
        }
    }
/////////////////////////////
    private static void printMenu(User user) {
         System.out.println("\n---------- LIBRARY MENU (" + user.getName() + ") ----------");
        System.out.println(" 1  List available books");
         System.out.println(" 2  List all books");
         System.out.println(" 3  Search by title");
        System.out.println(" 4  Search by author");
        System.out.println(" 5  Borrow a book");
          System.out.println(" 6  Return a book");
        System.out.println(" 7  My borrowed books");
        if (user.canManageBooks()) {
               System.out.println(" 8  Add a book            (librarian)");
            System.out.println(" 9  Add a user            (librarian)");
            System.out.println("10  Admin report          (librarian)");
        }
        System.out.println("11  Simulate concurrent users");
        System.out.println("12  Save now");
            System.out.println(" 0  Save and exit");
        System.out.print("Your choice: ");
    }

//////////////////////////////////////////
    private static void addBook(Library library, Scanner scanner, User user) {
        System.out.print("Is it an e-book? y/n: ");
           boolean isEBook = scanner.nextLine().trim().equalsIgnoreCase("y");
        System.out.print("Title: ");
         String title = scanner.nextLine().trim();
         System.out.print("Author: ");
        String author = scanner.nextLine().trim();

         int id = library.nextBookId();
        Book book;
        if (isEBook) 
            {
                double size = readInt(scanner, "File size in MB: ");
            book = new EBook(id, title, author, size);
        } 
        else 
            {
            book = new Book(id, title, author);
        }
        library.addBook(user, book);
            System.out.println("Added: " + book);
    }
//////////////////////////////
    private static void addUser(Library library, Scanner scanner, User user) {

        System.out.print("Role student or Librarian: ");
            String role= scanner.nextLine().trim();
         System.out.print("New user id: ");
             String id= scanner.nextLine().trim();
            System.out.print("name: ");
        String name =scanner.nextLine().trim();

        User newUser = UserFactory.create(role, id, name); // role class is found by reflection
        library.addUser(user, newUser);
        System.out.println("Added: " + newUser);
    }
///////////////////////////////////
    private static void printBooks(List<Book> books) {
        if (books.isEmpty()) 
        {
        System.out.println("(no books)");
        } 
        else 
        {
            books.forEach(System.out::println);
        }
    }
////////////////////////////////////////
    private static int readInt(Scanner scanner, String message) {
        System.out.print(message);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new LibraryException("That is not a number");
        }
    }

//////////////////////////////////////////

    private static void addSampleData(Library library) {
        Librarian librarian = new Librarian("L1", "Huda");
        library.registerUser(librarian);
        library.registerUser(new Student("S1", "Ali"));
        library.registerUser(new Student("S2", "Sara"));
        library.addBook(librarian, new Book(1, "Clean Code", "Robert Martin"));
        library.addBook(librarian, new Book(2, "Effective Java", "Joshua Bloch"));
        library.addBook(librarian, new Book(3, "Head First Java", "Kathy Sierra"));
        library.addBook(librarian, new Book(4, "Refactoring", "Martin Fowler"));
        library.addBook(librarian, new EBook(5, "Java Concurrency in Practice", "Brian Goetz", 12.5));
        library.addBook(librarian, new EBook(6, "Design Patterns", "Erich Gamma", 8.0));
    }
}
