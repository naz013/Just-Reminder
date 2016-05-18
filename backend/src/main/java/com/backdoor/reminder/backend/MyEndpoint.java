/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.backdoor.reminder.backend;

import com.backdoor.reminder.backend.domain.Group;
import com.backdoor.reminder.backend.domain.Note;
import com.backdoor.reminder.backend.domain.Profile;
import com.backdoor.reminder.backend.domain.Reminder;
import com.backdoor.reminder.backend.form.GroupForm;
import com.backdoor.reminder.backend.form.NoteForm;
import com.backdoor.reminder.backend.form.ProfileForm;
import com.backdoor.reminder.backend.form.ReminderForm;
import com.google.api.server.spi.Constant;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
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
    public Profile register(@Named("token") String token, ProfileForm profileForm) throws BadRequestException {

        String displayName = profileForm.getDisplayName();

        if (token == null) {
            throw new BadRequestException("Incorrect token");
        }

        List<Profile> list = ofy().load().type(Profile.class).list();
        Profile profile = null;
        if (list == null || list.size() == 0) {
            if (displayName == null) {
                displayName = extractDefaultDisplayNameFromEmail(profileForm.getEmail());
            }

            Key<Profile> profileKey = factory().allocateId(Profile.class);
            long userId = profileKey.getId();
            String mainEmail = profileForm.getEmail();
            profile = new Profile(userId, displayName, mainEmail);
        } else {
            for (Profile item : list) {
                if (item.getMainEmail().equals(profileForm.getEmail())) {
                    profile = item;
                    break;
                }
            }
        }
        ofy().save().entity(profile).now();
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
        return ofy().load().key(Key.create(Profile.class, userId)).now();
    }

    private static Profile getProfileFromUser(User user) {
        // First fetch the user's Profile from the datastore.
        Profile profile = ofy().load().key(
                Key.create(Profile.class, user.getUserId())).now();
        if (profile == null) {
            // Create a new Profile if it doesn't exist.
            // Use default displayName and teeShirtSize
            String email = user.getEmail();
            Key<Profile> profileKey = factory().allocateId(Profile.class);
            profile = new Profile(profileKey.getId(),
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
            path = "updateReminder/{id}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Reminder updateReminder(final User user,
                                   @Named("id") final long id, final ReminderForm reminderForm)
            throws UnauthorizedException, NotFoundException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        String userId = user.getUserId();
        Key<Profile> profileKey = Key.create(Profile.class, userId);

        List<Reminder> list = ofy().load().type(Reminder.class)
                .ancestor(profileKey)
                .order("eventTime").list();
        Reminder reminder = null;
        for (Reminder item : list) {
            if (item.getId() == id) {
                reminder = item;
                break;
            }
        }
        if (reminder == null) {
            throw new NotFoundException("No reminder found with id: " + id);
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
    public List<Reminder> getRemindersList(final User user) throws UnauthorizedException,
            NotFoundException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        String userId = user.getUserId();
        Key<Profile> profileKey = Key.create(Profile.class, userId);

        List<Reminder> list = ofy().load().type(Reminder.class)
                .ancestor(profileKey)
                .order("eventTime").list();

        if (list == null || list.size() == 0) {
            throw new NotFoundException("For now you don't have any reminder is DB.");
        }

        return list;
    }

    @ApiMethod(
            name = "getReminder",
            path = "getReminder/{id}",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    public Reminder getReminder(final User user,
                                @Named("id") final long id)
            throws NotFoundException, UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        String userId = user.getUserId();
        Key<Profile> profileKey = Key.create(Profile.class, userId);

        List<Reminder> list = ofy().load().type(Reminder.class)
                .ancestor(profileKey)
                .order("eventTime").list();
        Reminder reminder = null;
        for (Reminder item : list) {
            if (item.getId() == id) {
                reminder = item;
                break;
            }
        }
        if (reminder == null) {
            throw new NotFoundException("No reminder found with id: " + id);
        }
        return reminder;
    }

    @ApiMethod(
            name = "deleteReminder",
            path = "deleteReminder/{id}",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    public Reminder deleteReminder(final User user,
                                   @Named("id") final long id)
            throws NotFoundException, UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        String userId = user.getUserId();
        Key<Profile> profileKey = Key.create(Profile.class, userId);

        List<Reminder> list = ofy().load().type(Reminder.class)
                .ancestor(profileKey)
                .order("eventTime").list();
        Reminder reminder = null;
        for (Reminder item : list) {
            if (item.getId() == id) {
                reminder = item;
                break;
            }
        }
        if (reminder == null) {
            throw new NotFoundException("No reminder found with id: " + id);
        } else {
            ofy().delete().key(Key.create(reminder.getWebsafeKey())).now();
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
            path = "updateGroup/{id}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Group updateGroup(final User user,
                             @Named("id") final long id, final GroupForm groupForm)
            throws UnauthorizedException, NotFoundException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        String userId = user.getUserId();
        Key<Profile> profileKey = Key.create(Profile.class, userId);

        List<Group> list = ofy().load().type(Group.class)
                .ancestor(profileKey)
                .order("name").list();
        Group group = null;
        for (Group item : list) {
            if (item.getId() == id) {
                group = item;
                break;
            }
        }

        if (group == null) {
            throw new NotFoundException("No group found with id: " + id);
        } else {
            Profile profile = getProfileFromUser(user);
            group.update(groupForm);
            ofy().save().entities(group, profile).now();
        }

        return group;
    }

    @ApiMethod(
            name = "deleteGroup",
            path = "deleteGroup/{id}",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    public Group deleteGroup(final User user,
                             @Named("id") final long id)
            throws NotFoundException, UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        String userId = user.getUserId();
        Key<Profile> profileKey = Key.create(Profile.class, userId);

        List<Group> list = ofy().load().type(Group.class)
                .ancestor(profileKey)
                .order("name").list();
        Group group = null;
        for (Group item : list) {
            if (item.getId() == id) {
                group = item;
                break;
            }
        }

        if (group == null) {
            throw new NotFoundException("No group found with id: " + id);
        } else {
            ofy().delete().key(Key.create(group.getWebsafeKey())).now();
        }
        return group;
    }

    @ApiMethod(
            name = "getGroup",
            path = "getGroup/{id}",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    public Group getGroup(final User user,
                          @Named("id") final long id)
            throws NotFoundException, UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        String userId = user.getUserId();
        Key<Profile> profileKey = Key.create(Profile.class, userId);

        List<Group> list = ofy().load().type(Group.class)
                .ancestor(profileKey)
                .order("name").list();
        Group group = null;
        for (Group item : list) {
            if (item.getId() == id) {
                group = item;
                break;
            }
        }
        if (group == null) {
            throw new NotFoundException("No group found with id: " + id);
        }
        return group;
    }

    @ApiMethod(
            name = "getGroupsList",
            path = "getGroupsList",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public List<Group> getGroupsList(final User user) throws UnauthorizedException, NotFoundException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        String userId = user.getUserId();
        Key<Profile> profileKey = Key.create(Profile.class, userId);

        List<Group> list = ofy().load().type(Group.class)
                .ancestor(profileKey)
                .order("name").list();
        if (list == null || list.size() == 0) {
            throw new NotFoundException("Not found any group entity in DB.");
        }
        return list;
    }

    @ApiMethod(
            name = "insertNote",
            path = "insertNote",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Note insertNote(final User user, final NoteForm noteForm)
            throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        final String userId = user.getUserId();
        Key<Profile> profileKey = Key.create(Profile.class, userId);
        final Key<Note> groupKey = factory().allocateId(profileKey, Note.class);
        final long groupId = groupKey.getId();

        Note note = ofy().transact(new Work<Note>() {
            @Override
            public Note run() {
                Profile profile = getProfileFromUser(user);
                Note note = new Note(groupId, userId, noteForm);
                ofy().save().entities(note, profile).now();
                return note;
            }
        });

        return note;
    }

    @ApiMethod(
            name = "updateNote",
            path = "updateNote/{id}",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Note updateNote(final User user,
                             @Named("id") final long id, final NoteForm noteForm)
            throws UnauthorizedException, NotFoundException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        String userId = user.getUserId();
        Key<Profile> profileKey = Key.create(Profile.class, userId);

        List<Note> list = ofy().load().type(Note.class)
                .ancestor(profileKey)
                .order("dateTime").list();
        Note note = null;
        for (Note item : list) {
            if (item.getId() == id) {
                note = item;
                break;
            }
        }

        if (note == null) {
            throw new NotFoundException("No note found with id: " + id);
        } else {
            Profile profile = getProfileFromUser(user);
            note.update(noteForm);
            ofy().save().entities(note, profile).now();
        }

        return note;
    }

    @ApiMethod(
            name = "deleteNote",
            path = "deleteNote/{id}",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    public Note deleteNote(final User user,
                             @Named("id") final long id)
            throws NotFoundException, UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        String userId = user.getUserId();
        Key<Profile> profileKey = Key.create(Profile.class, userId);

        List<Note> list = ofy().load().type(Note.class)
                .ancestor(profileKey)
                .order("dateTime").list();
        Note note = null;
        for (Note item : list) {
            if (item.getId() == id) {
                note = item;
                break;
            }
        }

        if (note == null) {
            throw new NotFoundException("No note found with id: " + id);
        } else {
            ofy().delete().key(Key.create(note.getWebsafeKey())).now();
        }
        return note;
    }

    @ApiMethod(
            name = "getNote",
            path = "getNote/{id}",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    public Note getNote(final User user,
                          @Named("id") final long id)
            throws NotFoundException, UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        String userId = user.getUserId();
        Key<Profile> profileKey = Key.create(Profile.class, userId);

        List<Note> list = ofy().load().type(Note.class)
                .ancestor(profileKey)
                .order("dateTime").list();
        Note note = null;
        for (Note item : list) {
            if (item.getId() == id) {
                note = item;
                break;
            }
        }
        if (note == null) {
            throw new NotFoundException("No note found with id: " + id);
        }
        return note;
    }

    @ApiMethod(
            name = "getNotesList",
            path = "getNotesList",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public List<Note> getNotesList(final User user) throws UnauthorizedException, NotFoundException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        String userId = user.getUserId();
        Key<Profile> profileKey = Key.create(Profile.class, userId);

        List<Note> list = ofy().load().type(Note.class)
                .ancestor(profileKey)
                .order("text").list();
        if (list == null || list.size() == 0) {
            throw new NotFoundException("Not found any notes entity in DB.");
        }
        return list;
    }
}
