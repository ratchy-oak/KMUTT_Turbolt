package app;

import java.time.LocalDateTime;
import java.util.List;

record CustomerLocation(String name, String nodeId) {
    @Override
    public String toString() {
        return name;
    }
}
