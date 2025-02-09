package com.example.infits;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MealTrackerAddMealFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MealTrackerAddMealFragment extends Fragment {

    String url = String.format("%sgetFoodItems.php",DataFromDatabase.ipConfig);
    String url1 = String.format("%sgetFavouriteFoodItems.php", DataFromDatabase.ipConfig);
    String heading;

    private TextView title,recentTextView,FavouritesTextview,frequentTextView;;
    View underlineFrequent,underlineRecent,underlineFavourites;
    SearchView searchBreakfast;
    RecyclerView breakfastitems;
    JSONObject mainJSONobj = new JSONObject();
    JSONArray jsonArray= new JSONArray();
    RequestQueue queue,requestQueue;
    ArrayList<addmealInfo> addmealInfos = new ArrayList<>();
    ImageView calorieImgback,searchbarSetting;
    //    AddMealAdapter addMealAdapter;
    MealtrackerAddMealAdapter addMealAdapter;
    ArrayList<addmealInfo> filteredlist;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MealTrackerAddMealFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MealTrackerAddMealFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MealTrackerAddMealFragment newInstance(String param1, String param2) {
        MealTrackerAddMealFragment fragment = new MealTrackerAddMealFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_meal_tracker_add_meal, container, false);
        hooks(view);

//        Toast.makeText(requireContext(), "Addmeal", Toast.LENGTH_SHORT).show();

        breakfastitems=view.findViewById(R.id.breakfastitems);
        calorieImgback=view.findViewById(R.id.calorieImgback);
//        breakfastitems.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false));
        breakfastitems.setLayoutManager(new GridLayoutManager(requireContext(),2));

        Bundle bundle = getArguments();
        if (bundle != null) {
            // Extract the message from the bundle using the key "fragment"
            heading = bundle.getString("fragment");
        }


        //setting title
//        heading = getStringExtra("fragment");
        title.setText(heading);

        //basic actions
        AddFrequentMeal();
        calorieImgback.setOnClickListener(v -> requireActivity().onBackPressed());

        searchBreakfast.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
//                if(newText.isEmpty())
                if (TextUtils.isEmpty(newText)) searchbarSetting.setVisibility(View.VISIBLE);
                else    searchbarSetting.setVisibility(View.GONE);
                filter(newText);
                return false;
            }
        });

        recentTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UnderLineLayout(v);
                for (int i=0;i<addmealInfos.size();i++) {
                    AddRecentMeal(addmealInfos.get(i).mealname, addmealInfos.get(i).mealcalorie
                            , addmealInfos.get(i).protein, addmealInfos.get(i).carb, addmealInfos.get(i).protein);
                }
            }
        });
        frequentTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UnderLineLayout(v);
                AddFrequentMeal();

            }
        });
        FavouritesTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UnderLineLayout(v);
                AddFavouritesMeal();

            }
        });

        return view;
    }

    private void hooks(View v) {
        title = v.findViewById(R.id.title);
//        Toast.makeText(this, "title", Toast.LENGTH_SHORT).show();
        underlineFrequent = v.findViewById(R.id.underlineFrequent);
        underlineRecent = v.findViewById(R.id.underlineRecent);
        underlineFavourites = v.findViewById(R.id.underlineFavourites);
//        Toast.makeText(this, "underline", Toast.LENGTH_SHORT).show();
        recentTextView = v.findViewById(R.id.recentTextView);
        FavouritesTextview = v.findViewById(R.id.FavouritesTextview);
        frequentTextView = v.findViewById(R.id.frequentTextView);
        searchbarSetting = v.findViewById(R.id.search_bar_setting);
//        Toast.makeText(this, "textview", Toast.LENGTH_SHORT).show();
        searchBreakfast=v.findViewById(R.id.searchBreakfast);

    }

    private void filter(String text) {
//        Toast.makeText(this, "Filter called", Toast.LENGTH_SHORT).show();
        filteredlist = new ArrayList<addmealInfo>();

        for (addmealInfo item : addmealInfos) {
            if (item.mealname.toLowerCase().contains(text.toLowerCase())) {
                filteredlist.add(item);
            }
        }
        if (filteredlist.isEmpty()) {
            Log.d("Error","No Data Found");
//            Toast.makeText(getActivity(), "No Data Found..", Toast.LENGTH_SHORT).show();
        } else {
            addMealAdapter.filterList(filteredlist);
        }
    }
    private void AddRecentMeal(String name,String calorieValue,String protin,String carb,String fat){
        addmealInfos.clear();
        try {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("RecentMeal", Context.MODE_PRIVATE);

            if(sharedPreferences.contains("RecentMealInfo")) {
                JSONObject jsonObject = new JSONObject(sharedPreferences.getString("RecentMealInfo", ""));
                JSONArray jsonArray1 = jsonObject.getJSONArray("RecentMealInfo");
                for (int i = 0; i < jsonArray1.length(); i++) {
                    JSONObject jsonObject1=jsonArray1.getJSONObject(i);
                    addmealInfos.add(new addmealInfo(Integer.parseInt(jsonObject1.getString("icon")),heading,jsonObject1.getString("name"),
                            jsonObject1.getString("calorie"),jsonObject1.getString("protin"),jsonObject1.getString("carb"),
                            jsonObject1.getString("fat")));
                }
                Log.d("TAG", "AddRecentMeal: "+jsonObject.toString());
            }

            addMealAdapter=new MealtrackerAddMealAdapter(requireContext(),addmealInfos);
            breakfastitems.setAdapter(addMealAdapter);
        }catch (JSONException jsonException){
            Log.d("JSONException",jsonException.toString());
        }
    }
    private void AddFrequentMeal(){
//        Toast.makeText(this, "Freq", Toast.LENGTH_SHORT).show();
        addmealInfos.clear();
        queue = Volley.newRequestQueue(requireContext());
        StringRequest stringRequest=new StringRequest(Request.Method.GET,url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("food");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1=jsonArray.getJSONObject(i);
                    String name=jsonObject1.getString("name");
                    String calorie=jsonObject1.getString("calorie")+"  kcal";
                    String carb=jsonObject1.getString("carb");
                    String protein=jsonObject1.getString("protein");
                    String fat=jsonObject1.getString("fat");
//                    String fiber = jsonObject1.getString("fiber");
//                    Log.d("error123", name);

//                     addmealInfo a = new addmealInfo(R.drawable.donutimg_full,heading,name,calorie,carb,protein,fat);
//                     a.setFiber(fiber);

                    addmealInfos.add(new addmealInfo(R.drawable.donutimg_full,heading,name,calorie,carb,protein,fat));
//                    addmealInfos.add(a);


                }
//                Log.d("error123", addmealInfos.toString());
                addMealAdapter=new MealtrackerAddMealAdapter(requireContext(),addmealInfos);
                breakfastitems.setAdapter(addMealAdapter);
            }catch (JSONException e){
                Log.d("error123", e.toString());
                e.printStackTrace();
            }

        },error -> {
            Log.d("error123", error.toString());
            Toast.makeText(requireContext(),error.toString().trim(),Toast.LENGTH_SHORT).show();
        }){

        };
        queue.add(stringRequest);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    }
    private void AddFavouritesMeal(){
        addmealInfos.clear();
        requestQueue = Volley.newRequestQueue(requireContext());
        StringRequest stringRequest1 = new StringRequest(Request.Method.POST, url1, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("favouriteFoodItems");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1=jsonArray.getJSONObject(i);
                    String name=jsonObject1.getString("nameofFoodItem");
                    String calorie=jsonObject1.getString("calorie")+"  kcal";
                    String carb=jsonObject1.getString("carb");
                    String protein=jsonObject1.getString("protein");
                    String fat=jsonObject1.getString("fat");
//                    String fiber = jsonObject1.getString("fiber");
                    addmealInfos.add(new addmealInfo(R.drawable.hotdog,heading,name,calorie,carb,protein,fat));
                }
                addMealAdapter=new MealtrackerAddMealAdapter(requireContext(),addmealInfos);
                breakfastitems.setAdapter(addMealAdapter);
            }catch (Exception e){
                Log.d("JSONException",e.toString());
            }
        },error -> {
            Toast.makeText(requireContext(), error.toString(), Toast.LENGTH_SHORT).show();
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> data = new HashMap<>();
                data.put("clientuserID", DataFromDatabase.clientuserID);
                return data;
            }
        };
//        for(int j=0;j<addmealInfos.size();j++){
//            if(addmealInfos.get(j).mealname.indexOf())
//        }
        requestQueue.add(stringRequest1);

    }
    private void UnderLineLayout(View view){
        switch (view.getId()){
            case R.id.frequentTextView:
                underlineFrequent.setVisibility(View.VISIBLE);
                underlineRecent.setVisibility(View.INVISIBLE);
                underlineFavourites.setVisibility(View.INVISIBLE);
                break;
            case R.id.recentTextView:
                underlineRecent.setVisibility(View.VISIBLE);
                underlineFrequent.setVisibility(View.INVISIBLE);
                underlineFavourites.setVisibility(View.INVISIBLE);
                break;
            case R.id.FavouritesTextview:
                underlineFavourites.setVisibility(View.VISIBLE);
                underlineRecent.setVisibility(View.INVISIBLE);
                underlineFrequent.setVisibility(View.INVISIBLE);
                break;
            default:
                underlineFrequent.setVisibility(View.VISIBLE);
                underlineRecent.setVisibility(View.INVISIBLE);
                underlineFavourites.setVisibility(View.INVISIBLE);
                break;
        }
    }

}