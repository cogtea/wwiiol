package archer.handietalkie.utilities;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import java.util.ArrayList;

import archer.handietalkie.database.DataBaseController;
import archer.handietalkie.models.CpModel;

/**
 * Created by Ramy on 10/1/17.
 */

public class SuggestionProvider extends SearchRecentSuggestionsProvider {
    public static final String AUTHORITY = SuggestionProvider.class
            .getName();
    public static final int MODE = DATABASE_MODE_QUERIES;
    private static final String[] COLUMNS = {
            "_id", // must include this column
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
            SearchManager.SUGGEST_COLUMN_SHORTCUT_ID};

    public SuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        String query = selectionArgs[0];
        if (query == null || query.length() == 0) {
            return null;
        }

        MatrixCursor cursor = new MatrixCursor(COLUMNS);

        ArrayList<CpModel> list = new DataBaseController(getContext()).searchByCp(query);
        for (CpModel cpModel : list) {
            cursor.addRow(createRow(cpModel));
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    private Object[] createRow(CpModel cpModel) {
        return new Object[]{cpModel.getId(), // _id
                cpModel.getName(), // text1
                "",
                cpModel.getId(), "android.intent.action.VIEW", // action
                SearchManager.SUGGEST_NEVER_MAKE_SHORTCUT};
    }

}
