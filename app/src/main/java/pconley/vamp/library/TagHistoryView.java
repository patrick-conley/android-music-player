package pconley.vamp.library;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Stack;

import pconley.vamp.R;
import pconley.vamp.model.Tag;

/**
 * Display a list of tags that are ancestors to the collection being displayed.
 */
public class TagHistoryView extends RecyclerView {

	private Adapter adapter;

	public TagHistoryView(Context context) {
		this(context, null, 0);
	}

	public TagHistoryView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TagHistoryView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		adapter = new Adapter();
		setAdapter(adapter);
	}

	public void push(Tag tag) {
		adapter.push(tag);
		scrollToPosition(adapter.getItemCount() - 1);
	}

	public void pop() {
		adapter.pop();
		scrollToPosition(adapter.getItemCount() - 1);
	}

	/**
	 * Adapter controlling tags in the history
	 */
	public class Adapter
			extends RecyclerView.Adapter<TagHistoryView.ViewHolder> {

		private Stack<Tag> tags;

		public Adapter() {
			this.tags = new Stack<Tag>();
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent,
				int i) {
			View view = LayoutInflater
					.from(parent.getContext())
					.inflate(R.layout.tag_history_item, parent, false);

			return new ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(ViewHolder viewHolder, int i) {
			viewHolder.bind(tags.get(i));
		}

		@Override
		public int getItemCount() {
			return tags.size();
		}

		public void push(Tag tag) {
			tags.push(tag);
			notifyItemInserted(getItemCount() - 1);
		}

		public void pop() {
			// Tags should only be empty when backing out of the app
			if (!tags.isEmpty()) {
				tags.pop();
				notifyItemRemoved(getItemCount());
			}
		}

	}

	/**
	 * Describe the view holding each tag in the history
	 */
	public static class ViewHolder extends RecyclerView.ViewHolder {

		private TextView textView;

		public ViewHolder(View itemView) {
			super(itemView);
			this.textView = (TextView) itemView.findViewById(
					R.id.tag_history_item);
		}

		public void bind(Tag tag) {
			textView.setText(tag.getValue());
		}
	}
}
