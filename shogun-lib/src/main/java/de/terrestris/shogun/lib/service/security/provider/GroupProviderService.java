/* SHOGun, https://terrestris.github.io/shogun/
 *
 * Copyright © 2022-present terrestris GmbH & Co. KG
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
package de.terrestris.shogun.lib.service.security.provider;

import de.terrestris.shogun.lib.model.Group;
import de.terrestris.shogun.lib.model.User;

import java.util.List;

public interface GroupProviderService {

    List<Group> findByUser(User user);

    List<User> getGroupMembers(String keycloakId);

    void setTransientRepresentations(Group group);

    List<Group> getGroupsForUser();

    Group findOrCreateByProviderId(String keycloakGroupId);

}
