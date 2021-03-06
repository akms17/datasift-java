package org.datasift.examples;

import java.net.MalformedURLException;

import org.datasift.Config;
import org.datasift.EAPIError;
import org.datasift.EAccessDenied;
import org.datasift.ECompileFailed;
import org.datasift.EInvalidData;
import org.datasift.IMultiStreamConsumerEvents;
import org.datasift.Interaction;
import org.datasift.StreamConsumer;
import org.datasift.User;

public class ConsumeStreamWS implements IMultiStreamConsumerEvents {

	private User _user = null;
	
	/**
	 * @param args
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) {
		new ConsumeStreamWS().run(args);
	}

	protected void run(String[] hashes) {
		try {
			// Authenticate
			System.out.println("Creating user...");
			_user = new User(Config.username, Config.api_key);

			// Get the hash list from the parameters
			System.out.println("Building hash list...");

			// Create the consumer
			System.out.println("Getting the consumer...");
			StreamConsumer consumer = StreamConsumer.factory(_user, StreamConsumer.TYPE_WS, this);

			// And start consuming
			System.out.println("Consuming...");
			System.out.println("--");
			consumer.consume();

			// Send subscriptions
			for (String hash : hashes) {
				boolean tryagain = true;
				while (tryagain) {
					try {
						consumer.subscribe(hash);
						System.out.println("Subscribing to \"" + hash + "\"...");
						tryagain = false;
						Thread.sleep(100);
					} catch (EAPIError e) {
						if (!e.getMessage().contains("not connected")) {
							throw e;
						}
					}
				}
			}
		} catch (EInvalidData e) {
			System.out.print("InvalidData: ");
			System.out.println(e.getMessage());
		} catch (ECompileFailed e) {
			System.out.print("CompileFailed: ");
			System.out.println(e.getMessage());
		} catch (EAccessDenied e) {
			System.out.print("AccessDenied: ");
			System.out.println(e.getMessage());
		} catch (EAPIError e) {
			System.out.print("APIError: ");
			System.out.println(e.getMessage());
		} catch (InterruptedException e) {
			System.out.print("InterruptedException: ");
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Handle incoming data.
	 * 
	 * @param StreamConsumer
	 *            consumer The consumer object.
	 * @param String
	 *            hash The hash of the stream that matched this interaction.
	 * @param Interaction
	 *            interaction The interaction data.
	 * @throws EInvalidData
	 */
	public void onInteraction(StreamConsumer c, String hash, Interaction i)
			throws EInvalidData {
		try {
			System.out.print(i.getStringVal("interaction.author.name"));
			System.out.print(": ");
			System.out.println(i.getStringVal("interaction.content"));
		} catch (EInvalidData e) {
			// The interaction did not contain either a type or content.
			System.out.println("Exception: " + e.getMessage());
			System.out.print("Interaction: ");
			System.out.println(i);
		}
		System.out.println("--");
	}

	/**
	 * Handle delete notifications.
	 * 
	 * @param StreamConsumer
	 *            consumer The consumer object.
	 * @param JSONObject
	 *            interaction The interaction data.
	 * @throws EInvalidData
	 */
	public void onDeleted(StreamConsumer c, String hash, Interaction i)
			throws EInvalidData {
		// Ignored for this example
	}

	/**
	 * Called when the consumer has stopped.
	 * 
	 * @param DataSift_StreamConsumer
	 *            $consumer The consumer object.
	 * @param string
	 *            $reason The reason the consumer stopped.
	 */
	public void onStopped(StreamConsumer consumer, String reason) {
		System.out.print("Stopped: ");
		System.out.println(reason);
	}

	/**
	 * Called when a warning is received in the data stream.
	 * 
	 * @param DataSift_StreamConsumer consumer The consumer object.
	 * @param string message The warning message.
	 */
	public void onWarning(StreamConsumer consumer, String message)
			throws EInvalidData {
		System.out.println("Warning: " + message);
	}

	/**
	 * Called when an error is received in the data stream.
	 * 
	 * @param DataSift_StreamConsumer consumer The consumer object.
	 * @param string message The error message.
	 */
	public void onError(StreamConsumer consumer, String message)
			throws EInvalidData {
		System.out.println("Error: " + message);
	}
}
