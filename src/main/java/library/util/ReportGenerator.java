package library.util;

import library.model.Book;
import library.model.User;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.List;

public class ReportGenerator {

    private ReportGenerator() {
    }

    public static String generate(List<Book> books, List<User> users) {
            StringBuilder sb = new StringBuilder();
        long available = books.stream().filter(Book::isAvailable).count();

        sb.append("ADMIN REPORt\n");
            sb.append("Generated at: ").append(LocalDateTime.now()).append("\n");
         sb.append("total books : ").append(books.size()).append("\n");
          sb.append("Available   : ").append(available).append("\n");
        sb.append("borrowed    : ").append(books.size() - available).append("\n");
        sb.append("Total users : ").append(users.size()).append("\n\n");
        sb.append("--- Users (fields read by reflection) ---\n");
        for (User user : users)
            {
            sb.append(describeObject(user)).append("\n");
        }

        sb.append("\n--- Books (fields read by reflection) ---\n");
        for (Book book : books)
            {
            sb.append(describeObject(book)).append("\n");
        }

        sb.append("\n Class structure -\n");
         sb.append(describeClass(User.class));
         sb.append(describeClass(Book.class));
        sb.append("==================================\n");
        return sb.toString();
    }

    /////////////////////
    public static String describeObject(Object obj) {
        StringBuilder sb = new StringBuilder(obj.getClass().getSimpleName()).append("{");
        boolean first =true;
        
        Class<?> cls = obj.getClass();
        while (cls != null && cls != Object.class) 
        {
            for (Field field : cls.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) 
            {  continue;
                }
                field.setAccessible(true);
                try {
                    if (!first) 
                    {
                        sb.append(", ");
                    }
                    sb.append(field.getName()).append("=").append(field.get(obj));
                    first = false;
                } catch (IllegalAccessException e) 
                {
                    sb.append(field.getName()).append("=?");
                }
            }
            
            cls = cls.getSuperclass();
        }
        return sb.append("}").toString();
    }

    /** Lists the fields and the number of methods of a class. */
    public static String describeClass(Class<?> cls) {
        StringBuilder sb =new StringBuilder(cls.getSimpleName()).append(": fields [");
         Field[] fields =cls.getDeclaredFields();
        for (int i = 0; i < fields.length; i++)
            {
            sb.append(fields[i].getType().getSimpleName()).append(" ").append(fields[i].getName());
            if (i < fields.length - 1)
            {
                sb.append(", ");
            }
        }
        sb.append("], methods: ").append(cls.getDeclaredMethods().length).append("\n");
        return sb.toString();
    }
}
