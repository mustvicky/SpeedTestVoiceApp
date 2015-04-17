package com.rv.speedtest.api;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.rv.speedtest.datastore.InMemoryStorage;
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
        return new InMemoryStorage(); 
    }
} 
