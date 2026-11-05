package library;

import library.model.Book;
import library.model.Librarian;
import library.model.Student;
import library.service.Library;
import library.util.LibraryException;
import library.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;


public class ConcurrencyDemo {

    private ConcurrencyDemo() {
    }

    public static void run() throws InterruptedException {
        raceForOneBook();
         System.out.println();
        everyoneBorrowsEverything();
    }

    /////////10 students try to borrow the same book at the same moment
    private static void raceForOneBook() throws InterruptedException {
        System.out.println("=== Demo 1: 10 students race for ONE book ===");

            Library library =new Library();
        Librarian boss =new Librarian("L1", "Librarian");

        library.registerUser(boss);
            library.addBook(boss, new Book(1, "Clean Code", "Robert Martin"));

        AtomicInteger winners =new AtomicInteger();
            CountDownLatch startSignal =new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();

        for (int i = 1;i<= 10; i++) 
        {
            String id = "S" + i;

            library.registerUser(new Student(id, "Student" + i));
            Thread t = new Thread(() -> {
                try {
                       startSignal.await(); //all threads wait here, ten start together
                    library.borrowBook(id, 1);
                        winners.incrementAndGet();
                    Log.info(id + " got the book");
                } 
                catch (LibraryException e)
                {
                Log.info(id + " failed: " + e.getMessage());
                } catch (InterruptedException e) 
                {
                Thread.currentThread().interrupt();
                }
            }, "student-" + i);

            threads.add(t);
            t.start();
        }

        startSignal.countDown();
        for (Thread t : threads) {
            t.join();
        }
        System.out.println("Students who got the book: " + winners.get() + " (should be 1)");
    }
//////////////////////////////////////
////////5 students each try to borrow all 5 book then return what they got
    private static void everyoneBorrowsEverything() throws InterruptedException {
            System.out.println("=== Demo 2: 5 students try to borrow all 5 books (limit is 3 each) ===");

            Library library =new Library();
        Librarian boss =new Librarian("L1", "Librarian");
     library.registerUser(boss);
        for (int b = 1; b <= 5; b++) {
            library.addBook(boss, new Book(b, "Book " + b, "Author " + b));
        }

     AtomicInteger borrowed =new AtomicInteger();
         AtomicInteger rejected =new AtomicInteger();
             List<Thread> threads =new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
              String id ="S" + i;
                 library.registerUser(new Student(id, "Student" + i));
 Thread t =new Thread(() -> {for (int bookId = 1; bookId <= 5; bookId++) {
                    try {
                    library.borrowBook(id, bookId);
                        borrowed.incrementAndGet();
                     } catch (LibraryException e) {
                     rejected.incrementAndGet();
                    }
                }
                int holding =library.getBooksBorrowedBy(id).size();
                  Log.info(id + " is holding " + holding + " book(s)");
                
                for (Book book : library.getBooksBorrowedBy(id)) {
                library.returnBook(id, book.getId());
                }
            }, "student-" + i);

            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
        t.join();
        }
             System.out.println("Successful borrows: " + borrowed.get() + ", rejected: " + rejected.get());
        System.out.println("Books available at the end: " + library.listAvailableBooks().size() + " of 5");
    }
}
