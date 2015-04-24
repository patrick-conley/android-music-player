package pconley.vamp.scanner;

import pconley.vamp.R;
import pconley.vamp.library.LibraryActivity;
import pconley.vamp.util.BroadcastConstants;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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

/**
 * Fragment monitoring progress of the {@link FilesystemScanner}.
 * 
 * @author pconley
 */
public class ScannerProgressDialogFragment extends DialogFragment {

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
	public void onResume() {
		super.onResume();

		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				receiver, new IntentFilter(BroadcastConstants.FILTER_SCANNER));
	}

	@Override
	public void onPause() {
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
				receiver);

		super.onPause();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_scanner_progress,
				container, false);

		getDialog().setTitle(R.string.fragment_progress_name);

		progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
		commentView = (TextView) view.findViewById(R.id.progress_comment);
		totalsView = (TextView) view.findViewById(R.id.progress_totals);

		setCancelable(true);

		return view;
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		getActivity().finish();

		super.onCancel(dialog);
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
				((LibraryActivity) getActivity()).loadLibrary();

				Toast.makeText(
						getActivity(),
						intent.getStringExtra(BroadcastConstants.EXTRA_MESSAGE),
						Toast.LENGTH_LONG).show();

				dismiss();

				break;
			case UPDATE:

				if (intent.hasExtra(BroadcastConstants.EXTRA_MAX)) {
					progressBar.setIndeterminate(false);
					setMax(intent.getIntExtra(BroadcastConstants.EXTRA_MAX, 0));
				}

				if (intent.hasExtra(BroadcastConstants.EXTRA_PROGRESS)) {
					setProgress(intent.getIntExtra(
							BroadcastConstants.EXTRA_PROGRESS, 0));
				}

				if (intent.hasExtra(BroadcastConstants.EXTRA_MESSAGE)) {
					commentView.setText(intent
							.getStringExtra(BroadcastConstants.EXTRA_MESSAGE));
				}

				break;
			}
		}
	}

}
