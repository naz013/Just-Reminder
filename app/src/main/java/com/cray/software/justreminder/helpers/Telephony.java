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
     * Open email client for sending new mail.
     * @param file file to attach to mail.
     * @param context application context.
     */
    public static void sendMail(File file, Context context){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Note");
        Uri uri = Uri.fromFile(file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
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