package lk.codehive.ravishka.fitbro.models;

public class Exercise {
    private String name;
    private String muscleGroup;
    private int sets;
    private String reps;
    private String rest;
    private String imageUrl; // Thumbnail URL or resource name

    // No-argument constructor required for Firestore deserialization
    public Exercise() {}

    public Exercise(String name, String muscleGroup, int sets, String reps, String rest, String imageUrl) {
        this.name = name;
        this.muscleGroup = muscleGroup;
        this.sets = sets;
        this.reps = reps;
        this.rest = rest;
        this.imageUrl = imageUrl;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }

    public String getReps() { return reps; }
    public void setReps(String reps) { this.reps = reps; }

    public String getRest() { return rest; }
    public void setRest(String rest) { this.rest = rest; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}