package com.example.clownmaze.core.entity;

/**
 * Command interface — encapsulates a single hero action.
 *
 * <p>Implementations: {@link MoveCommand}, {@link MashCommand}, {@link InteractCommand}.
 * Future: {@code UseKeyCommand} (S3-04), {@code AnswerRiddleCommand} (S3-02).
 *
 * <p>Pattern: Command (GoF #10).
 */
public interface ICommand {
    /** Executes the encapsulated action. */
    void execute();
}
