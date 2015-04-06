package com.rv.speedtest.api;

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
import com.rv.speedtest.api.model.OutputSpeech;
import com.rv.speedtest.api.model.Response;
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
        SpeechletResponse speechletResponse = new SpeechletResponse();
        Response response = new Response();
        response.setShouldEndSession(true);
        speechletResponse.setResponse(response);
        OutputSpeech speechoutput = new OutputSpeech();
        response.setOutputSpeech(speechoutput);
        speechoutput.setText("Welcome");
        
        return objectMapper.writeValueAsString(speechletResponse);
    }
}
