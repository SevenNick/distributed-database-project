# 分布式数据库项目

组长：谢思豪

组员：麻俊特 19210240038，

## 运行项目

（注意：以下命令均需要在新开一个命令行进程执行，也可以使用tmux或者后台运行的方式运行，建议使用tmux）

1. 启动rmiregistry。切换到src/transaction目录下，注意该章节中所有的命令都在src/transaction目录下运行

```shell
make runregistry
```

2. 启动Transaction Manger（TM）。

```shell
make runtm
```

3. 启动多个Resource Manager（RM）。

```shell
make runrmcars
```
```shell
make runrmflights
```
```shell
make runrmrooms
```
```shell
make runrmcustomers
```
```shell
make runrmreservations
```

4. 启动Workflow Controller（WC）。

```shell
make runwc
```

5. （可选）可以再启动客户端来与该数据库进行交互。

```shell
make runclient
```

通过以上方式，分布式数据库便已经完成启动。

## 运行测试用例

1. 按照上一章节（**运行项目**）的方式的方式启动分布式数据库。

2. 开启新的命令行进程，切换到src/test目录。通过不同的方式运行不同的测试用例：

   1. 普通的功能测试；

   ```shell
   make run_normal_test
   ```

   2. RM鲁棒性测试。注意在测试的过程中，会出现RM的退出，需要到对应的命令行下对该退出的RM进行重启，以完成测试。

    ```shell
   make run_rm_failure_test
    ```

   3. TM鲁棒性测试。注意在测试的过程中，会出现TM的退出，需要到对应的命令行下进行TM重启，以完成测试。

    ```shell
   make run_tm_failure_test
    ```

   4. 系统整体鲁棒性测试。注意在测试的过程中，所有的进程均会退出，需要到对应的命令行下分别进行重启，以完成测试。

    ```shell
   make run_system_failure_test
    ```

以上测试覆盖了绝大部分分布式数据库系统对于ACID的要求。