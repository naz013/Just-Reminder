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

public class Contacts {
    Context cContext;
    public Contacts(Context context){
        this.cContext = context;
    }

    public Bitmap openPhoto(long contactId) {
        Bitmap bmp = null;
        if (contactId != 0) {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            try {
                AssetFileDescriptor fd =
                        cContext.getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
                bmp = BitmapFactory.decodeStream(fd.createInputStream());
            } catch (IOException e) {
                return null;
            }
        }
        return bmp;
    }

    public int getContactIDFromNumber(String contactNumber,Context context) {
        int phoneContactID = 0;
        try {
            String contact = Uri.encode(contactNumber);
            Cursor contactLookupCursor = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact),new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
            while(contactLookupCursor.moveToNext()){
                phoneContactID = contactLookupCursor.getInt(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
            }
            contactLookupCursor.close();
        } catch (IllegalArgumentException iae) {
            return 0;
        }
        return phoneContactID;
    }

    public String getContactNameFromNumber(String contactNumber,Context context) {
        String phoneContactID = null;
        if (contactNumber != null) {
            try {
            String contact = Uri.encode(contactNumber);
            Cursor contactLookupCursor = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact), new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
            while (contactLookupCursor.moveToNext()) {
                phoneContactID = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            contactLookupCursor.close();
            } catch (IllegalArgumentException iae) {
                return phoneContactID;
            }
        }
        return phoneContactID;
    }

    public String getMail(int id){
        String mail = null;
        if (id != 0) {
            ContentResolver cr = cContext.getContentResolver();
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

    public String get_Number(String name, Context context) {
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
