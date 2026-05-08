package app;

import java.time.LocalDateTime;
import java.util.List;

enum OrderStatus {
    WAITING("รอจัดเตรียม"),
    PREPARING("กำลังเตรียม"),
    OUT_FOR_DELIVERY("กำลังจัดส่ง"),
    DELIVERED("ส่งสำเร็จ");

    final String label;

    OrderStatus(String label) {
        this.label = label;
    }
}
