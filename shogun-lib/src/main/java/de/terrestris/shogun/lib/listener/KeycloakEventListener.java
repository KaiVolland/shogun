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

package de.terrestris.shogun.lib.listener;

import de.terrestris.shogun.lib.event.KeycloakEvent;
import de.terrestris.shogun.lib.service.GroupService;
import de.terrestris.shogun.lib.service.security.provider.UserProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class KeycloakEventListener {
    @Autowired
    UserProviderService userProviderService;

    @Autowired
    GroupService groupService;

    @EventListener
    public void onKeycloakEvent(KeycloakEvent event) {
        switch (event.getEventType()) {
            case USER_CREATED, USER_GROUP_MEMBERSHIP_CHANGED -> userProviderService.findOrCreateByProviderId(event.getKeycloakId());
            case GROUP_CREATED -> groupService.findOrCreateByKeycloakId(event.getKeycloakId());
        }
    }
}
