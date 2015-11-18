package com.rv.speedtest.api;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.rv.speedtest.datastore.DynamoDBStorage;
import com.rv.speedtest.datastore.Storage;


@Configuration 
@ComponentScan("com.rv.speedtest") 
@EnableWebMvc
@CommonsLog
public class AppConfig
{
    @Bean
    public Storage getStorage()
    {
        String awsAccessKeyId = System.getProperty("aws.dynamodb.accessKeyId");
        String awsSecretAccessKey = System.getProperty("aws.dynamodb.secretAccessKey");
        AWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
        AmazonDynamoDB dynamo = new AmazonDynamoDBClient(awsCredentials);
        DynamoDBMapper mapper = new DynamoDBMapper(dynamo);
        return new DynamoDBStorage(mapper); 
    }
} 
