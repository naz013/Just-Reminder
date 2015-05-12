package com.cray.software.justreminder.cloud;

import android.content.Context;

import com.cray.software.justreminder.helpers.SharedPrefs;

import java.io.IOException;

public class ExchangeHelper {

    Context ctx;
    SharedPrefs prefs;

    //ExchangeCredentials credentials;
    //ExchangeService service;

    public ExchangeHelper(Context context){
        this.ctx = context;
    }

    public void authorize(){
        /*service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        TasksData data = new TasksData(ctx);
        data.open();
        Cursor c = data.getAccount();
        SyncHelper helper = new SyncHelper(ctx);
        if (c != null && c.moveToFirst()) {
            String login = helper.decrypt(c.getString(c.getColumnIndex(ExchangeConstants.COLUMN_USER)));
            String password = helper.decrypt(c.getString(c.getColumnIndex(ExchangeConstants.COLUMN_PASSWORD)));
            String domain = c.getString(c.getColumnIndex(ExchangeConstants.COLUMN_DOMAIN));
            credentials = new WebCredentials(login, password);
            service.setCredentials(credentials);
            if (domain != null && !domain.matches("")) {
                try {
                    service.setUrl(new URI("https://" + domain + "/EWS/Exchange.asmx"));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    service.setUrl(new URI("https://outlook.office365.com/EWS/Exchange.asmx"));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }*/
    }

    public boolean isLinked(){
        /*TasksData data = new TasksData(ctx);
        data.open();
        Cursor c = data.getAccount();
        return c != null;*/
        return false;
    }

    public void insertTask(String taskTitle, String listId, long time, String note) throws Exception {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            //Task task = new Task(service);
        }
    }

    public void updateTaskStatus(String status, String listId, String taskId) throws IOException {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();

        }
    }

    public void deleteTask(String listId, String taskId) throws IOException {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
        }
    }

    public void updateTask(String text, String listId, String taskId, String note, long time) throws IOException {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();

        }
    }

    public void getTasks() throws Exception {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            /*Log.d(Constants.LOG_TAG, "Authorized");
            ItemView itemView = new ItemView(Integer.MAX_VALUE);
            FindItemsResults<Item> response = service.findItems(WellKnownFolderName.Tasks, itemView);
            Log.d(Constants.LOG_TAG, "Received items size " + response.getItems().size());
            TasksData data = new TasksData(ctx);
            data.open();
            Calendar calendar = Calendar.getInstance();
            for (Item item : response.getItems()) {
                ItemId id = item.getId();
                Task task = Task.bind(service, id);
                if (task != null){
                    String itemId = task.getId().toString();
                    String message = task.getBody().toString();
                    int actWork = task.getActualWork();
                    Date date = null;
                    try {
                        date = task.getAssignedTime();
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    if (date != null) calendar.setTime(date);
                    long assignTime = date != null ? calendar.getTimeInMillis() : 0;
                    int count = task.getChangeCount();
                    try {
                        date = task.getCompleteDate();
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    if (date != null) calendar.setTime(date);
                    long completeTime = date != null ? calendar.getTimeInMillis() : 0;
                    try {
                        date = task.getDueDate();
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    if (date != null) calendar.setTime(date);
                    long dueTime = date != null ? calendar.getTimeInMillis() : 0;

                    int isCompleted = task.getIsComplete() ? 1 : 0;
                    int isRecurring = task.getIsRecurring() ? 1 : 0;
                    int isTeam = task.getIsTeamTask() ? 1 : 0;

                    String mileage = task.getMileage();
                    double percent = task.getPercentComplete();
                    try {
                        date = task.getStartDate();
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    if (date != null) calendar.setTime(date);
                    long startTime = date != null ? calendar.getTimeInMillis() : 0;

                    String status = task.getStatus().toString();
                    int total = task.getTotalWork();

                    String statusDescr = task.getStatusDescription();
                    String owner = task.getOwner();
                    String mode = task.getMode().toString();

                    long idN = data.addExchangeTask(message, actWork, assignTime, count, completeTime, dueTime,
                            isCompleted, isRecurring, isTeam, mileage, percent, null, startTime,
                            status, total, statusDescr, owner, mode, itemId);

                    StringList contacts = task.getContacts();
                    if (contacts != null && contacts.getSize() > 0){
                        for (String value : contacts){
                            data.addContact(idN, value);
                        }
                    }

                    StringList companies = task.getCompanies();
                    if (companies != null && companies.getSize() > 0){
                        for (String value : companies){
                            data.addCompany(idN, value);
                        }
                    }
                }
            }*/
        }
    }
}
