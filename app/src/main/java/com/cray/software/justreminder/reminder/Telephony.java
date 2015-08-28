package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

public class Telephony {
    public Telephony(){}

    public static void sendMail(File file, Context context){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Note");
        Uri uri = Uri.fromFile(file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, "Send email..."));
    }

    public static void sendSms(String number, Context context){
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setData(Uri.parse("sms:" + number));
        context.startActivity(smsIntent);
    }

    public static void makeCall(String number, Context context){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        context.startActivity(callIntent);
    }

    public static void openApp(String appName, Context context) {
        Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(appName);
        context.startActivity(LaunchIntent);
    }

    public static void openLink(String link, Context context) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        context.startActivity(browserIntent);
    }

    public static void skypeCall(String number, Context context){
        String uri = "skype:" + number + "?call";
        Intent sky = new Intent("android.intent.action.VIEW");
        sky.setData(Uri.parse(uri));
        context.startActivity(sky);
    }

    public static void skypeVideoCall(String number, Context context){
        String uri = "skype:" + number + "?call&video=true";
        Intent sky = new Intent("android.intent.action.VIEW");
        sky.setData(Uri.parse(uri));
        context.startActivity(sky);
    }

    public static void skypeChat(String number, Context context){
        String uri = "skype:" + number + "?chat";
        Intent sky = new Intent("android.intent.action.VIEW");
        sky.setData(Uri.parse(uri));
        context.startActivity(sky);
    }
}
