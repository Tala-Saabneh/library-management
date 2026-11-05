package library.util;
import library.model.User;
import java.lang.reflect.Constructor;
public class UserFactory {

    private static final String MODEL_PACKAGE ="library.model.";

    private UserFactory() {
    }

 public static User create(String role, String id, String name) {
        try {
                Class<?> cls = Class.forName(MODEL_PACKAGE + role);
         if (!User.class.isAssignableFrom(cls))
            {
                throw new LibraryException("Not a user role: " + role);
            }
              Constructor<?> constructor = cls.getConstructor(String.class, String.class);
            return (User) constructor.newInstance(id, name);
        } 
        catch (ReflectiveOperationException e) {
            throw new LibraryException("Unknown user role: " + role);
        }
    }
}
