package org.molguin.islandgardener.utils;

public abstract class Callback<Arg, Ret> {
    public abstract Ret apply(Arg arg);
}
