package com.example.clownmaze.core.interact;

import java.util.List;

import com.badlogic.gdx.math.MathUtils;

import com.example.clownmaze.audio.AudioManager;
import com.example.clownmaze.core.GameStateManager;
import com.example.clownmaze.core.entity.Hero;
import com.example.clownmaze.core.entity.Spider;

/** Random negative effect triggered by a FAKE interactable object. */
public final class FakeEffect {

    private FakeEffect() {}

    public static void applyRandom(Hero hero, List<Spider> spiders) {
        switch (MathUtils.random(2)) {
            case 0 -> GameStateManager.getInstance().reduceTimer(5f);
            case 1 -> spawnSpiderNear(hero, spiders);
            case 2 -> AudioManager.getInstance().playClownSting();
        }
    }

    private static void spawnSpiderNear(Hero hero, List<Spider> spiders) {
        float ox = MathUtils.random(-32f, 32f);
        float oy = MathUtils.random(-32f, 32f);
        spiders.add(new Spider(hero.getX() + ox, hero.getY() + oy));
    }
}
