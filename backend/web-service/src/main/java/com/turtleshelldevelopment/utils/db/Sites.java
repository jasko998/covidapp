package com.turtleshelldevelopment.utils.db;

public record Sites(String name, int id) {
    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
