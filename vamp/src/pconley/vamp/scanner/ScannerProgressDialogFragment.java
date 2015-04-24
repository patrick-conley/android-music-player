package pconley.vamp.scanner;

import pconley.vamp.R;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ScannerProgressDialogFragment extends DialogFragment {
	
	private int progress = 0;
	private int max = 0;

	private ProgressBar progressBar;
	private TextView commentView;
	private TextView totalsView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_scanner_progress,
				container, false);
		
		getDialog().setTitle(R.string.fragment_progress_name);

		progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
		commentView = (TextView) view.findViewById(R.id.progress_comment);
		totalsView = (TextView) view.findViewById(R.id.progress_totals);

		setCancelable(false);

		return view;
	}

	public void setIndeterminate(boolean indeterminate) {
		progressBar.setIndeterminate(indeterminate);
	}

	public void setMax(int max) {
		this.max = max;
		progressBar.setMax(max);
		displayTotals();
	}

	public void setProgress(int progress) {
		this.progress = progress;
		progressBar.setProgress(progress);
		displayTotals();
	}
	
	public void displayComment(String text) {
		commentView.setText(text);
	}
	
	private void displayTotals() {
		totalsView.setText(String.format("%d/%d", progress, max));
	}

}
