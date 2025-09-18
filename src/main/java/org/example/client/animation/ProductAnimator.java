package org.example.client.animation;

import org.example.data.Product;
import java.util.HashMap;
import java.util.Map;

public class ProductAnimator {
    private final Map<Long, AnimationState> animationStates;

    public ProductAnimator() {
        this.animationStates = new HashMap<>();
    }

    public void addProduct(Product product) {
        animationStates.put(product.getId(), new AnimationState());
    }

    public void removeProduct(Long productId) {
        AnimationState state = animationStates.get(productId);
        if (state != null) {
            state.setRemoving(true);
        }
    }

    public void update() {
        animationStates.values().removeIf(AnimationState::update);
    }

    public float getAnimationState(Long productId) {
        AnimationState state = animationStates.get(productId);
        return state != null ? state.getValue() : 1.0f;
    }

    public boolean isAnimating(Long productId) {
        AnimationState state = animationStates.get(productId);
        return state != null && !state.isComplete();
    }

    private static class AnimationState {
        private float value;
        private boolean removing;
        private boolean complete;

        public AnimationState() {
            this.value = 0.0f;
            this.removing = false;
            this.complete = false;
        }

        public boolean update() {
            if (complete) {
                return true;
            }

            if (removing) {
                value -= 0.05f;
                if (value <= 0) {
                    complete = true;
                    value = 0;
                }
            } else {
                value += 0.05f;
                if (value >= 1) {
                    value = 1;
                    complete = true;
                }
            }

            return complete;
        }

        public float getValue() {
            return value;
        }

        public boolean isRemoving() {
            return removing;
        }

        public void setRemoving(boolean removing) {
            this.removing = removing;
        }

        public boolean isComplete() {
            return complete;
        }
    }
}