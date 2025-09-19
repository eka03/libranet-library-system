import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

// Custom exception for library operations
class LibraryException extends Exception {
    public LibraryException(String message) {
        super(message);
    }
}

// Interface for playable items
interface Playable {
    void play();

    double getDuration();
}

// Base abstract class for all library items
abstract class LibraryItem {
    protected final int id;
    protected final String title;
    protected final String author;
    protected boolean isAvailable;
    protected LocalDate dueDate;
    protected static final double DEFAULT_FINE_RATE = 10.0; 

    public LibraryItem(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isAvailable = true;
        this.dueDate = null;
    }

    // Common operations
    public void borrowItem(String borrowDateStr) throws LibraryException {
        if (!isAvailable) {
            throw new LibraryException("Item is not available for borrowing");
        }

        try {
            LocalDate borrowDate = LocalDate.parse(borrowDateStr);
            this.dueDate = borrowDate.plusDays(14); // 2 weeks borrowing period
            this.isAvailable = false;
        } catch (DateTimeParseException e) {
            throw new LibraryException("Invalid date format. Please use YYYY-MM-DD");
        }
    }

    public double returnItem(String returnDateStr) throws LibraryException {
        if (isAvailable) {
            throw new LibraryException("Item was not borrowed");
        }

        try {
            LocalDate returnDate = LocalDate.parse(returnDateStr);
            this.isAvailable = true;

            if (returnDate.isAfter(dueDate)) {
                long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
                return daysOverdue * DEFAULT_FINE_RATE;
            }
            return 0.0;
        } catch (DateTimeParseException e) {
            throw new LibraryException("Invalid date format. Please use YYYY-MM-DD");
        }
    }

    public boolean checkAvailability() {
        return isAvailable;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    @Override
    public String toString() {
        return String.format("ID: %d, Title: %s, Author: %s, Available: %s",
                id, title, author, isAvailable ? "Yes" : "No");
    }
}

// Book class
class Book extends LibraryItem {
    private final int pageCount;

    public Book(int id, String title, String author, int pageCount) {
        super(id, title, author);
        this.pageCount = pageCount;
    }

    public int getPageCount() {
        return pageCount;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", Type: Book, Pages: %d", pageCount);
    }
}

// Audiobook class implementing Playable interface
class Audiobook extends LibraryItem implements Playable {
    private final double duration; // in hours

    public Audiobook(int id, String title, String author, double duration) {
        super(id, title, author);
        this.duration = duration;
    }

    @Override
    public void play() {
        System.out.println("Playing audiobook: " + getTitle());
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", Type: Audiobook, Duration: %.2f hours", duration);
    }
}

// E-magazine class
class EMagazine extends LibraryItem {
    private final int issueNumber;
    private boolean isArchived;

    public EMagazine(int id, String title, String author, int issueNumber) {
        super(id, title, author);
        this.issueNumber = issueNumber;
        this.isArchived = false;
    }

    public void archiveIssue() {
        this.isArchived = true;
        System.out.println("Issue #" + issueNumber + " of " + getTitle() + " has been archived.");
    }

    public boolean isArchived() {
        return isArchived;
    }

    public int getIssueNumber() {
        return issueNumber;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", Type: E-Magazine, Issue: %d, Archived: %s",
                issueNumber, isArchived ? "Yes" : "No");
    }
}

// Library management system
class LibraNet {
    private final Map<Integer, LibraryItem> items;
    private final Map<Integer, Double> fines;

    public LibraNet() {
        items = new HashMap<>();
        fines = new HashMap<>();
    }

    public void addItem(LibraryItem item) {
        items.put(item.getId(), item);
    }

    public LibraryItem getItem(int id) {
        return items.get(id);
    }

    public void borrowItem(int id, String borrowDate) throws LibraryException {
        LibraryItem item = items.get(id);
        if (item == null) {
            throw new LibraryException("Item with ID " + id + " not found");
        }
        item.borrowItem(borrowDate);
    }

    public double returnItem(int id, String returnDate) throws LibraryException {
        LibraryItem item = items.get(id);
        if (item == null) {
            throw new LibraryException("Item with ID " + id + " not found");
        }

        double fine = item.returnItem(returnDate);
        if (fine > 0) {
            fines.put(id, fines.getOrDefault(id, 0.0) + fine);
        }
        return fine;
    }

    public List<LibraryItem> searchByTitle(String title) {
        return items.values().stream()
                .filter(item -> item.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<LibraryItem> searchByAuthor(String author) {
        return items.values().stream()
                .filter(item -> item.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .collect(Collectors.toList());
    }

    public <T> List<T> searchByType(Class<T> type) {
        return items.values().stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
    }

    public double getTotalFines() {
        return fines.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public double getFinesForItem(int id) {
        return fines.getOrDefault(id, 0.0);
    }

    public List<LibraryItem> getAvailableItems() {
        return items.values().stream()
                .filter(LibraryItem::checkAvailability)
                .collect(Collectors.toList());
    }

    public List<LibraryItem> getBorrowedItems() {
        return items.values().stream()
                .filter(item -> !item.checkAvailability()) 
                .collect(Collectors.toList());
    }

    // Helper method to display all items
    public void displayAllItems() {
        System.out.println("\n ALL LIBRARY ITEMS ");
        items.values().forEach(System.out::println);
    }
}

// Example usage with a simple menu system
class LibraNetSystem { // REMOVED: public modifier
    private static LibraNet libraNet = new LibraNet();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Pre-populate with some sample items
        initializeLibrary();

        boolean running = true;
        while (running) {
            System.out.println("\n LIBRANET LIBRARY MANAGEMENT SYSTEM ");
            System.out.println("1. Display all the items");
            System.out.println("2. Borrow an item");
            System.out.println("3. Return an item");
            System.out.println("4. Search by title");
            System.out.println("5. Search by author");
            System.out.println("6. Search by type");
            System.out.println("7. Show available items");
            System.out.println("8. Show borrowed items");
            System.out.println("9. Show total fines");
            System.out.println("10. Use specialized functions");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        libraNet.displayAllItems();
                        break;
                    case 2:
                        borrowItem();
                        break;
                    case 3:
                        returnItem();
                        break;
                    case 4:
                        searchByTitle();
                        break;
                    case 5:
                        searchByAuthor();
                        break;
                    case 6:
                        searchByType();
                        break;
                    case 7:
                        showAvailableItems();
                        break;
                    case 8:
                        showBorrowedItems();
                        break;
                    case 9:
                        showFines();
                        break;
                    case 10:
                        useSpecializedFunctions();
                        break;
                    case 0:
                        running = false;
                        System.out.println("Thank you for using LibraNet! Come Again.");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try after sometime.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void initializeLibrary() {
        // Adding sample items to the library
        libraNet.addItem(new Book(1, "The Great Gatsby", "F. Scott Fitzgerald", 180));
        libraNet.addItem(new Book(2, "To Kill a Mockingbird", "Harper Lee", 281));
        libraNet.addItem(new Audiobook(3, "The Alchemist", "Paulo Coelho", 4.5));
        libraNet.addItem(new Audiobook(4, "Atomic Habits", "James Clear", 5.2));
        libraNet.addItem(new EMagazine(5, "National Geographic", "Various Authors", 256));
        libraNet.addItem(new EMagazine(6, "Scientific American", "Various Authors", 312));

        System.out.println("Library initialized with sample items.");
    }

    private static void borrowItem() {
        try {
            System.out.print("Enter item ID to borrow: ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter borrow date (YYYY-MM-DD): ");
            String borrowDate = scanner.nextLine();

            libraNet.borrowItem(id, borrowDate);
            System.out.println("Item borrowed successfully. Due date: " +
                    libraNet.getItem(id).getDueDate());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number for ID.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void returnItem() {
        try {
            System.out.print("Enter item ID to return: ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter return date (YYYY-MM-DD): ");
            String returnDate = scanner.nextLine();

            double fine = libraNet.returnItem(id, returnDate);
            if (fine > 0) {
                System.out.println("Item returned successfully. Fine: " + fine + " rs");
            } else {
                System.out.println("Item returned successfully. No fines.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number for ID.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void searchByTitle() {
        System.out.print("Enter title to search: ");
        String title = scanner.nextLine();

        List<LibraryItem> results = libraNet.searchByTitle(title);
        if (results.isEmpty()) {
            System.out.println("No items found with that title.");
        } else {
            System.out.println("Search results:");
            results.forEach(System.out::println);
        }
    }

    private static void searchByAuthor() {
        System.out.print("Enter author to search: ");
        String author = scanner.nextLine();

        List<LibraryItem> results = libraNet.searchByAuthor(author);
        if (results.isEmpty()) {
            System.out.println("No items found by that author.");
        } else {
            System.out.println("Search results:");
            results.forEach(System.out::println);
        }
    }

    private static void searchByType() {
        System.out.println("Select item type:");
        System.out.println("1. Books");
        System.out.println("2. Audiobooks");
        System.out.println("3. E-Magazines");
        System.out.print("Enter your choice: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            List<?> results;
            switch (choice) {
                case 1:
                    results = libraNet.searchByType(Book.class);
                    break;
                case 2:
                    results = libraNet.searchByType(Audiobook.class);
                    break;
                case 3:
                    results = libraNet.searchByType(EMagazine.class);
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }

            if (results.isEmpty()) {
                System.out.println("No items of this type found.");
            } else {
                System.out.println("Search results:");
                results.forEach(System.out::println);
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }
    }

    private static void showAvailableItems() {
        List<LibraryItem> available = libraNet.getAvailableItems();
        if (available.isEmpty()) {
            System.out.println("No available items at the moment.");
        } else {
            System.out.println("Available items:");
            available.forEach(System.out::println);
        }
    }

    private static void showBorrowedItems() {
        List<LibraryItem> borrowed = libraNet.getBorrowedItems();
        if (borrowed.isEmpty()) {
            System.out.println("No borrowed items at the moment.");
        } else {
            System.out.println("Borrowed items:");
            borrowed.forEach(System.out::println);
        }
    }

    private static void showFines() {
        System.out.println("Total fines collected: " + libraNet.getTotalFines() + " rs");
    }

    private static void useSpecializedFunctions() {
        System.out.println("Specialized functions:");
        System.out.println("1. Get page count of a book");
        System.out.println("2. Play an audiobook");
        System.out.println("3. Archive an e-magazine");
        System.out.print("Enter your choice: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    System.out.print("Enter book ID: ");
                    int bookId = Integer.parseInt(scanner.nextLine());
                    LibraryItem bookItem = libraNet.getItem(bookId);
                    if (bookItem instanceof Book) {
                        Book book = (Book) bookItem;
                        System.out.println("Page count: " + book.getPageCount());
                    } else {
                        System.out.println("This item is not a book.");
                    }
                    break;

                case 2:
                    System.out.print("Enter audiobook ID: ");
                    int audioId = Integer.parseInt(scanner.nextLine());
                    LibraryItem audioItem = libraNet.getItem(audioId);
                    if (audioItem instanceof Audiobook) {
                        Audiobook audiobook = (Audiobook) audioItem;
                        audiobook.play();
                        System.out.println("Duration: " + audiobook.getDuration() + " hours");
                    } else {
                        System.out.println("This item is not an audiobook.");
                    }
                    break;

                case 3:
                    System.out.print("Enter e-magazine ID: ");
                    int magId = Integer.parseInt(scanner.nextLine());
                    LibraryItem magItem = libraNet.getItem(magId);
                    if (magItem instanceof EMagazine) {
                        EMagazine magazine = (EMagazine) magItem;
                        magazine.archiveIssue();
                    } else {
                        System.out.println("This item is not an e-magazine.");
                    }
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}