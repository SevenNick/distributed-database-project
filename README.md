# 分布式旅游预订系统

## 项目地址

[Github/SevenNick/distributed-database-project](https://github.com/SevenNick/distributed-database-project)

## 运行项目

> 注：以下命令均需要新开一个命令行进程执行，也可以使用 tmux 或者后台运行的方式运行，建议使用 tmux。

1. 启动 rmiregistry。切换到 src/transaction 目录下，注意该章节中所有的命令都在 src/transaction 目录下运行

   ```shell
   make runregistry
   ```

2. 启动 Transaction Manager（TM）。

   ```shell
   make runtm
   ```

3. 启动多个 Resource Manager（RM）。

   ```shell
   make runrmflights
   ```

   ```shell
   make runrmrooms
   ```

   ```shell
   make runrmcars
   ```

   ```shell
   make runrmcustomers
   ```

   ```shell
   make runrmreservations
   ```

4. 启动 Workflow Controller（WC）。

   ```shell
   make runwc
   ```

5. （可选）启动客户端运行业务逻辑。与该系统进行交互。

   ```shell
   make runclient
   ```

## 运行测试用例

1. 前置条件

   在运行测试用例之前，请确保在/usr/share/java 目录下存在以下两个 jar 包：

   - junit-4.12.jar
   - hamcrest-core-1.3.jar

2. 按照[上一章节](#运行项目)介绍的运行项目的方式启动整个系统（无需启动 client）。

3. 开启新的命令行进程，切换到 src/test 目录。根据不同的测试需求可以有五种不同的运行方式：

   1. 仅测试无故障情况下的系统功能；

      ```shell
      make run_normal_test
      ```

   2. 仅测试 RM 故障情况下的系统功能。

      ```shell
      make run_rm_failure_test
      ```

   3. 仅测试 TM 故障情况下的系统功能

      ```shell
      make run_tm_failure_test
      ```

   4. 测试整个系统故障情况下的系统功能。

      ```shell
      make run_system_failure_test
      ```

   5. 运行上述全部四种测试

      ```shell
      make run_test
      ```

> 注：在进行故障测试的过程中，系统组件会模拟故障发生并退出。为了测试能正常运行，测试人员需根据测试程序的提示在规定的时间内重启因故障而退出的系统组件。
