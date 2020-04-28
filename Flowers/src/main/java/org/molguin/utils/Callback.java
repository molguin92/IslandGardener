package org.molguin.utils;

public abstract class Callback<Arg, Ret> {
    public abstract Ret apply(Arg arg);
}
