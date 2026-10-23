package com.cupids.curseoftheforest.minigames;

/** Thrown when Dayo tries to buy an item he cannot afford. */
public class InsufficientCoinsException extends Exception {
    public InsufficientCoinsException(String msg) {
        super(msg);
    }
}
