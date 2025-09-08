package com.gym.adminservice.Utils;

import java.text.Normalizer;

import org.springframework.stereotype.Component;

@Component
/*
 * this utility class generates a unique product code based on the product's
 * name, category, and brand.
 * and then add custom id for each product and their variations
 */
public class ProductIDGenUtil {

    public  String genProductCode(String productName, String category, String brand) {
        /* Extract first three letters from each input, normalize to remove spaces and
        special characters  */
        String namePart = normalize(productName).substring(0, Math.min(3, normalize(productName).length()))
                .toUpperCase();

        /* Extract first three letters from each input, normalize to remove spaces and
         special characters */
        String catPart = normalize(category).substring(0, Math.min(3, normalize(category).length())).toUpperCase();

        /* Extract first three letters from each input, normalize to remove spaces and
         special characters */
        String brandPart = normalize(brand).substring(0, Math.min(3, normalize(brand).length())).toUpperCase();

        return namePart + "-" + catPart + "-" + brandPart;
    }

    // Normalize the input by removing spaces and special characters
    private  String normalize(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^a-zA-Z0-9]", ""); // remove spaces & special chars
    }

    public String generateProductId(String productCode, String flavourName) {
        int hash = Math.abs(flavourName.hashCode());
        int numeric = hash % 1000; // keep it 3 digits
        return String.format("%s%03d", productCode, numeric);
    }
}
