package com.example.clownmaze.core.entity;

/**
 * Command that registers one mash keypress while the Hero is trapped.
 * When {@link Hero#MASH_REQUIRED} presses accumulate, the Hero escapes.
 *
 * <p>Pattern: Command (GoF #10).
 */
public final class MashCommand implements ICommand {

    private final Hero hero;

    public MashCommand(Hero hero) {
        this.hero = hero;
    }

    @Override
    public void execute() {
        hero.mash();
    }
}
