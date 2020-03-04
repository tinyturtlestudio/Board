package com.example.board;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    FirebaseStorage storage = FirebaseStorage.getInstance();



    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList <String> mImageNames = new ArrayList<>();
    private ArrayList <String> mImages = new ArrayList<>();
    private ArrayList <String> mDescription = new ArrayList<>();
    private ArrayList <String> mDistance = new ArrayList<>();
    private ArrayList <QueryDocumentSnapshot> eventsQuery;
    private Context mContext;

    public RecyclerViewAdapter(Context mContext,ArrayList<String> mImageNames, ArrayList<String> mImages,
                               ArrayList<String> mDescription, ArrayList<String> mDistance, ArrayList<QueryDocumentSnapshot> eventsQuery) {
        this.mImageNames = mImageNames;
        this.mImages = mImages;
        this.mContext = mContext;
        this.mDescription = mDescription;
        this.mDistance = mDistance;
        this.eventsQuery = eventsQuery;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        StorageReference imageRef = storage.getReferenceFromUrl(mImages.get(position));
        Log.d(TAG,"onBindViewHolder: Called.");
        Glide.with(mContext)
                .asBitmap()
                .load(imageRef)
                .into(holder.image);
        holder.imageName.setText(mImageNames.get(position));
        holder.description.setText(mDescription.get(position));
        holder.distance.setText(mDistance.get(position) + "mi");
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick: clicked on: " + mImageNames.get(position));
                Log.d("eventsQuery", eventsQuery.get(position).getData().get("eventName").toString() +" " + eventsQuery.get(position).getId());
                Intent eventDetails = new Intent(mContext,IndividualEventPage.class);
                eventDetails.putExtra("eventUserId",eventsQuery.get(position).getData().get("userId").toString());
                eventDetails.putExtra("eventUserName",eventsQuery.get(position).getData().get("userName").toString());
                eventDetails.putExtra("eventId",eventsQuery.get(position).getId());
                eventDetails.putExtra("eventAddress",eventsQuery.get(position).getData().get("eventAddress").toString());
                eventDetails.putExtra("eventDate",eventsQuery.get(position).getData().get("eventDate").toString());
                eventDetails.putExtra("eventName",eventsQuery.get(position).getData().get("eventName").toString());
                eventDetails.putExtra("eventTime",eventsQuery.get(position).getData().get("eventTime").toString());
                eventDetails.putExtra("imageRef",eventsQuery.get(position).getData().get("imageRef").toString());
                eventDetails.putExtra("eventDetails",eventsQuery.get(position).getData().get("eventDetails").toString());
                eventDetails.putExtra("eventLat",eventsQuery.get(position).getData().get("eventLat").toString());
                eventDetails.putExtra("eventLng",eventsQuery.get(position).getData().get("eventLng").toString());
                eventDetails.putExtra("eventDistance",mDistance.get(position) + " mi");
                mContext.startActivity(eventDetails);

            }
        });
    }




    @Override
    public int getItemCount() {
        return mImageNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView image;
        TextView imageName;
        TextView description;
        TextView distance;
        RelativeLayout parentLayout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            imageName = itemView.findViewById(R.id.image_name);
            description = itemView.findViewById(R.id.description);
            distance = itemView.findViewById(R.id.eventDistance);
            parentLayout = itemView.findViewById(R.id.parent_layout);

        }


    }
}