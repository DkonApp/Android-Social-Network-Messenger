package id.dkon.app.adapter;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import id.dkon.app.R;
import id.dkon.app.model.GalleryItem;
import id.dkon.app.model.Image;


public class GallerySpotlightListAdapter extends RecyclerView.Adapter<GallerySpotlightListAdapter.MyViewHolder> {

    private List<GalleryItem> items;
    private Context mContext;

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {

        void onItemClick(View view, GalleryItem obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {

        this.mOnItemClickListener = mItemClickListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView thumbnail, mPlayIcon;
        public ProgressBar mProgressBar;
        public MaterialRippleLayout mParent;

        public MyViewHolder(View view) {

            super(view);

            mParent = (MaterialRippleLayout) view.findViewById(R.id.parent);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            mPlayIcon = (ImageView) view.findViewById(R.id.play_icon);
        }
    }


    public GallerySpotlightListAdapter(Context context, List<GalleryItem> items) {

        mContext = context;
        this.items = items;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_spotlight_thumbnail, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        final GalleryItem item = items.get(position);

        holder.mPlayIcon.setVisibility(View.GONE);
        holder.thumbnail.setVisibility(View.VISIBLE);
        holder.mProgressBar.setVisibility(View.VISIBLE);

        if (item.getVideoUrl().length() != 0) {

            if (item.getPreviewVideoImgUrl().length() > 0) {

                final ProgressBar progressBar = holder.mProgressBar;
                final ImageView imageView = holder.thumbnail;
                final ImageView playIcon = holder.mPlayIcon;

                Glide.with(mContext)
                        .load(item.getPreviewVideoImgUrl())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                progressBar.setVisibility(View.GONE);
                                playIcon.setVisibility(View.VISIBLE);
                                imageView.setImageResource(R.drawable.profile_default_photo);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                progressBar.setVisibility(View.GONE);
                                playIcon.setVisibility(View.VISIBLE);
                                return false;
                            }
                        })
                        .into(holder.thumbnail);

            } else {

                holder.mProgressBar.setVisibility(View.GONE);
                holder.mPlayIcon.setVisibility(View.VISIBLE);
                holder.thumbnail.setImageResource(R.drawable.profile_default_photo);
            }

        } else {

            if (item.getImgUrl() != null && item.getImgUrl().length() > 0) {

                final ProgressBar progressBar = holder.mProgressBar;
                final ImageView imageView = holder.thumbnail;

                Glide.with(mContext)
                        .load(item.getImgUrl())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                progressBar.setVisibility(View.GONE);
                                imageView.setImageResource(R.drawable.profile_default_photo);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(holder.thumbnail);

            } else {

                holder.mProgressBar.setVisibility(View.GONE);
                holder.thumbnail.setImageResource(R.drawable.profile_default_photo);
            }
        }

        holder.mParent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (mOnItemClickListener != null) {

                    mOnItemClickListener.onItemClick(view, item, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {

        return items.size();
    }
}