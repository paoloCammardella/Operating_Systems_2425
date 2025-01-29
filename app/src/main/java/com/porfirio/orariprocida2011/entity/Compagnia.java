package com.porfirio.orariprocida2011.entity;

import java.util.ArrayList;
import java.util.Objects;

public class Compagnia {

    private final String id;
    private final String name;
    private final ArrayList<String> contactsNames;
    private final ArrayList<String> contactsNumbers;

    public Compagnia(String id, String name) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.contactsNames = new ArrayList<>(4);
        this.contactsNumbers = new ArrayList<>(4);
    }

    public void addContact(String name, String number) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(number);

        contactsNames.add(name);
        contactsNumbers.add(number);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContactName(int index) {
        return contactsNames.get(index);
    }

    public String getContactNumber(int index) {
        return contactsNumbers.get(index);
    }

    public int getContactsCount() {
        return contactsNames.size();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
