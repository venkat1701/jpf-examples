# Java PathFinder Examples

This repository contains a collection of example concurrency problems that can be analyzed with Java PathFinder (JPF).

JPF is an extensible software model checker for Java bytecode programs, developed at NASA Ames Research Center and now available as open source under the Apache-2.0 license. It helps detect and explain defects, collect runtime information, and verify concurrent Java applications.

These examples demonstrate how to use JPF to identify common concurrency issues like deadlocks, race conditions, and atomicity violations in realistic scenarios. This repository is designed for researchers, software developers, and students interested in learning about concurrency issues and formal verification.

## Introduction

JPF started as a model checker but has evolved into a comprehensive runtime system with multiple execution modes and extensions. It can detect concurrency defects that are difficult to find through traditional testing due to non-deterministic thread scheduling.

This repository demonstrates JPF's practical application through carefully designed examples that highlight specific concurrency challenges:

1. **DistributedCache**: Demonstrates potential deadlocks in cache invalidation operations
2. **Message Processing PubSub**: Shows race conditions in a producer-consumer message processing model

## Getting Started

### Prerequisites

To run these examples, you'll need:

- Java Development Kit (JDK) 11 or higher
- Maven 3.6.0 or higher
- Java PathFinder (JPF) Core

### Installing JPF

1. Clone the JPF Core repository:
   ```
   git clone https://github.com/javapathfinder/jpf-core.git
   ```

2. Build JPF Core:
   ```
   cd jpf-core
   ./gradlew build
   ```

3. Create or update your `~/.jpf/site.properties` file with:
   ```
   jpf-core = /path/to/your/jpf-core
   extensions=${jpf-core}
   ```

### Running the Examples

1. Clone this repository:
   ```
   git clone https://github.com/venkat1701/jpf-examples.git
   ```

2. Build the examples:
   ```
   cd jpf-examples
   mvn clean package
   ```

3. Run an example with JPF:
   ```
   java -jar $JPF_HOME/build/RunJPF.jar src/main/resources/jpf/DistributedCache.jpf
   ```

## Examples

### DistributedCache

The DistributedCache example demonstrates a classic deadlock scenario that can occur in distributed cache systems. Two threads operate on a shared cache resource, and each acquires locks in different orders, potentially leading to deadlock.

[Learn more about the DistributedCache example](docs/DistributedCache.md)

### Message Processing PubSub

The Message Processing PubSub example shows a race condition in a producer-consumer pattern. A producer thread adds messages to a queue while a consumer processes them, with a shared flag controlling synchronization. The non-atomic operations on this flag can lead to race conditions.

[Learn more about the Message Processing PubSub example](docs/MessageProcessingPubSub.md)

## Integration with JPF

Each example includes a JPF configuration file (`.jpf`) that specifies how JPF should analyze the code. These configurations demonstrate various JPF features like:

- Detecting deadlocks
- Finding race conditions
- Analyzing thread scheduling
- Tracking shared object access
- Visualizing thread interactions

## Resources

- [Java PathFinder GitHub Repository](https://github.com/javapathfinder/jpf-core)
- [JPF Wiki](https://github.com/javapathfinder/jpf-core/wiki)
- [NASA Java PathFinder Homepage](https://javapathfinder.sourceforge.net/)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.