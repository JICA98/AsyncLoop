# AsyncLoop
[![Apache License](https://img.shields.io/github/license/JICA98/async-spb)](https://github.com/JICA98/async-spb/blob/psycho/LICENSE)

The AsyncLoop library provides utilities to simplify the usage of ActiveJ promises and asynchronous operations in Java applications. It offers a set of methods to run tasks on an event loop, retrieve results from promises, and handle exceptions more conveniently.

## Installation

To use the AsyncLoop library in your Java project, you can add the following Maven dependency:

```xml
<dependency>
    <groupId>jica.spb.asyncLoop</groupId>
    <artifactId>async-loop-spb</artifactId>
    <version>0.0.1</version>
</dependency>
```

## Usage

The main entry point of the library is the AsyncLoop class, which provides various methods for running tasks and handling asynchronous operations. Here are the public methods exposed by the AsyncLoop class:

* `run(Runnable runnable)`
Run a single Runnable task on the event loop.

```java
AsyncLoop asyncLoop = new AsyncLoop();
asyncLoop.run(() -> {
    // Your code here
});
```

* `run(Collection<Runnable> runnables)`
Run a collection of Runnable tasks on the event loop.

```java
List<Runnable> tasks = Arrays.asList(
    () -> { /* Task 1 */ },
    () -> { /* Task 2 */ }
);

AsyncLoop asyncLoop = new AsyncLoop();
asyncLoop.run(tasks);
```
* `get(Supplier<T> supplier)`
Retrieve a result using a Supplier on the event loop.

```java
AsyncLoop asyncLoop = new AsyncLoop();
Result<Integer> result = asyncLoop.get(() -> 42);
```
* `accept(ConsumerWrapper<T> wrapper)`
Run a consumer task using a ConsumerWrapper on the event loop.

```java
AsyncLoop asyncLoop = new AsyncLoop();
ConsumerWrapper<String> consumerWrapper = ConsumerWrapper.of(text -> System.out.println(text), "Hello, world!");
Result<Void> result = asyncLoop.accept(consumerWrapper);
```
* `apply(FunctionWrapper<I, O> wrapper)`
Run a function task using a FunctionWrapper on the event loop.

```java
AsyncLoop asyncLoop = new AsyncLoop();
FunctionWrapper<Integer, String> functionWrapper = FunctionWrapper.of(number -> "Result: " + number, 42);
Result<String> result = asyncLoop.apply(functionWrapper);
```
For more details on all available methods and their usage, refer to the library's JavaDoc.

## Examples
Here are a few usage examples of the AsyncLoop library:

Example 1: Running Tasks
```java
AsyncLoop asyncLoop = new AsyncLoop();
asyncLoop.run(() -> System.out.println("Task executed on event loop"));
```
Example 2: Getting Results
```java
AsyncLoop asyncLoop = new AsyncLoop();
Result<Integer> result = asyncLoop.get(() -> 42);
result.whenValue(value -> System.out.println("Value: " + value));
result.whenException(exception -> System.err.println("Exception: " + exception));
```
Example 3: Running Consumer Tasks
```java
AsyncLoop asyncLoop = new AsyncLoop();
ConsumerWrapper<String> consumerWrapper = ConsumerWrapper.of(text -> System.out.println("Received: " + text), "Hello!");
Result<Void> result = asyncLoop.accept(consumerWrapper);
```
Example 4: Running Function Tasks
```java
AsyncLoop asyncLoop = new AsyncLoop();
FunctionWrapper<Integer, String> functionWrapper = FunctionWrapper.of(number -> "Result: " + number, 42);
Result<String> result = asyncLoop.apply(functionWrapper);
```
Example 5: Executing a plusOne function
```java
class Main {
    public static void main(String[] args) {
        AsyncLoop asyncLoop = new AsyncLoop();
        Stream<Integer> integers = Stream.of(1, 3, 4);
        asyncLoop.apply(integers.map(FunctionWrapper.of(Main::plusOne)))
                .whenException(System.out::println)
                .values()
                .forEach(System.out::println);

    }

    private static int plusOne(int number) {
        return number + 1;
    }
}
```