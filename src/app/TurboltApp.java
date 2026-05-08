package app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class TurboltApp {
    // จุดเริ่มต้นของโปรแกรม: เปิดหน้าต่าง Swing ของแอป Turbolt
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            TurboltFrame frame = new TurboltFrame(new DeliverySystem());
            frame.setVisible(true);
        });
    }
}
