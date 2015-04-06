package com.rv.speedtest.api;

import java.io.IOException;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rv.speedtest.api.model.LaunchRequest;
import com.rv.speedtest.api.model.OutputSpeech;
import com.rv.speedtest.api.model.Response;
import com.rv.speedtest.api.model.SpeechletRequest;
import com.rv.speedtest.api.model.SpeechletResponse;

@Controller
@CommonsLog
public class MainController
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @RequestMapping (value = "/speechlet", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String genericEntryMethod(@RequestBody String request) throws JsonProcessingException
    {
        return getEntryMethod(request);
    }
    
    @RequestMapping (value = "/getspeechlet", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getEntryMethod(@RequestBody String request) throws JsonProcessingException
    {
        System.out.println("Request= " + request);
        return handleLaunchRequest(null);
    }

    private String handleLaunchRequest(LaunchRequest launchRequest) throws JsonProcessingException
    {
        SpeechletResponse speechletResponse = new SpeechletResponse();
        Response response = new Response();
        response.setShouldEndSession(true);
        speechletResponse.setResponse(response);
        OutputSpeech speechoutput = new OutputSpeech();
        response.setOutputSpeech(speechoutput);
        speechoutput.setText("Welcome");
        
        return objectMapper.writeValueAsString(speechletResponse);
    }
    
    private String handleGenericError(String message) throws JsonProcessingException
    {
        SpeechletResponse speechletResponse = new SpeechletResponse();
        Response response = new Response();
        response.setShouldEndSession(true);
        speechletResponse.setResponse(response);
        OutputSpeech speechoutput = new OutputSpeech();
        response.setOutputSpeech(speechoutput);
        speechoutput.setText(message);
        
        return objectMapper.writeValueAsString(speechletResponse);
    }
    
    @RequestMapping (value = "/speechlet2", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String genericEntryMethod2(@RequestBody String request) throws IOException
    {
        System.out.println("Request in /speechlet2= " + request);
        SpeechletRequest speechletRequest = objectMapper.readValue(request, SpeechletRequest.class);
        if (speechletRequest.getRequest() != null)
        {
            if (speechletRequest.getRequest() instanceof LaunchRequest)
            {
                return handleLaunchRequest((LaunchRequest)speechletRequest.getRequest());
            }
            return handleGenericError("Only launch request is handled till now");
        }
        return handleGenericError("Request object is null");
    }
    
}
