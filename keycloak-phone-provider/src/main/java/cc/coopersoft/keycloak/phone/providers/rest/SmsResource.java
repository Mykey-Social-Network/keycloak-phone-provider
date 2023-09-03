package cc.coopersoft.keycloak.phone.providers.rest;

import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import org.keycloak.models.KeycloakSession;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class SmsResource {

    private final KeycloakSession session;

    public SmsResource(KeycloakSession session) {
        this.session = session;
    }

    @Path("verification-code")
    public VerificationCodeResource getVerificationCodeResource() {
        return new VerificationCodeResource(session);
    }

    @Path("authentication-code")
    public TokenCodeResource getAuthenticationCodeResource() {
        return new TokenCodeResource(session, TokenCodeType.AUTH);
    }
//    @Path("authentication-code")
//    @GET
//    public Response getAuthenticationCodeResource() {
////        return Response.ok(new TokenCodeResource(session, TokenCodeType.AUTH), MediaType.APPLICATION_JSON_TYPE)
////                .header("Access-Control-Allow-Origin", "*")
////                .header("Access-Control-Allow-Methods", "GET, OPTIONS")  // Adjust allowed methods
////                .header("Access-Control-Allow-Headers", "origin, content-type, accept")  // Adjust allowed headers
////                .build();
//        return new TokenCodeResource(session, TokenCodeType.AUTH);
//    }

    @Path("registration-code")
    public TokenCodeResource getRegistrationCodeResource() {
        return new TokenCodeResource(session, TokenCodeType.REGISTRATION);
    }

    @Path("reset-code")
    public TokenCodeResource getResetCodeResource() {
        return new TokenCodeResource(session, TokenCodeType.RESET);
    }

    @Path("otp-code")
    public TokenCodeResource getOTPCodeResource() {
        return new TokenCodeResource(session, TokenCodeType.OTP);
    }

}
