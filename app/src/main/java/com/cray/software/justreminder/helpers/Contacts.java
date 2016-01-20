package com.cray.software.justreminder.helpers;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import java.io.IOException;

/**
 * Helper class for accessing to contacts.
 */
public class Contacts {

    private Context mContext;

    public Contacts(Context context){
        this.mContext = context;
    }

    /**
     * Get photo of contact.
     * @param context application context.
     * @param contactId contact identifier.
     * @return Photo in bitmap format
     */
    public static Bitmap getPhoto(Context context, long contactId) {
        Bitmap bmp = null;
        if (contactId != 0) {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            try {
                AssetFileDescriptor fd =
                        context.getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
                bmp = BitmapFactory.decodeStream(fd.createInputStream());
            } catch (IOException e) {
                return null;
            }
        }
        return bmp;
    }

    /**
     * Get contact identifier by contact name.
     * @param name contact name.
     * @param context application context.
     * @return Contact identifier
     */
    public static int getIdFromNumber(String name, Context context) {
        int phoneContactID = 0;
        try {
            String contact = Uri.encode(name);
            Cursor contactLookupCursor = context.getContentResolver()
                    .query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact),
                            new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID},
                            null, null, null);
            while(contactLookupCursor.moveToNext()){
                phoneContactID = contactLookupCursor.getInt(
                        contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
            }
            contactLookupCursor.close();
        } catch (IllegalArgumentException iae) {
            return 0;
        }
        return phoneContactID;
    }

    /**
     * Get contact identifier by contact e-mail.
     * @param e_mail contact e-mail.
     * @param context application context.
     * @return Contact identifier
     */
    public static int getIdFromMail(String e_mail, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI, Uri.encode(e_mail));
        int contactId =0;

        ContentResolver contentResolver = context.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {
                ContactsContract.Data.CONTACT_ID}, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                contactId = contactLookup.getInt(contactLookup.getColumnIndex(ContactsContract.Data.CONTACT_ID));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return contactId;
    }

    /**
     * Get contact name by contact e_mail.
     * @param e_mail contact e-mail.
     * @param context application context.
     * @return Contact identifier
     */
    public static String getNameFromMail(String e_mail, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI, Uri.encode(e_mail));
        String name = "?";

        ContentResolver contentResolver = context.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {
                ContactsContract.Data.DISPLAY_NAME }, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex( ContactsContract.Data.DISPLAY_NAME ));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return name;
    }

    /**
     * Get contact name by contact number.
     * @param contactNumber contact number.
     * @param context application context.
     * @return Contact name
     */
    public static String getNameFromNumber(String contactNumber, Context context) {
        String phoneContactID = null;
        if (contactNumber != null) {
            try {
            String contact = Uri.encode(contactNumber);
            Cursor contactLookupCursor = context.getContentResolver().query(
                    Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact),
                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID},
                    null, null, null);
            while (contactLookupCursor.moveToNext()) {
                phoneContactID = contactLookupCursor.getString(
                        contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            contactLookupCursor.close();
            } catch (IllegalArgumentException iae) {
                return phoneContactID;
            }
        }
        return phoneContactID;
    }

    /**
     * Get e=mail for contact.
     * @param id contact identifier.
     * @return
     */
    public String getMail(int id){
        String mail = null;
        if (id != 0) {
            ContentResolver cr = mContext.getContentResolver();
            Cursor emailCur = cr.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    new String[]{String.valueOf(id)}, null);
            while (emailCur.moveToNext()) {
                mail = emailCur.getString(
                        emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            }
            emailCur.close();
        }
        return mail;
    }

    /**
     * Get contact number bu contact name.
     * @param name contact name.
     * @param context application context.
     * @return
     */
    public static String getNumber(String name, Context context) {
        String number="";
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like '%" + name +"%'";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null);
        if (c != null && c.moveToFirst()) {
            number = c.getString(0);
            c.close();
        }
        if (number == null){
            number = "noNumber";
        }
        return number;
    }
}
