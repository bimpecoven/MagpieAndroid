package com.magpie.magpie.BadgePage;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;

import com.magpie.magpie.CollectionUtils.Element;
import com.magpie.magpie.InfoAndShare.InfoPage;
import com.magpie.magpie.NavActivity;
import com.magpie.magpie.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BadgePage.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BadgePage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BadgePage extends Fragment implements AdapterView.OnItemSelectedListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ArrayList<Element> displayElements;
    private ListView badgeList;
    private GridView badgeGrid;
    private CustomBadgeListAdapter cbla;
    private CustomBadgeListAdapter cbga;
    private Spinner filter;
    private NavActivity navActivity;
    private ZipFile imgZIP;
    private String type;

    public BadgePage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BadgePage.
     */
    // TODO: Rename and change types and number of parameters
    public static BadgePage newInstance(String param1, String param2) {
        BadgePage fragment = new BadgePage();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /*
     * Method onCreate: Handles the creation of the navActivity instance. The
     * navActivity field will be used to reference the current active collection and
     * the associated data inside it.
     *
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        type = b.getString("Type");
        navActivity = (NavActivity)getActivity();
        navActivity.setTitle(navActivity.getActiveCollection().getName());
        imgZIP = navActivity.getActiveCollection().getPicZip();
    }

    /*
     * Method onCreateView: Creates the visual that the user interacts with.
     * Note that there are two different lists "active" when the fragment is launched.
     * As a result, both are instantiated on launch, and never again until relaunch.
     *
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Assigns the images residing in the associated ZIP file to each element
        addImagesToElements();
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_badge_page, container, false);
        badgeGrid = (GridView) v.findViewById(R.id.BadgeGridView);
        badgeList = (ListView) v.findViewById(R.id.BadgeListView);
        //filter = (Spinner) v.findViewById(R.id.BadgeFilter);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this.getContext(), R.array.BadgeFilterArray, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //filter.setAdapter(spinnerAdapter);
        //filter.setOnItemSelectedListener(this);
        navActivity.getFilter().setAdapter(spinnerAdapter);
        navActivity.getFilter().setOnItemSelectedListener(this);
        displayElements = new ArrayList<>();
        displayElements.addAll(navActivity.getActiveCollection().getCollectionElements());
        //Each list has the same adapter, with the string provided determining which type it is.
        cbla = new CustomBadgeListAdapter(this, displayElements, "List", navActivity.getActiveCollection().getPicZip(), navActivity);
        cbga = new CustomBadgeListAdapter(this, displayElements, "Grid", navActivity.getActiveCollection().getPicZip(), navActivity);
        badgeList.setAdapter(cbla);
        badgeGrid.setAdapter(cbga);
        badgeList.setOnItemClickListener(onLVClick);
        badgeGrid.setOnItemClickListener(onGVClick);
        cbla.notifyDataSetChanged();
        cbga.notifyDataSetChanged();
        whatWasClicked();
        return v;
    }

    /*
     * AdapterView.OnItemClickListener onLVClick: handles the selecting of any ListView Item and performs and action on it.
     * The action in this case is to start the InfoPage fragment. The setSelectedElement method in Collection is called.
     * Allowing for a determination of what Element was selected.
     *
     */

    AdapterView.OnItemClickListener onLVClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            //navActivity.getActiveCollection().setSelectedElement(i);
            navActivity.setActiveElement(navActivity.getActiveCollection().getElementAtIndex(i));
            Fragment fr = new InfoPage();
            navActivity.startNewFragment(fr);

        }
    };

    /*
     * AdapterView.OnItemClickListener onGVClick: handles the selecting of any GridView Item and performs and action on it.
     * The action in this case is to start the InfoPage fragment. The setSelectedElement method in Collection is called.
     * Allowing for a determination of what Element was selected.
     *
     */

    AdapterView.OnItemClickListener onGVClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            navActivity.getActiveCollection().setSelectedElement(i);
            Fragment fr = new InfoPage();
            navActivity.startNewFragment(fr);
        }
    };

    /*
     * Method onItemSelected: Calls the respective method when the user selects a filter in the Spinner.
     *
     */

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position == 0){
            filterUnachieved();
        }
        else if(position == 1){
            filterAchieved();
        }
        else {
            filterTime(position);
        }
    }

    /*
     * Method onNothingSelected: Unused, required for Spinner implementation
     *
     */

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /*
     * Method filterUnachieved: Will display only badges that have the isCollected set to false.
     *
     */

    private void filterUnachieved(){
        displayElements.clear();
        for(Element e : navActivity.getActiveCollection().getCollectionElements()){
            if(!e.isCollected()){
                displayElements.add(e);
            }
        }
        cbla.notifyDataSetChanged();
        cbga.notifyDataSetChanged();
    }

    /*
     * Method filterAchieved: Will display only badges that have the isCollected set to true.
     *
     */

    private void filterAchieved(){
        displayElements.clear();
        for(Element e : navActivity.getActiveCollection().getCollectionElements()){
            if(e.isCollected()){
                displayElements.add(e);
            }
        }
        cbla.notifyDataSetChanged();
        cbga.notifyDataSetChanged();
    }

    /*
     * Method filterTime: Based on the selection, the list will only display badges in a particular time away.
     * These are:
     *      Less than five minutes
     *      Less than ten minutes
     *      Less than fifteen minutes
     *      More than fifteen minutes
     *
     */

    public void filterTime(int pos) {
        if (pos == 2) {
            displayElements.clear();
            for (Element e : navActivity.getActiveCollection().getCollectionElements()) {
                if (Double.compare(e.getTime(), 5.0) < 0) {
                    displayElements.add(e);
                }
            }
        }
        else if(pos == 3){
            displayElements.clear();
            for(Element e : navActivity.getActiveCollection().getCollectionElements()){
                if(Double.compare(e.getTime(), 10.0) < 0){
                    displayElements.add(e);
                }
            }
        }
        else if(pos == 4){
            displayElements.clear();
            for(Element e : navActivity.getActiveCollection().getCollectionElements()){
                if(Double.compare(e.getTime(), 15.0) < 0){
                    displayElements.add(e);
                }
            }
        }
        else {
            displayElements.clear();
            for(Element e : navActivity.getActiveCollection().getCollectionElements()){
                if(Double.compare(e.getTime(), 15.0) >= 0){
                    displayElements.add(e);
                }
            }
        }
        cbla.notifyDataSetChanged();
        cbga.notifyDataSetChanged();
    }

    /*
     * Method addImagesToElements: Adds all images to the elements.
     * Due to the uncertainty of how the CMS is storing and naming images, currently it takes them element by
     * element and assigns them blindly.
     *
     */

    private void addImagesToElements() {
        int reqHeight;
        int reqWidth;
        Bitmap bm;
        if(imgZIP != null) {
            Enumeration<? extends ZipEntry> entries = imgZIP.entries();
            ZipEntry curImage = entries.nextElement();
            for (Element e : navActivity.getActiveCollection().getCollectionElements()) {
                try {
                    InputStream zin = imgZIP.getInputStream(curImage);
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inJustDecodeBounds = true;
                    bm = BitmapFactory.decodeStream(zin, null, opt);
                    reqHeight = 75;
                    reqWidth = 75;
                    opt.inSampleSize = calculateSampleSize(opt, reqHeight, reqWidth);
                    opt.inJustDecodeBounds = false;
                    zin = imgZIP.getInputStream(curImage);
                    bm = BitmapFactory.decodeStream(zin, null, opt);
                    e.setBadge(bm);
                    if (entries.hasMoreElements()) {
                        curImage = entries.nextElement();
                        String elementName = curImage.getName();
                        String extension = elementName.substring(elementName.length() - 3);
                        if (extension.compareTo("zip") == 0) {
                            curImage = imgZIP.entries().nextElement();
                        }
                    }
                } catch (IOException ioe) {
                    Log.d("ZIPREADINGIO", ioe.getMessage());
                }
            }
        }
    }

    //Taken straight from the Android Developers site -- URL: https://developer.android.com/topic/performance/graphics/load-bitmap.html
    private int calculateSampleSize(BitmapFactory.Options opt, int reqHeight, int reqWidth) {
        final int height = opt.outHeight;
        final int width = opt.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth){
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth)
            {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void whatWasClicked(){

        //if(type.compareTo("List") == 0){
        if (navActivity.getSelectedView() == R.id.radio_list_view) {
            badgeList.setVisibility(View.VISIBLE);
            badgeGrid.setVisibility(View.INVISIBLE);
        }
        //else if(type.compareTo("Grid") == 0){
        else if (navActivity.getSelectedView() == R.id.radio_grid_view) {
            badgeList.setVisibility(View.INVISIBLE);
            badgeGrid.setVisibility(View.VISIBLE);
        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    /*
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    */
}
