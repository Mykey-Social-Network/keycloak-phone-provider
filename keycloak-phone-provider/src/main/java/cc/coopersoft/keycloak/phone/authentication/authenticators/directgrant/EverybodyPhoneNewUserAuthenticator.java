package cc.coopersoft.keycloak.phone.authentication.authenticators.directgrant;

import cc.coopersoft.common.OptionalUtils;
import cc.coopersoft.keycloak.phone.Utils;
import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.representations.TokenCodeRepresentation;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneVerificationCodeProvider;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.Nullable;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;

import java.util.Optional;


public class EverybodyPhoneNewUserAuthenticator extends BaseDirectGrantAuthenticator {

    private static final Logger logger = Logger.getLogger(EverybodyPhoneNewUserAuthenticator.class);

    public EverybodyPhoneNewUserAuthenticator(KeycloakSession session) {
        if (session.getContext().getRealm() == null) {
            throw new IllegalStateException("The service cannot accept a session without a realm in its context.");
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

//  @Override
//  public void authenticate(AuthenticationFlowContext context) {
//    getPhoneNumber(context)
//        .ifPresentOrElse(phoneNumber -> getAuthenticationCode(context)
//                .ifPresentOrElse(code -> authToUser(context, phoneNumber, code),
//                    ()-> invalidCredentials(context)),
//            () -> invalidCredentials(context));
//  }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        getFirstName(context)
                .ifPresentOrElse(firstName -> getLastName(context)
                                .ifPresentOrElse(lastName -> getPhoneNumber(context)
                                                .ifPresentOrElse(phoneNumber -> getAuthenticationCode(context)
                                                                .ifPresentOrElse(code -> authToUser(context, phoneNumber, firstName,lastName , code),
                                                                        () -> invalidCredentials(context)),
                                                        () -> invalidCredentials(context)),
                                        () -> invalidCredentials(context)),
                        () -> invalidCredentials(context))
        ;
    }

    private void authToUser(AuthenticationFlowContext context, String phoneNumber, String firstName, String lastName, String code) {
        PhoneVerificationCodeProvider phoneVerificationCodeProvider = context.getSession().getProvider(PhoneVerificationCodeProvider.class);
        TokenCodeRepresentation tokenCode = phoneVerificationCodeProvider.ongoingProcess(phoneNumber, TokenCodeType.AUTH);

        if (tokenCode == null || !tokenCode.getCode().equals(code)) {
            invalidCredentials(context);
            return;
        }

        UserModel user = Utils.findUserByPhone(context.getSession(), context.getRealm(), phoneNumber)
                .orElseGet(() -> {
                    return getUserModelByPhoneNumberAsUserName(context, phoneNumber, firstName, lastName);
                });
        if (user != null) {
            context.setUser(user);
            phoneVerificationCodeProvider.tokenValidated(user, phoneNumber, tokenCode.getId(), false);
            context.success();
        }
    }


//    @Nullable
//    private UserModel getUserModelByNewUserName(AuthenticationFlowContext context, String newUserName) {
//        return getUserModelUserName(context,newUserName);
//    }

    @Nullable
    private UserModel getUserModelByPhoneNumberAsUserName(AuthenticationFlowContext context, String phoneNumber, String firstName, String lastName) {
        return getUserModelUserName(context, phoneNumber, firstName, lastName);
    }

    @Nullable
    private UserModel getUserModelUserName(AuthenticationFlowContext context, String userName, String firstName, String lastName) {
        if (context.getSession().users().getUserByUsername(context.getRealm(), userName) != null) {
            invalidCredentials(context, AuthenticationFlowError.USER_CONFLICT);
            return null;
        }
        UserModel newUser = context.getSession().users().addUser(context.getRealm(), userName);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEnabled(true);
        context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, userName);
        return newUser;
    }


    private Optional<String> getFirstName(AuthenticationFlowContext context) {
        return OptionalUtils.ofBlank(context.getHttpRequest().getDecodedFormParameters().getFirst("first_name"));

    }

    private Optional<String> getLastName(AuthenticationFlowContext context) {
        return OptionalUtils.ofBlank(context.getHttpRequest().getDecodedFormParameters().getFirst("last_name"));

    }

}
