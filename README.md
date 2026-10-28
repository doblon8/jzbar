# jzbar

Java Foreign Function & Memory bindings for [ZBar](https://github.com/mchehab/zbar).

## Installation

`jzbar` is available from Maven Central.

To add `jzbar` as a dependency using Maven, include the following in your `pom.xml`:

```xml
<dependency>
  <groupId>io.github.doblon8</groupId>
  <artifactId>jzbar</artifactId>
  <version>0.5.0</version>
</dependency>
```

For other build tools, retrieve the dependency from Maven Central using the same coordinates.

`jzbar` bundles the native `ZBar` library for supported platforms, so no system-wide installation is required.

## Usage example

```java
import com.google.zxing.WriterException;
import io.github.doblon8.jzbar.*;
import io.github.doblon8.jzbar.utils.ImageUtils;
import io.github.doblon8.jzbar.utils.QRCodeGenerator;

void main() throws WriterException, ZBarException {

    System.out.println("ZBar version: " + ZBar.version());

    var img = QRCodeGenerator.generateQRCodeImage("Hello, World!", 100, 4);
    var data = ImageUtils.convertToY800(img);

    try (var scanner = new ImageScanner();
         var image = new Image()) {
        // Configure the scanner to only scan QR codes
        scanner.setConfig(SymbolType.NONE, Config.ENABLE, 0);
        scanner.setConfig(SymbolType.QRCODE, Config.ENABLE, 1);

        // Set the image data
        image.setSize(img.getWidth(), img.getHeight());
        image.setFormat("Y800");
        image.setData(data);

        // Scan the image
        var n = scanner.scanImage(image);
        if (n == 0) {
            System.err.println("No symbols found.");
        } else if (n == 1) {
            var symbol = image.getFirstSymbol();
            System.out.println("Scanned data: " + symbol.getData());
            System.out.println("Symbol type: " + symbol.getType());
            System.out.println("Symbol quality: " + symbol.getQuality());
            System.out.println("Symbol location: " + symbol.getLocationPolygon());
        } else {
            System.err.println("Multiple symbols found; handling not implemented.");
        }
    }
}

// ZBar version: 0.23.93
// Scanned data: Hello, World!
// Symbol type: QR-Code
// Symbol quality: 1
// Symbol location: [Point[x=18, y=18], Point[x=18, y=81], Point[x=81, y=81], Point[x=81, y=18]]
```

## License

jzbar is free software, licensed under the GNU Lesser General Public License, version 2.1 only (SPDX: `LGPL-2.1-only`).

Copyright (C) 2025 doblon8 and contributors

See the [LICENSE](./LICENSE) file for the license text, and [THIRD-PARTY-NOTICES](./THIRD-PARTY-NOTICES) for the licenses of the bundled native libraries.

jzbar bundles prebuilt native ZBar libraries. To use your own build instead, put `libzbar.so`, `libzbar.dylib` or `zbar.dll` (with `libiconv-2.dll`) in a directory on `java.library.path`. [BUILDING.md](./BUILDING.md) explains how to rebuild the bundled libraries from the source bundle published with each release.
