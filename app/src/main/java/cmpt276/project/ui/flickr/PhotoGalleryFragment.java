package cmpt276.project.ui.flickr;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView;
import android.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import cmpt276.project.R;
import cmpt276.project.model.flickr.FlickrFetchr;
import cmpt276.project.model.flickr.GalleryItem;
import cmpt276.project.model.flickr.QueryPreferences;
import cmpt276.project.model.flickr.ThumbnailDownloader;

/**
 * This fragment displays the images available to the user, and downloads and saves them when a user clicks them.
 */
public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";
    public static final String FILE_FLICKR_DRAWABLE = "flickrDrawable";
    public static final String LOG_SAVE_IMAGE = "SAVE_IMAGE";

    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        updateItems();

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
            new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
                @Override
                public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {
                    Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                    photoHolder.bindDrawable(drawable);
                }
            }
        );
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = v.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        setupAdapter();

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.fragment_photo_gallery, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    Log.d(TAG, "QueryTextSubmit: " + s);
                    QueryPreferences.setStoredQuery(getActivity(), s);
                    updateItems();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    Log.d(TAG, "QueryTextChange: " + s);
                    return false;
                }
            });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageView mItemImageView;

        public PhotoHolder(final View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
        }

        public void registerClicks(final int position){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("caption: " + mItems.get(position).getCaption());
                    System.out.println("ID: " + mItems.get(position).getId());
                    System.out.println("url: " + mItems.get(position).getUrl());

                    itemView.setForeground(getResources().getDrawable(R.drawable.gallery_item_selected));

                    // https://stackoverflow.com/questions/39897338/how-to-get-current-time-stamp-in-android/39897615
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
                    sdf.setTimeZone(TimeZone.getTimeZone("PDT"));

                    String bitmapUrl = mItems.get(position).getUrl();
                    String imageName = mItems.get(position).getCaption();

                    String temp = sdf.format(new Date());
                    imageName += temp;
                    imageName += ".png";

                    new DownloadFile(bitmapUrl, imageName).execute();
                }
            });
        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_gallery, viewGroup, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            Drawable placeholder = getResources().getDrawable(R.drawable.magnifying_glass);
            photoHolder.bindDrawable(placeholder);
            photoHolder.registerClicks(position);
            mThumbnailDownloader.queueThumbnail(photoHolder, galleryItem.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,List<GalleryItem>> {

        private String mQuery;

        public FetchItemsTask(String query) {
            mQuery = query;
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {

            if (mQuery == null) {
                return new FlickrFetchr().fetchRecentPhotos();
            } else {
                return new FlickrFetchr().searchPhotos(mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            mItems = items;
            setupAdapter();
        }
    }

    // Got help and code from: https://stackoverflow.com/questions/54996665/how-to-save-downloaded-file-in-internal-storage-in-android-studio
    // Downloads and saves images to /data/data/cmpt276.project/app_flickrDrawable
    private class DownloadFile extends AsyncTask<String, Void, Bitmap> {

        String url;
        String name;

        DownloadFile(String URL, String name) {
            this.url = URL;
            this.name = name;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... URL) {

            System.out.println("IMAGE URL: " + url);
            System.out.println("IMAGE CAPTION: " + name);

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(url).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);

                if (bitmap == null) return null;

                ContextWrapper cw = new ContextWrapper(getContext());
                File directory = cw.getDir(FILE_FLICKR_DRAWABLE, Context.MODE_PRIVATE);

                File mypath = new File(directory, name);

                FileOutputStream fos ;
                try {
                    fos = new FileOutputStream(mypath);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                } catch (Exception e) {
                    Log.e(LOG_SAVE_IMAGE, e.getMessage(), e);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            System.out.println("SUCCESSFULLY SAVED");
        }
    }

}