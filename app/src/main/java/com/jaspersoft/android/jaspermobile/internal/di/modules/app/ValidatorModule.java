package com.jaspersoft.android.jaspermobile.internal.di.modules.app;

import com.jaspersoft.android.jaspermobile.data.validator.ProfileValidatorImpl;
import com.jaspersoft.android.jaspermobile.domain.validator.ProfileValidator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Module
public final class ValidatorModule {
    @Singleton
    @Provides
    ProfileValidator provideProfileValidator(ProfileValidatorImpl profileValidator) {
        return profileValidator;
    }
}
