package at.joleo.classificationdemo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ClassificationActivity extends ActionBarActivity {

	private Button startButton;
	private Button toggleLogButton;
	private Button toggleDurationButton;
	private Button toggleMatrixButton;
	private Button toggleResultButton;

	private Spinner classifierSpinner;
	private ProgressBar progressBar;
	private static TextView outputTextView;
	private static TextView logTextView;
	private static TextView durationTextView;
	private static TextView matrixTextView;
	private CheckBox xCheckbox;
	private CheckBox yCheckbox;
	private CheckBox zCheckbox;
	private CheckBox AvgCheckbox;
	private CheckBox PeakCheckbox;
	private CheckBox AbsDevCheckbox;
	private CheckBox StandDevCheckbox;
	private long startTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_classification);

		Spinner spinner = (Spinner) findViewById(R.id.classifierSpinner);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(this, R.array.classifiers,
						android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);

		startButton = (Button) findViewById(R.id.startClassifyButton);
		toggleLogButton = (Button) findViewById(R.id.LogToggleButton);
		toggleDurationButton = (Button) findViewById(R.id.DurationToggleButton);
		toggleMatrixButton = (Button) findViewById(R.id.MatrixToggleButton);
		toggleResultButton = (Button) findViewById(R.id.ResultToggleButton);

		classifierSpinner = (Spinner) findViewById(R.id.classifierSpinner);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		outputTextView = (TextView) findViewById(R.id.SummaryTextView);
		logTextView = (TextView) findViewById(R.id.LogTextView);
		durationTextView = (TextView) findViewById(R.id.DurationTextView);
		matrixTextView = (TextView) findViewById(R.id.MatrixTextView);
		xCheckbox = (CheckBox) findViewById(R.id.xCheckbox);
		yCheckbox = (CheckBox) findViewById(R.id.yCheckbox);
		zCheckbox = (CheckBox) findViewById(R.id.zCheckbox);
		AvgCheckbox = (CheckBox) findViewById(R.id.AVGCheckbox);
		PeakCheckbox = (CheckBox) findViewById(R.id.PeakCheckbox);
		AbsDevCheckbox = (CheckBox) findViewById(R.id.AbsDevCheckbox);
		StandDevCheckbox = (CheckBox) findViewById(R.id.StandDevCheckbox);

		progressBar.setVisibility(View.INVISIBLE);
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startTime = System.currentTimeMillis();
				progressBar.setVisibility(View.VISIBLE);
				outputTextView.setText("working...");
				startButton.setEnabled(false);
				startClassification();
			}

		});

		toggleLogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				toggleLog();
			}

			private void toggleLog() {
				if (logTextView.getVisibility() == View.VISIBLE) {
					toggleLogButton.setText("Hide");
					logTextView.setVisibility(View.INVISIBLE);
				} 
				else if(logTextView.getVisibility() == View.INVISIBLE) {
					toggleLogButton.setText("Show");
					logTextView.setVisibility(View.VISIBLE);
				}
			}
		});
		
		toggleDurationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				toggleDuration();
			}

			private void toggleDuration() {
				if (durationTextView.getVisibility() == View.VISIBLE) {
					toggleDurationButton.setText("Hide");
					durationTextView.setVisibility(View.INVISIBLE);
				} 
				else if(durationTextView.getVisibility() == View.INVISIBLE) {
					toggleDurationButton.setText("Show");
					durationTextView.setVisibility(View.VISIBLE);
				}
			}
		});
		
		toggleMatrixButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				toggleMatrix();
			}

			private void toggleMatrix() {
				if (matrixTextView.getVisibility() == View.VISIBLE) {
					toggleMatrixButton.setText("Hide");
					matrixTextView.setVisibility(View.INVISIBLE);
				} 
				else if(matrixTextView.getVisibility() == View.INVISIBLE) {
					toggleMatrixButton.setText("Show");
					matrixTextView.setVisibility(View.VISIBLE);
				}
			}
		});
		
		toggleResultButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				toggleResult();
			}

			private void toggleResult() {
				if (outputTextView.getVisibility() == View.VISIBLE) {
					toggleResultButton.setText("Hide");
					outputTextView.setVisibility(View.INVISIBLE);
				} 
				else if(outputTextView.getVisibility() == View.INVISIBLE) {
					toggleResultButton.setText("Show");
					outputTextView.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	private void startClassification() {
		Instances newData = null;
		int index = classifierSpinner.getSelectedItemPosition();
		try {
			DataSource source;
			File f = new File(Environment.getExternalStorageDirectory(),
					"data.arff");
			source = new DataSource(f.getAbsolutePath());
			Instances data = source.getDataSet();
			if (data.classIndex() == -1) {
				data.setClassIndex(data.numAttributes() - 1);
			}
			outputTextView.setText("Classes: " + data.numClasses() + '\n'
					+ "Attributes: " + data.numAttributes() + '\n'
					+ "Instances: " + data.numInstances());
			// Filtering
			Remove remove = new Remove();
			remove.setAttributeIndicesArray(checkCheckboxes());
			remove.setInvertSelection(true);
			remove.setInputFormat(data);
			newData = Filter.useFilter(data, remove);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			outputTextView.setText(e.getMessage());
		}
		switch (index) {
		case 0:
			startRandomForest(newData);
			break;
		case 1:
			startJ48(newData);
			break;
		case 2:
			startNaiveBayes(newData);
			break;
		case 3:
			startSVM(newData);
			break;
		}
		progressBar.setVisibility(View.INVISIBLE);
		startButton.setEnabled(true);
		float seconds = (System.currentTimeMillis() - startTime) / 1000.0f;
		durationTextView.setText(String.format("%.3f", seconds) + " seconds");

	}

	private int[] checkCheckboxes() {
		ArrayList<Integer> attributesList = new ArrayList<Integer>();
		for (int i = 0; i < 46; i++) {
			attributesList.add(i);
		}
		if (!xCheckbox.isChecked()) {
			for (int i = 2; i <= 11; i++) {
				attributesList.set(i, null);
			}
		}
		if (!yCheckbox.isChecked()) {
			for (int i = 12; i <= 21; i++) {
				attributesList.set(i, null);
			}
		}
		if (!zCheckbox.isChecked()) {
			for (int i = 22; i <= 31; i++) {
				attributesList.set(i, null);
			}
		}
		if (!AvgCheckbox.isChecked()) {
			for (int i = 32; i <= 34; i++) {
				attributesList.set(i, null);
			}
		}
		if (!PeakCheckbox.isChecked()) {
			for (int i = 35; i <= 37; i++) {
				attributesList.set(i, null);
			}
		}
		if (!AbsDevCheckbox.isChecked()) {
			for (int i = 38; i <= 40; i++) {
				attributesList.set(i, null);
			}
		}
		if (!StandDevCheckbox.isChecked()) {
			for (int i = 41; i <= 43; i++) {
				attributesList.set(i, null);
			}
		}
		int[] result = toIntArray(attributesList);
		return result;
	}

	private int[] toIntArray(List<Integer> list) {
		list.removeAll(Collections.singleton(null));
		int[] ret = new int[list.size()];
		for (int i = 0; i < ret.length; i++)
			ret[i] = list.get(i);
		return ret;
	}

	private void startSVM(Instances data) {
		try {
			outputTextView.setText("Processing " + data.numInstances()
					+ "data instances. Please wait...");
			SMO smo = new SMO(); // new instance of tree
			smo.buildClassifier(data); // build classifier
			Evaluation eval = new Evaluation(data);
			eval.crossValidateModel(smo, data, 10, new Random(1));
			outputTextView.setText(eval.toSummaryString());
			logTextView.setText(smo.toString());
			matrixTextView.setText(eval.toMatrixString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			outputTextView.setText(e.getMessage());
		}
	}

	private void startJ48(Instances data) {
		try {
			outputTextView.setText("Processing " + data.numInstances()
					+ "data instances. Please wait...");
			J48 jtree = new J48(); // new instance of tree
			jtree.buildClassifier(data); // build classifier
			Evaluation eval = new Evaluation(data);
			eval.crossValidateModel(jtree, data, 10, new Random(1));
			outputTextView.setText(eval.toSummaryString());
			logTextView.setText(jtree.toString());
			matrixTextView.setText(eval.toMatrixString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			outputTextView.setText(e.getMessage());
		}
	}

	private void startNaiveBayes(Instances data) {
		try {
			outputTextView.setText("Processing " + data.numInstances()
					+ "data instances. Please wait...");
			NaiveBayes nb = new NaiveBayes(); // new instance of tree
			nb.buildClassifier(data); // build classifier
			Evaluation eval = new Evaluation(data);
			eval.crossValidateModel(nb, data, 10, new Random(1));
			outputTextView.setText(eval.toSummaryString());
			logTextView.setText(nb.toString());
			matrixTextView.setText(eval.toMatrixString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			outputTextView.setText(e.getMessage());
		}
	}

	private void startRandomForest(Instances data) {
		try {
			outputTextView.setText("Processing " + data.numInstances()
					+ "data instances. Please wait...");
			RandomForest rf = new RandomForest(); // new instance of tree
			rf.buildClassifier(data); // build classifier
			Evaluation eval = new Evaluation(data);
			eval.crossValidateModel(rf, data, 10, new Random(1));
			outputTextView.setText(eval.toSummaryString());
			logTextView.setText(rf.toString());
			matrixTextView.setText(eval.toMatrixString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			outputTextView.setText(e.getMessage());
		}

	}

	// so that we know something was triggered
	public void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
}
