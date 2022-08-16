# AsynProxy

AsynProxy是一个使用动态代理来简化异步代码的工具.

我们经常会遇到需要使用异步线程来提高代码执行效率的情况, 但从异步线程中获取执行结果的代码非常繁琐, 例如在使用CompletableFuture的时候, 你需要声明一个变量接收CompletableFuture.supplyAsync()方法返回的对象, 然后在后续代码中通过该对象的get()方法获得异步执行的结果.

如果某段代码只需要开一个线程, 这段逻辑看起来还不错, 如果你需要开上五个类似的线程, 那情况就变得让人头疼了.

AsynProxy通过动态代理的方式, 简化了这一过程, 从变量声明的角度看, 它使你的代码像是没有开过线程一样简洁.

## 使用

### 返回值是interface的情况

下面是一个例子, 假如你需要对某段逻辑开启异步线程, 并从中获得一个List.

```
List<Integer> run = AsynProxy.proxy(() -> {
                    List<Integer> objects = new ArrayList<>();
                    objects.add(1);
                    return objects;
                }, List.class)
                .run();
                
// 其他同步逻辑...

// 程序将在.toString()的位置等待异步线程的执行 
run.toString()
```

AsynProxy.proxy()接收两个参数, 第一个参数是异步线程将要执行的代码逻辑, 第二个参数用来告诉AsynProxy, 我们想要的结果是什么类型的数据. 

异步线程中的异常, 将在执行.toString()的时候抛出. 当然我们可以对它设置一个fallback方法, 用来避免异常的抛出. 下面的例子中, 异常将会触发fallback()方法所接收的回调函数产出的结果.

```
List<Integer> run = AsynProxy.proxy(() -> {
                    List<Integer> objects = new ArrayList<>();
                    objects.add(1);
                    return objects;
                }, List.class)
                .fallback((e) -> {
                    return new ArrayList<>();
                })
                .run();
                
// 其他同步逻辑...

// 程序将在.toString()的位置等待异步线程的执行 
run.toString()
```

有时我们还需要对异步的执行时间进行限制. 下面的例子展示了, 异步线程只有500ms的执行时间. 需要注意的是, 500ms的限制, 将从run.toString()方法被调用时开始计时, 而不是AsynProxy.proxy().run()方法被调用时开始计时.

```
List<Integer> run = AsynProxy.proxy(() -> {
                    List<Integer> objects = new ArrayList<>();
                    objects.add(1);
                    return objects;
                }, List.class)
                .timeOut(500L)
                .timeUnit(TimeUnit.MILLISECONDS)
                .fallback((e) -> {
                    return new ArrayList<>();
                })
                .run();
                
// 其他同步逻辑...

// 程序将在.toString()的位置等待异步线程的执行 
run.toString()
```

有时我们需要给异步线程设置自己的线程池

```
List<Integer> run = AsynProxy.proxy(() -> {
                    List<Integer> objects = new ArrayList<>();
                    objects.add(1);
                    return objects;
                }, List.class)
                .executor(Executors.newSingleThreadExecutor())
                .run();
                
// 其他同步逻辑...

// 程序将在.toString()的位置等待异步线程的执行 
run.toString()
```

当我们获得的返回值是一个接口时, AsynProxy将使用JDK动态代理来实现.

### 返回值可为空的情况

```
Wrapper<TestA> testa = AsynProxy.proxyNullable(() -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return new TestA("wanxm");
                }, TestA.class)
                .run()
              
              
System.out.println(run.isPresent());
System.out.println(run.get());
```

由于动态代理类替换了原始的结果, 所以当你对testa做是否为空的判断, 得到的结果将永远是false, 但我们的异步逻辑, 确实有可能产生null数据, 这种情况下, AsynProxy支持使用.proxyNullable方法得到一个将结果包裹起来的中间类, 你可以对这个中间类执行.isPresent方法, 检查执行结果是否为null.

还有一种情况, 你需要使用到.proxyNullable, 即被代理类被final关键字标示, 这种类无法进行动态代理, 此时可以使用.proxyNullable, 因为该方法本质上是动态代理了Wrapper类, 所以不会出现这种问题.

## 其他特性

### 默认线程池

默认情况下, AsynProxy使用 Executors.newCachedThreadPool() 作为其工作的线程池.

### 线程名传递

AsynProxy会自动传递线程名, 内部线程的名字将会和调用AsynProxy的线程一致.
