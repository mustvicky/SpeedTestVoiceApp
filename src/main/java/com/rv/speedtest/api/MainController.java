package com.rv.speedtest.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.amazon.speech.speechlet.authentication.SpeechletRequestSignatureVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rv.speedtest.api.model.AbstractRequest;
import com.rv.speedtest.api.model.Card;
import com.rv.speedtest.api.model.IntentRequest;
import com.rv.speedtest.api.model.InvitationCode;
import com.rv.speedtest.api.model.LaunchRequest;
import com.rv.speedtest.api.model.OutputSpeech;
import com.rv.speedtest.api.model.RegisterDeviceRequest;
import com.rv.speedtest.api.model.RegisterDeviceResponse;
import com.rv.speedtest.api.model.Response;
import com.rv.speedtest.api.model.SessionEndedRequest;
import com.rv.speedtest.api.model.SpeechletRequest;
import com.rv.speedtest.api.model.SpeechletResponse;
import com.rv.speedtest.api.model.User;
import com.rv.speedtest.datastore.Storage;
import com.rv.speedtest.datastore.model.CustomerState;
import com.rv.speedtest.gcm.server.Message;
import com.rv.speedtest.gcm.server.Message.Builder;
import com.rv.speedtest.gcm.server.Result;
import com.rv.speedtest.gcm.server.Sender;

@Controller
@CommonsLog
public class MainController {
	private static final String SPEAK_TAG_OPEN = "<speak>";
	private static final String SPEAK_TAG_CLOSE = "</speak>";
    private static final String APP_VUI_NAME = "Phone Finder for Alexa";
	private static final String APP_ANDROID_NAME = "Phone Finder for Alexa";
	private static final String APP_INVOCATION_NAME = "Phone Finder";
	private static final String ANDROID_APP_INSTALLATION_MESSAGE = "Please install \""  + APP_ANDROID_NAME + "\" from amazon appstore, or google playstore. ";

    // Sender id for caricaturers. Check from : https://console.developers.google.com/project/360023129197/apiui/credential?authuser=0
	private static final String AUTH_KEY = "AIzaSyBJA1IZj-t7iEx1K3fdZIAju9b966skWOA";
	private static final ObjectMapper objectMapper = new ObjectMapper();
	static
	{
	    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	private static final String REQUEST_ID = "requestId";
	
	@Autowired
	private Storage storageInstance;

	// TODO: Rohan Joshi 04092015 Add a method which can be called by the mobile
	// app to store pin -> registration Id.
	// TODO: Rohan Joshi 04092015 Add a method which is invoked by Alexa and
	// which sends a dummmy push message to the mobile application. There will
	// only be 1 kind of push message which needs to be sent to the app which
	// would be a ping for the phone to start the download speed testing.
	// TODO: Rohan Joshi 04092015 Add a method which the app can call with the
	// download speed. This method also invokes TTS so that the output is spoken
	// to the user.

	private static class PushMessageSendFailedException extends Exception
	{
	    private final String errorCode;
	    public String getErrorCode()
        {
            return errorCode;
        }
	    public PushMessageSendFailedException(String errorCode, String errMsg)
        {
            super(errMsg);
            this.errorCode = errorCode;
        }
	}
	private String sendPushMessage(String mobileRegistrationId,
			HashMap<String, String> payload) throws IOException, PushMessageSendFailedException
	{
		Sender sender = new Sender(AUTH_KEY);
		Message.Builder builder = new Message.Builder();
		addPayloadForMessage(builder, payload);
		Message messageToSend = builder.build();
		Result result = sender.send(messageToSend, mobileRegistrationId, 2);
		if (result == null || result.getMessageId() == null || result.getErrorCodeName() != null)
		{
		    log.error("Error in the result object. Result [" + result + "]");
		    throw new PushMessageSendFailedException(result.getErrorCodeName(), "Error in sending push message");
		}
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

	@RequestMapping(value = "/registerDevice", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String registerDevice(@RequestBody String request) throws IOException {
	    RegisterDeviceRequest registerDeviceRequest = objectMapper.readValue(request, RegisterDeviceRequest.class);
	    log.info("Register device request with registration code = ["+registerDeviceRequest.getMobileRegistrationId()+
	            "], and invitation code = ["+registerDeviceRequest.getInvitationCode()+"]");
	    InvitationCode invitationCode = new InvitationCode();
	    invitationCode.setCode(registerDeviceRequest.getInvitationCode());
	    CustomerState customerState = storageInstance.getCustomerStateFromInviteCode(invitationCode);
	    RegisterDeviceResponse response = new RegisterDeviceResponse();
	    if (customerState == null)
	    {
	        response.setError("App couldn't identify your invitation pin: " + registerDeviceRequest.getInvitationCode());
	    }
	    else if (customerState.getDeviceRegistrationId() != null)
	    {
	        response.setError("Your device is already paired.");
	    }
	    else 
	    {
	        customerState.setDeviceRegistrationId(registerDeviceRequest.getMobileRegistrationId());
	        storageInstance.saveCustomerState(customerState);
	    }
	    return objectMapper.writeValueAsString(response);
	}

	private String handleFindPhoneRequest(SpeechletRequest speechletRequest)
			throws IOException {
		SpeechletResponse speechletResponse = new SpeechletResponse();
		Response response = new Response();
		response.setShouldEndSession(true);
		speechletResponse.setResponse(response);
		OutputSpeech speechoutput = new OutputSpeech();
		response.setOutputSpeech(speechoutput);
		
		Card card = new Card();
        response.setCard(card);
        
		StringBuilder outputStringBuilder = new StringBuilder();
		outputStringBuilder.append(SPEAK_TAG_OPEN);
		User user = speechletRequest.getSession().getUser();
		CustomerState customerState = storageInstance.getCustomerStateFromUserId(user.getUserId());
		
		boolean includeAppLinkInCard = false;
		if (customerState == null)
		{
		    outputStringBuilder.append("Welcome to " + APP_VUI_NAME + ". ");
		    customerState = new CustomerState();
		    InvitationCode invitationCode = storageInstance.getUniqueInvitationCode();
		    customerState.setInvitationCode(invitationCode.getCode());
		    customerState.setInvitationCodeExpiryTimeStringUTC(DateUtils.toDateString(DateUtils.getInvitationCodeExpiryTimeFromNow()));
		    customerState.setUserId(user.getUserId());
		    storageInstance.saveCustomerState(customerState);
		    card.setTitle("Pair Phone");
		    includeAppLinkInCard = true;
		    outputStringBuilder.append("I require a companion android app to operate. " + getPhonePairMessage(invitationCode));
		}
		else if (customerState.getDeviceRegistrationId() != null)
		{
		        // send the message
		    	String requestId = generateRequestId();
		    	HashMap<String,String> payload = new HashMap<>();
		    	payload.put(REQUEST_ID,requestId);
		    	
		        try
                {
		            card.setTitle("Find Phone");
                    String messageId = sendPushMessage(customerState.getDeviceRegistrationId(), payload);
                    log.info("Sent the push message with request id: " + requestId + ". Message id [" + messageId + "]");
                    if (isPhoneFinderQuestionRequest(speechletRequest))
                    {
                        outputStringBuilder.append("I'm not sure. Let me ring it for you. ");
                    }
                    else 
                    {
                        outputStringBuilder.append("Your phone must be ringing now. ");
                    }
                } catch (PushMessageSendFailedException ex)
                {
                    if ("NotRegistered".equals(ex.getErrorCode()))
                    {
                        log.warn(ex);
                        storageInstance.invalidateCustomerStateFromUsedId(user.getUserId());
                        outputStringBuilder.append("I'm sorry, looks like you have uninstalled the phone app. I have un paired your phone from "+ APP_VUI_NAME +" App.");
                    }
                    else
                    {
                        log.error(ex);
                        outputStringBuilder.append("I'm sorry, I'm having trouble contacting your phone. ");
                    }
                }
		}
		else if (DateUtils.toDateString(new DateTime().toDate()).compareTo(customerState.getInvitationCodeExpiryTimeStringUTC()) > 0)
		{
		    card.setTitle("Invitation Pin Expired");
		    InvitationCode invitationCode = storageInstance.getUniqueInvitationCode();
		    customerState.setInvitationCode(invitationCode.getCode());
		    customerState.setInvitationCodeExpiryTimeStringUTC(DateUtils.toDateString(DateUtils.getInvitationCodeExpiryTimeFromNow()));
		    outputStringBuilder.append(getPhonePairMessage(invitationCode));
		    storageInstance.saveCustomerState(customerState);
		    includeAppLinkInCard = true;
		}
		else {
		    card.setTitle("Reusing Invitation Pin");
		    // invitation code is still valid
		    String invitationVuiCode = customerState.getInvitationCode();
		    outputStringBuilder.append("Your invitation pin is <say-as interpret-as=\"digits\">" + invitationVuiCode + "</say-as>. ");
		    outputStringBuilder.append(ANDROID_APP_INSTALLATION_MESSAGE);
		    includeAppLinkInCard = true;
		}
		
        outputStringBuilder.append(SPEAK_TAG_CLOSE);
		speechoutput.setSsml(outputStringBuilder.toString());
		if (includeAppLinkInCard)
		{
		    outputStringBuilder.append("\n\nAndroid App link: http://bit.ly/alexaphonefinder");
		}
		card.setContent(outputStringBuilder.toString().replaceAll("\\<.*?\\>", ""));
		return objectMapper.writeValueAsString(speechletResponse);
	}

	private String getPhonePairMessage(InvitationCode invitationCode)
    {
        return ANDROID_APP_INSTALLATION_MESSAGE + "Provide <say-as interpret-as=\"digits\">" + 
	            invitationCode.getCode() + "</say-as> as the invitation pin on android app.";
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
		speechoutput.setSsml(message);

		return objectMapper.writeValueAsString(speechletResponse);
	}

	@RequestMapping(value = "/alexa", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String alexaEntryMethod(@RequestBody String request, @RequestHeader("Signature") String signature, 
	        @RequestHeader("SignatureCertChainUrl") String signatureCertChainUrl)
			throws IOException {
		log.info("Request in /alexa= " + request);
		log.info("Signature = [" + signature + "], and signature url = [" + signatureCertChainUrl + "]");
		SpeechletRequest speechletRequest = objectMapper.readValue(request, SpeechletRequest.class);
		if (!speechletRequest.isDoNotCheckSignature())
		{
		    log.info("Signature should be checked");
		    try
		    {
		        SpeechletRequestSignatureVerifier.checkRequestSignature(request.getBytes(), signature, signatureCertChainUrl);
		    }
		    catch (SecurityException ex)
		    {
		        log.error("Signature did not match, not proceeding any further");
		        throw ex;
		    }
		}
		
		if (isPhoneFinderRequest(speechletRequest)) 
		{
		    return handleFindPhoneRequest(speechletRequest);
		}
		else if (isUnpairPhoneRequest(speechletRequest))
		{
		    return unpairPhone(speechletRequest);
		}
		else if (isExitRequest(speechletRequest))
		{
		    log.info("Exit request. Nothing to do");
		    return "";
		}
		else if (isHelpRequest(speechletRequest))
		{
		    return handleHelpRequest(speechletRequest);
		}
		return handleGenericError("Request object is null");
	}

    private String handleHelpRequest(SpeechletRequest speechletRequest) throws JsonProcessingException
    {
        SpeechletResponse speechletResponse = new SpeechletResponse();
        Response response = new Response();
        response.setShouldEndSession(true);
        speechletResponse.setResponse(response);
        
        StringBuilder outputStringBuilder = new StringBuilder();
        outputStringBuilder.append(SPEAK_TAG_OPEN);
        outputStringBuilder.append("To find your phone, simply launch the skill anytime. "
                + "To un pair your phone, say \"Alexa, tell " + APP_INVOCATION_NAME + " to un pair my phone\"");
        
        OutputSpeech speechoutput = new OutputSpeech();
        response.setOutputSpeech(speechoutput);
        outputStringBuilder.append(SPEAK_TAG_CLOSE);
        speechoutput.setSsml(outputStringBuilder.toString());
        
        Card card = new Card();
        card.setContent(outputStringBuilder.toString().replaceAll("\\<.*?\\>", ""));
        card.setTitle("Help");
        response.setCard(card);

        return objectMapper.writeValueAsString(speechletResponse);
    }

    private String unpairPhone(SpeechletRequest speechletRequest) throws JsonProcessingException
    {
        SpeechletResponse speechletResponse = new SpeechletResponse();
        Response response = new Response();
        response.setShouldEndSession(true);
        speechletResponse.setResponse(response);
        
        StringBuilder outputStringBuilder = new StringBuilder();
        outputStringBuilder.append(SPEAK_TAG_OPEN);
        CustomerState customerState = null;
        User user = speechletRequest.getSession().getUser();
        customerState = storageInstance.getCustomerStateFromUserId(user.getUserId());
        if (customerState == null)
        {
            outputStringBuilder.append("I don't have your phone paired. ");
        }
        else
        {
            storageInstance.invalidateCustomerStateFromUsedId(user.getUserId());
            outputStringBuilder.append("I have un paired your phone successfully. ");
        }
        
        OutputSpeech speechoutput = new OutputSpeech();
        response.setOutputSpeech(speechoutput);
        outputStringBuilder.append(SPEAK_TAG_CLOSE);
        speechoutput.setSsml(outputStringBuilder.toString());
        
        Card card = new Card();
        card.setContent(outputStringBuilder.toString().replaceAll("\\<.*?\\>", ""));
        card.setTitle("Unpairing Phone");
        response.setCard(card);
        
        return objectMapper.writeValueAsString(speechletResponse);
    }

    private boolean isExitRequest(SpeechletRequest speechletRequest)
    {
        if (speechletRequest.getRequest() == null)
        {
            log.error("Request object in speechlet request is null");
            return false;
        }
        AbstractRequest abstractRequest = speechletRequest.getRequest();
        if (abstractRequest instanceof SessionEndedRequest)
        {
            log.info("request is session ended request");
            return true;
        }
        
        return false;
    }

    private boolean isUnpairPhoneRequest(SpeechletRequest speechletRequest)
    {
        if (speechletRequest.getRequest() == null)
        {
            log.error("Request object in speechlet request is null");
            return false;
        }
        AbstractRequest abstractRequest = speechletRequest.getRequest();
        if (abstractRequest instanceof LaunchRequest)
        {
            log.info("request is launch request");
            return false;
        }
        
        if (abstractRequest instanceof IntentRequest)
        {
            log.info("request is launch request");
            IntentRequest intentRequest = (IntentRequest) abstractRequest;
            if (intentRequest.getIntent() == null)
            {
                log.error("intent req does not contain an intent");
                return false;
            }
            
            if ("UnpairPhone".equals(intentRequest.getIntent().getName()))
            {
                log.info("request is UnpairPhone intent.");
                return true;
            }
        }
        return false;
    }
    
    private boolean isHelpRequest(SpeechletRequest speechletRequest)
    {
        if (speechletRequest.getRequest() == null)
        {
            log.error("Request object in speechlet request is null");
            return false;
        }
        AbstractRequest abstractRequest = speechletRequest.getRequest();
        if (abstractRequest instanceof LaunchRequest)
        {
            log.info("request is launch request");
            return false;
        }
        
        if (abstractRequest instanceof IntentRequest)
        {
            log.info("request is launch request");
            IntentRequest intentRequest = (IntentRequest) abstractRequest;
            if (intentRequest.getIntent() == null)
            {
                log.error("intent req does not contain an intent");
                return false;
            }
            
            if ("AMAZON.HelpIntent".equals(intentRequest.getIntent().getName()) 
                    || "Help".equals(intentRequest.getIntent().getName()) )
            {
                log.info("request is Help intent.");
                return true;
            }
        }
        return false;
    }

    private boolean isPhoneFinderRequest(SpeechletRequest speechletRequest)
    {
        if (speechletRequest.getRequest() == null)
        {
            log.error("Request object in speechlet request is null");
            return false;
        }
        AbstractRequest abstractRequest = speechletRequest.getRequest();
        if (abstractRequest instanceof LaunchRequest)
        {
            log.info("request is launch request");
            return true;
        }
        
        if (abstractRequest instanceof IntentRequest)
        {
            log.info("request is launch request");
            IntentRequest intentRequest = (IntentRequest) abstractRequest;
            if (intentRequest.getIntent() == null)
            {
                log.error("intent req does not contain an intent");
                return false;
            }
            
            if ("FindPhone".equals(intentRequest.getIntent().getName()) ||
                    "QuestionWhereIsPhone".equals(intentRequest.getIntent().getName()))
            {
                log.info("request is FindPhone or QuestionWhereIsPhone intent.");
                return true;
            }
        }
        return false;
    }
    
    private boolean isPhoneFinderQuestionRequest(SpeechletRequest speechletRequest)
    {
        if (speechletRequest.getRequest() == null)
        {
            log.error("Request object in speechlet request is null");
            return false;
        }
        AbstractRequest abstractRequest = speechletRequest.getRequest();
        if (abstractRequest instanceof LaunchRequest)
        {
            log.info("request is launch request");
            return false;
        }
        
        if (abstractRequest instanceof IntentRequest)
        {
            log.info("request is launch request");
            IntentRequest intentRequest = (IntentRequest) abstractRequest;
            if (intentRequest.getIntent() == null)
            {
                log.error("intent req does not contain an intent");
                return false;
            }
            
            if ("QuestionWhereIsPhone".equals(intentRequest.getIntent().getName()))
            {
                log.info("request is FindPhone or QuestionWhereIsPhone intent.");
                return true;
            }
        }
        return false;
    }

}
