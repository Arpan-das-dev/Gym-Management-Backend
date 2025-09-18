package com.gym.notificationservice.Config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
/**
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sns.SnsClient;
*/
//@Configuration
public class AwsConfig {
    /*
     * The following AWS clients are currently **not in use** because we are not using
     * AWS SES (Simple Email Service) and SNS (Simple Notification Service) for sending emails/SMS.
     * Hence, these beans are commented out to prevent Spring from trying to initialize them.
     */
    //private static final String AWS_ACCESS_KEY = "YOUR_ACCESS_KEY";
    //  private static final String AWS_SECRET_KEY = "YOUR_SECRET_KEY";
    //AwsBasicCredentials basicCredentials = new AwsBasicCredentials(AWS_ACCESS_KEY,AWS_SECRET_KEY);
    // Bean for AWS SES client
    // Used to send emails via Amazon Simple Email Service
  /*  @Bean
    public SesClient sesClient(){
        return SesClient.builder()
                .region(Region.AF_SOUTH_1)
                .build();
    }

 // Bean for AWS SNS client
    // Used to send SMS or push notifications via Amazon SNS
    @Bean
    public SnsClient snsClient(){
        return SnsClient.builder()
                .region(Region.AF_SOUTH_1)
                .build();
    }
    */
}
