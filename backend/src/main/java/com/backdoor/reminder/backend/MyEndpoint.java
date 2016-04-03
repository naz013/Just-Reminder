/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.backdoor.reminder.backend;

import com.backdoor.reminder.backend.domain.Group;
import com.backdoor.reminder.backend.domain.Profile;
import com.backdoor.reminder.backend.domain.Reminder;
import com.backdoor.reminder.backend.form.GroupForm;
import com.backdoor.reminder.backend.form.ProfileForm;
import com.backdoor.reminder.backend.form.ReminderForm;
import com.google.api.server.spi.Constant;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;

import java.util.List;

import javax.inject.Named;

import static com.backdoor.reminder.backend.service.OfyService.factory;
import static com.backdoor.reminder.backend.service.OfyService.ofy;

/** An endpoint class we are exposing */
    @Api(
            name = "reminder",
            version = "v1",
            scopes = {Constants.EMAIL_SCOPE},
            clientIds = {Constant.API_EXPLORER_CLIENT_ID, Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID},
            audiences = {Constants.ANDROID_AUDIENCE},
            description = "API for accessing Reminder application data.",
            namespace = @ApiNamespace(
                    ownerDomain = "backend.reminder.backdoor.com",
                    ownerName = "backend.reminder.backdoor.com",
                    packagePath=""
            )
    )
public class MyEndpoint {

    private static String extractDefaultDisplayNameFromEmail(String email) {
        return email == null ? null : email.substring(0, email.indexOf("@"));
    }

    @ApiMethod(
            name = "register",
            path = "profile",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Profile register(final User user, ProfileForm profileForm) throws UnauthorizedException {

        String displayName = profileForm.getDisplayName();

        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        String userId = user.getUserId();
        String mainEmail = user.getEmail();

        Profile profile = ofy().load().key(Key.create(Profile.class, userId)).now();
        if (profile == null) {
            if (displayName == null) {
                displayName = extractDefaultDisplayNameFromEmail(user.getEmail());
            }

            profile = new Profile(userId, displayName, mainEmail);
        } else {
            profile.update(displayName);
        }

        ofy().save().entity(profile).now();

        // Return the profile
        return profile;
    }

    @ApiMethod(
            name = "getProfile",
            path = "profile",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Profile getProfile(final User user) throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        String userId = user.getUserId();
        Profile profile = ofy().load().key(Key.create(Profile.class, userId)).now();
        return profile;
    }

    private static Profile getProfileFromUser(User user) {
        // First fetch the user's Profile from the datastore.
        Profile profile = ofy().load().key(
                Key.create(Profile.class, user.getUserId())).now();
        if (profile == null) {
            // Create a new Profile if it doesn't exist.
            // Use default displayName and teeShirtSize
            String email = user.getEmail();
            profile = new Profile(user.getUserId(),
                    extractDefaultDisplayNameFromEmail(email), email);
        }
        return profile;
    }

    @ApiMethod(
            name = "insertReminder",
            path = "insertReminder",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Reminder insertReminder(final User user, final ReminderForm reminderForm)
            throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        final String userId = user.getUserId();
        Key<Profile> profileKey = Key.create(Profile.class, userId);
        final Key<Reminder> reminderKey = factory().allocateId(profileKey, Reminder.class);
        final long reminderId = reminderKey.getId();

        Reminder reminder = ofy().transact(new Work<Reminder>() {
            @Override
            public Reminder run() {
                Profile profile = getProfileFromUser(user);
                Reminder reminder = new Reminder(reminderId, userId, reminderForm);
                ofy().save().entities(reminder, profile).now();
                return reminder;
            }
        });

        return reminder;
    }

    @ApiMethod(
            name = "updateReminder",
            path = "updateReminder/{websafeReminderKey}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Reminder updateReminder(final User user,
                                   @Named("websafeReminderKey") final String websafeReminderKey, final ReminderForm reminderForm)
            throws UnauthorizedException, NotFoundException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        Key<Reminder> reminderKey = Key.create(websafeReminderKey);
        Reminder reminder = ofy().load().key(reminderKey).now();
        if (reminder == null) {
            throw new NotFoundException("No reminder found with key: " + websafeReminderKey);
        } else {
            Profile profile = getProfileFromUser(user);
            reminder.update(reminderForm);
            ofy().save().entities(reminder, profile).now();
        }

        return reminder;
    }

    @ApiMethod(
            name = "getRemindersList",
            path = "getRemindersList",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public List<Reminder> getRemindersList(final User user) throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        String userId = user.getUserId();
        Key<Profile> profileKey = Key.create(Profile.class, userId);

        return ofy().load().type(Reminder.class)
                .ancestor(profileKey)
                .order("eventTime").list();
    }

    @ApiMethod(
            name = "getReminder",
            path = "getReminder/{websafeReminderKey}",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    public Reminder getReminder(final User user,
                                @Named("websafeReminderKey") final String websafeReminderKey)
            throws NotFoundException, UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        Key<Reminder> reminderKey = Key.create(websafeReminderKey);
        Reminder reminder = ofy().load().key(reminderKey).now();
        if (reminder == null) {
            throw new NotFoundException("No reminder found with key: " + websafeReminderKey);
        }
        return reminder;
    }

    @ApiMethod(
            name = "deleteReminder",
            path = "deleteReminder/{websafeReminderKey}",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    public Reminder deleteReminder(final User user,
                                   @Named("websafeReminderKey") final String websafeReminderKey)
            throws NotFoundException, UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        Key<Reminder> reminderKey = Key.create(websafeReminderKey);
        Reminder reminder = ofy().load().key(reminderKey).now();
        if (reminder == null) {
            throw new NotFoundException("No reminder found with key: " + websafeReminderKey);
        } else {
            ofy().delete().key(reminderKey).now();
        }
        return reminder;
    }

    @ApiMethod(
            name = "insertGroup",
            path = "insertGroup",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Group insertGroup(final User user, final GroupForm groupForm)
            throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        final String userId = user.getUserId();
        Key<Profile> profileKey = Key.create(Profile.class, userId);
        final Key<Group> groupKey = factory().allocateId(profileKey, Group.class);
        final long groupId = groupKey.getId();

        Group group = ofy().transact(new Work<Group>() {
            @Override
            public Group run() {
                Profile profile = getProfileFromUser(user);
                Group group = new Group(groupId, userId, groupForm);
                ofy().save().entities(group, profile).now();
                return group;
            }
        });

        return group;
    }

    @ApiMethod(
            name = "updateGroup",
            path = "updateGroup/{websafeGroupKey}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Group updateGroup(final User user,
                             @Named("websafeGroupKey") final String websafeGroupKey, final GroupForm groupForm)
            throws UnauthorizedException, NotFoundException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        Key<Group> groupKey = Key.create(websafeGroupKey);
        Group group = ofy().load().key(groupKey).now();
        if (group == null) {
            throw new NotFoundException("No group found with key: " + websafeGroupKey);
        } else {
            Profile profile = getProfileFromUser(user);
            group.update(groupForm);
            ofy().save().entities(group, profile).now();
        }

        return group;
    }

    @ApiMethod(
            name = "deleteGroup",
            path = "deleteGroup/{websafeGroupKey}",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    public Group deleteGroup(final User user,
                             @Named("websafeGroupKey") final String websafeGroupKey)
            throws NotFoundException, UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        Key<Group> groupKey = Key.create(websafeGroupKey);
        Group group = ofy().load().key(groupKey).now();

        if (group == null) {
            throw new NotFoundException("No group found with key: " + websafeGroupKey);
        } else {
            String groupId = group.getUuId();
            ofy().delete().key(groupKey).now();
            Key<Profile> profileKey = Key.create(Profile.class, user.getUserId());

            List<Reminder> list = ofy().load().type(Reminder.class)
                    .ancestor(profileKey).list();
            for (Reminder reminder : list) {
                if (reminder.getCategory().matches(groupId)) {
                    ofy().delete().entity(reminder);
                }
            }
        }
        return group;
    }

    @ApiMethod(
            name = "getGroup",
            path = "getGroup/{websafeGroupKey}",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    public Group getGroup(final User user,
                          @Named("websafeGroupKey") final String websafeGroupKey)
            throws NotFoundException, UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        Key<Group> groupKey = Key.create(websafeGroupKey);
        Group group = ofy().load().key(groupKey).now();
        if (group == null) {
            throw new NotFoundException("No group found with key: " + websafeGroupKey);
        }
        return group;
    }

    @ApiMethod(
            name = "getGroupsList",
            path = "getGroupsList",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public List<Group> getGroupsList(final User user) throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        Key<Profile> profileKey = Key.create(Profile.class, user.getUserId());
        return ofy().load().type(Group.class)
                .ancestor(profileKey)
                .order("name").list();
    }
}
