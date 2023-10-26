package com.cpen321.tunematch;

public class Friend {
    private String name;
    private String id;
    private boolean isListening;

    public Friend(String name, String id) {
        this.name = name;
        this.id = id;
        this.isListening = false;
    }

    public String getName() {
        return name;
    }
    public String getId() {return id;}

    // getters and setters
}
