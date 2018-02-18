package com.porfirio.orariprocida2011.obsolete;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.porfirio.orariprocida2011.R;

public class FinestraDialog extends Dialog implements OnClickListener {

		ConfigData configData;
	private EditText et;

		public FinestraDialog(Context context, ConfigData cd) {
		super(context);
		setContentView(R.layout.finestradialog);
		et = (EditText) findViewById(R.id.editFinestra);
		Button okButton = (Button) findViewById(R.id.btnOK);
		okButton.setOnClickListener(this);
		configData=cd;
		}
		
		
		@Override
		public void onClick(View v) {
				int f=Integer.parseInt(et.getText().toString());

				if ((f>0)&&(f<=24))
					configData.setFinestraTemporale(f);
				this.dismiss();
			}

}
