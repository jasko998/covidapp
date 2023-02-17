package com.turtleshelldevelopment;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.turtleshelldevelopment.endpoints.*;
import com.turtleshelldevelopment.pages.*;
import com.turtleshelldevelopment.utils.Issuers;
import com.turtleshelldevelopment.utils.ModelUtil;
import com.turtleshelldevelopment.utils.TokenUtils;
import com.turtleshelldevelopment.utils.permissions.PermissionType;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import static com.turtleshelldevelopment.EndpointFilters.verifyCredentials;
import static spark.Service.ignite;
import static spark.Spark.*;

public class Routing {

    public Routing() {
        port(8091);
        exception(Exception.class, (exception, request, response) -> BackendServer.serverLogger.error(exception.getMessage()));
        staticFileLocation("/frontend");
        notFound(new NotFoundPage());
        BackendServer.serverLogger.info("Setting up API Server routes...");
        path("/", () -> {
            get("/", (req, resp) -> {
                if(req.cookie("token") != null) {
                    DecodedJWT jwt = JWT.decode(req.cookie("token"));
                    if(jwt.getIssuer().equals(Issuers.AUTHENTICATION.getIssuer())) {
                        System.out.println("Token exists with auth, proceeding to dashboard");
                        resp.redirect("/dashboard");
                        return null;
                    } else {
                        return new VelocityTemplateEngine().render(new ModelAndView(new ModelUtil(req).build(), "/frontend/index.vm"));
                    }
                }
                return new VelocityTemplateEngine().render(new ModelAndView(new ModelUtil(req).build(), "/frontend/index.vm"));
            });
            before("/dashboard", EndpointFilters::verifyCredentials);
            get("/dashboard", new DashboardPage());
            before("/print_record/print", EndpointFilters::verifyCredentials);
            post("/print_record/print", new PrintInfoPage());
            createAPIRoutes();
            createContactRoutes();
            createInsuranceRoutes();
            createRecordRoutes();
            createPatientRoutes();
            createSiteRoutes();
            createUserRoutes();
            createVaccineRoutes();
            createReportRoutes();
        });
        BackendServer.serverLogger.info("Ready to Fire");
    }

    public void createAPIRoutes() {
        before("/api/*", EndpointFilters::verifyCredentials);
        path("/api", () -> get("/lookupAddress", new GeocodingEndpoint()));
    }

    public void createSiteRoutes() {
        path("/site", () -> {
            before("/add", (req, res) -> verifyCredentials(req, res, PermissionType.ADD_SITE));
            get("/add", new SiteCreatePage());
            post("/add", new NewSiteEndpoint());
        });
    }

    public void createInsuranceRoutes() {
        before("/insurance/*", EndpointFilters::verifyCredentials);
        path("/insurance", () -> {
            post("/add/:id", new AddInsuranceInformationEndpoint());
            get("/add/:id", new AddInsuranceInformationPage());
            get("/remove/:user_id/:id", new DeleteInsuranceInformationEndpoint());
        });
    }

    public void createUserRoutes() {
        path("/user", () -> {
            before("/add", (req, resp) -> verifyCredentials(req, resp, PermissionType.ADD_USER));
            get("/add", new UserCreatePage());
            post("/add", new NewAccountEndpoint());
            before("/edit", (req, resp) -> verifyCredentials(req, resp, PermissionType.EDIT_USER));
            get("/edit", new UserEditPage());
            patch("/edit/:id", new UserAccountEditEndpoint());
            get("/disable/:id", new DisableUserAccountEndpoint());
            get("/enable/:id", new EnableUserAccountEndpoint());
            before("/permission/set/:id", (req, resp) -> verifyCredentials(req, resp, PermissionType.EDIT_USER));
            post("/permission/set/:id", new ChangePermissionEndpoint());
            before("/site/set/:id", (req, resp) -> verifyCredentials(req, resp, PermissionType.EDIT_USER));
            post("/site/set/:id", new ChangeSiteEndpoint());
            path("/login", () -> {
                before("/mfa", (req, res) -> {
                    TokenUtils tokenUtils = new TokenUtils(req.cookie("token"), Issuers.MFA_LOGIN.getIssuer());
                    if (tokenUtils.isInvalid()) {
                        //Invalid token, Remove it
                        res.cookie("/", "token", null, 0, true, true);
                        halt(401, new ModelUtil(req).addMFAError(false, "Invalid Token", false).toJSONString());
                    }
                });
                post("/mfa", new MfaEndpoint());
            });
            path("/mfa", () -> {
                post("/generate", new GenerateMFAEndpoint());
                post("/validate", new ValidateGeneratedMFAEndpoint());
            });
            get("/refresh", new RefreshTokenEndpoint());
            post("/login", new LoginEndpoint());
            before("/logout", EndpointFilters::verifyCredentials);
            get("/logout", new LogoutEndpoint());
        });
    }

    public void createPatientRoutes() {
        before("/patient", (req, resp) -> EndpointFilters.verifyCredentials(req, resp, PermissionType.READ_PATIENT));
        path("/patient", () -> {
            get("/view/:id", new ViewPatientPage());
            get("/print/:id", new PrintRecordPage());
        });
    }

    public void createVaccineRoutes() {
        before("/vaccine/*", (req, resp) -> EndpointFilters.verifyCredentials(req, resp, PermissionType.EDIT_PATIENT));
        path("/vaccine", () -> {
           get("/add/:id", new NewDosePage());
           post("/add/:id", new AddVaccineEndpoint());
        });
    }

    public void createContactRoutes() {
        before("/contact/*", (req, resp) -> EndpointFilters.verifyCredentials(req, resp, PermissionType.EDIT_PATIENT));
        path("/contact", () -> {
            //Move to delete later
            get("/remove/:user_id/:id", new DeleteContactInformationEndpoint());
            get("/add/:id", new AddContactPage());
            post("/add/:id", new AddContactEndpoint());
        });
    }

    public void createRecordRoutes() {
        path("/record", () -> {
            before("/add", (req, resp) -> verifyCredentials(req, resp, PermissionType.WRITE_PATIENT));
            get("/add", new AddRecordPage());
            post("/add", new AddRecordEndpoint());
            before("/edit/:id", (req, resp) -> EndpointFilters.verifyCredentials(req, resp, PermissionType.EDIT_PATIENT));
            post("/edit/:id", new UpdateRecordEndpoint());
            before("/search", (req, resp) -> EndpointFilters.verifyCredentials(req, resp, PermissionType.READ_PATIENT));
            get("/search", new SearchRecordPage());
            post("/search", new SearchPatientsEndpoint());
        });
    }

    public void createReportRoutes() {
        before("/report/*", (req, resp) -> EndpointFilters.verifyCredentials(req, resp, PermissionType.REPORTS));
        path("/report", () -> {
            get("/daily", new DailyReportPage());
            post("/daily/generate", new DailyReportEndpoint());
            get("/weekly", new WeeklyReportPage());
            post("/weekly/generate", new WeeklyReportEndpoint());
        });
    }
    public void fire() {
        ignite();
        BackendServer.serverLogger.info("We have Lift off!");
    }
}
