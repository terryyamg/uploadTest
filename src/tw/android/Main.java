package tw.android;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import tw.android.AndroidMultiPartEntity.ProgressListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Main extends Activity {

	private ProgressBar progressBar;
	private String FILE_UPLOAD_URL,filePath = null;
	private TextView txtPercentage;
	private Button btnUpload;
	long totalSize = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		txtPercentage = (TextView) findViewById(R.id.txtPercentage);
		btnUpload = (Button) findViewById(R.id.btnUpload);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		/*上傳server網址*/
		FILE_UPLOAD_URL= "http://xxxxx.3eeweb.com/AndroidFileUpload/fileUpload.php";
		
		String folder="life"; //資料夾名稱
		String fileName="IMG_20150226_161243.jpg"; //要上傳的手機裡的檔案-檔案名稱
		
		filePath = "/storage/sdcard0/"+folder+"/"+fileName; // 檔案路徑位置
		
		/*上傳按鈕*/
		btnUpload.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// 上傳至後端sever
				new UploadFileToServer().execute();
			}
		});

	}

	/* 上傳至後端sever */
	private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
		/* 背景執行前的顯示動作 */
		@Override
		protected void onPreExecute() {
			// 設定進度條初始為0
			progressBar.setProgress(0);
			super.onPreExecute();
		}

		/* 背景執行中的顯示動作 */
		@Override
		protected void onProgressUpdate(Integer... progress) {
			// 顯示進度條
			progressBar.setVisibility(View.VISIBLE);

			// 更新進度療
			progressBar.setProgress(progress[0]);

			// 更新顯示文字百分比
			txtPercentage.setText(String.valueOf(progress[0]) + "%");
		}

		/* 背景執行的工作 */
		@Override
		protected String doInBackground(Void... params) {
			return uploadFile();
		}

		@SuppressWarnings("deprecation")
		private String uploadFile() {
			String responseString = null;

			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(FILE_UPLOAD_URL);

			try {
				AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
						new ProgressListener() {

							public void transferred(long num) {
								publishProgress((int) ((num / (float) totalSize) * 100));
							}
						});

				File sourceFile = new File(filePath);

				
				// Adding file data to http body
				entity.addPart("image", new FileBody(sourceFile));
				/* 攜帶參數(非必要) */
				// Extra parameters if you want to pass to server
//				entity.addPart("website",
//						new StringBody("www.androidhive.info"));
//				entity.addPart("email", new StringBody("abc@gmail.com"));

				totalSize = entity.getContentLength();
				httppost.setEntity(entity);

				// Making server call
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity r_entity = response.getEntity();

				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					// Server response
					responseString = EntityUtils.toString(r_entity);
				} else {
					responseString = "Error occurred! Http Status Code: "
							+ statusCode;
				}

			} catch (ClientProtocolException e) {
				responseString = e.toString();
			} catch (IOException e) {
				responseString = e.toString();
			}

			return responseString;

		}

		/* 背景執行完成後的顯示動作 */
		@Override
		protected void onPostExecute(String result) {
			Log.e("END", "Response from server: " + result);

			/* 完成後alert顯示訊息 */
			showAlert(result);

			super.onPostExecute(result);
		}

	}

	/* 完成後alert顯示訊息 */
	private void showAlert(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message).setTitle("Response from Servers")
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// do nothing
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

}