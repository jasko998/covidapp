package com.turtleshelldevelopment.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class TextQR {

    public static String getQrStringFromURI(String uri) {
        Writer qrWrite = new QRCodeWriter();
        BitMatrix bitMatrix;
        try {
            bitMatrix = qrWrite.encode(uri, BarcodeFormat.QR_CODE, 16, 16);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
        return convert(bitMatrix);
    }

    public static String convert(BitMatrix bitMatrix) {
        StringBuilder sb = new StringBuilder();
        for (int rows = 0; rows < bitMatrix.getHeight(); rows++) {
            for (int cols = 0; cols < bitMatrix.getWidth(); cols++) {
                boolean x = bitMatrix.get(rows, cols);
                if (!x) {
                    // white
                    sb.append("\033[47m  \033[0m");
                } else {
                    sb.append("\033[40m  \033[0m");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
