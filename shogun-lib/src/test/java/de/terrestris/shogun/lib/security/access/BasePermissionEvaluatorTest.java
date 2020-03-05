package de.terrestris.shogun.lib.security.access;

import de.terrestris.shogun.lib.model.Application;
import de.terrestris.shogun.lib.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BasePermissionEvaluatorTest {

    @Mock
    private UserRepository userRepositoryMock;

    @InjectMocks
    private BasePermissionEvaluator permissionEvaluator;

    @Test
    public void hasPermission_ShouldRestrictAccessIfAuthenticationIsNull() {
        Authentication authentication = null;
        Application targetDomainObject = new Application();
        String permissionObject = "READ";

        boolean permissionResult = permissionEvaluator.hasPermission(authentication, targetDomainObject, permissionObject);

        assertFalse(permissionResult);
    }

    @Test
    public void hasPermission_ShouldRestrictAccessIfTargetDomainObjectIsNull() {
        Authentication authentication = mock(Authentication.class);
        Application targetDomainObject = null;
        String permissionObject = "READ";

        boolean permissionResult = permissionEvaluator.hasPermission(authentication, targetDomainObject, permissionObject);

        assertFalse(permissionResult);
    }

    @Test
    public void hasPermission_ShouldRestrictAccessIfPermissionObjectIsNull() {
        Authentication authentication = mock(Authentication.class);
        Application targetDomainObject = new Application();
        String permissionObject = null;

        boolean permissionResult = permissionEvaluator.hasPermission(authentication, targetDomainObject, permissionObject);

        assertFalse(permissionResult);
    }

    @Test
    public void hasPermission_ShouldRestrictAccessIfPermissionObjectIsNotAString() {
        Authentication authentication = mock(Authentication.class);
        Application targetDomainObject = new Application();
        int permissionObject = 42;

        boolean permissionResult = permissionEvaluator.hasPermission(authentication, targetDomainObject, permissionObject);

        assertFalse(permissionResult);
        verify(authentication, times(0)).getPrincipal();
        verifyNoMoreInteractions(authentication);
    }

    // TODO Fix test
//    @Test
//    public void hasPermission_ShouldRestrictAccessIfPrinicipalIsNotAUser() {
//        Authentication authentication = mock(Authentication.class);
//        when(authentication.getPrincipal()).thenReturn("Not a User object");
//
//        Application targetDomainObject = new Application();
//        targetDomainObject.setId(1L);
//        String permissionObject = "READ";
//
//        BaseEntityPermissionEvaluator baseEntityPermissionEvaluatorMock = mock(BaseEntityPermissionEvaluator.class);
//        when(baseEntityPermissionEvaluatorMock.hasPermission(null, targetDomainObject, PermissionType.valueOf(permissionObject))).thenReturn(false);
//
//        when(permissionEvaluatorFactoryMock.getEntityPermissionEvaluator(targetDomainObject.getClass())).thenReturn(baseEntityPermissionEvaluatorMock);
//
//        boolean permissionResult = permissionEvaluator.hasPermission(authentication, targetDomainObject, permissionObject);
//
//        assertFalse(permissionResult);
//        verify(authentication, times(1)).getPrincipal();
//        verifyNoMoreInteractions(authentication);
//    }

    // TODO Fix test
//    @Test
//    public void hasPermission_ShouldRestrictAccessForSecuredTargetDomainObjectWithoutPermissions() throws NoSuchFieldException, IllegalAccessException {
//        Authentication authenticationMock = mock(Authentication.class);
//        final User user = new User();
//        user.setUsername("Test User");
//        user.setId(1909L);
//
//        when(authenticationMock.getPrincipal()).thenReturn(user);
//
//        final Long userId = user.getId();
//
//        Application targetDomainObject = new Application();
//        targetDomainObject.setId(1L);
//        final Class<?> domainObjectClass = targetDomainObject.getClass();
//
//        String permissionObject = "READ";
//        final PermissionType permission = PermissionType.valueOf(permissionObject);
//
//        final boolean expectedPermission = false;
//
//        BaseEntityPermissionEvaluator baseEntityPermissionEvaluatorMock = mock(BaseEntityPermissionEvaluator.class);
//        when(baseEntityPermissionEvaluatorMock.hasPermission(user, targetDomainObject, PermissionType.valueOf(permissionObject))).thenReturn(expectedPermission);
//
//        when(permissionEvaluatorFactoryMock.getEntityPermissionEvaluator(domainObjectClass)).thenReturn(baseEntityPermissionEvaluatorMock);
//
//        when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(user));
//
//        boolean permissionResult = permissionEvaluator.hasPermission(authenticationMock, targetDomainObject, permissionObject);
//
//        assertEquals(expectedPermission, permissionResult);
//        verify(baseEntityPermissionEvaluatorMock, times(1)).hasPermission(user, targetDomainObject, permission);
//        verifyNoMoreInteractions(baseEntityPermissionEvaluatorMock);
//
//        verify(permissionEvaluatorFactoryMock, times(1)).getEntityPermissionEvaluator(domainObjectClass);
//        verifyNoMoreInteractions(permissionEvaluatorFactoryMock);
//
//        verify(userRepositoryMock, times(1)).findById(userId);
//        verifyNoMoreInteractions(userRepositoryMock);
//
//        verify(authenticationMock, times(1)).getPrincipal();
//        verifyNoMoreInteractions(authenticationMock);
//    }

    // TODO Fix test
//    @Test
//    public void hasPermission_ShouldRestrictAccessForSecuredTargetDomainObjectWithPermissions() throws NoSuchFieldException, IllegalAccessException {
//        Authentication authenticationMock = mock(Authentication.class);
//        final User user = new User();
//        user.setUsername("Test User");
//        user.setId(1909L);
//
//        when(authenticationMock.getPrincipal()).thenReturn(user);
//
//        final Long userId = user.getId();
//
//        Application targetDomainObject = new Application();
//        targetDomainObject.setId(1L);
//        final Class<?> domainObjectClass = targetDomainObject.getClass();
//
//        String permissionObject = "READ";
//        final PermissionType permission = PermissionType.valueOf(permissionObject);
//
//        final boolean expectedPermission = true;
//
//        BaseEntityPermissionEvaluator baseEntityPermissionEvaluatorMock = mock(BaseEntityPermissionEvaluator.class);
//        when(baseEntityPermissionEvaluatorMock.hasPermission(user, targetDomainObject, PermissionType.valueOf(permissionObject))).thenReturn(expectedPermission);
//
//        when(permissionEvaluatorFactoryMock.getEntityPermissionEvaluator(domainObjectClass)).thenReturn(baseEntityPermissionEvaluatorMock);
//
//        when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(user));
//
//        boolean permissionResult = permissionEvaluator.hasPermission(authenticationMock, targetDomainObject, permissionObject);
//
//        assertEquals(expectedPermission, permissionResult);
//
//        verify(baseEntityPermissionEvaluatorMock, times(1)).hasPermission(user, targetDomainObject, permission);
//        verifyNoMoreInteractions(baseEntityPermissionEvaluatorMock);
//
//        verify(permissionEvaluatorFactoryMock, times(1)).getEntityPermissionEvaluator(domainObjectClass);
//        verifyNoMoreInteractions(permissionEvaluatorFactoryMock);
//
//        verify(userRepositoryMock, times(1)).findById(userId);
//        verifyNoMoreInteractions(userRepositoryMock);
//
//        verify(authenticationMock, times(1)).getPrincipal();
//        verifyNoMoreInteractions(authenticationMock);
//    }

}
