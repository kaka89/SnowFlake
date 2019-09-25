# SnowFlake

Twitter 的 雪花算法 算法的 Java 实现版本。

## 算法思路

`8字节（64位）占位的模式`

主要分为以下四个部分：`时间序列|数据中心序列|机器序列|自增序列`

在当前代码中的大小设置如下：

- 时间序列：42 位，每 139 年开始重复
- 数据中心序列：5 位，共能承载 32 个数据中心
- 机器序列：5 位，共能承载 32 台机器（ID 生成机器）
- 自增序列：12 位，值的范围为：0 ～ 4095

## 算法调整说明

```text
  在实际开发中可以根据自己的需要去调整每个序列的占位比，如 DC 可能不会有那么多，而机器会偏多，此时可扩宽机器序列位，减少 DC 序列位。同样的，可以为了简便，采用 int 型（4 字节，32 位）来处理，或者采用 String 灵活调节字节数。
```

## 测试运行

- 编译：`javac SnowFlake.java`
- 运行：`java SnowFlake`

## 注意事项

注意不同机器、不同 DC 需要配置不同的值序列号，如果配置了相同的值，就会造成长生相同 ID。

PS: 很多人在灰度发布、或者实例扩容的时候容易忘记更改机器的序列号。
