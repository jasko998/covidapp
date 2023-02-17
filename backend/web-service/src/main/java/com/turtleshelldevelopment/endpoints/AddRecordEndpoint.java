package com.turtleshelldevelopment.endpoints;

import com.turtleshelldevelopment.BackendServer;
import com.turtleshelldevelopment.Constants;
import com.turtleshelldevelopment.utils.*;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

import java.sql.*;
import java.time.*;
import java.util.regex.Pattern;

//*******************************************************************
//*                                                                 *
//* Created By: Entry Point Route                                   *
//* Created On: 10/14/2022, 1:17:25 PM                              *
//* Last Modified By: Colin Kinzel                                  *
//* Last Modified On: 10/14/2022, 1:17:25 PM                        *
//* Description: Handles Adding a Patient Entry into the database   *
//*                                                                 *
//*******************************************************************
public class AddRecordEndpoint implements Route {
    @Override
    public Object handle(Request request, Response response) {
        if(request.requestMethod().equalsIgnoreCase("POST")) {
            TokenUtils tokenVerifier = new TokenUtils(request.cookie("token"), Issuers.AUTHENTICATION.getIssuer());
            if(tokenVerifier.isInvalid()) {
                System.out.println("Token is invalid: " + tokenVerifier.getErrorReason());
                response.redirect("/");
                return ResponseUtils.createError("Invalid Token", 401, response);
            }

            System.out.println("Params are: " + request.queryParams());
            String date = request.queryParams("curr-date");
            String patientFirstName = request.queryParams("fname");
            String patientMiddleName = request.queryParams("mname");
            String patientLastName = request.queryParams("lname");
            String patientSSN = request.queryParams("ss4");
            String patientSex = request.queryParams("sex");
            try {
                Integer.parseInt(patientSSN);
            } catch (NumberFormatException e) {
                return ResponseUtils.createError("Invalid patient SSN", 400, response);
            }
            String patientBirthDate = request.queryParams("curr-date");

            //Patient Contact Info
            String contactAddress = request.queryParams("address");
            String contactPhone = request.queryParams("phone");
            String contactPhoneType = request.queryParams("PhoneType");
            String contactEmail = request.queryParams("email");

            String insuranceProviderName = request.queryParams("insProvider");
            String insuranceGroupNumber = request.queryParams("insGroup");
            String insurancePolicyNumber = request.queryParams("insPolicy");

            String vaccineManu = request.queryParams("vaxMan");
            String vaccineLotNum = request.queryParams("lotNumber");
            String vaccineSeries = request.queryParams("dose");
            int site, user;

            boolean shouldCreateContact = (contactEmail != null && !contactEmail.isEmpty()) || (contactPhone != null && !contactPhone.isEmpty()) || (contactAddress != null && !contactAddress.isEmpty());
            boolean shouldCreateInsurance = (insuranceProviderName != null && !insuranceProviderName.isEmpty()) && (insuranceGroupNumber != null && !insuranceGroupNumber.isEmpty()) && (insurancePolicyNumber != null && !insurancePolicyNumber.isEmpty());

            if(!FormValidator.checkValues()) {
                return ResponseUtils.createError("Missing Values", 400, response);
            }
            Pattern phoneNum = Pattern.compile("^\\([0-9]{3}\\) [0-9]{3} - [0-9]{4}$");
            if(contactPhone != null && !contactPhone.isEmpty()) {
                if (!phoneNum.matcher(contactPhone).matches()) {
                    return ResponseUtils.createError("Phone number not properly formatted", 400, response);
                } else {
                    contactPhone = Pattern.compile("[0-9]+").matcher(contactPhone).group();
                    System.out.println("phone is: " + contactPhone);
                }
            }
            LocalDate dateFiled;
            if((dateFiled = FormValidator.parseDateFromForm(date)) == null) {
                return ResponseUtils.createError("Invalid Date Filed", 400, response);
            }
            LocalDate dateOfBirth;
            if((dateOfBirth = FormValidator.parseDateFromForm(patientBirthDate)) == null) {
                return ResponseUtils.createError("Invalid Date of Birth", 400, response);
            }
            if(!Constants.CLIENT_SEX_IDENTITIES.contains(patientSex)) {
                return ResponseUtils.createError("Invalid Sex Identity", 400, response);
            }
            if(Constants.VACCINE_SERIES.get(vaccineSeries) == null) {
                return ResponseUtils.createError("Invalid Vaccine Series", 400, response);
            }
            if((user = tokenVerifier.getUserId()) == -1) {
                return ResponseUtils.createError("Invalid User", 400, response);
            }
            if((site = tokenVerifier.getSiteId()) == -1) {
                return ResponseUtils.createError("Invalid Site", 400, response);
            }
            try(Connection databaseConnection = BackendServer.database.getDatabase().getConnection();
                PreparedStatement createPatient = databaseConnection.prepareStatement("INSERT INTO PatientInformation(first_name, middle_name, last_name, last_ss_num, dob, email, gender) VALUES (?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                PreparedStatement createVaccine = databaseConnection.prepareStatement("INSERT INTO Vaccine(lot_num, site_id, patient_id, administered_date, manufacturer, dose, administrated_by) VALUES (?,?,?,?,?,?,?);");
                PreparedStatement createInsurance = databaseConnection.prepareStatement("INSERT INTO Insurance(patient_id, provider, group_number, policy_number) VALUES (?,?,?,?)");
                PreparedStatement createContact = databaseConnection.prepareStatement("INSERT INTO PatientContact(patient_id, address, phone_num, phone_type, inactive) VALUES(?,?,?,?,FALSE); ");

            ) {

                createPatient.setString(1, patientFirstName);
                createPatient.setString(2, patientMiddleName);
                createPatient.setString(3, patientLastName);
                createPatient.setInt(4, Integer.parseInt(patientSSN));
                ZoneOffset timeZone = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
                createPatient.setDate(5, new Date(dateOfBirth.toEpochSecond(LocalTime.now(), timeZone)));
                if(contactEmail != null && !contactEmail.isEmpty()) {
                    createPatient.setString(6, contactEmail);
                } else {
                    createPatient.setString(6, "");
                }
                createPatient.setString(7, patientSex);
                //DONE

                if(shouldCreateContact) {
                    if(contactAddress != null && !contactAddress.isEmpty()) {
                        createContact.setString(2, contactAddress);
                    } else {
                        createContact.setNull(2, Types.CHAR);
                    }
                    if(contactPhone != null && !contactPhone.isEmpty()) {
                        createContact.setString(3, contactPhone);
                        createContact.setString(4, contactPhoneType);
                    } else {
                        createContact.setNull(3, Types.CHAR);
                        createContact.setNull(4, Types.CHAR);
                    }
                }
                //DONE

                if(shouldCreateInsurance) {
                    createInsurance.setString(2, insuranceProviderName);
                    createInsurance.setString(3, insuranceGroupNumber);
                    createInsurance.setString(4, insurancePolicyNumber);
                }

                createVaccine.setString(1, vaccineLotNum);
                createVaccine.setInt(2, site);
                createVaccine.setDate(4, new Date(dateFiled.toEpochSecond(LocalTime.now(), timeZone)));
                createVaccine.setString(5, vaccineManu.substring(0, 1).toUpperCase() + vaccineManu.substring(1));
                createVaccine.setInt(6, Constants.VACCINE_SERIES.get(vaccineSeries));
                createVaccine.setInt(7, user);

                if(createPatient.executeUpdate() == 1) {
                    ResultSet generatedKeys = createPatient.getGeneratedKeys();
                    if(generatedKeys.next()) {
                        final int patientId = generatedKeys.getInt(1);
                        if(shouldCreateContact) {
                            createContact.setInt(1, patientId);
                            if (createContact.executeUpdate() == 0) {
                                return ResponseUtils.createError("Failed to create contact for patient", 500, response);
                            }
                        }
                        if(shouldCreateInsurance) {
                            createInsurance.setInt(1, patientId);
                            if(createInsurance.executeUpdate() == 0) {
                                return ResponseUtils.createError("Failed to create insurance for patient", 500, response);
                            }
                        }
                        createVaccine.setInt(3, patientId);
                        if(createVaccine.executeUpdate() == 0) {
                            return ResponseUtils.createError("Failed to create vaccine for patient", 500, response);
                        }
                    } else {
                        return ResponseUtils.createError("Failed to retrieve key for new patient", 500, response);
                    }
                }

                databaseConnection.close();
                return ResponseUtils.createSuccess("Added new patient", response);
            } catch (SQLException e) {
                e.printStackTrace();
                return new VelocityTemplateEngine().render(new ModelAndView(new ModelUtil(request).addError(500, "Failed to connect with database").build(), "/frontend/error.vm"));
            }
        }
        return ResponseUtils.createError("Failed to create patient", 500, response);
    }
}
