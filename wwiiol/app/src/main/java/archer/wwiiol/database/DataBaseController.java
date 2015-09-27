package archer.wwiiol.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import archer.wwiiol.models.CpModel;


public class DataBaseController extends SQLiteOpenHelper {
    Context context;
    /**
     * Database declare
     */
    public static final String DATABASE_NAME = "wwiiol";
    public static final int DATABASE_VERSION = 1;
    /**
     * Tables Declare
     */
    public static final String CPLIST_TABLE = "cplist";
    public static final String FALIST_TABLE = "falist";

    /* --------------------------------- */
    private static final String ID = "ID";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String ORIG = "orig";


	/* --------------------------------- */

    public static final String CP_TABLE_CREATE = "create table " + CPLIST_TABLE
            + "(" + ID + "  integer  , " + NAME + " integer , " + TYPE + " integer , " + ORIG + " integer );";

    public static final String FA_TABLE_CREATE = "create table " + FALIST_TABLE
            + "(" + ID + "  integer  , " + NAME + " integer , " + TYPE + " integer , " + ORIG + " integer );";

    private static final String TAG = "DataBase - log";

    /* Constructor */
    public DataBaseController(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CP_TABLE_CREATE);
        database.execSQL(FA_TABLE_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + CPLIST_TABLE);
        database.execSQL("DROP TABLE IF EXISTS " + FALIST_TABLE);

        onCreate(database);
    }

    public ArrayList<CpModel> getCpList() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + CPLIST_TABLE + " ;", null);
        ArrayList<CpModel> arrayList = new ArrayList<CpModel>();
        while (c.moveToNext()) {
            CpModel cpModel = new CpModel();
            cpModel.setId(c.getInt(0));
            cpModel.setName(c.getString(1));
            cpModel.setType(c.getInt(2));
            cpModel.setOrig(c.getInt(3));
            arrayList.add(cpModel);
        }
        c.close();
        db.close();
        return arrayList;
    }
    public ArrayList<CpModel> getFaList() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + FALIST_TABLE + " ;", null);
        ArrayList<CpModel> arrayList = new ArrayList<CpModel>();
        while (c.moveToNext()) {
            CpModel cpModel = new CpModel();
            cpModel.setId(c.getInt(0));
            cpModel.setName(c.getString(1));
            cpModel.setType(c.getInt(2));
            cpModel.setOrig(c.getInt(3));
            arrayList.add(cpModel);
        }
        c.close();
        db.close();
        return arrayList;
    }

    public CpModel getCp(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + CPLIST_TABLE + " WHERE " + ID + " = " + id + ";", null);
        CpModel cpModel = new CpModel();
        while (c.moveToNext()) {
            cpModel.setId(c.getInt(0));
            cpModel.setName(c.getString(1));
            cpModel.setType(c.getInt(2));
            cpModel.setOrig(c.getInt(3));
        }
        c.close();
        db.close();
        return cpModel;
    }
    public CpModel getFacility(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + FALIST_TABLE + " WHERE " + ID + " = " + id + ";", null);
        CpModel cpModel = new CpModel();
        while (c.moveToNext()) {
            cpModel.setId(c.getInt(0));
            cpModel.setName(c.getString(1));
            cpModel.setType(c.getInt(2));
            cpModel.setOrig(c.getInt(3));
        }
        c.close();
        db.close();
        return cpModel;
    }

    public void insertCpList(ArrayList<CpModel> modelArrayList) {
        // delete items
        deleteItems(CPLIST_TABLE);
        //
        SQLiteDatabase db = this.getWritableDatabase();
        // Gets the data repository in write mode
        for (int i = 0; i < modelArrayList.size(); i++) {
            CpModel cpModel = modelArrayList.get(i);
            ContentValues values = new ContentValues();
            values.put(ID, cpModel.getId());
            values.put(NAME, cpModel.getName());
            values.put(TYPE, cpModel.getType());
            values.put(ORIG, cpModel.getOrig());
            db.insert(CPLIST_TABLE, null, values);
        }

        db.close();
    }
    public void insertFacilityList(ArrayList<CpModel> modelArrayList) {
        // delete items
        deleteItems(FALIST_TABLE);
        //
        SQLiteDatabase db = this.getWritableDatabase();
        // Gets the data repository in write mode
        for (int i = 0; i < modelArrayList.size(); i++) {
            CpModel cpModel = modelArrayList.get(i);
            ContentValues values = new ContentValues();
            values.put(ID, cpModel.getId());
            values.put(NAME, cpModel.getName());
            values.put(TYPE, cpModel.getType());
            values.put(ORIG, cpModel.getOrig());
            db.insert(FALIST_TABLE, null, values);
        }

        db.close();
    }
    public void deleteItems(String table) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // map of values, where column names are the keys
        db.execSQL("DELETE  FROM " + table + " ;");
        db.close();
    }
}