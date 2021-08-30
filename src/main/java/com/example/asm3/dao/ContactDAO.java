package com.example.asm3.dao;

import com.example.asm3.controller.GroupController;
import com.example.asm3.entity.Contact;
import com.example.asm3.entity.Group;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;


public class ContactDAO {

    private static final File FILE = new File("./src/main/java/com/example/asm3/data/contacts.txt");
    private static final String PATH = FILE.getAbsolutePath();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    static ObservableList<Contact> contacts;
    static ObservableList<Group> groups;

    static {
        try {
            groups = GroupDAO.loadGroup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ObservableList<Contact> getContacts() {
        return contacts;
    }

    public static ObservableList<Contact> loadContacts() throws IOException {
        contacts = FXCollections.observableArrayList();
        Scanner sc = null;

        try {
            sc = new Scanner(new FileReader(PATH));
            sc.useDelimiter(",,,");
            while(sc.hasNextLine()){
                sc.skip(sc.delimiter());
                String firstName = sc.next().trim();
                String lastName = sc.next();
                String phone = sc.next();
                String email = sc.next();
                LocalDate dob =  LocalDate.parse(sc.next(), FORMATTER);
                String groupName = sc.next().trim();
                int index = GroupController.findIndexByGroupName(groupName);
                Contact contact = new Contact();
                if (index != -1) {
                    Group group = groups.get(index);
                    contact = new Contact(firstName, lastName, phone, email, dob, group);
                } else {
                    contact = new Contact(firstName, lastName, phone, email, dob);
                }
                contacts.add(contact);

//                 FOR DEBUG
//                System.out.println("FirstName " + sc.next().trim());
//                System.out.println("LastName " + sc.next());
//                System.out.println("phone " + sc.next());
//                System.out.println("Email " + sc.next());
//                System.out.println("Dob " + sc.next());
//                System.out.println("Group " + sc.next());
//                System.out.println("-----------------");
            }
        } finally {
          sc.close();
        }
        return contacts;
    }

    public static void saveContactsToFile() throws IOException {
        Writer wr = null;
        String firstName, lastName, phone, email, dob, group;
        StringBuilder result;
        try {
            wr = new FileWriter(FILE);
            for(Contact contact : contacts) {
                firstName = contact.getFirstName();
                lastName = contact.getLastName();
                phone = contact.getPhone();
                email = contact.getEmail();
                dob = contact.getDob().format(FORMATTER);
                group = contact.getGroup().getName();

                result = new StringBuilder(",,," + firstName + ",,," + lastName + ",,," + phone
                        + ",,," + email + ",,," + dob + ",,," + group + "\n");
                wr.write(result.toString());
            }
        } finally {
            wr.close();
        }
    }
}
