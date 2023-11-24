package com.redstonery;

import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

public class Circuit {
    private final String name;
    private final Set<String> descriptions;

    public Circuit(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.name = name;
        this.descriptions = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public Set<String> getDescriptions() {
        return Collections.unmodifiableSet(descriptions);
    }

    public void clearDescriptions() {
        descriptions.clear();
    }

    public void addDescription(String description) {
        descriptions.add(description);
    }
}
