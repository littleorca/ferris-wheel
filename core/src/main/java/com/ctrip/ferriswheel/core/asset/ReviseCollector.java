package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.action.Action;
import com.ctrip.ferriswheel.common.action.ActionListener;
import com.ctrip.ferriswheel.core.action.UpdateChart;
import com.ctrip.ferriswheel.core.action.UpdateForm;

import java.util.*;

/**
 * Edit event listener for monitor revises and generate change-list.
 */
public class ReviseCollector implements ActionListener {
    private Stack<Action> actionStack = new Stack<>();
    private Stack<Integer> actionCountStack = new Stack<>();
    private LinkedList<Action> actions = new LinkedList<>();

    @Override
    public boolean beforeAction(Action action) {
        actionStack.push(action);
        actionCountStack.push(actions.size());
        return true;
    }

    @Override
    public void afterActionDone(Action action, Object result) {
        if (action != actionStack.pop()) {
            throw new IllegalStateException();
        }
        Integer size = actionCountStack.pop();
        actions.add(size, action);
    }

    @Override
    public void afterActionFailed(Action action) {
        if (action != actionStack.pop()) {
            throw new IllegalStateException();
        }
        Integer size = actionCountStack.pop();
        while (actions.size() > size) {
            actions.remove((int) size);
        }
    }

    public List<Action> drainRevises() {
        List<Action> ret = actions;
        actions = new LinkedList<>();
        return filterActions(ret);
    }

    private List<Action> filterActions(List<Action> rawActions) {
        List<Action> filteredActions = new ArrayList<>(rawActions.size());
        Map<String, Integer> occurrences = new HashMap<>();
        for (Action action : rawActions) {
            if (action instanceof UpdateChart ||
                    action instanceof UpdateForm) {
                // those actions only need the last one.
                // fixme key may not unique if some of the names contains $ or !
                String key = null;
                if (action instanceof UpdateChart) {
                    key = action.getActionCode() + "$"
                            + ((UpdateChart) action).getSheetName() + "!"
                            + ((UpdateChart) action).getChartName();
                } else if (action instanceof UpdateForm) {
                    key = action.getActionCode() + "$"
                            + ((UpdateForm) action).getSheetName() + "!"
                            + ((UpdateForm) action).getFormName();
                }
                Integer pos = occurrences.get(key);
                if (pos != null) {
                    filteredActions.set(pos, action); // remove former duplicated action
                } else {
                    occurrences.put(key, filteredActions.size());
                    filteredActions.add(action);
                }

            } else {
                filteredActions.add(action);
            }
        }
        return filteredActions;
    }

}
