package com.porunga.youroomclient;

import android.content.Context;
import android.text.style.URLSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.PopupWindow;
import android.widget.TextView;

public class PopupLink extends PopupWindow {
	private final LayoutInflater inflater;

	private final View popUpContainer;
	private final AbsListView parent;
	TextView linkTextView;
	private int urlNum = 0;

	public PopupLink(Context context, AbsListView parent) {
		super(parent);
		this.parent = parent;
		this.inflater = LayoutInflater.from(context);

		popUpContainer = inflater.inflate(R.layout.popup_window, null);

		setWindowLayoutMode(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		setContentView(popUpContainer);
		linkTextView = (TextView) popUpContainer.findViewById(R.id.popup_text);
		parent.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				if (isShowing())
					dismiss();

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}
		});
		parent.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_BACK)
					if (isShowing()) {
						dismiss();
						return true;
					}
				return false;
			}
		});

	}

	public int getUrlNum() {
		return urlNum;
	}

	public void setLinkText(String link) {
		TextView linkTextView = (TextView) popUpContainer.findViewById(R.id.popup_text);
		linkTextView.setText(link);
		URLSpan[] urls = linkTextView.getUrls();

		StringBuffer urls_builder = new StringBuffer();
		String crlf = System.getProperty("line.separator");
		urlNum = urls.length;
		if (urlNum != 0) {
			for (URLSpan url : urls) {
				urls_builder.append(url.getURL());
				urls_builder.append(crlf + crlf);
			}
			linkTextView.setText(urls_builder.toString().substring(0, urls_builder.length() - 2));
		}
	}
}
