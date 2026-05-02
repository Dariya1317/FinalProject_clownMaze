package com.example.clownmaze;

import com.badlogic.gdx.Game;
import com.example.clownmaze.screens.MainMenuScreen;

public class Main extends Game {

    @Override
    public void create() {
        setScreen(new MainMenuScreen(this));
    }
}
