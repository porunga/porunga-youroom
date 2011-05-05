package com.porunga.youroomclient;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ContentsDialogUtil extends YouRoomUtil {

	private static final int SHOW_IMAGE_DIALOG = 1;
	private static final String ATTACHMENT_TYPE_TEXT = "Text";
	private static final String ATTACHMENT_TYPE_LINK = "Link";
	private static final String ATTACHMENT_TYPE_FILE = "File";
	private static final String ATTACHMENT_TYPE_IMAGE = "Image";

	private AlertDialog contentDialog;
	protected AlertDialog imageDialog;
	protected ImageView imageView;
	protected MainHandler mHandler = new MainHandler();

	public ContentsDialogUtil(Context base) {
		super(base);
		// TODO Auto-generated constructor stub
	}

	public void showContentsDialog(YouRoomEntry item, final String roomId) {
		TextView text = new TextView(this);
		text.setAutoLinkMask(Linkify.ALL);
		text.setText(item.getContent());
		URLSpan[] urls = text.getUrls();
		String type = item.getAttachmentType();
		final String entryId = String.valueOf(item.getId());
		if (urls.length == 0 && type.equals(ATTACHMENT_TYPE_TEXT))
			showLongTextDialog(item.getText());
		else if (urls.length == 0 && type.equals(ATTACHMENT_TYPE_IMAGE))
			showAttachmentImageDialog(roomId, entryId);
		else if (urls.length != 0 || !type.equals("")) {
			ArrayList<DialogContent> rows = new ArrayList<DialogContent>();
			if (type.equals(ATTACHMENT_TYPE_TEXT))
				rows.add(new DialogContent(ATTACHMENT_TYPE_TEXT, item.getText()));
			else if (type.equals(ATTACHMENT_TYPE_LINK))
				rows.add(new DialogContent(ATTACHMENT_TYPE_LINK, item.getLink()));
			else if (type.equals(ATTACHMENT_TYPE_FILE))
				rows.add(new DialogContent(ATTACHMENT_TYPE_FILE, item.getFileName()));
			else if (type.equals(ATTACHMENT_TYPE_IMAGE))
				rows.add(new DialogContent(ATTACHMENT_TYPE_IMAGE, item.getFileName()));

			for (URLSpan url : urls)
				rows.add(new DialogContent(ATTACHMENT_TYPE_LINK, url.getURL()));
			DialogContentsAdapter adapter = new DialogContentsAdapter(this, R.layout.dialog_list_item, rows);
			ListView contentsListView = new ListView(this);
			contentsListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					contentDialog.dismiss();
					ListView listView = (ListView) parent;
					DialogContent dialogContent = (DialogContent) listView.getItemAtPosition(position);
					String category = dialogContent.getCategory();
					if (category.equals(ATTACHMENT_TYPE_TEXT))
						showLongTextDialog(dialogContent.getContent());
					if (category.equals(ATTACHMENT_TYPE_LINK))
						getBaseContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(dialogContent.getContent())));
					if (category.equals(ATTACHMENT_TYPE_FILE)) {
						String api = "https://www.youroom.in/r/" + roomId + "/entries/" + entryId + "/attachment";
						getBaseContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(api)));
					}
					if (category.equals(ATTACHMENT_TYPE_IMAGE)) {
						showAttachmentImageDialog(roomId,entryId);
					}
				}
			});
			contentsListView.setAdapter(adapter);
			contentsListView.setScrollingCacheEnabled(false);
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setView(contentsListView);
			contentDialog = alertDialogBuilder.create();
			contentDialog.show();
		}
	}

	public class DialogContentsAdapter extends ArrayAdapter<DialogContent> {
		private LayoutInflater inflater;

		public DialogContentsAdapter(Context context, int textViewResourceId, ArrayList<DialogContent> dialogContents) {
			super(context, textViewResourceId, dialogContents);
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.dialog_list_item, null);
			}

			DialogContent dialogContent = (DialogContent) this.getItem(position);
			ImageView categoryImage = null;
			TextView content = null;

			if (dialogContent != null) {
				categoryImage = (ImageView) view.findViewById(R.id.category);
				content = (TextView) view.findViewById(R.id.dialog_content);
			}
			if (categoryImage != null) {
				if (dialogContent.getCategory().equals(ATTACHMENT_TYPE_TEXT))
					categoryImage.setImageResource(R.drawable.text_icon);
				else if (dialogContent.getCategory().equals(ATTACHMENT_TYPE_LINK))
					categoryImage.setImageResource(R.drawable.link_icon);
				else if (dialogContent.getCategory().equals(ATTACHMENT_TYPE_FILE))
					categoryImage.setImageResource(R.drawable.file_icon);
				else if (dialogContent.getCategory().equals(ATTACHMENT_TYPE_IMAGE))
					categoryImage.setImageResource(R.drawable.image_icon);
			}
			if (content != null) {
				if (dialogContent.getCategory().equals(ATTACHMENT_TYPE_TEXT))
					content.setText(getString(R.string.attachment_type_text));
				else
					content.setText(dialogContent.getContent());
			}

			return view;
		}
	}

	public void showLongTextDialog(String text) {
		ScrollView scv = new ScrollView(this);
		TextView textView = new TextView(this);
		textView.setAutoLinkMask(Linkify.ALL);
		textView.setText(text);
		scv.addView(textView);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setView(scv);
		alertDialogBuilder.create().show();
	}

	public void showAttachmentImageDialog(final String roomId, final String entryId) {
		final YouRoomCommand youRoomCommand = new YouRoomCommand(getOauthTokenFromLocal());
		imageView = new ImageView(this);

		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getString(R.string.now_loading));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.show();

		(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					imageView.setImageBitmap(youRoomCommand.getAttachmentImage(roomId, entryId));
					progressDialog.dismiss();
					mHandler.sendEmptyMessage(SHOW_IMAGE_DIALOG);
				} catch (YouRoomServerException e) {
					Toast.makeText(getBaseContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		})).start();

	}

	private class MainHandler extends Handler {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			
			case SHOW_IMAGE_DIALOG: {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getBaseContext());
				alertDialogBuilder.setView(imageView);
				alertDialogBuilder.create().show();
//				imageDialog.show();
				break;
			}
			}
		}
	}

}
