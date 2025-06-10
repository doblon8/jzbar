package com.github.doblon8.jzbar.utils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ImageUtils {
    public static byte[] convertToY800(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            throw new IllegalArgumentException("Image must be of type TYPE_BYTE_GRAY");
        }

        // Directly extract the byte data from the BufferedImage
        byte[] imageData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        // Create a new byte array for Y800 format
        int width = image.getWidth();
        int height = image.getHeight();
        byte[] y800Data = new byte[width * height];

        // Copy pixel data to Y800 format
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelIndex = y * width + x;
                int pixelValue = imageData[pixelIndex] & 0xFF; // Convert signed byte to unsigned int
                y800Data[pixelIndex] = (byte) pixelValue;
            }
        }

        return y800Data;
    }

    public static BufferedImage rotateImage(BufferedImage image, double degrees) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage rotated = new BufferedImage(width, height, image.getType());
        Graphics2D g2d = rotated.createGraphics();

        double radians = Math.toRadians(degrees);
        AffineTransform affineTransform = AffineTransform.getRotateInstance(radians, width / 2.0, height / 2.0);
        g2d.setTransform(affineTransform);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return rotated;
    }
}
