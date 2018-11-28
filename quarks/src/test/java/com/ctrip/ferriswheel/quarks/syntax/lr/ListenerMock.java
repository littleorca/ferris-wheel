package com.ctrip.ferriswheel.quarks.syntax.lr;

import com.ctrip.ferriswheel.quarks.Symbol;
import com.ctrip.ferriswheel.quarks.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListenerMock implements LREventListener {
    static final int TYPE_BEGIN = 0;
    static final int TYPE_SHIFT = 1;
    static final int TYPE_REDUCE = 2;
    static final int TYPE_FINISH = 3;

    static class EventLog {
        int type;
        Symbol handle;
        Token token;
        List<Symbol> sequence;

        public EventLog(int type, Symbol handle, Token token, List<Symbol> sequence) {
            this.type = type;
            this.handle = handle;
            this.token = token;
            this.sequence = sequence;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EventLog) {
                EventLog e = (EventLog) obj;
                if (type != e.type) {
                    return false;
                }
                if (handle == null && e.handle != null) {
                    return false;
                }
                if (handle != null && !handle.equals(e.handle)) {
                    return false;
                }
                if (token == null && e.token != null) {
                    return false;
                }
                if (token != null && !token.equals(e.token)) {
                    return false;
                }
                if (sequence == null && e.sequence != null) {
                    return false;
                }
                if (sequence != null && !Objects.equals(sequence, e.sequence)) {
                    return false;
                }
                return true;
            }
            return false;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public Symbol getHandle() {
            return handle;
        }

        public void setHandle(Symbol handle) {
            this.handle = handle;
        }

        public Token getToken() {
            return token;
        }

        public void setToken(Token token) {
            this.token = token;
        }
    }

    List<EventLog> events = new ArrayList<>();
    StringBuilder strLog = new StringBuilder();

    @Override
    public void onBegin() {
        events.add(new EventLog(TYPE_BEGIN, null, null, null));
        strLog.append(" # BEGIN\n");
    }

    @Override
    public void onShift(Symbol handle, Token token) {
        events.add(new EventLog(TYPE_SHIFT, handle, token, null));
        strLog.append(" # SHIFT: ").append(handle).append(": ").append(token).append("\n");
    }

    @Override
    public void onReduce(Symbol handle, List<Symbol> sequence) {
        events.add(new EventLog(TYPE_REDUCE, handle, null, sequence));
        strLog.append(" # REDUCE: ").append(handle).append(": ").append(sequence).append("\n");
    }

    @Override
    public void onFinish() {
        events.add(new EventLog(TYPE_FINISH, null, null, null));
        strLog.append(" # FINISH\n");
    }

    String getStrLog() {
        return strLog.toString();
    }
}
