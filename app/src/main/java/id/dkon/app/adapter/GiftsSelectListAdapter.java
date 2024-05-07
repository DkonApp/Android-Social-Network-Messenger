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
import java.util.Locale;

import id.dkon.app.R;
import id.dkon.app.model.BaseGift;


public class GiftsSelectListAdapter extends RecyclerView.Adapter<GiftsSelectListAdapter.MyViewHolder> {

	private Context mContext;
	private List<BaseGift> itemList;

	private OnItemClickListener mOnItemClickListener;

	public class MyViewHolder extends RecyclerView.ViewHolder {

		public ImageView mImage;
		public TextView mPriceLabel;
		public MaterialRippleLayout mParent;
		public ProgressBar mProgressBar;

		public MyViewHolder(View view) {

			super(view);

			mParent = (MaterialRippleLayout) view.findViewById(R.id.parent);

			mImage = (ImageView) view.findViewById(R.id.image);
			mPriceLabel = (TextView) view.findViewById(R.id.price_label);
			mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
		}
	}


	public GiftsSelectListAdapter(Context mContext, List<BaseGift> itemList) {

		this.mContext = mContext;
		this.itemList = itemList;
	}

	public interface OnItemClickListener {

		void onItemClick(View view, BaseGift obj, int position);
	}

	public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {

		this.mOnItemClickListener = mItemClickListener;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gift_thumbnail, parent, false);


		return new MyViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final BaseGift item = itemList.get(position);

		holder.mProgressBar.setVisibility(View.VISIBLE);
		holder.mImage.setVisibility(View.GONE);
		holder.mPriceLabel.setVisibility(View.GONE);

		if (item.getImgUrl() != null && item.getImgUrl().length() > 0) {

			final ImageView img = holder.mImage;
			final TextView price = holder.mPriceLabel;
			final ProgressBar progressView = holder.mProgressBar;

			Picasso.with(mContext)
					.load(item.getImgUrl())
					.into(holder.mImage, new Callback() {

						@Override
						public void onSuccess() {

							progressView.setVisibility(View.GONE);
							img.setVisibility(View.VISIBLE);
							price.setVisibility(View.VISIBLE);
						}

						@Override
						public void onError() {

							progressView.setVisibility(View.GONE);
							img.setVisibility(View.VISIBLE);
							img.setImageResource(R.drawable.img_loading);
							price.setVisibility(View.VISIBLE);
						}
					});

		} else {

			holder.mProgressBar.setVisibility(View.GONE);
			holder.mImage.setVisibility(View.VISIBLE);
			holder.mPriceLabel.setVisibility(View.VISIBLE);

			holder.mImage.setImageResource(R.drawable.ic_feeling_none);
		}

		holder.mPriceLabel.setText(String.format(Locale.getDefault(), mContext.getString(R.string.price_of), item.getCost()));

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