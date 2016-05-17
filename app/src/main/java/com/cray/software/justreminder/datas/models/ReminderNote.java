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

package com.cray.software.justreminder.datas.models;

public class ReminderNote {
    private String noteText;
    private byte[] image;
    private int color;
    private String taskTitle, taskNote, taskStatus, taskListId, taskIdentifier;
    private long taskDate;
    private long noteId, taskId;

    public ReminderNote(){
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public String getTaskIdentifier(){
        return taskIdentifier;
    }

    public void setTaskIdentifier(String taskIdentifier){
        this.taskIdentifier = taskIdentifier;
    }

    public String getTaskListId(){
        return taskListId;
    }

    public void setTaskListId(String taskListId){
        this.taskListId = taskListId;
    }

    public String getNoteText(){
        return noteText;
    }

    public void setNoteText(String noteText){
        this.noteText = noteText;
    }

    public byte[] getImage(){
        return image;
    }

    public void setImage(byte[] image){
        this.image = image;
    }

    public String getTaskTitle(){
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle){
        this.taskTitle = taskTitle;
    }

    public String getTaskNote(){
        return taskNote;
    }

    public void setTaskNote(String taskNote){
        this.taskNote = taskNote;
    }

    public String getTaskStatus(){
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus){
        this.taskStatus = taskStatus;
    }

    public long getTaskDate(){
        return taskDate;
    }

    public void setTaskDate(long taskDate){
        this.taskDate = taskDate;
    }

    public long getNoteId(){
        return noteId;
    }

    public void setNoteId(long noteId){
        this.noteId = noteId;
    }

    public long getTaskId(){
        return taskId;
    }

    public void setTaskId(long taskId){
        this.taskId = taskId;
    }
}
