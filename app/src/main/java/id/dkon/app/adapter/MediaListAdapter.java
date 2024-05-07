package id.dkon.app.adapter;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.List;

import id.dkon.app.BuildConfig;
import id.dkon.app.R;
import id.dkon.app.app.App;
import id.dkon.app.model.MediaItem;


public class MediaListAdapter extends RecyclerView.Adapter<MediaListAdapter.MyViewHolder> {

    private List<MediaItem> items;
    private Context ctx;

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {

        void onItemClick(View view, MediaItem obj, int position, int action);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {

        this.mOnItemClickListener = mItemClickListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView thumbnail, mPlayIcon;
        public ProgressBar mProgressBar;
        public MaterialRippleLayout mParent;
        public LinearLayout mDelete;

        public MyViewHolder(View view) {

            super(view);

            mParent = (MaterialRippleLayout) view.findViewById(R.id.parent);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            mPlayIcon = (ImageView) view.findViewById(R.id.play_icon);
            mDelete = (LinearLayout) view.findViewById(R.id.delete);
            mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        }
    }


    public MediaListAdapter(Context context, List<MediaItem> items) {

        this.ctx = context;
        this.items = items;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        final MediaItem item = items.get(position);

        holder.thumbnail.setVisibility(View.VISIBLE);
        holder.mProgressBar.setVisibility(View.VISIBLE);
        holder.mDelete.setVisibility(View.GONE);
        holder.mPlayIcon.setVisibility(View.GONE);

        if (item.getImageUrl().length() > 0) {

            final ProgressBar progressBar = holder.mProgressBar;
            final ImageView imageView = holder.thumbnail;
            final LinearLayout deleteButton = holder.mDelete;
            final ImageView playButton = holder.mPlayIcon;

            Glide.with(ctx)
                    .load(item.getImageUrl())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                            progressBar.setVisibility(View.GONE);
                            imageView.setImageResource(R.drawable.img_loading);
                            deleteButton.setVisibility(View.VISIBLE);

                            if (item.getType() != 0) {

                                playButton.setVisibility(View.VISIBLE);
                            }

                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                            progressBar.setVisibility(View.GONE);
                            deleteButton.setVisibility(View.VISIBLE);

                            if (item.getType() != 0) {

                                playButton.setVisibility(View.VISIBLE);
                            }

                            return false;
                        }
                    })
                    .into(holder.thumbnail);

        } else {

            holder.mProgressBar.setVisibility(View.GONE);
            holder.mDelete.setVisibility(View.VISIBLE);

            if (item.getType() != 0) {

                holder.mPlayIcon.setVisibility(View.VISIBLE);

                if (item.getSelectedImageFileName().length() > 0) {

                    holder.thumbnail.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), item.getSelectedImageFileName())));

                } else {

                    holder.thumbnail.setImageResource(R.drawable.ic_video_preview);
                }

            } else {

                if (item.getSelectedImageFileName().length() > 0) {

                    holder.thumbnail.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(item.getSelectedImageFileName())));
                }
            }
        }

        holder.mDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mOnItemClickListener != null) {

                    mOnItemClickListener.onItemClick(v, item, position, 1);
                }
            }
        });

        holder.mParent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (mOnItemClickListener != null) {

                    mOnItemClickListener.onItemClick(view, item, position, 0);
                }
            }
        });
    }

    @Override
    public int getItemCount() {

        return items.size();
    }
}