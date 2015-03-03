package at.joleo.classificationdemo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Date;

import android.R.string;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class CollectionActivity extends ActionBarActivity implements
		SensorEventListener {

	private Button newButton;
	private Button startButton;
	private Button stopButton;
	private Button saveButton;
	private RadioGroup labeledRadioGroup;
	private RadioButton labeledRadio;
	private RadioButton unlabeledRadio;
	private String activity = "default";
	private TextView nameTv;
	private TextView xyzTv;
	private TextView infoTv;
	private TextView saveTv;

	private String filename;
	private StringBuilder dataBuffer; // saves what we'll write to file
	private int lineCnt;

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;

	private int status;

	private static final int REC_STARTED = 1;
	private static final int REC_STOPPED = 2;

	// where on the sdcard the recorded files are stored
	private static final String savedDir = "/AcceleroRecord";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collection);

		newButton = (Button) findViewById(R.id.newButton);
		startButton = (Button) findViewById(R.id.startButton);
		stopButton = (Button) findViewById(R.id.stopButton);
		saveButton = (Button) findViewById(R.id.saveButton);
		labeledRadioGroup = (RadioGroup) findViewById(R.id.labeledRadioGroup);
		labeledRadio = (RadioButton) findViewById(R.id.labeledRadioButton);
		unlabeledRadio = (RadioButton) findViewById(R.id.unlabeledRadioButton);

		nameTv = (TextView) findViewById(R.id.filenameTextview);
		xyzTv = (TextView) findViewById(R.id.xyzTextview);
		infoTv = (TextView) findViewById(R.id.infoTextview);
		saveTv = (TextView) findViewById(R.id.saveTextview);

		// Sensor related stuff
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_FASTEST);

		labeledRadioGroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {

					}
				});
		newButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// set filename and empty other views
				if (!labeledRadio.isChecked() || !unlabeledRadio.isChecked()) {
					showToast("labeled or unlabeled data?");
				}
				if (labeledRadio.isChecked()) {
					displayFilenameForm();
				}
				if (unlabeledRadio.isChecked()) {
					activity = "unlabeled";
					Long tsLong = System.currentTimeMillis() / 1000;
					String ts = tsLong.toString();
					filename = activity + "_" + ts + ".csv";
					nameTv.setText("Filename: " + filename);
					xyzTv.setText("");
					infoTv.setText("");
					saveTv.setText("");
				}
			}
		});

		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// empties databuffer and turns on sensor recording
				status = REC_STARTED;
				dataBuffer = new StringBuilder();
				lineCnt = 0;
				xyzTv.setText("recording ...");
			}
		});

		stopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// didn't start recording yet
				if (status != REC_STARTED) {
					String msg = "start recording first";
					Toast.makeText(getApplicationContext(), msg,
							Toast.LENGTH_SHORT).show();
					return;
				}

				// turns off sensor recording
				status = REC_STOPPED;
				Log.w("data", dataBuffer.toString());

				// display how many lines we recorded
				String msg = "recorded " + lineCnt + " lines of data points";
				infoTv.setText(msg);

				xyzTv.setText("");
			}
		});

		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// writes out databuffer content to file
				writeFile();
			}
		});
	}

	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (status == REC_STARTED) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			String delim = ", ";
			String line = event.timestamp + delim + x + delim + y + delim + z
					+ "\n";
			dataBuffer.append(line);
			lineCnt++;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// ignore
	}

	private void displayFilenameForm() {
		AlertDialog levelDialog = null;

		// Strings to Show In Dialog with Radio Buttons
		final CharSequence[] items = { "Walking", "Jogging", "Climbing Stairs",
				"Sitting", "Standing", "Lying Down" };

		// Creating and Building the Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select an cctivity");
		builder.setSingleChoiceItems(items, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						switch (item) {
						case 0:
							activity = "walking";
							break;
						case 1:
							activity = "jogging";
							break;
						case 2:
							activity = "stairs";
							break;
						case 3:
							activity = "sitting";
							break;
						case 4:
							activity = "standing";
							break;
						case 5:
							activity = "lying";
							break;
						}
					}
				});

		// Set up the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// adding timestamp to filename
				Long tsLong = System.currentTimeMillis() / 1000;
				String ts = tsLong.toString();
				filename = activity + "_" + ts + ".csv";
				nameTv.setText("Filename: " + filename);
				xyzTv.setText("");
				infoTv.setText("");
				saveTv.setText("");
			}
		});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		levelDialog = builder.create();
		levelDialog.show();
	}

	private void writeFile() {
		if (checkExternalMedia()) {
			Log.w("data", "writing to file");

			File log = new File(Environment.getExternalStorageDirectory(),
					filename);
			byte[] data = dataBuffer.toString().getBytes();

			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(
						log.getAbsolutePath(), false));
				out.write(dataBuffer.toString());
				out.write(" : \n");
			} catch (Exception e) {
				Log.e("data", "Error opening Log.", e);
			}

			String size;
			if (data.length < 1024)
				size = data.length + " B";
			else
				size = (data.length / 1024) + " KB";
			String msg = "file saved to " + filename + "\nsize: " + size;
			saveTv.setText(msg);
		} else {
			String msg = "external media not found";
			saveTv.setText(msg);
		}
	}

	private boolean checkExternalMedia() {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			Log.i("data", "State=" + state + " Not good");
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}

		Log.i("data", "Available=" + mExternalStorageAvailable + "Writeable="
				+ mExternalStorageWriteable + " State" + state);
		return (mExternalStorageAvailable && mExternalStorageWriteable);
	}

	// so that we know something was triggered
	public void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
}