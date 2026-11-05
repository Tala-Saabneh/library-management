package library;

import library.model.Book;
import library.model.EBook;
import library.model.Librarian;
import library.model.Student;
import library.model.User;
import library.service.BackupTask;
import library.service.Library;
import library.util.LibraryException;
import library.util.ReportGenerator;
import library.util.UserFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StorageAndReflectionTest {

    @Rule
    public TemporaryFolder tempFolder=new TemporaryFolder();

    private Library createLibrary() {
          Library library= new Library();
        Librarian librarian = new Librarian("L1", "Huda");

             library.registerUser(librarian);
        library.registerUser(new Student("S1", "Ali"));

            library.addBook(librarian, new Book(1, "Clean Code", "Robert Martin"));
        library.addBook(librarian, new EBook(2, "Effective Java", "Joshua Bloch", 7.5));

            library.borrowBook("S1", 1);
        return library;
    }

    ////////////////////////////

    @Test
    public void testSaveAndLoad() throws IOException {

        Path folder=tempFolder.newFolder("data").toPath();
        createLibrary().saveTo(folder);

                    Library loaded= new Library();
         assertTrue(loaded.loadFrom(folder));

            assertEquals(2,loaded.getAllBooks().size());
        assertEquals(2,loaded.getAllUsers().size());

         assertTrue(loaded.findUser("S1").get() instanceof Student);

        assertTrue(loaded.findUser("L1").get() instanceof Librarian);

            assertTrue(loaded.findBook(2).get() instanceof EBook);

         assertEquals(7.5, ((EBook) loaded.findBook(2).get()).getFileSizeMb(), 0.001);
    }
//////////////////////////////////////////////
    @Test
    public void testBorrowedAfterLoad() throws IOException {
        Path folder = tempFolder.newFolder("data").toPath();
            createLibrary().saveTo(folder);

        Library loaded = new Library();
        loaded.loadFrom(folder);
         assertEquals("S1", loaded.findBook(1).get().getBorrowedBy());
             assertTrue(loaded.findBook(2).get().isAvailable());}
///////////////////////////////////////////////
    @Test
    public void testLoadNoFiles() throws IOException {
        Path emptyFolder = tempFolder.newFolder("empty").toPath();
            assertFalse(new Library().loadFrom(emptyFolder));}
///////////////////////reflectio
    @Test
    public void testCreateUser() {
        User student = UserFactory.create("Student", "S5", "Omar");
            User librarian = UserFactory.create("Librarian", "L5", "Dana");

        assertTrue(student instanceof Student);
            assertTrue(librarian instanceof Librarian);
        assertEquals("Omar", student.getName());
    }
//////////////////
    @Test(expected = LibraryException.class)
    public void testUnknownRole() {
    UserFactory.create("Teacher", "T1", "Someone");}
////////////////////////
    @Test(expected = LibraryException.class)
    public void testNotUserClass() {
     UserFactory.create("Book", "B1", "Not a user");}
//////////////////////////////////
    @Test
    public void testAdminReport() {
        Library library = createLibrary();
            String report = library.adminReport(library.findUser("L1").get());

        assertTrue(report.contains("Total books : 2"));
            assertTrue(report.contains("Borrowed    : 1"));
        assertTrue(report.contains("Total users : 2"));
        /////reflection
         assertTrue(report.contains("borrowedBy=S1"));
        assertTrue(report.contains("fileSizeMb=7.5"));
         assertTrue(report.contains("Student{"));
    }
////////////////////////
    @Test
    public void testDescribeObject() {
         String text = ReportGenerator.describeObject(new Student("S9", "Lina"));

        assertTrue(text.contains("id=S9"));
         assertTrue(text.contains("name=Lina"));
    }

/////////////////////////////////backup
    @Test
    public void testBackupThread() throws Exception {
        Path backupFolder = tempFolder.newFolder("backup").toPath();
         BackupTask task = new BackupTask(createLibrary(), backupFolder, 50);

        task.start();
            long deadline = System.currentTimeMillis() + 3000;
        while (task.getBackupCount() == 0 && System.currentTimeMillis() < deadline) {
                Thread.sleep(20);
        }
        task.stop();
        assertTrue(task.getBackupCount() >= 1);
            assertTrue(Files.exists(backupFolder.resolve("books.txt")));
        assertTrue(Files.exists(backupFolder.resolve("users.txt")));
    }
///////////////////////////////////
    @Test
    public void testBackupNow() throws IOException {
        Path backupFolder =tempFolder.newFolder("backup2").toPath();
            BackupTask task= new BackupTask(createLibrary(),backupFolder, 60_000);
        task.backupNow();
            assertEquals(1,task.getBackupCount());
        assertTrue(Files.exists(backupFolder.resolve("books.txt")));
    }
}
