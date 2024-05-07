package id.dkon.app.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.balysv.materialripple.MaterialRippleLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import id.dkon.app.R;
import id.dkon.app.model.Feeling;


public class FeelingsListAdapter extends RecyclerView.Adapter<FeelingsListAdapter.MyViewHolder> {

	private Context mContext;
	private List<Feeling> itemList;

	private OnItemClickListener mOnItemClickListener;

	public class MyViewHolder extends RecyclerView.ViewHolder {

		public ImageView mFeelingImg;
		public MaterialRippleLayout mParent;
		public ProgressBar mProgressBar;

		public MyViewHolder(View view) {

			super(view);

			mParent = (MaterialRippleLayout) view.findViewById(R.id.parent);

			mFeelingImg = (ImageView) view.findViewById(R.id.feelingImg);
			mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		}
	}


	public FeelingsListAdapter(Context mContext, List<Feeling> itemList) {

		this.mContext = mContext;
		this.itemList = itemList;
	}

	public interface OnItemClickListener {

		void onItemClick(View view, Feeling obj, int position);
	}

	public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {

		this.mOnItemClickListener = mItemClickListener;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.feeling_thumbnail, parent, false);


		return new MyViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final Feeling item = itemList.get(position);

		holder.mProgressBar.setVisibility(View.VISIBLE);
		holder.mFeelingImg.setVisibility(View.GONE);

		if (item.getImgUrl() != null && item.getImgUrl().length() > 0) {

			final ImageView img = holder.mFeelingImg;
			final ProgressBar progressView = holder.mProgressBar;

			Picasso.with(mContext)
					.load(item.getImgUrl())
					.into(holder.mFeelingImg, new Callback() {

						@Override
						public void onSuccess() {

							progressView.setVisibility(View.GONE);
							img.setVisibility(View.VISIBLE);
						}

						@Override
						public void onError() {

							progressView.setVisibility(View.GONE);
							img.setVisibility(View.VISIBLE);
							img.setImageResource(R.drawable.mood);
						}
					});

		} else {

			holder.mProgressBar.setVisibility(View.GONE);
			holder.mFeelingImg.setVisibility(View.VISIBLE);

			holder.mFeelingImg.setImageResource(R.drawable.ic_feeling_none);
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

		return itemList.size();
	}
}