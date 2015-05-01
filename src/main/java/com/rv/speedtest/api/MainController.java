package com.rv.speedtest.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import lombok.Data;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.id.UUIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rv.speedtest.api.model.LaunchRequest;
import com.rv.speedtest.api.model.OutputSpeech;
import com.rv.speedtest.api.model.RegisterDeviceRequest;
import com.rv.speedtest.api.model.RegisterDeviceResponse;
import com.rv.speedtest.api.model.ReportNetworkSpeedRequest;
import com.rv.speedtest.api.model.ReportNetworkSpeedResponse;
import com.rv.speedtest.api.model.Response;
import com.rv.speedtest.api.model.SpeechletRequest;
import com.rv.speedtest.api.model.SpeechletResponse;
import com.rv.speedtest.api.model.User;
import com.rv.speedtest.datastore.Storage;
import com.rv.speedtest.datastore.model.CustomerRequestState;
import com.rv.speedtest.datastore.model.CustomerState;
import com.rv.speedtest.datastore.model.NetworkSpeedRequest;
import com.rv.speedtest.datastore.model.NetworkSpeedResponse;
import com.rv.speedtest.gcm.server.Message;
import com.rv.speedtest.gcm.server.Message.Builder;
import com.rv.speedtest.gcm.server.Result;
import com.rv.speedtest.gcm.server.Sender;

@Controller
@CommonsLog
public class MainController {
	private static final String APP_VUI_NAME = "Phone finder";
	private static final String APP_ANDROID_NAME = "Alexa Phone finder";
    private static final int FIVE_MINS_MILLIS = 60*5*1000;
    private static final String GCM_SEND_URL = "https://android.googleapis.com/gcm/send";
	private static final String AUTH_KEY = "AIzaSyBpDJsDuAaroobcxArYGIPzF9G5KudlAaA";
	private static final String REG_ID = "APA91bEpA-bLwPn-rbX7wvv8bl7XuoakrZqL8ubCv8ECtlx6domBPctuN6kWtEd1fdOPAX8RtCeIwrbDpc9_3ljQuU5L6lFuNusjzErAKARGkp-dCh8KZAqidBNcB5RHp1yGzLiOujICTjU4gd2UpaVMxERpHwQlxg";
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	private static final String ANDROID_APP_SPOKEN_NAME = "Speed Test";
	
	private static final String REQUEST_ID = "requestId";
	
	@Autowired
	private Storage storageInstance;

	@RequestMapping(value = "/speechlet", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String genericEntryMethod(@RequestBody String request)
			throws JsonProcessingException {
		// Send message
		try {
			//sendPushMessage("messageId");
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

	private String sendPushMessage(String mobileRegistrationId,
			HashMap<String, String> payload) throws IOException
	{
		Sender sender = new Sender(AUTH_KEY);
		Message.Builder builder = new Message.Builder();
		addPayloadForMessage(builder, payload);
		Message messageToSend = builder.build();
		Result result = sender.send(messageToSend, mobileRegistrationId, 2);
		return result.getMessageId();
	}

	private void addPayloadForMessage(Builder builder,
			HashMap<String, String> payload)
	{
		for (Entry<String, String> entry : payload.entrySet())
		{
			builder.addData(entry.getKey(), entry.getValue());
		}
	}

	@RequestMapping(value = "/reportNetworkSpeed", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
	public String reportNetworkSpeed(@RequestBody String request) throws IOException
	{
	    ReportNetworkSpeedRequest networkSpeed = objectMapper.readValue(request, ReportNetworkSpeedRequest.class);
	    System.out.println("Speed : "+networkSpeed.getNetworkSpeedInKb());
	    CustomerRequestState requestState = storageInstance.getCustomerRequestState(networkSpeed.getMessageId());
	    ReportNetworkSpeedResponse networkSpeedResponse = new ReportNetworkSpeedResponse();
	    if (requestState == null)
	    {
	        networkSpeedResponse.setError("Couldn't find any pending request");
	        return objectMapper.writeValueAsString(networkSpeedResponse);
	    }
	    NetworkSpeedResponse speedResponse = new NetworkSpeedResponse();
	    speedResponse.setDownloadSpeedInKB(networkSpeed.getNetworkSpeedInKb());
	    requestState.setNetworkSpeedResponse(speedResponse);
	    return objectMapper.writeValueAsString(networkSpeedResponse);
	}
	
	@RequestMapping(value = "/registerDevice", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
	public String registerDevice(@RequestBody String request) throws IOException {
	    RegisterDeviceRequest registerDeviceRequest = objectMapper.readValue(request, RegisterDeviceRequest.class);
	    CustomerState state = storageInstance.getCustomerStateFromInviteCode(registerDeviceRequest.getInvitationCode());
	    RegisterDeviceResponse response = new RegisterDeviceResponse();
	    if (state == null)
	    {
	        response.setError("App couldn't identify your invitation code: " + registerDeviceRequest.getInvitationCode());
	    }
	    else if (state.getMobileRegistrationId() != null)
	    {
	        response.setError("Your mobile is already paired.");
	    }
	    else 
	    {
	        state.setMobileRegistrationId(registerDeviceRequest.getMobileRegistrationId());
	    }
	    return objectMapper.writeValueAsString(response);
	}

	private String handleLaunchRequest(SpeechletRequest speechletRequest)
			throws IOException {
		SpeechletResponse speechletResponse = new SpeechletResponse();
		Response response = new Response();
		response.setShouldEndSession(true);
		speechletResponse.setResponse(response);
		OutputSpeech speechoutput = new OutputSpeech();
		response.setOutputSpeech(speechoutput);
		StringBuilder outputStringBuilder = new StringBuilder();
		CustomerState customerState = null;
		User user = speechletRequest.getSession().getUser();
		customerState = storageInstance.getCustomerStateFromUserId(user.getUserId());
		
		if (customerState == null)
		{
		    outputStringBuilder.append("Welcome to " + APP_VUI_NAME + ". ");
		    customerState = new CustomerState();
		    InvitationCode invitationCode = generateRandomInvitationCode();
		    customerState.setInvitationCode(invitationCode.getGuiCode());
		    customerState.setInvitationVuiCode(invitationCode.getVuiCode());
		    customerState.setInvitationExpiryTimeMillis(System.currentTimeMillis() + FIVE_MINS_MILLIS);
		    customerState.setUserId(user.getUserId());
		    storageInstance.createCustomerState(customerState);
		    outputStringBuilder.append("You need to pair a companion android app in order to use " + APP_VUI_NAME + ". Please install " 
		            + APP_ANDROID_NAME + " from google android playstore and provide " + invitationCode.getVuiCode() + " as the invitation code to pair the android app with Alexa ");
		}
		else if (customerState.getMobileRegistrationId() != null)
		{
		        // send the message
		    	String requestId = generateRequestId();
		    	HashMap<String,String> payload = new HashMap<>();
		    	payload.put(REQUEST_ID,requestId);
		    	
		        sendPushMessage(customerState.getMobileRegistrationId(),payload);
		        System.out.println("Sent the push message with request id: " + requestId);
		        outputStringBuilder.append("Ringing your phone.");
		}
		else if (System.currentTimeMillis() > customerState.getInvitationExpiryTimeMillis())
		{
		    InvitationCode invitationCode = generateRandomInvitationCode();
		    customerState.setInvitationCode(invitationCode.getGuiCode());
		    customerState.setInvitationVuiCode(invitationCode.getVuiCode());
		    customerState.setInvitationExpiryTimeMillis(System.currentTimeMillis() + FIVE_MINS_MILLIS);
		    outputStringBuilder.append("Please install " 
                    + APP_ANDROID_NAME + " from google android playstore and provide " + invitationCode.getVuiCode() + " as the invitation code to pair the android app with Alexa");
		}
		else {
		    String invitationVuiCode = customerState.getInvitationVuiCode();
		    outputStringBuilder.append("Please install " 
                    + APP_ANDROID_NAME + " from google android playstore and provide " + invitationVuiCode + " as the invitation code to pair the android app with Alexa");
		}

		speechoutput.setText(outputStringBuilder.toString());
		return objectMapper.writeValueAsString(speechletResponse);
	}

	@Data
	class InvitationCode 
	{
	    private String vuiCode;
	    private String guiCode;
	}
	
    private InvitationCode generateRandomInvitationCode()
    {
        InvitationCode ic = new InvitationCode();
        int randomNumber = Math.abs((new Random().nextInt() % 10000));
        ic.setGuiCode(randomNumber + "");
        StringBuilder randomCode = new StringBuilder();
        while (randomNumber > 0)
        {
            int lastDigit = randomNumber % 10;
            randomNumber /= 10;
            randomCode.insert(0, " " + lastDigit);
        }
        ic.setVuiCode(randomCode.substring(1, randomCode.length()));
        return ic;
    }

    private String generateRequestId()
    {
    	return RandomStringUtils.randomAlphanumeric(8);
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
		    return handleLaunchRequest(speechletRequest);
		}
		return handleGenericError("Request object is null");
	}

}
