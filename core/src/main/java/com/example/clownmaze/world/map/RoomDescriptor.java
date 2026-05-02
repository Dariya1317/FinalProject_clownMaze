package com.example.clownmaze.world.map;

public record RoomDescriptor(
    int id,
    String tmxPath,
    float heroSpawnX,
    float heroSpawnY,
    float clownSpawnX,
    float clownSpawnY
) {}
