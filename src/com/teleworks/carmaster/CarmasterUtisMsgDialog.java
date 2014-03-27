package com.teleworks.carmaster;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class CarmasterUtisMsgDialog extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_utis_msg_dialog);
		setResult(Activity.RESULT_CANCELED);

		Toast.makeText(CarmasterUtisMsgDialog.this, "되었습니다.",
				Toast.LENGTH_SHORT).show();
		Button btnOk = (Button) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}