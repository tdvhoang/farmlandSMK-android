package hdv.ble.tdx.data.local;

import android.database.Cursor;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import hdv.ble.tdx.data.model.IkyDevice;
import rx.Observable;
import rx.Subscriber;

@Singleton
public class DatabaseHelper {

    private final BriteDatabase mDb;

    @Inject
    public DatabaseHelper(DbOpenHelper dbOpenHelper) {
        mDb = SqlBrite.create().wrapDatabaseHelper(dbOpenHelper);
    }

    public BriteDatabase getBriteDb() {
        return mDb;
    }

    /**
     * Remove all the data from all the tables in the database.
     */
    public Observable<Void> clearTables() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                BriteDatabase.Transaction transaction = mDb.newTransaction();
                try {
                    Cursor cursor = mDb.query("SELECT name FROM sqlite_master WHERE type='table'");
                    while (cursor.moveToNext()) {
                        mDb.delete(cursor.getString(cursor.getColumnIndex("name")), null);
                    }
                    cursor.close();
                    transaction.markSuccessful();
                    subscriber.onCompleted();
                } finally {
                    transaction.end();
                }
            }
        });
    }

    public Observable<Void> deleteAllIkyDevices() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                BriteDatabase.Transaction transaction = mDb.newTransaction();
                try {
                    mDb.delete(Db.BeaconTable.TABLE_NAME, null);
                    transaction.markSuccessful();
                    subscriber.onCompleted();
                } finally {
                    transaction.end();
                }
            }
        });
    }
    // Delete all beacons in table and add the new ones.
    public Observable<Void> setIkyDevices(final List<IkyDevice> ikyDevices) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                BriteDatabase.Transaction transaction = mDb.newTransaction();
                try {
                    mDb.delete(Db.BeaconTable.TABLE_NAME, null);
                    for (IkyDevice ikyDevice : ikyDevices) {
                        mDb.insert(Db.BeaconTable.TABLE_NAME,
                                Db.BeaconTable.toContentValues(ikyDevice));
                    }
                    transaction.markSuccessful();
                    subscriber.onCompleted();
                } finally {
                    transaction.end();
                }
            }
        });
    }

    public Observable<IkyDevice> findIkyDevice(final String uuid) {
        return Observable.create(new Observable.OnSubscribe<IkyDevice>() {
            @Override
            public void call(Subscriber<? super IkyDevice> subscriber) {
                Cursor cursor = mDb.query(
                        "SELECT * FROM " + Db.BeaconTable.TABLE_NAME +
                                " WHERE " + Db.BeaconTable.COLUMN_UUID + " = ? ", uuid);
                while (cursor.moveToNext()) {
                    subscriber.onNext(Db.BeaconTable.parseCursor(cursor));
                }
                cursor.close();
                subscriber.onCompleted();
            }
        });
    }

    public Observable<IkyDevice> findIkyDevice() {
        return Observable.create(new Observable.OnSubscribe<IkyDevice>() {
            @Override
            public void call(Subscriber<? super IkyDevice> subscriber) {
                Cursor cursor = mDb.query("SELECT * FROM " + Db.BeaconTable.TABLE_NAME);
                while (cursor.moveToNext()) {
                    subscriber.onNext(Db.BeaconTable.parseCursor(cursor));
                }
                cursor.close();
                subscriber.onCompleted();
            }
        });
    }

    public Observable<String> findIkysUuids() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Cursor cursor = mDb.query("SELECT DISTINCT " + Db.BeaconTable.COLUMN_UUID +
                                " FROM " + Db.BeaconTable.TABLE_NAME);
                while (cursor.moveToNext()) {
                    subscriber.onNext(cursor.getString(
                            cursor.getColumnIndexOrThrow(Db.BeaconTable.COLUMN_UUID)));
                }
                cursor.close();
                subscriber.onCompleted();
            }
        });
    }

}
