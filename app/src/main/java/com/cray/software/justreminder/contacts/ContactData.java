package com.cray.software.justreminder.contacts;

public class ContactData {
    private String name;
    private String uri;
    private int id;

    public ContactData(String name, String uri, int id) {
        this.name = name;
        this.uri = uri;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
