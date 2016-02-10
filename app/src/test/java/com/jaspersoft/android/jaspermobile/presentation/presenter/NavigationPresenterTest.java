package com.jaspersoft.android.jaspermobile.presentation.presenter;

import com.jaspersoft.android.jaspermobile.FakePostExecutionThread;
import com.jaspersoft.android.jaspermobile.FakePreExecutionThread;
import com.jaspersoft.android.jaspermobile.data.ComponentManager;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileMetadata;
import com.jaspersoft.android.jaspermobile.domain.ProfileMetadataCollection;
import com.jaspersoft.android.jaspermobile.domain.interactor.profile.GetActiveProfileUseCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.profile.GetProfilesUseCase;
import com.jaspersoft.android.jaspermobile.presentation.contract.NavigationContract;
import com.jaspersoft.android.jaspermobile.presentation.model.ProfileViewModel;
import com.jaspersoft.android.jaspermobile.presentation.model.mapper.ProfileViewModelMapper;
import com.jaspersoft.android.jaspermobile.presentation.navigation.FakePageFactory;
import com.jaspersoft.android.jaspermobile.presentation.navigation.Navigator;
import com.jaspersoft.android.jaspermobile.presentation.navigation.Page;
import com.jaspersoft.android.jaspermobile.presentation.navigation.StartUpPage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class NavigationPresenterTest {
    private static final ProfileViewModel VIEW_PROFILE = new ProfileViewModel("label", "6.0", true);
    private static final ProfileMetadata DOMAIN_PROFILE = new ProfileMetadata(
            Profile.create("fake"),
            new JasperServer.Builder()
                    .setBaseUrl("")
                    .setEdition("PRO")
                    .setVersion("5.6")
                    .create(),
            true);

    private static final List<ProfileMetadata> DOMAIN_PROFILES = Collections.singletonList(DOMAIN_PROFILE);
    private static final List<ProfileViewModel> VIEW_PROFILES = Collections.singletonList(VIEW_PROFILE);

    @Mock
    Navigator mNavigator;
    @Mock
    ProfileViewModelMapper profileViewModelMapper;
    @Mock
    ComponentManager mComponentManager;

    @Mock
    ProfileMetadataCollection mProfileMetadataCollection;

    @Mock
    NavigationContract.View mView;

    private FakePageFactory mFakePageFactory;
    private FakeGetProfilesUseCase mGetProfilesUseCase;
    private FakeGetActiveProfileUseCase mGetActiveProfileUseCase;
    private NavigationPresenter mNavigationPresenter;
    private Profile fakeProfile = Profile.create("fake");


    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupSpies();
        mockMapper();

        mNavigationPresenter = new NavigationPresenter(
                mNavigator,
                mFakePageFactory,
                mComponentManager,
                profileViewModelMapper,
                mGetProfilesUseCase,
                mGetActiveProfileUseCase
        );
        mNavigationPresenter.injectView(mView);
    }

    private void setupSpies() {
        mFakePageFactory = spy(new FakePageFactory());
        mGetProfilesUseCase = spy(new FakeGetProfilesUseCase());
        mGetActiveProfileUseCase = spy(new FakeGetActiveProfileUseCase());
    }

    private void mockMapper() {
        when(profileViewModelMapper.transform(anyListOf(ProfileMetadata.class)))
                .thenReturn(VIEW_PROFILES);
        when(profileViewModelMapper.transform(any(ProfileMetadata.class)))
                .thenReturn(VIEW_PROFILE);
    }

    @Test
    public void should_load_profiles_on_view() throws Exception {
        givenProfilesCollectionWithActiveOne();

        whenLoadProfiles();

        thenShouldExecuteGetProfilesCase();
        thenShouldMapDomainProfiles();
        thenShouldShowProfiles();
    }

    private void thenShouldMapDomainProfiles() {
        verify(profileViewModelMapper).transform(DOMAIN_PROFILES);
    }

    private void thenShouldExecuteGetProfilesCase() {
        verify(mGetProfilesUseCase).execute(any(Subscriber.class));
    }

    private void thenShouldShowProfiles() {
        verify(mView).showProfiles(VIEW_PROFILES);
    }

    private void whenLoadProfiles() {
        mNavigationPresenter.loadProfiles();
    }

    @Test
    public void should_load_active_profile_on_view() throws Exception {
        whenLoadsActiveProfile();

        thenShouldExecuteGetActiveProfileCase();
        thenShouldMapDomainProfile();
        thenShouldShowActiveProfile();
        thenShouldToggleRecentlyViewed();
    }

    private void givenProfilesCollectionWithActiveOne() {
        when(mProfileMetadataCollection.get()).thenReturn(DOMAIN_PROFILES);
        when(mProfileMetadataCollection.containsActiveProfile()).thenReturn(true);
    }

    private void thenShouldToggleRecentlyViewed() {
        verify(mView).toggleRecentlyViewedNavigation(true);
    }

    private void thenShouldShowActiveProfile() {
        verify(mView).showActiveProfile(VIEW_PROFILE);
    }

    private void thenShouldExecuteGetActiveProfileCase() {
        verify(mGetActiveProfileUseCase).execute(any(Subscriber.class));
    }

    private void thenShouldMapDomainProfile() {
        verify(profileViewModelMapper).transform(DOMAIN_PROFILE);
    }

    private void whenLoadsActiveProfile() {
        mNavigationPresenter.loadActiveProfile();
    }

    @Test
    public void should_activate_new_profile() throws Exception {
        whenActivatesProfile();

        thenShouldActivateProfileWithComponent();
        thenNavigatesToStartPage();
    }

    private void thenNavigatesToStartPage() {
        ArgumentCaptor<Page> argument = ArgumentCaptor.forClass(Page.class);
        verify(mNavigator).navigate(argument.capture(), eq(true));
        assertThat(argument.getValue(), is(instanceOf(StartUpPage.class)));
    }

    private void thenShouldActivateProfileWithComponent() {
        verify(mComponentManager).setupActiveProfile(fakeProfile);
    }

    private void whenActivatesProfile() {
        mNavigationPresenter.activateProfile(fakeProfile);
    }

    @Test
    public void should_exit_current_session() throws Exception {
        givenProfilesWithNoActiveOne();

        whenLoadProfiles();

        thenShouldExecuteGetProfilesCase();
        thenNavigatesToStartPage();
    }

    private void givenProfilesWithNoActiveOne() {
        when(mProfileMetadataCollection.containsActiveProfile()).thenReturn(false);
    }

    private class FakeGetProfilesUseCase extends GetProfilesUseCase {
        public FakeGetProfilesUseCase() {
            super(FakePreExecutionThread.create(), FakePostExecutionThread.create(), null, null);
        }

        @Override
        protected Observable<ProfileMetadataCollection> buildUseCaseObservable() {
            return Observable.just(mProfileMetadataCollection);
        }
    }

    private class FakeGetActiveProfileUseCase extends GetActiveProfileUseCase {
        public FakeGetActiveProfileUseCase() {
            super(FakePreExecutionThread.create(), FakePostExecutionThread.create(), null, null);
        }

        @Override
        protected Observable<ProfileMetadata> buildUseCaseObservable() {
            return Observable.just(DOMAIN_PROFILE);
        }
    }
}