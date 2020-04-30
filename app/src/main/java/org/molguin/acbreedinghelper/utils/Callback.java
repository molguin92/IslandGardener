package org.molguin.acbreedinghelper.utils;

public abstract class Callback<Arg, Ret> {
    public abstract Ret apply(Arg arg);
}
