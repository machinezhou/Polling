package com.lawson.polling;

/**
 * Created by lawson on 16/3/29.
 * valid polling period when interruption occur
 */
public enum ValidPeriodPolicy {

  /** multiple of 2 */
  INTERVAL_S(1 << 1),
  /** multiple of 4 */
  INTERVAL_M(1 << 2),
  /** multiple of 8 */
  INTERVAL_L(1 << 3);

  final int index;

  ValidPeriodPolicy(int index) {
    this.index = index;
  }

  public static long httpInterruptionInterval(long i) {
    return i << ValidPeriodPolicy.INTERVAL_M.index;
  }

  public static long lteInsteadInterval(long i) {
    return i << ValidPeriodPolicy.INTERVAL_S.index;
  }

  public static long umtsInsteadInterval(long i) {
    return i << ValidPeriodPolicy.INTERVAL_M.index;
  }

  public static long gprsInsteadInterval(long i) {
    return i << ValidPeriodPolicy.INTERVAL_L.index;
  }

  public static long disconnectInterval(long i) {
    return i << ValidPeriodPolicy.INTERVAL_M.index;
  }

  public static long windowPauseInterval(long i) {
    return i << ValidPeriodPolicy.INTERVAL_S.index;
  }

  public static long windowDestroyInterval(long i) {
    return i << ValidPeriodPolicy.INTERVAL_M.index;
  }

  public static long exp(long i) {
    return i << ValidPeriodPolicy.INTERVAL_L.index;
  }

}
