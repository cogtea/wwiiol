package archer.handietalkie.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import archer.handietalkie.R;
import archer.handietalkie.models.FacilityModel;


public class FacilityAdapter extends RecyclerView.Adapter<FacilityAdapter.ViewHolder> {
    private ArrayList<FacilityModel> mDataset;

    public FacilityAdapter(ArrayList<FacilityModel> myDataset, Activity activity) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.facility_row, parent, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mName.setText(mDataset.get(position).getOpen());
        holder.mFacility.setText(mDataset.get(position).getName());
        if (mDataset.get(position).getCtry() == 1) {
            holder.mFlag.setImageResource(R.drawable.britain);
        } else if (mDataset.get(position).getCtry() == 3) {
            holder.mFlag.setImageResource(R.drawable.france);
        } else if (mDataset.get(position).getCtry() == 2) {
            holder.mFlag.setImageResource(R.drawable.unitedstates);
        } else if (mDataset.get(position).getCtry() == 4) {
            holder.mFlag.setImageResource(R.drawable.german);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mFacility, mDate, mName;
        public ImageView mFlag;

        public ViewHolder(View v) {
            super(v);
            mName = (TextView) v.findViewById(R.id.rowText);
            mFlag = (ImageView) v.findViewById(R.id.rowIcon);
            mDate = (TextView) v.findViewById(R.id.date);
            mFacility = (TextView) v.findViewById(R.id.facility);
        }
    }

}
