package id.dkon.app.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;
import java.util.Locale;

import github.ankushsachdeva.emojicon.EmojiconTextView;
import id.dkon.app.R;
import id.dkon.app.constants.Constants;
import id.dkon.app.model.Chat;
import id.dkon.app.model.Group;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class GroupsListAdapter extends RecyclerView.Adapter<GroupsListAdapter.ViewHolder> implements Constants {

    private Context ctx;
    private List<Group> items;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {

        void onItemClick(View view, Group item, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {

        this.mOnItemClickListener = mItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title, time;
        public CircularImageView image, online, verified, icon;
        public LinearLayout parent;
        public EmojiconTextView message;

        public ViewHolder(View view) {

            super(view);

            title = (TextView) view.findViewById(R.id.title);
            message = (EmojiconTextView) view.findViewById(R.id.message);
            time = (TextView) view.findViewById(R.id.time);
            image = (CircularImageView) view.findViewById(R.id.image);
            parent = (LinearLayout) view.findViewById(R.id.parent);

            online = (CircularImageView) view.findViewById(R.id.online);
            verified = (CircularImageView) view.findViewById(R.id.verified);
            icon = (CircularImageView) view.findViewById(R.id.icon);
        }
    }

    public GroupsListAdapter(Context mContext, List<Group> items) {

        this.ctx = mContext;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_list_row, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final Group item = items.get(position);

        holder.icon.setVisibility(View.GONE);
        holder.online.setVisibility(View.GONE);
        holder.verified.setVisibility(View.GONE);
        holder.image.setVisibility(View.VISIBLE);

        if (item.getVerify() == 1) {

            holder.verified.setVisibility(View.VISIBLE);
        }

        if (item.getLowPhotoUrl().length() > 0) {

            try {

                Glide.with(ctx).load(item.getLowPhotoUrl())
                        .transition(withCrossFade())
                        .into(holder.image);

            } catch (Exception e) {

                Log.e("GroupsListAdapter", e.toString());
            }

        } else {

            holder.image.setImageResource(R.drawable.profile_default_photo);
        }

        holder.title.setText(item.getFullname());
        holder.message.setText(item.getBio());

        holder.time.setText(String.format(Locale.getDefault(), "%d %s", item.getFollowersCount(), ctx.getString(R.string.label_followers)));

        holder.parent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mOnItemClickListener != null) {

                    mOnItemClickListener.onItemClick(v, items.get(position), position);
                }
            }
        });
    }

    public Group getItem(int position) {

        return items.get(position);
    }

    @Override
    public int getItemCount() {

        return items.size();
    }

    public interface OnClickListener {

        void onItemClick(View view, Chat item, int pos);
    }
}