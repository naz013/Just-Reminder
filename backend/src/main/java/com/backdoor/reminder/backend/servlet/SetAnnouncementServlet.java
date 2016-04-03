package com.backdoor.reminder.backend.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet for putting announcements in memcache.
 * The announcement announces conferences that are nearly sold out
 * (defined as having 1 - 5 seats left)
 */
@SuppressWarnings("serial")
public class SetAnnouncementServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // TODO
        // Query for conferences with less than 5 seats left
        /*Iterable<Conference> iterable = ofy().load().type(Conference.class).
                filter("maxAttendees =" , 150);

        // TODO
        // Iterate over the conferences with less than 5 seats less
        // and get the name of each one
        List<String> conferenceNames = new ArrayList<>(0);
        for (Conference conference : iterable) {
            conferenceNames.add(conference.getName());
        }
        if (conferenceNames.size() > 0) {

            // Build a String that announces the nearly sold-out conferences
            StringBuilder announcementStringBuilder = new StringBuilder(
                    "Last chance to attend!");
            //Joiner joiner = Joiner.on(", ").skipNulls();
            //announcementStringBuilder.append(joiner.join(conferenceNames));

            // TODO
            // Get the Memcache Service
            MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();

            // TODO
            // Put the announcement String in memcache,
            // keyed by Constants.MEMCACHE_ANNOUNCEMENTS_KEY
            String announcementKey = Constants.MEMCACHE_ANNOUNCEMENTS_KEY;
            String announcementText = announcementStringBuilder.toString();
            memcacheService.put(announcementKey, announcementText);
        }

        // Set the response status to 204 which means
        // the request was successful but there's no data to send back
        // Browser stays on the same page if the get came from the browser*/
        response.setStatus(204);
    }
}
