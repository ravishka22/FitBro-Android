package lk.codehive.ravishka.fitbro.models;

public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private String category;

    public Product(String id, String name, String description, double price, String imageUrl, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }
}