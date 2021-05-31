/* SHOGun, https://terrestris.github.io/shogun/
 *
 * Copyright © 2020-present terrestris GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.terrestris.shogun.lib.util;

import de.terrestris.shogun.lib.enumeration.PermissionCollectionType;
import de.terrestris.shogun.lib.event.OnRegistrationConfirmedEvent;
import de.terrestris.shogun.lib.model.Group;
import de.terrestris.shogun.lib.model.User;
import de.terrestris.shogun.lib.repository.GroupRepository;
import de.terrestris.shogun.lib.repository.UserRepository;
import de.terrestris.shogun.lib.security.SecurityContextUtil;
import de.terrestris.shogun.lib.service.security.permission.UserInstancePermissionService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Component
public class KeycloakUtil {

    @Autowired
    protected RealmResource keycloakRealm;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected GroupRepository groupRepository;

    @Autowired
    private KeycloakUtil keycloakUtil;

    @Autowired
    private SecurityContextUtil securityContextUtil;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private UserInstancePermissionService userInstancePermissionService;

    public UserResource getUserResource(User user) {
        UsersResource kcUsers = this.keycloakRealm.users();
        return kcUsers.get(user.getKeycloakId());
    }

    public UserResource getUserResource(String id) {
        UsersResource kcUsers = this.keycloakRealm.users();
        return kcUsers.get(id);
    }

    public GroupResource getGroupResource(Group group) {
        GroupsResource kcGroups = this.keycloakRealm.groups();
        return kcGroups.group(group.getKeycloakId());
    }

    public GroupResource getGroupResource(String id) {
        GroupsResource kcGroups = this.keycloakRealm.groups();
        return kcGroups.group(id);
    }

    public void addUserToGroup(User user, Group group) {
        UserResource kcUser = this.getUserResource(user);
        GroupResource kcGroup = this.getGroupResource(group);

        kcUser.joinGroup(kcGroup.toRepresentation().getId());
    }

    public void addUserToGroup(User user, GroupRepresentation kcGroup) {
        UserResource kcUser = this.getUserResource(user);

        kcUser.joinGroup(kcGroup.getId());
    }

    public GroupResource getResourceFromRepresentation(GroupRepresentation representation) {
        return this.keycloakRealm.groups().group(representation.getId());
    }

    public UserResource getResourceFromRepresentation(UserRepresentation representation) {
        return this.keycloakRealm.users().get(representation.getId());
    }

    public boolean addSubGroupToGroup(GroupRepresentation parentGroup, GroupRepresentation subGroup) {
        String subGroupName = subGroup.getName();
        String groupName = parentGroup.getName();
        GroupResource parentGroupResource = this.getResourceFromRepresentation(parentGroup);
        try (Response response = parentGroupResource.subGroup(subGroup)) {
            if (response.getStatusInfo().equals(Response.Status.NO_CONTENT)) {
                log.info("Added group " + subGroupName + " as SubGroup to " + groupName);
                return true;
            } else {
                String message = "Error adding group " + subGroupName + " as SubGroup to " + groupName +
                    ", Error Message : " + response;
                log.error(message);
                throw new WebApplicationException(message, response);
            }
        }
    }

    public List<GroupRepresentation> getGroupByName(String groupName) {
        GroupsResource kcGroupRepresentation = this.keycloakRealm.groups();
        return kcGroupRepresentation.groups().stream()
            .filter(groupRepresentation -> StringUtils.equalsIgnoreCase(groupName, groupRepresentation.getName())).collect(Collectors.toList());
    }

    public Boolean existsGroup(String groupName) {
        List<GroupRepresentation> availableGroups = this.getGroupByName(groupName);

        return !availableGroups.isEmpty();
    }

    public GroupRepresentation addGroup(String groupName) throws WebApplicationException {

        List<GroupRepresentation> availableGroups = this.getGroupByName(groupName);

        if (!availableGroups.isEmpty()) {
            log.debug("Group {} already exists.", groupName);

            return availableGroups.get(0);
        }

        GroupRepresentation group = new GroupRepresentation();
        group.setName(groupName);

        try (Response response = this.keycloakRealm.groups().add(group)) {
            if (!response.getStatusInfo().equals(Response.Status.CREATED)) {
                Response.StatusType statusInfo = response.getStatusInfo();
                response.bufferEntity();
                String body = response.readEntity(String.class);
                String message = "Create method returned status "
                    + statusInfo.getReasonPhrase() + " (Code: " + statusInfo.getStatusCode() +
                    "); expected status: Created (201). Response body: " + body;
                log.error(message);

                throw new WebApplicationException(message, response);
            }

            group.setId(CreatedResponseUtil.getCreatedId(response));

            return group;
        }
    }

    public RolesResource getRoles() {
        return keycloakRealm.roles();
    }

    public boolean isUserInGroup(User user, Group group) {
        UserResource kcUser = this.getUserResource(user);
        GroupResource kcGroup = this.getGroupResource(group);
        return kcUser.groups().contains(kcGroup);
    }

    /**
     * @deprecated
     * This method was moved to the {@link SecurityContextUtil} as it receives an authentication as parameter.
     */
    @Deprecated
    public String getKeycloakUserIdFromAuthentication(Authentication authentication) {
        return securityContextUtil.getKeycloakUserIdFromAuthentication(authentication);
    }

    /**
     * Fetch user name of user from keycloak
     * @param user
     * @return
     */
    public String getUserNameFromKeycloak(User user) {
        UsersResource users = this.keycloakRealm.users();
        UserResource kcUser = users.get(user.getKeycloakId());
        UserRepresentation kcUserRepresentation = kcUser.toRepresentation();
        return String.format("%s %s", kcUserRepresentation.getFirstName(), kcUserRepresentation.getLastName());
    }

    /**
     * @deprecated
     * Renamed to `getKeycloakUserGroups`.
     */
    @Deprecated
    public List<GroupRepresentation> getUserGroups(User user) {
        return this.getKeycloakUserGroups(user);
    };

    /**
     * Get the Keycloak GroupRepresentations from a user instance.
     *
     * @param user
     * @return
     */
    public List<GroupRepresentation> getKeycloakUserGroups(User user) {
        UserResource userResource = this.getUserResource(user);
        List<GroupRepresentation> groups = new ArrayList<>();

        try {
            groups = userResource.groups();
        } catch (Exception e) {
            log.warn("Could not get the GroupRepresentations for the groups of user with SHOGun ID {} and " +
                    "Keycloak ID {}. This may happen if the user is not available in Keycloak.",
                     user.getId(), user.getKeycloakId());
            log.trace("Full stack trace: ", e);
        }

        return groups;
    }

    /**
     * Checks if a passed user with the passed keycloak ID exists in the SHOGun DB and creates it if not.
     *
     * The groups of the user are also checked and created if needed.
     *
     * @param keycloakUserId
     * @return
     */
    public User createOrGetShogunUser(String keycloakUserId) {
        Optional<User> userOptional = userRepository.findByKeycloakId(keycloakUserId);
        User user = userOptional.orElse(null);

        // User is not yet it SHOGun DB
        if (user == null) {
            user = new User(keycloakUserId, null, null, null);
            userRepository.save(user);

            // If the user doesn't exist, we assume it's the first login after registration.
            eventPublisher.publishEvent(new OnRegistrationConfirmedEvent(user));

            // Add admin instance permissions for the user.
            userInstancePermissionService.setPermission(user, user, PermissionCollectionType.ADMIN);

            log.info("User with keycloak id {} did not yet exist in the SHOGun DB and was therefore created.", keycloakUserId);
            return user;
        }

        List<GroupRepresentation> keycloakUserGroups = this.getKeycloakUserGroups(user);

        // Add missing groups to shogun db
        keycloakUserGroups
            .stream()
            .map(GroupRepresentation::getId)
            .forEach(this::createOrGetShogunGroup);

        return user;
    }

    /**
     *  Delete a user from the SHOGun DB by its keycloak Id.
     *
     * @param keycloakUserId
     */
    public void deleteShogunUser(String keycloakUserId) {
        Optional<User> userOptional = userRepository.findByKeycloakId(keycloakUserId);
        User user = userOptional.orElse(null);
        if (user == null) {
            log.debug("User with keycloak id {} was deleted in Keycloak. It did not exists in SHOGun DB. No action needed.", keycloakUserId);
            return;
        }
        userInstancePermissionService.deleteAllForEntity(user);
        userRepository.delete(user);
        log.info("User with keycloak id {} was deleted in Keycloak and was therefore deleted in SHOGun DB, too.", keycloakUserId);
    }

    /**
     * Checks if a group with the passed keycloak ID exists in the SHOGun DB and creates it if not.
     *
     * @param keycloakGroupId
     * @return
     */
    public Group createOrGetShogunGroup(String keycloakGroupId) {
        Optional<Group> groupOptional = groupRepository.findByKeycloakId(keycloakGroupId);
        Group group = groupOptional.orElse(null);

        if (group == null) {
            group = new Group(keycloakGroupId, null);
            groupRepository.save(group);

            log.info("Group with keycloak id {} did not yet exist in the SHOGun DB and was therefore created.", keycloakGroupId);
            return group;
        }

        return group;
    }

    /**
     *  Delete a group from the SHOGun DB by its keycloak Id.
     *
     * @param keycloakGroupId
     */
    public void deleteShogunGroup(String keycloakGroupId) {
        Optional<Group> groupOptional = groupRepository.findByKeycloakId(keycloakGroupId);
        Group group = groupOptional.orElse(null);
        if (group == null) {
            log.debug("Group with keycloak id {} was deleted in Keycloak. It did not exists in SHOGun DB. No action needed.", keycloakGroupId);
            return;
        }
        groupRepository.delete(group);
        log.info("Group with keycloak id {} was deleted in Keycloak and was therefore deleted in SHOGun DB, too.", keycloakGroupId);
    }

}
