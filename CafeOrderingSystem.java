import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

// Base class for all menu items
abstract class MenuItem {
    protected String name;
    protected double price;
    protected String variation;

    public MenuItem(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public abstract void selectVariation(Scanner sc);
    
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getVariation() { return variation; }
    
    public String getDisplayName() {
        return variation != null ? name + " (" + variation + ")" : name;
    }
}

// Drink class with size selection
class Drink extends MenuItem {
    private String size;
    private static final Map<String, Double> SIZE_PRICES = Map.of(
        "Small", 0.0,
        "Medium", 15.0,
        "Large", 25.0
    );

    public Drink(String name, double basePrice) {
        super(name, basePrice);
    }

    @Override
    public void selectVariation(Scanner sc) {
        // Select size first
        System.out.println("\nSelect size:");
        System.out.println("1. Small (Base Price)");
        System.out.println("2. Medium (+₱15)");
        System.out.println("3. Large (+₱25)");
        System.out.print("Enter choice: ");
        int sizeChoice = sc.nextInt();
        sc.nextLine();
        
        switch(sizeChoice) {
            case 1: size = "Small"; break;
            case 2: size = "Medium"; price += 15; break;
            case 3: size = "Large"; price += 25; break;
            default: size = "Small";
        }

        // Select flavor
        System.out.println("\nSelect flavor:");
        if (name.equals("Milktea")) {
            System.out.println("1. Salted Caramel\n2. Okinawa\n3. WinterMelon\n4. Matcha");
        } else if (name.equals("Coffee")) {
            System.out.println("1. Ice Caramel\n2. Espresso\n3. Americano\n4. Latte");
        } else if (name.equals("Fruit Soda")) {
            System.out.println("1. Lemon\n2. Strawberry\n3. Lychee\n4. Green Apple");
        }
        System.out.print("Enter choice: ");
        int flavorChoice = sc.nextInt();
        sc.nextLine();
        
        String[] milkteaFlavors = {"Salted Caramel", "Okinawa", "WinterMelon", "Matcha"};
        String[] coffeeFlavors = {"Ice Caramel", "Espresso", "Americano", "Latte"};
        String[] sodaFlavors = {"Lemon", "Strawberry", "Lychee", "Green Apple"};
        
        if (name.equals("Milktea")) {
            variation = size + " " + milkteaFlavors[flavorChoice - 1];
        } else if (name.equals("Coffee")) {
            variation = size + " " + coffeeFlavors[flavorChoice - 1];
        } else if (name.equals("Fruit Soda")) {
            variation = size + " " + sodaFlavors[flavorChoice - 1];
        }
    }
}

// Pastry class
class Pastry extends MenuItem {
    public Pastry(String name, double price) {
        super(name, price);
    }

    @Override
    public void selectVariation(Scanner sc) {
        System.out.println("\nSelect flavor:");
        if (name.equals("Cake") || name.equals("Cupcake")) {
            System.out.println("1. Chocolate\n2. Red Velvet\n3. Carrot");
        } else if (name.equals("Donut")) {
            System.out.println("1. Caramel\n2. Chocolate Sprinkles\n3. Red Velvet");
        }
        System.out.print("Enter choice: ");
        int choice = sc.nextInt();
        sc.nextLine();
        
        if (name.equals("Cake") || name.equals("Cupcake")) {
            String[] flavors = {"Chocolate", "Red Velvet", "Carrot"};
            variation = flavors[choice - 1];
        } else if (name.equals("Donut")) {
            String[] flavors = {"Caramel", "Chocolate Sprinkles", "Red Velvet"};
            variation = flavors[choice - 1];
        }
    }
}

// Main Course class
class MainCourse extends MenuItem {
    public MainCourse(String name, double price) {
        super(name, price);
    }

    @Override
    public void selectVariation(Scanner sc) {
        System.out.println("\nSelect type:");
        if (name.equals("Pasta")) {
            System.out.println("1. Spaghetti\n2. Carbonara");
            int choice = sc.nextInt();
            sc.nextLine();
            variation = (choice == 1) ? "Spaghetti" : "Carbonara";
        } else if (name.equals("Burger")) {
            System.out.println("1. Plain");
            int choice = sc.nextInt();
            sc.nextLine();
            variation = "Plain";
        } else if (name.equals("Fries")) {
            System.out.println("1. Cheese\n2. Sour Cream");
            int choice = sc.nextInt();
            sc.nextLine();
            variation = (choice == 1) ? "Cheese" : "Sour Cream";
        }
    }
}

// Order Item to track quantity
class OrderItem {
    private MenuItem item;
    private int quantity;

    public OrderItem(MenuItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public MenuItem getItem() { return item; }
    public int getQuantity() { return quantity; }
    public void addQuantity(int qty) { this.quantity += qty; }
    public double getTotalPrice() { return item.getPrice() * quantity; }
}

// Order management class
class Order {
    private List<OrderItem> items;
    private LocalDateTime timeIn;
    private LocalDateTime timeOut;

    public Order() {
        items = new ArrayList<>();
        timeIn = LocalDateTime.now();
    }

    public void addItem(MenuItem item, int quantity) {
        // Check if item with same variation exists
        for (OrderItem oi : items) {
            if (oi.getItem().getDisplayName().equals(item.getDisplayName())) {
                oi.addQuantity(quantity);
                return;
            }
        }
        items.add(new OrderItem(item, quantity));
    }

    public void displayOrder() {
        if (items.isEmpty()) {
            System.out.println("\nYour order is empty!");
            return;
        }
        System.out.println("\n========== Current Order ==========");
        for (OrderItem oi : items) {
            System.out.printf("%d  %-30s ₱%.2f\n", 
                oi.getQuantity(), 
                oi.getItem().getDisplayName(), 
                oi.getTotalPrice());
        }
        System.out.println("===================================");
    }

    public double getSubtotal() {
        return items.stream().mapToDouble(OrderItem::getTotalPrice).sum();
    }

    public void checkout() {
        timeOut = LocalDateTime.now();
        
        Duration duration = Duration.between(timeIn, timeOut);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        
        // Calculate time-based charge (₱50 for every 30 minutes)
        long totalMinutes = duration.toMinutes();
        double timeCharge = Math.floor(totalMinutes / 30.0) * 50;

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        
        System.out.println("\nTime Out: " + timeOut.format(timeFormatter));
        System.out.println("Time Stayed: " + hours + " hour " + minutes + " minutes");
        
        if (timeCharge > 0) {
            System.out.println("Applying time-based charges...");
            System.out.printf("(Add every after 30 minutes) Extra Charge for long stay: ₱%.0f\n", timeCharge);
        }

        // Print receipt
        System.out.println("\n========================================");
        System.out.println("               C A F É  J A V A");
        System.out.println("                 O F F I C I A L");
        System.out.println("                     R E C E I P T");
        System.out.println("========================================");
        System.out.println("Time In: " + timeIn.format(timeFormatter));
        System.out.println("Time Out: " + timeOut.format(timeFormatter));
        System.out.println("Duration: " + hours + "h " + minutes + "m");
        System.out.println("----------------------------------------");
        System.out.println("Items Purchased:");
        System.out.println("----------------------------------------");
        
        for (OrderItem oi : items) {
            System.out.printf("%-2d %-30s ₱%.2f\n", 
                oi.getQuantity(), 
                oi.getItem().getDisplayName(), 
                oi.getTotalPrice());
        }
        
        System.out.println("----------------------------------------");
        System.out.printf("Subtotal:                       ₱%.2f\n", getSubtotal());
        if (timeCharge > 0) {
            System.out.printf("Time-Based Charge:              ₱%.0f\n", timeCharge);
        }
        System.out.println("----------------------------------------");
        System.out.printf("TOTAL AMOUNT:                  ₱%.2f\n", getSubtotal() + timeCharge);
        System.out.println("========================================");
        System.out.println("     THANK YOU FOR DINING WITH US!");
        System.out.println("          PLEASE COME AGAIN :)");
        System.out.println("========================================");
    }

    public LocalDateTime getTimeIn() { return timeIn; }
}

// Main Cafe System
public class CafeOrderingSystem {
    private static Scanner sc = new Scanner(System.in);
    private static Order order;

    public static void main(String[] args) {
        order = new Order();
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        System.out.println("=== Welcome to Café Java ===");
        System.out.println("Time In: " + order.getTimeIn().format(timeFormatter));
        
        boolean running = true;
        while (running) {
            displayKiosk();
            int choice = sc.nextInt();
            sc.nextLine();
            
            switch (choice) {
                case 1: viewMenu(); break;
                case 2: addItemToOrder(); break;
                case 3: order.displayOrder(); break;
                case 4: 
                    order.checkout();
                    running = false;
                    break;
                case 5: 
                    System.out.println("Thank you for visiting Café Java!");
                    running = false;
                    break;
                default: 
                    System.out.println("Invalid choice!");
            }
        }
    }

    private static void displayKiosk() {
        System.out.println("\nKiosk:");
        System.out.println("1. View Menu");
        System.out.println("2. Add Item to Order");
        System.out.println("3. View Current Order");
        System.out.println("4. Checkout");
        System.out.println("5. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void viewMenu() {
        System.out.println("\n========== Menu ==========");
        System.out.println("\nDRINKS");
        System.out.println("Milktea (Salted Caramel, Okinawa, WinterMelon, Matcha) - ₱55");
        System.out.println("Coffee (Ice Caramel, Espresso, Americano, Latte) - ₱60");
        System.out.println("Fruit Soda (Lemon, Strawberry, Lychee, Green Apple) - ₱45");
        
        System.out.println("\nPASTRIES");
        System.out.println("Cake (Chocolate, Red Velvet, Carrot) - ₱85");
        System.out.println("Donut (Caramel, Chocolate Sprinkles, Red Velvet) - ₱45");
        System.out.println("Cupcake (Chocolate, Red Velvet, Carrot) - ₱50");
        
        System.out.println("\nMAIN COURSE");
        System.out.println("Pasta (Spaghetti, Carbonara) - ₱60");
        System.out.println("Burger (Plain) - ₱75");
        System.out.println("Fries (Cheese, Sour Cream) - ₱50");
        System.out.println("==========================");
    }

    private static void addItemToOrder() {
        boolean addingItems = true;
        
        while (addingItems) {
            System.out.println("\nChoose Category:");
            System.out.println("1. Main Course");
            System.out.println("2. Drinks");
            System.out.println("3. Pastries");
            System.out.print("Enter choice: ");
            int category = sc.nextInt();
            sc.nextLine();
            
            MenuItem item = null;
            
            switch (category) {
                case 1: item = selectMainCourse(); break;
                case 2: item = selectDrink(); break;
                case 3: item = selectPastry(); break;
                default: System.out.println("Invalid category!"); continue;
            }
            
            if (item != null) {
                item.selectVariation(sc);
                System.out.print("Enter quantity: ");
                int qty = sc.nextInt();
                sc.nextLine();
                order.addItem(item, qty);
                System.out.println("Item added to order!");
            }
            
            System.out.print("\nWould you like to add more items? (Y/N): ");
            String more = sc.nextLine();
            if (more.equalsIgnoreCase("N")) {
                addingItems = false;
            }
        }
    }

    private static MenuItem selectMainCourse() {
        System.out.println("\nMain Course:");
        System.out.println("1. Pasta - ₱60");
        System.out.println("2. Burger - ₱75");
        System.out.println("3. Fries - ₱50");
        System.out.print("Enter choice: ");
        int choice = sc.nextInt();
        sc.nextLine();
        
        switch (choice) {
            case 1: return new MainCourse("Pasta", 60);
            case 2: return new MainCourse("Burger", 75);
            case 3: return new MainCourse("Fries", 50);
            default: return null;
        }
    }

    private static MenuItem selectDrink() {
        System.out.println("\nDrinks:");
        System.out.println("1. Milktea - ₱55");
        System.out.println("2. Coffee - ₱60");
        System.out.println("3. Fruit Soda - ₱45");
        System.out.print("Enter choice: ");
        int choice = sc.nextInt();
        sc.nextLine();
        
        switch (choice) {
            case 1: return new Drink("Milktea", 55);
            case 2: return new Drink("Coffee", 60);
            case 3: return new Drink("Fruit Soda", 45);
            default: return null;
        }
    }

    private static MenuItem selectPastry() {
        System.out.println("\nPastries:");
        System.out.println("1. Cake - ₱85");
        System.out.println("2. Donut - ₱45");
        System.out.println("3. Cupcake - ₱50");
        System.out.print("Enter choice: ");
        int choice = sc.nextInt();
        sc.nextLine();
        
        switch (choice) {
            case 1: return new Pastry("Cake", 85);
            case 2: return new Pastry("Donut", 45);
            case 3: return new Pastry("Cupcake", 50);
            default: return null;
        }
    }
}