package app;

import java.time.LocalDateTime;
import java.util.List;

record MenuItem(String name, String category, int price, int categoryBonus) {
    @Override
    public String toString() {
        return name;
    }
}
