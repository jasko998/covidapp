package com.turtleshelldevelopment.utils.db;

import com.turtleshelldevelopment.BackendServer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Patient {

    private final int id;
    private final int SSNumber;
    private final String firstName;
    private final String middleName;
    private final String lastName;
    private final String email;
    private final String gender;
    private final Date birthDate;

    public static Patient getPatient(String id) {
        try(Connection databaseConnection = BackendServer.database.getDatabase().getConnection(); CallableStatement getPatients = databaseConnection.prepareCall("CALL GET_PATIENT(?)");) {
            getPatients.setInt(1, Integer.parseInt(id));
            ResultSet res = getPatients.executeQuery();
            if(res.next()) {
                Patient pat = new Patient(res.getInt("patient_id"), res.getString("first_name"),
                        res.getString("middle_name"), res.getString("last_name"),
                        res.getInt("last_ss_num"), res.getDate("dob"),
                        res.getString("email"), res.getString("gender"));
                getPatients.close();

                return pat;
            } else {
                getPatients.close();
                System.out.println("Result set is empty");
                return null;
            }
        } catch (SQLException e) {
            BackendServer.serverLogger.error("Error: {}", e.getMessage());
            return null;
        }
    }


    public Patient(int id, String firstName, String middleName, String lastName, int SSNumber, Date birthDate, String email, String gender) {
        this.id = id;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.SSNumber = SSNumber;
        this.birthDate = birthDate;
        this.email = email;
        this.gender = gender;
    }

    public int getId() {
        return id;
    }

    public int getSSNumber() {
        return SSNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public List<Insurance> getInsurances() {
        List<Insurance> insurances = new ArrayList<>();
        try(Connection databaseConnection = BackendServer.database.getDatabase().getConnection(); CallableStatement getInsurances = databaseConnection.prepareCall("CALL GET_INSURANCES(?)")) {
            getInsurances.setInt(1, this.id);
            ResultSet res = getInsurances.executeQuery();
            while(res.next()) {
                insurances.add(new Insurance(res.getInt("insurance_id"), res.getString("provider"), res.getString("group_number"),
                        res.getString("policy_number")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return insurances;
        }
        return insurances;
    }

    public List<Contact> getContacts() {
        List<Contact> contacts = new ArrayList<>();
        try(Connection databaseConnection = BackendServer.database.getDatabase().getConnection(); CallableStatement getInsurances = databaseConnection.prepareCall("CALL GET_CONTACTS(?)");) {
            getInsurances.setInt(1, this.id);
            ResultSet res = getInsurances.executeQuery();
            while(res.next()) {
                contacts.add(new Contact(res.getInt("id"), res.getInt("patient_id"),
                        res.getString("address"), res.getString("phone_num"), res.getString("phone_type")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return contacts;
        }
        return contacts;
    }

    public List<Vaccine> getVaccines() {
        List<Vaccine> vaccines = new ArrayList<>();
        try (Connection databaseConnection = BackendServer.database.getDatabase().getConnection(); CallableStatement getInsurances = databaseConnection.prepareCall("CALL GET_VACCINE_INFO(?)")) {
            getInsurances.setInt(1, this.id);
            ResultSet res = getInsurances.executeQuery();
            while(res.next()) {
                vaccines.add(
                        new Vaccine(
                            res.getString("lot_num"),
                            res.getInt("site_id"),
                            res.getInt("patient_id"),
                            res.getDate("administered_date"),
                            res.getString("manufacturer"),
                            res.getString("dose"),
                            res.getInt("administrated_by"),
                            new ProvidingUser(
                                res.getString("first_name"),
                                res.getString("last_name"),
                                res.getString("email")
                            ),
                            new Site(
                                res.getInt("site_id"),
                                res.getString("location"),
                                res.getString("phone_number"),
                                res.getString("fips"),
                                res.getString("zip_code"),
                                res.getString("name")
                            )
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return vaccines;
        }
        return vaccines;
    }
}
