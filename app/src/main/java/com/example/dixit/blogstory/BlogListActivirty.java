package com.example.dixit.blogstory;

import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class BlogListActivirty extends ListActivity {

    protected String[] mBlogPostTitles;
    public static final int NUMBER_OF_POSTS=20;
    public static final String TAG= BlogListActivirty.class.getSimpleName();
    protected JSONObject mBlogData;
    protected ProgressBar mProgressBar;

    private final String KEY_TITLE = "title";
    private final String KEY_AUTHOR = "author";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_list_activirty);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
        if(isNetworkAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);
            GetBlogPostsTask getBlogPostsTask = new GetBlogPostsTask();
            getBlogPostsTask.execute();
        }
        else
        {
            Toast.makeText(this, "Network is unavailable !", Toast.LENGTH_LONG).show();
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo=manager.getActiveNetworkInfo();
        boolean isAvailable=false;
        if(networkinfo!=null && networkinfo.isConnected())
        {
            isAvailable=true;
        }
        return isAvailable;
    }
    public void handleBlogResponse()
    {
        mProgressBar.setVisibility(View.INVISIBLE);
        if(mBlogData==null)
        {
            updateDisplayForError();
        }
        else
        {  try {
            JSONArray jsonPosts=mBlogData.getJSONArray("posts");
            mBlogPostTitles= new String[jsonPosts.length()];
            ArrayList<HashMap<String, String>> blogPosts =
                    new ArrayList<HashMap<String, String>>();
            for(int i=0;i<jsonPosts.length();i++) {
                JSONObject post = jsonPosts.getJSONObject(i);
                String title = post.getString(KEY_TITLE);
                title=Html.fromHtml(title).toString();
                String author = post.getString(KEY_AUTHOR);
                author = Html.fromHtml(author).toString();
                HashMap<String, String> blogPost = new HashMap<String, String>();
                blogPost.put(KEY_TITLE, title);
                blogPost.put(KEY_AUTHOR, author);

                blogPosts.add(blogPost);

            }
            String[] keys = { KEY_TITLE, KEY_AUTHOR };
            int[] ids = { android.R.id.text1, android.R.id.text2 };
            SimpleAdapter adapter = new SimpleAdapter(this, blogPosts,
                    android.R.layout.simple_list_item_2,
                    keys, ids);
            setListAdapter(adapter);
        }
        catch (JSONException e){
            Log.e(TAG,"Exception caught!", e);
        }
        }
    }
    private void updateDisplayForError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title));
        builder.setMessage(getString(R.string.error_message));
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();

        TextView emptyTextView = (TextView) getListView().getEmptyView();
        emptyTextView.setText(getString(R.string.no_items));

    }

    private class GetBlogPostsTask extends AsyncTask<Object,Void,JSONObject >{
        @Override
        protected JSONObject doInBackground(Object... arg0){
            int responseCode=-1;
            JSONObject jsonResponse=null;
            try {
                URL blogFeedUrl = new URL("http://blog.teamtreehouse.com/api/get_recent_summary/?count=" + NUMBER_OF_POSTS);
                HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
                connection.connect();

                responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    Reader reader = new InputStreamReader(inputStream);
                    int contentLength = connection.getContentLength();
                    char[] charArray = new char[contentLength];
                    reader.read(charArray);
                    String responseData = new String(charArray);
                    jsonResponse= new JSONObject(responseData);
                }
                else {
                    Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
                }
            }
            catch (MalformedURLException e)
            {
                Log.e(TAG, "Exception caught: ",e);

            }
            catch (IOException e)
            {
                Log.e(TAG, "Exception caught: ",e);

            }
            catch (Exception e)
            {
                Log.e(TAG, "Exception caught: ",e);

            }
            return jsonResponse;
        }
         @Override
        protected void onPostExecute(JSONObject result)
          {
            mBlogData=result;
              handleBlogResponse();
          }
    }
}
