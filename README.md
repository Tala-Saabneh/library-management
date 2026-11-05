# Library Management System

A console application (Java 11+) to manage a digital library: add, list, search, borrow and return books,
with two kinds of users (students and librarians) that have different permissions.

## Project structure

```
library-management/
├── pom.xml                  Maven file (dependencies + JAR settings)
├── build.sh / build.bat     compile + create the JAR with plain javac / jar
├── library-management.jar   ready-to-run JAR
└── src
    ├── main/java/library
    │   ├── Main.java               console menu
    │   ├── ConcurrencyDemo.java    many threads using the library
    │   ├── model/                  User, Student, Librarian, Book, EBook
    │   ├── service/                Library, FileStorage, BackupTask
    │   └── util/                   Repository<T>, UserFactory, ReportGenerator, Log, LibraryException
    └── test/java/library
        ├── LibraryTest.java                borrow/return rules, limits, roles, search, threads
        ├── StorageAndReflectionTest.java   save/load, reflection, backup thread
        └── LibraryMockitoTest.java         tests with Mockito mocks
```

## How to run

### 1. From the command line (javac + jar)

```
./build.sh                          (Windows: build.bat)
java -jar library-management.jar
```

Or step by step, to see what happens:

```
javac -d out $(find src/main -name "*.java")      # .java  ->  bytecode (.class)
java -cp out library.Main                         # JVM runs the bytecode
jar cfe library-management.jar library.Main -C out .   # bundle into a JAR
java -jar library-management.jar
```

Only the threads demo: `java -jar library-management.jar --demo`

Sample users when you start the first time: `L1` (librarian), `S1` and `S2` (students).
Data is saved in the folder `data/` and a background thread copies it to `backup/` every 30 seconds.

### 2. With Maven (needs internet the first time)

```
mvn test        # run all unit tests
mvn package     # tests + creates target/library-management.jar
```

## Where each concept is used

| Concept | Where |
|---|---|
| OOP: encapsulation | private fields + getters in `Book`, `User` |
| OOP: inheritance | `Student`, `Librarian` extend `User`; `EBook` extends `Book` |
| OOP: polymorphism | `getMaxBorrowLimit()`, `canManageBooks()` and `describe()` are overridden in the subclasses |
| JDK / JRE / JVM, bytecode | `build.sh`: `javac` makes bytecode, `jar` bundles it, `java -jar` runs it on the JVM |
| Java 8+ features | streams, lambdas and `Optional` in `Library` and `Repository` (search, available books, `findUser`) |
| JAR file | `library-management.jar` (Main-Class set in the manifest) |
| Threads | `ConcurrencyDemo` (menu option 11 or `--demo`), `BackupTask` (background thread) |
| Thread safety | all public methods of `Library` are `synchronized` |
| Generics | `Repository<T>` stores both books and users |
| Reflection | `UserFactory` creates a role from its name; `ReportGenerator` reads fields and class structure |
| Coding style | camelCase methods, PascalCase classes, Javadoc on every class, short methods |
| Unit testing | JUnit 4 + Mockito in `src/test` |

## Business rules

* A student can borrow at most **3** books at the same time (a librarian: 10).
* A book can only be borrowed if it is available.
* A book can only be returned by the user who borrowed it.
* Only a librarian can add books, add users and see the admin report.

## Debugging tips

* Run with logs: `java -Dlibrary.debug=true -jar library-management.jar`.
  `Library` then prints who borrowed what, how many books the user already has, and the thread name.
* In an IDE, put a breakpoint inside `Library.borrowBook` (on the limit check) and run
  `LibraryTest.studentCannotBorrowAFourthBook` in debug mode.
  A good exercise: change `>=` to `>` in the limit check, run the tests, see which test fails, then find the bug with the debugger.

## Notes

* `LibraryMockitoTest` needs the Mockito library, so run it with Maven (`mvn test`) or add the JUnit and Mockito jars to your IDE project.
