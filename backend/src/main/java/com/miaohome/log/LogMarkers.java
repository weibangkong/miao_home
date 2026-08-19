package com.miaohome.log;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * 日志标记常量
 * 用于区分业务日志与程序日志，配合 logback 结构化 JSON 输出。
 *
 * <pre>{@code
 * // 业务日志（输出中包含 "marker":"BIZ"）
 * log.info(LogMarkers.BIZ, "猫咪认养成功: catId={}, adopterId={}", catId, adopterId);
 *
 * // 程序日志（无 marker）
 * log.debug("查询猫咪列表耗时: {}ms", elapsed);
 * }</pre>
 *
 * @author weibang kong
 */
public final class LogMarkers {

    /** 业务操作标记 */
    public static final Marker BIZ = MarkerFactory.getMarker("BIZ");

    private LogMarkers() {
        // 工具类不允许实例化
    }
}
