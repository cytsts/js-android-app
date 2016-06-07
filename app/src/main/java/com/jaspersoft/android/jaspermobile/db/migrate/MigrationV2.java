/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.db.migrate;

import android.database.sqlite.SQLiteDatabase;

/**
 * @author Tom Koptel
 * @since 2.1
 */
final class MigrationV2 implements Migration {
    @Override
    public void migrate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS report_options;");

        db.execSQL("ALTER TABLE server_profiles RENAME TO tmp_server_profiles;");
        db.execSQL(
                "CREATE TABLE server_profiles ( _id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        " alias TEXT, server_url TEXT, organization TEXT, username TEXT," +
                        " password TEXT, edition TEXT, version_code NUMERIC );"
        );
        db.execSQL("INSERT INTO server_profiles(alias, server_url, organization, username, password)" +
                " select alias, server_url, organization, username, password from tmp_server_profiles;");
        db.execSQL("DROP TABLE IF EXISTS tmp_server_profiles;");

        db.execSQL("ALTER TABLE favorites RENAME TO tmp_favorites;");
        db.execSQL(
                "CREATE TABLE favorites ( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT," +
                        " title TEXT, uri TEXT, description TEXT, wstype TEXT, username TEXT, " +
                        "organization TEXT, server_profile_id INTEGER REFERENCES server_profiles(_id)" +
                        " ON DELETE CASCADE )"
        );
        db.execSQL("INSERT INTO favorites(name, title, uri, description, wstype, username, organization, server_profile_id)" +
                " select name, title, uri, description, wstype, username, organization, server_profile_id from tmp_favorites;");
        db.execSQL("DROP TABLE IF EXISTS tmp_favorites;");
    }
}
