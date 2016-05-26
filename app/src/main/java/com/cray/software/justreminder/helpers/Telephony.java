/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * Helper class to interact with calls, messages and emails.
 */
public class Telephony {

    public Telephony(){}

    /**
     * Open email client for sending note.
     * @param file file to attach to mail.
     * @param context application context.
     */
    public static void sendNote(File file, Context context, String message){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String title = "Note";
        String note = "";
        if (message != null) {
            if (message.length() > 100) {
                title = message.substring(0, 48);
                title = title + "...";
            }
            if (message.length() > 150) {
                note = message.substring(0, 135);
                note = note + "...";
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, note);
        Uri uri = Uri.fromFile(file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, "Send email..."));
    }

    /**
     * Open email client for sending new mail.
     * @param context application context.
     * @param email email to send.
     * @param subject mail subject.
     * @param message mail text.
     */
    public static void sendMail(Context context, String email, String subject,
                                String message, String filePath){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        if (filePath != null) {
            Uri uri = Uri.fromFile(new File(filePath));
            if (uri != null) intent.putExtra(Intent.EXTRA_STREAM, uri);
        }
        context.startActivity(Intent.createChooser(intent, "Send email..."));
    }

    /**
     * Open default SMS messenger for sending new message.
     * @param number number to send.
     * @param context application context.
     */
    public static void sendSms(String number, Context context){
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setData(Uri.parse("sms:" + number));
        context.startActivity(smsIntent);
    }

    /**
     * Start calling to contact.
     * @param number number to call.
     * @param context application context.
     */
    public static void makeCall(String number, Context context){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        context.startActivity(callIntent);
    }

    /**
     * Open application.
     * @param appPackage application package name.
     * @param context application context.
     */
    public static void openApp(String appPackage, Context context) {
        Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(appPackage);
        context.startActivity(LaunchIntent);
    }

    /**
     * Open link in browser.
     * @param link link to open.
     * @param context application context.
     */
    public static void openLink(String link, Context context) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        context.startActivity(browserIntent);
    }

    /**
     * Open Skype client for new Voice call.
     * @param number skype contact.
     * @param context application context.
     */
    public static void skypeCall(String number, Context context){
        String uri = "skype:" + number + "?call";
        Intent sky = new Intent("android.intent.action.VIEW");
        sky.setData(Uri.parse(uri));
        context.startActivity(sky);
    }

    /**
     * Open Skype client for new Video call.
     * @param number skype contact.
     * @param context application context.
     */
    public static void skypeVideoCall(String number, Context context){
        String uri = "skype:" + number + "?call&video=true";
        Intent sky = new Intent("android.intent.action.VIEW");
        sky.setData(Uri.parse(uri));
        context.startActivity(sky);
    }

    /**
     * Open Skype client with new chat window.
     * @param number skype contact.
     * @param context application context.
     */
    public static void skypeChat(String number, Context context){
        String uri = "skype:" + number + "?chat";
        Intent sky = new Intent("android.intent.action.VIEW");
        sky.setData(Uri.parse(uri));
        context.startActivity(sky);
    }
}
