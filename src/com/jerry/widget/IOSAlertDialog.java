package com.jerry.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.jerry.lily.R;

public class IOSAlertDialog extends Dialog {

	public IOSAlertDialog(Context context, int theme) {
		super(context, theme);
		getWindow().setLayout(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		getWindow().setWindowAnimations(R.style.windowAnimation);
	}

	public static class Builder {
		private Context context;
		private String title;
		private String message;
		private String positiveButtonText;
		private String negativeButtonText;
		private DialogInterface.OnClickListener positiveButtonClickListener, negativeButtonClickListener;

		public Builder(Context context) {
			this.context = context;
		}

		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}

		public Builder setMessage(int message) {
			this.message = (String) context.getText(message);
			return this;
		}

		public Builder setTitle(int title) {
			this.title = (String) context.getText(title);
			return this;
		}

		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder setPositiveButton(int positiveButtonText,DialogInterface.OnClickListener listener) {
			this.positiveButtonText = (String) context.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}


		public Builder setPositiveButton(String positiveButtonText,DialogInterface.OnClickListener listener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}
		
		public Builder setNegativeButtonText(String negativeButtonText) {
			this.negativeButtonText = negativeButtonText;
			return this;
		}

		public Builder setNegativeButton(String negativeButtonText,DialogInterface.OnClickListener listener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		public IOSAlertDialog create() {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final IOSAlertDialog dialog = new IOSAlertDialog(context,R.style.iosDialog);
			View layout = inflater.inflate(R.layout.alert_dialog, null);
			((TextView) layout.findViewById(R.id.alert_dialog_title)).setText(title);
			if (positiveButtonText != null) {
				((Button) layout.findViewById(R.id.alert_dialog_positiveButton)).setText(positiveButtonText);
				if (positiveButtonClickListener != null) {
					((Button) layout.findViewById(R.id.alert_dialog_positiveButton)).setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							positiveButtonClickListener.onClick(dialog,DialogInterface.BUTTON_POSITIVE);
						}
					});
				} else {
					((Button) layout.findViewById(R.id.alert_dialog_positiveButton)).setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
				}
			} else {
				layout.findViewById(R.id.alert_dialog_positiveButton).setVisibility(View.GONE);
			}

			if (negativeButtonText != null) {
				((Button) layout.findViewById(R.id.alert_dialog_negativeButton)).setText(negativeButtonText);
			} 

			if (negativeButtonClickListener != null) {
				((Button) layout.findViewById(R.id.alert_dialog_negativeButton)).setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						negativeButtonClickListener.onClick(dialog,DialogInterface.BUTTON_NEGATIVE);
					}
				});
			} else {
				((Button) layout.findViewById(R.id.alert_dialog_negativeButton)).setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}

			if (message != null) {
				((TextView) layout.findViewById(R.id.alert_dialog_message)).setText(message);
			} 

			dialog.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			return dialog;
		}
	}
}


