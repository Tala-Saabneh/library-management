package library;

import library.model.Book;
import library.model.User;
import library.service.Library;
import library.util.LibraryException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LibraryMockitoTest {

    private Library library;
    private User fakeReader;  

    private User fakeManager;   
////////////fack one
    @Before
    public void setUp() {
        library =new Library();

            fakeReader = mock(User.class);
         when(fakeReader.getId()).thenReturn("R1");
         when(fakeReader.getName()).thenReturn("Reader");
        when(fakeReader.getMaxBorrowLimit()).thenReturn(1);
    when(fakeReader.canManageBooks()).thenReturn(false);

        fakeManager = mock(User.class);
         when(fakeManager.getId()).thenReturn("M1");
        when(fakeManager.getName()).thenReturn("Manager");
           when(fakeManager.getMaxBorrowLimit()).thenReturn(5);
        when(fakeManager.canManageBooks()).thenReturn(true);

        library.registerUser(fakeReader);
          library.registerUser(fakeManager);
        library.addBook(fakeManager, new Book(1, "Book One", "Author A"));
        library.addBook(fakeManager, new Book(2, "Book Two", "Author B"));
    }
//////////
    @Test
    public void libraryAsksTheUserForItsBorrowLimit() {
        library.borrowBook("R1", 1);

        try {
            library.borrowBook("R1", 2);
              fail("the mock user has a limit of 1");
        } 
        catch (LibraryException expected) {
             assertTrue(expected.getMessage().contains("limit"));
        }

        verify(fakeReader, atLeastOnce()).getMaxBorrowLimit();
            assertEquals(1, library.getBooksBorrowedBy("R1").size());
    }
//////////////////////
    @Test
    public void libraryChecksPermissionBeforeAddingABook() {
        try 
        {
                library.addBook(fakeReader, new Book(3, "Hidden", "Nobody"));
            fail("a user who cannot manage books must be rejected");
        } 
        catch (LibraryException expected) {
            
        }

        verify(fakeReader).canManageBooks();
            assertFalse(library.findBook(3).isPresent());
    }
////////////////////////
    @Test
    public void managerMockCanAddABook() {
             library.addBook(fakeManager, new Book(3, "Allowed", "Somebody"));
assertTrue(library.findBook(3).isPresent());
    }
}
