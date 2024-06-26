package id.dkon.app.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import id.dkon.app.R;
import id.dkon.app.model.Guest;


public class GuestListAdapter extends RecyclerView.Adapter<GuestListAdapter.MyViewHolder> {

	private Context mContext;
	private List<Guest> itemList;

	public class MyViewHolder extends RecyclerView.ViewHolder {

		public TextView mProfileFullname, mProfileUsername;
		public ImageView mProfilePhoto, mProfileOnlineIcon, mProfileIcon;
		public MaterialRippleLayout mParent;
		public ProgressBar mProgressBar;

		public MyViewHolder(View view) {

			super(view);

			mParent = (MaterialRippleLayout) view.findViewById(R.id.parent);

			mProfilePhoto = (ImageView) view.findViewById(R.id.profileImg);
			mProfileFullname = (TextView) view.findViewById(R.id.profileFullname);
			mProfileUsername = (TextView) view.findViewById(R.id.profileUsername);
			mProfileOnlineIcon = (ImageView) view.findViewById(R.id.profileOnlineIcon);
			mProfileIcon = (ImageView) view.findViewById(R.id.profileIcon);
			mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		}
	}


	public GuestListAdapter(Context mContext, List<Guest> itemList) {

		this.mContext = mContext;
		this.itemList = itemList;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_thumbnail, parent, false);


		return new MyViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, int position) {

		final Guest item = itemList.get(position);

		holder.mProgressBar.setVisibility(View.VISIBLE);
		holder.mProfilePhoto.setVisibility(View.GONE);

		if (item.getGuestUserPhotoUrl() != null && item.getGuestUserPhotoUrl().length() > 0) {

			final ImageView img = holder.mProfilePhoto;
			final ProgressBar progressView = holder.mProgressBar;

			Picasso.with(mContext)
					.load(item.getGuestUserPhotoUrl())
					.into(holder.mProfilePhoto, new Callback() {

						@Override
						public void onSuccess() {

							progressView.setVisibility(View.GONE);
							img.setVisibility(View.VISIBLE);
						}

						@Override
						public void onError() {

							progressView.setVisibility(View.GONE);
							img.setVisibility(View.VISIBLE);
							img.setImageResource(R.drawable.profile_default_photo);
						}
					});

		} else {

			holder.mProgressBar.setVisibility(View.GONE);
			holder.mProfilePhoto.setVisibility(View.VISIBLE);

			holder.mProfilePhoto.setImageResource(R.drawable.profile_default_photo);
		}

		holder.mProfileFullname.setText(item.getGuestUserFullname());

		holder.mProfileUsername.setText(item.getTimeAgo() + " @" + item.getGuestUserUsername());

		if (item.isOnline()) {

			holder.mProfileOnlineIcon.setVisibility(View.VISIBLE);

		} else {

			holder.mProfileOnlineIcon.setVisibility(View.GONE);
		}

		if (item.isVerify()) {

			holder.mProfileIcon.setVisibility(View.VISIBLE);

		} else {

			holder.mProfileIcon.setVisibility(View.GONE);
		}
	}

	@Override
	public int getItemCount() {

		return itemList.size();
	}
}