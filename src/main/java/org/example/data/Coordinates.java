package org.example.data;

import java.io.Serializable;

public class Coordinates implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long x;
    private float y;

    public Coordinates(Long x, float y) {
        setX(x);
        setY(y);
    }

    public Long getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(Long x) {
        if (x == null || x <= -349) {
            throw new IllegalArgumentException("Координата X должна быть > -349 и не null.");
        }
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("(x=%d, y=%.2f)", x, y);
    }
}