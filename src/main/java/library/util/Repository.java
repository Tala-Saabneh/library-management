package library.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Repository<T> {

    private final List<T> items = new ArrayList<>();

    public void add(T item)
    {
        items.add(item);
    }

    public int size() {
    return items.size();
    }

    public void clear() 
    {
        items.clear();  }

    public List<T> getAll() {
     return new ArrayList<>(items);
    }

    public Optional<T> findFirst(Predicate<T> condition) {
        return items.stream().filter(condition).findFirst();
    }

    public List<T> filter(Predicate<T> condition) {
    return items.stream().filter(condition).collect(Collectors.toList());
    }
}
