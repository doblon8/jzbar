# jzbar

Java Foreign Function & Memory bindings for [ZBar](https://github.com/mchehab/zbar).

## Installation

`jzbar` is available from Maven Central.

To add `jzbar` as a dependency using Maven, include the following in your `pom.xml`:

```xml
<dependency>
  <groupId>io.github.doblon8</groupId>
  <artifactId>jzbar</artifactId>
  <version>0.3.0</version>
</dependency>
```

For other build tools, retrieve the dependency from Maven Central using the same coordinates.

`jzbar` bundles the native `ZBar` library for supported platforms, so no system-wide installation is required.

## Java bindings
Java bindings were generated using [jextract](https://github.com/openjdk/jextract) on Linux (x86_64).

You can regenerate them by running the provided script from the project root (after editing it to set the correct `JEXTRACT_HOME`, `INCLUDE_DIR`, and `LIB_PATH` variables for your system):

```sh
./generate-bindings.sh
```

## License

This project is licensed under the GNU LGPL 2.1, the same as the `ZBar` library.

See the [LICENSE](./LICENSE) file for details.

