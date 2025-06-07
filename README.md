# jzbar
Java Foreign Function & Memory bindings for
[ZBar](https://zbar.sourceforge.net/).
## Requirements
[ZBar](http://zbar.sourceforge.net/) shared library (`libzbar`) must be installed on the system.
  - On Linux: Install via your package manager (e.g., `sudo apt install libzbar-dev`)
  - On macOS: Use Homebrew (`brew install zbar`)
  - On Windows: Ensure `zbar.dll` is available in your `PATH`
This library uses the Java Foreign Function & Memory API (FFM) to dynamically link to ZBar at runtime. Per LGPL 2.1, you may replace the ZBar shared library with a modified version.
## Installation
This library is not yet published to a public Maven repository. To use it, clone the repo and install it locally using Maven:
```bash
git clone https://github.com/doblon8/jzbar
cd jzbar
mvn clean install
```
This installs the library to your local Maven repository (`~/.m2/repository`) so you can add it as a dependency in your own projects.
## License
Copyright © 2025 doblon8

Distributed under the [Apache License, Version2.0](https://www.apache.org/licenses/LICENSE-2.0).
