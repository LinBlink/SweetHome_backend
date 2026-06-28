package asia.sweethome.health.constant;

import java.util.List;

/**
 * 健康指标类型常量
 *
 * @author: LOCRIAN_V
 * @date: 7/19/2026
 */
public class HealthMetricConstant {

    public static final String HEIGHT = "HEIGHT";
    public static final String WEIGHT = "WEIGHT";
    public static final String BLOOD_PRESSURE = "BLOOD_PRESSURE";

    public static final List<String> TYPE_LIST = List.of(HEIGHT, WEIGHT, BLOOD_PRESSURE);
}
