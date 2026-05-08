package app;

import java.time.LocalDateTime;
import java.util.List;

record BatchResult(List<DeliveryOrder> orders, String message) {
}
