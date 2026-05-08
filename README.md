# Turbolt

Turbolt เป็นแอปเดสก์ท็อป Java Swing สำหรับระบบเดลิเวอรี่ร้านอาหารในหมู่บ้านหอยทาก ใช้ข้อมูลเมนูจากรูปที่ให้มา แผนที่หมู่บ้านแบบมีน้ำหนัก การหาเส้นทางด้วย Dijkstra และการจัดคิวออเดอร์สำหรับลูกค้า ร้านค้า และผู้จัดส่ง

## วิธีรัน

รันจากโฟลเดอร์โปรเจกต์:

```bash
javac src/app/*.java src/datastruct/Queue.java src/structures/graph/Graph.java src/structures/graph/Edge.java src/structures/tree/*.java
java -cp src app.TurboltApp
```

หน้าต่างแอปจะเปิดขึ้นพร้อม 3 แท็บ: ลูกค้า, ร้านค้า และผู้จัดส่ง
