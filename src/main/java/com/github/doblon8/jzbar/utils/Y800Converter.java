package com.github.doblon8.jzbar.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class Y800Converter {
    public static byte[] toY800(BufferedImage image) {
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
}
