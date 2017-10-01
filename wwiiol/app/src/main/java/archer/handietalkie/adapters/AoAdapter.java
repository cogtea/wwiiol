package archer.handietalkie.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import archer.handietalkie.MainActivity;
import archer.handietalkie.R;
import archer.handietalkie.models.AoModel;

/**
 * Created by Ramy Sabry on 9/26/2015.
 */
public class AoAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<AoModel>> _listDataChild;

    public AoAdapter(Context context, List<String> listDataHeader,
                     HashMap<String, List<AoModel>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public AoModel getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final AoModel child = getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.cityname);
        ImageView own = (ImageView) convertView
                .findViewById(R.id.own);
        ImageView contention = (ImageView) convertView
                .findViewById(R.id.contention);
        if (child.isContention()) {
            contention.setVisibility(View.VISIBLE);
            // blinking
            final Animation animation = new AlphaAnimation(1, 0);
            animation.setDuration(1000);
            animation.setInterpolator(new LinearInterpolator());
            animation.setRepeatCount(Animation.INFINITE);
            animation.setRepeatMode(Animation.REVERSE);
            contention.startAnimation(animation);
            //
            contention.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(((AppCompatActivity) _context).findViewById(android.R.id.content), child.getName() + " has contention", Snackbar.LENGTH_LONG).setAction("Status", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((MainActivity) _context).onChildClick(null, null, groupPosition, childPosition, getChildId(groupPosition, childPosition));
                        }
                    }).show();
                }
            });
        } else {
            contention.setVisibility(View.INVISIBLE);
        }
        txtListChild.setText(child.getName());
        if (child.getOwn() == 1) {
            own.setImageResource(R.drawable.britain);
        } else if (child.getOwn() == 3) {
            own.setImageResource(R.drawable.france);
        } else if (child.getOwn() == 2) {
            own.setImageResource(R.drawable.unitedstates);
        } else if (child.getOwn() == 4) {
            own.setImageResource(R.drawable.german);
        }
        convertView.setMinimumHeight(140);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}