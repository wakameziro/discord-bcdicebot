package com.hiyoko.discord.bot.BCDice;

import java.io.IOException;
import java.net.URLEncoder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import com.hiyoko.discord.bot.BCDice.dto.DicerollResult;
import com.hiyoko.discord.bot.BCDice.dto.SystemInfo;
import com.hiyoko.discord.bot.BCDice.dto.SystemList;
import com.hiyoko.discord.bot.BCDice.dto.VersionInfo;

/**
 * BCDice-API Client
 * @author Shunshun94
 *
 */
public class BCDiceClient {
	private final String url;
	private final Client client;
	
	private String system = "DiceBot";

	/**
	 * 
	 * @param bcDiceUrl BCDice-API server URL
	 */
	public BCDiceClient(String bcDiceUrl) {
		url = bcDiceUrl.endsWith("/") ? bcDiceUrl : bcDiceUrl + "/";
		client = ClientBuilder.newBuilder().build();
	}
	
	/**
	 * 
	 * @param path the path to the called API command
	 * @return the API called result as String
	 * @throws IOException When access is failed
	 */
	public String getUrl(String path) throws IOException {
		Response response = null;
		String targetUrl = url + path;
		try {
			response = client.target(targetUrl).request().get();
		} catch(Exception e) {
			if(response != null) {
				response.close();
			}
			throw new IOException(e.getMessage() + "(" + targetUrl + ")", e);
		}
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
        	String msg = "[" + response.getStatus() + "] " + targetUrl;
        	response.close();
        	throw new IOException(msg);
        }
        String result = response.readEntity(String.class);
        response.close();
        return result;
	}

	/**
	 * 
	 * @return version info of the server
	 * @throws IOException When access is failed
	 */
	public VersionInfo getVersion() throws IOException {
		return new VersionInfo(getUrl("v1/version"));
	}
	
	/**
	 * 
	 * @return The dice systems list
	 * @throws IOException When access is failed
	 */
	public SystemList getSystems() throws IOException {
		return new SystemList(getUrl("v1/systems"));
	}
	
	/**
	 * 
	 * @param gameType game name
	 * @return The detail of the dice system of the rule
	 * @throws IOException When access is failed
	 */
	public SystemInfo getSystemInfo(String gameType) throws IOException {
		String rawJson = getUrl("v1/systeminfo?system=" + URLEncoder.encode(gameType, "UTF-8"));
		try {
			// IOException should be thrown from getURL and SystemInfo constructor.
			// I have to show which method throws the Exception.
			return new SystemInfo(rawJson);
		} catch (IOException e) {
			throw new IOException("System '" + gameType + "' is not found", e);
		}
	}
	
	/**
	 * 
	 * @param command dice command
	 * @param system dice system of the rule
	 * @return dice result
	 * @throws IOException When access is failed
	 */
	public DicerollResult rollDice(String command, String system) throws IOException {
		return new DicerollResult(getUrl("v1/diceroll?command=" + URLEncoder.encode(command, "UTF-8") + "&system=" + URLEncoder.encode(system, "UTF-8")));
	}
	
	/**
	 * rollDice method with the current system.
	 * @param command dice command
	 * @return dice result
	 * @throws IOException
	 */
	public DicerollResult rollDice(String command) throws IOException {
		return rollDice(command, system);
	}
	
	/**
	 * change current system.
	 * @param newSystem
	 * @return new current system.
	 */
	public String setSystem(String newSystem) {
		system = newSystem;
		return system;
	}
	
	/**
	 * 
	 * @return current system name
	 */
	public String getSystem() {
		return system;
	}
	
	public String toString() {
		return "[BCDiceClient] for " + url + " : " + system;
	}
}