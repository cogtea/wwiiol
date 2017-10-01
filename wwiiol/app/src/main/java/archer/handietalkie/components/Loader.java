package archer.handietalkie.components;

import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import archer.handietalkie.database.DataBaseController;
import archer.handietalkie.models.CpModel;

/**
 * Created by Ramy on 9/26/2015.
 */
public class Loader {
    public static final int CITY = 1;
    public static final int FACILITY = 2;
    private String mAos;
    private int mType;

    public void load(InputStream in, Context context, int type) {
        mType = type;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            //
            if (mType == CITY) {
                new DataBaseController(context).insertCpList(readFeed(parser));
            } else if (mType == FACILITY) {
                new DataBaseController(context).insertFacilityList(readFeed(parser));
            }
            //
            in.close();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //
    }

    private ArrayList<CpModel> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<CpModel> entries = new ArrayList();
        if (mType == CITY) {
            parser.require(XmlPullParser.START_TAG, mAos, "cplist");
        } else if (mType == FACILITY) {
            parser.require(XmlPullParser.START_TAG, mAos, "facilitylist");
        }
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("cp") || name.equals("fac")) {
                entries.add(readCp(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    // Processes link tags in the feed.
    private CpModel readCp(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (mType == CITY) {
            parser.require(XmlPullParser.START_TAG, mAos, "cp");
        } else if (mType == FACILITY) {
            parser.require(XmlPullParser.START_TAG, mAos, "fac");
        }
        String id = parser.getAttributeValue(null, "id");
        String name = parser.getAttributeValue(null, "name");
        String type = parser.getAttributeValue(null, "type");
        String origCountry = parser.getAttributeValue(null, "orig-country");
        String ox = parser.getAttributeValue(null, "x");
        String oy = parser.getAttributeValue(null, "y");

        CpModel aoModel = new CpModel();
        aoModel.setId(Integer.parseInt(id));
        aoModel.setName(name);
        aoModel.setType(Integer.parseInt(type));
        aoModel.setOrig(Integer.parseInt(origCountry));
        try {
            aoModel.setOx(Double.parseDouble(ox));
            aoModel.setOy(Double.parseDouble(oy));
        } catch (NumberFormatException e) {
            aoModel.setOx(0);
            aoModel.setOy(0);
        }
        //
        parser.nextTag();
        //
        return aoModel;
    }
}
