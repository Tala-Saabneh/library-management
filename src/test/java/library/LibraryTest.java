package library;

import library.model.Book;
import library.model.EBook;
import library.model.Librarian;
import library.model.Student;
import library.service.Library;
import library.util.LibraryException;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LibraryTest {

    private Library library;
    private Librarian librarian;
    private Student ali;
    private Student sara;

    @Before
    public void setUp() {
        library =new Library();
          librarian =new Librarian("L1", "Huda");
        ali= new Student("S1", "Ali");
        sara= new Student("S2", "Sara");
             library.registerUser(librarian);
        library.registerUser(ali);
         library.registerUser(sara);

        library.addBook(librarian, new Book(1, "Clean Code", "Robert Martin"));
         library.addBook(librarian, new Book(2, "Effective Java", "Joshua Bloch"));
         library.addBook(librarian, new Book(3, "Head First Java", "Kathy Sierra"));
        library.addBook(librarian, new Book(4, "Refactoring", "Martin Fowler"));
        library.addBook(librarian, new EBook(5, "Java Concurrency", "Brian Goetz", 12.5));
    }

/////// borrowing 

    @Test
    public void borrowMakesBookUnavailable() {
        library.borrowBook("S1", 1);

          assertFalse(library.findBook(1).get().isAvailable());
        assertEquals("S1", library.findBook(1).get().getBorrowedBy());
           assertEquals(4, library.listAvailableBooks().size());
    }
/////////////////////////////
    @Test
    public void cannotBorrowABookThatIsAlreadyBorrowed() {
          library.borrowBook("S1", 1);
        try 
        {
            library.borrowBook("S2", 1);
            fail("second borrow should fail");
        } 
        catch (LibraryException expected) {
        assertTrue(expected.getMessage().contains("already borrowed"));
        }
    }
/////////////////////////////////
    @Test
    public void studentCanBorrowUpToThreeBooks() {
        library.borrowBook("S1", 1);
        library.borrowBook("S1", 2);
            library.borrowBook("S1", 3);

            assertEquals(3, library.getBooksBorrowedBy("S1").size());
        
    }
    
//////////////////////////////////
    @Test
    public void studentCannotBorrowAFourthBook() {
         library.borrowBook("S1", 1);
        library.borrowBook("S1", 2);
         library.borrowBook("S1", 3);
        try 
        {
            library.borrowBook("S1", 4);
        fail("limit should stop the 4th book");
        } 
        catch (LibraryException expected) {
        assertTrue(expected.getMessage().contains("limit"));
        }
            assertTrue(library.findBook(4).get().isAvailable());
    }
////////////////////////////////////
    @Test
    public void librarianHasAHigherLimitThanAStudent() {
        for (int i = 1; i <= 4; i++)
        {
         library.borrowBook("L1", i);
        }
        assertEquals(4, library.getBooksBorrowedBy("L1").size());
    }
///////////////////////////////////////////////
    @Test(expected = LibraryException.class)
    public void borrowingUnknownBookFails() {
        library.borrowBook("S1", 99);
    }
///////////////////////////////////////////////
    @Test(expected = LibraryException.class)
    public void borrowingWithUnknownUserFails() {
        library.borrowBook("X9", 1);
    }
//////////////////////////////
////////////////////////returning
    @Test
    public void returnMakesBookFree() {
        library.borrowBook("S1", 1);
            library.returnBook("S1", 1);

            assertTrue(library.findBook(1).get().isAvailable());
    }
//////////////////////////////////////
    @Test
    public void returnFreesSlot() {
        library.borrowBook("S1", 1);
            library.borrowBook("S1", 2);
            library.borrowBook("S1", 3);
            library.returnBook("S1", 2);

        library.borrowBook("S1", 4);
 assertEquals(3, library.getBooksBorrowedBy("S1").size());
    }
//////////////////////////////////////////////////////
    @Test(expected = LibraryException.class)
    public void cannotReturnABookThatWasNotBorrowed() {
        library.returnBook("S1", 1);
    }
////////////////////////////////////
    @Test
    public void returnByOtherUserFails() {
        library.borrowBook("S1", 1);
        try 
        {
         library.returnBook("S2", 1);
            fail("S2 does not have this book");
        } catch (LibraryException expected) {
            assertTrue(expected.getMessage().contains("someone else"));
        }
        assertFalse(library.findBook(1).get().isAvailable());
    }
////////////////////////////////
///////////////////////// roles

    @Test
    public void studentCannotAddBooks() {
        try 
        {
            library.addBook(ali, new Book(10, "Secret", "Nobody"));
            fail("students must not add books");
        } 
            catch (LibraryException expected) {
        assertTrue(expected.getMessage().contains("librarian"));
        }
        assertFalse(library.findBook(10).isPresent());
    }
////////////////////////////////////////////////
    @Test
    public void studentCannotAddUsers() {
        try 
        {
            library.addUser(ali, new Student("S3", "Omar"));
            fail("students must not add users");
        } catch (LibraryException expected) {
            assertFalse(library.findUser("S3").isPresent());
        }
    }
/////////////////////////////////
    @Test
    public void librarianCanAddBooksAndUsers() {
        library.addBook(librarian, new Book(10, "New Book", "New Author"));
          library.addUser(librarian, new Student("S3", "Omar"));
          assertTrue(library.findBook(10).isPresent());
         assertTrue(library.findUser("S3").isPresent());
    }
/////////////////////////////////////
    @Test(expected = LibraryException.class)
    public void studentCannotSeeAdminReport() {
        library.adminReport(ali);


    }
///////////////////////////////////
    @Test(expected = LibraryException.class)
    public void duplicateBookIdIsRejected() {

        library.addBook(librarian, new Book(1, "Copy", "Someone"));
    }
///////////////////////////////////
    /////search
    @Test
    public void searchByTitleIgnoresCase() {
        List<Book> result = library.searchByTitle("java");

        assertEquals(3, result.size()); }
/////////////////////////////////////////////////
    @Test
    public void searchByAuthorFindsBooks() {
        List<Book> result = library.searchByAuthor("martin");
        assertEquals(2, result.size());
    }
//////////////////////////////////////////////////
 ////////threads
    @Test
    public void oneWinnerPerBook() throws InterruptedException {
        int numberOfUsers = 20;
        for (int i = 1; i <= numberOfUsers; i++) {
            library.registerUser(new Student("T" + i, "Tester" + i));
        }

          AtomicInteger winners =new AtomicInteger();
         CountDownLatch start =new CountDownLatch(1);
    List<Thread> threads =new ArrayList<>();

        for (int i = 1; i <= numberOfUsers; i++)
            {
            String id ="T" + i;
            Thread t =new Thread(() -> {
                try {
                    start.await();
                      library.borrowBook(id, 1);
                     winners.incrementAndGet();
                } 
                catch (LibraryException e) {
                    
                } catch (InterruptedException e) 
                {
                    Thread.currentThread().interrupt();
                }
            });

            threads.add(t);
            t.start();
        }

        start.countDown();
        for (Thread t : threads) {
         t.join();
        }

         assertEquals(1, winners.get());
    }
////////////////////////////////////
    @Test
    public void studentLimitWithManyThreads() throws InterruptedException {
        List<Thread> threads= new ArrayList<>();

        for (int bookId = 1;bookId <= 5;bookId++) 
            {
            int id = bookId;
            Thread t = new Thread(() -> {
                try 
                {
                    library.borrowBook("S1", id);
                } 
                catch (LibraryException e) {
                    // rejected because of the limit
                }
            });
            threads.add(t);
            t.start();

        }

        for (Thread t : threads) 
            {
            t.join();
        }

    assertEquals(3, library.getBooksBorrowedBy("S1").size());
    }
}
