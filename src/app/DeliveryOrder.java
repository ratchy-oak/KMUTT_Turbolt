package app;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

final class DeliveryOrder {
    static final DateTimeFormatter ORDER_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    final int id;
    final MenuItem item;
    final CustomerLocation location;
    final RouteResult route;
    final LocalDateTime createdAt;
    final int roundIndex;
    int deliverySequence = -1;
    OrderStatus status;

    DeliveryOrder(int id, MenuItem item, CustomerLocation location, RouteResult route, LocalDateTime createdAt, int roundIndex, OrderStatus status) {
        this.id = id;
        this.item = item;
        this.location = location;
        this.route = route;
        this.createdAt = createdAt;
        this.roundIndex = roundIndex;
        this.status = status;
    }

    String displayId() {
        return String.format("TB-%03d", id);
    }

    MenuItem item() {
        return item;
    }

    CustomerLocation location() {
        return location;
    }

    RouteResult route() {
        return route;
    }

    LocalDateTime createdAt() {
        return createdAt;
    }

    int roundIndex() {
        return roundIndex;
    }

    int deliverySequence() {
        return deliverySequence;
    }

    void setDeliverySequence(int deliverySequence) {
        this.deliverySequence = deliverySequence;
    }

    OrderStatus status() {
        return status;
    }

    void setStatus(OrderStatus status) {
        this.status = status;
    }

    String statusLabel() {
        return status.label + " / รอบ " + (roundIndex + 1);
    }

    String orderTimeLabel() {
        return createdAt.format(ORDER_TIME_FORMAT);
    }

    // ประมาณเวลาของไรเดอร์ ใช้ route ที่เริ่มจากตำแหน่งปัจจุบันของไรเดอร์
    int etaMinutes(RouteResult riderRoute) {
        return riderRoute.distance() + switch (item.category()) {
            case "เครื่องดื่ม" -> 3;
            case "ของหวาน" -> 4;
            default -> 7;
        };
    }

    // คะแนนลำดับเริ่มต้น: ใช้ระยะจากร้านอาหารไปยังบ้านลูกค้า
    int priorityScore() {
        return priorityScore(route);
    }

    // คะแนนลำดับแบบกำหนด route เอง ใช้กับไรเดอร์เพื่อคิดจากตำแหน่งปัจจุบันไปยังบ้านถัดไป
    int priorityScore(RouteResult priorityRoute) {
        int distancePenalty = Math.max(0, priorityRoute.distance() / 2);
        return item.categoryBonus() - distancePenalty;
    }

    @Override
    public String toString() {
        return displayId() + " " + item.name();
    }
}
