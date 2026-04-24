package com.example.clownmaze.core.entity;
@FunctionalInterface
public interface WalkabilityChecker {
    boolean isWalkable(float worldX, float worldY);
}
