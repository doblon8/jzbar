package io.github.doblon8.jzbar;

import io.github.doblon8.jzbar.utils.ImageUtils;
import io.github.doblon8.jzbar.utils.QRCodeGenerator;
import com.google.zxing.WriterException;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QRCodeScanIntegrationTest {
    private String scanQRCodeBufferedImage(BufferedImage img) throws ZBarException {
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
                throw new ZBarException("No symbols found.");
            } else if (n == 1) {
                return image.getFirstSymbol().getData();
            } else {
                throw new ZBarException("Multiple symbols found; handling not implemented.");
            }
        }
    }

    @Test
    void scanQRCodeBufferedImage() throws WriterException, ZBarException {
        String text = "Hello, World!";
        int size = 100;
        BufferedImage image = QRCodeGenerator.generateQRCodeImage(text, size);
        String scannedText = scanQRCodeBufferedImage(image);
        assertEquals(text, scannedText, "Scanned text does not match original text");

        // Test with various rotations
        int[] angles = {90, 180, 270, 45, 135, 225, 315, 58, 106, 187, 273, 341};
        for (int angle : angles) {
            BufferedImage rotatedImage = ImageUtils.rotateImage(image, angle);
            String rotatedScannedText = scanQRCodeBufferedImage(rotatedImage);
            assertEquals(text, rotatedScannedText, "Scanned text from " + angle + "-degree rotated image does not match original text");
        }
    }
}
