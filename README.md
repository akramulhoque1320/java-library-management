# Java Library Management System

A console-based library management system written in **Java 17** with no external libraries.

## Features

- Add, remove, list and search books (by title or author)
- Register members
- Borrow books (14-day loans, at most 3 books per member)
- Return books, with a late fine of 10 taka per day
- See all overdue books
- Data saved to `library.dat` and loaded on start
- Dependency-free test suite

## Build and run

```bash
# compile
javac -d out src/com/ahnaf/library/*.java

# run the app
java -cp out com.ahnaf.library.Main
```

```
=== Library Management System ===

1) List books        2) Add book        3) Remove book
4) Search books      5) List members    6) Add member
7) Borrow book       8) Return book     9) Overdue books
0) Save and exit
Choose: 7
ISBN: 978-0134685991
Member id: 1
Borrowed "Effective Java". Due back on 2026-10-27.
```

## Run the tests

```bash
javac -d out src/com/ahnaf/library/*.java test/com/ahnaf/library/*.java
java -cp out com.ahnaf.library.LibraryTest
```

## Design

| Class | Responsibility |
|-------|----------------|
| `Book` | Book details and loan state (borrower, due date) |
| `Member` | Library member |
| `Library` | Business rules: loans, limits, fines, search, save/load |
| `LibraryException` | Rule violations, shown to the user as friendly errors |
| `Main` | Console menu |

Concepts used: OOP and encapsulation, collections (`LinkedHashMap`, `List`), `java.time`, file I/O with `java.nio`, switch expressions and custom exceptions.
