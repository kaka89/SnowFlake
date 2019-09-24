
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

/**
 * twitter的snowflake算法 -- java版本
 */

public class SnowFlake {

  /**
   * 起始的时间戳 2018-08-20
   */
  private static final long START_TIMESTAMP = 1534694400000L;

  /**
   * 每一部分占用的位数
   */
  private static final long SEQUENCE_BIT = 12; // 序列号占用的位数
  private static final long MACHINE_BIT = 5; // 机器标识占用的位数
  private static final long DATA_CENTER_BIT = 5;// 数据中心占用的位数

  /**
   * 每一部分的最大值
   */
  private static final long MAX_DATA_CENTER_NUM = -1L ^ (-1L << DATA_CENTER_BIT);
  private static final long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
  private static final long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

  /**
   * 每一部分向左的位移
   */
  private static final long MACHINE_LEFT = SEQUENCE_BIT;
  private static final long DATA_CENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
  private static final long TIMESTAMP_LEFT = DATA_CENTER_LEFT + DATA_CENTER_BIT;
  private static final int ONE = 1;

  private long dataCenterId; // 数据中心
  private long machineId; // 机器标识
  private LongAdder sequence = new LongAdder(); // 序列号
  private volatile long lastTimestamp = -1L;// 上一次时间戳

  /**
   * constructor
   *
   * @param dataCenterId
   * @param machineId
   */
  public SnowFlake(long dataCenterId, long machineId) {
    if (dataCenterId > MAX_DATA_CENTER_NUM || dataCenterId < 0) {
      throw new IllegalArgumentException("dataCenterId can't be greater than MAX_DATA_CENTER_NUM or less than 0");
    }
    if (machineId > MAX_MACHINE_NUM || machineId < 0) {
      throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
    }
    this.dataCenterId = dataCenterId;
    this.machineId = machineId;
  }

  /**
   * 产生下一个ID
   *
   * @return new value
   * @throws RuntimeException
   */
  public synchronized long nextId() {
    long curStamp = getNewTimestamp();
    if (curStamp < lastTimestamp) {
      throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
    }

    if (curStamp == lastTimestamp) {
      // 相同毫秒内，序列号自增
      sequence.add(ONE);
      // 同一毫秒的序列数已经达到最大
      if (sequence.longValue() >= MAX_SEQUENCE) {
        curStamp = getNextMill();
        sequence.reset();
      }
    } else {
      // 不同毫秒内，序列号置为0
      sequence.reset();
    }

    lastTimestamp = curStamp;
    return (curStamp - START_TIMESTAMP) << TIMESTAMP_LEFT // 时间戳部分
        | dataCenterId << DATA_CENTER_LEFT // 数据中心部分
        | machineId << MACHINE_LEFT // 机器标识部分
        | sequence.longValue(); // 序列号部分
  }

  /**
   * 获取下一个毫秒时间戳
   *
   * @return next mill
   */
  private long getNextMill() {
    long mill = getNewTimestamp();
    while (mill <= lastTimestamp) {
      mill = getNewTimestamp();
    }
    return mill;
  }

  /**
   * 获取最新的时间戳
   *
   * @return current time millis
   */
  private long getNewTimestamp() {
    return System.currentTimeMillis();
  }

  public static void main(String[] args) {
    SnowFlake snowFlake = new SnowFlake(1, 1);
    Set<String> ids = new HashSet<>();
    for (int i = 0; i < 1000000; i++) {
      long id = snowFlake.nextId();
      ids.add(String.valueOf(id));
      System.out.println(id);
    }
    System.out.println(ids.size());
  }

}
