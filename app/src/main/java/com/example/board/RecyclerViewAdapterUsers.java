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


public class RecyclerViewAdapterUsers extends RecyclerView.Adapter<RecyclerViewAdapterUsers.ViewHolder> {
    FirebaseStorage storage = FirebaseStorage.getInstance();



    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList <String> mImageNames = new ArrayList<>();
    private ArrayList <String> mImages = new ArrayList<>();
    private ArrayList <String> mUserIds = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapterUsers(Context mContext, ArrayList<String> mImageNames, ArrayList<String> mImages, ArrayList<String> mUserIds) {
        this.mImageNames = mImageNames;
        this.mImages = mImages;
        this.mContext = mContext;
        this.mUserIds = mUserIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_user,parent,false);
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
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick: clicked on: " + mImageNames.get(position));
                Intent profileDetails = new Intent(mContext,ProfileActivity.class);
                profileDetails.putExtra("uid",mUserIds.get(position));
                mContext.startActivity(profileDetails);

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
        RelativeLayout parentLayout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            imageName = itemView.findViewById(R.id.image_name);
            parentLayout = itemView.findViewById(R.id.parent_layout);

        }


    }
}