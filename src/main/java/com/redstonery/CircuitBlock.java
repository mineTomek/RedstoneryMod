package com.redstonery;

import net.minecraft.util.math.Direction;

public class CircuitBlock {
    private String id;
    private Direction facing;
    private boolean state;
    private boolean locked;
    private int delay;
    private boolean subtract;
    private boolean open;
    private int page;
    private boolean inverted;

    public CircuitBlock(String id, Direction facing, boolean state, boolean locked, int delay,
            boolean subtract, boolean open, int page, boolean inverted) {
        this.id = id;
        this.facing = facing;
        this.state = state;
        this.locked = locked;
        this.delay = delay;
        this.subtract = subtract;
        this.open = open;
        this.page = page;
        this.inverted = inverted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Direction getFacing() {
        return facing;
    }

    public void setFacing(Direction facing) {
        this.facing = facing;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public boolean isSubtract() {
        return subtract;
    }

    public void setSubtract(boolean subtract) {
        this.subtract = subtract;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }
}
