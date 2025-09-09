package com.gym.adminservice.Utils;

import org.springframework.stereotype.Component;

@Component
/*
 * this utility class is responsible for generating unique plan ids
 */
public class PlanIdGenUtil {

    /*
     * this is the method to generate unique plan ids
     * it takes plan name, duration and price as input parameters
     * it normalizes the plan name by converting it to lowercase and trimming
     * whitespace
     * it computes hash codes for the normalized plan name, duration and price
     * it combines these hash codes using a formula to produce a unique hash code
     * it converts the combined hash code to a string and returns it as the unique
     * plan id
     */
    public String generatePlanId(String planName, Integer duration, Double price) {
        String normalizedName = planName.toLowerCase().trim(); // Normalize the plan name by converting to lowercase and
                                                               // trimming whitespace

        /*
         * Computing hash codes for each attribute
         * Using all the Primitives wrapper classes to get the hash codes
         * to avoid any null pointer exceptions
         */
        int nameHash = String.valueOf(normalizedName).hashCode();
        int durationHash = Integer.valueOf(duration).hashCode();
        int priceHash = Double.valueOf(price).hashCode();

        int combinedHash = Math.abs((priceHash * 31 + nameHash) * 31 + durationHash); // Combining the hash codes using
                                                                                      // a formula and taking absolute
                                                                                      // value to ensure non-negative
                                                                                      // hash codes
                                                                                      
        return Integer.toString(combinedHash); // Converting the combined hash code to a string and returning as the
                                               // unique plan id
    }
}
