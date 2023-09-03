package cc.coopersoft.keycloak.phone.authentication.authenticators.conditional;

import cc.coopersoft.common.OptionalUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.authentication.authenticators.conditional.ConditionalUserAttributeValueFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class ConditionalNewUserProvided implements ConditionalAuthenticator {

    static final ConditionalNewUserProvided SINGLETON = new ConditionalNewUserProvided();

    @Override
    public boolean matchCondition(AuthenticationFlowContext context) {
        var config = context.getAuthenticatorConfig().getConfig();
        boolean negateOutput = Boolean.parseBoolean(config.getOrDefault(ConditionalUserAttributeValueFactory.CONF_NOT, "false"));

        boolean hasFirstName = OptionalUtils.ofBlank(
                context.getHttpRequest().getDecodedFormParameters().getFirst("first_name")).isPresent();
        boolean hasLastName = OptionalUtils.ofBlank(
                context.getHttpRequest().getDecodedFormParameters().getFirst("last_name")).isPresent();
        var result = hasFirstName && hasLastName;
        return negateOutput != result;
    }

    @Override
    public void action(AuthenticationFlowContext authenticationFlowContext) {
        // Not used
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        // Not used
    }

    @Override
    public void close() {
        // Does nothing
    }
}
