package com.ctrip.ferriswheel.core.intf;

/**
 * Action handler.
 */
public interface ActionHandler {
    /**
     * Handle the specified action.
     *
     * @param action
     */
    void handle(Action action);
}
