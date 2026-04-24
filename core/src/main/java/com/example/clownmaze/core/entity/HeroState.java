package com.example.clownmaze.core.entity;

/**
 * Finite-state-machine states for the Hero entity.
 *
 * <p>Transitions:
 * <pre>
 *   NORMAL  ──trap()──►  TRAPPED  ──mash×8──►  NORMAL  (TrapEscapeEvent)
 *   NORMAL  ──slow()──►  SLOWED   ──3 sec ──►  NORMAL
 *   TRAPPED ──timer──►  NORMAL               (TrapFailEvent)
 *   TRAPPED  slow()  ignored  (trap takes priority)
 * </pre>
 *
 * Pattern: State (GoF #7).
 */
public enum HeroState {
    /** Default: hero can walk and run freely. */
    NORMAL,

    /** After spider contact: walk only, no running, lasts {@link Hero#SLOW_DURATION} seconds. */
    SLOWED,

    /** Stepped on a trap: immobilised, must mash {@link Hero#MASH_REQUIRED} times to escape,
     *  or the trap timer fires {@link com.example.clownmaze.core.EventBus.TrapFailEvent}. */
    TRAPPED
}
