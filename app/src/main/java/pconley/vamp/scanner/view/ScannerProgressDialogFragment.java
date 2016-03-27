package pconley.vamp.scanner.view;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import pconley.vamp.R;
import pconley.vamp.library.view.LibraryActivity;
import pconley.vamp.scanner.ScannerEvent;
import pconley.vamp.scanner.ScannerService;
import pconley.vamp.util.BroadcastConstants;

/**
 * Fragment monitoring progress of the {@link ScannerService}.
 *
 * @author pconley
 */
public class ScannerProgressDialogFragment extends DialogFragment {
	public static final String TAG = "Scanner dialog";

	private ScannerBroadcastReceiver receiver;

	private int progress = 0;
	private int max = 0;

	private ProgressBar progressBar;
	private TextView commentView;
	private TextView totalsView;

	public ScannerProgressDialogFragment() {
		receiver = new ScannerBroadcastReceiver();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setCancelable(false);

		// Restore the dialog after sleep or rotate
		setRetainInstance(true);
	}

	@Override
	public void onStart() {
		super.onStart();

		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				receiver, new IntentFilter(BroadcastConstants.FILTER_SCANNER));
	}

	@Override
	public void onStop() {
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
				receiver);

		super.onStop();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_scanner_progress,
		                             container, false);

		getDialog().setTitle(R.string.fragment_progress_name);

		progressBar = (ProgressBar) view.findViewById(R.id.scanner_progress);
		commentView = (TextView) view.findViewById(R.id.scanner_view_comment);
		totalsView = (TextView) view.findViewById(R.id.scanner_view_totals);

		return view;
	}

	@Override
	public void onDestroyView() {
		// Magic that allows the dialog's state to be saved when its parent
		// activity is destroyed (by rotate, Home, etc.)
		if (getDialog() != null) {
			getDialog().setDismissMessage(null);
		}

		super.onDestroyView();
	}

	private void setMax(int max) {
		this.max = max;
		progressBar.setMax(max);
		displayTotals();
	}

	private void setProgress(int progress) {
		this.progress = progress;
		progressBar.setProgress(progress);
		displayTotals();
	}

	private void displayTotals() {
		totalsView.setText(String.format("%d/%d", progress, max));
	}

	private class ScannerBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			switch ((ScannerEvent) intent
					.getSerializableExtra(BroadcastConstants.EXTRA_EVENT)) {
				case FINISHED:
					LibraryActivity activity = (LibraryActivity) getActivity();
					activity.loadLibrary();

					Toast.makeText(
							activity,
							intent.getStringExtra(
									BroadcastConstants.EXTRA_MESSAGE),
							Toast.LENGTH_LONG).show();

					dismiss();

					break;
				case UPDATE:

					if (intent.hasExtra(BroadcastConstants.EXTRA_TOTAL)) {
						progressBar.setIndeterminate(false);
						setMax(intent
								       .getIntExtra(
										       BroadcastConstants.EXTRA_TOTAL,
										       0));
					}

					if (intent.hasExtra(BroadcastConstants.EXTRA_PROGRESS)) {
						setProgress(intent.getIntExtra(
								BroadcastConstants.EXTRA_PROGRESS, 0));
					}

					if (intent.hasExtra(BroadcastConstants.EXTRA_MESSAGE)) {
						commentView.setText(intent
								                    .getStringExtra(
										                    BroadcastConstants
												                    .EXTRA_MESSAGE));
					}

					break;
				case ERROR:

					Toast.makeText(
							getActivity(),
							intent.getStringExtra(
									BroadcastConstants.EXTRA_MESSAGE),
							Toast.LENGTH_SHORT).show();
					break;
			}
		}
	}

}
