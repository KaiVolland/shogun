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
package de.terrestris.shogun.lib.service;

import de.terrestris.shogun.lib.model.User;
import de.terrestris.shogun.lib.repository.UserRepository;
import de.terrestris.shogun.lib.util.KeycloakUtil;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService extends BaseService<UserRepository, User> {

    @Autowired
    KeycloakUtil keycloakUtil;

    @PostFilter("hasRole('ROLE_ADMIN') or hasPermission(filterObject, 'READ')")
    @Transactional(readOnly = true)
    @Override
    public List<User> findAll() {
        List<User> users = (List<User>) repository.findAll();

        for (User user : users) {
            this.setTransientKeycloakRepresentations(user);
        }

        return users;
    }

    @PostFilter("hasRole('ROLE_ADMIN') or hasPermission(filterObject, 'READ')")
    @Transactional(readOnly = true)
    @Override
    public List<User> findAllBy(Specification specification) {
        List<User> users = (List<User>) repository.findAll(specification);

        for (User user : users) {
            this.setTransientKeycloakRepresentations(user);
        }

        return users;
    }

    @PostAuthorize("hasRole('ROLE_ADMIN') or hasPermission(returnObject.orElse(null), 'READ')")
    @Transactional(readOnly = true)
    @Override
    public Optional<User> findOne(Long id) {
        Optional<User> user = repository.findById(id);

        if (user.isPresent()) {
            this.setTransientKeycloakRepresentations(user.get());
        }

        return user;
    }

    @PostAuthorize("hasRole('ROLE_ADMIN') or hasPermission(returnObject.orElse(null), 'READ')")
    @Transactional(readOnly = true)
    public Optional<User> findByKeyCloakId(String keycloakId) {
        Optional<User> user = repository.findByKeycloakId(keycloakId);

        if (user.isPresent()) {
            this.setTransientKeycloakRepresentations(user.get());
        }

        return user;
    }

    private User setTransientKeycloakRepresentations(User user) {
        UserResource userResource = keycloakUtil.getUserResource(user);

        try {
            UserRepresentation userRepresentation = userResource.toRepresentation();
            user.setKeycloakRepresentation(userRepresentation);
        } catch (Exception e) {
            LOG.warn("Could not get the UserRepresentation for user with SHOGun ID {} and " +
                    "Keycloak ID {}. This may happen if the user is not available in Keycloak.",
                    user.getId(), user.getKeycloakId());
            LOG.trace("Full stack trace: ", e);
        }

        return user;
    }

}
