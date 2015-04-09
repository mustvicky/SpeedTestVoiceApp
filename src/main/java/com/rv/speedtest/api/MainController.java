package com.rv.speedtest.api;

import java.io.IOException;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.rv.speedtest.api.model.LaunchRequest;
import com.rv.speedtest.api.model.OutputSpeech;
import com.rv.speedtest.api.model.Response;
import com.rv.speedtest.api.model.SpeechletRequest;
import com.rv.speedtest.api.model.SpeechletResponse;

@Controller
@CommonsLog
public class MainController {
	private static final String GCM_SEND_URL = "https://android.googleapis.com/gcm/send";
	private static final String AUTH_KEY = "AIzaSyBpDJsDuAaroobcxArYGIPzF9G5KudlAaA";
	private static final String REG_ID = "APA91bEpA-bLwPn-rbX7wvv8bl7XuoakrZqL8ubCv8ECtlx6domBPctuN6kWtEd1fdOPAX8RtCeIwrbDpc9_3ljQuU5L6lFuNusjzErAKARGkp-dCh8KZAqidBNcB5RHp1yGzLiOujICTjU4gd2UpaVMxERpHwQlxg";
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@RequestMapping(value = "/speechlet", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String genericEntryMethod(@RequestBody String request)
			throws JsonProcessingException {
		// Send message
		try {
			sendPushMessage();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// return getEntryMethod(request);
		return "String";
	}

	// TODO: Rohan Joshi 04092015 Add a method which can be called by the mobile
	// app to store pin -> registration Id.
	// TODO: Rohan Joshi 04092015 Add a method which is invoked by Alexa and
	// which sends a dummmy push message to the mobile application. There will
	// only be 1 kind of push message which needs to be sent to the app which
	// would be a ping for the phone to start the download speed testing.
	// TODO: Rohan Joshi 04092015 Add a method which the app can call with the
	// download speed. This method also invokes TTS so that the output is spoken
	// to the user.

	private void sendPushMessage() throws IOException {
		Sender sender = new Sender(AUTH_KEY);
		Message.Builder builder = new Message.Builder();
		builder.addData("key", "value");
		Message messageToSend = builder.build();
		sender.send(messageToSend, REG_ID, 2);
	}

	@RequestMapping(value = "/getspeechlet", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String getEntryMethod(@RequestBody String request)
			throws JsonProcessingException {
		System.out.println("Request= " + request);
		return handleLaunchRequest(null);
	}

	private String handleLaunchRequest(LaunchRequest launchRequest)
			throws JsonProcessingException {
		SpeechletResponse speechletResponse = new SpeechletResponse();
		Response response = new Response();
		response.setShouldEndSession(true);
		speechletResponse.setResponse(response);
		OutputSpeech speechoutput = new OutputSpeech();
		response.setOutputSpeech(speechoutput);
		speechoutput.setText("Welcome");

		return objectMapper.writeValueAsString(speechletResponse);
	}

	private String handleGenericError(String message)
			throws JsonProcessingException {
		SpeechletResponse speechletResponse = new SpeechletResponse();
		Response response = new Response();
		response.setShouldEndSession(true);
		speechletResponse.setResponse(response);
		OutputSpeech speechoutput = new OutputSpeech();
		response.setOutputSpeech(speechoutput);
		speechoutput.setText(message);

		return objectMapper.writeValueAsString(speechletResponse);
	}

	@RequestMapping(value = "/speechlet2", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String genericEntryMethod2(@RequestBody String request)
			throws IOException {
		System.out.println("Request in /speechlet2= " + request);
		SpeechletRequest speechletRequest = objectMapper.readValue(request,
				SpeechletRequest.class);
		if (speechletRequest.getRequest() != null) {
			if (speechletRequest.getRequest() instanceof LaunchRequest) {
				return handleLaunchRequest((LaunchRequest) speechletRequest
						.getRequest());
			}
			return handleGenericError("Only launch request is handled till now");
		}
		return handleGenericError("Request object is null");
	}

}
