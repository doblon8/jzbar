import com.google.zxing.WriterException;
import io.github.doblon8.jzbar.*;
import io.github.doblon8.jzbar.utils.ImageUtils;
import io.github.doblon8.jzbar.utils.QRCodeGenerator;

void main() throws WriterException, ZBarException {
    var img = QRCodeGenerator.generateQRCodeImage("Hello, World!", 100, 4);
    byte[] data = ImageUtils.convertToY800(img);

    try (ImageScanner scanner = new ImageScanner();
         Image image = new Image()) {
        // Configure the scanner to only scan QR codes
        scanner.setConfig(SymbolType.NONE, Config.ENABLE, 0);
        scanner.setConfig(SymbolType.QRCODE, Config.ENABLE, 1);

        // Set the image data
        image.setSize(img.getWidth(), img.getHeight());
        image.setFormat("Y800");
        image.setData(data);

        // Scan the image
        int n = scanner.scanImage(image);
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
