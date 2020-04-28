package org.molguin;

public abstract class Callback<Arg, Ret> {
    public abstract Ret apply(Arg arg);
}
