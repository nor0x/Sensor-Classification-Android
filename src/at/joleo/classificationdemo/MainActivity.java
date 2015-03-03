package at.joleo.classificationdemo;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getOverflowMenu();
		
	}

	// inflate for action bar
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	// handle click events for action bar items
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

	    switch (item.getItemId()) {
	    
	        case R.id.classificationAction:
	            Intent intent1 = new Intent(MainActivity.this, ClassificationActivity.class);
	            startActivity(intent1);
	            return true;
	            
	        case R.id.collectionAction:
	            Intent intent2 = new Intent(MainActivity.this, CollectionActivity.class);
	            startActivity(intent2);
	            return true;
	            
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	// put the other two menu on the three dots (overflow)
	private void getOverflowMenu() {

	    try {
	    	
	       ViewConfiguration config = ViewConfiguration.get(this);
	       java.lang.reflect.Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	       if(menuKeyField != null) {
	           menuKeyField.setAccessible(true);
	           menuKeyField.setBoolean(config, false);
	       }
	   } catch (Exception e) {
	       e.printStackTrace();
	   }
	    
	 }
	
	// so that we know something was triggered
	public void showToast(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
}
