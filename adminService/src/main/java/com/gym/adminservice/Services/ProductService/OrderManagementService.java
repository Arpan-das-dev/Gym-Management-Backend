package com.gym.adminservice.Services.ProductService;

import org.springframework.stereotype.Service;

@Service
public class OrderManagementService {
    /*
     * as of now we don't need any order management service methods
     * because we are not building the product service now so actually we will get all the
     * order data from the product service
     * so keeping this class for future use
     */

    /*
     * it will have methods to get all the orders which are placed by customers
     * and also to get the details of a single order by order id and can accept or reject an order
     * and let the user know about the order confirmation or rejection via emailservice(microservice architecture)
     * using webclient
     * after accepting an order the status will be changed to that-> order confirmed please collect the order 
     * from the gym from admin desk as it is local set up we are not implementing shipping and delivery services
     * and also to get all the orders of a particular customer by customer id
     */
}
