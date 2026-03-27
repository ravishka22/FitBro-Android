package lk.codehive.ravishka.fitbro.models;

public class Exercise {
    private String name;
    private String muscleGroup;
    private int sets;
    private String reps;
    private String rest;
    private String imageUrl; // Thumbnail URL or resource name

    public Exercise(String name, String muscleGroup, int sets, String reps, String rest, String imageUrl) {
        this.name = name;
        this.muscleGroup = muscleGroup;
        this.sets = sets;
        this.reps = reps;
        this.rest = rest;
        this.imageUrl = imageUrl;
    }

    public String getName() { return name; }
    public String getMuscleGroup() { return muscleGroup; }
    public int getSets() { return sets; }
    public String getReps() { return reps; }
    public String getRest() { return rest; }
    public String getImageUrl() { return imageUrl; }
}