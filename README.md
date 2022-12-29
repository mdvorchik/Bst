## Источник
https://hal.inria.fr/tel-01887505/document
## Верификация корректности

1) Дублируем все операции на последовательное BstSeq под глобальным локом
2) Проверяем, что отсортированные inorder traversal BstSeq и BstPar совпадают

## Результаты 5 запусков

```
1)
Threads count: 1
x: 0	ops: 1619045
x: 10	ops: 1639442
x: 50	ops: 1111897

Threads count: 2
x: 0	ops: 3182762
x: 10	ops: 2662500
x: 50	ops: 2065860

Threads count: 3
x: 0	ops: 4217288
x: 10	ops: 3833764
x: 50	ops: 2896927

Threads count: 4
x: 0	ops: 4953685
x: 10	ops: 4416863
x: 50	ops: 3672235

2)
Threads count: 1
x: 0	ops: 2072730
x: 10	ops: 1446208
x: 50	ops: 1260555

Threads count: 2
x: 0	ops: 3036683
x: 10	ops: 2836884
x: 50	ops: 2249295

Threads count: 3
x: 0	ops: 4449576
x: 10	ops: 3886642
x: 50	ops: 3099316

Threads count: 4
x: 0	ops: 4970787
x: 10	ops: 4647514
x: 50	ops: 3697439

3)
Threads count: 1
x: 0	ops: 2008062
x: 10	ops: 1418314
x: 50	ops: 1026953

Threads count: 2
x: 0	ops: 3471991
x: 10	ops: 2985176
x: 50	ops: 2284090

Threads count: 3
x: 0	ops: 4378097
x: 10	ops: 3905783
x: 50	ops: 3064755

Threads count: 4
x: 0	ops: 4867070
x: 10	ops: 4447839
x: 50	ops: 3545906

4)
Threads count: 1
x: 0	ops: 1965716
x: 10	ops: 1434594
x: 50	ops: 1074145

Threads count: 2
x: 0	ops: 2908682
x: 10	ops: 2809623
x: 50	ops: 1833614

Threads count: 3
x: 0	ops: 4221190
x: 10	ops: 3973163
x: 50	ops: 3078119

Threads count: 4
x: 0	ops: 5059973
x: 10	ops: 4445725
x: 50	ops: 3508544

5)
Threads count: 1
x: 0	ops: 2090893
x: 10	ops: 1411618
x: 50	ops: 1128954

Threads count: 2
x: 0	ops: 3304247
x: 10	ops: 2997141
x: 50	ops: 2228423

Threads count: 3
x: 0	ops: 3854334
x: 10	ops: 3137687
x: 50	ops: 3071059

Threads count: 4
x: 0	ops: 5260638
x: 10	ops: 4659696
x: 50	ops: 3487504

```
## Усредненные результаты

| x / thread_count | 1        | 2        | 3        | 4        | 4 / 1 |
|------------------|----------|----------|----------|----------|-------|
| 0                | 1951289  | 3180873  | 4224097  | 5022430  | 2.57  |
| 10               | 1470035  | 2858264  | 3747407  | 4523527  | 3.08  |
| 50               | 1120500  | 2132256  | 3042035  | 3582325  | 3.20  |

Среднее ускорение: 2.95