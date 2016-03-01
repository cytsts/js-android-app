package com.jaspersoft.android.jaspermobile.data;

import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ActiveProfileCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.components.AppComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.ProfileComponent;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ComponentManagerTest {

    @Mock
    ActiveProfileCache mActiveProfileCache;
    @Mock
    ProfileCache mProfileCache;

    @Mock
    AppComponent mAppComponent;
    @Mock
    ProfileComponent mProfileComponent;

    @Mock
    GraphObject mGraphObject;

    private ComponentManager mComponentManager;
    private final Profile activeProfile = Profile.create("active");

    @Rule
    public ExpectedException expected = none();

    private Profile mActivatedProfile;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();
        mComponentManager = new ComponentManager(
                mGraphObject,
                mActiveProfileCache,
                mProfileCache
        );
    }

    private void setupMocks() {
        when(mProfileCache.getAll()).thenReturn(Collections.<Profile>emptyList());
    }

    @Test
    public void should_trigger_active_profile_missing_if_no_profiles() throws Exception {
        givenAppComponent();
        givenNoProfileComponent();
        givenNoActiveProfile();
        givenNoRegisteredProfiles();

        whenSetupProfileComponent();

        thenShouldRetrieveAllProfiles();
        thenShouldReturnFakeProfile();
    }

    @Test
    public void should_setup_profile_if_exists() throws Exception {
        givenAppComponent();
        givenNoProfileComponent();
        givenActiveProfile();

        whenSetupProfileComponent();

        thenShouldSetupProfileComponent();
        thenReturnActiveProfile();
    }

    @Test
    public void should_setup_active_profile() throws Exception {
        givenAppComponent();
        givenActiveProfile();

        whenSetupActiveProfile();

        thenShouldWriteToActiveCache();
        thenShouldSetupProfileComponent();
    }

    @Test
    public void should_activate_first_available_account() throws Exception {
        givenAppComponent();
        givenNoProfileComponent();
        givenNoActiveProfile();
        givenOneRegisteredProfile();

        whenSetupProfileComponent();

        thenShouldRetrieveAllProfiles();
        thenShouldWriteToActiveCache();
        thenReturnActiveProfile();
        thenShouldSetupProfileComponent();
    }

    @Test
    public void should_reuse_available_profile_component() throws Exception {
        givenAppComponent();
        givenActiveProfile();
        givenProfileComponent();
        givenProfileComponentRepresentsActiveProfile();

        whenSetupProfileComponent();

        thenShouldNotSetupAnyProfile();
    }

    private void givenNoActiveProfile() {
        when(mActiveProfileCache.get()).thenReturn(null);
    }

    private void givenActiveProfile() {
        when(mActiveProfileCache.get()).thenReturn(activeProfile);
        when(mProfileCache.getAll()).thenReturn(Collections.singletonList(activeProfile));
    }

    private void givenProfileComponent() {
        when(mGraphObject.getProfileComponent()).thenReturn(mProfileComponent);
    }

    private void givenProfileComponentRepresentsActiveProfile() {
        when(mProfileComponent.getProfile()).thenReturn(activeProfile);
    }

    private void givenNoProfileComponent() {
        when(mGraphObject.getProfileComponent()).thenReturn(null);
    }

    private void givenAppComponent() {
        when(mGraphObject.getComponent()).thenReturn(mAppComponent);
    }

    private void givenOneRegisteredProfile() {
        when(mProfileCache.getAll()).thenReturn(Collections.singletonList(activeProfile));
    }

    private void givenNoRegisteredProfiles() {
        when(mProfileCache.getAll()).thenReturn(Collections.<Profile>emptyList());
    }

    private void whenSetupProfileComponent() {
        mActivatedProfile = mComponentManager.setupProfileComponent();
    }

    private void whenSetupActiveProfile() {
        mComponentManager.setupActiveProfile(activeProfile);
    }

    private void thenReturnActiveProfile() {
        assertThat(mActivatedProfile, is(activeProfile));
    }

    private void thenShouldSetupProfileComponent() {
        verify(mGraphObject).setProfileComponent(any(ProfileComponent.class));
    }

    private void thenShouldWriteToActiveCache() {
        verify(mActiveProfileCache).put(activeProfile);
    }

    private void thenShouldRetrieveAllProfiles() {
        verify(mProfileCache, times(2)).getAll();
    }

    private void thenShouldReturnFakeProfile() {
        assertThat(mActivatedProfile, is(Profile.getFake()));
    }

    private void thenShouldNotSetupAnyProfile() {
        verifyNoMoreInteractions(mAppComponent);
    }
}