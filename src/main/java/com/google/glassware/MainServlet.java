/*
 * Copyright (C) 2013 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.glassware;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.mirror.model.Command;
import com.google.api.services.mirror.model.Contact;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.MenuValue;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Handles POST requests from index.jsp
 *
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
public class MainServlet extends HttpServlet {

  /**
   * Private class to process batch request results.
   * <p/>
   * For more information, see
   * https://code.google.com/p/google-api-java-client/wiki/Batch.
   */
  private final class BatchCallback extends JsonBatchCallback<TimelineItem> {
    private int success = 0;
    private int failure = 0;

    @Override
    public void onSuccess(TimelineItem item, HttpHeaders headers) throws IOException {
      ++success;
    }

    @Override
    public void onFailure(GoogleJsonError error, HttpHeaders headers) throws IOException {
      ++failure;
      LOG.info("Failed to insert item: " + error.getMessage());
    }
  }

  private static final Logger LOG = Logger.getLogger(MainServlet.class.getSimpleName());
  public static final String CONTACT_ID = "com.google.glassware.contact.java-quick-start";
  public static final String CONTACT_NAME = "Java Quick Start";

  private static final String PAGINATED_HTML =
      "<article class='auto-paginate'>"
      + "<h2 class='blue text-large'>Did you know...?</h2>"
      + "<p>Cats are <em class='yellow'>solar-powered.</em> The time they spend napping in "
      + "direct sunlight is necessary to regenerate their internal batteries. Cats that do not "
      + "receive sufficient charge may exhibit the following symptoms: lethargy, "
      + "irritability, and disdainful glares. Cats will reactivate on their own automatically "
      + "after a complete charge cycle; it is recommended that they be left undisturbed during "
      + "this process to maximize your enjoyment of your cat.</p><br/><p>"
      + "For more cat maintenance tips, tap to view the website!</p>"
      + "</article>";

  /**
   * Do stuff when buttons on index.jsp are clicked
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

    String userId = AuthUtil.getUserId(req);
    Credential credential = AuthUtil.newAuthorizationCodeFlow().loadCredential(userId);
    String message = "";

    if (req.getParameter("operation").equals("insertSubscription")) {

      // subscribe (only works deployed to production)
      try {
        MirrorClient.insertSubscription(credential, WebUtil.buildUrl(req, "/notify"), userId,
            req.getParameter("collection"));
        message = "Application is now subscribed to updates.";
      } catch (GoogleJsonResponseException e) {
        LOG.warning("Could not subscribe " + WebUtil.buildUrl(req, "/notify") + " because "
            + e.getDetails().toPrettyString());
        message = "Failed to subscribe. Check your log for details";
      }

    } else if (req.getParameter("operation").equals("deleteSubscription")) {

      // subscribe (only works deployed to production)
      MirrorClient.deleteSubscription(credential, req.getParameter("subscriptionId"));

      message = "Application has been unsubscribed.";

    } else if (req.getParameter("operation").equals("insertItem")) {
      LOG.fine("Inserting Timeline Item");
      
      List<TimelineItem> timeLineList = new ArrayList<TimelineItem>();
     
      //Custom logic
      //Receive html input in the form, parse and create the timeline items
      if (req.getParameter("message") != null) {
        String htmlContent = req.getParameter("message");
        Document doc = Jsoup.parse(htmlContent);
        
        int steps = 1;
        String speakabletext = "";
        for (Element stepElement : doc.select("div.step")) {
        	System.out.println("Steps: " + doc.select("div.step").size());
        	TimelineItem timelineItem = new TimelineItem();
            timelineItem.setBundleId("ABC");
            timelineItem.setId(String.valueOf(steps));
            
            String cardHtml = "<table><tr><td style=\"color:white;font-size:45px;\">STEP " + String.valueOf(steps) + ":</td></tr><tr>";
            String textContent = "";
    		String imgContent = "";
        	for (Element el : stepElement.getElementsByClass("detail")) {
        		if(el.getElementsByTag("img").size() > 0){
            		Element imgElement = el.getElementsByTag("img").first();
            		imgContent = imgElement.toString();
            		System.out.println("img tag:" + imgElement.toString());
            	}
        		if(el.getElementsByTag("p").size() > 0){
            		System.out.println("text tag: " + el.getElementsByTag("p").first().text());
            		textContent = el.getElementsByTag("p").first().text();
            		speakabletext = el.getElementsByTag("p").first().text();
            	}
    		}
        	cardHtml += "<td>" + imgContent + "</td> <td style=\"vertical-align:top;color:yellow;font-size:30px;\"><p  style=\"color:yellow;font-size:35px;\"> " + textContent + "</p></td>";
        	cardHtml += " </tr></table>";
        	timelineItem.setHtml(cardHtml);
        	
        	MenuItem item = new MenuItem();
        	item.setId("readaloud");
        	item.setAction("READ_ALOUD");
        	
        	List<MenuValue> values = new ArrayList<MenuValue>();
        	MenuValue value1 = new MenuValue();
        	value1.setDisplayName("ReadAloud");
        	value1.setIconUrl("http://lh5.ggpht.com/k2fX8KJrSE-431-tfEOZs0Q0I2I0RaSDGyysh6BbecmM5vObrk_MBBGpIMsPoXo8yTsOLqiXaM4ItqN-5Vyg0cTIpmPSE7pM04I");
        	values.add(value1);
        	item.setValues(values);
        	
        	List<MenuItem> items = new ArrayList<MenuItem>();
        	items.add(item);
        	timelineItem.setMenuItems(items);
        	timelineItem.setSpeakableText(speakabletext);
        	timeLineList.add(timelineItem);
        	System.out.println();
        	++steps;
		}
        
        //Communicating with GDK to invoke the native camera 
        MenuItem item = new MenuItem();
    	item.setId("takePicture");
    	item.setAction("OPEN_URI");
    	item.setPayload("glassware://com.example.chembuddy");
    	
    	List<MenuValue> values = new ArrayList<MenuValue>();
    	MenuValue value1 = new MenuValue();
    	value1.setDisplayName("Open");
    	value1.setState("DEFAULT");
    	value1.setIconUrl("http://lh5.ggpht.com/k2fX8KJrSE-431-tfEOZs0Q0I2I0RaSDGyysh6BbecmM5vObrk_MBBGpIMsPoXo8yTsOLqiXaM4ItqN-5Vyg0cTIpmPSE7pM04I");
    	values.add(value1);
    	MenuValue value2 = new MenuValue();
    	value2.setDisplayName("Launching");
    	value2.setState("PENDING");
    	value2.setIconUrl("http://lh5.ggpht.com/k2fX8KJrSE-431-tfEOZs0Q0I2I0RaSDGyysh6BbecmM5vObrk_MBBGpIMsPoXo8yTsOLqiXaM4ItqN-5Vyg0cTIpmPSE7pM04I");
    	values.add(value2);
    	MenuValue value3 = new MenuValue();
    	value3.setDisplayName("Launched");
    	value3.setState("CONFIRMED");
    	value3.setIconUrl("http://lh5.ggpht.com/k2fX8KJrSE-431-tfEOZs0Q0I2I0RaSDGyysh6BbecmM5vObrk_MBBGpIMsPoXo8yTsOLqiXaM4ItqN-5Vyg0cTIpmPSE7pM04I");
    	values.add(value3);
    	item.setValues(values);
        
        List<MenuItem> items = new ArrayList<MenuItem>();
    	items.add(item);
        TimelineItem timelineItemFinal = new TimelineItem();
        timelineItemFinal.setBundleId("ABC");
        timelineItemFinal.setId("0");
        timelineItemFinal.setText("Submit Your Results");
        timelineItemFinal.setMenuItems(items);
        timeLineList.add(timelineItemFinal);
        
        
        TimelineItem timelineItem = new TimelineItem();
        timelineItem.setIsBundleCover(true);
        timelineItem.setBundleId("ABC");
        timelineItem.setId(String.valueOf(steps));
        timelineItem.setHtml("<table> <tr><td style=\"color:white;font-size:55px;\">TEST:</td></tr> <tr><td style=\"color:yellow;font-size:55px;\"> Sodium Hydrogen Carbonate</td></tr> </table>");
        timelineItem.setSpeakableText("Detailed steps for Sodium Hydrogen Carbonate Test");
    	//timelineItem.setSpeakableType("Overall Experiment");
        timeLineList.add(timelineItem);
        Collections.reverse(timeLineList);
        
        timelineItem.setNotification(new NotificationConfig().setLevel("DEFAULT"));
      }

      //Triggers an audible tone when the timeline item is received
      MirrorClient.insertListTimelineItem(credential, timeLineList);

      message = "A timeline item has been inserted.";

    } else if (req.getParameter("operation").equals("insertPaginatedItem")) {
      LOG.fine("Inserting Timeline Item");
      TimelineItem timelineItem = new TimelineItem();
      timelineItem.setHtml(PAGINATED_HTML);

      List<MenuItem> menuItemList = new ArrayList<MenuItem>();
      menuItemList.add(new MenuItem().setAction("OPEN_URI").setPayload(
          "https://www.google.com/search?q=cat+maintenance+tips"));
      timelineItem.setMenuItems(menuItemList);

      // Triggers an audible tone when the timeline item is received
      timelineItem.setNotification(new NotificationConfig().setLevel("DEFAULT"));

      MirrorClient.insertTimelineItem(credential, timelineItem);

      message = "A timeline item has been inserted.";

    } else if (req.getParameter("operation").equals("insertItemWithAction")) {
      LOG.fine("Inserting Timeline Item");
      TimelineItem timelineItem = new TimelineItem();
      timelineItem.setText("Tell me what you had for lunch :)");

      List<MenuItem> menuItemList = new ArrayList<MenuItem>();
      // Built in actions
      menuItemList.add(new MenuItem().setAction("REPLY"));
      menuItemList.add(new MenuItem().setAction("READ_ALOUD"));

      // And custom actions
      List<MenuValue> menuValues = new ArrayList<MenuValue>();
      menuValues.add(new MenuValue().setIconUrl(WebUtil.buildUrl(req, "/static/images/drill.png"))
          .setDisplayName("Drill In"));
      menuItemList.add(new MenuItem().setValues(menuValues).setId("drill").setAction("CUSTOM"));

      timelineItem.setMenuItems(menuItemList);
      timelineItem.setNotification(new NotificationConfig().setLevel("DEFAULT"));

      MirrorClient.insertTimelineItem(credential, timelineItem);

      message = "A timeline item with actions has been inserted.";

    } else if (req.getParameter("operation").equals("insertContact")) {
      if (req.getParameter("iconUrl") == null || req.getParameter("name") == null) {
        message = "Must specify iconUrl and name to insert contact";
      } else {
        // Insert a contact
        LOG.fine("Inserting contact Item");
        Contact contact = new Contact();
        contact.setId(req.getParameter("id"));
        contact.setDisplayName(req.getParameter("name"));
        contact.setImageUrls(Lists.newArrayList(req.getParameter("iconUrl")));
        contact.setAcceptCommands(Lists.newArrayList(new Command().setType("TAKE_A_NOTE")));
        MirrorClient.insertContact(credential, contact);

        message = "Inserted contact: " + req.getParameter("name");
      }

    } else if (req.getParameter("operation").equals("deleteContact")) {

      // Insert a contact
      LOG.fine("Deleting contact Item");
      MirrorClient.deleteContact(credential, req.getParameter("id"));

      message = "Contact has been deleted.";

    } else if (req.getParameter("operation").equals("insertItemAllUsers")) {
      if (req.getServerName().contains("glass-java-starter-demo.appspot.com")) {
        message = "This function is disabled on the demo instance.";
      }

      // Insert a contact
      List<String> users = AuthUtil.getAllUserIds();
      LOG.info("found " + users.size() + " users");
      if (users.size() > 10) {
        // We wouldn't want you to run out of quota on your first day!
        message =
            "Total user count is " + users.size() + ". Aborting broadcast " + "to save your quota.";
      } else {
        TimelineItem allUsersItem = new TimelineItem();
        allUsersItem.setText("Hello Everyone!");

        BatchRequest batch = MirrorClient.getMirror(null).batch();
        BatchCallback callback = new BatchCallback();

        // TODO: add a picture of a cat
        for (String user : users) {
          Credential userCredential = AuthUtil.getCredential(user);
          MirrorClient.getMirror(userCredential).timeline().insert(allUsersItem)
              .queue(batch, callback);
        }

        batch.execute();
        message =
            "Successfully sent cards to " + callback.success + " users (" + callback.failure
                + " failed).";
      }


    } else if (req.getParameter("operation").equals("deleteTimelineItem")) {

      // Delete a timeline item
      LOG.fine("Deleting Timeline Item");
      MirrorClient.deleteTimelineItem(credential, req.getParameter("itemId"));

      message = "Timeline Item has been deleted.";

    } else {
      String operation = req.getParameter("operation");
      LOG.warning("Unknown operation specified " + operation);
      message = "I don't know how to do that";
    }
    WebUtil.setFlash(req, message);
    res.sendRedirect(WebUtil.buildUrl(req, "/"));
  }
  
  
}
